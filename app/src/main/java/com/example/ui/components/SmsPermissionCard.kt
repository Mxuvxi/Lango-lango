package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.entity.LangueCode
import com.example.service.SmsPermissionService
import com.example.service.SmsPermissionSnapshot

@Stable
class SmsPermissionController(
    val snapshot: SmsPermissionSnapshot,
    val shouldShowRationale: Boolean,
    val hasRequestedOnce: Boolean,
    val requestSmsPermissions: () -> Unit,
    val openAppSettings: () -> Unit
)

@Composable
fun rememberSmsPermissionController(): SmsPermissionController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var snapshot by remember { mutableStateOf(SmsPermissionService.checkPermissions(context)) }
    var shouldShowRationale by remember { mutableStateOf(SmsPermissionService.shouldShowRationale(context)) }
    var hasRequestedOnce by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasRequestedOnce = true
        snapshot = SmsPermissionService.checkPermissions(context)
        shouldShowRationale = SmsPermissionService.shouldShowRationale(context)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                snapshot = SmsPermissionService.checkPermissions(context)
                shouldShowRationale = SmsPermissionService.shouldShowRationale(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return remember(snapshot, shouldShowRationale, hasRequestedOnce) {
        SmsPermissionController(
            snapshot = snapshot,
            shouldShowRationale = shouldShowRationale,
            hasRequestedOnce = hasRequestedOnce,
            requestSmsPermissions = {
                launcher.launch(SmsPermissionService.requiredSmsPermissions)
            },
            openAppSettings = {
                runCatching {
                    context.startActivity(SmsPermissionService.buildAppSettingsIntent(context))
                }
            }
        )
    }
}

@Composable
fun SmsPermissionManagementCard(
    langue: String,
    controller: SmsPermissionController,
    modifier: Modifier = Modifier
) {
    val isSango = langue == LangueCode.SG
    var showDisclosureDialog by remember { mutableStateOf(false) }
    val snap = controller.snapshot

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (snap.isFullyGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = if (snap.isFullyGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSango) {
                        "Permissions SMS & Mode dégradé"
                    } else {
                        "Permissions SMS & Mode dégradé USSD"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isSango) {
                    "Mû lege na Lango ti tokua na ti wara asango ti kua na SMS tongana Internet a yeke pepe. Tongana mo ke, Lango a yeke sala kua na SMS ti téléfone ti mo."
                } else {
                    "Prépare l'envoi direct et la réception automatique des alertes SMS/USSD sans Internet. En cas de refus, le mode repli sans permission reste actif."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            PermissionStatusRow(
                label = if (isSango) "Envoi SMS direct (SEND_SMS)" else "Envoi SMS direct (SEND_SMS)",
                granted = snap.canSendSms,
                fallbackText = "Repli composeur"
            )
            Spacer(modifier = Modifier.height(6.dp))
            PermissionStatusRow(
                label = if (isSango) "Réception alertes USSD (RECEIVE_SMS)" else "Réception alertes USSD (RECEIVE_SMS)",
                granted = snap.canReceiveSms,
                fallbackText = "Manuel"
            )

            if (!snap.hasTelephonyHardware) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cet appareil ne possède pas de module SIM/téléphonie : seul le repli via application SMS externe est utilisé.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!snap.isFullyGranted) {
                val permanentlyDenied = controller.hasRequestedOnce && !controller.shouldShowRationale && !snap.isFullyGranted
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showDisclosureDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("sms_permission_request_button")
                    ) {
                        Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isSango) "Yeda SMS" else "Autoriser SMS")
                    }
                    if (permanentlyDenied || controller.shouldShowRationale) {
                        OutlinedButton(
                            onClick = controller.openAppSettings,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("sms_permission_settings_button")
                        ) {
                            Icon(Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paramètres")
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSango) "Mode dégradé SMS prêt" else "Mode dégradé SMS & USSD actif",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(
                        onClick = controller.openAppSettings,
                        modifier = Modifier.testTag("sms_permission_manage_button")
                    ) {
                        Text("Gérer")
                    }
                }
            }
        }
    }

    if (showDisclosureDialog) {
        AlertDialog(
            onDismissRequest = { showDisclosureDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Sms,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    if (isSango) "Tene ti batango sango (Loi 24.001)" else "Utilisation des SMS (Loi n° 24.001)"
                )
            },
            text = {
                Text(
                    text = if (isSango) {
                        "Lango a yeke tokua gï acommande LANGO na alerte USSD tongana réseau a yeke pepe. Lango a yeke diko atene ti mo ti personnel pepe."
                    } else {
                        "Lango utilise uniquement ces permissions pour envoyer tes candidatures/commandes USSD hors-ligne et détecter les confirmations d'offres ou Mobile Money Lango. Tes SMS privés ne sont jamais lus ni transmis."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDisclosureDialog = false
                        controller.requestSmsPermissions()
                    },
                    modifier = Modifier.testTag("sms_permission_disclosure_confirm")
                ) {
                    Text(if (isSango) "Gue na li ni" else "Continuer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosureDialog = false }) {
                    Text(if (isSango) "Zia" else "Plus tard")
                }
            }
        )
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    granted: Boolean,
    fallbackText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = if (granted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (granted) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (granted) "Autorisé" else fallbackText,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
