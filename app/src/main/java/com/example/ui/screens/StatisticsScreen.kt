package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.flow.map

@Composable
fun StatisticsScreen(
    viewModel: FreedomViewModel,
    onBackClick: () -> Unit
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()

    var selectedFilterTab by remember { mutableStateOf("Weekly") } // Weekly, Monthly

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
                    text = FreedomLocales.get("statistics", language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Subtabs: Weekly | Monthly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(CardSurface, RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                listOf("Weekly", "Monthly").forEach { tab ->
                    val isSel = selectedFilterTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) PurplePrimary else Color.Transparent)
                            .clickable { selectedFilterTab = tab }
                            .testTag("stats_tab_$tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tab == "Weekly") (if (language == "BN") "সাপ্তাহিক" else "Weekly") else (if (language == "BN") "মাসিক" else "Monthly"),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stat Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = if (language == "BN") "মোট প্রফিট" else "Total Earnings",
                    value = "৳ ${String.format("%.0f", user?.totalIncome ?: 0.0)}",
                    color = NeonGreen,
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = if (language == "BN") "কাজের সাফল্যের হার" else "Task Win Rate",
                    value = "৯৬.৮%",
                    color = GoldAccent,
                    icon = Icons.Default.CheckCircleOutline,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Weekly Earning Line Chart on Compose Canvas
            WeeklyEarningLineChart(language = language)

            Spacer(modifier = Modifier.height(24.dp))

            // Income Breakdown Pie Chart on Compose Canvas
            IncomeBreakdownPieChart(language = language)

            Spacer(modifier = Modifier.height(24.dp))

            // Extra Reports Summary card
            ReportsSummaryCard(language = language)

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, color = Color.Gray, fontSize = 11.sp)
            Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WeeklyEarningLineChart(language: String) {
    val weeklyAmounts = listOf(40f, 80f, 60f, 110f, 90f, 150f, 130f)
    val daysBN = listOf("সোম", "মঙ্গল", "বুধ", "বৃহ", "শুক্র", "শনি", "রবি")
    val daysEN = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val days = if (language == "BN") daysBN else daysEN

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (language == "BN") "📈 আয় বিশ্লেষণ (সাপ্তাহিক)" else "📈 Earning Analytics (Weekly)",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Canvas drawing line graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointCount = weeklyAmounts.size
                val stepX = width / (pointCount - 1)

                val maxAmt = 200f
                val points = weeklyAmounts.mapIndexed { index, amt ->
                    val x = index * stepX
                    // invert Y since top-left is 0,0
                    val y = height - (amt / maxAmt) * height
                    Offset(x, y)
                }

                // Draw background gradient under curve
                val gradientPath = Path().apply {
                    moveTo(0f, height)
                    points.forEach { moveToPoint ->
                        lineTo(moveToPoint.x, moveToPoint.y)
                    }
                    lineTo(width, height)
                    close()
                }

                drawPath(
                    path = gradientPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryLight.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                // Draw curve path line
                val linePath = Path().apply {
                    points.forEachIndexed { index, offset ->
                        if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = PrimaryLight,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw data points nodes
                points.forEach { pt ->
                    drawCircle(
                        color = Color.Black,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = GoldAccent,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEach { day ->
                    Text(text = day, color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun IncomeBreakdownPieChart(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (language == "BN") "🍰 আয়ের উৎস বিশ্লেষণ" else "🍰 Income Sources Breakdown",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pie Canvas Left
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .weight(1.2f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val sweeps = listOf(144f, 108f, 72f, 36f) // 40%, 30%, 20%, 10%
                        val colors = listOf(PrimaryLight, ElectricBlue, GoldAccent, Color(0xFFFF4081))

                        var startAngle = 0f
                        sweeps.forEachIndexed { idx, sweep ->
                            drawArc(
                                color = colors[idx],
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                            startAngle += sweep
                        }

                        // Inner donut hole
                        drawCircle(
                            color = CardSurface,
                            radius = size.minDimension / 4f
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Legends Right
                Column(modifier = Modifier.weight(1.8f)) {
                    LegendItem(color = PrimaryLight, label = if (language == "BN") "টাস্ক সেন্টার (৪০%)" else "Task Center (40%)")
                    LegendItem(color = ElectricBlue, label = if (language == "BN") "টিম রেফারেল (৩০%)" else "Team Referrals (30%)")
                    LegendItem(color = GoldAccent, label = if (language == "BN") "দৈনিক বোনাস (২০%)" else "Daily Bonus (20%)")
                    LegendItem(color = Color(0xFFFF4081), label = if (language == "BN") "লাকি স্পিন (১০%)" else "Lucky Wheel (10%)")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun ReportsSummaryCard(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = if (language == "BN") "📋 পরিসংখ্যান রিপোর্ট" else "📋 Statistical Report Summary",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (language == "BN") "সম্পন্ন কাজের সংখ্যা" else "Completed Tasks Logs", color = Color.Gray, fontSize = 11.sp)
                Text(text = "১৪৫ টি কাজ", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Divider(color = Color(0xFF1E1C31), modifier = Modifier.padding(vertical = 6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (language == "BN") "রেফারেল কমিশন প্রাপ্ত" else "Referral Commission Earned", color = Color.Gray, fontSize = 11.sp)
                Text(text = "৳ ৪,৫২০.০০", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Divider(color = Color(0xFF1E1C31), modifier = Modifier.padding(vertical = 6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = if (language == "BN") "মোট বোনাস ক্লেইম" else "Total Bonus Cash Claimed", color = Color.Gray, fontSize = 11.sp)
                Text(text = "৳ ১,২০০.০০", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
