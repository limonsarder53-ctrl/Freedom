package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import com.example.data.database.UserProfile
import kotlinx.coroutines.flow.map

@Composable
fun VipScreen(
    viewModel: FreedomViewModel,
    onBackClick: () -> Unit
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()

    var showConfirmDialogPlan by remember { mutableStateOf<VipPlan?>(null) }

    val plans = listOf(
        VipPlan("Basic VIP", 499.0, 50.0, "Bronze", listOf(
            if (language == "BN") "দৈনিক টাস্ক বোনাস ৫০ টাকা" else "Daily task bonus: 50 BDT",
            if (language == "BN") "১-দিনের মধ্যে উইথড্র" else "Withdrawal within 24 hours",
            if (language == "BN") "২% অতিরিক্ত রেফারেল কমিশন" else "2% Extra referral multiplier"
        )),
        VipPlan("Premium VIP", 999.0, 150.0, "Gold", listOf(
            if (language == "BN") "দৈনিক টাস্ক বোনাস ১৫০ টাকা" else "Daily task bonus: 150 BDT",
            if (language == "BN") "১২ ঘণ্টার মধ্যে অগ্রাধিকার উইথড্র" else "Withdrawal within 12 hours priority",
            if (language == "BN") "৫% অতিরিক্ত রেফারেল কমিশন" else "5% Extra referral multiplier",
            if (language == "BN") "বিজ্ঞাপনহীন প্রিমিয়াম ইন্টারফেস" else "Completely Ad-free UI"
        )),
        VipPlan("Elite VIP", 1999.0, 300.0, "Diamond", listOf(
            if (language == "BN") "দৈনিক টাস্ক বোনাস ৩০০ টাকা" else "Daily task bonus: 300 BDT",
            if (language == "BN") "১ ঘণ্টার মধ্যে জরুরি উইথড্র" else "Withdrawal within 1 hour express",
            if (language == "BN") "১০% অতিরিক্ত রেফারেল কমিশন" else "10% Extra referral multiplier",
            if (language == "BN") "বিজ্ঞাপনহীন + ২৪/৭ লাইভ এজেন্ট চ্যাট" else "Ad-free + 24/7 dedicated live agent support"
        ))
    )

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
                .verticalScroll(rememberScrollState())
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
                    text = FreedomLocales.get("vip_membership", language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // VIP Earnings Tier Status module
            EarningsTierProgressCard(userProfile = user, language = language)

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle banner
            Text(
                text = FreedomLocales.get("vip_slogan", language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = GoldAccent,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = FreedomLocales.get("choose_plan", language),
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Plans listing
            plans.forEach { plan ->
                val isUserPlan = user?.vipLevel == plan.levelName && user?.isVip == true
                val cardBorderColor = if (isUserPlan) GoldAccent else Color(0xFF2C2A3F)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("vip_plan_card_${plan.name.replace(" ", "_")}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                if (isUserPlan) Brush.linearGradient(listOf(Color(0xFF2E1C5A), CardSurface)) else Brush.linearGradient(listOf(CardSurface, CardSurface))
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Daily Gain: ৳${String.format("%.0f", plan.dailyLimit)}",
                                    color = NeonGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(GoldAccent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "৳ ${String.format("%.0f", plan.price)}",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Divider(color = Color.DarkGray, thickness = 0.5.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Features List
                        plan.features.forEach { feature ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = feature, color = Color.LightGray, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isUserPlan) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = NeonGreen.copy(alpha = 0.2f),
                                    disabledContentColor = NeonGreen
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = if (language == "BN") "আপনার চলমান প্ল্যান" else "Active Premium Tier", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Button(
                                onClick = { showConfirmDialogPlan = plan },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_vip_${plan.levelName}"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PurplePrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = if (language == "BN") "এখনই কিনুন" else "Unlock VIP Tier",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Extra Benefits grid
            VipBenefitsGrid(language = language)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Purchase Confirm dialog
    if (showConfirmDialogPlan != null) {
        val selectedPlan = showConfirmDialogPlan!!
        Dialog(onDismissRequest = { showConfirmDialogPlan = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSurface),
                border = BorderStroke(1.dp, Color.DarkGray),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (language == "BN") "মেম্বারশিপ নিশ্চিত করুন" else "Confirm VIP Upgrade",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == "BN") {
                            "আপনি কি ৳${selectedPlan.price} টাকা পরিশোধ করে '${selectedPlan.name}' এ আপগ্রেড করতে চান?"
                        } else {
                            "Are you sure you want to upgrade to '${selectedPlan.name}' for ৳${selectedPlan.price}?"
                        },
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showConfirmDialogPlan = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (language == "BN") "বাতিল" else "Cancel", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                val currentBalance = user?.balance ?: 0.0
                                if (currentBalance < selectedPlan.price) {
                                    viewModel.showToast(
                                        if (language == "BN") "অপর্যাপ্ত ব্যালেন্স! দয়া করে রিচার্জ করুন।" else "Insufficient balance! Please deposit BDT first."
                                    )
                                } else {
                                    viewModel.purchaseVipPlan(selectedPlan.levelName, selectedPlan.price)
                                    viewModel.showToast(
                                        if (language == "BN") "অভিনন্দন! আপনি সফলভাবে VIP আপগ্রেড করেছেন।" else "Success! VIP Premium activated successfully!"
                                    )
                                }
                                showConfirmDialogPlan = null
                            },
                            modifier = Modifier.weight(1f).testTag("dialog_confirm_buy_vip"),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                        ) {
                            Text(text = if (language == "BN") "হ্যাঁ, কিনুন" else "Buy Now", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

data class VipPlan(val name: String, val price: Double, val dailyLimit: Double, val levelName: String, val features: List<String>)

@Composable
fun VipBenefitsGrid(language: String) {
    val benefits = listOf(
        BenefitItem(Icons.Default.Speed, if (language == "BN") "১ ঘণ্টার উইথড্র" else "1-Hour Payout"),
        BenefitItem(Icons.Default.Block, if (language == "BN") "১০০% বিজ্ঞাপনহীন" else "Ad-Free Access"),
        BenefitItem(Icons.Default.SupportAgent, if (language == "BN") "২৪/৭ বিশেষ চ্যাট" else "Direct Live Support"),
        BenefitItem(Icons.Default.CardGiftcard, if (language == "BN") "ডবল স্পিন লিমিট" else "Double Spin Limit"),
        BenefitItem(Icons.Default.Cached, if (language == "BN") "অটো পেমেন্ট ভেরিফাই" else "Auto Deposit Claim"),
        BenefitItem(Icons.Default.Timeline, if (language == "BN") "মাসিক প্রাইজমানি" else "Premium Leaderboard")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (language == "BN") "🌟 মেম্বারশিপ এর বাড়তি সুবিধাসমূহ" else "🌟 VIP Premium Super Benefits",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            benefits.take(3).forEach { item ->
                BenefitCard(item = item, modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            benefits.takeLast(3).forEach { item ->
                BenefitCard(item = item, modifier = Modifier.weight(1f))
            }
        }
    }
}

data class BenefitItem(val icon: ImageVector, val text: String)

@Composable
fun BenefitCard(item: BenefitItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = item.icon, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.text, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

data class EarningsTier(
    val id: String,
    val nameEN: String,
    val nameBN: String,
    val minEarnings: Double,
    val multiplier: Double,
    val badge: String,
    val icon: ImageVector,
    val color: Color,
    val benefitsEN: List<String>,
    val benefitsBN: List<String>
)

@Composable
fun EarningsTierProgressCard(
    userProfile: UserProfile?,
    language: String
) {
    val totalEarnings = userProfile?.totalIncome ?: 0.0

    val earningsTiers = remember {
        listOf(
            EarningsTier(
                id = "bronze",
                nameEN = "Bronze Member",
                nameBN = "ব্রোঞ্জ মেম্বার",
                minEarnings = 0.0,
                multiplier = 1.0,
                badge = "🟫",
                icon = Icons.Default.Star,
                color = Color(0xFFCD7F32),
                benefitsEN = listOf("Standard daily task rewards (1.0x)", "Normal 24-hour withdrawals", "5% Level 1 referral commission"),
                benefitsBN = listOf("স্ট্যান্ডার্ড দৈনিক টাস্ক বোনাস (১.০x)", "২৪ ঘণ্টার মধ্যে সাধারণ উত্তোলন", "৫% লেভেল ১ রেফারেল কমিশন")
            ),
            EarningsTier(
                id = "silver",
                nameEN = "Silver Professional",
                nameBN = "সিলভার প্রফেশনাল",
                minEarnings = 500.0,
                multiplier = 1.1,
                badge = "⬜",
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFFC0C0C0),
                benefitsEN = listOf("1.1x tasks reward bonus (+10%)", "Priority withdrawal within 12h", "7% Level 1 referral commission"),
                benefitsBN = listOf("১.১x টাস্ক রিওয়ার্ড বোনাস (+১০%)", "১২ ঘণ্টার মধ্যে অগ্রাধিকার উত্তোলন", "৭% লেভেল ১ রেফারেল কমিশন")
            ),
            EarningsTier(
                id = "gold",
                nameEN = "Gold Ambassador",
                nameBN = "গোল্ড অ্যাম্বাসেডর",
                minEarnings = 2000.0,
                multiplier = 1.25,
                badge = "🟨",
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFFFFD700),
                benefitsEN = listOf("1.25x tasks reward bonus (+25%)", "Priority withdrawal within 6h", "10% Level 1 + 2% Level 2 commission"),
                benefitsBN = listOf("১.২৫x টাস্ক রিওয়ার্ড বোনাস (+২৫%)", "৬ ঘণ্টার মধ্যে অগ্রাধিকার উত্তোলন", "১০% লেভেল ১ + ২% লেভেল ২ কমিশন")
            ),
            EarningsTier(
                id = "platinum",
                nameEN = "Platinum Leader",
                nameBN = "প্ল্যাটিনাম লিডার",
                minEarnings = 5000.0,
                multiplier = 1.5,
                badge = "🟦",
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFF00E5FF),
                benefitsEN = listOf("1.5x tasks reward bonus (+50%)", "Express withdrawal within 2h", "12% Level 1 + 4% Level 2 commission"),
                benefitsBN = listOf("১.৫x টাস্ক রিওয়ার্ড বোনাস (+৫০%)", "২ ঘণ্টার মধ্যে এক্সপ্রেস উত্তোলন", "১২% লেভেল ১ + ৪% লেভেল ২ কমিশন")
            ),
            EarningsTier(
                id = "diamond",
                nameEN = "Diamond Elite Legend",
                nameBN = "ডায়মন্ড লিজেন্ড",
                minEarnings = 10000.0,
                multiplier = 2.0,
                badge = "💎",
                icon = Icons.Default.WorkspacePremium,
                color = Color(0xFFE040FB),
                benefitsEN = listOf("2.0x DOUBLE task earnings (+100%)", "Instant withdrawal within 1h", "15% Level 1 + 5% Level 2 + 2% Level 3 commission", "24/7 VIP Dedicated Account Support"),
                benefitsBN = listOf("২.০x দ্বিগুণ টাস্ক রিওয়ার্ড (+১০০%)", "১ ঘণ্টার মধ্যে ইনস্ট্যান্ট উত্তোলন", "১৫% লেভেল ১ + ৫% লেভেল ২ + ২% লেভেল ৩ কমিশন", "২৪/৭ বিশেষ ভিআইপি অ্যাকাউন্ট সাপোর্ট")
            )
        )
    }

    val currentTierIndex = remember(totalEarnings) {
        earningsTiers.indexOfLast { totalEarnings >= it.minEarnings }.coerceAtLeast(0)
    }
    val currentTier = earningsTiers[currentTierIndex]
    val nextTier = if (currentTierIndex < earningsTiers.lastIndex) earningsTiers[currentTierIndex + 1] else null

    var selectedDetailTierIndex by remember { mutableStateOf(currentTierIndex) }
    val selectedDetailTier = earningsTiers[selectedDetailTierIndex]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("earnings_tier_status_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(currentTier.color, Color(0xFF2C2A3F))))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            currentTier.color.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Header: Icon + Current Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(currentTier.color.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, currentTier.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentTier.icon,
                        contentDescription = "Tier Badge",
                        tint = currentTier.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == "BN") "আপনার বর্তমান আর্নিং টিয়ার" else "YOUR EARNINGS STATUS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (language == "BN") currentTier.nameBN else currentTier.nameEN,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentTier.badge,
                            fontSize = 16.sp
                        )
                    }
                }

                // Multiplier Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(currentTier.color.copy(alpha = 0.2f))
                        .border(0.5.dp, currentTier.color, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${currentTier.multiplier}x Rewards",
                        color = currentTier.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Total Earnings display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (language == "BN") "সর্বমোট উপার্জন" else "Total Earnings",
                        fontSize = 12.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "৳ ${String.format("%.2f", totalEarnings)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NeonGreen
                    )
                }

                if (nextTier != null) {
                    val remaining = nextTier.minEarnings - totalEarnings
                    Text(
                        text = if (language == "BN") {
                            "পরবর্তী টিয়ার: ৳${String.format("%.0f", remaining)} প্রয়োজন"
                        } else {
                            "৳${String.format("%.0f", remaining)} more to next level"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.LightGray
                    )
                } else {
                    Text(
                        text = if (language == "BN") "আপনি সর্বোচ্চ স্তরে আছেন! 👑" else "Max Level Reached! 👑",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            if (nextTier != null) {
                val progressFraction = ((totalEarnings - currentTier.minEarnings) / (nextTier.minEarnings - currentTier.minEarnings))
                    .coerceIn(0.0, 1.0).toFloat()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1C2C))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(currentTier.color, nextTier.color)
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "৳ ${String.format("%.0f", currentTier.minEarnings)}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "৳ ${String.format("%.0f", nextTier.minEarnings)}",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Full progress at max level
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF), Color(0xFFE040FB))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Divider(color = Color.DarkGray.copy(alpha = 0.5f), thickness = 1.dp)

            Spacer(modifier = Modifier.height(14.dp))

            // Title for Explorer Tab
            Text(
                text = if (language == "BN") "👑 টিয়ার এবং সুবিধাসমূহ পর্যালোচনা" else "👑 Explore Tiers & Benefits",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Tier Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                earningsTiers.forEachIndexed { index, tier ->
                    val isSelected = index == selectedDetailTierIndex
                    val isUnlocked = totalEarnings >= tier.minEarnings
                    val isActive = index == currentTierIndex

                    Box(
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) tier.color.copy(alpha = 0.2f)
                                else Color(0xFF1B192A)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) tier.color else if (isActive) GoldAccent.copy(alpha = 0.5f) else Color(0xFF2C2A3F),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedDetailTierIndex = index }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = tier.badge,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == "BN") tier.nameBN.split(" ")[0] else tier.nameEN.split(" ")[0],
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                            )
                            if (isUnlocked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Unlocked",
                                    tint = NeonGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selected Tier Details Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B192A)),
                border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header inside details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == "BN") selectedDetailTier.nameBN else selectedDetailTier.nameEN,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = selectedDetailTier.color
                            )
                            Text(
                                text = if (selectedDetailTier.minEarnings == 0.0) {
                                    if (language == "BN") "প্রারম্ভিক স্তর" else "Starting level"
                                } else {
                                    if (language == "BN") "সর্বনিম্ন ইনকাম: ৳${String.format("%.0f", selectedDetailTier.minEarnings)}" else "Min earnings: ৳${String.format("%.0f", selectedDetailTier.minEarnings)}"
                                },
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Status Badge
                        val detailStatusText = when {
                            selectedDetailTierIndex == currentTierIndex -> if (language == "BN") "চলমান স্তর" else "Active Current"
                            totalEarnings >= selectedDetailTier.minEarnings -> if (language == "BN") "আনলকড" else "Unlocked"
                            else -> if (language == "BN") "লকড" else "Locked"
                        }
                        val detailStatusBg = when {
                            selectedDetailTierIndex == currentTierIndex -> GoldAccent.copy(alpha = 0.15f)
                            totalEarnings >= selectedDetailTier.minEarnings -> NeonGreen.copy(alpha = 0.15f)
                            else -> Color.DarkGray.copy(alpha = 0.15f)
                        }
                        val detailStatusColor = when {
                            selectedDetailTierIndex == currentTierIndex -> GoldAccent
                            totalEarnings >= selectedDetailTier.minEarnings -> NeonGreen
                            else -> Color.LightGray
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(detailStatusBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = detailStatusText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = detailStatusColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Divider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 0.5.dp)

                    Spacer(modifier = Modifier.height(10.dp))

                    // Benefits List
                    val benefitsList = if (language == "BN") selectedDetailTier.benefitsBN else selectedDetailTier.benefitsEN
                    benefitsList.forEach { benefit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (totalEarnings >= selectedDetailTier.minEarnings) Icons.Default.Check else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (totalEarnings >= selectedDetailTier.minEarnings) selectedDetailTier.color else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = benefit,
                                color = if (totalEarnings >= selectedDetailTier.minEarnings) Color.LightGray else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
