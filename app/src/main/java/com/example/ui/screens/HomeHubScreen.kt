package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SupervisorAccount
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.LangoDomainLogic
import com.example.data.local.LangoSeedData
import com.example.data.local.entity.ContenuPedagogiqueEntity
import com.example.data.local.entity.LangueCode
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.PortfolioEntity
import com.example.data.local.entity.SyncOutboxEntity
import com.example.data.local.entity.SyncStatus
import com.example.ui.components.LangoAvatar
import com.example.ui.components.QrPassportDialog
import com.example.ui.components.QrVerifyPassportDialog
import com.example.ui.i18n.LangoCopy
import com.example.ui.i18n.LangoI18n
import com.example.ui.util.LangoSpeakerState
import com.example.ui.util.rememberLangoSpeaker
import com.example.ui.viewmodel.LangoAppState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeHubScreen(
    state: LangoAppState.Ready,
    onOpenSettings: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenMobileMoney: () -> Unit,
    onOpenCaisseDevis: () -> Unit,
    onOpenCoachAi: () -> Unit,
    onOpenEmployerSpace: () -> Unit,
    onOpenPublishOpportunity: () -> Unit,
    onTogglePackDownload: (ContenuPedagogiqueEntity) -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenVerification: () -> Unit,
    onAddPortfolioWork: (competenceId: String, titre: String, description: String, uri: Uri?) -> Unit,
    onDeletePortfolioWork: (PortfolioEntity) -> Unit,
    onOpenEditProfile: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit,
    onSelectCityFilter: (String?) -> Unit,
    onSelectTypeFilter: (String?) -> Unit,
    onOpenOpportunity: (String) -> Unit,
    onSyncNow: () -> Unit,
    onDismissBanner: () -> Unit
) {
    val copy = LangoI18n.forLang(state.prefs.langue)
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showSyncQueueDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val unreadNotifCount = state.notifications.count { !it.lue }
    val effectiveOffline = state.prefs.isOfflineSimulated || !state.isDeviceOnline
    val speaker = rememberLangoSpeaker()

    LaunchedEffect(state.statusBanner) {
        val msg = state.statusBanner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            onDismissBanner()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(onClick = onOpenEditProfile)
                                .testTag("top_bar_profile_button")
                        ) {
                            LangoAvatar(
                                photoPath = state.profile?.photoProfilUri.orEmpty(),
                                displayName = state.user?.prenom ?: "L",
                                size = 38.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Lango",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = state.user?.let { "${it.prenom} • ${it.ville}" } ?: copy.slogan,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onOpenNotifications,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_notifications_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifCount > 0) {
                                        Badge { Text("$unreadNotifCount") }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Notifications,
                                    contentDescription = "Notifications"
                                )
                            }
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(id = R.string.cd_settings)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = copy.tabLearn
                                )
                            },
                            label = { Text(copy.tabLearn) },
                            modifier = Modifier.testTag("nav_tab_learn")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Rounded.Verified else Icons.Outlined.Verified,
                                    contentDescription = copy.tabProve
                                )
                            },
                            label = { Text(copy.tabProve) },
                            modifier = Modifier.testTag("nav_tab_prove")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Rounded.Work else Icons.Outlined.WorkOutline,
                                    contentDescription = copy.tabConnect
                                )
                            },
                            label = { Text(copy.tabConnect) },
                            modifier = Modifier.testTag("nav_tab_connect")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(modifier = Modifier.fillMaxHeight()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        NavigationRailItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Rounded.MenuBook else Icons.Outlined.MenuBook,
                                    contentDescription = copy.tabLearn
                                )
                            },
                            label = { Text(copy.tabLearn) }
                        )
                        NavigationRailItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 1) Icons.Rounded.Verified else Icons.Outlined.Verified,
                                    contentDescription = copy.tabProve
                                )
                            },
                            label = { Text(copy.tabProve) }
                        )
                        NavigationRailItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Rounded.Work else Icons.Outlined.WorkOutline,
                                    contentDescription = copy.tabConnect
                                )
                            },
                            label = { Text(copy.tabConnect) }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .align(Alignment.TopCenter)
                            .fillMaxSize()
                    ) {
                        OfflineSyncStrip(
                            isOffline = effectiveOffline,
                            pendingCount = state.pendingSyncCount,
                            copy = copy,
                            onInspectQueue = { showSyncQueueDialog = true },
                            onSyncNow = onSyncNow
                        )

                        when (selectedTab) {
                            0 -> LearnLibrarySection(
                                state = state,
                                copy = copy,
                                speaker = speaker,
                                onOpenCoachAi = onOpenCoachAi,
                                onTogglePackDownload = onTogglePackDownload,
                                onOpenLesson = onOpenLesson
                            )
                            1 -> ProveProfileSection(
                                state = state,
                                copy = copy,
                                speaker = speaker,
                                onOpenVerification = onOpenVerification,
                                onOpenMobileMoney = onOpenMobileMoney,
                                onOpenCaisseDevis = onOpenCaisseDevis,
                                onAddPortfolioWork = onAddPortfolioWork,
                                onDeletePortfolioWork = onDeletePortfolioWork,
                                onOpenEditProfile = onOpenEditProfile,
                                onToggleAvailability = onToggleAvailability
                            )
                            else -> ConnectOpportunitiesSection(
                                state = state,
                                copy = copy,
                                speaker = speaker,
                                onOpenEmployerSpace = onOpenEmployerSpace,
                                onOpenPublishOpportunity = onOpenPublishOpportunity,
                                onSelectCityFilter = onSelectCityFilter,
                                onSelectTypeFilter = onSelectTypeFilter,
                                onOpenOpportunity = onOpenOpportunity
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSyncQueueDialog) {
        SyncQueueDialog(
            items = state.syncOutboxItems,
            isOffline = effectiveOffline,
            onSyncNow = onSyncNow,
            onDismiss = { showSyncQueueDialog = false }
        )
    }
}

@Composable
private fun AudioReadButton(
    utteranceId: String,
    spokenText: String,
    speaker: LangoSpeakerState
) {
    val isPlaying = speaker.activeUtteranceId == utteranceId
    IconButton(
        onClick = { speaker.toggleSpeak(utteranceId, spokenText) },
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .testTag("audio_btn_$utteranceId")
    ) {
        Icon(
            imageVector = if (isPlaying) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = stringResource(id = R.string.cd_audio_guide),
            tint = if (isPlaying) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun OfflineSyncStrip(
    isOffline: Boolean,
    pendingCount: Int,
    copy: LangoCopy,
    onInspectQueue: () -> Unit,
    onSyncNow: () -> Unit
) {
    Surface(
        color = if (isOffline) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onInspectQueue)
            .testTag("sync_strip_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isOffline) Icons.Rounded.CloudOff else Icons.Rounded.CloudDone,
                    contentDescription = stringResource(id = R.string.cd_sync_status),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buildString {
                        append(if (isOffline) copy.offlineBadge else copy.onlineBadge)
                        if (pendingCount > 0) {
                            append(" • ${copy.pendingSyncPrefix} $pendingCount")
                        }
                    },
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (!isOffline && pendingCount > 0) {
                TextButton(
                    onClick = onSyncNow,
                    modifier = Modifier.testTag("sync_now_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(copy.syncNow)
                }
            }
        }
    }
}

@Composable
private fun SyncQueueDialog(
    items: List<SyncOutboxEntity>,
    isOffline: Boolean,
    onSyncNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("File de synchronisation hors-ligne") },
        text = {
            if (items.isEmpty()) {
                Text("Aucune opération en attente. Tout est à jour localement.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items.take(6).forEach { item ->
                        val isPending = item.status == SyncStatus.PENDING_UPLOAD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.entityType,
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = if (isPending) "En attente" else "Synchronisé",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isPending) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isOffline && items.any { it.status == SyncStatus.PENDING_UPLOAD }) {
                TextButton(
                    onClick = {
                        onSyncNow()
                        onDismiss()
                    }
                ) {
                    Text("Synchroniser")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}

@Composable
private fun LearnLibrarySection(
    state: LangoAppState.Ready,
    copy: LangoCopy,
    speaker: LangoSpeakerState,
    onOpenCoachAi: () -> Unit,
    onTogglePackDownload: (ContenuPedagogiqueEntity) -> Unit,
    onOpenLesson: (String) -> Unit
) {
    val isSango = state.prefs.langue == LangueCode.SG
    val progressByContent = remember(state.progressions) {
        state.progressions.associateBy { it.contenuId }
    }
    val primaryTrade = state.profile?.metierPrincipal.orEmpty()
    val sortedContenus = remember(state.contenus, primaryTrade) {
        state.contenus.sortedByDescending { it.competenceId == primaryTrade }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = copy.learnHeader,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenCoachAi)
                    .testTag("open_coach_ai_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isSango) "Coach Kua IA + Audio" else "Coach Métiers IA + Voix",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isSango) {
                                    "Hunda tene na ndo devis, sécurité na kua ti mo."
                                } else {
                                    "Conseils pratiques en français ou sango, lus à voix haute."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    Button(
                        onClick = onOpenCoachAi,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Text(if (isSango) "Hunda" else "Ouvrir")
                    }
                }
            }
        }
        items(sortedContenus, key = { it.id }) { pack ->
            val title = if (isSango) pack.titreSg else pack.titreFr
            val resume = if (isSango) pack.resumeSg else pack.resumeFr
            val sizeMb = String.format("%.1f Mo", pack.tailleKo / 1000f)
            val progression = progressByContent[pack.id]
            val isRecommended = primaryTrade.isNotBlank() && pack.competenceId == primaryTrade

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = if (isRecommended) 1.5.dp else 1.dp,
                    color = if (isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${pack.dureeMin} min • $sizeMb",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            if (isRecommended) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isSango) "Nzoni" else "Conseillé",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (progression?.estTermine == true) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isSango) "Hunzi (${progression.scoreQuiz}%)" else "Terminé (${progression.scoreQuiz}%)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (pack.estTelecharge) {
                                Text(
                                    text = copy.downloadedLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AudioReadButton(
                                utteranceId = "pack_${pack.id}",
                                spokenText = "$title. $resume",
                                speaker = speaker
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = resume,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (progression != null && !progression.estTermine && progression.totalEtapes > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = {
                                (progression.etapeActuelle.toFloat() / progression.totalEtapes).coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (pack.estTelecharge) {
                        Button(
                            onClick = { onOpenLesson(pack.id) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("open_lesson_${pack.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    progression?.estTermine == true -> if (isSango) "Bâ encore" else "Revoir la leçon"
                                    progression != null -> if (isSango) "Gue na li ni" else "Continuer la leçon"
                                    else -> if (isSango) "To nda ti leçon" else "Ouvrir la leçon"
                                }
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onTogglePackDownload(pack) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("download_pack_${pack.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(copy.downloadPack)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProveProfileSection(
    state: LangoAppState.Ready,
    copy: LangoCopy,
    speaker: LangoSpeakerState,
    onOpenVerification: () -> Unit,
    onOpenMobileMoney: () -> Unit,
    onOpenCaisseDevis: () -> Unit,
    onAddPortfolioWork: (competenceId: String, titre: String, description: String, uri: Uri?) -> Unit,
    onDeletePortfolioWork: (PortfolioEntity) -> Unit,
    onOpenEditProfile: () -> Unit,
    onToggleAvailability: (Boolean) -> Unit
) {
    val isSango = state.prefs.langue == LangueCode.SG
    val context = LocalContext.current
    val score = state.profile?.scoreFiabilite ?: 25
    val isAvailable = state.profile?.disponible ?: true
    val bio = state.profile?.bioCourte.orEmpty()
    val photoPath = state.profile?.photoProfilUri.orEmpty()
    val validVerifs = state.verifications.filter { it.statut == "VALIDE" }
    val competencesById = remember(state.competences) {
        state.competences.associateBy { it.id }
    }
    val primaryTradeLabel = competencesById[state.profile?.metierPrincipal]?.let {
        if (isSango) it.nomSg else it.nomFr
    }.orEmpty()
    val passportCode = remember(state.user?.id) {
        val raw = state.user?.id?.removePrefix("usr_")?.uppercase() ?: "LANGO"
        "LG-$raw"
    }
    val caisseSummary = remember(state.caisseOperations) {
        LangoDomainLogic.computeCaisseSummary(state.caisseOperations)
    }

    var showPortfolioDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedPhotoUri = uri
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = copy.proveHeader,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Public Lango Passport Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            LangoAvatar(
                                photoPath = photoPath,
                                displayName = state.user?.prenom ?: "L",
                                size = 60.dp,
                                modifier = Modifier
                                    .clickable(onClick = onOpenEditProfile)
                                    .testTag("passport_avatar_button")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Text(
                                        text = "Passeport $passportCode",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = state.user?.prenom ?: "Lango",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (primaryTradeLabel.isNotBlank()) {
                                    Text(
                                        text = primaryTradeLabel,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${state.user?.ville ?: ""} • ${state.user?.quartier ?: ""} • ${state.user?.telephone ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                if (bio.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                        Row {
                            AudioReadButton(
                                utteranceId = "profile_passport",
                                spokenText = "Passeport Lango de ${state.user?.prenom ?: ""}. Ville ${state.user?.ville ?: ""}. Score de fiabilité $score sur 100. ${state.badges.size} badges et ${validVerifs.size} avis validés.",
                                speaker = speaker
                            )
                            IconButton(
                                onClick = { showQrDialog = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("open_qr_passport_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCode2,
                                    contentDescription = "QR Code Passeport Lango",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = onOpenEditProfile,
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("edit_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Modifier le profil",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    val badgesList = state.badges.joinToString(", ") {
                                        if (it.niveau == "VERIFIE") "${it.titreFr} (Vérifié)" else it.titreFr
                                    }.ifBlank { "En formation" }
                                    val shareBody = buildString {
                                        append("Passeport Lango RCA ($passportCode) — ${state.user?.prenom ?: ""}\n")
                                        append("Ville : ${state.user?.ville ?: ""} (${state.user?.quartier ?: ""})\n")
                                        append("Tél : ${state.user?.telephone ?: ""}\n")
                                        if (bio.isNotBlank()) append("Profil : $bio\n")
                                        append("Disponibilité : ${if (isAvailable) "Disponible immédiatement" else "Occupé(e)"}\n")
                                        append("Score de fiabilité : $score/100\n")
                                        append("Compétences : $badgesList\n")
                                        append("Réalisations portfolio : ${state.portfolio.size}")
                                    }
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareBody)
                                    }
                                    runCatching {
                                        context.startActivity(Intent.createChooser(shareIntent, "Partager ma carte Lango"))
                                    }
                                },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("share_lango_card_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = "Partager ma carte Lango",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    FilterChip(
                        selected = isAvailable,
                        onClick = { onToggleAvailability(!isAvailable) },
                        label = {
                            Text(
                                if (isAvailable) {
                                    if (isSango) "Disponible laso" else "Disponible pour missions"
                                } else {
                                    if (isSango) "Kua a yeke na mbi" else "Actuellement occupé(e)"
                                }
                            )
                        },
                        leadingIcon = if (isAvailable) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("toggle_availability_chip")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${copy.reliabilityScore} : $score / 100",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (score / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${state.badges.size}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = copy.badgesCountLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "${validVerifs.size} / 3",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = copy.verificationsGoalLabel,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Artisan Caisse & Devis + Mobile Money shortcuts
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenCaisseDevis,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("prove_open_caisse_devis_button")
                ) {
                    Icon(Icons.Rounded.ReceiptLong, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isSango) {
                            "Caisse & Devis (${caisseSummary.soldeNet} FCFA)"
                        } else {
                            "Cahier de caisse & Devis (${caisseSummary.soldeNet} FCFA)"
                        }
                    )
                }
                OutlinedButton(
                    onClick = onOpenMobileMoney,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("prove_open_mobile_money_button")
                ) {
                    Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSango) "Mobile Money / USSD (Orange & Telecel)" else "Services Mobile Money & USSD (Orange / Telecel)")
                }
            }
        }

        // Acquired Badges List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSango) "Abadge ti mbi" else "Badges de compétence",
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(
                    onClick = onOpenVerification,
                    modifier = Modifier.testTag("prove_open_verifications_button")
                ) {
                    Text(if (isSango) "Hunda 3 avis" else "Vérifier (3 tiers)")
                }
            }
        }

        if (state.badges.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = if (isSango) {
                            "Gue na « Manda » ti hunzi module oko si mo wara kozo badge."
                        } else {
                            "Aucun badge encore. Termine une micro-formation dans « Apprendre » !"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(state.badges, key = { it.id }) { badge ->
                val badgeTitle = if (isSango) badge.titreSg else badge.titreFr
                val isVerified = badge.niveau == "VERIFIE"
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = if (isVerified) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = badgeTitle, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (isVerified) "Certifié par 3 tiers" else "Acquis • En attente 3/3",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Portfolio Section
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSango) "Portfolio (Akua ti mbi)" else "Portfolio de réalisations",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedButton(
                    onClick = { showPortfolioDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("add_portfolio_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSango) "Zia foto" else "Ajouter")
                }
            }
        }

        if (state.portfolio.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = if (isSango) {
                            "Zia afoto ti akua so mo sara (bongo, panneau, kua...) ti fa na aclient."
                        } else {
                            "Ajoute des photos de tes réalisations (couture, réparation, installation...) compressées pour le mode hors-ligne."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(state.portfolio, key = { it.id }) { work ->
                val compName = competencesById[work.competenceId]?.let {
                    if (isSango) it.nomSg else it.nomFr
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (work.photoLocaleUri.isNotBlank()) {
                            AsyncImage(
                                model = File(work.photoLocaleUri),
                                contentDescription = work.titre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            if (!compName.isNullOrBlank()) {
                                Text(
                                    text = compName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = work.titre, style = MaterialTheme.typography.titleMedium)
                            if (work.descriptionCourte.isNotBlank()) {
                                Text(
                                    text = work.descriptionCourte,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { onDeletePortfolioWork(work) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_portfolio_${work.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Supprimer",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Validated Client Reviews
        if (validVerifs.isNotEmpty()) {
            item {
                Text(
                    text = if (isSango) "Avis ti aclient" else "Avis validés",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            items(validVerifs, key = { it.id }) { review ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${review.verificateurNom} (${review.relation})",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${review.note}/5", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        if (review.commentaire.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "« ${review.commentaire} »",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPortfolioDialog) {
        var title by remember { mutableStateOf("") }
        var desc by remember { mutableStateOf("") }
        var selectedComp by remember {
            mutableStateOf(
                state.profile?.metierPrincipal?.ifBlank { null }
                    ?: state.competences.firstOrNull()?.id
                    ?: "comp_couture"
            )
        }

        AlertDialog(
            onDismissRequest = {
                showPortfolioDialog = false
                pickedPhotoUri = null
            },
            title = { Text(if (isSango) "Fini kua ti portfolio" else "Nouvelle réalisation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isSango) "Métier :" else "Compétence associée :",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.competences, key = { it.id }) { comp ->
                            FilterChip(
                                selected = selectedComp == comp.id,
                                onClick = { selectedComp = comp.id },
                                label = { Text(if (isSango) comp.nomSg else comp.nomFr) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            )
                        }
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isSango) "Iri ti kua (ex: Robe pagne)" else "Titre (ex: Tenue wax sur mesure)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text(if (isSango) "Kete tene" else "Description courte") },
                        maxLines = 2
                    )
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.AddAPhoto, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (pickedPhotoUri != null) {
                                "Photo sélectionnée (prête)"
                            } else {
                                "Choisir une photo (optionnel)"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = title.trim().length >= 2,
                    onClick = {
                        onAddPortfolioWork(selectedComp, title, desc, pickedPhotoUri)
                        pickedPhotoUri = null
                        showPortfolioDialog = false
                    },
                    modifier = Modifier.testTag("save_portfolio_confirm_button")
                ) {
                    Text(if (isSango) "Bata" else "Enregistrer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pickedPhotoUri = null
                        showPortfolioDialog = false
                    }
                ) {
                    Text(copy.cancel)
                }
            }
        )
    }
    if (showQrDialog) {
        QrPassportDialog(
            langue = state.prefs.langue,
            passportCode = passportCode,
            ownerName = state.user?.prenom ?: "Artisan Lango",
            city = state.user?.ville.orEmpty(),
            phone = state.user?.telephone.orEmpty(),
            score = score,
            badgesVerifiedRatio = "${validVerifs.size}/3",
            tradeLabel = primaryTradeLabel,
            onDismiss = { showQrDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectOpportunitiesSection(
    state: LangoAppState.Ready,
    copy: LangoCopy,
    speaker: LangoSpeakerState,
    onOpenEmployerSpace: () -> Unit,
    onOpenPublishOpportunity: () -> Unit,
    onSelectCityFilter: (String?) -> Unit,
    onSelectTypeFilter: (String?) -> Unit,
    onOpenOpportunity: (String) -> Unit
) {
    val isSango = state.prefs.langue == LangueCode.SG
    var subTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Offres, 1 = Mes candidatures
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var onlyRecommended by rememberSaveable { mutableStateOf(false) }
    var showVerifyQrDialog by remember { mutableStateOf(false) }

    val primaryTradeForQr = state.competences
        .firstOrNull { it.id == state.profile?.metierPrincipal }
        ?.nomFr
        .orEmpty()
    val localPassportPayload = remember(state.user, state.profile, state.verifications, primaryTradeForQr) {
        state.user?.let { usr ->
            val validCount = state.verifications.count { it.statut == "VALIDE" }
            LangoDomainLogic.encodePassportQrPayload(
                passportCode = "LG-${usr.id.removePrefix("usr_").uppercase()}",
                ownerName = usr.prenom,
                city = usr.ville,
                phone = usr.telephone,
                score = state.profile?.scoreFiabilite ?: 25,
                badgesVerifiedRatio = "$validCount/3",
                tradeLabel = primaryTradeForQr
            )
        }
    }

    val userCity = state.user?.ville.orEmpty()
    val primaryTrade = state.profile?.metierPrincipal.orEmpty()
    val earnedCompIds = remember(state.badges) {
        state.badges.map { it.competenceId }.toSet()
    }

    val appliedIds = remember(state.candidatures) {
        state.candidatures.associateBy { it.opportuniteId }
    }
    val oppsById = remember(state.opportunites) {
        state.opportunites.associateBy { it.id }
    }
    val filtered = remember(
        state.opportunites,
        state.selectedCityFilter,
        state.selectedTypeFilter,
        searchQuery,
        onlyRecommended,
        userCity,
        primaryTrade,
        earnedCompIds
    ) {
        val q = searchQuery.trim().lowercase()
        state.opportunites.filter { opp ->
            val cityOk = state.selectedCityFilter == null || opp.ville == state.selectedCityFilter
            val typeOk = state.selectedTypeFilter == null || opp.type == state.selectedTypeFilter
            val queryOk = q.isEmpty() ||
                opp.titre.lowercase().contains(q) ||
                opp.employeurNom.lowercase().contains(q) ||
                opp.ville.lowercase().contains(q) ||
                opp.quartier.lowercase().contains(q)
            val recOk = !onlyRecommended || LangoDomainLogic.isRecommendedOpportunity(
                opp = opp,
                userCity = userCity,
                primaryTradeId = primaryTrade,
                earnedCompetenceIds = earnedCompIds
            )
            cityOk && typeOk && queryOk && recOk
        }.sortedByDescending { opp ->
            LangoDomainLogic.isRecommendedOpportunity(
                opp = opp,
                userCity = userCity,
                primaryTradeId = primaryTrade,
                earnedCompetenceIds = earnedCompIds
            )
        }
    }
    val offerTypes = listOf("EMPLOI", "MISSION", "STAGE", "SERVICE")

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = copy.connectHeader,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = onOpenPublishOpportunity,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("open_publish_opp_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isSango) "Zia kua" else "Publier")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenEmployerSpace,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("open_employer_space_button")
                ) {
                    Icon(Icons.Rounded.SupervisorAccount, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Recruteur (${state.candidaturesRecues.size})")
                }
                OutlinedButton(
                    onClick = { showVerifyQrDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("connect_verify_qr_button")
                ) {
                    Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Vérifier QR")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    modifier = Modifier.testTag("connect_subtab_offers")
                ) {
                    Text(if (isSango) "Akua (${filtered.size})" else "Offres (${filtered.size})")
                }
                SegmentedButton(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    modifier = Modifier.testTag("connect_subtab_applications")
                ) {
                    Text(
                        if (isSango) {
                            "Candidatures (${state.candidatures.size})"
                        } else {
                            "Mes envois (${state.candidatures.size})"
                        }
                    )
                }
            }
        }

        if (subTab == 0) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isSango) "Gi kua, gbata, patron..." else "Rechercher métier, quartier, employeur...")
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Effacer la recherche")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("connect_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = onlyRecommended,
                            onClick = { onlyRecommended = !onlyRecommended },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(if (isSango) "Ti mbi" else "Pour moi") },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("connect_filter_recommended")
                        )
                    }
                    item {
                        FilterChip(
                            selected = state.selectedCityFilter == null,
                            onClick = { onSelectCityFilter(null) },
                            label = { Text(copy.filterAllCities) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                    items(LangoSeedData.rcaVilles) { city ->
                        FilterChip(
                            selected = state.selectedCityFilter == city,
                            onClick = { onSelectCityFilter(city) },
                            label = { Text(city) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedTypeFilter == null,
                            onClick = { onSelectTypeFilter(null) },
                            label = { Text(if (isSango) "Kue" else "Tous types") },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                    items(offerTypes) { type ->
                        FilterChip(
                            selected = state.selectedTypeFilter == type,
                            onClick = { onSelectTypeFilter(type) },
                            label = { Text(type) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = if (isSango) {
                                "Kua a yeke pepe na filtre so."
                            } else {
                                "Aucune offre ne correspond à ce filtre. Essaie « Toutes les villes » ou efface ta recherche."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(filtered, key = { it.id }) { opp ->
                    val candidature = appliedIds[opp.id]
                    val isRecommended = LangoDomainLogic.isRecommendedOpportunity(
                        opp = opp,
                        userCity = userCity,
                        primaryTradeId = primaryTrade,
                        earnedCompetenceIds = earnedCompIds
                    )
                    OpportunityCard(
                        opp = opp,
                        speaker = speaker,
                        isRecommended = isRecommended,
                        appliedStatus = candidature?.statut,
                        applyLabel = if (candidature != null) {
                            if (isSango) "A tokua awe" else "Voir ma candidature"
                        } else {
                            copy.applyOneClick
                        },
                        onOpenDetail = { onOpenOpportunity(opp.id) }
                    )
                }
            }
        } else {
            if (state.candidatures.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text = if (isSango) {
                                "Mo tokua iri ti mo na kua oko pepe encore."
                            } else {
                                "Aucune candidature envoyée. Ouvre une offre pour postuler en 1 clic ou par SMS."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(state.candidatures, key = { it.id }) { cand ->
                    val opp = oppsById[cand.opportuniteId]
                    val isQueued = cand.statut == "BROUILLON_OFFLINE"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = opp != null) {
                                opp?.let { onOpenOpportunity(it.id) }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opp?.titre ?: "Opportunité",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isQueued) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.primaryContainer
                                    }
                                ) {
                                    Text(
                                        text = when {
                                            isQueued -> "En file hors-ligne"
                                            cand.statut == "RETENUE" -> "Retenue !"
                                            cand.statut == "REFUSEE" -> "Non retenue"
                                            cand.statut == "VUE" -> "Vue par recruteur"
                                            else -> "Envoyée (${cand.canalEnvoi})"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            if (opp != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${opp.employeurNom} • ${opp.ville} • ${opp.remunerationFcfa} FCFA",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (cand.noteEmployeur.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Retour employeur : ${cand.noteEmployeur}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVerifyQrDialog) {
        QrVerifyPassportDialog(
            langue = state.prefs.langue,
            localUserPassportPayload = localPassportPayload,
            knownCandidates = state.candidaturesRecues,
            onDismiss = { showVerifyQrDialog = false }
        )
    }
}

@Composable
private fun OpportunityCard(
    opp: OpportuniteEntity,
    speaker: LangoSpeakerState,
    isRecommended: Boolean,
    appliedStatus: String?,
    applyLabel: String,
    onOpenDetail: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail)
            .testTag("opp_card_${opp.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (appliedStatus != null || isRecommended) 1.5.dp else 1.dp,
            color = if (appliedStatus != null || isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = opp.type,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    if (isRecommended) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "Recommandé",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (appliedStatus != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = if (appliedStatus == "BROUILLON_OFFLINE") "En file" else "Postulé",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${opp.remunerationFcfa} FCFA",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    AudioReadButton(
                        utteranceId = "opp_${opp.id}",
                        spokenText = "${opp.titre} chez ${opp.employeurNom}, à ${opp.ville}. Rémunération ${opp.remunerationFcfa} francs CFA. ${opp.descriptionCourte}",
                        speaker = speaker
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = opp.titre, style = MaterialTheme.typography.titleLarge)
            Text(
                text = opp.employeurNom,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${opp.ville} (${opp.quartier})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = opp.descriptionCourte,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onOpenDetail,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("opp_apply_btn_${opp.id}")
            ) {
                Text(applyLabel)
            }
        }
    }
}
