package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.flow.map
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BonusScreen(
    viewModel: FreedomViewModel
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()

    val scope = rememberCoroutineScope()

    var promoCodeInput by remember { mutableStateOf("") }
    var mysteryBoxOpening by remember { mutableStateOf(false) }
    var isSpinning by remember { mutableStateOf(false) }

    // Spin animation target rotation degree
    var rotationDegree by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationDegree,
        animationSpec = tween(durationMillis = 3500, easing = CubicBezierEasing(0.1f, 0.8f, 0.2f, 1.0f)),
        finishedListener = { finalRot ->
            isSpinning = false
            // calculate winning sector
            val normalizedDegrees = (360f - (finalRot % 360f)) % 360f
            val sectorCount = 6
            val sectorAngle = 360f / sectorCount
            val winningSectorIndex = ((normalizedDegrees + (sectorAngle / 2f)) % 360f / sectorAngle).toInt()

            val prizes = listOf("৳5.00", "৳10.00", "৳50.00", "৳100.00", "Mystery Box (৳20)", "৳2.00")
            val prizeStr = prizes[winningSectorIndex]

            val wonAmount = when (winningSectorIndex) {
                0 -> 5.0
                1 -> 10.0
                2 -> 50.0
                3 -> 100.0
                4 -> 20.0
                else -> 2.0
            }

            viewModel.showToast(
                if (language == "BN") "অভিনন্দন! আপনি লাকি হুইলে $prizeStr জিতেছেন!" else "Congratulations! You won $prizeStr in the Lucky Wheel!"
            )
        },
        label = "LuckyWheelRotation"
    )

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
                .padding(bottom = 80.dp) // padding for bottom nav
        ) {
            // Header
            BonusHeader(language = language, user = user)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Daily Login Card
                DailyLoginBonusCard(
                    user = user,
                    language = language,
                    onClaimClick = { viewModel.claimDailyBonus() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Check-in Calendar Grid (7 Days)
                DailyCalendarGrid(streak = user?.streak ?: 0, language = language)

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Bouncing Mystery Box Card
                MysteryBoxCard(
                    language = language,
                    isOpening = mysteryBoxOpening,
                    onOpenBox = {
                        scope.launch {
                            mysteryBoxOpening = true
                            delay(2500)
                            val prize = viewModel.spinLuckyWheel() // triggers random reward BDT
                            mysteryBoxOpening = false
                            viewModel.showToast(
                                if (language == "BN") "সবাস! মিস্ট্রি বক্স খুলে পেয়েছেন $prize" else "Hooray! Opened Mystery Box and got $prize"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Canvas-drawn Spin Lucky Wheel
                LuckyWheelSection(
                    language = language,
                    rotationAngle = animatedRotation,
                    isSpinning = isSpinning,
                    onSpinClick = {
                        if (!isSpinning) {
                            isSpinning = true
                            // spin at least 5 complete rotations + random degree
                            val targetExtraDeg = (0..359).random().toFloat()
                            rotationDegree += 360f * 5 + targetExtraDeg
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Promo and Coupon Code Input
                PromoCodeCard(
                    language = language,
                    promoCodeInput = promoCodeInput,
                    onCodeChange = { promoCodeInput = it },
                    onApplyClick = {
                        viewModel.applyPromoCode(promoCodeInput)
                        promoCodeInput = ""
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun BonusHeader(language: String, user: com.example.data.database.UserProfile?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (language == "BN") "🎁 বোনাস সেন্টার" else "🎁 Bonus Center",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = GoldAccent
            )
            Text(
                text = if (language == "BN") "বিনামূল্যে অতিরিক্ত রিওয়ার্ড ক্লেইম করুন" else "Fulfill bonus milestones for extra passive cash",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Coins balance preview
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${((user?.balance ?: 0.0) * 10).toInt()} Pts",
                    color = GoldAccent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DailyLoginBonusCard(
    user: com.example.data.database.UserProfile?,
    language: String,
    onClaimClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_bonus_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(1.dp, PurplePrimary)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(GoldAccent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (language == "BN") "আজকের লগইন বোনাস" else "Today's Login Bonus",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (language == "BN") "+৳২৫.০০ নগদ ব্যালেন্স" else "+৳25.00 cash rewards",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = onClaimClick,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("claim_login_bonus_btn")
            ) {
                Text(
                    text = FreedomLocales.get("claim", language),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DailyCalendarGrid(streak: Int, language: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (language == "BN") "📅 ৭ দিনের চেক-ইন ক্যালেন্ডার" else "📅 7-Day Check-in Calendar",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(7) { index ->
                val dayNum = index + 1
                val isClaimed = dayNum <= streak
                val isCurrent = dayNum == streak + 1

                val containerColor = when {
                    isClaimed -> PurplePrimary.copy(alpha = 0.15f)
                    isCurrent -> GoldAccent.copy(alpha = 0.2f)
                    else -> CardSurface
                }

                val borderStrokeColor = when {
                    isClaimed -> PurplePrimary
                    isCurrent -> GoldAccent
                    else -> Color(0xFF2C2A3F)
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = BorderStroke(0.5.dp, borderStrokeColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == "BN") "দিন $dayNum" else "Day $dayNum",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (isClaimed) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Claimed",
                                tint = NeonGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Text(
                            text = "+৳${5 * dayNum}",
                            color = if (isClaimed) NeonGreen else Color.LightGray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MysteryBoxCard(
    language: String,
    isOpening: Boolean,
    onOpenBox: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mystery_box_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = FreedomLocales.get("mystery_box", language),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Bouncing Box icon
            val infiniteTransition = rememberInfiniteTransition(label = "boxBounce")
            val translationY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "boxBounceY"
            )

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer(translationY = if (isOpening) 0f else translationY),
                contentAlignment = Alignment.Center
            ) {
                if (isOpening) {
                    // Open animation spinner
                    CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(80.dp))
                    Icon(
                        imageVector = Icons.Default.OfflineBolt,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AllInbox,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(76.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (language == "BN") "র্যান্ডম ক্যাশ প্রাইজ ক্লেইম করুন (৳৫ - ৳১০০)" else "Open to win random BDT prizes instantly!",
                color = Color.LightGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onOpenBox,
                enabled = !isOpening,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("open_mystery_box_btn")
            ) {
                Text(
                    text = if (isOpening) (if (language == "BN") "বক্স খোলা হচ্ছে..." else "Opening chest...") else (if (language == "BN") "ওপেন করুন 🔓" else "Open Box 🔓"),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun LuckyWheelSection(
    language: String,
    rotationAngle: Float,
    isSpinning: Boolean,
    onSpinClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = FreedomLocales.get("lucky_wheel", language),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Dynamic Custom Pie Wheel Drawn on Canvas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .rotate(rotationAngle),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val diameter = size.minDimension
                    val radius = diameter / 2f
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)

                    val prizes = listOf("৳5", "৳10", "৳50", "৳100", "Mystery", "৳2")
                    val colors = listOf(
                        PrimaryDark, GoldAccent, PrimaryLight,
                        GoldDark, Color(0xFFFF4081), Color(0xFF00E5FF)
                    )

                    val sweepAngle = 360f / 6f
                    for (i in 0 until 6) {
                        drawArc(
                            color = colors[i],
                            startAngle = i * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(diameter, diameter)
                        )
                    }

                    // Draw inner central circle
                    drawCircle(
                        color = Color.Black,
                        radius = 24.dp.toPx(),
                        center = centerOffset
                    )

                    drawCircle(
                        color = GoldAccent,
                        radius = 20.dp.toPx(),
                        center = centerOffset,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Inner circle icon / center pin text
                Text(
                    text = "🌟",
                    fontSize = 16.sp,
                    modifier = Modifier.rotate(-rotationAngle) // counter-rotate so star stays upright
                )
            }

            // Wheel indicator pointer pin at top
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Indicator",
                tint = GoldAccent,
                modifier = Modifier
                    .size(44.dp)
                    .offset(y = (-10).dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSpinClick,
                enabled = !isSpinning,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("spin_now_btn")
            ) {
                Text(
                    text = if (isSpinning) (if (language == "BN") "চাকা ঘুরছে..." else "Spinning...") else (if (language == "BN") "স্পিন করুন সোনালি চাকা!" else "Spin Now!"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun PromoCodeCard(
    language: String,
    promoCodeInput: String,
    onCodeChange: (String) -> Unit,
    onApplyClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = FreedomLocales.get("promo_code", language),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = promoCodeInput,
                    onValueChange = onCodeChange,
                    placeholder = { Text(text = "e.g. FREEDOM50", fontSize = 12.sp, color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("promo_code_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = Color.DarkGray
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApplyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("promo_apply_btn")
                ) {
                    Text(
                        text = FreedomLocales.get("apply", language),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (language == "BN") "টিপস: FREEDOM50 বা VIPPROMO ব্যবহার করে ফ্রি বোনাস পান।" else "Tips: Try code FREEDOM50 or VIPPROMO for instant free balance.",
                color = Color.Gray,
                fontSize = 10.sp
            )
        }
    }
}
