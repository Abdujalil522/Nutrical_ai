package com.example.data.ai

data class FoodAnalysisResult(
    val foodName: String,
    val portionDescription: String,
    val calories: Int,
    val proteinG: Float,
    val fatG: Float,
    val carbsG: Float,
    val breakdown: List<FoodItemDetail> = emptyList(),
    val healthTip: String = ""
)

data class FoodItemDetail(
    val name: String,
    val amount: String,
    val calories: Int
)
