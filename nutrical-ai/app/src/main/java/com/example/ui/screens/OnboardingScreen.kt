package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Male
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.viewmodel.NutriViewModel
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(
    viewModel: NutriViewModel,
    onFinish: () -> Unit
) {
    val step by viewModel.obStep.collectAsState()
    val gender by viewModel.obGender.collectAsState()
    val age by viewModel.obAge.collectAsState()
    val weightKg by viewModel.obWeightKg.collectAsState()
    val heightCm by viewModel.obHeightCm.collectAsState()
    val goal by viewModel.obGoal.collectAsState()
    val targetWeightKg by viewModel.obTargetWeightKg.collectAsState()
    val activity by viewModel.obActivity.collectAsState()

    BackHandler {
        if (step > 0) {
            viewModel.prevOnboardingStep()
        } else {
            viewModel.navigateBack()
        }
    }

    val totalSteps = 6
    val progress = (step + 1) / totalSteps.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Header with back and progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 0) {
                    IconButton(
                        onClick = { viewModel.prevOnboardingStep() },
                        modifier = Modifier.testTag("btn_ob_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Orqaga",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )

                Text(
                    text = "${step + 1}/$totalSteps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Content Animated
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                modifier = Modifier.weight(1f),
                label = "onboarding_step"
            ) { currentStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {
                        0 -> StepGender(selected = gender, onSelect = { viewModel.obGender.value = it })
                        1 -> StepAge(age = age, onAgeChange = { viewModel.obAge.value = it })
                        2 -> StepMetrics(
                            heightCm = heightCm,
                            weightKg = weightKg,
                            onHeightChange = { viewModel.obHeightCm.value = it },
                            onWeightChange = { viewModel.obWeightKg.value = it }
                        )
                        3 -> StepGoal(selected = goal, onSelect = { viewModel.obGoal.value = it })
                        4 -> StepTargetWeight(
                            currentWeightKg = weightKg,
                            targetWeightKg = targetWeightKg,
                            onTargetChange = { viewModel.obTargetWeightKg.value = it }
                        )
                        5 -> StepActivity(selected = activity, onSelect = { viewModel.obActivity.value = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Continue Button
            Button(
                onClick = {
                    viewModel.nextOnboardingStep()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("btn_ob_continue")
            ) {
                Text(
                    text = if (step == totalSteps - 1) "Hisoblash va Boshlash" else "Davom etish",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StepGender(
    selected: String,
    onSelect: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Jinsingizni tanlang",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "BMR va metabolizmni aniq hisoblash uchun kerak",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GenderOptionCard(
                title = "Erkak",
                icon = Icons.Default.Male,
                isSelected = selected == "Erkak",
                modifier = Modifier.weight(1f),
                onClick = { onSelect("Erkak") },
                testTag = "gender_male"
            )
            GenderOptionCard(
                title = "Ayol",
                icon = Icons.Default.Female,
                isSelected = selected == "Ayol",
                modifier = Modifier.weight(1f),
                onClick = { onSelect("Ayol") },
                testTag = "gender_female"
            )
        }
    }
}

@Composable
private fun GenderOptionCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    val borderColor = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val bgColor = if (isSelected) EmeraldContainer else MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = modifier
            .height(160.dp)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) EmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) EmeraldDark else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StepAge(
    age: Int,
    onAgeChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Yoshingiz nechida?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Yoshingizga qarab kunlik kaloriya sarfi o'zgaradi",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
        )

        Text(
            text = "$age",
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            color = EmeraldDark
        )
        Text(
            text = "yosh",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { if (age > 14) onAgeChange(age - 1) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Kamaytirish", tint = MaterialTheme.colorScheme.onSurface)
            }

            Slider(
                value = age.toFloat(),
                onValueChange = { onAgeChange(it.roundToInt()) },
                valueRange = 14f..80f,
                colors = SliderDefaults.colors(
                    thumbColor = EmeraldPrimary,
                    activeTrackColor = EmeraldPrimary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            IconButton(
                onClick = { if (age < 85) onAgeChange(age + 1) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Oshirish", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun StepMetrics(
    heightCm: Float,
    weightKg: Float,
    onHeightChange: (Float) -> Unit,
    onWeightChange: (Float) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Bo'yingiz va vazningiz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Tana massasi indeksi (BMI) va BMR uchun asosiy ko'rsatkichlar",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        // Height Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Bo'y (Bo'yingiz)", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${heightCm.roundToInt()} sm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldDark
                    )
                }
                Slider(
                    value = heightCm,
                    onValueChange = onHeightChange,
                    valueRange = 130f..220f,
                    colors = SliderDefaults.colors(
                        thumbColor = EmeraldPrimary,
                        activeTrackColor = EmeraldPrimary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        // Weight Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hozirgi vazn", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format("%.1f", weightKg)} kg",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProteinColor
                    )
                }
                Slider(
                    value = weightKg,
                    onValueChange = onWeightChange,
                    valueRange = 35f..160f,
                    colors = SliderDefaults.colors(
                        thumbColor = ProteinColor,
                        activeTrackColor = ProteinColor,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
private fun StepGoal(
    selected: String,
    onSelect: (String) -> Unit
) {
    val goals = listOf(
        Triple("Ozish", "Vazn tashlash va ortiqcha yog'larni yo'qotish", Icons.AutoMirrored.Filled.TrendingDown),
        Triple("Vazn yig'ish", "Sog'lom tana vaznini oshirish", Icons.AutoMirrored.Filled.TrendingUp),
        Triple("Baqquvvat bo'lish", "Mushak massasini oshirish va quvvat to'plash", Icons.Default.FitnessCenter),
        Triple("Formani saqlash", "Hozirgi sog'lom vazn va energiyani ushlab turish", Icons.Default.SelfImprovement)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Asosiy maqsadingiz nima?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Maqsadingizga qarab kaloriya defitsiti yoki profisiti rejalashtiriladi",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )

        goals.forEach { (title, subtitle, icon) ->
            val isSelected = selected == title
            val borderColor = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            val bgColor = if (isSelected) EmeraldContainer else MaterialTheme.colorScheme.surface

            Card(
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
                    .clickable { onSelect(title) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) EmeraldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = if (isSelected) EmeraldDark else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tanlandi",
                            tint = EmeraldDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepTargetWeight(
    currentWeightKg: Float,
    targetWeightKg: Float,
    onTargetChange: (Float) -> Unit
) {
    val diff = targetWeightKg - currentWeightKg

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Maqsadli vazningiz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Orzu qilgan natijangiz qancha kiloga teng?",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
        )

        Text(
            text = "${String.format("%.1f", targetWeightKg)} kg",
            fontSize = 58.sp,
            fontWeight = FontWeight.ExtraBold,
            color = FatColor
        )

        val diffText = if (diff < 0) {
            "${String.format("%.1f", -diff)} kg kamayish kerak"
        } else if (diff > 0) {
            "${String.format("%.1f", diff)} kg qo'shish kerak"
        } else {
            "Formani ushlab turish"
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFFEF3C7))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = diffText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB45309)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Slider(
            value = targetWeightKg,
            onValueChange = onTargetChange,
            valueRange = 35f..150f,
            colors = SliderDefaults.colors(
                thumbColor = FatColor,
                activeTrackColor = FatColor,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("35 kg", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Text("Hozirgi: ${currentWeightKg.roundToInt()} kg", color = EmeraldDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("150 kg", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StepActivity(
    selected: String,
    onSelect: (String) -> Unit
) {
    val activities = listOf(
        Pair("Kam harakat (O'tirib ishlash)", "Kun davomida kam jismoniy harakat"),
        Pair("Yengil faol (Haftada 1-3 kun)", "Haftada 1-3 marta yengil mashq yoki ko'p piyoda yurish"),
        Pair("O'rtacha faol (Haftada 3-5 kun)", "Muntazam sport mashg'ulotlari"),
        Pair("Juda faol (Har kuni faol mashg'ulot)", "Kuchli sport va faol jismoniy hayot"),
        Pair("Professional sportchi", "Og'ir jismoniy mehnat yoki kuniga 2 mahal mashq")
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Kunlik harakat darajangiz",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Kunlik energiya sarfi (TDEE) hisoblash uchun zarur",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
        )

        activities.forEach { (title, subtitle) ->
            val isSelected = selected == title
            val borderColor = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            val bgColor = if (isSelected) EmeraldContainer else MaterialTheme.colorScheme.surface

            Card(
                colors = CardDefaults.cardColors(containerColor = bgColor),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable { onSelect(title) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tanlandi",
                            tint = EmeraldDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
