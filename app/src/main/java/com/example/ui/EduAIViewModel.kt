package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ChatUiState {
    object Idle : ChatUiState
    object Loading : ChatUiState
    data class Success(val answer: AIAnswer) : ChatUiState
    data class Error(val message: String) : ChatUiState
}

sealed interface QuizUiState {
    object Idle : QuizUiState
    object Loading : QuizUiState
    data class Active(val quiz: AICreatedQuiz, val currentQuestionIndex: Int, val selectedAnswers: Map<Int, Int>, val submitted: Boolean) : QuizUiState
    data class Completed(val quiz: AICreatedQuiz, val score: Int, val total: Int) : QuizUiState
    data class Error(val message: String) : QuizUiState
}

sealed interface PlannerUiState {
    object Idle : PlannerUiState
    object Loading : PlannerUiState
    object Success : PlannerUiState
    data class Error(val message: String) : PlannerUiState
}

sealed interface FlashcardUiState {
    object Idle : FlashcardUiState
    object Loading : FlashcardUiState
    data class Success(val flashcards: AICreatedFlashcards) : FlashcardUiState
    data class Error(val message: String) : FlashcardUiState
}

class EduAIViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EduRepository
    val userProfile: StateFlow<UserProfile?>
    val allQuizzes: StateFlow<List<QuizResult>>
    val chatHistory: StateFlow<List<LearningHistory>>
    val studyTasks: StateFlow<List<StudyTask>>

    init {
        val database = EduDatabase.getDatabase(application)
        repository = EduRepository(database.dao())
        
        userProfile = repository.userProfile
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
            
        allQuizzes = repository.allQuizzes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        chatHistory = repository.learningLogs
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
            
        studyTasks = repository.studyTasks
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- State Holders ---
    private val _chatUiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val chatUiState: StateFlow<ChatUiState> = _chatUiState.asStateFlow()

    private val _quizUiState = MutableStateFlow<QuizUiState>(QuizUiState.Idle)
    val quizUiState: StateFlow<QuizUiState> = _quizUiState.asStateFlow()

    private val _plannerUiState = MutableStateFlow<PlannerUiState>(PlannerUiState.Idle)
    val plannerUiState: StateFlow<PlannerUiState> = _plannerUiState.asStateFlow()

    private val _flashcardUiState = MutableStateFlow<FlashcardUiState>(FlashcardUiState.Idle)
    val flashcardUiState: StateFlow<FlashcardUiState> = _flashcardUiState.asStateFlow()

    // --- Actions ---

    // Save Profile
    fun createProfile(name: String, grade: String, focusArea: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = UserProfile(name = name, grade = grade, focusArea = focusArea)
            repository.saveProfile(profile)
            FirebaseService.syncProfileToCloud(profile)
        }
    }

    // Ask Tutor
    fun askAiTutor(question: String) {
        val currentProfile = userProfile.value ?: UserProfile(name = "Student", grade = "10th Grade", focusArea = "General Study")
        val apiKey = BuildConfig.GEMINI_API_KEY ?: ""
        
        _chatUiState.value = ChatUiState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    GeminiClient.askTutor(
                        question = question,
                        studentGrade = currentProfile.grade,
                        studentFocus = currentProfile.focusArea,
                        apiKey = apiKey
                    )
                }
                
                // Save resources list as a semi-colon separated string, or local details
                val resourcesStr = response.resources?.joinToString(";") { "${it.title}|${it.type}|${it.url}" } ?: ""
                
                // Record the Q&A in local database
                withContext(Dispatchers.IO) {
                    repository.recordChat(
                        LearningHistory(
                            question = question,
                            answer = response.explanation,
                            resources = resourcesStr
                        )
                    )
                }
                
                _chatUiState.value = ChatUiState.Success(response)
            } catch (e: Exception) {
                _chatUiState.value = ChatUiState.Error(e.localizedMessage ?: "Unknown API response issue")
            }
        }
    }

    fun clearChatState() {
        _chatUiState.value = ChatUiState.Idle
    }

    // --- Quiz Management ---
    
    fun generateNewQuiz(topic: String) {
        val apiKey = BuildConfig.GEMINI_API_KEY ?: ""
        _quizUiState.value = QuizUiState.Loading
        
        viewModelScope.launch {
            try {
                val createdQuiz = withContext(Dispatchers.IO) {
                    GeminiClient.generateQuiz(topic = topic, apiKey = apiKey)
                }
                _quizUiState.value = QuizUiState.Active(
                    quiz = createdQuiz,
                    currentQuestionIndex = 0,
                    selectedAnswers = emptyMap(),
                    submitted = false
                )
            } catch (e: Exception) {
                _quizUiState.value = QuizUiState.Error(e.localizedMessage ?: "Could not build quiz")
            }
        }
    }

    fun selectQuizAnswer(questionIndex: Int, optionIndex: Int) {
        val state = _quizUiState.value
        if (state is QuizUiState.Active && !state.submitted) {
            val updatedSelection = state.selectedAnswers.toMutableMap().apply {
                put(questionIndex, optionIndex)
            }
            _quizUiState.value = state.copy(selectedAnswers = updatedSelection)
        }
    }

    fun submitQuiz() {
        val state = _quizUiState.value
        if (state is QuizUiState.Active && !state.submitted) {
            _quizUiState.value = state.copy(submitted = true)
            
            // Calculate final score
            var score = 0
            state.quiz.questions.forEachIndexed { idx, question ->
                if (state.selectedAnswers[idx] == question.answerIndex) {
                    score++
                }
            }
            
            // Save to database
            viewModelScope.launch(Dispatchers.IO) {
                val result = QuizResult(
                    topic = state.quiz.topic,
                    score = score,
                    totalQuestions = state.quiz.questions.size
                )
                repository.recordQuiz(result)
                FirebaseService.syncQuizResultToCloud(result)
            }
        }
    }

    fun nextQuizQuestion() {
        val state = _quizUiState.value
        if (state is QuizUiState.Active) {
            if (state.currentQuestionIndex < state.quiz.questions.size - 1) {
                _quizUiState.value = state.copy(currentQuestionIndex = state.currentQuestionIndex + 1)
            } else {
                // Done! Compute score
                var score = 0
                state.quiz.questions.forEachIndexed { idx, question ->
                    if (state.selectedAnswers[idx] == question.answerIndex) {
                        score++
                    }
                }
                _quizUiState.value = QuizUiState.Completed(
                    quiz = state.quiz,
                    score = score,
                    total = state.quiz.questions.size
                )
            }
        }
    }

    fun resetQuiz() {
        _quizUiState.value = QuizUiState.Idle
    }

    // --- Study Planner Management ---

    fun generatePlannerSchedule(subjects: List<String>, hours: Int) {
        val apiKey = BuildConfig.GEMINI_API_KEY ?: ""
        _plannerUiState.value = PlannerUiState.Loading
        
        viewModelScope.launch {
            try {
                val plan = withContext(Dispatchers.IO) {
                    GeminiClient.generateStudyPlan(subjectsList = subjects, hoursPerDay = hours, apiKey = apiKey)
                }
                
                withContext(Dispatchers.IO) {
                    // Overwrite the existing plan tasks or add new ones to give the user a clear smart planner
                    repository.dao.clearAllStudyTasks()
                    plan.schedule.forEach { task ->
                        repository.addStudyTask(
                            StudyTask(
                                subject = task.subject,
                                timeSlot = task.timeSlot,
                                taskDescription = task.taskDescription,
                                isCompleted = false
                            )
                        )
                    }
                }
                _plannerUiState.value = PlannerUiState.Success
            } catch (e: Exception) {
                _plannerUiState.value = PlannerUiState.Error(e.localizedMessage ?: "Failed to generate schedule")
            }
        }
    }

    fun toggleTaskCompletion(task: StudyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateStudyTask(updated)
            FirebaseService.syncStudyTaskToCloud(updated)
        }
    }

    fun addManualTask(subject: String, timeSlot: String, taskDescription: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newTask = StudyTask(
                subject = subject,
                timeSlot = timeSlot,
                taskDescription = taskDescription,
                isCompleted = false
            )
            repository.addStudyTask(newTask)
            FirebaseService.syncStudyTaskToCloud(newTask)
        }
    }

    fun deleteTask(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudyTask(id)
        }
    }

    // --- Flashcards Management ---

    fun generateNewFlashcards(topic: String) {
        val apiKey = BuildConfig.GEMINI_API_KEY ?: ""
        _flashcardUiState.value = FlashcardUiState.Loading

        viewModelScope.launch {
            try {
                val createdFlashcards = withContext(Dispatchers.IO) {
                    GeminiClient.generateFlashcards(topic = topic, apiKey = apiKey)
                }
                _flashcardUiState.value = FlashcardUiState.Success(createdFlashcards)
            } catch (e: Exception) {
                _flashcardUiState.value = FlashcardUiState.Error(e.localizedMessage ?: "Could not build flashcards")
            }
        }
    }

    fun resetFlashcards() {
        _flashcardUiState.value = FlashcardUiState.Idle
    }

    fun clearAllUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllData()
            _chatUiState.value = ChatUiState.Idle
            _quizUiState.value = QuizUiState.Idle
            _plannerUiState.value = PlannerUiState.Idle
            _flashcardUiState.value = FlashcardUiState.Idle
        }
    }
}
