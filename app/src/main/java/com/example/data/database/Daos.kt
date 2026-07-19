package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FreedomDao {

    // UserProfile
    @Query("SELECT * FROM user_profiles WHERE phone = :phone")
    fun getUser(phone: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE phone = :phone")
    suspend fun getUserSync(phone: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserProfile)

    @Update
    suspend fun updateUser(user: UserProfile)

    // Tasks
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE category = :category")
    fun getTasksByCategory(category: String): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Update
    suspend fun updateTask(task: Task)

    // UserTaskLog
    @Query("SELECT * FROM user_task_logs WHERE phone = :phone ORDER BY timestamp DESC")
    fun getTaskLogs(phone: String): Flow<List<UserTaskLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskLog(log: UserTaskLog)

    // Transactions (Deposits / Withdrawals)
    @Query("SELECT * FROM transactions WHERE phone = :phone ORDER BY timestamp DESC")
    fun getTransactions(phone: String): Flow<List<TransactionLog>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionLog)

    @Update
    suspend fun updateTransaction(transaction: TransactionLog)

    // TeamReferrals
    @Query("SELECT * FROM team_referrals WHERE referrerPhone = :referrerPhone ORDER BY joinedDate DESC")
    fun getReferrals(referrerPhone: String): Flow<List<TeamReferral>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferrals(referrals: List<TeamReferral>)

    // Challenges
    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<ChallengeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenges(challenges: List<ChallengeItem>)

    @Update
    suspend fun updateChallenge(challenge: ChallengeItem)
}
