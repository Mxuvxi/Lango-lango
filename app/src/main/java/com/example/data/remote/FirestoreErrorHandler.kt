package com.example.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write")
}

fun handleFirestoreError(
    error: Exception,
    operationType: OperationType,
    path: String?
) {
    val user = Firebase.auth.currentUser
    val errInfo = JSONObject().apply {
        put("error", error.localizedMessage ?: error.toString())
        put("operationType", operationType.value)
        put("path", path ?: JSONObject.NULL)
        put(
            "authInfo",
            JSONObject().apply {
                put("userId", user?.uid ?: JSONObject.NULL)
                put("email", user?.email ?: JSONObject.NULL)
                put("emailVerified", user?.isEmailVerified ?: false)
                put("isAnonymous", user?.isAnonymous ?: false)
                put("tenantId", user?.tenantId ?: JSONObject.NULL)
                val providers = JSONArray()
                user?.providerData?.forEach { info ->
                    providers.put(
                        JSONObject().apply {
                            put("providerId", info.providerId)
                            put("displayName", info.displayName ?: JSONObject.NULL)
                            put("email", info.email ?: JSONObject.NULL)
                            put("photoUrl", info.photoUrl?.toString() ?: JSONObject.NULL)
                        }
                    )
                }
                put("providerInfo", providers)
            }
        )
    }
    Log.e("FirestoreError", "Firestore Error: $errInfo", error)
}
