package com.example.school.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import school.app.shared.generated.resources.Res
import school.app.shared.generated.resources.schoollogo
import school.app.shared.generated.resources.username
import school.app.shared.generated.resources.password
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (userRole: String, studentId: String?) -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Authenticated) {
            val auth = (authState as AuthUiState.Authenticated).response
            onLoginSuccess(auth.userRole, auth.studentId)
        }
    }

    // Outer background fills the entire available window/screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE1E1E1)),
        contentAlignment = Alignment.Center,
    ) {
        // Scrollable container to handle smaller screens or window resizing safely
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .widthIn(max = 577.dp) // Caps maximum width to match your design
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.White)
                .verticalScroll(scrollState)
                .padding(horizontal = 40.dp, vertical = 32.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
            ) {
                // 1. School Logo Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.schoollogo),
                        contentDescription = "School Logo",
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(140.dp),
                    )
                }

                // 2. Screen Title
                Text(
                    text = "Login",
                    color = Color.Black,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp),
                )

                // 3. Email / Student ID Field
                Text(
                    text = "Email",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.username),
                            contentDescription = "Username Icon",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Password Field
                Text(
                    text = "Password",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 6.dp),
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Image(
                            painter = painterResource(Res.drawable.password),
                            contentDescription = "Password Icon",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                )

                if (authState is AuthUiState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (authState as AuthUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // 5. Login Button
                Button(
                    onClick = { viewModel.login(username, password) },
                    enabled = authState !is AuthUiState.Authenticating,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009DFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    if (authState is AuthUiState.Authenticating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    } else {
                        Text(
                            text = "Login",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 6. Security Warning Notice
                Text(
                    text = "WARNING: Unauthorized access to this system is forbidden and will be prosecuted by law.",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Text(
                    text = "By accessing this system, you agree that your actions may be monitored if unauthorized usage is suspected.",
                    color = Color.DarkGray,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(bottom = 20.dp),
                )

                // 7. Administrative Queries Footer
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                            append("Administrative Queries: ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF00BBFF), fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                            append("admin@brainybairn.edu.gh")
                        }
                    },
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
        }
    }
}
