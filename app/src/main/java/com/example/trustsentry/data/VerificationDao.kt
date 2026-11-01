package com.example.trustsentry.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    @Query("SELECT * FROM verifications ORDER BY verifiedAt DESC")
    fun getAllVerifications(): Flow<List<VerificationEntity>>

    @Query("SELECT * FROM verifications WHERE isPinned = 1 ORDER BY verifiedAt DESC")
    fun getPinnedVerifications(): Flow<List<VerificationEntity>>

    @Query("SELECT * FROM verifications WHERE status = :status ORDER BY verifiedAt DESC")
    fun getVerificationsByStatus(status: String): Flow<List<VerificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerification(verification: VerificationEntity): Long

    @Query("UPDATE verifications SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Delete
    suspend fun deleteVerification(verification: VerificationEntity)

    @Query("DELETE FROM verifications")
    suspend fun clearAll()
}
