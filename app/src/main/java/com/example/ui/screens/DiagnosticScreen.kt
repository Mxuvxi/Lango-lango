package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CompetenceEntity
import com.example.data.local.entity.LangueCode
import com.example.ui.i18n.LangoI18n

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticScreen(
    selectedLang: String,
    competences: List<CompetenceEntity>,
    onFinish: (niveau: String, objectif: String, metier: String) -> Unit
) {
    val copy = LangoI18n.forLang(selectedLang)
    var selectedLevel by remember { mutableStateOf("DEBUTANT") }
    var selectedGoal by remember { mutableStateOf("APPRENDRE") }
    var selectedTrade by remember(competences) {
        mutableStateOf(competences.firstOrNull()?.id ?: "comp_couture")
    }

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
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = copy.diagnosticTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = copy.diagnosticSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = copy.levelQuestion, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticOptionCard(
                label = copy.levelBeginner,
                icon = Icons.Rounded.MenuBook,
                selected = selectedLevel == "DEBUTANT",
                tag = "diag_level_beginner",
                onClick = { selectedLevel = "DEBUTANT" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticOptionCard(
                label = copy.levelPractitioner,
                icon = Icons.Rounded.Build,
                selected = selectedLevel == "PRATIQUE",
                tag = "diag_level_practitioner",
                onClick = { selectedLevel = "PRATIQUE" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticOptionCard(
                label = copy.levelExperienced,
                icon = Icons.Rounded.School,
                selected = selectedLevel == "EXPERIMENTE",
                tag = "diag_level_experienced",
                onClick = { selectedLevel = "EXPERIMENTE" }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = copy.goalQuestion, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            DiagnosticOptionCard(
                label = copy.goalLearn,
                icon = Icons.Rounded.School,
                selected = selectedGoal == "APPRENDRE",
                tag = "diag_goal_learn",
                onClick = { selectedGoal = "APPRENDRE" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticOptionCard(
                label = copy.goalJob,
                icon = Icons.Rounded.Work,
                selected = selectedGoal == "EMPLOI",
                tag = "diag_goal_job",
                onClick = { selectedGoal = "EMPLOI" }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DiagnosticOptionCard(
                label = copy.goalClients,
                icon = Icons.Rounded.Payments,
                selected = selectedGoal == "CLIENTS",
                tag = "diag_goal_clients",
                onClick = { selectedGoal = "CLIENTS" }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = copy.tradeQuestion, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                competences.forEach { comp ->
                    val label = if (selectedLang == LangueCode.SG) comp.nomSg else comp.nomFr
                    FilterChip(
                        selected = selectedTrade == comp.id,
                        onClick = { selectedTrade = comp.id },
                        leadingIcon = {
                            Icon(Icons.Rounded.Storefront, contentDescription = null)
                        },
                        label = { Text(label) },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onFinish(selectedLevel, selectedGoal, selectedTrade) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("diagnostic_finish_button")
            ) {
                Text(
                    text = copy.finishDiagnostic,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DiagnosticOptionCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
