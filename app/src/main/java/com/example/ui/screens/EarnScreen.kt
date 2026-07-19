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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.Task
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

@Composable
fun EarnScreen(
    viewModel: FreedomViewModel
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val user by viewModel.activeUser.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOrder by remember { mutableStateOf("Default") } // Default, HighToLow, EasyToHard

    // Active dialog task state
    var activeDialogTask by remember { mutableStateOf<Task?>(null) }

    // Categories list
    val categories = listOf("All", "Daily", "Social", "App", "Premium", "Games")

    // Filter tasks
    val filteredTasks = tasks.filter { task ->
        val matchesSearch = task.name.contains(searchQuery, ignoreCase = true) || task.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || task.category.equals(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }.let { list ->
        when (sortOrder) {
            "HighToLow" -> list.sortedByDescending { it.reward }
            "EasyToHard" -> list.sortedBy { it.reward }
            else -> list
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
                .padding(bottom = 80.dp) // padding for bottom nav
        ) {
            // Task Header
            TaskHeaderPanel(language = language, user = user)

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Search Bar and Filter
                SearchAndFilterRow(
                    language = language,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    sortOrder = sortOrder,
                    onSortChange = { sortOrder = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Horizontal Category Tabs
                HorizontalCategoryTabs(
                    language = language,
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Tasks List
                if (filteredTasks.isEmpty()) {
                    EmptyTasksState(language = language)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                language = language,
                                onTaskClick = { clickedTask ->
                                    activeDialogTask = clickedTask
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Task details modal with dynamic countdown timer simulation
    if (activeDialogTask != null) {
        TaskDetailsDialog(
            task = activeDialogTask!!,
            language = language,
            onDismiss = { activeDialogTask = null },
            onConfirmClaim = { taskId ->
                viewModel.completeTask(taskId)
                activeDialogTask = null
            }
        )
    }
}

@Composable
fun TaskHeaderPanel(language: String, user: com.example.data.database.UserProfile?) {
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
                text = if (language == "BN") "টাস্ক সেন্টার" else "Tasks & Earn",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = if (language == "BN") "সহজ কাজ করুন, নগদ টাকা আয় করুন" else "Fulfill easy micro-tasks for instant cash",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Coins Indicator Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            border = BorderStroke(0.5.dp, Color(0xFF2C2A3F))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "৳ ${String.format("%.2f", user?.todayEarnings ?: 0.0)}",
                    color = NeonGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterRow(
    language: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    sortOrder: String,
    onSortChange: (String) -> Unit
) {
    var showFilterDropdown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(text = FreedomLocales.get("search_task", language), fontSize = 13.sp, color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp)) },
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("task_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = PurplePrimary,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Filter button
        Box {
            IconButton(
                onClick = { showFilterDropdown = !showFilterDropdown },
                modifier = Modifier
                    .size(50.dp)
                    .background(CardSurface, RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color.DarkGray, RoundedCornerShape(12.dp))
                    .testTag("task_filter_btn")
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
            }

            DropdownMenu(
                expanded = showFilterDropdown,
                onDismissRequest = { showFilterDropdown = false },
                modifier = Modifier.background(CardSurface)
            ) {
                DropdownMenuItem(
                    text = { Text(text = if (language == "BN") "ডিফল্ট সর্ট" else "Default Order", color = Color.White) },
                    onClick = {
                        onSortChange("Default")
                        showFilterDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = if (language == "BN") "বেশি আয় প্রথমে" else "High Paying First", color = Color.White) },
                    onClick = {
                        onSortChange("HighToLow")
                        showFilterDropdown = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = if (language == "BN") "সহজ কাজ প্রথমে" else "Easy Tasks First", color = Color.White) },
                    onClick = {
                        onSortChange("EasyToHard")
                        showFilterDropdown = false
                    }
                )
            }
        }
    }
}

@Composable
fun HorizontalCategoryTabs(
    language: String,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategory == category
            val label = when (category) {
                "All" -> FreedomLocales.get("all", language)
                "Daily" -> if (language == "BN") "দৈনিক টাস্ক" else "Daily"
                "Social" -> if (language == "BN") "সোশ্যাল" else "Social"
                "App" -> if (language == "BN") "অ্যাপ ইন্সটল" else "Apps"
                "Premium" -> if (language == "BN") "ভিআইপি কাজ" else "Premium"
                else -> if (language == "BN") "গেম খেলুন" else "Games"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) PurplePrimary else CardSurface)
                    .border(0.5.dp, if (isSelected) PurplePrimary else Color.DarkGray, RoundedCornerShape(20.dp))
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .testTag("category_tab_$category"),
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
}

@Composable
fun TaskCard(
    task: Task,
    language: String,
    onTaskClick: (Task) -> Unit
) {
    val cardBorderColor = if (task.isCompleted) NeonGreen else Color(0xFF2C2A3F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task) }
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        border = BorderStroke(0.5.dp, cardBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Category Gradient Icon Left
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            colors = getCategoryGradient(task.category)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getTaskIcon(task.iconName),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task info middle
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (task.category.equals("Premium", ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(GoldAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(text = "VIP", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = task.description,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = task.timeLimit, fontSize = 10.sp, color = Color.LightGray)

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "+৳ ${String.format("%.2f", task.reward)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Start/Go/Claim button right
            if (task.isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = NeonGreen,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PurplePrimary, PurpleDark)
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == "BN") "শুরু" else "Go",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TaskDetailsDialog(
    task: Task,
    language: String,
    onDismiss: () -> Unit,
    onConfirmClaim: (String) -> Unit
) {
    var countdown by remember { mutableStateOf(10) } // 10 seconds demo countdown
    var timerActive by remember { mutableStateOf(false) }
    var taskFinished by remember { mutableStateOf(false) }

    LaunchedEffect(timerActive) {
        if (timerActive) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            taskFinished = true
            timerActive = false
        }
    }

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
                // Category big icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(colors = getCategoryGradient(task.category))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getTaskIcon(task.iconName),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = task.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Instruction panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = FreedomLocales.get("task_details", language),
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == "BN") {
                                "নির্দেশনা: কাজ শুরু করতে নিচের বাটনে চাপুন। একটি বিজ্ঞাপন বা লিঙ্ক খুলবে। আপনাকে সম্পূর্ণ ${task.timeLimit} অপেক্ষা করতে হবে। পুরো সময় শেষ হওয়ার পূর্বে ফিরে গেলে কোনো ব্যালেন্স পাবেন না।"
                            } else {
                                "Instructions: Tap the button below to start. A partner page will load. You must wait the full ${task.timeLimit}. Closing the task early forfeits all rewards."
                            },
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (language == "BN") "পুরষ্কার:" else "Reward:", color = Color.Gray, fontSize = 12.sp)
                    Text(text = "৳ ${String.format("%.2f", task.reward)}", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive timer countdown button trigger
                if (!timerActive && !taskFinished) {
                    Button(
                        onClick = { timerActive = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_start_task_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text(text = if (language == "BN") "কাজটি শুরু করুন" else "Go to Task", color = Color.White)
                    }
                } else if (timerActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldAccent)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == "BN") "দয়া করে অপেক্ষা করুন... $countdown সেকেন্ড" else "Please wait... $countdown sec",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (taskFinished) {
                    Button(
                        onClick = { onConfirmClaim(task.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("dialog_claim_reward_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text(text = FreedomLocales.get("claim", language) + " ৳${task.reward}", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(text = if (language == "BN") "বাতিল" else "Close", color = Color.Gray)
                }
            }
        }
    }
}

fun getCategoryGradient(category: String): List<Color> {
    return when (category) {
        "Daily" -> listOf(Color(0xFFE52D27), Color(0xFFB31217))
        "Social" -> listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
        "App" -> listOf(Color(0xFF00B4DB), Color(0xFF0083B0))
        "Premium" -> listOf(GoldAccent, GoldDark)
        "Games" -> listOf(Color(0xFFFF8C00), Color(0xFFFF4500))
        else -> listOf(Color(0xFF5C258D), Color(0xFF4389A2))
    }
}

fun getTaskIcon(iconName: String): ImageVector {
    return when (iconName) {
        "video" -> Icons.Default.PlayCircleFilled
        "ads" -> Icons.Default.Tv
        "news" -> Icons.Default.Article
        "link" -> Icons.Default.Link
        "website" -> Icons.Default.Web
        "fb" -> Icons.Default.Facebook
        "ig" -> Icons.Default.CameraAlt
        "yt" -> Icons.Default.Slideshow
        "tg" -> Icons.Default.Forum
        "tt" -> Icons.Default.VideoLibrary
        "app" -> Icons.Default.InstallMobile
        "game" -> Icons.Default.SportsEsports
        "premium" -> Icons.Default.CardGiftcard
        else -> Icons.Default.ArrowForward
    }
}

@Composable
fun EmptyTasksState(language: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SentimentVeryDissatisfied,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (language == "BN") "দুঃখিত! কোনো টাস্ক পাওয়া যায়নি।" else "No tasks found in this category.",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (language == "BN") "পরে আবার চেষ্টা করুন" else "Please check back later",
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}
