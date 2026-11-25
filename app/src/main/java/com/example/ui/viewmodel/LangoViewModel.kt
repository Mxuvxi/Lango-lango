package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.CandidatureEntity
import com.example.data.local.entity.CandidatureRecueEntity
import com.example.data.local.entity.CoachMessageEntity
import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.LangueCode
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.PortfolioEntity
import com.example.data.local.entity.ProfilEntity
import com.example.data.local.entity.ProgressionEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.UtilisateurEntity
import com.example.data.local.entity.VerificationEntity
import com.example.data.preferences.LangoPreferences
import com.example.data.remote.FirebaseSyncRepository
import com.example.data.repository.CoachStreamEvent
import com.example.data.repository.LangoAiRepository
import com.example.data.repository.LangoRepository
import com.example.ui.util.ConnectivityObserver
import com.example.ui.util.ImageCompressionUtil
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "LangoViewModel"

sealed interface CoachAiUiState {
    data object Idle : CoachAiUiState
    data object Loading : CoachAiUiState
    data class Streaming(val partialText: String) : CoachAiUiState
    data class Error(val message: String) : CoachAiUiState
}

sealed interface LangoAppState {
    data object Loading : LangoAppState
    data class Ready(
        val prefs: LangoPreferences,
        val user: UtilisateurEntity?,
        val profile: ProfilEntity?,
        val competences: List<CompetenceEntity>,
        val contenus: List<ContenuPedagogiqueEntity>,
        val progressions: List<ProgressionEntity>,
        val badges: List<BadgeEntity>,
        val verifications: List<VerificationEntity>,
        val portfolio: List<PortfolioEntity>,
        val opportunites: List<OpportuniteEntity>,
        val candidatures: List<CandidatureEntity>,
        val candidaturesRecues: List<CandidatureRecueEntity>,
        val caisseOperations: List<CaisseOperationEntity>,
        val devisList: List<DevisEntity>,
        val coachMessages: List<CoachMessageEntity>,
        val coachAiState: CoachAiUiState,
        val notifications: List<NotificationEntity>,
        val pendingSyncCount: Int,
        val syncOutboxItems: List<SyncOutboxEntity>,
        val isDeviceOnline: Boolean,
        val selectedCityFilter: String?,
        val selectedTypeFilter: String?,
        val statusBanner: String?
    ) : LangoAppState
}

class LangoViewModel(
    private val appContext: Context,
    private val repository: LangoRepository,
    private val cloudSync: FirebaseSyncRepository,
    private val aiRepository: LangoAiRepository,
    private val connectivityObserver: ConnectivityObserver,
    @Suppress("unused") private val currentUserId: String
) : ViewModel() {

    private val selectedCityFilter = MutableStateFlow<String?>(null)
    private val selectedTypeFilter = MutableStateFlow<String?>(null)
    private val statusBanner = MutableStateFlow<String?>(null)
    private val coachAiState = MutableStateFlow<CoachAiUiState>(CoachAiUiState.Idle)

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
        viewModelScope.launch {
            cloudSync.observeOpportunites()
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val rootError = firestoreError ?: error
                    Log.w(TAG, "Error observing cloud opportunites", rootError)
                }
                .collect { cloudItems ->
                    repository.mergeCloudOpportunites(cloudItems)
                }
        }
        viewModelScope.launch {
            combine(
                connectivityObserver.observeIsConnected(),
                repository.preferences,
                repository.pendingSyncCount
            ) { online, prefs, pending ->
                online && !prefs.isOfflineSimulated && pending > 0
            }.collect { shouldSync ->
                if (shouldSync) {
                    performCloudSync()
                }
            }
        }
    }

    private val coreUserFlow = combine(
        repository.preferences,
        repository.currentUser,
        repository.currentProfile,
        repository.pendingSyncCount
    ) { prefs, user, profile, pending ->
        Quad(prefs, user, profile, pending)
    }

    private val catalogFlow = combine(
        repository.competences,
        repository.contenus,
        repository.progressions,
        repository.opportunites
    ) { competences, contenus, progressions, opportunites ->
        Quad(competences, contenus, progressions, opportunites)
    }

    private val proofFlow = combine(
        repository.badges,
        repository.verifications,
        repository.portfolio,
        repository.candidatures
    ) { badges, verifications, portfolio, candidatures ->
        Quad(badges, verifications, portfolio, candidatures)
    }

    private val businessFlow = combine(
        repository.candidaturesRecues,
        repository.caisseOperations,
        repository.devisList,
        repository.coachMessages
    ) { receivedCands, caisseOps, devis, coachMsgs ->
        Quad(receivedCands, caisseOps, devis, coachMsgs)
    }

    private val syncAndFilterFlow = combine(
        repository.notifications,
        repository.syncOutboxItems,
        connectivityObserver.observeIsConnected(),
        selectedCityFilter
    ) { notifications, outbox, deviceOnline, cityFilter ->
        Quad(notifications, outbox, deviceOnline, cityFilter)
    }

    private val uiExtrasFlow = combine(
        selectedTypeFilter,
        statusBanner,
        coachAiState
    ) { type, banner, aiState ->
        Triple(type, banner, aiState)
    }

    val uiState: StateFlow<LangoAppState> = combine(
        coreUserFlow,
        catalogFlow,
        proofFlow,
        combine(businessFlow, syncAndFilterFlow) { biz, sync -> biz to sync },
        uiExtrasFlow
    ) { core, catalog, proof, bizAndSync, extras ->
        val biz = bizAndSync.first
        val syncExtra = bizAndSync.second
        LangoAppState.Ready(
            prefs = core.first,
            user = core.second,
            profile = core.third,
            pendingSyncCount = core.fourth,
            competences = catalog.first,
            contenus = catalog.second,
            progressions = catalog.third,
            opportunites = catalog.fourth,
            badges = proof.first,
            verifications = proof.second,
            portfolio = proof.third,
            candidatures = proof.fourth,
            candidaturesRecues = biz.first,
            caisseOperations = biz.second,
            devisList = biz.third,
            coachMessages = biz.fourth,
            notifications = syncExtra.first,
            syncOutboxItems = syncExtra.second,
            isDeviceOnline = syncExtra.third,
            selectedCityFilter = syncExtra.fourth,
            selectedTypeFilter = extras.first,
            statusBanner = extras.second,
            coachAiState = extras.third
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LangoAppState.Loading
    )

    fun selectLanguage(langue: String) {
        viewModelScope.launch {
            repository.setLangue(langue)
        }
    }

    fun registerUser(
        prenom: String,
        telephone: String,
        ville: String,
        quartier: String,
        role: String,
        languePref: String,
        consentAccepted: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.registerUser(
                prenom = prenom,
                telephone = telephone,
                ville = ville,
                quartier = quartier,
                role = role,
                languePref = languePref,
                consentementLoi24001 = consentAccepted
            )
            syncPassportSnapshotIfOnline()
            onComplete()
        }
    }

    fun saveDiagnostic(
        niveau: String,
        objectif: String,
        metier: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveDiagnostic(niveau, objectif, metier)
            syncPassportSnapshotIfOnline()
            onComplete()
        }
    }

    fun togglePackDownload(contenu: ContenuPedagogiqueEntity) {
        viewModelScope.launch {
            repository.togglePackDownloaded(contenu.id, !contenu.estTelecharge)
        }
    }

    fun saveLessonProgress(
        contenuId: String,
        etape: Int,
        totalEtapes: Int,
        scoreQuiz: Int = 0,
        completed: Boolean = false,
        onBadgeAwarded: (BadgeEntity?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val badge = repository.saveLessonProgressAndAwardBadge(
                contenuId = contenuId,
                etapeActuelle = etape,
                totalEtapes = totalEtapes,
                scoreQuiz = scoreQuiz,
                estTermine = completed
            )
            onBadgeAwarded(badge)
        }
    }

    fun addPortfolioWork(
        competenceId: String,
        titre: String,
        description: String,
        pickedUri: Uri?
    ) {
        viewModelScope.launch {
            val savedPath = if (pickedUri != null) {
                ImageCompressionUtil.compressAndSavePortfolioImage(appContext, pickedUri).orEmpty()
            } else {
                ""
            }
            repository.addPortfolioWork(
                competenceId = competenceId,
                titre = titre,
                descriptionCourte = description,
                localPhotoPath = savedPath
            )
            syncPassportSnapshotIfOnline()
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = if (lang == LangueCode.SG) {
                "Kua ti mo a lï na portfolio awe"
            } else {
                "Réalisation ajoutée au portfolio hors-ligne"
            }
        }
    }

    fun deletePortfolioWork(work: PortfolioEntity) {
        viewModelScope.launch {
            repository.deletePortfolioWork(work)
            syncPassportSnapshotIfOnline()
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = if (lang == LangueCode.SG) {
                "Kua a lungula na portfolio awe"
            } else {
                "Réalisation supprimée du portfolio"
            }
        }
    }

    fun updateProfileDetails(
        prenom: String,
        telephone: String,
        role: String,
        ville: String,
        quartier: String,
        metierPrincipal: String,
        niveauInitial: String,
        objectif: String,
        bioCourte: String,
        disponible: Boolean,
        pickedPhotoUri: Uri?,
        removePhoto: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentReady = uiState.value as? LangoAppState.Ready
            val currentPhotoPath = currentReady?.profile?.photoProfilUri.orEmpty()
            val finalPhotoPath = when {
                pickedPhotoUri != null -> {
                    ImageCompressionUtil.compressAndSaveProfilePhoto(appContext, pickedPhotoUri)
                        ?: currentPhotoPath
                }
                removePhoto -> {
                    ImageCompressionUtil.deleteAllProfileFiles(appContext)
                    ""
                }
                else -> currentPhotoPath
            }
            repository.updateProfileDetails(
                prenom = prenom,
                telephone = telephone,
                role = role,
                ville = ville,
                quartier = quartier,
                metierPrincipal = metierPrincipal,
                niveauInitial = niveauInitial,
                objectif = objectif,
                bioCourte = bioCourte,
                disponible = disponible,
                photoProfilUri = finalPhotoPath
            )
            syncPassportSnapshotIfOnline()
            val lang = currentReady?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = if (lang == LangueCode.SG) {
                "Profil ti mo a bata awe"
            } else {
                "Profil Lango mis à jour"
            }
            onComplete()
        }
    }

    fun toggleAvailability(disponible: Boolean) {
        viewModelScope.launch {
            repository.toggleAvailability(disponible)
        }
    }

    fun requestVerification(
        badgeId: String,
        verificateurNom: String,
        verificateurTel: String,
        relation: String,
        canal: String
    ) {
        viewModelScope.launch {
            repository.requestPeerVerification(
                badgeId = badgeId,
                verificateurNom = verificateurNom,
                verificateurTel = verificateurTel,
                relation = relation,
                canal = canal
            )
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = if (lang == LangueCode.SG) {
                "Hundango tene a tokua awe"
            } else {
                "Demande de vérification enregistrée"
            }
        }
    }

    fun confirmVerification(
        verifId: String,
        note: Int,
        commentaire: String
    ) {
        viewModelScope.launch {
            val upgraded = repository.confirmVerification(verifId, note, commentaire)
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = when {
                upgraded && lang == LangueCode.SG -> "Félicitations ! Badge ti mo a ga VÉRIFIÉ (3/3)"
                upgraded -> "Bravo ! Badge certifié Vérifié (3/3)"
                lang == LangueCode.SG -> "Avis a bata na Lango awe"
                else -> "Vérification validée avec succès"
            }
        }
    }

    fun applyToOpportunity(
        opportunite: OpportuniteEntity,
        messageCourt: String,
        canalEnvoi: String
    ) {
        viewModelScope.launch {
            val result = repository.applyToOpportunity(opportunite, messageCourt, canalEnvoi)
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = when {
                result?.statut == "BROUILLON_OFFLINE" && lang == LangueCode.SG ->
                    "Iri ti mo a bata sân réseau, a yeke tokua na peko"
                result?.statut == "BROUILLON_OFFLINE" ->
                    "Candidature enregistrée (sera envoyée dès le retour du réseau)"
                lang == LangueCode.SG -> "Iri ti mo a tokua na patron awe !"
                else -> "Candidature envoyée avec ton profil Lango !"
            }
        }
    }

    fun updateReceivedCandidatureStatus(
        cand: CandidatureRecueEntity,
        newStatus: String,
        noteEmployeur: String
    ) {
        viewModelScope.launch {
            repository.updateReceivedCandidatureStatus(cand, newStatus, noteEmployeur)
            statusBanner.value = "Décision enregistrée : ${cand.candidatNom} ($newStatus)"
        }
    }

    fun addCaisseOperation(
        type: String,
        categorie: String,
        montantFcfa: Int,
        clientOuNote: String,
        moyenPaiement: String
    ) {
        viewModelScope.launch {
            val created = repository.addCaisseOperation(type, categorie, montantFcfa, clientOuNote, moyenPaiement)
            if (created != null && canPushCloud()) {
                cloudSync.upsertCaisseOperation(created)
            }
            statusBanner.value = if (type == "ENTREE") {
                "Entrée de $montantFcfa FCFA enregistrée"
            } else {
                "Sortie de $montantFcfa FCFA enregistrée"
            }
        }
    }

    fun deleteCaisseOperation(opId: String) {
        viewModelScope.launch {
            repository.deleteCaisseOperation(opId)
            if (canPushCloud()) {
                cloudSync.deleteCaisseOperation(opId)
            }
        }
    }

    fun createDevis(
        clientNom: String,
        clientTel: String,
        descriptionTravaux: String,
        montantFcfa: Int,
        acompteFcfa: Int,
        moyenPaiement: String
    ) {
        viewModelScope.launch {
            val created = repository.createDevis(
                clientNom = clientNom,
                clientTel = clientTel,
                descriptionTravaux = descriptionTravaux,
                montantFcfa = montantFcfa,
                acompteFcfa = acompteFcfa,
                moyenPaiement = moyenPaiement
            )
            if (created != null) {
                if (canPushCloud()) {
                    cloudSync.upsertDevis(created)
                }
                statusBanner.value = "Devis ${created.referenceCode} créé hors-ligne"
            }
        }
    }

    fun markDevisStatus(devis: DevisEntity, newStatus: String) {
        viewModelScope.launch {
            repository.markDevisStatus(devis, newStatus)
            if (canPushCloud()) {
                repository.getDevisByIdOnce(devis.id)?.let { updated ->
                    cloudSync.upsertDevis(updated)
                }
            }
            statusBanner.value = if (newStatus == "PAYE") {
                "Reçu ${devis.referenceCode} payé (+${devis.montantFcfa} FCFA en caisse)"
            } else {
                "Statut du devis ${devis.referenceCode} mis à jour"
            }
        }
    }

    fun deleteDevis(devisId: String) {
        viewModelScope.launch {
            repository.deleteDevis(devisId)
            if (canPushCloud()) {
                cloudSync.deleteDevis(devisId)
            }
        }
    }

    fun sendCoachQuestion(question: String) {
        val cleanQuestion = question.trim()
        if (cleanQuestion.isBlank()) return
        val ready = uiState.value as? LangoAppState.Ready ?: return
        val lang = ready.prefs.langue
        val isOffline = ready.prefs.isOfflineSimulated || !ready.isDeviceOnline

        if (isOffline) {
            coachAiState.value = CoachAiUiState.Error(
                if (lang == LangueCode.SG) {
                    "Réseau a yeke pepe. Coach IA a hunda connexion Internet, me mo lingbi ti diko akua ti giriri."
                } else {
                    "Mode hors-ligne actif : le Coach IA nécessite une connexion Internet pour une nouvelle question, mais tout ton historique reste consultable ci-dessus."
                }
            )
            return
        }

        val tradeLabel = ready.competences
            .firstOrNull { it.id == ready.profile?.metierPrincipal }
            ?.let { if (lang == LangueCode.SG) it.nomSg else it.nomFr }
            .orEmpty()
        val userName = ready.user?.prenom.orEmpty()
        val city = ready.user?.ville.orEmpty()

        viewModelScope.launch {
            coachAiState.value = CoachAiUiState.Loading
            try {
                val history = repository.getCoachMessagesOnce()
                repository.saveCoachMessage(
                    role = "USER",
                    texte = cleanQuestion,
                    langue = lang,
                    metierContext = tradeLabel
                )
                aiRepository.streamAdvice(
                    question = cleanQuestion,
                    langue = lang,
                    userName = userName,
                    tradeLabel = tradeLabel,
                    city = city,
                    recentHistory = history
                ).collect { event ->
                    when (event) {
                        is CoachStreamEvent.Partial -> {
                            coachAiState.value = CoachAiUiState.Streaming(event.fullText)
                        }
                        is CoachStreamEvent.Completed -> {
                            repository.saveCoachMessage(
                                role = "MODEL",
                                texte = event.fullText,
                                langue = lang,
                                metierContext = tradeLabel
                            )
                            coachAiState.value = CoachAiUiState.Idle
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Coach IA timed out", e)
                coachAiState.value = CoachAiUiState.Error(
                    "Délai d'attente dépassé (60s). Réessaie avec une question plus courte."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Coach IA failed", e)
                coachAiState.value = CoachAiUiState.Error(
                    e.localizedMessage ?: "Erreur lors de l'appel au Coach IA."
                )
            }
        }
    }

    fun clearCoachHistory() {
        viewModelScope.launch {
            repository.clearCoachHistory()
            coachAiState.value = CoachAiUiState.Idle
        }
    }

    fun dismissCoachError() {
        if (coachAiState.value is CoachAiUiState.Error) {
            coachAiState.value = CoachAiUiState.Idle
        }
    }

    fun publishOpportunity(
        employeurNom: String,
        titre: String,
        type: String,
        ville: String,
        quartier: String,
        competenceId: String,
        remunerationFcfa: Int,
        descriptionCourte: String,
        contactTel: String,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val opp = repository.publishOpportunity(
                employeurNom = employeurNom,
                titre = titre,
                type = type,
                ville = ville,
                quartier = quartier,
                competenceId = competenceId,
                remunerationFcfa = remunerationFcfa,
                descriptionCourte = descriptionCourte,
                contactTel = contactTel
            )
            if (canPushCloud()) {
                cloudSync.publishOpportunite(opp)
            }
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = if (lang == LangueCode.SG) {
                "Fini kua a zia na Lango awe !"
            } else {
                "Offre publiée dans le fil local !"
            }
            onComplete()
        }
    }

    fun recordMobileMoneyPayment(
        operatorName: String,
        packName: String,
        amountFcfa: Int,
        phoneNumber: String,
        viaUssdFallback: Boolean
    ) {
        viewModelScope.launch {
            repository.recordMobileMoneyAction(
                operatorName = operatorName,
                packName = packName,
                amountFcfa = amountFcfa,
                phoneNumber = phoneNumber,
                viaUssdFallback = viaUssdFallback
            )
            statusBanner.value = if (viaUssdFallback) {
                "Code USSD $operatorName prêt sur ton composeur"
            } else {
                "Demande $operatorName ($amountFcfa FCFA) enregistrée"
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsRead()
        }
    }

    fun selectCityFilter(city: String?) {
        selectedCityFilter.value = city
    }

    fun selectTypeFilter(type: String?) {
        selectedTypeFilter.value = type
    }

    fun setSmsAlerts(enabled: Boolean, ville: String? = null) {
        viewModelScope.launch {
            repository.setSmsAlerts(enabled, ville)
        }
    }

    fun toggleOfflineSimulation(offline: Boolean) {
        viewModelScope.launch {
            repository.toggleOfflineSimulation(offline)
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            val synced = performCloudSync()
            val lang = (uiState.value as? LangoAppState.Ready)?.prefs?.langue ?: LangueCode.FR
            statusBanner.value = when {
                !synced && lang == LangueCode.SG -> "Réseau a yeke pepe : sango a bata na téléphone"
                !synced -> "Mode hors-ligne : données conservées localement en sécurité"
                lang == LangueCode.SG -> "Sango kue a gue na cloud awe"
                else -> "Données synchronisées avec Firebase Cloud"
            }
        }
    }

    private suspend fun canPushCloud(): Boolean {
        val prefs = repository.preferences.first()
        val isOnline = connectivityObserver.observeIsConnected().first()
        return isOnline && !prefs.isOfflineSimulated
    }

    private suspend fun syncPassportSnapshotIfOnline() {
        if (!canPushCloud()) return
        val snap = repository.getCloudSyncSnapshot() ?: return
        cloudSync.upsertUserPassport(
            user = snap.user,
            profile = snap.profile,
            badgesCount = snap.badgesCount,
            verifiedBadgesCount = snap.verifiedBadgesCount,
            portfolioCount = snap.portfolioCount
        )
    }

    private suspend fun performCloudSync(): Boolean {
        if (!canPushCloud()) return false
        val snap = repository.getCloudSyncSnapshot()
        var allOk = true
        if (snap != null) {
            if (cloudSync.upsertUserPassport(
                    user = snap.user,
                    profile = snap.profile,
                    badgesCount = snap.badgesCount,
                    verifiedBadgesCount = snap.verifiedBadgesCount,
                    portfolioCount = snap.portfolioCount
                ).isFailure
            ) {
                allOk = false
            }
            snap.caisseOperations.forEach { op ->
                if (cloudSync.upsertCaisseOperation(op).isFailure) allOk = false
            }
            snap.devisList.forEach { devis ->
                if (cloudSync.upsertDevis(devis).isFailure) allOk = false
            }
        }
        if (allOk) {
            repository.triggerSyncNow()
        }
        return allOk
    }

    fun clearBanner() {
        statusBanner.value = null
    }

    fun exportPersonalData(onJsonReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportPersonalDataJson()
            onJsonReady(json)
        }
    }

    fun eraseAllData(onDone: () -> Unit) {
        viewModelScope.launch {
            cloudSync.deleteAllUserCloudData()
            repository.wipeAllDataLoi24001()
            selectedCityFilter.value = null
            selectedTypeFilter.value = null
            statusBanner.value = null
            coachAiState.value = CoachAiUiState.Idle
            onDone()
        }
    }

    private data class Quad<A, B, C, D>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D
    )
}
