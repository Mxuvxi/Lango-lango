package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.local.LangoDatabase
import com.example.data.local.entity.NotificationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "LangoSmsReceiver"

/**
 * BroadcastReceiver foundation for degraded-mode SMS & USSD alerts.
 * Only processes Lango/Mobile Money alerts when RECEIVE_SMS is explicitly granted.
 */
class LangoSmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val snapshot = SmsPermissionService.checkPermissions(context)
        if (!snapshot.canReceiveSms) {
            Log.w(TAG, "RECEIVE_SMS permission not granted; ignoring incoming SMS broadcast.")
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages.firstOrNull()?.displayOriginatingAddress.orEmpty()
        val fullBody = messages.joinToString(separator = "") { it.messageBody.orEmpty() }
        val parsed = SmsPermissionService.parseLangoSmsAlert(sender, fullBody) ?: return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = LangoDatabase.getInstance(context.applicationContext)
                val user = db.utilisateurDao().getCurrentUserOnce() ?: return@launch
                db.opportuniteDao().insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(8)}",
                        utilisateurId = user.id,
                        titre = parsed.title,
                        corps = parsed.summary,
                        type = if (parsed.category == "VERIFICATION") "VERIFICATION" else "OFFRE",
                        canal = "SMS"
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist incoming SMS alert", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
