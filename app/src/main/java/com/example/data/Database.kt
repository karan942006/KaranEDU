package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Single-user profile
    val name: String,
    val grade: String,
    val focusArea: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topic: String,
    val score: Int,
    val totalQuestions: Int,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_history")
data class LearningHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resources: String = "" // Semicolon-separated or JSON list of recommended resources
)

@Entity(tableName = "study_tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String,
    val timeSlot: String,
    val taskDescription: String,
    val isCompleted: Boolean = false,
    val date: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface eduaiDao {
    // Profile
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()

    // Quiz Results
    @Query("SELECT * FROM quiz_results ORDER BY date DESC")
    fun getAllQuizResultsFlow(): Flow<List<QuizResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResult)

    @Query("DELETE FROM quiz_results")
    suspend fun clearQuizResults()

    // Learning History (Chat logs)
    @Query("SELECT * FROM learning_history ORDER BY timestamp DESC")
    fun getAllLearningHistoryFlow(): Flow<List<LearningHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningHistory(history: LearningHistory)

    @Query("DELETE FROM learning_history")
    suspend fun clearLearningHistory()

    // Study Tasks
    @Query("SELECT * FROM study_tasks ORDER BY date DESC, timeSlot ASC")
    fun getAllStudyTasksFlow(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTask(task: StudyTask)

    @Update
    suspend fun updateStudyTask(task: StudyTask)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteStudyTask(id: Int)

    @Query("DELETE FROM study_tasks")
    suspend fun clearAllStudyTasks()
}

// --- Room Database ---

@Database(
    entities = [UserProfile::class, QuizResult::class, LearningHistory::class, StudyTask::class],
    version = 1,
    exportSchema = false
)
abstract class EduDatabase : RoomDatabase() {
    abstract fun dao(): eduaiDao

    companion object {
        @Volatile
        private var INSTANCE: EduDatabase? = null

        fun getDatabase(context: Context): EduDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduDatabase::class.java,
                    "eduai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository Pattern ---

class EduRepository(val dao: eduaiDao) {
    val userProfile: Flow<UserProfile?> = dao.getUserProfileFlow()
    val allQuizzes: Flow<List<QuizResult>> = dao.getAllQuizResultsFlow()
    val learningLogs: Flow<List<LearningHistory>> = dao.getAllLearningHistoryFlow()
    val studyTasks: Flow<List<StudyTask>> = dao.getAllStudyTasksFlow()

    suspend fun getUserProfileSync(): UserProfile? = dao.getUserProfile()
    suspend fun saveProfile(profile: UserProfile) = dao.saveUserProfile(profile)
    suspend fun recordQuiz(result: QuizResult) = dao.insertQuizResult(result)
    suspend fun recordChat(history: LearningHistory) = dao.insertLearningHistory(history)
    suspend fun addStudyTask(task: StudyTask) = dao.insertStudyTask(task)
    suspend fun updateStudyTask(task: StudyTask) = dao.updateStudyTask(task)
    suspend fun deleteStudyTask(id: Int) = dao.deleteStudyTask(id)
    
    suspend fun clearAllData() {
        dao.clearProfile()
        dao.clearQuizResults()
        dao.clearLearningHistory()
        dao.clearAllStudyTasks()
    }
}
