package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.UserProfile
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.flow.map

@Composable
fun DashboardScreen(
    viewModel: FreedomViewModel,
    onNavigateToTab: (Int) -> Unit, // tab index
    onNavigateToScreen: (String) -> Unit // screen name
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val totalTasks = tasks.size.coerceAtLeast(1)
    val completedTasks = tasks.count { it.isCompleted }
    val progress = completedTasks.toFloat() / totalTasks.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, DeepSurface)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp) // padding for bottom navigation
        ) {
            // Dashboard Top Bar
            DashboardTopBar(
                user = user,
                language = language,
                onProfileClick = { onNavigateToScreen("settings") }
            )

            Column(modifier = Modifier.padding(horizontal = 18.dp)) {
                Spacer(modifier = Modifier.height(12.dp))

                // Premium Balance Card (Total Balance, Today Earnings, Total Income)
                BalanceCard(
                    user = user,
                    language = language,
                    onDepositClick = { onNavigateToTab(4) }, // Wallet Tab
                    onWithdrawClick = { onNavigateToTab(4) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Daily Streak & Task Progress Section
                StreakAndProgressRow(
                    streak = user?.streak ?: 0,
                    progress = progress,
                    completed = completedTasks,
                    total = totalTasks,
                    language = language,
                    onClaimClick = { viewModel.claimDailyBonus() }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Action Cards Scroller
                QuickActionsSection(
                    language = language,
                    onNavigateToTab = onNavigateToTab,
                    onNavigateToScreen = onNavigateToScreen
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick VIP Banner
                VipPromoBanner(language = language, onClick = { onNavigateToScreen("vip") })

                Spacer(modifier = Modifier.height(24.dp))

                // Top Earners Leaderboard Preview (Top 5 preview)
                LeaderboardPreviewSection(language = language, onSeeAllClick = { onNavigateToScreen("events") })

                Spacer(modifier = Modifier.height(24.dp))

                // Live Statistics Card
                LiveStatisticsPanel(language = language)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun DashboardTopBar(
    user: UserProfile?,
    language: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo & Brand Name
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PurplePrimary, GoldAccent)))
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(CardSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Freedom",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                val totalEarnings = user?.totalIncome ?: 0.0
                val tierBadge = when {
                    totalEarnings >= 10000.0 -> "💎 Diamond"
                    totalEarnings >= 5000.0 -> "🟦 Platinum"
                    totalEarnings >= 2000.0 -> "🟨 Gold"
                    totalEarnings >= 500.0 -> "⬜ Silver"
                    else -> "🟫 Bronze"
                }
                Text(
                    text = if (user?.isVip == true) "💎 ${user.vipLevel} VIP | $tierBadge" else "$tierBadge Member",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (user?.isVip == true) GoldAccent else Color.Gray
                )
            }
        }

        // Notification & Profile clickables
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .padding(end = 8.dp)
                    .background(CardSurface, CircleShape)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary)
                    .clickable { onProfileClick() }
                    .testTag("top_bar_profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user?.fullName?.take(2)?.uppercase()) ?: "US",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BalanceCard(
    user: UserProfile?,
    language: String,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_balance_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PurplePrimary, PurpleDark)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = FreedomLocales.get("total_balance", language).uppercase(),
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "৳ ${String.format("%.2f", user?.balance ?: 0.0)}",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Equivalent gold points
                    Row(
                        modifier = Modifier
                            .background(Color(0x33000000), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${((user?.balance ?: 0.0) * 10).toInt()} Pts",
                            color = GoldAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Divider(color = Color(0x33FFFFFF), thickness = 1.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = FreedomLocales.get("today_earnings", language),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "৳ ${String.format("%.2f", user?.todayEarnings ?: 0.0)}",
                            color = NeonGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = FreedomLocales.get("total_income", language),
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "৳ ${String.format("%.2f", user?.totalIncome ?: 0.0)}",
                            color = GoldAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Deposit & Withdraw Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDepositClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_deposit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AddCard, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == "BN") "ডিপোজিট" else "Deposit",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onWithdrawClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_withdraw_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == "BN") "উইথড্র" else "Withdraw",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakAndProgressRow(
    streak: Int,
    progress: Float,
    completed: Int,
    total: Int,
    language: String,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak Fire
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0x1AFF3D00), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF3D00), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = FreedomLocales.get("daily_streak", language),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "$streak " + (if (language == "BN") "দিন" else "Days"),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Daily Claim Button
                Button(
                    onClick = onClaimClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp).testTag("claim_daily_streak_btn")
                ) {
                    Text(
                        text = if (language == "BN") "🎁 ক্লেইম" else "Claim",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = FreedomLocales.get("tasks_progress", language),
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$completed/$total",
                    color = GoldAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PurplePrimary,
                trackColor = Color(0xFF2C2A3F)
            )
        }
    }
}

data class QuickAction(val titleBN: String, val titleEN: String, val icon: ImageVector, val color: Color, val destination: String, val isTab: Boolean, val tabIndex: Int = 0)

@Composable
fun QuickActionsSection(
    language: String,
    onNavigateToTab: (Int) -> Unit,
    onNavigateToScreen: (String) -> Unit
) {
    val items = listOf(
        QuickAction("টাস্ক সেন্টার", "Tasks", Icons.Default.Task, NeonGreen, "Earn", true, 1),
        QuickAction("লাকি হুইল", "Spin Wheel", Icons.Default.Cached, GoldAccent, "Bonus", true, 2),
        QuickAction("টিম রেফার", "Referrals", Icons.Default.GroupAdd, ElectricBlue, "Referral", true, 3),
        QuickAction("ভিআইপি প্ল্যান", "VIP Gold", Icons.Default.WorkspacePremium, PurplePrimary, "vip", false),
        QuickAction("পরিসংখ্যান", "Analytics", Icons.Default.InsertChart, Color(0xFFFF3F80), "statistics", false)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (language == "BN") "🚀 দ্রুত ক্লেইম করুন" else "🚀 Quick Earn Panels",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .height(110.dp)
                        .clickable {
                            if (item.isTab) onNavigateToTab(item.tabIndex) else onNavigateToScreen(item.destination)
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(item.color.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.color,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Text(
                            text = if (language == "BN") item.titleBN else item.titleEN,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VipPromoBanner(language: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(85.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GoldAccent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF2A1C54), Color(0xFF140D2F))
                    )
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(GoldAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (language == "BN") "💎 ভিআইপি মেম্বারশিপ" else "💎 Join VIP Elite Tiers",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == "BN") "প্রতিদিন ৳৩০০+ অটো উপার্জন চালু করুন" else "Earn 300+ BDT passively every day",
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                }
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = GoldAccent)
        }
    }
}

data class LeaderboardUser(val rank: Int, val name: String, val points: String, val isSelf: Boolean = false)

@Composable
fun LeaderboardPreviewSection(language: String, onSeeAllClick: () -> Unit) {
    val topUsers = listOf(
        LeaderboardUser(1, "Limon Hassan", "১২৪,৫০০ Pts", false),
        LeaderboardUser(2, "Karim Miah", "৯৪,২০০ Pts", false),
        LeaderboardUser(3, "Rahim Ahmed", "৭৮,০০০ Pts", false),
        LeaderboardUser(4, "Sadia Khan", "৫৮,৩০০ Pts", false),
        LeaderboardUser(5, "Fahim Reza", "৪৫,১০০ Pts", false)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = FreedomLocales.get("leaderboard_preview", language),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (language == "BN") "সব দেখুন" else "See All",
                color = GoldAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSeeAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                topUsers.forEachIndexed { index, user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Rank Badge
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (user.rank) {
                                            1 -> GoldAccent
                                            2 -> Color(0xFFC0C0C0)
                                            3 -> Color(0xFFCD7F32)
                                            else -> Color(0xFF2C2A3F)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.rank.toString(),
                                    color = if (user.rank <= 3) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = user.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.points,
                                color = GoldAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (index < topUsers.size - 1) {
                        Divider(color = Color(0xFF1E1C31), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}
