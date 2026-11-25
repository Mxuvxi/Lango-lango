package com.example.service

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private const val TAG = "SmsPermissionService"

data class SmsPermissionSnapshot(
    val hasTelephonyHardware: Boolean,
    val canSendSms: Boolean,
    val canReceiveSms: Boolean
) {
    val isFullyGranted: Boolean
        get() = hasTelephonyHardware && canSendSms && canReceiveSms

    val isFallbackComposerOnly: Boolean
        get() = !canSendSms || !hasTelephonyHardware
}

sealed interface SmsDispatchResult {
    data object SentDirect : SmsDispatchResult
    data object OpenedSmsComposerFallback : SmsDispatchResult
    data class Failed(val reason: String) : SmsDispatchResult
}

data class ParsedLangoSmsAlert(
    val category: String, // OFFRE, VERIFICATION, MOBILE_MONEY, USSD_REPLI
    val title: String,
    val summary: String
)

object SmsPermissionService {

    val requiredSmsPermissions: Array<String> = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS
    )

    fun checkPermissions(context: Context): SmsPermissionSnapshot {
        val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
        val sendGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val receiveGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED

        return SmsPermissionSnapshot(
            hasTelephonyHardware = hasTelephony,
            canSendSms = sendGranted,
            canReceiveSms = receiveGranted
        )
    }

    fun shouldShowRationale(context: Context): Boolean {
        val activity = context.findActivity() ?: return false
        return requiredSmsPermissions.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }
    }

    fun buildAppSettingsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Dispatches an SMS directly when SEND_SMS + telephony are available;
     * otherwise gracefully falls back to Android's zero-permission ACTION_SENDTO composer.
     */
    fun sendDirectSmsOrFallback(
        context: Context,
        phoneNumber: String,
        message: String
    ): SmsDispatchResult {
        val cleanPhone = phoneNumber.trim()
        val cleanBody = message.trim()
        if (cleanBody.isEmpty()) {
            return SmsDispatchResult.Failed("Message SMS vide.")
        }

        val snapshot = checkPermissions(context)
        if (snapshot.hasTelephonyHardware && snapshot.canSendSms && cleanPhone.isNotEmpty()) {
            val sent = runCatching {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                if (smsManager == null) {
                    return@runCatching false
                }
                val parts = smsManager.divideMessage(cleanBody)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanPhone, null, cleanBody, null, null)
                }
                true
            }.onFailure { err ->
                Log.e(TAG, "Direct SmsManager dispatch failed, falling back to composer", err)
            }.getOrDefault(false)

            if (sent) {
                return SmsDispatchResult.SentDirect
            }
        }

        return openSmsComposerFallback(context, cleanPhone, cleanBody)
    }

    fun openSmsComposerFallback(
        context: Context,
        phoneNumber: String,
        message: String
    ): SmsDispatchResult {
        return runCatching {
            val uri = Uri.parse("smsto:${phoneNumber.trim()}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", message)
                if (context !is Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            SmsDispatchResult.OpenedSmsComposerFallback
        }.getOrElse { err ->
            Log.e(TAG, "Failed to open SMS composer fallback", err)
            SmsDispatchResult.Failed("Aucune application SMS disponible sur cet appareil.")
        }
    }

    /**
     * Pure parser for incoming degraded-mode SMS & USSD confirmation alerts.
     * Returns null when the SMS is unrelated to Lango or Mobile Money workflows.
     */
    fun parseLangoSmsAlert(sender: String, body: String): ParsedLangoSmsAlert? {
        val trimmed = body.trim()
        if (trimmed.isEmpty()) return null
        val upper = trimmed.uppercase()

        return when {
            upper.startsWith("LANGO OFFRE") || upper.contains("OFFRE LANGO") -> {
                ParsedLangoSmsAlert(
                    category = "OFFRE",
                    title = "Alerte offre par SMS",
                    summary = trimmed.take(160)
                )
            }
            upper.startsWith("LANGO VERIF") || upper.contains("CODE LANGO") -> {
                ParsedLangoSmsAlert(
                    category = "VERIFICATION",
                    title = "Validation client par SMS",
                    summary = trimmed.take(160)
                )
            }
            upper.contains("ORANGE MONEY") || upper.contains("TELECEL MONEY") ||
                upper.contains("*144#") || upper.contains("*145#") -> {
                ParsedLangoSmsAlert(
                    category = "MOBILE_MONEY",
                    title = "Notification Mobile Money (${sender.ifBlank { "Opérateur" }})",
                    summary = trimmed.take(160)
                )
            }
            upper.startsWith("LANGO") -> {
                ParsedLangoSmsAlert(
                    category = "USSD_REPLI",
                    title = "Alerte mode dégradé Lango",
                    summary = trimmed.take(160)
                )
            }
            else -> null
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
