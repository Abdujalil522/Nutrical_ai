package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.NutriCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("NutriCal AI", appName)
    }

    @Test
    fun `nutri calculator computes valid BMR and TDEE for male`() {
        val result = NutriCalculator.calculate(
            gender = "Erkak",
            age = 25,
            weightKg = 75f,
            heightCm = 175f,
            targetGoal = "Ozish",
            activityLevel = "O'rtacha faol (Haftada 3-5 kun)"
        )

        assertTrue("BMR should be greater than 1500", result.bmr > 1500)
        assertTrue("TDEE should be greater than BMR", result.tdee > result.bmr)
        assertTrue("Target calories should have deficit for weight loss", result.targetCalories < result.tdee)
        assertTrue("Protein should be adequate", result.targetProteinG >= 140)
    }
}
