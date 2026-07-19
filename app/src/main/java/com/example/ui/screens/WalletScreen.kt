package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.TransactionLog
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

@Composable
fun WalletScreen(
    viewModel: FreedomViewModel
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var activeTab by remember { mutableStateOf("Deposit") } // Deposit, Withdraw, History

    // Deposit State
    var depositGateway by remember { mutableStateOf("bKash") }
    var depositAmount by remember { mutableStateOf("") }
    var depositTransId by remember { mutableStateOf("") }

    // Withdraw State
    var withdrawGateway by remember { mutableStateOf("bKash") }
    var withdrawPhone by remember { mutableStateOf("") }
    var withdrawAmount by remember { mutableStateOf("") }

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
                .padding(bottom = 80.dp) // padding for bottom nav
        ) {
            // Header
            WalletHeader(language = language)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Wallet Balance Card
                WalletBalanceDisplay(user = user, language = language)

                Spacer(modifier = Modifier.height(18.dp))

                // Tab selectors: Deposit | Withdraw | History
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(CardSurface, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    listOf("Deposit", "Withdraw", "History").forEach { tab ->
                        val isSel = activeTab == tab
                        val label = when (tab) {
                            "Deposit" -> if (language == "BN") "ডিপোজিট" else "Deposit"
                            "Withdraw" -> if (language == "BN") "উইথড্র" else "Withdraw"
                            else -> if (language == "BN") "ইতিহাস" else "History"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) PurplePrimary else Color.Transparent)
                                .clickable { activeTab = tab }
                                .testTag("wallet_tab_$tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tab Content
                when (activeTab) {
                    "Deposit" -> {
                        DepositSection(
                            language = language,
                            gateway = depositGateway,
                            onGatewaySelect = { depositGateway = it },
                            amount = depositAmount,
                            onAmountChange = { depositAmount = it },
                            transId = depositTransId,
                            onTransIdChange = { depositTransId = it },
                            onCopyClick = { num ->
                                clipboardManager.setText(AnnotatedString(num))
                                viewModel.showToast(
                                    if (language == "BN") "নম্বর কপি হয়েছে!" else "Number copied!"
                                )
                            },
                            onSubmitClick = {
                                val amt = depositAmount.toDoubleOrNull() ?: 0.0
                                if (amt < 200.0) {
                                    viewModel.showToast(
                                        if (language == "BN") "সর্বনিম্ন ডিপোজিট ২০০ টাকা!" else "Minimum deposit is 200 BDT!"
                                    )
                                } else if (depositTransId.trim().length < 8) {
                                    viewModel.showToast(
                                        if (language == "BN") "সঠিক ট্রানজেকশন আইডি দিন!" else "Provide a valid Transaction ID!"
                                    )
                                } else {
                                    val adminNumber = when (depositGateway) {
                                        "bKash" -> "01783452910"
                                        "Nagad" -> "01944502830"
                                        else -> "01522819030"
                                    }
                                    viewModel.deposit(depositGateway, adminNumber, amt, depositTransId)
                                    depositAmount = ""
                                    depositTransId = ""
                                    activeTab = "History"
                                }
                            }
                        )
                    }

                    "Withdraw" -> {
                        WithdrawSection(
                            language = language,
                            gateway = withdrawGateway,
                            onGatewaySelect = { withdrawGateway = it },
                            phone = withdrawPhone,
                            onPhoneChange = { withdrawPhone = it },
                            amount = withdrawAmount,
                            onAmountChange = { withdrawAmount = it },
                            onSubmitClick = {
                                val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                                val curBalance = user?.balance ?: 0.0

                                if (amt < 100.0) {
                                    viewModel.showToast(
                                        if (language == "BN") "সর্বনিম্ন উইথড্র ১০০ টাকা!" else "Minimum withdrawal is 100 BDT!"
                                    )
                                } else if (amt > curBalance) {
                                    viewModel.showToast(
                                        if (language == "BN") "অপর্যাপ্ত ব্যালেন্স!" else "Insufficient BDT Balance!"
                                    )
                                } else if (withdrawPhone.trim().length < 11) {
                                    viewModel.showToast(
                                        if (language == "BN") "সঠিক ১১ ডিজিটের ফোন নম্বর দিন!" else "Provide a valid 11-digit phone number!"
                                    )
                                } else {
                                    viewModel.withdraw(withdrawGateway, withdrawPhone, amt)
                                    withdrawAmount = ""
                                    withdrawPhone = ""
                                    activeTab = "History"
                                }
                            }
                        )
                    }

                    "History" -> {
                        HistorySection(
                            transactions = transactions,
                            language = language
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WalletHeader(language: String) {
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
                text = if (language == "BN") "নগদ ওয়ালেট" else "Secure Wallet",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (language == "BN") "নিরাপদে রিচার্জ ও উইথড্র করুন" else "Secure cash deposit and quick withdrawal gateways",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(NeonGreen.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = NeonGreen)
        }
    }
}

@Composable
fun WalletBalanceDisplay(user: com.example.data.database.UserProfile?, language: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = FreedomLocales.get("total_balance", language).uppercase(),
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "৳ ${String.format("%.2f", user?.balance ?: 0.0)}",
                color = GoldAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = if (language == "BN") "ডিপোজিট ব্যালেন্স" else "Deposit Funds", color = Color.Gray, fontSize = 10.sp)
                    Text(text = "৳ ${String.format("%.2f", (user?.balance ?: 0.0) * 0.4)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Divider(modifier = Modifier.width(1.dp).height(30.dp).background(Color.DarkGray))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = if (language == "BN") "উপার্জিত ব্যালেন্স" else "Earning Funds", color = Color.Gray, fontSize = 10.sp)
                    Text(text = "৳ ${String.format("%.2f", (user?.balance ?: 0.0) * 0.6)}", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DepositSection(
    language: String,
    gateway: String,
    onGatewaySelect: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    transId: String,
    onTransIdChange: (String) -> Unit,
    onCopyClick: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    val adminNumber = when (gateway) {
        "bKash" -> "01783452910"
        "Nagad" -> "01944502830"
        else -> "01522819030"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Gateway selector
        Text(text = if (language == "BN") "পেমেন্ট মাধ্যম বেছে নিন:" else "Select Recharge Gateway:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("bKash", "Nagad", "Rocket").forEach { gt ->
                val isSel = gateway == gt
                val gtColor = when (gt) {
                    "bKash" -> Color(0xFFD12053)
                    "Nagad" -> Color(0xFFF37021)
                    else -> Color(0xFF8C3494)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) gtColor else CardSurface)
                        .border(0.5.dp, if (isSel) gtColor else Color.DarkGray, RoundedCornerShape(8.dp))
                        .clickable { onGatewaySelect(gt) }
                        .testTag("dep_gateway_$gt"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = gt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Copy Admin Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = "১. প্রথমে নিচে দেওয়া নম্বরে টাকা পার্সোনাল সেন্ডমানি (Send Money) করুন।", color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "$gateway: $adminNumber", color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { onCopyClick(adminNumber) }, modifier = Modifier.size(24.dp).testTag("copy_admin_num")) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "২. পেমেন্ট সম্পন্ন করে নিচের বক্সে টাকার পরিমাণ ও ট্রানজেকশন আইডি দিন।", color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Amount Field
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text(FreedomLocales.get("amount", language)) },
            leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dep_amount_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Trans ID
        OutlinedTextField(
            value = transId,
            onValueChange = onTransIdChange,
            label = { Text(FreedomLocales.get("trans_id", language)) },
            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = GoldAccent) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dep_trans_id_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color.DarkGray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSubmitClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("dep_submit_btn")
        ) {
            Text(
                text = FreedomLocales.get("submit", language),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "⚠️ " + FreedomLocales.get("deposit_msg", language),
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun WithdrawSection(
    language: String,
    gateway: String,
    onGatewaySelect: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    onSubmitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // Gateway selector
        Text(text = if (language == "BN") "উত্তোলন মাধ্যম বেছে নিন:" else "Select Withdrawal Method:", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("bKash", "Nagad", "Rocket").forEach { gt ->
                val isSel = gateway == gt
                val gtColor = when (gt) {
                    "bKash" -> Color(0xFFD12053)
                    "Nagad" -> Color(0xFFF37021)
                    else -> Color(0xFF8C3494)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) gtColor else CardSurface)
                        .border(0.5.dp, if (isSel) gtColor else Color.DarkGray, RoundedCornerShape(8.dp))
                        .clickable { onGatewaySelect(gt) }
                        .testTag("withdraw_gateway_$gt"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = gt, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phone input
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text(FreedomLocales.get("withdraw_num", language)) },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("withdraw_phone_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text(FreedomLocales.get("amount", language)) },
            leadingIcon = { Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldAccent) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("withdraw_amount_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSubmitClick,
            colors = ButtonDefaults.buttonColors(containerColor = NeonRed),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("withdraw_submit_btn")
        ) {
            Text(
                text = FreedomLocales.get("withdraw", language),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "⚠️ " + FreedomLocales.get("withdraw_limit", language),
            color = Color.Gray,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HistorySection(
    transactions: List<TransactionLog>,
    language: String
) {
    if (transactions.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (language == "BN") "কোনো লেনদেনের তথ্য পাওয়া যায়নি।" else "No transactions logged yet.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(transactions, key = { it.id }) { log ->
                val isDeposit = log.type.equals("Deposit", ignoreCase = true)
                val statusColor = when (log.status) {
                    "Completed" -> NeonGreen
                    "Pending" -> GoldAccent
                    else -> NeonRed
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_log_item_${log.id}"),
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (isDeposit) NeonGreen.copy(alpha = 0.12f) else NeonRed.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (isDeposit) NeonGreen else NeonRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "${log.method} " + (if (isDeposit) (if (language == "BN") "ডিপোজিট" else "Deposit") else (if (language == "BN") "উইথড্র" else "Withdraw")),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val dateStr = remember(log.timestamp) {
                                    try {
                                        val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
                                        sdf.format(java.util.Date(log.timestamp))
                                    } catch (e: Exception) {
                                        ""
                                    }
                                }
                                Text(
                                    text = "${if (language == "BN") "তারিখ" else "Date"}: $dateStr",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (isDeposit) "+" else "-") + " ৳${String.format("%.1f", log.amount)}",
                                color = if (isDeposit) NeonGreen else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Box(
                                modifier = Modifier
                                    .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = log.status,
                                    color = statusColor,
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
