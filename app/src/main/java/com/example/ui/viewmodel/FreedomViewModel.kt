package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.FreedomRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FreedomViewModel(
    application: Application,
    private val repository: FreedomRepository
) : AndroidViewModel(application) {

    // Authentication States
    private val _activeUserPhone = MutableStateFlow<String?>(null)
    val activeUserPhone: StateFlow<String?> = _activeUserPhone.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isRegistering = MutableStateFlow(false)
    val isRegistering: StateFlow<Boolean> = _isRegistering.asStateFlow()

    // Active User Profile Flow
    val activeUser: StateFlow<UserProfile?> = _activeUserPhone
        .flatMapLatest { phone ->
            if (phone != null) repository.getUser(phone) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Task Flow
    val tasks: StateFlow<List<Task>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Transaction Flow
    val transactions: StateFlow<List<TransactionLog>> = _activeUserPhone
        .flatMapLatest { phone ->
            if (phone != null) repository.getTransactions(phone) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Referral Flow
    val referrals: StateFlow<List<TeamReferral>> = _activeUserPhone
        .flatMapLatest { phone ->
            if (phone != null) repository.getReferrals(phone) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Challenges Flow
    val challenges: StateFlow<List<ChallengeItem>> = repository.getAllChallenges()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // General Screen Routing Flow
    private val _currentScreen = MutableStateFlow("splash") // splash, welcome, login, home, vip, events, statistics, settings
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Inner dialog and toast events
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        // Automatically transition from splash to welcome after 2 seconds
        viewModelScope.launch {
            delay(2000)
            _currentScreen.value = "welcome"
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    // AUTH ACTIONS
    fun login(phone: String, password: String): Boolean {
        if (phone.isBlank() || password.isBlank()) {
            _authError.value = "ফোন নম্বর এবং পাসওয়ার্ড পূরণ করুন"
            return false
        }
        var success = false
        viewModelScope.launch {
            val existing = repository.getUserSync(phone)
            if (existing != null) {
                if (existing.phone == "0000000000" || existing.referralCode == password) { // Simple demo password validation: password matches referral or profile password field
                    _activeUserPhone.value = phone
                    repository.seedInitialDataIfNecessary(phone)
                    _authError.value = null
                    _currentScreen.value = "home"
                    _toastMessage.value = "সফলভাবে লগইন হয়েছে"
                    success = true
                } else {
                    _authError.value = "ভুল পাসওয়ার্ড, অনুগ্রহ করে সঠিক পাসওয়ার্ড দিন"
                }
            } else {
                // If user not exists, we can auto-register them or show error. Let's register them!
                val newUser = UserProfile(
                    phone = phone,
                    fullName = "ব্যবহারকারী",
                    referralCode = "REF" + (100..999).random(),
                    balance = 100.0, // initial welcome balance
                    todayEarnings = 0.0,
                    totalIncome = 100.0,
                    streak = 1,
                    lastCheckInDate = System.currentTimeMillis()
                )
                repository.insertUser(newUser)
                _activeUserPhone.value = phone
                repository.seedInitialDataIfNecessary(phone)
                _authError.value = null
                _currentScreen.value = "home"
                _toastMessage.value = "নতুন অ্যাকাউন্ট তৈরি এবং লগইন হয়েছে!"
                success = true
            }
        }
        return success
    }

    fun register(fullName: String, phone: String, referralCode: String, pass: String, confirmPass: String) {
        if (fullName.isBlank() || phone.isBlank() || pass.isBlank()) {
            _authError.value = "সবগুলো প্রয়োজনীয় ঘর পূরণ করুন"
            return
        }
        if (pass != confirmPass) {
            _authError.value = "পাসওয়ার্ড দুটি মেলেনি"
            return
        }
        viewModelScope.launch {
            val existing = repository.getUserSync(phone)
            if (existing != null) {
                _authError.value = "এই ফোন নম্বরটি ইতিমধ্যে নিবন্ধিত"
                return@launch
            }

            val referralBonus = if (referralCode.isNotBlank()) 50.0 else 0.0
            val newUser = UserProfile(
                phone = phone,
                fullName = fullName,
                referralCode = referralCode.ifBlank { "REF" + (100..999).random() },
                balance = 100.0 + referralBonus, // Welcome + referral bonus
                todayEarnings = referralBonus,
                totalIncome = 100.0 + referralBonus,
                streak = 1,
                lastCheckInDate = System.currentTimeMillis()
            )

            repository.insertUser(newUser)
            _activeUserPhone.value = phone
            repository.seedInitialDataIfNecessary(phone)
            _authError.value = null
            _currentScreen.value = "home"
            _toastMessage.value = "নিবন্ধন সফল হয়েছে!"
        }
    }

    fun loginAsGuest() {
        viewModelScope.launch {
            val guestPhone = "0000000000"
            val existing = repository.getUserSync(guestPhone)
            if (existing == null) {
                val guestProfile = UserProfile(
                    phone = guestPhone,
                    fullName = "Guest User (অতিথি)",
                    referralCode = "GUEST99",
                    balance = 50.0,
                    todayEarnings = 0.0,
                    totalIncome = 50.0,
                    streak = 1,
                    lastCheckInDate = System.currentTimeMillis()
                )
                repository.insertUser(guestProfile)
            }
            _activeUserPhone.value = guestPhone
            repository.seedInitialDataIfNecessary(guestPhone)
            _authError.value = null
            _currentScreen.value = "home"
            _toastMessage.value = "অতিথি হিসেবে সফল প্রবেশ"
        }
    }

    fun logout() {
        _activeUserPhone.value = null
        _currentScreen.value = "welcome"
        _toastMessage.value = "সফলভাবে লগআউট করা হয়েছে"
    }

    // SETTINGS ACTIONS
    fun toggleLanguage() {
        val user = activeUser.value ?: return
        val newLang = if (user.language == "BN") "EN" else "BN"
        viewModelScope.launch {
            repository.updateUser(user.copy(language = newLang))
            _toastMessage.value = if (newLang == "BN") "ভাষা পরিবর্তন করা হয়েছে: বাংলা" else "Language changed to English"
        }
    }

    fun toggleDarkMode() {
        val user = activeUser.value ?: return
        val currentMode = user.isDarkMode
        viewModelScope.launch {
            repository.updateUser(user.copy(isDarkMode = !currentMode))
        }
    }

    // EARNINGS / TASK ACTIONS
    fun completeTask(taskId: String) {
        val phone = _activeUserPhone.value ?: return
        val user = activeUser.value ?: return
        viewModelScope.launch {
            val taskList = tasks.value
            val task = taskList.find { it.id == taskId } ?: return@launch

            if (task.isCompleted) {
                _toastMessage.value = if (user.language == "BN") "টাস্কটি ইতিমধ্যে সম্পন্ন হয়েছে" else "Task already completed"
                return@launch
            }

            val multiplier = when {
                user.totalIncome >= 10000.0 -> 2.0
                user.totalIncome >= 5000.0 -> 1.5
                user.totalIncome >= 2000.0 -> 1.25
                user.totalIncome >= 500.0 -> 1.1
                else -> 1.0
            }
            val scaledReward = task.reward * multiplier

            // Update Task complete
            val updatedTask = task.copy(isCompleted = true, completedCount = task.completedCount + 1)
            repository.updateTask(updatedTask)

            // Insert log
            repository.insertTaskLog(
                UserTaskLog(
                    phone = phone,
                    taskId = taskId,
                    timestamp = System.currentTimeMillis(),
                    rewardEarned = scaledReward,
                    status = "Completed"
                )
            )

            // Update User Profile Balance
            val newBalance = user.balance + scaledReward
            val newToday = user.todayEarnings + scaledReward
            val newTotal = user.totalIncome + scaledReward
            repository.updateUser(user.copy(
                balance = newBalance,
                todayEarnings = newToday,
                totalIncome = newTotal
            ))

            // Update associated Challenge Progress
            val challengeList = challenges.value
            val targetChallenge = challengeList.find { it.id == "challenge_3" } // Complete 15 Daily Tasks
            if (targetChallenge != null && targetChallenge.progressCurrent < targetChallenge.progressMax) {
                val newProgress = targetChallenge.progressCurrent + 1
                repository.updateChallenge(targetChallenge.copy(progressCurrent = newProgress))
            }

            _toastMessage.value = if (user.language == "BN") {
                "অভিনন্দন! +৳${String.format("%.1f", scaledReward)} অর্জিত হয়েছে (টিয়ার বোনাস সহ)।"
            } else {
                "Congratulations! +৳${String.format("%.1f", scaledReward)} earned (with Tier multiplier)."
            }
        }
    }

    // BONUS CENTER ACTIONS
    fun claimDailyBonus() {
        val user = activeUser.value ?: return
        val phone = _activeUserPhone.value ?: return
        val now = System.currentTimeMillis()

        // 24 hours cooldown check (for simple testing we can skip or allow once per app session / calendar day)
        val diff = now - user.lastCheckInDate
        if (diff < 12 * 60 * 60 * 1000 && user.lastCheckInDate > 0L) { // Allow checkin after 12h for easier simulation
            _toastMessage.value = if (user.language == "BN") "আজকের বোনাস ইতিমধ্যে ক্লেইম করা হয়েছে!" else "Daily bonus already claimed today!"
            return
        }

        viewModelScope.launch {
            val bonusAmount = 25.0
            val newStreak = user.streak + 1
            repository.updateUser(user.copy(
                balance = user.balance + bonusAmount,
                todayEarnings = user.todayEarnings + bonusAmount,
                totalIncome = user.totalIncome + bonusAmount,
                streak = newStreak,
                lastCheckInDate = now
            ))

            // Log checking
            repository.insertTransaction(
                TransactionLog(
                    phone = phone,
                    type = "Bonus",
                    amount = bonusAmount,
                    method = "System",
                    number = "Daily Claim",
                    transIdOrRef = "CHECKIN_${newStreak}",
                    status = "Completed",
                    timestamp = now
                )
            )

            _toastMessage.value = if (user.language == "BN") "দৈনিক লগইন বোনাস +৳$bonusAmount ক্লেইম করা হয়েছে! 🔥 ${newStreak} দিনের স্ট্রিক" else "Daily login bonus +৳$bonusAmount claimed! 🔥 ${newStreak} days streak"
        }
    }

    fun spinLuckyWheel(): String {
        val user = activeUser.value ?: "guest"
        val phone = _activeUserPhone.value ?: return ""
        val prizes = listOf("৳5.00", "৳10.00", "৳50.00", "৳100.00", "Mystery Box (৳20)", "৳2.00")
        val prize = prizes.random()
        val amount = when (prize) {
            "৳5.00" -> 5.0
            "৳10.00" -> 10.0
            "৳50.00" -> 50.0
            "৳100.00" -> 100.0
            "Mystery Box (৳20)" -> 20.0
            else -> 2.0
        }

        viewModelScope.launch {
            val activeUserVal = activeUser.value ?: return@launch
            repository.updateUser(activeUserVal.copy(
                balance = activeUserVal.balance + amount,
                todayEarnings = activeUserVal.todayEarnings + amount,
                totalIncome = activeUserVal.totalIncome + amount
            ))

            repository.insertTransaction(
                TransactionLog(
                    phone = phone,
                    type = "LuckyWheel",
                    amount = amount,
                    method = "System",
                    number = "Lucky Wheel Spin",
                    transIdOrRef = "SPIN_" + (1000..9999).random(),
                    status = "Completed",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        return prize
    }

    fun claimChallenge(challengeId: String) {
        val user = activeUser.value ?: return
        viewModelScope.launch {
            val challengeList = challenges.value
            val challenge = challengeList.find { it.id == challengeId } ?: return@launch

            if (challenge.isClaimed) return@launch
            if (challenge.progressCurrent < challenge.progressMax) return@launch

            val rewardAmount = challenge.rewardCoins / 10.0 // 10 coins = 1 BDT conversion
            repository.updateChallenge(challenge.copy(isClaimed = true))
            repository.updateUser(user.copy(
                balance = user.balance + rewardAmount,
                totalIncome = user.totalIncome + rewardAmount
            ))

            _toastMessage.value = if (user.language == "BN") "মাইলস্টোন বোনাস +৳${rewardAmount} সফলভাবে ক্লেইম করা হয়েছে!" else "Milestone bonus +৳${rewardAmount} claimed successfully!"
        }
    }

    fun applyPromoCode(code: String): Boolean {
        val user = activeUser.value ?: return false
        val phone = _activeUserPhone.value ?: return false
        val upperCode = code.uppercase().trim()

        val reward = when (upperCode) {
            "FREEDOM50" -> 50.0
            "VIPPROMO" -> 100.0
            "BONUS100" -> 100.0
            else -> 0.0
        }

        if (reward == 0.0) {
            _toastMessage.value = if (user.language == "BN") "ভুল প্রমো কোড!" else "Invalid Promo Code!"
            return false
        }

        viewModelScope.launch {
            repository.updateUser(user.copy(
                balance = user.balance + reward,
                todayEarnings = user.todayEarnings + reward,
                totalIncome = user.totalIncome + reward
            ))

            repository.insertTransaction(
                TransactionLog(
                    phone = phone,
                    type = "PromoCode",
                    amount = reward,
                    method = "System",
                    number = upperCode,
                    transIdOrRef = "PROMO_" + (1000..9999).random(),
                    status = "Completed",
                    timestamp = System.currentTimeMillis()
                )
            )
        }

        _toastMessage.value = if (user.language == "BN") "প্রমো কোড সফলভাবে যুক্ত হয়েছে! +৳$reward" else "Promo code applied successfully! +৳$reward"
        return true
    }

    // WALLET ACTIONS
    fun deposit(method: String, number: String, amount: Double, transactionId: String) {
        val phone = _activeUserPhone.value ?: return
        val user = activeUser.value ?: return
        if (number.isBlank() || amount <= 0.0 || transactionId.isBlank()) {
            _toastMessage.value = if (user.language == "BN") "দয়া করে সঠিক তথ্য প্রদান করুন" else "Please fill correct details"
            return
        }

        viewModelScope.launch {
            val depositTx = TransactionLog(
                phone = phone,
                type = "Deposit",
                amount = amount,
                method = method,
                number = number,
                transIdOrRef = transactionId,
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(depositTx)
            _toastMessage.value = if (user.language == "BN") "ডিপোজিট রিকোয়েস্ট সাবমিট হয়েছে। ৫-৩০ মিনিটের মধ্যে অ্যাডমিন ভেরিফাই করবে।" else "Deposit submitted. Admin will verify in 5-30 minutes."

            // Auto-approve after 8 seconds to make the app's interactive demo extremely satisfying!
            launch {
                delay(8000)
                // fetch latest user and transaction to avoid conflict
                val latestUser = repository.getUserSync(phone)
                if (latestUser != null) {
                    val allTx = repository.getTransactions(phone).first()
                    val targetTx = allTx.find { it.transIdOrRef == transactionId && it.status == "Pending" }
                    if (targetTx != null) {
                        repository.insertTransaction(targetTx.copy(status = "Completed"))
                        repository.updateUser(latestUser.copy(
                            balance = latestUser.balance + amount,
                            totalIncome = latestUser.totalIncome + amount
                        ))
                        _toastMessage.value = if (user.language == "BN") "ডিপোজিট সফল! ৳$amount ব্যালেন্সে যোগ হয়েছে।" else "Deposit Approved! ৳$amount added to balance."
                    }
                }
            }
        }
    }

    fun withdraw(method: String, number: String, amount: Double) {
        val phone = _activeUserPhone.value ?: return
        val user = activeUser.value ?: return

        if (number.isBlank() || amount < 100.0 || amount > 25000.0) {
            _toastMessage.value = if (user.language == "BN") "উত্তোলনের সীমা: ন্যূনতম ১০০ টাকা, সর্বোচ্চ ২৫,০০০ টাকা" else "Withdraw limit: Min 100 BDT, Max 25,000 BDT"
            return
        }

        if (user.balance < amount) {
            _toastMessage.value = if (user.language == "BN") "আপনার পর্যাপ্ত ব্যালেন্স নেই!" else "Insufficient balance!"
            return
        }

        viewModelScope.launch {
            // Deduct instantly
            val fee = amount * 0.01 // 1% fee
            val netReceived = amount - fee
            repository.updateUser(user.copy(balance = user.balance - amount))

            val withdrawTx = TransactionLog(
                phone = phone,
                type = "Withdraw",
                amount = amount,
                method = method,
                number = number,
                transIdOrRef = "WD_" + (100000..999999).random().toString(),
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(withdrawTx)
            _toastMessage.value = if (user.language == "BN") "উত্তোলন রিকোয়েস্ট সফল! ১ ঘণ্টার মধ্যে টাকা পৌঁছে যাবে।" else "Withdraw request successful! Received within 1 hour."

            // Auto-approve after 12 seconds for interactive simulation!
            launch {
                delay(12000)
                val allTx = repository.getTransactions(phone).first()
                val targetTx = allTx.find { it.type == "Withdraw" && it.status == "Pending" }
                if (targetTx != null) {
                    repository.insertTransaction(targetTx.copy(status = "Completed"))
                    _toastMessage.value = if (user.language == "BN") "উত্তোলন সফল! ৳${amount - fee} আপনার $method অ্যাকাউন্টে পৌঁছেছে।" else "Withdraw completed! ৳${amount - fee} received on your $method account."
                }
            }
        }
    }

    // VIP MEMBERSHIP ACTIONS
    fun purchaseVipPlan(planName: String, price: Double) {
        val user = activeUser.value ?: return
        val phone = _activeUserPhone.value ?: return

        if (user.balance < price) {
            _toastMessage.value = if (user.language == "BN") "পর্যাপ্ত ব্যালেন্স নেই। দয়া করে প্রথমে ডিপোজিট করুন।" else "Insufficient balance. Please deposit first."
            return
        }

        viewModelScope.launch {
            // Deduct price and update VIP status
            repository.updateUser(user.copy(
                balance = user.balance - price,
                isVip = true,
                vipLevel = planName
            ))

            // Add transaction log
            repository.insertTransaction(
                TransactionLog(
                    phone = phone,
                    type = "VIP Purchase",
                    amount = price,
                    method = "System",
                    number = planName,
                    transIdOrRef = "VIP_" + planName.uppercase() + "_" + (1000..9999).random(),
                    status = "Completed",
                    timestamp = System.currentTimeMillis()
                )
            )

            // Update associated VIP Challenge
            val challengeList = challenges.value
            val targetChallenge = challengeList.find { it.id == "challenge_4" } // Unlock VIP Level 1
            if (targetChallenge != null) {
                repository.updateChallenge(targetChallenge.copy(progressCurrent = 1))
            }

            _toastMessage.value = if (user.language == "BN") "অভিনন্দন! আপনি এখন সফলভাবে Freedom $planName VIP গ্রাহক!" else "Congratulations! You are now a Freedom $planName VIP subscriber!"
        }
    }
}

class FreedomViewModelFactory(
    private val application: Application,
    private val repository: FreedomRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FreedomViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FreedomViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
