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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.BadgeEntity
import com.example.data.local.entity.CandidatureEntity
import com.example.data.local.entity.LangueCode
import com.example.data.local.entity.OpportuniteEntity
import com.example.data.local.entity.ProfilEntity
import com.example.data.local.entity.UtilisateurEntity
import com.example.ui.components.LangoAvatar

@Composable
fun OpportunityDetailScreen(
    opportunite: OpportuniteEntity,
    langue: String,
    user: UtilisateurEntity?,
    profile: ProfilEntity?,
    badges: List<BadgeEntity>,
    existingCandidature: CandidatureEntity?,
    isOffline: Boolean,
    onBack: () -> Unit,
    onApplyOneClick: (message: String, canal: String) -> Unit
) {
    val isSango = langue == LangueCode.SG
    val context = LocalContext.current
    var messageCourt by remember { mutableStateOf("") }

    val verifiedCount = badges.count { it.niveau == "VERIFIE" }
    val score = profile?.scoreFiabilite ?: 25
    val userName = user?.prenom ?: "Candidat Lango"
    val userCity = user?.ville ?: "RCA"
    val userPhone = user?.telephone ?: ""

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
                        .testTag("opp_detail_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSango) "Sango ti kua" else "Détail de l’opportunité",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Offer Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = opportunite.type,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "${opportunite.remunerationFcfa} FCFA",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = opportunite.titre, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = opportunite.employeurNom,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${opportunite.ville} • ${opportunite.quartier}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = opportunite.descriptionCourte,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${opportunite.contactTel}"))
                            runCatching { context.startActivity(dialIntent) }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${if (isSango) "Iri :" else "Appeler :"} ${opportunite.contactTel}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lango Profile Preview Card (sent in 1 click)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                            text = if (isSango) "Profil Lango so mo yeke tokua" else "Ton profil Lango joint automatiquement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LangoAvatar(
                            photoPath = profile?.photoProfilUri.orEmpty(),
                            displayName = userName,
                            size = 44.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$userName • $userCity ($userPhone)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (isSango) {
                                    "Fiabilité : $score/100 • ${badges.size} badge(s) ($verifiedCount vérifiés)"
                                } else {
                                    "Fiabilité : $score/100 • ${badges.size} badge(s) dont $verifiedCount vérifié(s)"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (existingCandidature != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (existingCandidature.statut == "BROUILLON_OFFLINE") {
                                    if (isSango) "A bata na file d’attente" else "Candidature enregistrée hors-ligne"
                                } else {
                                    if (isSango) "Iri ti mo a tokua awe !" else "Candidature envoyée (${existingCandidature.canalEnvoi})"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (existingCandidature.statut == "BROUILLON_OFFLINE") {
                                    "Envoi automatique dès le retour du réseau, ou utilise le SMS ci-dessous."
                                } else {
                                    "L’employeur peut voir ton score de fiabilité et tes badges."
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = messageCourt,
                    onValueChange = { messageCourt = it },
                    label = {
                        Text(if (isSango) "Kete tene (optionnel)" else "Message court à l’employeur (optionnel)")
                    },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_message_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onApplyOneClick(messageCourt, "APP") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("apply_one_click_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOffline) {
                            if (isSango) "Bata iri ti mbi (Hors-ligne)" else "Postuler (mise en file hors-ligne)"
                        } else {
                            if (isSango) "Tokua iri ti mbi hio" else "Postuler en 1 clic"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        val smsText = "Lango RCA - Candidature: $userName ($userCity, $userPhone). Fiabilite $score/100, ${badges.size} badge(s). Offre: ${opportunite.titre}. ${messageCourt.trim()}"
                        onApplyOneClick(messageCourt, "SMS")
                        val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${opportunite.contactTel}")).apply {
                            putExtra("sms_body", smsText)
                        }
                        runCatching { context.startActivity(smsIntent) }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("apply_sms_fallback_button")
                ) {
                    Icon(Icons.Rounded.Sms, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isSango) "Tokua na SMS (sân Internet)" else "Postuler par SMS (sans Internet)")
                }
            }
        }
    }
}
