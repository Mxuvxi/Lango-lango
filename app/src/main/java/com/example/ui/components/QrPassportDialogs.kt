package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.LangoDomainLogic
import com.example.data.local.OfflinePassportVerification
import com.example.data.local.entity.CandidatureRecueEntity
import com.example.data.local.entity.LangueCode
import com.example.ui.util.QrCodeGenerator

@Composable
fun QrCodeCanvas(
    payload: String,
    modifier: Modifier = Modifier
) {
    val matrix = remember(payload) { QrCodeGenerator.encode(payload) }
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val count = matrix.size
            val cellSize = size.width / count.toFloat()
            drawRect(color = Color.White, size = size)
            for (r in 0 until count) {
                for (c in 0 until count) {
                    if (matrix.isDark(r, c)) {
                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize + 0.6f, cellSize + 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QrPassportDialog(
    langue: String,
    passportCode: String,
    ownerName: String,
    city: String,
    phone: String,
    score: Int,
    badgesVerifiedRatio: String,
    tradeLabel: String,
    onDismiss: () -> Unit
) {
    val isSango = langue == LangueCode.SG
    val context = LocalContext.current
    val qrPayload = remember(passportCode, ownerName, city, phone, score, badgesVerifiedRatio, tradeLabel) {
        LangoDomainLogic.encodePassportQrPayload(
            passportCode = passportCode,
            ownerName = ownerName,
            city = city,
            phone = phone,
            score = score,
            badgesVerifiedRatio = badgesVerifiedRatio,
            tradeLabel = tradeLabel
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.QrCode2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(if (isSango) "QR Passeport Lango" else "QR Code Passeport Lango")
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isSango) {
                        "Fa QR so na patron wala client ti vérifier kua ti mo sân Internet."
                    } else {
                        "Fais scanner ou partage ce code pour prouver ton profil hors-ligne."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    QrCodeCanvas(
                        payload = qrPayload,
                        modifier = Modifier
                            .size(210.dp)
                            .testTag("passport_qr_canvas")
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$passportCode • $score/100 • $badgesVerifiedRatio badges",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, qrPayload)
                        }
                        runCatching {
                            context.startActivity(Intent.createChooser(intent, "Partager code QR Lango"))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("share_qr_payload_button")
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isSango) "Tokua code QR" else "Partager le code vérifiable")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_qr_passport_button")
            ) {
                Text(if (isSango) "Kanga" else "Fermer")
            }
        }
    )
}

@Composable
fun QrVerifyPassportDialog(
    langue: String,
    localUserPassportPayload: String?,
    knownCandidates: List<CandidatureRecueEntity>,
    onDismiss: () -> Unit
) {
    val isSango = langue == LangueCode.SG
    var inputCode by remember { mutableStateOf("") }
    var verifiedResult by remember { mutableStateOf<OfflinePassportVerification?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun verifyNow(raw: String) {
        val parsed = LangoDomainLogic.parsePassportQrPayload(raw)
        if (parsed == null) {
            verifiedResult = null
            errorMessage = if (isSango) {
                "Code Lango so a yeke nzoni pepe (tâ: LANGO1|LG-... wala LG-PY8401)."
            } else {
                "Code invalide. Colle un code QR Lango (LANGO1|...) ou un identifiant LG-XXXXXX."
            }
            return
        }
        errorMessage = null
        if (parsed.ownerName.isNotBlank()) {
            verifiedResult = parsed
            return
        }
        // Lookup short LG-... code locally
        val localParsed = localUserPassportPayload?.let { LangoDomainLogic.parsePassportQrPayload(it) }
        if (localParsed != null && localParsed.passportCode.equals(parsed.passportCode, ignoreCase = true)) {
            verifiedResult = localParsed
            return
        }
        val matchedCandidate = knownCandidates.firstOrNull {
            it.passeportCode.equals(parsed.passportCode, ignoreCase = true)
        }
        if (matchedCandidate != null) {
            verifiedResult = OfflinePassportVerification(
                isValid = true,
                passportCode = matchedCandidate.passeportCode,
                ownerName = matchedCandidate.candidatNom,
                city = matchedCandidate.candidatVille,
                phone = matchedCandidate.candidatTel,
                score = matchedCandidate.scoreFiabilite,
                badgesSummary = "${matchedCandidate.badgesValides}/3",
                tradeLabel = matchedCandidate.metierLabel
            )
        } else {
            verifiedResult = null
            errorMessage = "Aucun passeport local trouvé pour ${parsed.passportCode}. Demande le QR complet (LANGO1|...) du candidat."
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.QrCodeScanner,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(if (isSango) "Vérifier Passeport sân réseau" else "Vérifier un Passeport Lango")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Vérification 100% hors-ligne : colle le texte QR Lango ou saisis un numéro (ex : LG-PY8401).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { inputCode = it },
                    label = { Text("Code QR ou LG-XXXXXX") },
                    placeholder = { Text("LG-PY8401 ou LANGO1|...") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("verify_passport_input")
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            inputCode = "LG-PY8401"
                            verifyNow("LG-PY8401")
                        }
                    ) {
                        Text("Tester LG-PY8401")
                    }
                    if (!localUserPassportPayload.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                inputCode = localUserPassportPayload
                                verifyNow(localUserPassportPayload)
                            }
                        ) {
                            Text("Mon QR")
                        }
                    }
                }
                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                verifiedResult?.let { res ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("verified_passport_result_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                    text = "Passeport Lango Authentique",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${res.ownerName} (${res.passportCode})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "${res.tradeLabel} • ${res.city}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Fiabilité : ${res.score}/100 • Badges : ${res.badgesSummary} • Tél : ${res.phone}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { verifyNow(inputCode) },
                enabled = inputCode.trim().length >= 5,
                modifier = Modifier.testTag("verify_passport_submit_button")
            ) {
                Text("Vérifier")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
