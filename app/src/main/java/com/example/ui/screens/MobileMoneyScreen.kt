package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Phone
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
import com.example.data.local.entity.LangueCode

private data class MobileMoneyPack(
    val id: String,
    val titleFr: String,
    val titleSg: String,
    val amountFcfa: Int
)

@Composable
fun MobileMoneyScreen(
    langue: String,
    defaultPhone: String,
    isOffline: Boolean,
    onBack: () -> Unit,
    onPaymentAction: (operator: String, packName: String, amountFcfa: Int, phone: String, viaUssd: Boolean) -> Unit
) {
    val isSango = langue == LangueCode.SG
    val context = LocalContext.current

    val packs = remember {
        listOf(
            MobileMoneyPack("sms_plus", "Pack Alertes SMS 30 jours", "Sango ti kua na SMS (lâ 30)", 300),
            MobileMoneyPack("cert_express", "Pass Vérification & Certificat PDF", "Pass ti badge & mbeti", 500),
            MobileMoneyPack("employer_post", "Publier une offre (Employeur/PME)", "Zia fini kua (Patron)", 2000)
        )
    }

    var selectedOperator by remember { mutableStateOf("Orange Money") }
    var selectedPackIdx by remember { mutableIntStateOf(0) }
    var phoneInput by remember(defaultPhone) { mutableStateOf(defaultPhone.ifBlank { "+236 " }) }

    val selectedPack = packs[selectedPackIdx]
    val ussdCode = if (selectedOperator == "Orange Money") "*144#" else "*145#"
    val isPhoneValid = phoneInput.filter { it.isDigit() }.length >= 8

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
                        .testTag("mobile_money_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mobile Money & USSD",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isSango) {
                    "Futa na Orange Money wala Telecel Money. Tongana Internet a yeke pepe, pika USSD."
                } else {
                    "Orange Money en priorité, puis Telecel Money, avec repli USSD fonctionnel sans connexion."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isSango) "1. Soro réseau" else "1. Opérateur Mobile Money",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OperatorCard(
                    name = "Orange Money",
                    badge = if (isSango) "Kozo" else "Prioritaire",
                    ussd = "*144#",
                    selected = selectedOperator == "Orange Money",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("op_orange_money"),
                    onClick = { selectedOperator = "Orange Money" }
                )
                OperatorCard(
                    name = "Telecel Money",
                    badge = "RCA",
                    ussd = "*145#",
                    selected = selectedOperator == "Telecel Money",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("op_telecel_money"),
                    onClick = { selectedOperator = "Telecel Money" }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isSango) "2. Soro service" else "2. Choisir le service",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            packs.forEachIndexed { idx, pack ->
                val isSelected = selectedPackIdx == idx
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .minimumInteractiveComponentSize()
                        .clickable { selectedPackIdx = idx },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isSango) pack.titleSg else pack.titleFr,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = "${pack.amountFcfa} FCFA",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text("Numéro Mobile Money (+236)") },
                leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mobile_money_phone_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Online API Payment Button
            Button(
                onClick = {
                    val name = if (isSango) selectedPack.titleSg else selectedPack.titleFr
                    onPaymentAction(selectedOperator, name, selectedPack.amountFcfa, phoneInput, false)
                },
                enabled = isPhoneValid && !isOffline,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("mobile_money_api_pay_button")
            ) {
                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOffline) {
                        "Paiement API indisponible hors-ligne"
                    } else {
                        "Payer ${selectedPack.amountFcfa} FCFA ($selectedOperator)"
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // USSD Fallback Button (always works offline)
            OutlinedButton(
                onClick = {
                    val name = if (isSango) selectedPack.titleSg else selectedPack.titleFr
                    onPaymentAction(selectedOperator, name, selectedPack.amountFcfa, phoneInput, true)
                    val encodedUssd = Uri.encode(ussdCode)
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encodedUssd"))
                    runCatching { context.startActivity(dialIntent) }
                },
                enabled = isPhoneValid,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("mobile_money_ussd_button")
            ) {
                Icon(Icons.Rounded.Dialpad, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Repli USSD sans Internet ($ussdCode)")
            }
        }
    }
}

@Composable
private fun OperatorCard(
    name: String,
    badge: String,
    ussd: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                        text = badge,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                if (selected) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                text = "USSD : $ussd",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
