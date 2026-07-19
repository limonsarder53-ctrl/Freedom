package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

@Composable
fun MainAppContainer(
    viewModel: FreedomViewModel
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Screen State Machine: "Splash", "Welcome", "Auth", "MainTabs", "VIP", "Statistics", "Settings"
    var currentScreen by remember { mutableStateOf("Splash") }
    var currentTab by remember { mutableStateOf(0) } // Home, Earn, Bonus, Referral, Wallet
    var initialTabIsRegister by remember { mutableStateOf(false) }

    // Splash Timer
    LaunchedEffect(Unit) {
        delay(2500)
        if (user != null) {
            currentScreen = "MainTabs"
        } else {
            currentScreen = "Welcome"
        }
    }

    // Auto navigate to MainTabs when user logs in
    LaunchedEffect(user) {
        if (user != null && (currentScreen == "Welcome" || currentScreen == "Auth" || currentScreen == "Splash")) {
            currentScreen = "MainTabs"
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Router
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                "Splash" -> {
                    SplashScreenSimulated()
                }

                "Welcome" -> {
                    WelcomeScreen(
                        viewModel = viewModel,
                        onLoginClick = {
                            initialTabIsRegister = false
                            currentScreen = "Auth"
                        },
                        onRegisterClick = {
                            initialTabIsRegister = true
                            currentScreen = "Auth"
                        },
                        onGuestClick = {
                            viewModel.loginAsGuest()
                            currentScreen = "MainTabs"
                        }
                    )
                }

                "Auth" -> {
                    LoginRegisterScreen(
                        viewModel = viewModel,
                        initialTabIsRegister = initialTabIsRegister,
                        onBackClick = { currentScreen = "Welcome" }
                    )
                }

                "MainTabs" -> {
                    Scaffold(
                        bottomBar = {
                            BottomNavBar(
                                currentTab = currentTab,
                                onTabSelect = { currentTab = it },
                                language = language
                            )
                        },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                0 -> DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToTab = { currentTab = it },
                                    onNavigateToScreen = { screenName ->
                                        currentScreen = when (screenName) {
                                            "vip" -> "VIP"
                                            "statistics" -> "Statistics"
                                            else -> "Settings"
                                        }
                                    }
                                )

                                1 -> EarnScreen(viewModel = viewModel)
                                2 -> BonusScreen(viewModel = viewModel)
                                3 -> ReferralScreen(viewModel = viewModel)
                                4 -> WalletScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                "VIP" -> {
                    VipScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = "MainTabs" }
                    )
                }

                "Statistics" -> {
                    StatisticsScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = "MainTabs" }
                    )
                }

                "Settings" -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBackClick = { currentScreen = "MainTabs" },
                        onLogoutClick = {
                            viewModel.logout()
                            currentScreen = "Welcome"
                            currentTab = 0
                        }
                    )
                }
            }
        }

        // Global Overlay Notification Toast Card
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            if (toastMessage != null) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepSurface),
                    border = BorderStroke(1.5.dp, GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .testTag("global_toast_card")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(GoldAccent.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Stars, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = toastMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearToast() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreenSimulated() {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashLogoSpin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SplashLogoAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .rotate(angle)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PurplePrimary, GoldAccent)))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(BackgroundDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Logo",
                        tint = GoldAccent,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Freedom",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Smart Earning Platform",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun BottomNavBar(
    currentTab: Int,
    onTabSelect: (Int) -> Unit,
    language: String
) {
    NavigationBar(
        containerColor = CardSurface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(0.5.dp, Color(0xFF2C2A3F), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        val tabs = listOf(
            TabDetails(Icons.Default.Home, Icons.Outlined.Home, "home"),
            TabDetails(Icons.Default.Task, Icons.Outlined.Task, "earn"),
            TabDetails(Icons.Default.CardGiftcard, Icons.Outlined.CardGiftcard, "bonus"),
            TabDetails(Icons.Default.Group, Icons.Outlined.Group, "referral"),
            TabDetails(Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "wallet")
        )

        tabs.forEachIndexed { index, tab ->
            val isSelected = currentTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelect(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.filledIcon else tab.outlinedIcon,
                        contentDescription = tab.localeKey,
                        tint = if (isSelected) GoldAccent else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = FreedomLocales.get(tab.localeKey, language),
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GoldAccent else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                ),
                modifier = Modifier.testTag("nav_item_${tab.localeKey}")
            )
        }
    }
}

data class TabDetails(val filledIcon: androidx.compose.ui.graphics.vector.ImageVector, val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector, val localeKey: String)
