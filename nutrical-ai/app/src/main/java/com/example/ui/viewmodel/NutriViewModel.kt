package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.FoodAnalysisResult
import com.example.data.ai.GeminiFoodAnalyzer
import com.example.data.local.MealLog
import com.example.data.local.NutriDatabase
import com.example.data.local.UserProfile
import com.example.data.repository.NutriRepository
import com.example.domain.NutriCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    AUTH,
    ONBOARDING,
    CALCULATING,
    DASHBOARD,
    SCANNER,
    HISTORY,
    PROFILE,
    SETTINGS
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class NutriViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NutriRepository

    init {
        val database = NutriDatabase.getInstance(application)
        repository = NutriRepository(database.nutriDao())
    }

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _screenHistory = mutableListOf<AppScreen>()

    // Current date format YYYY-MM-DD
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormatter.format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val todayMeals: StateFlow<List<MealLog>> = _selectedDate.flatMapLatest { date ->
        repository.getMealsForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMeals: StateFlow<List<MealLog>> = repository.getAllMeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val todayWaterLog: StateFlow<com.example.data.local.WaterLog?> = _selectedDate.flatMapLatest { date ->
        repository.getWaterLogForDate(date)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Onboarding temporary form state
    var obGender = MutableStateFlow("Erkak")
    var obAge = MutableStateFlow(24)
    var obWeightKg = MutableStateFlow(75f)
    var obHeightCm = MutableStateFlow(176f)
    var obGoal = MutableStateFlow("Ozish")
    var obTargetWeightKg = MutableStateFlow(70f)
    var obActivity = MutableStateFlow("O'rtacha faol")
    var obStep = MutableStateFlow(0) // 0 to 5

    // Calculation screen progress
    private val _calcProgress = MutableStateFlow(0f)
    val calcProgress: StateFlow<Float> = _calcProgress.asStateFlow()

    private val _calcStatusText = MutableStateFlow("BMR hisoblanmoqda...")
    val calcStatusText: StateFlow<String> = _calcStatusText.asStateFlow()

    // Scanner state
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _scanBitmap = MutableStateFlow<Bitmap?>(null)
    val scanBitmap: StateFlow<Bitmap?> = _scanBitmap.asStateFlow()

    private val _scanImageUri = MutableStateFlow<String?>(null)
    val scanImageUri: StateFlow<String?> = _scanImageUri.asStateFlow()

    private val _analysisResult = MutableStateFlow<FoodAnalysisResult?>(null)
    val analysisResult: StateFlow<FoodAnalysisResult?> = _analysisResult.asStateFlow()

    val scanMealType = MutableStateFlow("Tushlik")
    val editFoodName = MutableStateFlow("")
    val editCalories = MutableStateFlow("")
    val editProtein = MutableStateFlow("")
    val editFat = MutableStateFlow("")
    val editCarbs = MutableStateFlow("")
    val editPortion = MutableStateFlow("")

    init {
        checkInitialState()
    }

    private fun checkInitialState() {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce()
            if (profile == null || !profile.isOnboarded) {
                _currentScreen.value = AppScreen.AUTH
            } else {
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        return if (_screenHistory.isNotEmpty()) {
            _currentScreen.value = _screenHistory.removeAt(_screenHistory.lastIndex)
            true
        } else if (_currentScreen.value != AppScreen.DASHBOARD && _currentScreen.value != AppScreen.AUTH) {
            _currentScreen.value = AppScreen.DASHBOARD
            true
        } else {
            false
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Auth actions
    fun authenticate(email: String, isSignUp: Boolean) {
        viewModelScope.launch {
            val existing = repository.getUserProfileOnce()
            if (existing != null && existing.isOnboarded) {
                _currentScreen.value = AppScreen.DASHBOARD
            } else {
                val profile = (existing ?: UserProfile()).copy(
                    email = email,
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                )
                repository.saveUserProfile(profile)
                _currentScreen.value = AppScreen.ONBOARDING
            }
        }
    }

    fun nextOnboardingStep() {
        if (obStep.value < 5) {
            obStep.value += 1
        } else {
            startCalculation()
        }
    }

    fun prevOnboardingStep() {
        if (obStep.value > 0) {
            obStep.value -= 1
        }
    }

    private fun startCalculation() {
        _currentScreen.value = AppScreen.CALCULATING
        viewModelScope.launch {
            _calcProgress.value = 0.15f
            _calcStatusText.value = "BMR (Asosiy metabolizm) hisoblanmoqda..."
            delay(800)

            _calcProgress.value = 0.45f
            _calcStatusText.value = "Kunlik energiya sarfi (TDEE) hisoblanmoqda..."
            delay(900)

            _calcProgress.value = 0.75f
            _calcStatusText.value = "Oqsillar, yog'lar va uglevodlar balansi aniqlanmoqda..."
            delay(900)

            val calc = NutriCalculator.calculate(
                gender = obGender.value,
                age = obAge.value,
                weightKg = obWeightKg.value,
                heightCm = obHeightCm.value,
                targetGoal = obGoal.value,
                activityLevel = obActivity.value
            )

            val currentProfile = repository.getUserProfileOnce() ?: UserProfile()
            val updated = currentProfile.copy(
                gender = obGender.value,
                age = obAge.value,
                weightKg = obWeightKg.value,
                heightCm = obHeightCm.value,
                targetGoal = obGoal.value,
                targetWeightKg = obTargetWeightKg.value,
                activityLevel = obActivity.value,
                bmr = calc.bmr,
                tdee = calc.tdee,
                targetCalories = calc.targetCalories,
                targetProteinG = calc.targetProteinG,
                targetFatG = calc.targetFatG,
                targetCarbsG = calc.targetCarbsG,
                isOnboarded = true,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)

            _calcProgress.value = 1.0f
            _calcStatusText.value = "Sizning shaxsiy taomnoma me'yoringiz tayyor!"
            delay(600)
            _screenHistory.clear()
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    // Scanner actions
    fun onImageSelected(bitmap: Bitmap, uriString: String? = null) {
        _scanBitmap.value = bitmap
        _scanImageUri.value = uriString
        _analysisResult.value = null
        startAiAnalysis(bitmap)
    }

    fun startAiAnalysis(bitmap: Bitmap, customHint: String? = null) {
        _isAnalyzing.value = true
        viewModelScope.launch {
            val result = GeminiFoodAnalyzer.analyzeFoodImage(bitmap, customHint)
            _isAnalyzing.value = false
            result.onSuccess { analysis ->
                _analysisResult.value = analysis
                editFoodName.value = analysis.foodName
                editCalories.value = analysis.calories.toString()
                editProtein.value = analysis.proteinG.toString()
                editFat.value = analysis.fatG.toString()
                editCarbs.value = analysis.carbsG.toString()
                editPortion.value = analysis.portionDescription
            }.onFailure {
                // Should not happen as GeminiFoodAnalyzer has fallback
                editFoodName.value = "Taom"
                editCalories.value = "350"
                editProtein.value = "15"
                editFat.value = "10"
                editCarbs.value = "40"
            }
        }
    }

    fun saveCurrentAnalyzedMeal() {
        val calories = editCalories.value.toIntOrNull() ?: 350
        val protein = editProtein.value.toFloatOrNull() ?: 15f
        val fat = editFat.value.toFloatOrNull() ?: 10f
        val carbs = editCarbs.value.toFloatOrNull() ?: 40f
        val foodName = editFoodName.value.ifBlank { "Taom" }
        val portion = editPortion.value

        val meal = MealLog(
            date = todayDateString,
            mealType = scanMealType.value,
            foodName = foodName,
            portionDescription = portion,
            calories = calories,
            proteinG = protein,
            fatG = fat,
            carbsG = carbs,
            imageUri = _scanImageUri.value,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertMeal(meal)
            _analysisResult.value = null
            _scanBitmap.value = null
            _scanImageUri.value = null
            navigateTo(AppScreen.DASHBOARD)
        }
    }

    fun addManualMeal(
        mealType: String,
        foodName: String,
        calories: Int,
        protein: Float,
        fat: Float,
        carbs: Float,
        portion: String = ""
    ) {
        viewModelScope.launch {
            val meal = MealLog(
                date = todayDateString,
                mealType = mealType,
                foodName = foodName,
                portionDescription = portion,
                calories = calories,
                proteinG = protein,
                fatG = fat,
                carbsG = carbs,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMeal(meal)
        }
    }

    fun deleteMeal(meal: MealLog) {
        viewModelScope.launch {
            repository.deleteMeal(meal)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            val calc = NutriCalculator.calculate(
                gender = profile.gender,
                age = profile.age,
                weightKg = profile.weightKg,
                heightCm = profile.heightCm,
                targetGoal = profile.targetGoal,
                activityLevel = profile.activityLevel
            )
            val updated = profile.copy(
                bmr = calc.bmr,
                tdee = calc.tdee,
                targetCalories = calc.targetCalories,
                targetProteinG = calc.targetProteinG,
                targetFatG = calc.targetFatG,
                targetCarbsG = calc.targetCarbsG,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveUserProfile(updated)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val date = _selectedDate.value
            val existing = repository.getWaterLogForDateOnce(date)
            val currentAmount = existing?.amountMl ?: 0
            val target = existing?.targetMl ?: (userProfile.value?.dailyWaterGoalMl ?: 2500)
            val updated = com.example.data.local.WaterLog(
                date = date,
                amountMl = (currentAmount + amountMl).coerceAtLeast(0),
                targetMl = target,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveWaterLog(updated)
        }
    }

    fun removeWater(amountMl: Int) {
        addWater(-amountMl)
    }

    fun setDailyWaterGoal(goalMl: Int) {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce()
            if (profile != null) {
                repository.saveUserProfile(profile.copy(dailyWaterGoalMl = goalMl))
            }
            val date = _selectedDate.value
            val existing = repository.getWaterLogForDateOnce(date)
            if (existing != null) {
                repository.saveWaterLog(existing.copy(targetMl = goalMl))
            }
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleWaterReminder(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce()
            if (profile != null) {
                repository.saveUserProfile(profile.copy(waterReminderEnabled = enabled))
            }
        }
    }

    fun toggleMealReminder(enabled: Boolean) {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce()
            if (profile != null) {
                repository.saveUserProfile(profile.copy(mealReminderEnabled = enabled))
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun resetApp() {
        viewModelScope.launch {
            val profile = repository.getUserProfileOnce()
            if (profile != null) {
                repository.saveUserProfile(profile.copy(isOnboarded = false))
            }
            _currentScreen.value = AppScreen.AUTH
        }
    }
}
