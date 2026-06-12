package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseService {
    private const val TAG = "FirebaseService"
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        try {
            val options = FirebaseOptions.Builder()
                .setApplicationId("1:819905509192:android:46aa7ebc3cfb93d558257d")
                .setApiKey("AIzaSyAl6tYvqP5hUk9lt8iifRK7PHUHoqimy5U")
                .setProjectId("karanedu")
                .setStorageBucket("karanedu.firebasestorage.app")
                .build()

            FirebaseApp.initializeApp(context.applicationContext, options)
            isInitialized = true
            Log.d(TAG, "Firebase initialized successfully with program options!")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase: ${e.message}", e)
        }
    }

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    // True Cloud Firestore sync for profile
    suspend fun syncProfileToCloud(profile: UserProfile) {
        val db = firestore ?: return
        val currentUid = currentUser?.uid ?: "anonymous_user"
        try {
            val data = mapOf(
                "uid" to currentUid,
                "name" to profile.name,
                "grade" to profile.grade,
                "focusArea" to profile.focusArea,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(currentUid).set(data).await()
            Log.d(TAG, "Successfully synced user profile to FireStore cloud.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync profile to Cloud: ${e.message}")
        }
    }

    // Sync Quiz Result to Firestore
    suspend fun syncQuizResultToCloud(result: QuizResult) {
        val db = firestore ?: return
        val currentUid = currentUser?.uid ?: "anonymous_user"
        try {
            val data = mapOf(
                "uid" to currentUid,
                "topic" to result.topic,
                "score" to result.score,
                "totalQuestions" to result.totalQuestions,
                "date" to result.date
            )
            db.collection("quiz_results").add(data).await()
            Log.d(TAG, "Successfully saved quiz result into FireStore cloud database.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed of firestore quiz sync: ${e.message}")
        }
    }

    // Sync Study Task to Firestore
    suspend fun syncStudyTaskToCloud(task: StudyTask) {
        val db = firestore ?: return
        val currentUid = currentUser?.uid ?: "anonymous_user"
        try {
            val data = mapOf(
                "uid" to currentUid,
                "id" to task.id,
                "subject" to task.subject,
                "timeSlot" to task.timeSlot,
                "taskDescription" to task.taskDescription,
                "isCompleted" to task.isCompleted,
                "date" to task.date
            )
            db.collection("study_tasks").document("${currentUid}_${task.id}").set(data).await()
            Log.d(TAG, "Successfully synced task to Firestore.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed study task firestore sync: ${e.message}")
        }
    }
}
