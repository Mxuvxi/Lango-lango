package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.local.LangoDatabase
import com.example.data.local.LangoDomainLogic
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.remote.FirebaseSyncRepository
import com.example.data.repository.LangoAiRepository
import com.example.data.repository.LangoRepository
import com.example.ui.auth.LangoAuthScreen
import com.example.ui.auth.authStateFlow
import com.example.ui.auth.signOutGoogleSession
import com.example.ui.screens.AiNotesToCourseScreen
import com.example.ui.screens.CaisseDevisScreen
import com.example.ui.screens.CoachAiScreen
import com.example.ui.screens.DiagnosticScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.EmployerCandidatesScreen
import com.example.ui.screens.HomeHubScreen
import com.example.ui.screens.LessonPlayerScreen
import com.example.ui.screens.MobileMoneyScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.OpportunityDetailScreen
import com.example.ui.screens.PublishOpportunityScreen
import com.example.ui.screens.RegistrationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VerificationRequestScreen
import com.example.ui.util.ConnectivityObserver
import com.example.ui.viewmodel.LangoAppState
import com.example.ui.viewmodel.LangoViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

object LangoRoutes {
    const val ONBOARDING = "onboarding"
    const val REGISTRATION = "registration"
    const val DIAGNOSTIC = "diagnostic"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val LESSON = "lesson/{contenuId}"
    const val VERIFICATION = "verification"
    const val OPPORTUNITY = "opportunity/{oppId}"
    const val MOBILE_MONEY = "mobile_money"
    const val NOTIFICATIONS = "notifications"
    const val PUBLISH_OPPORTUNITY = "publish_opportunity"
    const val CAISSE_DEVIS = "caisse_devis"
    const val EMPLOYER_CANDIDATES = "employer_candidates"
    const val COACH_AI = "coach_ai"
    const val AI_NOTES_STUDIO = "ai_notes_studio"

    fun lessonRoute(contenuId: String): String = "lesson/$contenuId"
    fun opportunityRoute(oppId: String): String = "opportunity/$oppId"
}

@Composable
fun LangoAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        LangoAuthScreen()
    } else {
        key(user.uid) {
            val viewModel: LangoViewModel = viewModel(
                key = user.uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val firestore = FirebaseFirestore.getInstance(databaseId)
                        val prefsRepo = UserPreferencesRepository(app)
                        val langoRepo = LangoRepository(
                            context = app,
                            db = LangoDatabase.getInstance(app),
                            prefsRepo = prefsRepo
                        )
                        LangoViewModel(
                            appContext = app,
                            repository = langoRepo,
                            cloudSync = FirebaseSyncRepository(firestore, auth),
                            aiRepository = LangoAiRepository(),
                            connectivityObserver = ConnectivityObserver(app),
                            currentUserId = user.uid
                        )
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            LangoNavGraph(
                uiState = uiState,
                viewModel = viewModel,
                userEmail = user.email.orEmpty(),
                onSignOut = {
                    scope.launch {
                        signOutGoogleSession(context, auth)
                    }
                }
            )
        }
    }
}

@Composable
fun LangoNavGraph(
    uiState: LangoAppState,
    viewModel: LangoViewModel,
    userEmail: String = "",
    onSignOut: () -> Unit = {}
) {
    when (uiState) {
        LangoAppState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LangoAppState.Ready -> {
            val navController = rememberNavController()
            val startDestination = when {
                uiState.user == null -> LangoRoutes.ONBOARDING
                !uiState.prefs.diagnosticCompleted -> LangoRoutes.DIAGNOSTIC
                else -> LangoRoutes.HOME
            }

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable(LangoRoutes.ONBOARDING) {
                    OnboardingScreen(
                        selectedLang = uiState.prefs.langue,
                        onSelectLang = { viewModel.selectLanguage(it) },
                        onStartClick = { navController.navigate(LangoRoutes.REGISTRATION) }
                    )
                }

                composable(LangoRoutes.REGISTRATION) {
                    RegistrationScreen(
                        selectedLang = uiState.prefs.langue,
                        onBack = { navController.popBackStack() },
                        onSubmit = { prenom, tel, ville, quartier, role, consent ->
                            viewModel.registerUser(
                                prenom = prenom,
                                telephone = tel,
                                ville = ville,
                                quartier = quartier,
                                role = role,
                                languePref = uiState.prefs.langue,
                                consentAccepted = consent
                            ) {
                                navController.navigate(LangoRoutes.DIAGNOSTIC) {
                                    popUpTo(LangoRoutes.ONBOARDING) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(LangoRoutes.DIAGNOSTIC) {
                    DiagnosticScreen(
                        selectedLang = uiState.prefs.langue,
                        competences = uiState.competences,
                        onFinish = { niveau, objectif, metier ->
                            viewModel.saveDiagnostic(niveau, objectif, metier) {
                                navController.navigate(LangoRoutes.HOME) {
                                    popUpTo(LangoRoutes.DIAGNOSTIC) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(LangoRoutes.HOME) {
                    HomeHubScreen(
                        state = uiState,
                        onOpenSettings = { navController.navigate(LangoRoutes.SETTINGS) },
                        onOpenNotifications = { navController.navigate(LangoRoutes.NOTIFICATIONS) },
                        onOpenMobileMoney = { navController.navigate(LangoRoutes.MOBILE_MONEY) },
                        onOpenCaisseDevis = { navController.navigate(LangoRoutes.CAISSE_DEVIS) },
                        onOpenCoachAi = { navController.navigate(LangoRoutes.COACH_AI) },
                        onOpenAiNotesStudio = { navController.navigate(LangoRoutes.AI_NOTES_STUDIO) },
                        onOpenEmployerSpace = { navController.navigate(LangoRoutes.EMPLOYER_CANDIDATES) },
                        onOpenPublishOpportunity = { navController.navigate(LangoRoutes.PUBLISH_OPPORTUNITY) },
                        onTogglePackDownload = { viewModel.togglePackDownload(it) },
                        onOpenLesson = { contenuId ->
                            navController.navigate(LangoRoutes.lessonRoute(contenuId))
                        },
                        onOpenVerification = { navController.navigate(LangoRoutes.VERIFICATION) },
                        onAddPortfolioWork = { compId, titre, desc, uri ->
                            viewModel.addPortfolioWork(compId, titre, desc, uri)
                        },
                        onDeletePortfolioWork = { viewModel.deletePortfolioWork(it) },
                        onOpenEditProfile = { navController.navigate(LangoRoutes.EDIT_PROFILE) },
                        onToggleAvailability = { viewModel.toggleAvailability(it) },
                        onSelectCityFilter = { viewModel.selectCityFilter(it) },
                        onSelectTypeFilter = { viewModel.selectTypeFilter(it) },
                        onOpenOpportunity = { oppId ->
                            navController.navigate(LangoRoutes.opportunityRoute(oppId))
                        },
                        onSyncNow = { viewModel.syncNow() },
                        onDismissBanner = { viewModel.clearBanner() }
                    )
                }

                composable(
                    route = LangoRoutes.LESSON,
                    arguments = listOf(navArgument("contenuId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val contenuId = backStackEntry.arguments?.getString("contenuId").orEmpty()
                    val contenu = uiState.contenus.firstOrNull { it.id == contenuId }
                        ?: uiState.contenus.firstOrNull()
                    if (contenu != null) {
                        val existingProg = uiState.progressions.firstOrNull { it.contenuId == contenu.id }
                        val startIdx = if (existingProg != null && !existingProg.estTermine) {
                            (existingProg.etapeActuelle - 1).coerceAtLeast(0)
                        } else {
                            0
                        }
                        LessonPlayerScreen(
                            contenu = contenu,
                            langue = uiState.prefs.langue,
                            initialStepIndex = startIdx,
                            onBack = { navController.popBackStack() },
                            onSaveProgress = { etape, total, score, completed, onBadge ->
                                viewModel.saveLessonProgress(
                                    contenuId = contenu.id,
                                    etape = etape,
                                    totalEtapes = total,
                                    scoreQuiz = score,
                                    completed = completed,
                                    onBadgeAwarded = onBadge
                                )
                            },
                            onGoToVerification = {
                                navController.navigate(LangoRoutes.VERIFICATION) {
                                    popUpTo(LangoRoutes.HOME)
                                }
                            }
                        )
                    }
                }

                composable(LangoRoutes.VERIFICATION) {
                    VerificationRequestScreen(
                        langue = uiState.prefs.langue,
                        userName = uiState.user?.prenom ?: "Un artisan Lango",
                        badges = uiState.badges,
                        verifications = uiState.verifications,
                        onBack = { navController.popBackStack() },
                        onSubmitRequest = { badgeId, nom, tel, relation, canal ->
                            viewModel.requestVerification(badgeId, nom, tel, relation, canal)
                        },
                        onConfirmVerification = { verifId, note, commentaire ->
                            viewModel.confirmVerification(verifId, note, commentaire)
                        }
                    )
                }

                composable(
                    route = LangoRoutes.OPPORTUNITY,
                    arguments = listOf(navArgument("oppId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val oppId = backStackEntry.arguments?.getString("oppId").orEmpty()
                    val opp = uiState.opportunites.firstOrNull { it.id == oppId }
                        ?: uiState.opportunites.firstOrNull()
                    if (opp != null) {
                        val existingCand = uiState.candidatures.firstOrNull { it.opportuniteId == opp.id }
                        OpportunityDetailScreen(
                            opportunite = opp,
                            langue = uiState.prefs.langue,
                            user = uiState.user,
                            profile = uiState.profile,
                            badges = uiState.badges,
                            existingCandidature = existingCand,
                            isOffline = uiState.prefs.isOfflineSimulated || !uiState.isDeviceOnline,
                            onBack = { navController.popBackStack() },
                            onApplyOneClick = { message, canal ->
                                viewModel.applyToOpportunity(opp, message, canal)
                            }
                        )
                    }
                }

                composable(LangoRoutes.PUBLISH_OPPORTUNITY) {
                    PublishOpportunityScreen(
                        langue = uiState.prefs.langue,
                        defaultEmployer = uiState.user?.prenom.orEmpty(),
                        defaultPhone = uiState.user?.telephone.orEmpty(),
                        defaultCity = uiState.user?.ville.orEmpty(),
                        competences = uiState.competences,
                        onBack = { navController.popBackStack() },
                        onPublish = { emp, titre, type, ville, quart, compId, amount, desc, tel ->
                            viewModel.publishOpportunity(
                                employeurNom = emp,
                                titre = titre,
                                type = type,
                                ville = ville,
                                quartier = quart,
                                competenceId = compId,
                                remunerationFcfa = amount,
                                descriptionCourte = desc,
                                contactTel = tel
                            ) {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable(LangoRoutes.MOBILE_MONEY) {
                    MobileMoneyScreen(
                        langue = uiState.prefs.langue,
                        defaultPhone = uiState.user?.telephone.orEmpty(),
                        isOffline = uiState.prefs.isOfflineSimulated || !uiState.isDeviceOnline,
                        onBack = { navController.popBackStack() },
                        onPaymentAction = { op, packName, amount, phone, viaUssd ->
                            viewModel.recordMobileMoneyPayment(op, packName, amount, phone, viaUssd)
                        }
                    )
                }

                composable(LangoRoutes.NOTIFICATIONS) {
                    NotificationsScreen(
                        langue = uiState.prefs.langue,
                        notifications = uiState.notifications,
                        onBack = { navController.popBackStack() },
                        onMarkAllRead = { viewModel.markAllNotificationsRead() }
                    )
                }

                composable(LangoRoutes.CAISSE_DEVIS) {
                    CaisseDevisScreen(
                        langue = uiState.prefs.langue,
                        artisanName = uiState.user?.prenom.orEmpty(),
                        operations = uiState.caisseOperations,
                        devisList = uiState.devisList,
                        onBack = { navController.popBackStack() },
                        onAddCaisseOperation = { type, cat, amount, note, moyen ->
                            viewModel.addCaisseOperation(type, cat, amount, note, moyen)
                        },
                        onDeleteCaisseOperation = { viewModel.deleteCaisseOperation(it) },
                        onCreateDevis = { clientNom, clientTel, desc, amount, acompte, moyen ->
                            viewModel.createDevis(clientNom, clientTel, desc, amount, acompte, moyen)
                        },
                        onMarkDevisStatus = { devis, status ->
                            viewModel.markDevisStatus(devis, status)
                        },
                        onDeleteDevis = { viewModel.deleteDevis(it) }
                    )
                }

                composable(LangoRoutes.EMPLOYER_CANDIDATES) {
                    val tradeLabel = uiState.competences
                        .firstOrNull { it.id == uiState.profile?.metierPrincipal }
                        ?.nomFr
                        .orEmpty()
                    val localQrPayload = uiState.user?.let { usr ->
                        val validCount = uiState.verifications.count { it.statut == "VALIDE" }
                        LangoDomainLogic.encodePassportQrPayload(
                            passportCode = "LG-${usr.id.removePrefix("usr_").uppercase()}",
                            ownerName = usr.prenom,
                            city = usr.ville,
                            phone = usr.telephone,
                            score = uiState.profile?.scoreFiabilite ?: 25,
                            badgesVerifiedRatio = "$validCount/3",
                            tradeLabel = tradeLabel
                        )
                    }
                    EmployerCandidatesScreen(
                        langue = uiState.prefs.langue,
                        opportunites = uiState.opportunites,
                        candidaturesRecues = uiState.candidaturesRecues,
                        localUserPassportPayload = localQrPayload,
                        onBack = { navController.popBackStack() },
                        onUpdateCandidateStatus = { cand, newStatus, note ->
                            viewModel.updateReceivedCandidatureStatus(cand, newStatus, note)
                        }
                    )
                }

                composable(LangoRoutes.COACH_AI) {
                    val tradeLabel = uiState.competences
                        .firstOrNull { it.id == uiState.profile?.metierPrincipal }
                        ?.let { if (uiState.prefs.langue == "SG") it.nomSg else it.nomFr }
                        .orEmpty()
                    CoachAiScreen(
                        langue = uiState.prefs.langue,
                        tradeLabel = tradeLabel,
                        isOffline = uiState.prefs.isOfflineSimulated || !uiState.isDeviceOnline,
                        messages = uiState.coachMessages,
                        aiState = uiState.coachAiState,
                        onBack = { navController.popBackStack() },
                        onSendQuestion = { viewModel.sendCoachQuestion(it) },
                        onClearHistory = { viewModel.clearCoachHistory() },
                        onDismissError = { viewModel.dismissCoachError() }
                    )
                }

                composable(LangoRoutes.AI_NOTES_STUDIO) {
                    AiNotesToCourseScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(LangoRoutes.EDIT_PROFILE) {
                    EditProfileScreen(
                        langue = uiState.prefs.langue,
                        user = uiState.user,
                        profile = uiState.profile,
                        competences = uiState.competences,
                        onBack = { navController.popBackStack() },
                        onSave = { prenom, tel, role, ville, quartier, metier, niveau, objectif, bio, disp, photoUri, removePhoto ->
                            viewModel.updateProfileDetails(
                                prenom = prenom,
                                telephone = tel,
                                role = role,
                                ville = ville,
                                quartier = quartier,
                                metierPrincipal = metier,
                                niveauInitial = niveau,
                                objectif = objectif,
                                bioCourte = bio,
                                disponible = disp,
                                pickedPhotoUri = photoUri,
                                removePhoto = removePhoto
                            ) {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                composable(LangoRoutes.SETTINGS) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val primaryTradeName = uiState.competences
                        .firstOrNull { it.id == uiState.profile?.metierPrincipal }
                        ?.nomFr
                        .orEmpty()
                    SettingsScreen(
                        prefs = uiState.prefs,
                        userVille = uiState.user?.ville.orEmpty(),
                        userMetier = primaryTradeName,
                        userEmail = userEmail,
                        onBack = { navController.popBackStack() },
                        onOpenEditProfile = { navController.navigate(LangoRoutes.EDIT_PROFILE) },
                        onOpenMobileMoney = { navController.navigate(LangoRoutes.MOBILE_MONEY) },
                        onSelectLang = { viewModel.selectLanguage(it) },
                        onToggleSmsAlerts = { viewModel.setSmsAlerts(it) },
                        onToggleOfflineSim = { viewModel.toggleOfflineSimulation(it) },
                        onExportPersonalData = {
                            viewModel.exportPersonalData { json ->
                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(android.content.Intent.EXTRA_TEXT, json)
                                }
                                runCatching {
                                    context.startActivity(
                                        android.content.Intent.createChooser(shareIntent, "Exporter mes données Lango")
                                    )
                                }
                            }
                        },
                        onSignOut = onSignOut,
                        onEraseAllData = {
                            viewModel.eraseAllData {
                                navController.navigate(LangoRoutes.ONBOARDING) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
