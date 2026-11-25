package com.example.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val AUTH_TAG = "Auth"

fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        trySend(firebaseAuth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

sealed interface GoogleSignInOutcome {
    data class Success(val user: FirebaseUser?) : GoogleSignInOutcome
    data class Cancelled(val message: String) : GoogleSignInOutcome
    data class Failure(val message: String) : GoogleSignInOutcome
}

suspend fun attemptSilentGoogleSignIn(
    activityContext: Context,
    webClientId: String,
    auth: FirebaseAuth = Firebase.auth
): Boolean {
    if (auth.currentUser != null || webClientId.isBlank()) return auth.currentUser != null
    return try {
        val credentialManager = CredentialManager.create(activityContext)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(true)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val response = credentialManager.getCredential(
            request = request,
            context = activityContext
        )
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            auth.signInWithCredential(firebaseCredential).await()
            true
        } else {
            false
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: NoCredentialException) {
        false
    } catch (e: Exception) {
        Log.d(AUTH_TAG, "Silent sign-in skipped: ${e.message}")
        false
    }
}

suspend fun launchInteractiveGoogleSignIn(
    activityContext: Context,
    webClientId: String,
    auth: FirebaseAuth = Firebase.auth
): GoogleSignInOutcome {
    if (webClientId.isBlank()) {
        return GoogleSignInOutcome.Failure("Client ID Google non configuré.")
    }
    return try {
        val credentialManager = CredentialManager.create(activityContext)
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val response = credentialManager.getCredential(
            request = request,
            context = activityContext
        )
        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
            val result = auth.signInWithCredential(firebaseCredential).await()
            GoogleSignInOutcome.Success(result.user)
        } else {
            GoogleSignInOutcome.Failure("Identifiant Google non reconnu.")
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: GetCredentialCancellationException) {
        Log.w(AUTH_TAG, "Credential Manager sign-in cancelled by user or config", e)
        GoogleSignInOutcome.Cancelled("Connexion annulée. Sélectionne un compte Google pour continuer.")
    } catch (e: GetCredentialException) {
        Log.e(AUTH_TAG, "Credential Manager sign-in failed", e)
        GoogleSignInOutcome.Failure(e.localizedMessage ?: "Échec de la connexion Google.")
    } catch (e: Exception) {
        Log.e(AUTH_TAG, "Firebase sign-in failed", e)
        GoogleSignInOutcome.Failure(e.localizedMessage ?: "Impossible de se connecter à Firebase.")
    }
}

suspend fun signOutGoogleSession(
    context: Context,
    auth: FirebaseAuth = Firebase.auth
) {
    withContext(NonCancellable) {
        runCatching { auth.signOut() }
        runCatching {
            CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
        }
    }
}
