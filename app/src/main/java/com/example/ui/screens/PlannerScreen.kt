package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.KaranEDUViewModel
import com.example.ui.PlannerUiState
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    viewModel: KaranEDUViewModel,
    onNavigateBack: () -> Unit
) {
    var subjectsInput by remember { mutableStateOf("Mathematics, Physics, Essay Writing") }
    var studyHours by remember { mutableFloatStateOf(4f) }
    
    val plannerUiState by viewModel.plannerUiState.collectAsState()
    val studyTasks by viewModel.studyTasks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AccentAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "AI Study Planner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // Setup controls at top if list is empty or they choose to recreate
            if (studyTasks.isEmpty() && plannerUiState !is PlannerUiState.Loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = AccentAmber.copy(alpha = 0.12f),
                        shape = CircleShape,
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.EventNote,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Build Custom AI Schedule",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "AI analyzes your subjects and study time parameters to output structured revision sessions, timeframes, and focused checklist goals.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    OutlinedTextField(
                        value = subjectsInput,
                        onValueChange = { subjectsInput = it },
                        label = { Text("List subjects you study (comma-separated)") },
                        placeholder = { Text("Chemistry, English Lit, Programming") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subjects_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentAmber
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Slider for study hours selection
                    Text(
                        text = "Free Revision Hours: ${studyHours.toInt()} Hours",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Slider(
                        value = studyHours,
                        onValueChange = { studyHours = it },
                        valueRange = 2f..10f,
                        steps = 7, // 2, 3, 4, 5, 6, 7, 8, 9, 10
                        colors = SliderDefaults.colors(
                            thumbColor = AccentAmber,
                            activeTrackColor = AccentAmber,
                            inactiveTrackColor = AccentAmber.copy(alpha = 0.24f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("hours_slider")
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val subjectsList = subjectsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            if (subjectsList.isNotEmpty()) {
                                viewModel.generatePlannerSchedule(subjectsList, studyHours.toInt())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_schedule_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Generate Study Plan",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (plannerUiState is PlannerUiState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentAmber)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Orchestrating custom study schedule matrix...",
                            fontWeight = FontWeight.Bold,
                            color = AccentAmber,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Render List of active study tasks (Checklist!)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Study Checklist",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val completed = studyTasks.count { it.isCompleted }
                            Text(
                                text = "Progress: $completed of ${studyTasks.size} slots completed",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                viewModel.generatePlannerSchedule(
                                    subjectsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    studyHours.toInt()
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerate Plan",
                                tint = AccentAmber
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(studyTasks) { index, task ->
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.toggleTaskCompletion(task) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF10B981)
                                        ),
                                        modifier = Modifier.testTag("task_checkbox_$index")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (task.isCompleted) Color(0xFF10B981).copy(alpha = 0.15f) else AccentOrange.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = task.subject,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (task.isCompleted) Color(0xFF10B981) else AccentOrange,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                imageVector = Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = task.timeSlot,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.outline,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = task.taskDescription,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    
                                    IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete study task slot",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons bottom
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.generatePlannerSchedule(
                                    subjectsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    studyHours.toInt()
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Re-Generate", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1.0f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
