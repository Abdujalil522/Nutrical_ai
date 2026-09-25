package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.MealLog
import com.example.ui.components.MacroProgressBar
import com.example.ui.components.NutriBottomBar
import com.example.ui.components.WaterTrackerCard
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.NutriViewModel

@Composable
fun DashboardScreen(viewModel: NutriViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val meals by viewModel.todayMeals.collectAsState()
    val waterLog by viewModel.todayWaterLog.collectAsState()

    var showManualAddDialog by remember { mutableStateOf(false) }
    var manualMealType by remember { mutableStateOf("Tushlik") }

    // Calculated daily consumption
    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.proteinG.toDouble() }.toFloat()
    val totalFat = meals.sumOf { it.fatG.toDouble() }.toFloat()
    val totalCarbs = meals.sumOf { it.carbsG.toDouble() }.toFloat()

    val targetCalories = profile?.targetCalories ?: 2000
    val targetProtein = profile?.targetProteinG ?: 140
    val targetFat = profile?.targetFatG ?: 60
    val targetCarbs = profile?.targetCarbsG ?: 220

    val remainingCalories = (targetCalories - totalCalories).coerceAtLeast(0)
    val calorieProgress = if (targetCalories > 0) (totalCalories / targetCalories.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedCalorieProgress by animateFloatAsState(targetValue = calorieProgress, label = "calorie_progress")

    val currentWaterMl = waterLog?.amountMl ?: 0
    val targetWaterMl = waterLog?.targetMl ?: (profile?.dailyWaterGoalMl ?: 2500)

    Scaffold(
        bottomBar = {
            NutriBottomBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.SCANNER) },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_scan_meal")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "AI Skaner")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Skaner", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Top user info and date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Assalomu alaykum!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = profile?.name?.ifBlank { "Foydalanuvchi" } ?: "Foydalanuvchi",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Streak Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF7ED))
                            .border(1.dp, Color(0xFFFFEDD5), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "1 kun",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Calorie Ring / Hero Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                        .testTag("card_daily_calories")
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kunlik kaloriya balansi",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = profile?.targetGoal ?: "Ozish",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Circular gauge
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(125.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { animatedCalorieProgress },
                                    modifier = Modifier.size(120.dp),
                                    color = EmeraldPrimary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 10.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$remainingCalories",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "kkal qoldi",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(18.dp))

                            // Stats breakdown
                            Column(modifier = Modifier.weight(1f)) {
                                CalorieStatRow(
                                    label = "Iste'mol qilindi",
                                    value = "$totalCalories kkal",
                                    dotColor = EmeraldPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                CalorieStatRow(
                                    label = "Kunlik me'yor",
                                    value = "$targetCalories kkal",
                                    dotColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                CalorieStatRow(
                                    label = "BMR (Asosiy sarf)",
                                    value = "${profile?.bmr ?: 1700} kkal",
                                    dotColor = ProteinColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Macros Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MacroProgressBar(
                                name = "Oqsil",
                                currentG = totalProtein,
                                targetG = targetProtein,
                                color = ProteinColor,
                                modifier = Modifier.weight(1f)
                            )
                            MacroProgressBar(
                                name = "Yog'",
                                currentG = totalFat,
                                targetG = targetFat,
                                color = FatColor,
                                modifier = Modifier.weight(1f)
                            )
                            MacroProgressBar(
                                name = "Uglevod",
                                currentG = totalCarbs,
                                targetG = targetCarbs,
                                color = CarbsColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Water Tracker Card (Necha litr suv ichayotgani)
                WaterTrackerCard(
                    currentMl = currentWaterMl,
                    targetMl = targetWaterMl,
                    onAddWater = { viewModel.addWater(it) },
                    onRemoveWater = { viewModel.removeWater(it) }
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Section Title: Kunlik taomlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bugungi taomlar tarixi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${meals.size} ta kiritilgan",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Meal Categories
            val categories = listOf(
                MealCategoryConfig("Norashta", Icons.Default.FreeBreakfast, Color(0xFFF59E0B)),
                MealCategoryConfig("Tushlik", Icons.Default.WbSunny, Color(0xFF10B981)),
                MealCategoryConfig("Kechki ovqat", Icons.Default.NightsStay, Color(0xFF6366F1)),
                MealCategoryConfig("Tamaddi", Icons.Default.Cookie, Color(0xFFEC4899))
            )

            categories.forEach { category ->
                val categoryMeals = meals.filter { it.mealType == category.name }
                val catCalories = categoryMeals.sumOf { it.calories }

                item {
                    MealCategoryCard(
                        category = category,
                        meals = categoryMeals,
                        totalCalories = catCalories,
                        onAddClick = {
                            manualMealType = category.name
                            viewModel.scanMealType.value = category.name
                            viewModel.navigateTo(AppScreen.SCANNER)
                        },
                        onQuickManualAdd = {
                            manualMealType = category.name
                            showManualAddDialog = true
                        },
                        onDeleteMeal = { viewModel.deleteMeal(it) }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showManualAddDialog) {
        ManualAddMealDialog(
            defaultMealType = manualMealType,
            onDismiss = { showManualAddDialog = false },
            onSave = { type, name, cal, p, f, c, portion ->
                viewModel.addManualMeal(type, name, cal, p, f, c, portion)
                showManualAddDialog = false
            }
        )
    }
}

data class MealCategoryConfig(
    val name: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun CalorieStatRow(
    label: String,
    value: String,
    dotColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MealCategoryCard(
    category: MealCategoryConfig,
    meals: List<MealLog>,
    totalCalories: Int,
    onAddClick: () -> Unit,
    onQuickManualAdd: () -> Unit,
    onDeleteMeal: (MealLog) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = category.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = category.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalCalories kkal",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = category.color
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onQuickManualAdd,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Qo'lda kiritish",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "AI Skaner",
                            tint = EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (meals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    meals.forEach { meal ->
                        MealItemRow(meal = meal, onDelete = { onDeleteMeal(meal) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MealItemRow(
    meal: MealLog,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!meal.imageUri.isNullOrBlank()) {
            AsyncImage(
                model = meal.imageUri,
                contentDescription = meal.foodName,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meal.foodName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (meal.portionDescription.isNotBlank()) {
                Text(
                    text = meal.portionDescription,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "O: ${meal.proteinG.toInt()}g", fontSize = 11.sp, color = ProteinColor, fontWeight = FontWeight.SemiBold)
                Text(text = "Y: ${meal.fatG.toInt()}g", fontSize = 11.sp, color = FatColor, fontWeight = FontWeight.SemiBold)
                Text(text = "U: ${meal.carbsG.toInt()}g", fontSize = 11.sp, color = CarbsColor, fontWeight = FontWeight.SemiBold)
            }
        }

        Text(
            text = "${meal.calories} kkal",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = EmeraldDark,
            modifier = Modifier.padding(end = 6.dp)
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(30.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "O'chirish",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun ManualAddMealDialog(
    defaultMealType: String,
    onDismiss: () -> Unit,
    onSave: (mealType: String, name: String, cal: Int, p: Float, f: Float, c: Float, portion: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var portion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "$defaultMealType ga taom qo'shish",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Taom nomi") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Kaloriya (kkal)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Oqsil (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Yog' (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Uglevod (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = portion,
                    onValueChange = { portion = it },
                    label = { Text("Porsiya tavsifi (ixtiyoriy)") },
                    placeholder = { Text("Masalan: 1 kosa, 250g") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calories.toIntOrNull() ?: 200
                    val p = protein.toFloatOrNull() ?: 10f
                    val f = fat.toFloatOrNull() ?: 5f
                    val c = carbs.toFloatOrNull() ?: 25f
                    val finalName = name.ifBlank { "Taom" }
                    onSave(defaultMealType, finalName, cal, p, f, c, portion)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = Color.White)
            ) {
                Text("Saqlash", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Bekor qilish")
            }
        }
    )
}
