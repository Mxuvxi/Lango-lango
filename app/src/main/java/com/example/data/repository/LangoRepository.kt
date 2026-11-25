package com.example.data.repository

import android.content.Context
import com.example.data.local.LangoDatabase
import com.example.data.local.LangoDomainLogic
import com.example.data.local.LangoSeedData
import com.example.data.local.LessonContentCatalog
import java.io.File
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.CandidatureEntity
import com.example.data.local.entity.CandidatureRecueEntity
import com.example.data.local.entity.CoachMessageEntity
import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.CoursGenereIaEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.PortfolioEntity
import com.example.data.local.entity.ProfilEntity
import com.example.data.local.entity.ProgressionEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.SyncStatus
import com.example.data.local.entity.UtilisateurEntity
import com.example.data.local.entity.VerificationEntity
import com.example.data.preferences.LangoPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.CloudOpportunite
import com.example.ui.util.ImageCompressionUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.util.UUID

class LangoRepository(
    private val context: Context,
    private val db: LangoDatabase,
    private val prefsRepo: UserPreferencesRepository
) {
    private val utilisateurDao = db.utilisateurDao()
    private val formationDao = db.formationDao()
    private val preuveDao = db.preuveDao()
    private val opportuniteDao = db.opportuniteDao()
    private val caisseDevisDao = db.caisseDevisDao()
    private val coachDao = db.coachDao()
    private val coursIaDao = db.coursIaDao()
    private val syncOutboxDao = db.syncOutboxDao()

    val preferences: Flow<LangoPreferences> = prefsRepo.preferences
    val currentUser: Flow<UtilisateurEntity?> = utilisateurDao.observeCurrentUser()
    val competences: Flow<List<CompetenceEntity>> = formationDao.observeCompetences()
    val contenus: Flow<List<ContenuPedagogiqueEntity>> = formationDao.observeContenus()
    val opportunites: Flow<List<OpportuniteEntity>> = opportuniteDao.observeOpportunites()
    val candidaturesRecues: Flow<List<CandidatureRecueEntity>> = opportuniteDao.observeReceivedCandidatures()
    val pendingSyncCount: Flow<Int> = syncOutboxDao.observePendingCount()
    val syncOutboxItems: Flow<List<SyncOutboxEntity>> = syncOutboxDao.observeRecentOutbox()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentProfile: Flow<ProfilEntity?> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(null) else utilisateurDao.observeProfile(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val badges: Flow<List<BadgeEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else preuveDao.observeBadges(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val verifications: Flow<List<VerificationEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else preuveDao.observeVerifications(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val portfolio: Flow<List<PortfolioEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else preuveDao.observePortfolio(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val progressions: Flow<List<ProgressionEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else formationDao.observeProgressions(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val candidatures: Flow<List<CandidatureEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else opportuniteDao.observeCandidatures(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: Flow<List<NotificationEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else opportuniteDao.observeNotifications(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val caisseOperations: Flow<List<CaisseOperationEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else caisseDevisDao.observeCaisseOperations(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val devisList: Flow<List<DevisEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else caisseDevisDao.observeDevis(user.id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val coachMessages: Flow<List<CoachMessageEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else coachDao.observeMessages(user.id)
    }

    suspend fun ensureSeedData() {
        if (formationDao.countCompetences() == 0) {
            formationDao.insertCompetences(LangoSeedData.competences)
            formationDao.insertContenus(LangoSeedData.contenus)
            opportuniteDao.insertOpportunites(LangoSeedData.buildOpportunites())
        }
        if (opportuniteDao.countReceivedCandidatures() == 0) {
            opportuniteDao.insertReceivedCandidatures(LangoSeedData.buildReceivedCandidatures())
        }
    }

    suspend fun setLangue(langue: String) {
        prefsRepo.setLangue(langue)
        utilisateurDao.getCurrentUserOnce()?.let { user ->
            utilisateurDao.upsertUser(
                user.copy(languePref = langue, syncStatus = SyncStatus.PENDING_UPLOAD)
            )
        }
    }

    suspend fun registerUser(
        prenom: String,
        telephone: String,
        ville: String,
        quartier: String,
        role: String,
        languePref: String,
        consentementLoi24001: Boolean
    ): UtilisateurEntity {
        val now = System.currentTimeMillis()
        val userId = "usr_${UUID.randomUUID().toString().take(8)}"
        val user = UtilisateurEntity(
            id = userId,
            prenom = prenom.trim(),
            telephone = telephone.trim(),
            ville = ville.trim(),
            quartier = quartier.trim(),
            languePref = languePref,
            role = role,
            consentementLoi24001 = consentementLoi24001,
            consentDate = now,
            createdAt = now,
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        utilisateurDao.upsertUser(user)
        utilisateurDao.upsertProfile(
            ProfilEntity(
                utilisateurId = userId,
                scoreFiabilite = 25,
                updatedAt = now
            )
        )
        enqueueOutbox("UTILISATEUR", userId, "{\"prenom\":\"${user.prenom}\",\"ville\":\"${user.ville}\"}")
        opportuniteDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                utilisateurId = userId,
                titre = "Bienvenue sur Lango, ${user.prenom} !",
                corps = "Tes formations hors-ligne et les offres près de ${user.ville} sont prêtes.",
                type = "OFFRE",
                canal = "IN_APP"
            )
        )
        prefsRepo.setLangue(languePref)
        prefsRepo.setOnboardingCompleted(true)
        prefsRepo.setSmsAlerts(false, ville)
        return user
    }

    suspend fun saveDiagnostic(
        niveauInitial: String,
        objectif: String,
        metierPrincipal: String
    ) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        val now = System.currentTimeMillis()
        val profile = ProfilEntity(
            utilisateurId = user.id,
            metierPrincipal = metierPrincipal,
            niveauInitial = niveauInitial,
            objectif = objectif,
            scoreFiabilite = 30,
            updatedAt = now
        )
        utilisateurDao.upsertProfile(profile)
        enqueueOutbox("PROFIL_DIAGNOSTIC", user.id, "{\"niveau\":\"$niveauInitial\",\"objectif\":\"$objectif\"}")
        prefsRepo.setDiagnosticCompleted(true)
    }

    suspend fun togglePackDownloaded(contenuId: String, downloaded: Boolean) {
        formationDao.setPackDownloaded(contenuId, downloaded)
    }

    suspend fun saveLessonProgressAndAwardBadge(
        contenuId: String,
        etapeActuelle: Int,
        totalEtapes: Int,
        scoreQuiz: Int,
        estTermine: Boolean
    ): BadgeEntity? {
        val user = utilisateurDao.getCurrentUserOnce() ?: return null
        val contenu = formationDao.getContenuOnce(contenuId) ?: return null
        val now = System.currentTimeMillis()

        val progId = "prog_${user.id}_$contenuId"
        formationDao.upsertProgression(
            ProgressionEntity(
                id = progId,
                utilisateurId = user.id,
                contenuId = contenuId,
                etapeActuelle = etapeActuelle,
                totalEtapes = totalEtapes,
                scoreQuiz = scoreQuiz,
                estTermine = estTermine,
                updatedAt = now,
                syncStatus = SyncStatus.PENDING_UPLOAD
            )
        )
        enqueueOutbox("PROGRESSION", progId, "{\"contenuId\":\"$contenuId\",\"done\":$estTermine}")

        if (!estTermine) return null

        val existingBadge = preuveDao.getBadgeForCompetence(user.id, contenu.competenceId)
        val lessonMeta = LessonContentCatalog.getForPack(contenuId)
        val badge = existingBadge ?: BadgeEntity(
            id = "bdg_${UUID.randomUUID().toString().take(8)}",
            utilisateurId = user.id,
            competenceId = contenu.competenceId,
            titreFr = lessonMeta.badgeTitreFr,
            titreSg = lessonMeta.badgeTitreSg,
            niveau = "PRATIQUE",
            obtenuLe = now,
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        if (existingBadge == null) {
            preuveDao.insertBadge(badge)
            enqueueOutbox("BADGE", badge.id, "{\"competenceId\":\"${contenu.competenceId}\"}")
            opportuniteDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    utilisateurId = user.id,
                    titre = "Badge obtenu : ${badge.titreFr}",
                    corps = "Fais-le vérifier par 3 pairs ou clients pour booster ton score de fiabilité.",
                    type = "BADGE",
                    canal = "IN_APP"
                )
            )
            recalculateReliabilityScore(user.id)
        }
        return badge
    }

    fun observeCoursGeneres(userId: String): Flow<List<CoursGenereIaEntity>> =
        coursIaDao.observeCoursGeneres(userId)

    suspend fun saveCoursGenere(cours: CoursGenereIaEntity) {
        coursIaDao.upsertCours(cours)
        enqueueOutbox(
            "COURS_GENERE_IA",
            cours.id,
            "{\"titre\":\"${cours.titre.replace("\"", "\\\"")}\",\"originalId\":\"${cours.coursOriginalId ?: ""}\"}"
        )
    }

    suspend fun updateCoursGenereQuiz(coursId: String, score: Int, total: Int) {
        coursIaDao.updateQuizResult(coursId, score, total)
        enqueueOutbox(
            "COURS_GENERE_IA",
            coursId,
            "{\"quizUpdated\":true,\"score\":$score,\"total\":$total}"
        )
    }

    suspend fun deleteCoursGenere(coursId: String) {
        coursIaDao.deleteCours(coursId)
    }

    suspend fun addPortfolioWork(
        competenceId: String,
        titre: String,
        descriptionCourte: String,
        localPhotoPath: String
    ) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        val item = PortfolioEntity(
            id = "pf_${UUID.randomUUID().toString().take(8)}",
            utilisateurId = user.id,
            competenceId = competenceId,
            titre = titre.trim(),
            descriptionCourte = descriptionCourte.trim(),
            photoLocaleUri = localPhotoPath,
            createdAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        preuveDao.insertPortfolioItem(item)
        enqueueOutbox("PORTFOLIO", item.id, "{\"titre\":\"${item.titre}\"}")
        recalculateReliabilityScore(user.id)
    }

    suspend fun deletePortfolioWork(work: PortfolioEntity) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        preuveDao.deletePortfolioItem(work.id)
        if (work.photoLocaleUri.isNotBlank()) {
            runCatching { File(work.photoLocaleUri).delete() }
        }
        enqueueOutbox("PORTFOLIO_DELETE", work.id, "{\"id\":\"${work.id}\"}")
        recalculateReliabilityScore(user.id)
    }

    suspend fun updateProfileDetails(
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
        photoProfilUri: String
    ) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        val existingProfile = utilisateurDao.getProfileOnce(user.id) ?: ProfilEntity(utilisateurId = user.id)
        val now = System.currentTimeMillis()

        utilisateurDao.upsertUser(
            user.copy(
                prenom = prenom.trim().ifBlank { user.prenom },
                telephone = telephone.trim().ifBlank { user.telephone },
                role = role.ifBlank { user.role },
                ville = ville.trim().ifBlank { user.ville },
                quartier = quartier.trim(),
                syncStatus = SyncStatus.PENDING_UPLOAD
            )
        )
        utilisateurDao.upsertProfile(
            existingProfile.copy(
                metierPrincipal = metierPrincipal,
                niveauInitial = niveauInitial.ifBlank { existingProfile.niveauInitial },
                objectif = objectif.ifBlank { existingProfile.objectif },
                bioCourte = bioCourte.trim(),
                disponible = disponible,
                photoProfilUri = photoProfilUri,
                updatedAt = now
            )
        )
        enqueueOutbox(
            "PROFIL_MAJ",
            user.id,
            "{\"prenom\":\"${prenom.trim()}\",\"ville\":\"${ville.trim()}\",\"disponible\":$disponible}"
        )
        recalculateReliabilityScore(user.id)
    }

    suspend fun toggleAvailability(disponible: Boolean) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        val existingProfile = utilisateurDao.getProfileOnce(user.id) ?: ProfilEntity(utilisateurId = user.id)
        utilisateurDao.upsertProfile(
            existingProfile.copy(
                disponible = disponible,
                updatedAt = System.currentTimeMillis()
            )
        )
        enqueueOutbox("DISPONIBILITE", user.id, "{\"disponible\":$disponible}")
        recalculateReliabilityScore(user.id)
    }

    suspend fun requestPeerVerification(
        badgeId: String,
        verificateurNom: String,
        verificateurTel: String,
        relation: String,
        canal: String
    ): VerificationEntity? {
        val user = utilisateurDao.getCurrentUserOnce() ?: return null
        val verif = VerificationEntity(
            id = "ver_${UUID.randomUUID().toString().take(8)}",
            utilisateurId = user.id,
            badgeId = badgeId,
            verificateurNom = verificateurNom.trim(),
            verificateurTel = verificateurTel.trim(),
            relation = relation,
            canal = canal,
            statut = "EN_ATTENTE",
            note = 5,
            commentaire = "",
            createdAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        preuveDao.insertVerification(verif)
        enqueueOutbox("VERIFICATION_REQ", verif.id, "{\"badgeId\":\"$badgeId\",\"canal\":\"$canal\"}")
        return verif
    }

    suspend fun confirmVerification(
        verifId: String,
        note: Int,
        commentaire: String
    ): Boolean {
        val user = utilisateurDao.getCurrentUserOnce() ?: return false
        val existing = preuveDao.getVerificationOnce(verifId) ?: return false
        val updated = existing.copy(
            statut = "VALIDE",
            note = note.coerceIn(1, 5),
            commentaire = commentaire.trim().ifBlank { "Travail sérieux et livré à temps." },
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        preuveDao.insertVerification(updated)
        enqueueOutbox("VERIFICATION_VALID", updated.id, "{\"note\":${updated.note}}")

        val validCount = preuveDao.countValidVerificationsForBadge(existing.badgeId)
        var badgeUpgraded = false
        if (validCount >= 3) {
            val badge = preuveDao.getBadgesOnce(user.id).firstOrNull { it.id == existing.badgeId }
            if (badge != null && badge.niveau != "VERIFIE") {
                preuveDao.insertBadge(
                    badge.copy(niveau = "VERIFIE", syncStatus = SyncStatus.PENDING_UPLOAD)
                )
                opportuniteDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        utilisateurId = user.id,
                        titre = "Compétence certifiée 3/3 !",
                        corps = "Ton badge '${badge.titreFr}' est maintenant Vérifié auprès des employeurs.",
                        type = "VERIFICATION",
                        canal = "IN_APP"
                    )
                )
                badgeUpgraded = true
            }
        }
        recalculateReliabilityScore(user.id)
        return badgeUpgraded
    }

    suspend fun applyToOpportunity(
        opportunite: OpportuniteEntity,
        messageCourt: String,
        canalEnvoi: String
    ): CandidatureEntity? {
        val user = utilisateurDao.getCurrentUserOnce() ?: return null
        val existing = opportuniteDao.getCandidatureForOffer(user.id, opportunite.id)
        if (existing != null) return existing

        val isOffline = prefsRepo.preferences.first().isOfflineSimulated
        val statut = if (canalEnvoi == "SMS" || !isOffline) "ENVOYEE" else "BROUILLON_OFFLINE"
        val syncState = if (canalEnvoi == "APP" && isOffline) SyncStatus.PENDING_UPLOAD else SyncStatus.SYNCED

        val candidature = CandidatureEntity(
            id = "cand_${UUID.randomUUID().toString().take(8)}",
            opportuniteId = opportunite.id,
            utilisateurId = user.id,
            messageCourt = messageCourt.trim(),
            canalEnvoi = canalEnvoi,
            statut = statut,
            createdAt = System.currentTimeMillis(),
            syncStatus = syncState
        )
        opportuniteDao.insertCandidature(candidature)
        // Mirror local user candidature into employer incoming list so employer space reflects it too
        val profile = utilisateurDao.getProfileOnce(user.id)
        val validBadgeCount = preuveDao.getBadgesOnce(user.id).size
        val tradeLabel = formationDao.observeCompetences().first()
            .firstOrNull { it.id == profile?.metierPrincipal }?.nomFr
            ?: "Artisan Lango"
        val passportCode = "LG-${user.id.removePrefix("usr_").uppercase()}"
        opportuniteDao.insertReceivedCandidatures(
            listOf(
                CandidatureRecueEntity(
                    id = "rec_${candidature.id}",
                    opportuniteId = opportunite.id,
                    candidatNom = user.prenom,
                    candidatTel = user.telephone,
                    candidatVille = user.ville,
                    metierLabel = tradeLabel,
                    scoreFiabilite = profile?.scoreFiabilite ?: 25,
                    badgesValides = validBadgeCount,
                    messageCourt = messageCourt.trim().ifBlank { "Disponible immédiatement." },
                    canalEnvoi = canalEnvoi,
                    statut = if (statut == "BROUILLON_OFFLINE") "ENVOYEE" else statut,
                    passeportCode = passportCode,
                    createdAt = candidature.createdAt
                )
            )
        )
        if (syncState == SyncStatus.PENDING_UPLOAD) {
            enqueueOutbox("CANDIDATURE", candidature.id, "{\"oppId\":\"${opportunite.id}\"}")
        }
        opportuniteDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                utilisateurId = user.id,
                titre = if (statut == "BROUILLON_OFFLINE") {
                    "Candidature en file d’attente"
                } else {
                    "Candidature envoyée (${canalEnvoi})"
                },
                corps = "${opportunite.titre} — ${opportunite.employeurNom}",
                type = "OFFRE",
                canal = canalEnvoi
            )
        )
        return candidature
    }

    suspend fun updateReceivedCandidatureStatus(
        cand: CandidatureRecueEntity,
        newStatus: String,
        noteEmployeur: String
    ) {
        val cleanNote = noteEmployeur.trim()
        opportuniteDao.updateReceivedCandidatureStatus(cand.id, newStatus, cleanNote)
        val user = utilisateurDao.getCurrentUserOnce()
        if (user != null && cand.id.startsWith("rec_cand_")) {
            opportuniteDao.syncUserCandidatureStatusForOffer(cand.opportuniteId, newStatus, cleanNote)
        }
        enqueueOutbox(
            "EMPLOYEUR_DECISION",
            cand.id,
            "{\"statut\":\"$newStatus\",\"candidat\":\"${cand.candidatNom}\"}"
        )
        if (user != null) {
            opportuniteDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    utilisateurId = user.id,
                    titre = "Candidature $newStatus : ${cand.candidatNom}",
                    corps = cleanNote.ifBlank { "Statut mis à jour dans l'espace Employeur / ONG." },
                    type = "OFFRE",
                    canal = "IN_APP"
                )
            )
        }
    }

    suspend fun addCaisseOperation(
        type: String,
        categorie: String,
        montantFcfa: Int,
        clientOuNote: String,
        moyenPaiement: String
    ): CaisseOperationEntity? {
        val user = utilisateurDao.getCurrentUserOnce() ?: return null
        val op = CaisseOperationEntity(
            id = "op_${UUID.randomUUID().toString().take(8)}",
            utilisateurId = user.id,
            type = type,
            categorie = categorie.trim(),
            montantFcfa = montantFcfa.coerceAtLeast(0),
            clientOuNote = clientOuNote.trim(),
            moyenPaiement = moyenPaiement,
            createdAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        caisseDevisDao.insertCaisseOperation(op)
        enqueueOutbox("CAISSE_OP", op.id, "{\"type\":\"$type\",\"fcfa\":${op.montantFcfa}}")
        return op
    }

    suspend fun deleteCaisseOperation(opId: String) {
        caisseDevisDao.deleteCaisseOperation(opId)
    }

    suspend fun createDevis(
        clientNom: String,
        clientTel: String,
        descriptionTravaux: String,
        montantFcfa: Int,
        acompteFcfa: Int,
        moyenPaiement: String
    ): DevisEntity? {
        val user = utilisateurDao.getCurrentUserOnce() ?: return null
        val ref = "DEV-${UUID.randomUUID().toString().take(6).uppercase()}"
        val devis = DevisEntity(
            id = "dev_${UUID.randomUUID().toString().take(8)}",
            utilisateurId = user.id,
            referenceCode = ref,
            clientNom = clientNom.trim(),
            clientTel = clientTel.trim(),
            descriptionTravaux = descriptionTravaux.trim(),
            montantFcfa = montantFcfa.coerceAtLeast(0),
            acompteFcfa = acompteFcfa.coerceIn(0, montantFcfa.coerceAtLeast(0)),
            statut = "BROUILLON",
            moyenPaiement = moyenPaiement,
            createdAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING_UPLOAD
        )
        caisseDevisDao.insertDevis(devis)
        enqueueOutbox("DEVIS", devis.id, "{\"ref\":\"$ref\",\"fcfa\":${devis.montantFcfa}}")
        return devis
    }

    suspend fun markDevisStatus(devis: DevisEntity, newStatus: String) {
        val updated = devis.copy(statut = newStatus, syncStatus = SyncStatus.PENDING_UPLOAD)
        caisseDevisDao.insertDevis(updated)
        if (newStatus == "PAYE" && devis.statut != "PAYE") {
            val user = utilisateurDao.getCurrentUserOnce()
            if (user != null) {
                val cashIn = CaisseOperationEntity(
                    id = "op_${UUID.randomUUID().toString().take(8)}",
                    utilisateurId = user.id,
                    type = "ENTREE",
                    categorie = "Règlement devis ${devis.referenceCode}",
                    montantFcfa = devis.montantFcfa,
                    clientOuNote = "${devis.clientNom} (${devis.moyenPaiement})",
                    moyenPaiement = devis.moyenPaiement,
                    createdAt = System.currentTimeMillis(),
                    syncStatus = SyncStatus.PENDING_UPLOAD
                )
                caisseDevisDao.insertCaisseOperation(cashIn)
                opportuniteDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        utilisateurId = user.id,
                        titre = "Reçu payé : ${devis.referenceCode}",
                        corps = "+${devis.montantFcfa} FCFA ajoutés automatiquement à ton cahier de caisse.",
                        type = "OFFRE",
                        canal = "IN_APP"
                    )
                )
            }
        }
        enqueueOutbox("DEVIS_STATUT", devis.id, "{\"ref\":\"${devis.referenceCode}\",\"statut\":\"$newStatus\"}")
    }

    suspend fun deleteDevis(devisId: String) {
        caisseDevisDao.deleteDevis(devisId)
    }

    suspend fun getCoachMessagesOnce(): List<CoachMessageEntity> {
        val user = utilisateurDao.getCurrentUserOnce() ?: return emptyList()
        return coachDao.getMessagesOnce(user.id)
    }

    suspend fun saveCoachMessage(
        role: String,
        texte: String,
        langue: String,
        metierContext: String
    ) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        coachDao.insertMessage(
            CoachMessageEntity(
                id = "ai_${UUID.randomUUID().toString().take(8)}",
                utilisateurId = user.id,
                role = role,
                texte = texte.trim(),
                langue = langue,
                metierContext = metierContext,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearCoachHistory() {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        coachDao.clearMessages(user.id)
    }

    suspend fun publishOpportunity(
        employeurNom: String,
        titre: String,
        type: String,
        ville: String,
        quartier: String,
        competenceId: String,
        remunerationFcfa: Int,
        descriptionCourte: String,
        contactTel: String
    ): OpportuniteEntity {
        val now = System.currentTimeMillis()
        val twoWeeks = 14L * 24L * 3600L * 1000L
        val opp = OpportuniteEntity(
            id = "opp_${UUID.randomUUID().toString().take(8)}",
            employeurNom = employeurNom.trim(),
            titre = titre.trim(),
            type = type,
            ville = ville.trim(),
            quartier = quartier.trim(),
            competenceId = competenceId,
            remunerationFcfa = remunerationFcfa,
            descriptionCourte = descriptionCourte.trim(),
            contactTel = contactTel.trim(),
            expireLe = now + twoWeeks,
            estActive = true
        )
        opportuniteDao.insertOpportunite(opp)
        enqueueOutbox("OPPORTUNITE", opp.id, "{\"titre\":\"${opp.titre}\",\"ville\":\"${opp.ville}\"}")
        utilisateurDao.getCurrentUserOnce()?.let { user ->
            opportuniteDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    utilisateurId = user.id,
                    titre = "Offre publiée : ${opp.titre}",
                    corps = "Visible localement pour ${opp.ville} (${opp.remunerationFcfa} FCFA).",
                    type = "OFFRE",
                    canal = "IN_APP"
                )
            )
        }
        return opp
    }

    suspend fun recordMobileMoneyAction(
        operatorName: String,
        packName: String,
        amountFcfa: Int,
        phoneNumber: String,
        viaUssdFallback: Boolean
    ) {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        val channel = if (viaUssdFallback) "USSD" else "IN_APP"
        enqueueOutbox(
            "MOBILE_MONEY",
            "mm_${UUID.randomUUID().toString().take(8)}",
            "{\"op\":\"$operatorName\",\"fcfa\":$amountFcfa,\"tel\":\"$phoneNumber\",\"ussd\":$viaUssdFallback}"
        )
        opportuniteDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                utilisateurId = user.id,
                titre = "$operatorName ($channel) • $amountFcfa FCFA",
                corps = if (viaUssdFallback) {
                    "Code USSD préparé pour « $packName ». Confirme sur ton écran d'appel."
                } else {
                    "Demande enregistrée pour « $packName » sur $phoneNumber."
                },
                type = "OFFRE",
                canal = channel
            )
        )
    }

    suspend fun markAllNotificationsRead() {
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        opportuniteDao.markAllNotificationsRead(user.id)
    }

    private suspend fun recalculateReliabilityScore(userId: String) {
        val profile = utilisateurDao.getProfileOnce(userId) ?: return
        val badges = preuveDao.getBadgesOnce(userId)
        val validVerifs = preuveDao.getVerificationsOnce(userId).count { it.statut == "VALIDE" }
        val portfolioCount = preuveDao.getPortfolioOnce(userId).size
        val verifiedBadges = badges.count { it.niveau == "VERIFIE" }

        val newScore = LangoDomainLogic.computeReliabilityScore(
            badgeCount = badges.size,
            verifiedBadgeCount = verifiedBadges,
            validVerificationsCount = validVerifs,
            portfolioCount = portfolioCount,
            hasBio = profile.bioCourte.isNotBlank(),
            isAvailable = profile.disponible,
            hasProfilePhoto = profile.photoProfilUri.isNotBlank()
        )

        utilisateurDao.upsertProfile(
            profile.copy(scoreFiabilite = newScore, updatedAt = System.currentTimeMillis())
        )
    }

    private suspend fun enqueueOutbox(entityType: String, entityId: String, payloadJson: String) {
        syncOutboxDao.enqueue(
            SyncOutboxEntity(
                id = "sync_${UUID.randomUUID().toString().take(8)}",
                entityType = entityType,
                entityId = entityId,
                payloadJson = payloadJson
            )
        )
    }

    suspend fun setSmsAlerts(enabled: Boolean, ville: String? = null) {
        prefsRepo.setSmsAlerts(enabled, ville)
        val user = utilisateurDao.getCurrentUserOnce() ?: return
        if (enabled) {
            opportuniteDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    utilisateurId = user.id,
                    titre = "Alertes SMS activées",
                    corps = "Tu recevras les nouvelles offres de ${ville ?: user.ville} sur ${user.telephone}.",
                    type = "OFFRE",
                    canal = "SMS"
                )
            )
        }
    }

    suspend fun toggleOfflineSimulation(offline: Boolean) {
        prefsRepo.setOfflineSimulated(offline)
        if (!offline) {
            triggerSyncNow()
        }
    }

    suspend fun triggerSyncNow() {
        syncOutboxDao.markAllAsSynced()
        opportuniteDao.promoteOfflineCandidaturesToSent()
    }

    suspend fun getCloudSyncSnapshot(): CloudSyncSnapshot? {
        val user = utilisateurDao.getCurrentUserOnce() ?: return null
        val profile = utilisateurDao.getProfileOnce(user.id)
        val badges = preuveDao.getBadgesOnce(user.id)
        val portfolioCount = preuveDao.getPortfolioOnce(user.id).size
        val caisseOps = caisseDevisDao.getCaisseOperationsOnce(user.id)
        val devis = caisseDevisDao.getDevisOnce(user.id)
        return CloudSyncSnapshot(
            user = user,
            profile = profile,
            badgesCount = badges.size,
            verifiedBadgesCount = badges.count { it.niveau == "VERIFIE" },
            portfolioCount = portfolioCount,
            caisseOperations = caisseOps,
            devisList = devis
        )
    }

    suspend fun getDevisByIdOnce(devisId: String): DevisEntity? =
        caisseDevisDao.getDevisById(devisId)

    suspend fun mergeCloudOpportunites(cloudItems: List<CloudOpportunite>) {
        if (cloudItems.isEmpty()) return
        ensureSeedData()
        val validCompIds = formationDao.observeCompetences().first().map { it.id }.toSet()
        val fallbackCompId = validCompIds.firstOrNull() ?: return
        val twoWeeks = 14L * 24L * 3600L * 1000L
        val mapped = cloudItems.map { cloud ->
            val createdMs = cloud.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            val safeCompId = if (cloud.competenceId in validCompIds) cloud.competenceId else fallbackCompId
            OpportuniteEntity(
                id = cloud.id,
                employeurNom = cloud.employeurNom,
                titre = cloud.titre,
                type = cloud.type,
                ville = cloud.ville,
                quartier = cloud.quartier,
                competenceId = safeCompId,
                remunerationFcfa = cloud.remunerationFcfa,
                descriptionCourte = cloud.descriptionCourte,
                contactTel = cloud.contactTel,
                expireLe = createdMs + twoWeeks,
                estActive = cloud.active
            )
        }
        opportuniteDao.insertOpportunites(mapped)
    }

    data class CloudSyncSnapshot(
        val user: UtilisateurEntity,
        val profile: ProfilEntity?,
        val badgesCount: Int,
        val verifiedBadgesCount: Int,
        val portfolioCount: Int,
        val caisseOperations: List<CaisseOperationEntity>,
        val devisList: List<DevisEntity>
    )

    suspend fun exportPersonalDataJson(): String {
        val user = utilisateurDao.getCurrentUserOnce() ?: return "{}"
        val profile = utilisateurDao.getProfileOnce(user.id)
        val userBadges = preuveDao.getBadgesOnce(user.id)
        val userVerifs = preuveDao.getVerificationsOnce(user.id)
        val userPortfolio = preuveDao.getPortfolioOnce(user.id)
        val userCands = opportuniteDao.observeCandidatures(user.id).first()
        val caisseOps = caisseDevisDao.getCaisseOperationsOnce(user.id)
        val devis = caisseDevisDao.getDevisOnce(user.id)
        val coachMsgs = coachDao.getMessagesOnce(user.id)

        fun esc(v: String): String = v.replace("\"", "\\\"").replace("\n", " ")

        return buildString {
            append("{\n")
            append("  \"norme\": \"Loi RCA n° 24.001 - Portabilité\",\n")
            append("  \"utilisateur\": {\n")
            append("    \"id\": \"${esc(user.id)}\",\n")
            append("    \"prenom\": \"${esc(user.prenom)}\",\n")
            append("    \"telephone\": \"${esc(user.telephone)}\",\n")
            append("    \"ville\": \"${esc(user.ville)}\",\n")
            append("    \"quartier\": \"${esc(user.quartier)}\",\n")
            append("    \"langue\": \"${esc(user.languePref)}\",\n")
            append("    \"role\": \"${esc(user.role)}\",\n")
            append("    \"consentement\": ${user.consentementLoi24001}\n")
            append("  },\n")
            append("  \"profil\": {\n")
            append("    \"niveau\": \"${esc(profile?.niveauInitial.orEmpty())}\",\n")
            append("    \"objectif\": \"${esc(profile?.objectif.orEmpty())}\",\n")
            append("    \"metierPrincipal\": \"${esc(profile?.metierPrincipal.orEmpty())}\",\n")
            append("    \"bioCourte\": \"${esc(profile?.bioCourte.orEmpty())}\",\n")
            append("    \"disponible\": ${profile?.disponible ?: true},\n")
            append("    \"aUnePhotoProfil\": ${profile?.photoProfilUri?.isNotBlank() == true},\n")
            append("    \"scoreFiabilite\": ${profile?.scoreFiabilite ?: 0}\n")
            append("  },\n")
            append("  \"badges\": [${userBadges.joinToString(",") { "\"${esc(it.titreFr)} (${esc(it.niveau)})\"" }}],\n")
            append("  \"verificationsValidees\": ${userVerifs.count { it.statut == "VALIDE" }},\n")
            append("  \"realisationsPortfolio\": ${userPortfolio.size},\n")
            append("  \"candidatures\": ${userCands.size},\n")
            append("  \"operationsCaisse\": ${caisseOps.size},\n")
            append("  \"devisArtisan\": ${devis.size},\n")
            append("  \"messagesCoachIA\": ${coachMsgs.size}\n")
            append("}")
        }
    }

    suspend fun wipeAllDataLoi24001() {
        utilisateurDao.wipeAllPersonalData()
        prefsRepo.clearAll()
        ImageCompressionUtil.deleteAllPortfolioFiles(context)
        ImageCompressionUtil.deleteAllProfileFiles(context)
    }
}
