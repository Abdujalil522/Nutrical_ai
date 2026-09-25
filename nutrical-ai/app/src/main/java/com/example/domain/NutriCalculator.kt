package com.example.domain

import kotlin.math.roundToInt

data class CalculationResult(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val targetProteinG: Int,
    val targetFatG: Int,
    val targetCarbsG: Int
)

object NutriCalculator {

    fun calculate(
        gender: String, // "Erkak" or "Ayol"
        age: Int,
        weightKg: Float,
        heightCm: Float,
        targetGoal: String, // "Ozish", "Vazn yig'ish", "Baqquvvat bo'lish", "Formani saqlash"
        activityLevel: String
    ): CalculationResult {
        // Mifflin-St Jeor formula
        val baseBmr = if (gender.contains("Erkak", ignoreCase = true)) {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) + 5f
        } else {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) - 161f
        }
        val bmr = baseBmr.roundToInt().coerceAtLeast(1000)

        val activityMultiplier = when {
            activityLevel.contains("Kam harakat", ignoreCase = true) -> 1.2f
            activityLevel.contains("Yengil", ignoreCase = true) -> 1.375f
            activityLevel.contains("O'rtacha", ignoreCase = true) -> 1.55f
            activityLevel.contains("Juda", ignoreCase = true) -> 1.725f
            activityLevel.contains("Professional", ignoreCase = true) -> 1.9f
            else -> 1.55f
        }

        val tdee = (bmr * activityMultiplier).roundToInt()

        val calorieAdjustment = when {
            targetGoal.contains("Ozish", ignoreCase = true) -> -450
            targetGoal.contains("Vazn yig'ish", ignoreCase = true) -> +450
            targetGoal.contains("Baqquvvat", ignoreCase = true) -> +300
            else -> 0
        }

        val targetCalories = (tdee + calorieAdjustment).coerceAtLeast(1200)

        // Macros calculation
        val proteinRatioPerKg = when {
            targetGoal.contains("Baqquvvat", ignoreCase = true) -> 2.2f
            targetGoal.contains("Ozish", ignoreCase = true) -> 2.0f
            else -> 1.8f
        }

        val proteinG = (weightKg * proteinRatioPerKg).roundToInt().coerceAtLeast(60)
        val proteinCalories = proteinG * 4

        val fatCalories = (targetCalories * 0.28f).roundToInt()
        val fatG = (fatCalories / 9f).roundToInt().coerceAtLeast(40)

        val remainingCalories = (targetCalories - proteinCalories - (fatG * 9)).coerceAtLeast(300)
        val carbsG = (remainingCalories / 4f).roundToInt()

        return CalculationResult(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            targetProteinG = proteinG,
            targetFatG = fatG,
            targetCarbsG = carbsG
        )
    }
}
