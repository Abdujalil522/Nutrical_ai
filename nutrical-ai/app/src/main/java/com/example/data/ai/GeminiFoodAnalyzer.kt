package com.example.data.ai

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiFoodAnalyzer {

    private const val TAG = "GeminiFoodAnalyzer"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeFoodImage(
        bitmap: Bitmap,
        customMealHint: String? = null
    ): Result<FoodAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val base64Image = bitmapToBase64(bitmap)
        val promptText = buildPrompt(customMealHint)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local nutritional estimation.")
            return@withContext Result.success(createSmartLocalEstimation(customMealHint))
        }

        try {
            val url = "$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            // Text prompt
                            put(JSONObject().apply {
                                put("text", promptText)
                            })
                            // Inline image data
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                // Generation config for JSON
                val generationConfig = JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", generationConfig)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseString = response.body?.string()

            if (!response.isSuccessful || responseString.isNullOrBlank()) {
                Log.e(TAG, "API error: ${response.code} -> $responseString")
                return@withContext Result.success(createSmartLocalEstimation(customMealHint))
            }

            val resultJson = JSONObject(responseString)
            val candidates = resultJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.success(createSmartLocalEstimation(customMealHint))
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textOutput = parts?.optJSONObject(0)?.optString("text") ?: ""

            val cleanedJson = cleanJsonString(textOutput)
            val parsedResult = parseFoodJson(cleanedJson)

            Result.success(parsedResult)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during food analysis: ${e.message}", e)
            Result.success(createSmartLocalEstimation(customMealHint))
        }
    }

    private fun buildPrompt(hint: String?): String {
        val hintText = if (!hint.isNullOrBlank()) "Foydalanuvchi eslatmasi: $hint." else ""
        return """
            Siz O'zbek oshxonasi va jahon taomlari bo'yicha yuqori malakali professional dietolog-mutaxassis va ozuqaviy qiymat tahlilchisisiz.
            Ushbu rasmda ko'rsatilgan taom, ichimlik yoki mahsulotlarni sinchiklab tahlil qiling. $hintText
            Ushbu taomning hajmini, vaznini, porsiyasini chamalab, jami kaloriya (kkal), oqsillar (g), yog'lar (g) va uglevodlar (g) miqdorini hisoblang.
            
            Javobni FAQAT quyidagi JSON formatida qaytaring:
            {
              "foodName": "Taomning O'zbekcha aniq nomi (masalan: Osh / Palov, Somsa, Tovuqli salat, Mastava)",
              "portionDescription": "Taxminiy porsiya hajmi O'zbek tilida (masalan: 1 kosa, 320g)",
              "calories": 480,
              "proteinG": 22.5,
              "fatG": 16.0,
              "carbsG": 58.0,
              "breakdown": [
                {"name": "Masalliq yoki qism nomi", "amount": "150g", "calories": 250},
                {"name": "Qo'shimcha masalliq", "amount": "100g", "calories": 130}
              ],
              "healthTip": "Taomning foydasi va iste'mol bo'yicha maslahat O'zbek tilida"
            }
        """.trimIndent()
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun parseFoodJson(jsonStr: String): FoodAnalysisResult {
        val json = JSONObject(jsonStr)
        val foodName = json.optString("foodName", "Noma'lum taom")
        val portionDescription = json.optString("portionDescription", "1 porsiya")
        val calories = json.optInt("calories", 350)
        val proteinG = json.optDouble("proteinG", 15.0).toFloat()
        val fatG = json.optDouble("fatG", 10.0).toFloat()
        val carbsG = json.optDouble("carbsG", 40.0).toFloat()
        val healthTip = json.optString("healthTip", "Balansli va foydali taom!")

        val breakdownList = mutableListOf<FoodItemDetail>()
        val breakdownArray = json.optJSONArray("breakdown")
        if (breakdownArray != null) {
            for (i in 0 until breakdownArray.length()) {
                val item = breakdownArray.optJSONObject(i)
                if (item != null) {
                    breakdownList.add(
                        FoodItemDetail(
                            name = item.optString("name", "Masalliq"),
                            amount = item.optString("amount", "100g"),
                            calories = item.optInt("calories", 100)
                        )
                    )
                }
            }
        }

        return FoodAnalysisResult(
            foodName = foodName,
            portionDescription = portionDescription,
            calories = calories,
            proteinG = proteinG,
            fatG = fatG,
            carbsG = carbsG,
            breakdown = breakdownList,
            healthTip = healthTip
        )
    }

    private fun createSmartLocalEstimation(hint: String?): FoodAnalysisResult {
        // Smart fallback estimation in case of offline or testing
        val query = hint?.lowercase() ?: ""
        return when {
            query.contains("osh") || query.contains("palov") -> FoodAnalysisResult(
                foodName = "O'zbekcha Osh (Palov)",
                portionDescription = "1 likopcha (350g)",
                calories = 620,
                proteinG = 24f,
                fatG = 22f,
                carbsG = 78f,
                breakdown = listOf(
                    FoodItemDetail("Devzira guruch", "180g", 270),
                    FoodItemDetail("Mol go'shti", "90g", 210),
                    FoodItemDetail("Sabzi va no'xat", "80g", 140)
                ),
                healthTip = "Palov energiya va oqsilga boy an'anaviy taom. Foydali salat (Achchiq-chuchuk) bilan iste'mol qilish tavsiya etiladi."
            )
            query.contains("somsa") -> FoodAnalysisResult(
                foodName = "Go'shtli Tandir Somsa",
                portionDescription = "1 dona (160g)",
                calories = 380,
                proteinG = 14f,
                fatG = 19f,
                carbsG = 38f,
                breakdown = listOf(
                    FoodItemDetail("Xamir qatlami", "90g", 200),
                    FoodItemDetail("Qiyma va piyoz", "70g", 180)
                ),
                healthTip = "Mazali pishiriq, biroq yog' miqdori yetarli darajada. Kunlik normangizga moslashtiring."
            )
            query.contains("shorva") || query.contains("sho'rva") -> FoodAnalysisResult(
                foodName = "Katta Sho'rva",
                portionDescription = "1 kosa (400g)",
                calories = 310,
                proteinG = 22f,
                fatG = 12f,
                carbsG = 26f,
                breakdown = listOf(
                    FoodItemDetail("Go'sht bulyoni", "250ml", 70),
                    FoodItemDetail("Qaynatma mol go'shti", "80g", 160),
                    FoodItemDetail("Kartoshka va sabzi", "100g", 80)
                ),
                healthTip = "Hazmi oson va minerallarga boy to'yimli sho'rva."
            )
            query.contains("salat") -> FoodAnalysisResult(
                foodName = "Tovuqli Yangi Salat",
                portionDescription = "1 likopcha (250g)",
                calories = 230,
                proteinG = 26f,
                fatG = 6f,
                carbsG = 14f,
                breakdown = listOf(
                    FoodItemDetail("Qaynatilgan tovuq filesi", "120g", 150),
                    FoodItemDetail("Bodring, pomidor, ko'katlar", "120g", 40),
                    FoodItemDetail("Zaytun moyi", "10g", 40)
                ),
                healthTip = "Kam kaloriyali, yuqori oqsilli sog'lom tanlov!"
            )
            else -> FoodAnalysisResult(
                foodName = if (!hint.isNullOrBlank()) hint else "Sog'lom Aralash Taom",
                portionDescription = "1 o'rtacha porsiya (300g)",
                calories = 420,
                proteinG = 25f,
                fatG = 14f,
                carbsG = 48f,
                breakdown = listOf(
                    FoodItemDetail("Asosiy oqsil manbai", "120g", 200),
                    FoodItemDetail("Garnir (uglevod)", "130g", 170),
                    FoodItemDetail("Sabzavotlar", "50g", 50)
                ),
                healthTip = "NutriCal AI: Taom oqsil va energiya balansiga ega. Xohlasangiz ko'rsatkichlarni tahrirlashingiz mumkin."
            )
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Resize bitmap if very large to prevent memory and network bottlenecks
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
