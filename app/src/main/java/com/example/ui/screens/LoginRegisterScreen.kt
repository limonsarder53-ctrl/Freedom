package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.flow.map
import com.example.ui.theme.*
import com.example.ui.viewmodel.FreedomViewModel

@Composable
fun LoginRegisterScreen(
    viewModel: FreedomViewModel,
    initialTabIsRegister: Boolean = false,
    onBackClick: () -> Unit
) {
    val language by viewModel.activeUser.map { it?.language ?: "BN" }.collectAsState(initial = "BN")
    val authError by viewModel.authError.collectAsState()

    var isRegisterTab by remember { mutableStateOf(initialTabIsRegister) }

    // Form inputs
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Dialog state
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

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
                .padding(24.dp)
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
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = if (isRegisterTab) FreedomLocales.get("register_tab", language) else FreedomLocales.get("login_tab", language),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // App Logo Icon
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(PurplePrimary, GoldAccent)))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(CardSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(36.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Freedom Platform",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Tab toggles: Login | Register
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(CardSurface, RoundedCornerShape(25.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (!isRegisterTab) PurplePrimary else Color.Transparent)
                        .clickable { isRegisterTab = false }
                        .testTag("login_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = FreedomLocales.get("login_tab", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(21.dp))
                        .background(if (isRegisterTab) PurplePrimary else Color.Transparent)
                        .clickable { isRegisterTab = true }
                        .testTag("register_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = FreedomLocales.get("register_tab", language),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Auth error display
            if (authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FF3D00)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Red)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authError ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input Fields
            if (isRegisterTab) {
                // Register fields
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text(FreedomLocales.get("full_name", language)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_fullname"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(FreedomLocales.get("phone_num", language)) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GoldAccent) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_phone"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(FreedomLocales.get("password", language)) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_password"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.LightGray,
                    focusedBorderColor = PurplePrimary,
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (isRegisterTab) {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(FreedomLocales.get("confirm_password", language)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_confirm_pass"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = referralCode,
                    onValueChange = { referralCode = it },
                    label = { Text(FreedomLocales.get("referral_code_opt", language)) },
                    leadingIcon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldAccent) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_referral"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.LightGray,
                        focusedBorderColor = PurplePrimary,
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
            } else {
                // Forgot password trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = FreedomLocales.get("forgot_password", language),
                        color = GoldAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable { showForgotPasswordDialog = true }
                            .padding(vertical = 4.dp)
                            .testTag("forgot_pass_btn")
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Submit CTA Button
            Button(
                onClick = {
                    if (isRegisterTab) {
                        viewModel.register(fullName, phone, referralCode, password, confirmPassword)
                    } else {
                        viewModel.login(phone, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("auth_submit_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurplePrimary,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (isRegisterTab) FreedomLocales.get("register_btn", language) else FreedomLocales.get("login_btn", language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Social Logins (Google / Facebook)
            Text(
                text = FreedomLocales.get("social_login", language),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Google Sim
                OutlinedButton(
                    onClick = {
                        // Simulated sign in
                        viewModel.loginAsGuest()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("google_login_sim"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Google", color = Color.White, fontSize = 14.sp)
                    }
                }

                // Facebook Sim
                OutlinedButton(
                    onClick = {
                        // Simulated sign in
                        viewModel.loginAsGuest()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("fb_login_sim"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Facebook, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Facebook", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Forgot Password Simulation Modal
    if (showForgotPasswordDialog) {
        Dialog(
            onDismissRequest = { showForgotPasswordDialog = false }
        ) {
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
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (language == "BN") "পাসওয়ার্ড উদ্ধার" else "Password Recovery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == "BN") "আপনার অ্যাকাউন্টটি নিবন্ধনের সময় দেওয়া মোবাইল নম্বরে একটি ওটিপি (OTP) পাঠানো হবে।" else "An OTP (One-Time Password) will be sent to the phone number you registered with.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    var recoverPhone by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = recoverPhone,
                        onValueChange = { recoverPhone = it },
                        label = { Text(FreedomLocales.get("phone_num", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = PurplePrimary,
                            unfocusedBorderColor = Color.DarkGray
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { showForgotPasswordDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (language == "BN") "বাতিল" else "Cancel", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                showForgotPasswordDialog = false
                                viewModel.showToast(if (language == "BN") "ওটিপি কোড সফলভাবে পাঠানো হয়েছে!" else "OTP Sent successfully!")
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                        ) {
                            Text(text = if (language == "BN") "কোড পাঠান" else "Send Code", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
