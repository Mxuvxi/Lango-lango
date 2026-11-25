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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Sms
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.LangoDomainLogic
import com.example.data.local.entity.CaisseOperationEntity
import com.example.data.local.entity.DevisEntity
import com.example.data.local.entity.LangueCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaisseDevisScreen(
    langue: String,
    artisanName: String,
    operations: List<CaisseOperationEntity>,
    devisList: List<DevisEntity>,
    onBack: () -> Unit,
    onAddCaisseOperation: (type: String, categorie: String, montant: Int, note: String, moyen: String) -> Unit,
    onDeleteCaisseOperation: (String) -> Unit,
    onCreateDevis: (clientNom: String, clientTel: String, desc: String, montant: Int, acompte: Int, moyen: String) -> Unit,
    onMarkDevisStatus: (DevisEntity, String) -> Unit,
    onDeleteDevis: (String) -> Unit
) {
    val isSango = langue == LangueCode.SG
    val context = LocalContext.current
    var subTab by rememberSaveable { mutableIntStateOf(0) } // 0 = Cahier, 1 = Devis & Reçus
    var showAddOpDialog by remember { mutableStateOf<String?>(null) } // "ENTREE" or "SORTIE"
    var showAddDevisDialog by remember { mutableStateOf(false) }

    val summary = remember(operations) { LangoDomainLogic.computeCaisseSummary(operations) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isSango) "Caisse & Mbeti ti kua" else "Cahier de caisse & Devis")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("caisse_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Retour"
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
                // Cashbook Summary Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = if (isSango) "Nginza ti caisse laso" else "Solde de caisse hors-ligne",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${summary.soldeNet} FCFA",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDownward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = if (isSango) "Alï" else "Entrées",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Text(
                                                text = "+${summary.totalEntrees} F",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowUpward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = if (isSango) "Asigigi" else "Dépenses",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                            Text(
                                                text = "-${summary.totalSorties} F",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Segmented toggle
                item {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = subTab == 0,
                            onClick = { subTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("caisse_tab_journal")
                        ) {
                            Text(if (isSango) "Caisse (${operations.size})" else "Cahier (${operations.size})")
                        }
                        SegmentedButton(
                            selected = subTab == 1,
                            onClick = { subTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("caisse_tab_devis")
                        ) {
                            Text(if (isSango) "Devis (${devisList.size})" else "Devis & Reçus (${devisList.size})")
                        }
                    }
                }

                if (subTab == 0) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showAddOpDialog = "ENTREE" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("caisse_add_entree_button")
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSango) "Nginza alï" else "+ Entrée")
                            }
                            OutlinedButton(
                                onClick = { showAddOpDialog = "SORTIE" },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("caisse_add_sortie_button")
                            ) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isSango) "Nginza asigigi" else "- Sortie")
                            }
                        }
                    }

                    if (operations.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "Note chaque encaissement client ou achat de matériel ici, même sans réseau. Lango calcule automatiquement ton bénéfice net.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(operations, key = { it.id }) { op ->
                            val isEntry = op.type == "ENTREE"
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = op.categorie,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = buildString {
                                                append(op.moyenPaiement.replace("_", " "))
                                                if (op.clientOuNote.isNotBlank()) append(" • ${op.clientOuNote}")
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = if (isEntry) "+${op.montantFcfa} F" else "-${op.montantFcfa} F",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isEntry) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                                    )
                                    IconButton(
                                        onClick = { onDeleteCaisseOperation(op.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Supprimer opération"
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Button(
                            onClick = { showAddDevisDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("create_devis_button")
                        ) {
                            Icon(Icons.Rounded.ReceiptLong, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSango) "Sara fini devis" else "Créer un devis / reçu client")
                        }
                    }

                    if (devisList.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "Prépare un devis professionnel en FCFA, partage-le par SMS au client, encaisse par Orange/Telecel Money (*144# / *145#) et transforme-le en reçu payé en 1 clic.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(devisList, key = { it.id }) { devis ->
                            val isPaid = devis.statut == "PAYE"
                            val reste = (devis.montantFcfa - devis.acompteFcfa).coerceAtLeast(0)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    width = if (isPaid) 1.5.dp else 1.dp,
                                    color = if (isPaid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${devis.referenceCode} • ${devis.clientNom}",
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isPaid) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.tertiaryContainer
                                            }
                                        ) {
                                            Text(
                                                text = devis.statut,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = devis.descriptionTravaux,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Total : ${devis.montantFcfa} FCFA • Acompte : ${devis.acompteFcfa} FCFA • Reste : $reste FCFA",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val smsBody = LangoDomainLogic.buildDevisSmsText(devis, artisanName)
                                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("smsto:${devis.clientTel}")
                                                    putExtra("sms_body", smsBody)
                                                }
                                                runCatching { context.startActivity(smsIntent) }
                                                if (devis.statut == "BROUILLON") {
                                                    onMarkDevisStatus(devis, "ENVOYE")
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                        ) {
                                            Icon(Icons.Rounded.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("SMS")
                                        }
                                        if (!isPaid) {
                                            OutlinedButton(
                                                onClick = {
                                                    val ussd = if (devis.moyenPaiement == "TELECEL_MONEY") "*145#" else "*144#"
                                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(ussd)}"))
                                                    runCatching { context.startActivity(dialIntent) }
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Rounded.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("USSD")
                                            }
                                            Button(
                                                onClick = { onMarkDevisStatus(devis, "PAYE") },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .minimumInteractiveComponentSize()
                                                    .testTag("mark_devis_paid_${devis.id}")
                                            ) {
                                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Payé")
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { onDeleteDevis(devis.id) },
                                                modifier = Modifier.minimumInteractiveComponentSize()
                                            ) {
                                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Supprimer devis")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddOpDialog != null) {
        val opType = showAddOpDialog!!
        var categorie by remember { mutableStateOf("") }
        var montantText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var moyen by remember { mutableStateOf("ESPECES") }

        val quickCats = if (opType == "ENTREE") {
            listOf("Vente / Couture", "Dépannage", "Pose solaire", "Acompte client")
        } else {
            listOf("Achat matériel", "Transport / Moto", "Crédit / Forfait", "Loyer atelier")
        }

        AlertDialog(
            onDismissRequest = { showAddOpDialog = null },
            title = {
                Text(if (opType == "ENTREE") "Nouvelle entrée FCFA" else "Nouvelle dépense FCFA")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickCats) { cat ->
                            FilterChip(
                                selected = categorie == cat,
                                onClick = { categorie = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = categorie,
                        onValueChange = { categorie = it },
                        label = { Text("Motif / Catégorie") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = montantText,
                        onValueChange = { montantText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Montant (FCFA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.testTag("caisse_amount_input")
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Client ou note (optionnel)") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ESPECES" to "Espèces", "ORANGE_MONEY" to "Orange", "TELECEL_MONEY" to "Telecel").forEach { (code, label) ->
                            FilterChip(
                                selected = moyen == code,
                                onClick = { moyen = code },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                val amount = montantText.toIntOrNull() ?: 0
                TextButton(
                    enabled = categorie.trim().length >= 2 && amount > 0,
                    onClick = {
                        onAddCaisseOperation(opType, categorie, amount, note, moyen)
                        showAddOpDialog = null
                    },
                    modifier = Modifier.testTag("caisse_confirm_add_button")
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddOpDialog = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showAddDevisDialog) {
        var clientNom by remember { mutableStateOf("") }
        var clientTel by remember { mutableStateOf("+236") }
        var description by remember { mutableStateOf("") }
        var montantText by remember { mutableStateOf("") }
        var acompteText by remember { mutableStateOf("0") }
        var moyen by remember { mutableStateOf("ORANGE_MONEY") }

        AlertDialog(
            onDismissRequest = { showAddDevisDialog = false },
            title = { Text("Nouveau devis client") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = clientNom,
                        onValueChange = { clientNom = it },
                        label = { Text("Nom du client") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = clientTel,
                        onValueChange = { clientTel = it },
                        label = { Text("Téléphone client") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description des travaux") },
                        maxLines = 2
                    )
                    OutlinedTextField(
                        value = montantText,
                        onValueChange = { montantText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Montant total (FCFA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = acompteText,
                        onValueChange = { acompteText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Acompte versé (FCFA)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ORANGE_MONEY" to "Orange M.", "TELECEL_MONEY" to "Telecel", "ESPECES" to "Espèces").forEach { (code, label) ->
                            FilterChip(
                                selected = moyen == code,
                                onClick = { moyen = code },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                val amount = montantText.toIntOrNull() ?: 0
                val acompte = acompteText.toIntOrNull() ?: 0
                TextButton(
                    enabled = clientNom.trim().length >= 2 && description.trim().length >= 3 && amount > 0,
                    onClick = {
                        onCreateDevis(clientNom, clientTel, description, amount, acompte, moyen)
                        showAddDevisDialog = false
                    },
                    modifier = Modifier.testTag("devis_confirm_create_button")
                ) {
                    Text("Créer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDevisDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
