package com.example.trustsentry.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "verifications")
data class VerificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val artifactId: String,
    val artifactType: String,
    val issuer: String,
    val score: Int,
    val status: String, // TRUSTED, REVIEW, SUSPICIOUS, INVALID, UNABLE_TO_VERIFY
    val riskLevel: String,
    val explanation: String,
    val rawValue: String?,
    val verifiedAt: Long,
    val isPinned: Boolean = false,
    val sha256Digest: String? = null
)
