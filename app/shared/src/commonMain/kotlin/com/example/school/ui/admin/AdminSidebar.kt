package com.example.school.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import school.app.shared.generated.resources.Res
import school.app.shared.generated.resources.schoollogo
import org.jetbrains.compose.resources.painterResource

@Composable
fun AdminSidebar(
    activeTab: String,
    onNavigateStages: () -> Unit,
    onNavigateVetting: () -> Unit,
    onNavigateCandidates: () -> Unit,
    onNavigateCycle: () -> Unit,
    onNavigateGuide: () -> Unit,
    onNavigateInbox: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(color = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // School Logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.schoollogo),
                    contentDescription = "School Logo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Text(
                    text = "ADMIN PORTAL",
                    color = Color.Black,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xff9a9a9a), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                AdminSidebarButton("Election Stages", activeTab == "Stages", onNavigateStages)
                Spacer(Modifier.height(8.dp))
                AdminSidebarButton("Open Vetting", activeTab == "Vetting", onNavigateVetting)
                Spacer(Modifier.height(8.dp))
                AdminSidebarButton("Candidate Review", activeTab == "Candidates", onNavigateCandidates)
                Spacer(Modifier.height(8.dp))
                AdminSidebarButton("Election Cycle", activeTab == "Cycle", onNavigateCycle)
                Spacer(Modifier.height(8.dp))
                AdminSidebarButton("Election Guide", activeTab == "Guide", onNavigateGuide)
                Spacer(Modifier.height(8.dp))
                AdminSidebarButton("Inbox", activeTab == "Inbox", onNavigateInbox)
            }
        }

        // Logout Button at Bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .width(180.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Logout",
                color = Color.White,
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun AdminSidebarButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(if (selected) 16.dp else 8.dp))
            .background(color = if (selected) Color(0xfff0efa7) else Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xff1f1f02) else Color.Black,
            style = TextStyle(fontSize = 16.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
