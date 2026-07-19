package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.TeamReferral
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.flow.map

@Composable
fun ReferralScreen(
    viewModel: FreedomViewModel
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()
    val referrals by viewModel.referrals.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    var selectedListTab by remember { mutableStateOf("All") } // All, Level 1, Level 2-3
    var activeRefDetails by remember { mutableStateOf<TeamReferral?>(null) }

    val userPhone = user?.phone ?: "01700000000"
    val refLink = "https://freedom.earn.com/ref/$userPhone"

    // Filter referrals based on tabs
    val filteredRefs = referrals.filter { ref ->
        when (selectedListTab) {
            "Level 1" -> ref.level == 1
            "Level 2-3" -> ref.level >= 2
            else -> true
        }
    }

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
            ReferralHeader(language = language)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Referral Link Copy panel
                ReferralLinkCard(
                    refLink = refLink,
                    language = language,
                    onCopyClick = {
                        clipboardManager.setText(AnnotatedString(refLink))
                        viewModel.showToast(
                            if (language == "BN") "লিংক কপি হয়েছে!" else "Referral link copied!"
                        )
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Statistics Cards
                ReferralStatsGrid(
                    totalRefs = referrals.size,
                    activeRefs = referrals.count { it.status == "Active" },
                    totalEarned = referrals.sumOf { it.commissionGenerated },
                    language = language
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Commission Level Structure
                CommissionStructurePanel(language = language)

                Spacer(modifier = Modifier.height(24.dp))

                // Referrals Tab Switcher & List
                ReferralsListSection(
                    referralsList = filteredRefs,
                    selectedTab = selectedListTab,
                    onTabSelect = { selectedListTab = it },
                    language = language,
                    onRefClick = { activeRefDetails = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Milestone Progress Board
                ReferralMilestonesPanel(totalRefs = referrals.size, language = language)

                Spacer(modifier = Modifier.height(24.dp))

                // Monthly Super Contest
                SuperContestPanel(language = language)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Details popup
    if (activeRefDetails != null) {
        ReferralDetailsDialog(
            ref = activeRefDetails!!,
            language = language,
            onDismiss = { activeRefDetails = null }
        )
    }
}

@Composable
fun ReferralHeader(language: String) {
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
                text = if (language == "BN") "রেফারেল ও টিম" else "Referral & Team",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (language == "BN") "বন্ধুদের ইনভাইট করে কমিশন ইনকাম করুন" else "Invite buddies and generate multi-tier commissions",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PurplePrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Group, contentDescription = null, tint = PurplePrimary)
        }
    }
}

@Composable
fun ReferralLinkCard(refLink: String, language: String, onCopyClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("referral_link_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (language == "BN") "আপনার পার্সোনাল রেফারেল লিংক" else "Your Personal Referral Link",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black, RoundedCornerShape(10.dp))
                    .border(0.5.dp, Color.DarkGray, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = refLink,
                    color = GoldAccent,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onCopyClick,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("copy_ref_link_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal share icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (language == "BN") "সহজে শেয়ার করুন:" else "Share with:", color = Color.Gray, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ShareCircleButton(Icons.Default.Send, ElectricBlue, onCopyClick) // telegram sim
                    ShareCircleButton(Icons.Default.Forum, NeonGreen, onCopyClick) // whatsapp sim
                    ShareCircleButton(Icons.Default.Facebook, Color(0xFF1877F2), onCopyClick) // fb sim
                    ShareCircleButton(Icons.Default.Share, PurplePrimary, onCopyClick) // system share sheet sim
                }
            }
        }
    }
}

@Composable
fun ShareCircleButton(icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(color.copy(alpha = 0.15f), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun ReferralStatsGrid(totalRefs: Int, activeRefs: Int, totalEarned: Double, language: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReferralMiniStatCard(
            title = FreedomLocales.get("total_referrals", language),
            value = totalRefs.toString(),
            color = PurplePrimary,
            modifier = Modifier.weight(1f)
        )
        ReferralMiniStatCard(
            title = FreedomLocales.get("active_team", language),
            value = activeRefs.toString(),
            color = NeonGreen,
            modifier = Modifier.weight(1f)
        )
        ReferralMiniStatCard(
            title = if (language == "BN") "মোট কমিশন" else "Total Earnings",
            value = "৳ ${String.format("%.0f", totalEarned)}",
            color = GoldAccent,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ReferralMiniStatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CommissionStructurePanel(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "📊 " + FreedomLocales.get("commission_structure", language),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                CommissionCell("Level 1 (Direct)", "15%", if (language == "BN") "১০০০ টাকা টাস্কে পাবেন ১৫০ টাকা" else "Earn 15 BDT on every 100 BDT task", modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.width(1.dp).height(50.dp).background(Color.DarkGray))
                CommissionCell("Level 2 (Team)", "8%", if (language == "BN") "বন্ধুর বন্ধু টাস্ক করলে পাবেন ৮%" else "Earn 8% from sub-referrals", modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.width(1.dp).height(50.dp).background(Color.DarkGray))
                CommissionCell("Level 3 (Team)", "3%", if (language == "BN") "লেভেল ৩ গ্রাহকদের থেকে পাবেন ৩%" else "Earn 3% passive team rewards", modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CommissionCell(label: String, rate: String, desc: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = rate, color = GoldAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, color = Color.LightGray, fontSize = 8.sp, textAlign = TextAlign.Center, lineHeight = 10.sp)
    }
}

@Composable
fun ReferralsListSection(
    referralsList: List<TeamReferral>,
    selectedTab: String,
    onTabSelect: (String) -> Unit,
    language: String,
    onRefClick: (TeamReferral) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (language == "BN") "👥 আমার টিম মেম্বার লিস্ট" else "👥 My Active Referrals",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Subtabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface, RoundedCornerShape(10.dp))
                .padding(3.dp)
        ) {
            listOf("All", "Level 1", "Level 2-3").forEach { tab ->
                val isSel = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) PurplePrimary else Color.Transparent)
                        .clickable { onTabSelect(tab) }
                        .padding(vertical = 8.dp)
                        .testTag("ref_subtab_$tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (referralsList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == "BN") "এই ধাপে কোনো মেম্বার পাওয়া যায়নি।" else "No team members found in this tier.",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                referralsList.forEach { ref ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRefClick(ref) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar circle
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(PurplePrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ref.name.take(2).uppercase(),
                                        color = PurplePrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(text = ref.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Lvl ${ref.level} • Join: ${ref.joinedDate}", color = Color.Gray, fontSize = 10.sp)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "৳ ${String.format("%.0f", ref.commissionGenerated)}",
                                    color = NeonGreen,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (ref.status == "Active") NeonGreen.copy(alpha = 0.15f) else Color.DarkGray,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (ref.status == "Active") (if (language == "BN") "সক্রিয়" else "Active") else (if (language == "BN") "নিষ্ক্রিয়" else "Inactive"),
                                        color = if (ref.status == "Active") NeonGreen else Color.LightGray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralMilestonesPanel(totalRefs: Int, language: String) {
    val milestones = listOf(
        Milestone(10, 500.0, "Bronze Badge"),
        Milestone(50, 2500.0, "Silver Trophy"),
        Milestone(100, 8000.0, "Gold Trophy"),
        Milestone(500, 50000.0, "Exclusive Laptop")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "🎁 " + FreedomLocales.get("milestone_rewards", language),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            milestones.forEach { ms ->
                val prog = totalRefs.toFloat() / ms.target.toFloat()
                val progressVal = prog.coerceIn(0f, 1f)

                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${ms.target} Referrals (${ms.rewardName})",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "+৳ ${String.format("%.0f", ms.bonusAmount)}",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { progressVal },
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PurplePrimary,
                            trackColor = Color(0xFF1E1C31)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${totalRefs}/${ms.target}",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

data class Milestone(val target: Int, val bonusAmount: Double, val rewardName: String)

@Composable
fun SuperContestPanel(language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == "BN") "🏆 জুলাই মেগা রেফারেল কনটেস্ট" else "🏆 July Mega Referrer Contest",
                    fontWeight = FontWeight.Bold,
                    color = GoldAccent,
                    fontSize = 13.sp
                )

                Box(
                    modifier = Modifier
                        .background(Color(0xFF2A1C54), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "১১ দিন বাকি", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (language == "BN") "টপ রেফারারদের জন্য রয়েছে সর্বমোট ৳ ১,০০,০০০ টাকার প্রাইজ পুল!" else "Exciting 100,000 BDT cash prize pool for our top referrers!",
                fontSize = 11.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Stars, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "BN") "আপনার কনটেস্ট র্যাঙ্ক: #৭" else "Your Current Contest Rank: #7",
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReferralDetailsDialog(ref: TeamReferral, language: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepSurface),
            border = BorderStroke(1.dp, Color.DarkGray)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(PurplePrimary.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = ref.name.take(2).uppercase(), color = PurplePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = ref.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Phone: ${ref.phone.take(4)}***${ref.phone.takeLast(4)}", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color.DarkGray)

                Spacer(modifier = Modifier.height(12.dp))

                RefDetailRow("Joined Date", ref.joinedDate)
                RefDetailRow("Team Level", "Level ${ref.level}")
                RefDetailRow("User Status", ref.status, if (ref.status == "Active") NeonGreen else Color.LightGray)
                RefDetailRow("Commission Earned", "৳ ${ref.commissionGenerated}", NeonGreen)

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text(text = "OK", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun RefDetailRow(label: String, value: String, color: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Text(text = value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
