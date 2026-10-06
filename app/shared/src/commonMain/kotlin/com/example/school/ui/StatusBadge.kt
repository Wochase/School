package com.example.e_voting.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatusBadge(status: String) {
    val (background, foreground) = when (status) {
        "Open" -> StatusGreen to Color.White
        "Results Published" -> PrimaryOrange to Color.White
        "Closed" -> Color.Gray to Color.White
        else -> StatusAmber to Color.White
    }
    Surface(color = background, shape = RoundedCornerShape(16.dp)) {
        Text(
            status,
            modifier = androidx.compose.ui.Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = foreground,
        )
    }
}
