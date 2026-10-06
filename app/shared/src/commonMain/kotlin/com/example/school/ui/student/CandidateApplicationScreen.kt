package com.example.e_voting.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.e_voting.ui.BackgroundOffWhite
import com.example.e_voting.ui.PrimaryOrange
import com.example.e_voting.ui.SecondaryNavy
import com.example.e_voting.ui.TextDark
import com.example.e_voting.ui.TextMuted

@Composable
fun CandidateApplicationScreen(
    studentId: String
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundOffWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Candidate Application",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = SecondaryNavy
            )
            Text(
                text = "Logged in as Student ID: $studentId",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )

            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Submit your vetting details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(Modifier.height(8.dp))
                    Text("Fill out the role application form below to run for an official student leadership position.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { /* Handle submission */ },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply Now", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}