package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EduAIViewModel
import com.example.data.FirebaseService
import com.example.data.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: EduAIViewModel,
    onNavigateToDashboard: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("High School") }
    var selectedFocus by remember { mutableStateOf("STEM / Science") }
    
    val grades = listOf("Middle School", "High School", "College / University", "General / Adult")
    val focusAreas = listOf("STEM / Science", "Humanities / Arts", "Languages", "Business / Commerce")

    var showError by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var authModeCloud by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isAuthLoading by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var authSuccessMsg by remember { mutableStateOf<String?>(null) }

    val currentUser = FirebaseService.currentUser
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            authModeCloud = true
            emailInput = currentUser.email ?: ""
            authSuccessMsg = "Connected securely to KaranEDU Cloud!"
            scope.launch {
                try {
                    FirebaseService.firestore?.collection("users")?.document(currentUser.uid)?.get()?.addOnSuccessListener { doc ->
                        if (doc != null && doc.exists()) {
                            name = doc.getString("name") ?: name
                            selectedGrade = doc.getString("grade") ?: selectedGrade
                            selectedFocus = doc.getString("focusArea") ?: selectedFocus
                        }
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Branding Icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "EduAI Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "EduAI",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
            )
            
            Text(
                text = "Personalized Learning Tutor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            // SDG 4 Badge Card
            Surface(
                color = Color(0xFFF97316).copy(alpha = 0.15f),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .wrapContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SDG 4: Quality Education",
                        color = Color(0xFFF97316),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Firebase Cloud Gmail Authentication and Database Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (authSuccessMsg != null) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Cloud Sync & Gmail Auth",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (authSuccessMsg != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "Synchronize your grades, study planner and quiz progress securely with Firebase Firestore.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Cloud Sync Engine",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = authModeCloud,
                            onCheckedChange = { authModeCloud = it }
                        )
                    }

                    if (authModeCloud) {
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Gmail or School Email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Secure Cloud Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isAuthLoading) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                            isAuthLoading = true
                                            authError = null
                                            authSuccessMsg = null
                                            FirebaseService.auth?.signInWithEmailAndPassword(emailInput.trim(), passwordInput)
                                                ?.addOnSuccessListener { result ->
                                                    isAuthLoading = false
                                                    authSuccessMsg = "Successfully connected with Firebase Auth."
                                                    // Load firestore profile fields if any
                                                    FirebaseService.firestore?.collection("users")?.document(result.user?.uid ?: "")?.get()
                                                        ?.addOnSuccessListener { doc ->
                                                            if (doc != null && doc.exists()) {
                                                                name = doc.getString("name") ?: name
                                                                selectedGrade = doc.getString("grade") ?: selectedGrade
                                                                selectedFocus = doc.getString("focusArea") ?: selectedFocus
                                                            }
                                                        }
                                                }?.addOnFailureListener { exception ->
                                                    isAuthLoading = false
                                                    authError = exception.localizedMessage ?: "Gmail Auth failed."
                                                }
                                        } else {
                                            authError = "Please enter both Email and Password."
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Sign In", fontSize = 13.sp)
                                }

                                Button(
                                    onClick = {
                                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                            isAuthLoading = true
                                            authError = null
                                            authSuccessMsg = null
                                            FirebaseService.auth?.createUserWithEmailAndPassword(emailInput.trim(), passwordInput)
                                                ?.addOnSuccessListener { result ->
                                                    isAuthLoading = false
                                                    authSuccessMsg = "New Firebase Gmail account generated!"
                                                }?.addOnFailureListener { exception ->
                                                    isAuthLoading = false
                                                    authError = exception.localizedMessage ?: "Registration failed."
                                                }
                                        } else {
                                            authError = "Please enter both Email and Password."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Register", fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Success/Error Info Banner
                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (authSuccessMsg != null) {
                        Text(
                            text = authSuccessMsg ?: "",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Main input card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Set Up Your Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (it.isNotEmpty()) showError = false
                        },
                        label = { Text("Your full name") },
                        isError = showError,
                        singleLine = true,
                        placeholder = { Text("Enter name...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    if (showError) {
                        Text(
                            text = "Name is required to personalize tutor.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.0.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Academic Grade / Level",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Grade choices
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        grades.forEach { grade ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selectedGrade == grade) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedGrade == grade,
                                    onClick = { selectedGrade = grade }
                                )
                                Text(
                                    text = grade,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Study Focus Area",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Focus choices
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        focusAreas.forEach { focus ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (selectedFocus == focus) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFocus == focus,
                                    onClick = { selectedFocus = focus }
                                )
                                Text(
                                    text = focus,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (name.trim().isEmpty()) {
                                showError = true
                            } else {
                                viewModel.createProfile(
                                    name = name,
                                    grade = selectedGrade,
                                    focusArea = selectedFocus
                                )
                                onNavigateToDashboard()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Begin Learning Journey",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "SDG 4 promotes inclusive and equitable quality education and opportunities for all.",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
