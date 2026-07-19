package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FreedomRepository(private val dao: FreedomDao) {

    // User Profile Queries
    fun getUser(phone: String): Flow<UserProfile?> = dao.getUser(phone)
    suspend fun getUserSync(phone: String): UserProfile? = dao.getUserSync(phone)
    suspend fun insertUser(user: UserProfile) = dao.insertUser(user)
    suspend fun updateUser(user: UserProfile) = dao.updateUser(user)

    // Tasks
    fun getAllTasks(): Flow<List<Task>> = dao.getAllTasks()
    fun getTasksByCategory(category: String): Flow<List<Task>> = dao.getTasksByCategory(category)
    suspend fun updateTask(task: Task) = dao.updateTask(task)

    // Task Logs
    fun getTaskLogs(phone: String): Flow<List<UserTaskLog>> = dao.getTaskLogs(phone)
    suspend fun insertTaskLog(log: UserTaskLog) = dao.insertTaskLog(log)

    // Transactions
    fun getTransactions(phone: String): Flow<List<TransactionLog>> = dao.getTransactions(phone)
    fun getAllTransactions(): Flow<List<TransactionLog>> = dao.getAllTransactions()
    suspend fun insertTransaction(transaction: TransactionLog) = dao.insertTransaction(transaction)

    // Referrals
    fun getReferrals(referrerPhone: String): Flow<List<TeamReferral>> = dao.getReferrals(referrerPhone)

    // Challenges
    fun getAllChallenges(): Flow<List<ChallengeItem>> = dao.getAllChallenges()
    suspend fun updateChallenge(challenge: ChallengeItem) = dao.updateChallenge(challenge)

    // Seed Initial Data
    suspend fun seedInitialDataIfNecessary(userPhone: String) {
        // Pre-populate tasks if none exist
        dao.insertTasks(listOf(
            // Daily Tasks
            Task("daily_vid_1", "Watch Video & Earn", "Watch 30 seconds video to earn instant rewards.", 5.0, "30 sec", "Daily", "video", false, 0, 5, "https://example.com/video1"),
            Task("daily_ads_1", "Watch Ads & Earn", "Watch premium ads for high CPM payouts.", 3.0, "15 sec", "Daily", "ads", false, 0, 10, "https://example.com/ads1"),
            Task("daily_news_1", "Read News & Earn", "Read trending technology articles for 1 minute.", 4.0, "1 min", "Daily", "news", false, 0, 3, "https://example.com/news1"),
            Task("daily_link_1", "Shortlink Visit", "Complete easy shortlinks for quick earnings.", 6.5, "45 sec", "Daily", "link", false, 0, 5, "https://example.com/short1"),
            Task("daily_web_1", "Website Visit", "Browse our partner websites for 1 minute.", 4.5, "1 min", "Daily", "website", false, 0, 8, "https://example.com/web1"),

            // Social Media Tasks
            Task("social_fb_1", "Like & Share (FB)", "Like our official page and share the pinned post.", 8.0, "1 min", "Social", "fb", false, 0, 1, "https://facebook.com/freedom_earn"),
            Task("social_ig_1", "Follow on Instagram", "Follow the official Freedom Earn profile on Instagram.", 5.0, "30 sec", "Social", "ig", false, 0, 1, "https://instagram.com/freedom_earn"),
            Task("social_yt_1", "Subscribe & Like (YT)", "Subscribe to our official channel and like the last video.", 10.0, "2 min", "Social", "yt", false, 0, 1, "https://youtube.com/freedom_earn"),
            Task("social_tg_1", "Join Telegram Channel", "Join our Telegram community for payment updates.", 7.0, "30 sec", "Social", "tg", false, 0, 1, "https://t.me/freedom_earn"),
            Task("social_tt_1", "Follow on TikTok", "Follow our TikTok account for success stories.", 6.0, "30 sec", "Social", "tt", false, 0, 1, "https://tiktok.com/@freedom_earn"),

            // App Tasks
            Task("app_vpn_1", "Install Freedom VPN", "Download, install, and open Freedom VPN for 30 seconds.", 25.0, "2 min", "App", "app", false, 0, 1, "https://play.google.com/store/apps/details?id=com.freedom.vpn"),
            Task("app_rating_1", "Rate Freedom App", "Give us a 5-star rating with an honest review.", 15.0, "1 min", "App", "app", false, 0, 1, "https://play.google.com/store/apps/details?id=com.freedom.earn"),
            Task("app_survey_1", "Complete CPA Offer", "Complete simple survey inside Offerwall.", 30.0, "5 min", "App", "app", false, 0, 3, "https://example.com/offerwall"),

            // Play Games
            Task("game_flappy", "Play Flappy Bird", "Play 5 minutes and score at least 15 points.", 10.0, "5 min", "Games", "game", false, 0, 5, "https://example.com/games/flappy"),
            Task("game_bubble", "Play Bubble Shooter", "Play Bubble Shooter for 5 minutes.", 10.0, "5 min", "Games", "game", false, 0, 5, "https://example.com/games/bubble"),

            // Premium Tasks
            Task("premium_write_1", "Article Writing", "Write a 300-word review about Freedom app.", 150.0, "15 min", "Premium", "premium", false, 0, 1, "https://example.com/submit/article"),
            Task("premium_survey_1", "Mobile Usage Survey", "Answer 15 questions about your daily smartphone usage.", 50.0, "8 min", "Premium", "premium", false, 0, 1, "https://example.com/survey/phone"),
            Task("premium_quiz_1", "Earn Crypto Quiz", "Answer 10 crypto questions correctly (100% score).", 40.0, "5 min", "Premium", "premium", false, 0, 2, "https://example.com/quiz/crypto")
        ))

        // Pre-populate challenges
        dao.insertChallenges(listOf(
            ChallengeItem("challenge_1", "Weekly Betting Warrior", 10, 3, 500, "Medium", "3 days 14 hours", false),
            ChallengeItem("challenge_2", "Ten Referral Milestone", 10, 4, 2000, "Hard", "12 days remaining", false),
            ChallengeItem("challenge_3", "Complete 15 Daily Tasks", 15, 6, 1000, "Easy", "5 days remaining", false),
            ChallengeItem("challenge_4", "Unlock VIP Level 1", 1, 0, 5000, "Hard", "No time limit", false)
        ))

        // Pre-populate referral team if none exist
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(Date())
        dao.insertReferrals(listOf(
            TeamReferral(1, userPhone, "Rahim Khan", "01712345678", 1, "12 Jul 2026", 2450.0, "Active"),
            TeamReferral(2, userPhone, "Karim Alam", "01812345678", 1, "15 Jul 2026", 1500.0, "Active"),
            TeamReferral(3, userPhone, "Limon Hassan", "01912345678", 2, "17 Jul 2026", 980.0, "Active"),
            TeamReferral(4, userPhone, "Jamil Ahmed", "01512345678", 3, "18 Jul 2026", 320.0, "Inactive")
        ))
    }
}
