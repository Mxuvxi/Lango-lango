package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.LangoSeedData
import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.LangueCode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublishOpportunityScreen(
    langue: String,
    defaultEmployer: String,
    defaultPhone: String,
    defaultCity: String,
    competences: List<CompetenceEntity>,
    onBack: () -> Unit,
    onPublish: (
        employeurNom: String,
        titre: String,
        type: String,
        ville: String,
        quartier: String,
        competenceId: String,
        remunerationFcfa: Int,
        descriptionCourte: String,
        contactTel: String
    ) -> Unit
) {
    val isSango = langue == LangueCode.SG
    var employerName by remember(defaultEmployer) { mutableStateOf(defaultEmployer) }
    var titre by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("MISSION") }
    var selectedVille by remember(defaultCity) {
        mutableStateOf(defaultCity.ifBlank { LangoSeedData.rcaVilles.first() })
    }
    var quartier by remember { mutableStateOf("") }
    var selectedComp by remember(competences) {
        mutableStateOf(competences.firstOrNull()?.id ?: "comp_couture")
    }
    var remunerationText by remember { mutableStateOf("35000") }
    var description by remember { mutableStateOf("") }
    var contactTel by remember(defaultPhone) { mutableStateOf(defaultPhone.ifBlank { "+236 " }) }

    val amount = remunerationText.filter { it.isDigit() }.toIntOrNull() ?: 0
    val isValid = employerName.trim().length >= 2 &&
        titre.trim().length >= 3 &&
        amount > 0 &&
        contactTel.filter { it.isDigit() }.length >= 8

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
                        .testTag("publish_opp_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSango) "Zia fini kua (Patron / ONG)" else "Publier une offre locale",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = titre,
                onValueChange = { titre = it },
                label = { Text(if (isSango) "Iri ti kua (ex: Aide couturier)" else "Titre du poste ou mission") },
                leadingIcon = { Icon(Icons.Rounded.Work, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = employerName,
                onValueChange = { employerName = it },
                label = { Text(if (isSango) "Iri ti atelier / ONG" else "Entreprise, atelier ou ONG") },
                leadingIcon = { Icon(Icons.Rounded.Business, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_employer_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Type d’opportunité", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MISSION", "EMPLOI", "STAGE", "SERVICE").forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(type) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Ville & Quartier", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LangoSeedData.rcaVilles.forEach { ville ->
                    FilterChip(
                        selected = selectedVille == ville,
                        onClick = { selectedVille = ville },
                        label = { Text(ville) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = quartier,
                onValueChange = { quartier = it },
                label = { Text("Quartier précis (ex: Marché Central)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Métier recherché", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                competences.forEach { comp ->
                    FilterChip(
                        selected = selectedComp == comp.id,
                        onClick = { selectedComp = comp.id },
                        label = { Text(if (isSango) comp.nomSg else comp.nomFr) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = remunerationText,
                onValueChange = { remunerationText = it },
                label = { Text("Rémunération (FCFA)") },
                leadingIcon = { Icon(Icons.Rounded.Payments, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_amount_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = contactTel,
                onValueChange = { contactTel = it },
                label = { Text("Téléphone de contact (+236)") },
                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("publish_phone_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description courte de la mission") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onPublish(
                        employerName,
                        titre,
                        selectedType,
                        selectedVille,
                        quartier.ifBlank { selectedVille },
                        selectedComp,
                        amount,
                        description.ifBlank { "Mission locale à $selectedVille." },
                        contactTel
                    )
                },
                enabled = isValid,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("publish_submit_button")
            ) {
                Text(
                    text = if (isSango) "Zia kua na Lango" else "Publier l’opportunité",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
