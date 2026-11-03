package com.example.trustsentry.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    CREATE, READ, UPDATE, DELETE, LIST
}

object FirestoreErrorHelper {
    private const val TAG = "FirestoreSecurity"

    fun handleFirestoreError(exception: Exception, operation: OperationType, path: String) {
        val json = JSONObject().apply {
            put("event", "FIRESTORE_OPERATION_FAILED")
            put("operation", operation.name)
            put("path", path)
            put("message", exception.localizedMessage ?: "Unknown error")
            if (exception is FirebaseFirestoreException) {
                put("code", exception.code.name)
            }
        }
        Log.e(TAG, json.toString(), exception)
    }
}
