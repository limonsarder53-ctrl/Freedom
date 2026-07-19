package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.ChallengeItem
import kotlinx.coroutines.flow.map
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel

@Composable
fun EventsScreen(
    viewModel: FreedomViewModel,
    onBackClick: () -> Unit
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val challenges by viewModel.challenges.collectAsState()

    var activeTab by remember { mutableStateOf("Challenges") } // Challenges, Leaderboard

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, DeepSurface)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(CardSurface, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = FreedomLocales.get("challenges", language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tab Selector: Challenges | Leaderboard
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(CardSurface, RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                listOf("Challenges", "Leaderboard").forEach { tab ->
                    val isSel = activeTab == tab
                    val label = when (tab) {
                        "Challenges" -> FreedomLocales.get("challenges_tab", language)
                        else -> FreedomLocales.get("leaderboard_tab", language)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) PurplePrimary else Color.Transparent)
                            .clickable { activeTab = tab }
                            .testTag("events_tab_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tab Content
            when (activeTab) {
                "Challenges" -> {
                    ChallengesList(
                        challenges = challenges,
                        language = language,
                        onClaimClick = { challengeId ->
                            viewModel.claimChallenge(challengeId)
                        }
                    )
                }

                "Leaderboard" -> {
                    LeaderboardPodiumAndList(language = language)
                }
            }
        }
    }
}

@Composable
fun ChallengesList(
    challenges: List<ChallengeItem>,
    language: String,
    onClaimClick: (String) -> Unit
) {
    if (challenges.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No active challenges.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(challenges, key = { it.id }) { ch ->
                val progressVal = (ch.progressCurrent.toFloat() / ch.progressMax.toFloat()).coerceIn(0f, 1f)
                val isFinished = ch.progressCurrent >= ch.progressMax
                val isClaimed = ch.isClaimed

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_card_${ch.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, if (isClaimed) Color.DarkGray else (if (isFinished) GoldAccent else Color(0xFF2C2A3F)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ch.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "+৳ ${String.format("%.0f", ch.rewardCoins.toDouble() / 10.0)}",
                                color = NeonGreen,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress slider
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { progressVal },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isFinished) GoldAccent else PurplePrimary,
                                trackColor = Color(0xFF1E1C31)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${ch.progressCurrent}/${ch.progressMax}",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // CTA Button
                        if (isClaimed) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF2C2A3F), disabledContentColor = Color.Gray)
                            ) {
                                Text(text = if (language == "BN") "ইতিমধ্যে ক্লেইম করেছেন" else "Already Claimed", fontSize = 11.sp)
                            }
                        } else if (isFinished) {
                            Button(
                                onClick = { onClaimClick(ch.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("claim_challenge_${ch.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color.Black)
                            ) {
                                Text(text = if (language == "BN") "বোনাস ক্লেইম করুন" else "Claim Extra Cash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = CardSurface, disabledContentColor = Color.DarkGray),
                                border = BorderStroke(0.5.dp, Color.DarkGray)
                            ) {
                                Text(text = if (language == "BN") "চলমান..." else "In Progress...", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardPodiumAndList(language: String) {
    val listData = listOf(
        LeaderboardUser(1, "Limon Hassan", "১২৪,৫০০ Pts"),
        LeaderboardUser(2, "Karim Miah", "৯৪,২০০ Pts"),
        LeaderboardUser(3, "Rahim Ahmed", "৭৮,০০০ Pts"),
        LeaderboardUser(4, "Sadia Khan", "৫৮,৩০০ Pts"),
        LeaderboardUser(5, "Fahim Reza", "৪৫,১০০ Pts"),
        LeaderboardUser(6, "Israt Jahan", "৩৮,২০০ Pts"),
        LeaderboardUser(7, "You (LimonSarder)", "৩৪,৫১০ Pts", true),
        LeaderboardUser(8, "Mamun Kabir", "৩১,২০০ Pts"),
        LeaderboardUser(9, "Apu Talukder", "২৮,৫০০ Pts"),
        LeaderboardUser(10, "Sumaiya Shimu", "২৫,৮০০ Pts")
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Visual Podium layout for Top 3
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Rank 2 - Silver
            PodiumItem(
                name = "Karim M.",
                points = "৯৪.২k Pts",
                height = 80.dp,
                color = Color(0xFFC0C0C0),
                crownTint = Color(0xFF9E9E9E),
                rankStr = "2"
            )

            // Rank 1 - Gold (Centered, Tallest)
            PodiumItem(
                name = "Limon H.",
                points = "১২৪.৫k Pts",
                height = 105.dp,
                color = GoldAccent,
                crownTint = Color(0xFFFFD700),
                rankStr = "1"
            )

            // Rank 3 - Bronze
            PodiumItem(
                name = "Rahim A.",
                points = "৭৮.০k Pts",
                height = 65.dp,
                color = Color(0xFFCD7F32),
                crownTint = Color(0xFF8D5B28),
                rankStr = "3"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Remainder scroll list
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listData.drop(3)) { user ->
                    val itemBg = if (user.isSelf) PurplePrimary.copy(alpha = 0.15f) else Color.Transparent
                    val itemBorder = if (user.isSelf) BorderStroke(0.5.dp, PurplePrimary) else null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = itemBg),
                        border = itemBorder
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E1C31)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = user.rank.toString(), color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = user.name,
                                    color = if (user.isSelf) GoldAccent else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (user.isSelf) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = user.points, color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumItem(
    name: String,
    points: String,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    crownTint: Color,
    rankStr: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Icon(Icons.Default.Stars, contentDescription = null, tint = crownTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CardSurface)
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = name.take(2).uppercase(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Column
        Card(
            modifier = Modifier
                .width(76.dp)
                .height(height),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.25f)),
            border = BorderStroke(1.dp, color)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "#$rankStr", color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = points, color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
