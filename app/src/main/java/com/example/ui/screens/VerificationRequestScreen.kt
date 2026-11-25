package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.LangueCode
import com.example.data.local.entity.VerificationEntity

@Composable
fun VerificationRequestScreen(
    langue: String,
    userName: String,
    badges: List<BadgeEntity>,
    verifications: List<VerificationEntity>,
    onBack: () -> Unit,
    onSubmitRequest: (badgeId: String, nom: String, tel: String, relation: String, canal: String) -> Unit,
    onConfirmVerification: (verifId: String, note: Int, commentaire: String) -> Unit
) {
    val isSango = langue == LangueCode.SG
    val context = LocalContext.current

    var selectedBadgeId by remember(badges) {
        mutableStateOf(badges.firstOrNull()?.id.orEmpty())
    }
    var verifNom by remember { mutableStateOf("") }
    var verifTel by remember { mutableStateOf("+236 ") }
    var selectedRelation by remember { mutableStateOf("CLIENT") }
    var validatingVerif by remember { mutableStateOf<VerificationEntity?>(null) }

    val selectedBadge = badges.firstOrNull { it.id == selectedBadgeId } ?: badges.firstOrNull()
    val badgeVerifs = remember(verifications, selectedBadge?.id) {
        if (selectedBadge == null) emptyList()
        else verifications.filter { it.badgeId == selectedBadge.id }
    }
    val validCount = badgeVerifs.count { it.statut == "VALIDE" }

    val canSubmit = selectedBadge != null &&
        verifNom.trim().length >= 2 &&
        verifTel.filter { it.isDigit() }.length >= 8

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("verification_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSango) "Fa kua na azo ota" else "Vérification par 3 tiers",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (badges.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = if (isSango) {
                                "Manda leçon oko kozo ti wara badge"
                            } else {
                                "Termine d’abord un module dans « Apprendre » pour débloquer ton premier badge à faire vérifier."
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            } else {
                // Select Badge
                Text(
                    text = if (isSango) "Soro badge ti mo :" else "Choisis le badge à certifier :",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badges.forEach { badge ->
                        val title = if (isSango) badge.titreSg else badge.titreFr
                        FilterChip(
                            selected = selectedBadge?.id == badge.id,
                            onClick = { selectedBadgeId = badge.id },
                            label = { Text(title) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Progress 0/3 -> 3/3 card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (validCount >= 3) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.tertiaryContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSango) {
                                    "Azo so a yeda : $validCount / 3"
                                } else {
                                    "Preuves validées : $validCount / 3"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (validCount / 3f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (validCount >= 3) {
                                if (isSango) "Badge ti mo a ga VÉRIFIÉ awe !" else "Badge certifié Vérifié auprès des employeurs !"
                            } else {
                                if (isSango) "Hunda aclient wala amaitre ota ti fa kua ti mo." else "Invite 3 anciens clients, pairs ou maîtres artisans (via app ou SMS)."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Request Form
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isSango) "Fini hundango tene" else "Nouvelle demande",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = verifNom,
                            onValueChange = { verifNom = it },
                            label = { Text(if (isSango) "Iri ti client / maître" else "Nom du client ou pair") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verif_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = verifTel,
                            onValueChange = { verifTel = it },
                            label = { Text(if (isSango) "Numéro (+236)" else "Numéro de téléphone (+236)") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("verif_phone_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("CLIENT" to "Client", "PAIR" to "Pair", "MAITRE" to "Maître").forEach { (code, label) ->
                                FilterChip(
                                    selected = selectedRelation == code,
                                    onClick = { selectedRelation = code },
                                    label = { Text(label) },
                                    modifier = Modifier.minimumInteractiveComponentSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val badge = selectedBadge ?: return@OutlinedButton
                                    onSubmitRequest(badge.id, verifNom, verifTel, selectedRelation, "APP")
                                    verifNom = ""
                                    verifTel = "+236 "
                                },
                                enabled = canSubmit,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("verif_submit_app_button")
                            ) {
                                Text(if (isSango) "Bata (App)" else "Via App")
                            }

                            Button(
                                onClick = {
                                    val badge = selectedBadge ?: return@Button
                                    val smsBody = "Lango RCA : $userName demande votre confirmation pour sa compétence '${badge.titreFr}'. Répondez OUI + note 1 à 5."
                                    onSubmitRequest(badge.id, verifNom, verifTel, selectedRelation, "SMS")
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:${verifTel.trim()}")
                                        putExtra("sms_body", smsBody)
                                    }
                                    runCatching { context.startActivity(intent) }
                                    verifNom = ""
                                    verifTel = "+236 "
                                },
                                enabled = canSubmit,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("verif_submit_sms_button")
                            ) {
                                Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isSango) "Tokua SMS" else "Envoyer SMS")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isSango) "Demandes ti mbi (${badgeVerifs.size})" else "Suivi des demandes (${badgeVerifs.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                badgeVerifs.forEach { verif ->
                    val isValide = verif.statut == "VALIDE"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${verif.verificateurNom} (${verif.relation})",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "${verif.verificateurTel} • Canal ${verif.canal}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isValide) {
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
                                            imageVector = if (isValide) Icons.Rounded.CheckCircle else Icons.Rounded.HourglassTop,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isValide) "Validé (${verif.note}/5)" else "En attente",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                            if (isValide && verif.commentaire.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "« ${verif.commentaire} »",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else if (!isValide) {
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { validatingVerif = verif },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .minimumInteractiveComponentSize()
                                        .testTag("confirm_verif_btn_${verif.id}")
                                ) {
                                    Text(if (isSango) "Sû retour ti lo" else "Enregistrer la confirmation reçue")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    validatingVerif?.let { target ->
        var rating by remember { mutableIntStateOf(5) }
        var comment by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { validatingVerif = null },
            title = { Text("Confirmation de ${target.verificateurNom}") },
            text = {
                Column {
                    Text("Note attribuée (1 à 5 étoiles) :")
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "$star étoiles",
                                    tint = if (star <= rating) {
                                        MaterialTheme.colorScheme.tertiary
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Avis court du client / pair") },
                        singleLine = false,
                        maxLines = 2
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirmVerification(target.id, rating, comment)
                        validatingVerif = null
                    },
                    modifier = Modifier.testTag("dialog_confirm_verif_save")
                ) {
                    Text("Valider")
                }
            },
            dismissButton = {
                TextButton(onClick = { validatingVerif = null }) {
                    Text("Annuler")
                }
            }
        )
    }
}
