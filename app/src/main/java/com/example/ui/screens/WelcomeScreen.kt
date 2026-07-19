package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun WelcomeScreen(
    viewModel: FreedomViewModel,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    var showVideoPlayer by remember { mutableStateOf(false) }

    // Outer Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, DeepSurface)
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Main Logo Frame
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PurplePrimary, GoldAccent)
                        )
                    )
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(CardSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = "Freedom Logo",
                        tint = GoldAccent,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Title & Slogan
            Text(
                text = "Freedom",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("app_title")
            )

            Text(
                text = FreedomLocales.get("app_slogan", language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mini description about freedom
            Text(
                text = FreedomLocales.get("about_text", language),
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Trust Statistics Badge Container
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrustBadge(
                    icon = Icons.Default.Star,
                    label = FreedomLocales.get("rating_label", language),
                    modifier = Modifier.weight(1f)
                )
                TrustBadge(
                    icon = Icons.Default.People,
                    label = FreedomLocales.get("users_label", language),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TrustBadge(
                    icon = Icons.Default.Payments,
                    label = FreedomLocales.get("paid_label", language),
                    modifier = Modifier.weight(1f)
                )
                TrustBadge(
                    icon = Icons.Default.Shield,
                    label = FreedomLocales.get("secure_label", language),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Interactive Simulated Video Player Card
            SimulatedVideoPlayer(
                language = language,
                isPlaying = showVideoPlayer,
                onPlayToggle = { showVideoPlayer = !showVideoPlayer }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Live Users and Stats section
            LiveStatisticsPanel(language = language)

            Spacer(modifier = Modifier.height(28.dp))

            // User Reviews Horizontal Scroller
            UserReviewsSection(language = language)

            Spacer(modifier = Modifier.height(35.dp))

            // Bottom CTAs
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("login_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = Color.White
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Login, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = FreedomLocales.get("login_btn", language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("register_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.5.dp, PurplePrimary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HowToReg, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = FreedomLocales.get("register_btn", language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onGuestClick,
                modifier = Modifier.testTag("guest_btn")
            ) {
                Text(
                    text = FreedomLocales.get("guest_btn", language),
                    color = GoldAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TrustBadge(icon: ImageVector, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        ),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GoldAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun SimulatedVideoPlayer(
    language: String,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentSub by remember { mutableStateOf("") }
    var timerRunning by remember { mutableStateOf(false) }

    // Subtitles map based on progress percent
    val subBN = mapOf(
        0f to "১. Freedom আর্নিং অ্যাপে আপনাদের সবাইকে স্বাগতম!",
        0.25f to "২. এখানে আপনি দৈনিক সাধারণ ও প্রিমিয়াম কাজ করে সহজেই টাকা ইনকাম করতে পারবেন।",
        0.55f to "৩. আমাদের রয়েছে ৫০০,০০০+ বিশ্বস্ত পরিবার এবং লক্ষ লক্ষ টাকা সফলভাবে পরিশোধ করা হয়েছে।",
        0.80f to "৪. সরাসরি বিকাশ, নগদ বা রকেটে মাত্র ১ ঘণ্টার মধ্যে পেমেন্ট উত্তোলন করুন!"
    )

    val subEN = mapOf(
        0f to "1. Welcome everyone to the Freedom Earning Platform!",
        0.25f to "2. Here you can easily earn money by completing simple daily and premium tasks.",
        0.55f to "3. We have over 500,000+ active users and millions paid successfully.",
        0.80f to "4. Withdraw your earnings securely to bKash, Nagad or Rocket within 1 hour!"
    )

    val subs = if (language == "BN") subBN else subEN

    // Simulate playback timer
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            timerRunning = true
            while (progress < 1.0f) {
                delay(300)
                progress += 0.015f

                // update subtitle
                val targetKey = subs.keys.filter { it <= progress }.maxOrNull() ?: 0f
                currentSub = subs[targetKey] ?: ""
            }
            progress = 0f
            timerRunning = false
            onPlayToggle() // pause/finish
        } else {
            timerRunning = false
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("simulated_player_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(PurplePrimary, GoldAccent)))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!isPlaying) {
                // Video Cover view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = onPlayToggle,
                        modifier = Modifier
                            .size(60.dp)
                            .background(PurplePrimary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = FreedomLocales.get("watch_intro", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                // Active video simulation view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE TUTORIAL",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onPlayToggle, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                        }
                    }

                    // Simulated video visualization (voiceover audio wave + subtitles)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Simulated voice soundwave
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(24.dp)
                        ) {
                            repeat(8) { index ->
                                val waveHeight = remember { mutableStateOf(10.dp) }
                                LaunchedEffect(isPlaying) {
                                    while (isPlaying) {
                                        delay((100..250).random().toLong())
                                        waveHeight.value = (6..22).random().dp
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(waveHeight.value)
                                        .background(GoldAccent, RoundedCornerShape(1.dp))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Live captions/subtitles box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x99161424), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentSub,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Progress slider line
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = GoldAccent,
                            trackColor = Color.DarkGray,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "${(progress * 100).toInt()}%", color = Color.Gray, fontSize = 9.sp)
                            Text(text = "00:60", color = Color.Gray, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveStatisticsPanel(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = FreedomLocales.get("live_stats", language),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    StatRow(
                        label = FreedomLocales.get("online_users", language),
                        value = "২৪,৫১০ জন",
                        iconTint = NeonGreen
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StatRow(
                        label = FreedomLocales.get("today_withdraw", language),
                        value = "৳ ৩,২৪,৫০০",
                        iconTint = GoldAccent
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    StatRow(
                        label = FreedomLocales.get("today_bonus", language),
                        value = "৳ ৪৫,২০০",
                        iconTint = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StatRow(
                        label = FreedomLocales.get("top_earner", language),
                        value = "Limon - ৳১২,৪৫০",
                        iconTint = PurplePrimary
                    )
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String, iconTint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(iconTint, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, color = Color.Gray, fontSize = 11.sp)
            Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class Review(val name: String, val textBN: String, val textEN: String, val rating: Int, val earn: String)

@Composable
fun UserReviewsSection(language: String) {
    val reviews = listOf(
        Review(
            "Rahim Khan",
            "“Freedom সেরা! আমি ইতিমধ্যে ৫,২০০ টাকা বিকাশ-এ উইথড্র নিয়েছি, পেমেন্ট ১ ঘণ্টায় চলে আসে!”",
            "\"Freedom is awesome! I have withdrawn 5,200 BDT to bKash and got paid within 1 hour!\"",
            5,
            "৳৫,২০০"
        ),
        Review(
            "Karim Alam",
            "“খুব চমৎকার অ্যাপ। দৈনিক কাজগুলো করা খুব সহজ আর প্রতি কাজের ক্লেইম রেট অনেক ভালো।”",
            "\"Excellent app. Daily tasks are very simple and the claim rates are highly attractive.\"",
            5,
            "৳৩,৫০০"
        ),
        Review(
            "Limon S.",
            "“টিম কমিশন সত্যিই চমৎকার। আমার বন্ধুরা কাজ করলেই আমি বোনাস পাচ্ছি। ধন্যবাদ Freedom!”",
            "\"Team commission is incredible. I get active commission when my referred friends work!\"",
            5,
            "৳১২,৪৫০"
        )
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (language == "BN") "💬 গ্রাহকদের মতামত" else "💬 User Reviews",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(reviews) { review ->
                Card(
                    modifier = Modifier
                        .width(260.dp)
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = review.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                            Row {
                                repeat(review.rating) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = GoldAccent,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (language == "BN") review.textBN else review.textEN,
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 15.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = (if (language == "BN") "মোট আয়: " else "Total Earn: ") + review.earn,
                                fontSize = 10.sp,
                                color = GoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
