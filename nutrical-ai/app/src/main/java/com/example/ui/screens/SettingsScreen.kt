package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NutriBottomBar
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.WaterBackground
import com.example.ui.theme.WaterPrimary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NutriViewModel

@Composable
fun SettingsScreen(viewModel: NutriViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showWaterGoalDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    val currentWaterGoal = profile?.dailyWaterGoalMl ?: 2500

    Scaffold(
        bottomBar = {
            NutriBottomBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Orqaga",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Sozlamalar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ilova, suv me'yori va bildirishnomalarni boshqarish",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Suv me'yori (Water Goal)
            SettingsSectionTitle(title = "Suv va Salomatlik")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItemClickable(
                        icon = Icons.Default.LocalDrink,
                        iconTint = WaterPrimary,
                        iconBg = WaterBackground,
                        title = "Kunlik suv me'yori",
                        value = "${String.format("%.1f", currentWaterGoal / 1000f)} litr (${currentWaterGoal} ml)",
                        onClick = { showWaterGoalDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItemToggle(
                        icon = Icons.Default.Notifications,
                        iconTint = EmeraldDark,
                        iconBg = EmeraldContainer,
                        title = "Suv ichish eslatmalari",
                        subtitle = "Kun davomida suv ichishni eslatib turish",
                        checked = profile?.waterReminderEnabled ?: true,
                        onCheckedChange = { viewModel.toggleWaterReminder(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Tashqi ko'rinish va Ilova
            SettingsSectionTitle(title = "Ilova interfeysi")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItemToggle(
                        icon = Icons.Default.DarkMode,
                        iconTint = Color(0xFF6366F1),
                        iconBg = Color(0xFFEEF2FF),
                        title = "Tungi mavzu (Dark Mode)",
                        subtitle = if (isDarkMode) "Qorong'i rejim yoqilgan" else "Yorug' va yoqimli oq rejim",
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItemClickable(
                        icon = Icons.Default.Straighten,
                        iconTint = Color(0xFFF59E0B),
                        iconBg = Color(0xFFFEF3C7),
                        title = "O'lchov birliklari",
                        value = "Metrik (kg, sm, kkal, ml)",
                        onClick = { }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Shaxsiy ko'rsatkichlar
            SettingsSectionTitle(title = "Hisob va Reja")

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItemClickable(
                        icon = Icons.Default.FitnessCenter,
                        iconTint = EmeraldDark,
                        iconBg = EmeraldContainer,
                        title = "Maqsad va parametrlarni yangilash",
                        value = "${profile?.targetGoal ?: "Ozish"} • ${profile?.weightKg?.toInt() ?: 75} kg",
                        onClick = {
                            viewModel.obStep.value = 0
                            viewModel.navigateTo(AppScreen.ONBOARDING)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItemClickable(
                        icon = Icons.Default.DeleteSweep,
                        iconTint = Color(0xFFEF4444),
                        iconBg = Color(0xFFFEE2E2),
                        title = "Bugungi va o'tgan ma'lumotlarni tozalash",
                        value = "Tarixni o'chirish",
                        onClick = { showClearDataDialog = true }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingItemClickable(
                        icon = Icons.Default.RestartAlt,
                        iconTint = Color(0xFFEF4444),
                        iconBg = Color(0xFFFEE2E2),
                        title = "Hisobdan chiqish / Qayta boshlash",
                        value = "Qayta kirish",
                        onClick = { viewModel.resetApp() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Ilova haqida
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NutriCal AI v1.2",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Google Gemini 3.5 Flash neyrotarmog'i orqali quvvatlanadi. Taomlarni sun'iy intellekt yordamida tezkor aniqlash va O'zbek tilidagi qulay hisoblagich.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Water Goal Selection Dialog
    if (showWaterGoalDialog) {
        val waterPresets = listOf(1500, 2000, 2500, 3000, 3500)
        AlertDialog(
            onDismissRequest = { showWaterGoalDialog = false },
            title = {
                Text(text = "Kunlik suv me'yorini tanlang", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    waterPresets.forEach { ml ->
                        val isSelected = currentWaterGoal == ml
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) WaterBackground else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    viewModel.setDailyWaterGoal(ml)
                                    showWaterGoalDialog = false
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${String.format("%.1f", ml / 1000f)} litr (${ml} ml)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WaterPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Tanlangan", tint = WaterPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWaterGoalDialog = false }) {
                    Text("Yopish")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = {
                Text(text = "Ma'lumotlarni tozalash", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(text = "Barcha kiritilgan taomlar va suv tarixi o'chiriladi. Ushbu amalni ortga qaytarib bo'lmaydi. Davom etasizmi?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Tozalash", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Bekor qilish")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingItemClickable(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SettingItemToggle(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = EmeraldPrimary
            )
        )
    }
}
