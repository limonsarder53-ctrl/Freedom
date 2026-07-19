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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

@Composable
fun SettingsScreen(
    viewModel: FreedomViewModel,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()
    val isDarkMode = user?.isDarkMode ?: true

    val scope = rememberCoroutineScope()

    var showChatDialog by remember { mutableStateOf(false) }
    var chatMessages = remember { mutableStateListOf<ChatMessage>() }

    // Populate initial chat support welcome messages
    LaunchedEffect(showChatDialog) {
        if (showChatDialog && chatMessages.isEmpty()) {
            chatMessages.add(
                ChatMessage(
                    text = if (language == "BN") "আসসালামু আলাইকুম! Freedom সাপোর্ট সেন্টারে আপনাকে স্বাগতম। আপনার ডিপোজিট, টাস্ক বা উইথড্র সংক্রান্ত কোনো সমস্যা থাকলে আমাদের বলুন।" else "Hello! Welcome to Freedom Support. Please let us know if you have any questions regarding deposits, tasks, or withdrawals.",
                    isUser = false
                )
            )
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
                    text = FreedomLocales.get("settings", language),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User Profile Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
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
                            Text(
                                text = (user?.fullName?.take(2)?.uppercase()) ?: "US",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = user?.fullName ?: "Guest User",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = user?.phone ?: "017*********",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        if (user?.isVip == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .background(GoldAccent, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "💎 ${user?.vipLevel} Member",
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Controls
            Text(text = if (language == "BN") "🛠️ কনফিগারেশন" else "🛠️ Custom Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Language switcher row
            CardSettingRow(
                icon = Icons.Default.Language,
                title = FreedomLocales.get("language", language),
                action = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = if (language == "BN") "বাংলা 🇧🇩" else "English 🇺🇸", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.toggleLanguage()
                            },
                            modifier = Modifier.size(32.dp).testTag("lang_toggle_btn")
                        ) {
                            Icon(Icons.Default.Cached, contentDescription = "Switch", tint = Color.White)
                        }
                    }
                }
            )

            // Dark Mode toggle row
            CardSettingRow(
                icon = Icons.Default.Brightness4,
                title = FreedomLocales.get("theme", language),
                action = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GoldAccent,
                            checkedTrackColor = PurplePrimary
                        ),
                        modifier = Modifier.testTag("theme_switch")
                    )
                }
            )

            // Account and Security row
            CardSettingRow(
                icon = Icons.Default.Security,
                title = FreedomLocales.get("security_settings", language),
                action = {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Support Center Section
            Text(text = if (language == "BN") "💬 সাহায্য ও সাপোর্ট" else "💬 Help & Support Center", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            // Help Chat Support Trigger
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showChatDialog = true }
                    .testTag("live_chat_support_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(NeonGreen.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = FreedomLocales.get("live_chat", language), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Online Agent available", color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Logout CTA Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = FreedomLocales.get("logout", language), fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Support Chat dialog simulation with agent responses
    if (showChatDialog) {
        var chatInput by remember { mutableStateOf("") }
        var isAgentTyping by remember { mutableStateOf(false) }

        Dialog(
            onDismissRequest = { showChatDialog = false }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepSurface),
                border = BorderStroke(1.dp, Color.DarkGray)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Chat header panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSurface)
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(NeonGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Freedom Support Agent", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = FreedomLocales.get("support_online", language), color = Color.Gray, fontSize = 9.sp)
                            }
                        }

                        IconButton(onClick = { showChatDialog = false }, modifier = Modifier.size(24.dp).testTag("close_chat_btn")) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    // Chat messages scroll list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val bubbleAlign = if (msg.isUser) Alignment.End else Alignment.Start
                            val bubbleBg = if (msg.isUser) PurplePrimary else CardSurface
                            val bubbleTextColor = Color.White

                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = bubbleAlign) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                                bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                            )
                                        )
                                        .background(bubbleBg)
                                        .padding(10.dp)
                                        .widthIn(max = 240.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = bubbleTextColor,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // typing simulator bubble
                        if (isAgentTyping) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardSurface)
                                        .padding(10.dp)
                                ) {
                                    Text(text = "Agent is typing...", color = Color.Gray, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Message input block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardSurface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text(text = FreedomLocales.get("chat_placeholder", language), fontSize = 12.sp, color = Color.Gray) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("chat_message_input"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.LightGray,
                                focusedBorderColor = PurplePrimary,
                                unfocusedBorderColor = Color.DarkGray
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (chatInput.trim().isNotEmpty()) {
                                    val userMsg = chatInput.trim()
                                    chatMessages.add(ChatMessage(text = userMsg, isUser = true))
                                    chatInput = ""

                                    // Trigger Agent simulated reply
                                    scope.launch {
                                        isAgentTyping = true
                                        delay(2000)
                                        isAgentTyping = false

                                        // contextual replies
                                        val lowercaseMsg = userMsg.lowercase()
                                        val replyText = when {
                                            lowercaseMsg.contains("deposit") || lowercaseMsg.contains("ডিপোজিট") || lowercaseMsg.contains("টাকা রিচার্জ") -> {
                                                if (language == "BN") "আপনার পাঠানো ডিপোজিট রিকোয়েস্টটি আমাদের সার্ভারে জমা হয়েছে। পরবর্তী ৫-১০ মিনিটের মধ্যে এটি অটো ভেরিফাই হয়ে ওয়ালেটে ব্যালেন্স যুক্ত হবে।" else "We have received your deposit transaction. It will be verified and credited to your wallet balance within 5-10 minutes automatically!"
                                            }
                                            lowercaseMsg.contains("withdraw") || lowercaseMsg.contains("উইথড্র") || lowercaseMsg.contains("টাকা উত্তোলন") -> {
                                                if (language == "BN") "উইথড্রয়াল রিকোয়েস্টগুলো আমাদের অ্যাকাউন্টস টিম সর্বোচ্চ ১ ঘণ্টার মধ্যে পরিশোধ করে থাকে। অনুগ্রহ করে ওয়ালেট হিস্ট্রি ট্যাব চেক করুন।" else "All withdrawal requests are cleared securely by our banking gateway within 1 hour. Please check your transaction history tab!"
                                            }
                                            lowercaseMsg.contains("task") || lowercaseMsg.contains("টাস্ক") || lowercaseMsg.contains("কাজ") -> {
                                                if (language == "BN") "টাস্ক সেন্টারে গিয়ে যেকোনো কাজে 'শুরু' বাটনে চাপুন, নির্দেশনা অনুযায়ী কাউন্টডাউন শেষ হওয়া পর্যন্ত অপেক্ষা করুন এবং পুরস্কার ক্লেইম করুন।" else "Go to the Tasks section, tap 'Start' on any card, wait for the countdown timer instructions to finish, and claim your BDT rewards!"
                                            }
                                            else -> {
                                                if (language == "BN") "ধন্যবাদ আমাদের মেসেজ করার জন্য! আপনার সমস্যাটি আমাদের টেকনিক্যাল টিমকে জানানো হয়েছে। আমরা খুব শীঘ্রই মেসেজের রিপ্লাই দেব।" else "Thank you for reaching out! Our support team has been notified. We will update you shortly."
                                            }
                                        }

                                        chatMessages.add(ChatMessage(text = replyText, isUser = false))
                                    }
                                }
                            },
                            modifier = Modifier
                                .background(PurplePrimary, CircleShape)
                                .size(36.dp)
                                .testTag("send_chat_msg_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun CardSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    action: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(PurplePrimary.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            action()
        }
    }
}
