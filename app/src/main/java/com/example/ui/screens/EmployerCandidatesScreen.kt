package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.data.local.entity.CandidatureRecueEntity
import com.example.data.local.entity.LangueCode
import com.example.data.local.entity.OpportuniteEntity
import com.example.ui.components.QrVerifyPassportDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerCandidatesScreen(
    langue: String,
    opportunites: List<OpportuniteEntity>,
    candidaturesRecues: List<CandidatureRecueEntity>,
    localUserPassportPayload: String?,
    onBack: () -> Unit,
    onUpdateCandidateStatus: (CandidatureRecueEntity, String, String) -> Unit
) {
    val isSango = langue == LangueCode.SG
    val context = LocalContext.current
    var selectedOfferId by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }
    var editingCandidate by remember { mutableStateOf<CandidatureRecueEntity?>(null) }
    var showQrVerifier by remember { mutableStateOf(false) }

    val oppsById = remember(opportunites) { opportunites.associateBy { it.id } }
    val filtered = remember(candidaturesRecues, selectedOfferId, selectedStatus) {
        candidaturesRecues.filter { cand ->
            val offerOk = selectedOfferId == null || cand.opportuniteId == selectedOfferId
            val statusOk = selectedStatus == null || cand.statut == selectedStatus
            offerOk && statusOk
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isSango) "Espace Patron / ONG" else "Espace Employeur & ONG")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("employer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQrVerifier = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("employer_verify_qr_action")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.QrCodeScanner,
                            contentDescription = "Vérifier un QR Passeport Lango"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${candidaturesRecues.size} candidatures reçues",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Classement par score de fiabilité & badges vérifiés (mode hors-ligne).",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(
                                onClick = { showQrVerifier = true },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Icon(Icons.Rounded.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vérifier QR")
                            }
                        }
                    }
                }

                // Filters
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedStatus == null,
                                onClick = { selectedStatus = null },
                                label = { Text("Tous (${candidaturesRecues.size})") }
                            )
                        }
                        items(listOf("ENVOYEE", "VUE", "RETENUE", "REFUSEE")) { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = if (selectedStatus == st) null else st },
                                label = { Text(st) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedOfferId == null,
                                onClick = { selectedOfferId = null },
                                label = { Text("Toutes les offres") }
                            )
                        }
                        items(opportunites, key = { it.id }) { opp ->
                            FilterChip(
                                selected = selectedOfferId == opp.id,
                                onClick = { selectedOfferId = if (selectedOfferId == opp.id) null else opp.id },
                                label = { Text(opp.titre.take(24)) }
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
                                text = "Aucune candidature pour ce filtre.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(filtered, key = { it.id }) { cand ->
                        val opp = oppsById[cand.opportuniteId]
                        val isRetained = cand.statut == "RETENUE"
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                width = if (isRetained) 1.5.dp else 1.dp,
                                color = if (isRetained) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cand.candidatNom,
                                            style = MaterialTheme.typography.titleLarge
                                        )
                                        Text(
                                            text = "${cand.metierLabel} • ${cand.candidatVille} • ${cand.passeportCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = when (cand.statut) {
                                            "RETENUE" -> MaterialTheme.colorScheme.primaryContainer
                                            "REFUSEE" -> MaterialTheme.colorScheme.errorContainer
                                            else -> MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    ) {
                                        Text(
                                            text = cand.statut,
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (opp != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Offre : ${opp.titre}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Verified,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Fiabilité ${cand.scoreFiabilite}/100 • ${cand.badgesValides} badges",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "« ${cand.messageCourt} »",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (cand.noteEmployeur.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Note recruteur : ${cand.noteEmployeur}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cand.candidatTel}"))
                                            runCatching { context.startActivity(dialIntent) }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Appeler")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val smsBody = "Bonjour ${cand.candidatNom}, suite à ta candidature Lango (${opp?.titre ?: "mission"}), nous souhaitons échanger avec toi."
                                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("smsto:${cand.candidatTel}")
                                                putExtra("sms_body", smsBody)
                                            }
                                            runCatching { context.startActivity(smsIntent) }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("SMS")
                                    }
                                    Button(
                                        onClick = { editingCandidate = cand },
                                        modifier = Modifier
                                            .weight(1f)
                                            .minimumInteractiveComponentSize()
                                            .testTag("decide_candidate_${cand.id}")
                                    ) {
                                        Text("Décider")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editingCandidate?.let { cand ->
        var note by remember(cand.id) { mutableStateOf(cand.noteEmployeur) }
        AlertDialog(
            onDismissRequest = { editingCandidate = null },
            title = { Text("Décision : ${cand.candidatNom}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Passeport ${cand.passeportCode} • Score ${cand.scoreFiabilite}/100",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note interne ou horaire d'entretien") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onUpdateCandidateStatus(cand, "VUE", note)
                                editingCandidate = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vue")
                        }
                        OutlinedButton(
                            onClick = {
                                onUpdateCandidateStatus(cand, "REFUSEE", note)
                                editingCandidate = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refuser")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateCandidateStatus(cand, "RETENUE", note)
                        editingCandidate = null
                    },
                    modifier = Modifier.testTag("retain_candidate_confirm_button")
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retenir")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCandidate = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showQrVerifier) {
        QrVerifyPassportDialog(
            langue = langue,
            localUserPassportPayload = localUserPassportPayload,
            knownCandidates = candidaturesRecues,
            onDismiss = { showQrVerifier = false }
        )
    }
}
