package com.example.school.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.school.ui.*

@Composable
fun AdminHelpScreen(
) {
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundOffWhite)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Welcome to the Admin Portal", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "This guide helps administrators configure, supervise, and secure electronic elections efficiently.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            }

            item {
                GuideSection(
                    stepNumber = "1",
                    title = "Open the candidate application period",
                    description = "Choose Open vetting, enter the election title and academic year, publish a student announcement, then add the roles students can apply for."
                )
            }

            item {
                GuideSection(
                    stepNumber = "2",
                    title = "Review applications",
                    description = "Open Candidate review to read each student's suitability statement and approve or reject the application. Students receive the decision in their inbox."
                )
            }

            item {
                GuideSection(
                    stepNumber = "3",
                    title = "Close vetting and build the ballot",
                    description = "When applications are finished, close the vetting stage from Election stages. Set up the election and voting dates; only approved applicants are placed on the ballot."
                )
            }

            item {
                GuideSection(
                    stepNumber = "4",
                    title = "Run the voting lifecycle",
                    description = "From Election stages or Election lifecycle, start a scheduled election early if needed, close voting when it ends, then publish results. Starting early opens voting immediately."
                )
            }

            item {
                GuideSection(
                    stepNumber = "5",
                    title = "Use the inbox and audit log",
                    description = "Inbox collects applications needing review and election activity. Audit logs show real actions as they occur; an empty log means no activity has been recorded yet."
                )
            }

            item {
                GuideSection(
                    stepNumber = "6",
                    title = "Find sections from any page",
                    description = "The navigation panel stays visible while you move through the admin portal. Use it to return to election stages, review candidates, open the inbox, or read this guide."
                )
            }
        }
    }
}

@Composable
fun GuideSection(stepNumber: String, title: String, description: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                color = PrimaryOrange,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(stepNumber, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
    }
}
