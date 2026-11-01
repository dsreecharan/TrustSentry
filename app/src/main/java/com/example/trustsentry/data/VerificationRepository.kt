package com.example.trustsentry.data

import com.example.trustsentry.model.VerificationResult
import kotlinx.coroutines.flow.Flow

class VerificationRepository(private val dao: VerificationDao) {

    fun getAllVerifications(): Flow<List<VerificationEntity>> = dao.getAllVerifications()

    fun getPinnedVerifications(): Flow<List<VerificationEntity>> = dao.getPinnedVerifications()

    fun getVerificationsByStatus(status: String): Flow<List<VerificationEntity>> =
        dao.getVerificationsByStatus(status)

    suspend fun saveVerification(result: VerificationResult): Long {
        val entity = VerificationEntity(
            artifactId = result.artifactId,
            artifactType = result.artifactType,
            issuer = result.issuer,
            score = result.score,
            status = result.status.name,
            riskLevel = result.riskLevel,
            explanation = result.explanation,
            rawValue = result.rawValue,
            verifiedAt = result.verifiedAt,
            isPinned = false,
            sha256Digest = result.sha256Digest
        )
        return dao.insertVerification(entity)
    }

    suspend fun togglePinned(id: Long, isPinned: Boolean) {
        dao.setPinned(id, !isPinned)
    }

    suspend fun delete(entity: VerificationEntity) {
        dao.deleteVerification(entity)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
