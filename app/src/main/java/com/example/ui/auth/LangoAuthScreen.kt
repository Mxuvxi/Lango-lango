package com.example.ui.auth

import android.util.Log
import android.util.Patterns
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.entity.LangueCode
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

private const val AUTH_TAG = "LangoAuth"

@Composable
fun LangoAuthScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val webClientId = remember(context) {
        context.getString(R.string.default_web_client_id)
    }

    var selectedLang by remember { mutableStateOf(LangueCode.FR) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(webClientId) {
        attemptSilentGoogleSignIn(
            activityContext = context,
            webClientId = webClientId
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedLang == LangueCode.FR,
                    onClick = { selectedLang = LangueCode.FR },
                    label = { Text("Français") },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
                FilterChip(
                    selected = selectedLang == LangueCode.SG,
                    onClick = { selectedLang = LangueCode.SG },
                    label = { Text("Sängö") },
                    modifier = Modifier.minimumInteractiveComponentSize()
                )
            }

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = stringResource(R.string.cd_lango_logo),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Text(
                text = "Lango",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (selectedLang == LangueCode.SG) {
                    "Bata Passeport Lango ti mo na ndo ti cloud"
                } else {
                    "Connexion & Synchronisation Lango"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Google Sign-In Primary Button
            Button(
                onClick = {
                    if (isLoading) return@Button
                    errorMessage = null
                    infoMessage = null
                    isLoading = true
                    scope.launch {
                        when (
                            val outcome = launchInteractiveGoogleSignIn(
                                activityContext = context,
                                webClientId = webClientId
                            )
                        ) {
                            is GoogleSignInOutcome.Success -> {
                                isLoading = false
                            }
                            is GoogleSignInOutcome.Cancelled -> {
                                isLoading = false
                                errorMessage = outcome.message
                            }
                            is GoogleSignInOutcome.Failure -> {
                                isLoading = false
                                errorMessage = outcome.message
                            }
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("google_sign_in_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (selectedLang == LangueCode.SG) "Lï na compte Google" else "Continuer avec Google",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
                Text(
                    text = " ou par email / téléphone ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                )
            }

            // Email & Password Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) {
                            if (selectedLang == LangueCode.SG) "Créer un compte Lango" else "Créer un compte par Email"
                        } else {
                            if (selectedLang == LangueCode.SG) "Connexion par Email" else "Connexion par Email & Mot de passe"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            if (emailError != null) emailError = null
                        },
                        label = { Text("Adresse email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        singleLine = true,
                        isError = emailError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )
                    emailError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            if (passwordError != null) passwordError = null
                        },
                        label = { Text("Mot de passe (6 caractères min)") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                        singleLine = true,
                        isError = passwordError != null,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )
                    passwordError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = {
                            if (isLoading) return@Button
                            val email = emailInput.trim()
                            val pass = passwordInput.trim()

                            var hasValidationError = false
                            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                emailError = "Veuillez entrer une adresse email valide."
                                hasValidationError = true
                            } else {
                                emailError = null
                            }

                            if (!isRegisterMode && pass.isBlank()) {
                                passwordError = "Veuillez entrer votre mot de passe."
                                hasValidationError = true
                            } else if (isRegisterMode && pass.length < 6) {
                                passwordError = "Le mot de passe doit comporter au moins 6 caractères."
                                hasValidationError = true
                            } else {
                                passwordError = null
                            }

                            if (hasValidationError) {
                                errorMessage = "Veuillez corriger les erreurs dans le formulaire."
                                return@Button
                            }

                            errorMessage = null
                            infoMessage = null
                            isLoading = true
                            scope.launch {
                                val auth = Firebase.auth
                                try {
                                    if (isRegisterMode) {
                                        auth.createUserWithEmailAndPassword(email, pass).await()
                                    } else {
                                        try {
                                            auth.signInWithEmailAndPassword(email, pass).await()
                                        } catch (e: Exception) {
                                            Log.d(AUTH_TAG, "Sign in failed, attempting registration", e)
                                            auth.createUserWithEmailAndPassword(email, pass).await()
                                        }
                                    }
                                    isLoading = false
                                } catch (e: Exception) {
                                    Log.e(AUTH_TAG, "Email auth failed", e)
                                    isLoading = false
                                    errorMessage = e.localizedMessage ?: "Échec de l'authentification. Vérifie tes identifiants."
                                }
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_email_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isRegisterMode) "Créer le compte" else "Se connecter")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isRegisterMode = !isRegisterMode
                                errorMessage = null
                                emailError = null
                                passwordError = null
                            }
                        ) {
                            Text(
                                text = if (isRegisterMode) "Déjà un compte ?" else "S'inscrire",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        if (!isRegisterMode) {
                            TextButton(
                                onClick = {
                                    val email = emailInput.trim()
                                    if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                        emailError = "Entre une adresse email valide ci-dessus pour réinitialiser."
                                        errorMessage = "Entre ton email pour recevoir le lien de réinitialisation."
                                        return@TextButton
                                    }
                                    errorMessage = null
                                    infoMessage = null
                                    isLoading = true
                                    scope.launch {
                                        try {
                                            Firebase.auth.sendPasswordResetEmail(email).await()
                                            isLoading = false
                                            infoMessage = "Email de réinitialisation envoyé à $email. Vérifie ta boîte de réception."
                                        } catch (e: Exception) {
                                            Log.e(AUTH_TAG, "Password reset failed", e)
                                            isLoading = false
                                            errorMessage = e.localizedMessage ?: "Impossible d'envoyer l'email de réinitialisation."
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("auth_forgot_password_button")
                            ) {
                                Text(
                                    text = "Mot de passe oublié ?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            errorMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            infoMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
