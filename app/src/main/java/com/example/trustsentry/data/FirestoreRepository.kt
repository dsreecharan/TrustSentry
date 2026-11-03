package com.example.trustsentry.data

import android.content.Context
import com.example.R
import com.example.trustsentry.model.VerificationResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class CloudVerification(
    val id: String = "",
    val artifactId: String = "",
    val artifactType: String = "",
    val issuer: String = "",
    val score: Int = 0,
    val status: String = "",
    val riskLevel: String = "",
    val explanation: String = "",
    val rawValue: String? = null,
    val sha256Digest: String? = null,
    val isPinned: Boolean = false
)

class FirestoreRepository(
    private val db: FirebaseFirestore
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing Firestore operations.")
    }

    suspend fun saveUserProfile(user: FirebaseUser) {
        val uid = user.uid
        val docRef = db.collection("users").document(uid)
        val path = docRef.path
        try {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to (user.displayName ?: "Anonymous Verifier"),
                "email" to (user.email ?: ""),
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHelper.handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    suspend fun syncVerification(result: VerificationResult) {
        val uid = requireUserId()
        val colRef = db.collection("users").document(uid).collection("verifications")
        val path = colRef.path
        try {
            val payload = mapOf(
                "artifactId" to result.artifactId,
                "artifactType" to result.artifactType,
                "issuer" to result.issuer,
                "score" to result.score,
                "status" to result.status.name,
                "riskLevel" to result.riskLevel,
                "explanation" to result.explanation,
                "rawValue" to (result.rawValue ?: ""),
                "sha256Digest" to (result.sha256Digest ?: ""),
                "isPinned" to false,
                "verifiedAt" to FieldValue.serverTimestamp()
            )
            colRef.add(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHelper.handleFirestoreError(e, OperationType.CREATE, path)
        }
    }

    fun observeVerifications(): Flow<List<CloudVerification>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/verifications"
        emitAll(
            db.collection("users").document(uid).collection("verifications")
                .orderBy("verifiedAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.documents.map { doc ->
                        CloudVerification(
                            id = doc.id,
                            artifactId = doc.getString("artifactId") ?: "",
                            artifactType = doc.getString("artifactType") ?: "",
                            issuer = doc.getString("issuer") ?: "",
                            score = doc.getLong("score")?.toInt() ?: 0,
                            status = doc.getString("status") ?: "UNABLE_TO_VERIFY",
                            riskLevel = doc.getString("riskLevel") ?: "LOW",
                            explanation = doc.getString("explanation") ?: "",
                            rawValue = doc.getString("rawValue"),
                            sha256Digest = doc.getString("sha256Digest"),
                            isPinned = doc.getBoolean("isPinned") ?: false
                        )
                    }
                }
                .catch { error ->
                    if (error is Exception) {
                        FirestoreErrorHelper.handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }
}
