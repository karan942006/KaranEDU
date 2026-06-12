package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// --- Gemini REST API Request & Response Models ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseFormat: ResponseFormat? = null,
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val type: String = "APPLICATION_JSON" // In REST API, it's responseMimeType in typical config, or formatted in config as:
    // val responseMimeType: String? = "application/json"
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content?
)

// --- Domain-Specific JSON Structures for Structured Output ---

@JsonClass(generateAdapter = true)
data class AIAnswer(
    val explanation: String,
    val keyConcepts: List<String>?,
    val resources: List<StudyResource>?,
    val followUpQuestions: List<String>?
)

@JsonClass(generateAdapter = true)
data class StudyResource(
    val title: String,
    val type: String, // "video", "article", "book", "course"
    val url: String
)

@JsonClass(generateAdapter = true)
data class AIQuizQuestion(
    val question: String,
    val options: List<String>,
    val answerIndex: Int,
    val explanation: String
)

@JsonClass(generateAdapter = true)
data class AICreatedQuiz(
    val topic: String,
    val questions: List<AIQuizQuestion>
)

@JsonClass(generateAdapter = true)
data class AIPlannedTask(
    val subject: String,
    val timeSlot: String,
    val taskDescription: String
)

@JsonClass(generateAdapter = true)
data class AIStudyPlanResponse(
    val schedule: List<AIPlannedTask>
)

@JsonClass(generateAdapter = true)
data class AIFlashcard(
    val front: String,
    val back: String,
    val explanation: String? = null
)

@JsonClass(generateAdapter = true)
data class AICreatedFlashcards(
    val topic: String,
    val cards: List<AIFlashcard>
)

// --- Retrofit Interface ---

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GeminiResponse
}

// --- Client Singleton ---

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    // Helper Moshi instance for local JSON parsing
    val moshiParser: Moshi = moshi

    // --- Core Action Methods ---

    suspend fun askTutor(question: String, studentGrade: String, studentFocus: String, apiKey: String): AIAnswer {
        val prompt = """
            Answer the following student question. 
            The student is in $studentGrade and is focusing on $studentFocus. Use friendly, clear language appropriate for this grade level.
            Provide:
            1. A comprehensive explanation (in simple educational language).
            2. A list of 2-3 key terms/concepts.
            3. A list of 3 high-quality study resources (helpful articles, YouTube search links, books). Format the URLs as direct helpful search queries if links aren't specific, e.g. "https://www.youtube.com/results?search_query=..."
            4. 3 relevant educational follow-up questions that of similar educational value.

            Respond ONLY in the following JSON format:
            {
               "explanation": "Detailed step-by-step simple explanation...",
               "keyConcepts": ["Concept A", "Concept B"],
               "resources": [
                   {"title": "Suggested video/book title", "type": "video", "url": "url..."},
                   {"title": "Suggested notes/article title", "type": "article", "url": "url..."}
               ],
               "followUpQuestions": ["Q1?", "Q2?", "Q3?"]
            }

            Student Question: "$question"
        """.trimIndent()

        val request = createJsonRequest(prompt)
        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanedJson = cleanJsonString(jsonText)
            moshi.adapter(AIAnswer::class.java).fromJson(cleanedJson) ?: throw Exception("Failed to parse AI response")
        } catch (e: Exception) {
            AIAnswer(
                explanation = "I encountered an issue answering that. Here is a brief guide: $question.\n\nError: ${e.localizedMessage ?: "Connection Timeout"}",
                keyConcepts = listOf("Review", "Error Check"),
                resources = listOf(
                    StudyResource("Search Education Materials", "article", "https://www.google.com/search?q=$question")
                ),
                followUpQuestions = listOf("Try asking in a simpler way", "Check your Internet connection")
            )
        }
    }

    suspend fun generateQuiz(topic: String, apiKey: String): AICreatedQuiz {
        val prompt = """
            Create a highly informative 3-question multiple choice quiz on the topic: "$topic".
            Each question must have exactly 4 options and a clear answerIndex (0 to 3) representing the correct option, and a helpful description/explanation explaining why that option is correct.
            Ensure the difficulty is appropriate for the topic.

            Respond ONLY in the following JSON format:
            {
               "topic": "$topic",
               "questions": [
                  {
                     "question": "Question text here?",
                     "options": ["Option A", "Option B", "Option C", "Option D"],
                     "answerIndex": 0,
                     "explanation": "Deep simple explanation explaining why option A is correct..."
                  },
                  ...
               ]
            }
        """.trimIndent()

        val request = createJsonRequest(prompt)
        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanedJson = cleanJsonString(jsonText)
            moshi.adapter(AICreatedQuiz::class.java).fromJson(cleanedJson) ?: throw Exception("Failed to parse AI quiz")
        } catch (e: Exception) {
            AICreatedQuiz(
                topic = topic,
                questions = listOf(
                    AIQuizQuestion(
                        question = "What is the primary concept behind $topic?",
                        options = listOf("Core Principles", "Unrelated Facts", "None", "Both"),
                        answerIndex = 0,
                        explanation = "This is a basic fallback question."
                    )
                )
            )
        }
    }

    suspend fun generateStudyPlan(subjectsList: List<String>, hoursPerDay: Int, apiKey: String): AIStudyPlanResponse {
        val subjectsStr = subjectsList.joinToString(", ")
        val prompt = """
            Create a personalized study schedule for a student studying the following subjects: $subjectsStr.
            The student is free to study for $hoursPerDay hours today.
            Break down the time into 3-4 strategic sessions with logical time slots (e.g. "09:00 AM - 10:30 AM", "11:00 AM - 12:30 PM", "02:00 PM - 03:00 PM").
            Divide subjects appropriately and provide concrete academic tasks for each (e.g., "Summarize chapter 2", "Do 10 practice questions", "Revise terminology terms").

            Respond ONLY in the following JSON format:
            {
              "schedule": [
                 {
                    "subject": "Name of Subject",
                    "timeSlot": "Start - End Time",
                    "taskDescription": "Specific actionable study guidelines..."
                 },
                 ...
              ]
            }
        """.trimIndent()

        val request = createJsonRequest(prompt)
        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanedJson = cleanJsonString(jsonText)
            moshi.adapter(AIStudyPlanResponse::class.java).fromJson(cleanedJson) ?: throw Exception("Plan parsing failed")
        } catch (e: Exception) {
            AIStudyPlanResponse(
                schedule = subjectsList.mapIndexed { idx, sub ->
                    AIPlannedTask(
                        subject = sub,
                        timeSlot = "Slot ${idx + 1}",
                        taskDescription = "Read textbook chapters and do personal practice questions."
                    )
                }
            )
        }
    }

    suspend fun generateFlashcards(topic: String, apiKey: String): AICreatedFlashcards {
        val prompt = """
            Create exactly 3 highly educational flashcards for active recall study about the topic: "$topic".
            Each card has a front (a question or core concept terms) and a back (the precise concise answer/explanation definition) and a simple tip for study recall.
            Provide:
            - front: A concise active query.
            - back: A brief direct master answer or solution.
            - explanation: A tip or extra helpful context.

            Respond ONLY in the following JSON format:
            {
               "topic": "$topic",
               "cards": [
                  {
                     "front": "Question/Concept",
                     "back": "Master answer/definition",
                     "explanation": "Brief active memory context"
                  },
                  ...
               ]
            }
        """.trimIndent()

        val request = createJsonRequest(prompt)
        return try {
            val response = apiService.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            val cleanedJson = cleanJsonString(jsonText)
            moshi.adapter(AICreatedFlashcards::class.java).fromJson(cleanedJson) ?: throw Exception("Failed to parse AI flashcards")
        } catch (e: Exception) {
            AICreatedFlashcards(
                topic = topic,
                cards = listOf(
                    AIFlashcard(
                        front = "Core question about $topic?",
                        back = "Essential master conceptual definition for $topic.",
                        explanation = "Try relating this concept to daily applications."
                    ),
                    AIFlashcard(
                        front = "Another important aspect of $topic?",
                        back = "Key supporting fact or procedural step.",
                        explanation = "Keep dynamic recall active."
                    ),
                    AIFlashcard(
                        front = "Common practical usage or exam question?",
                        back = "Standard application sample or rule detail.",
                        explanation = "Revise with periodic mock checkpoints."
                    )
                )
            )
        }
    }

    // Custom helper to clean markdown JSON wrappers, backticks, or prefix headers that the model might output
    private fun cleanJsonString(text: String): String {
        var clean = text.trim()
        if (clean.startsWith("```")) {
            clean = clean.removePrefix("```json").removePrefix("```").trim()
            if (clean.endsWith("```")) {
                clean = clean.removeSuffix("```").trim()
            }
        }
        return clean
    }

    private fun createJsonRequest(prompt: String): GenerateContentRequest {
        return GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            // Set responseMimeType to application/json so Gemini returns structured JSON
            generationConfig = GenerationConfig(
                temperature = 0.4f,
                maxOutputTokens = 2048
            )
        )
    }
}
