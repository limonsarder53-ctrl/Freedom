package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val phone: String,
    val fullName: String,
    val referralCode: String = "",
    val balance: Double = 0.0,
    val todayEarnings: Double = 0.0,
    val totalIncome: Double = 0.0,
    val streak: Int = 0,
    val lastCheckInDate: Long = 0L,
    val isVip: Boolean = false,
    val vipLevel: String = "None", // None, Basic, Premium, Elite
    val language: String = "BN", // BN, EN
    val isDarkMode: Boolean = true,
    val selectedCurrency: String = "৳"
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val reward: Double,
    val timeLimit: String,
    val category: String, // Daily, Social, App, Premium, Games
    val iconName: String,
    val isCompleted: Boolean = false,
    val completedCount: Int = 0,
    val limitCount: Int = 5,
    val deepLink: String = ""
)

@Entity(tableName = "user_task_logs")
data class UserTaskLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val taskId: String,
    val timestamp: Long,
    val rewardEarned: Double,
    val status: String // Pending, Completed
)

@Entity(tableName = "transactions")
data class TransactionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phone: String,
    val type: String, // Deposit, Withdraw
    val amount: Double,
    val method: String, // bKash, Nagad, Rocket
    val number: String,
    val transIdOrRef: String,
    val status: String, // Pending, Completed, Failed
    val timestamp: Long
)

@Entity(tableName = "team_referrals")
data class TeamReferral(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val referrerPhone: String,
    val name: String,
    val phone: String,
    val level: Int, // 1 (Direct), 2 (Team), 3 (Team)
    val joinedDate: String,
    val commissionGenerated: Double,
    val status: String // Active, Inactive
)

@Entity(tableName = "challenges")
data class ChallengeItem(
    @PrimaryKey val id: String,
    val title: String,
    val progressMax: Int,
    val progressCurrent: Int,
    val rewardCoins: Int,
    val difficulty: String, // Easy, Medium, Hard
    val endsIn: String,
    val isClaimed: Boolean = false
)
