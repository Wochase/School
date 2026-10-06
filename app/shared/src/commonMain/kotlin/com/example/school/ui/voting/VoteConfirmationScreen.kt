package com.example.e_voting.ui.voting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_voting.ui.*

@Composable
fun VoteConfirmationScreen(
    confirmationCode: String,
    onReturnToDashboard: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundOffWhite)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Success Badge Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(StatusGreen, shape = RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("Vote Cast Successfully!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SecondaryNavy)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your choices have been securely and anonymously recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Anonymous Confirmation Reference Code Box
                Surface(
                    color = PrimaryOrange.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Confirmation Reference Code", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = confirmationCode,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDarkOrange,
                            letterSpacing = 1.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Save this code for your records. It confirms your participation without linking your identity to your candidate selections.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onReturnToDashboard,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryNavy),
                ) {
                    Text("Return to Dashboard / Exit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
