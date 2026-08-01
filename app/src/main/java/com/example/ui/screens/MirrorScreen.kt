package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FlowSession
import com.example.ui.theme.*
import com.example.viewmodel.FlowViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MirrorScreen(
    viewModel: FlowViewModel,
    modifier: Modifier = Modifier
) {
    val sessions by viewModel.allSessions.collectAsState()
    var showClearConfirm by remember { mutableStateOf(false) }

    val totalTimeMin = remember(sessions) {
        sessions.sumOf { it.durationSeconds } / 60
    }

    val avgFlowScore = remember(sessions) {
        if (sessions.isEmpty()) 0f else sessions.map { it.flowScore }.average().toFloat()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Title
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "THE MIRROR",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quantified Awareness & Historical Focus",
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Theme Selector
        item {
            val currentThemeMode by viewModel.themeMode.collectAsState()
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FLOW AMBIENT MOODS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Align your physical environment with the digital canvas to prevent eye strain and cultivate presence.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        lineHeight = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    // Options row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val modes = listOf(
                            Triple(com.example.viewmodel.FlowThemeMode.CIRCADIAN, "Circadian", "Auto-shift"),
                            Triple(com.example.viewmodel.FlowThemeMode.DAWN, "Dawn", "Warm light"),
                            Triple(com.example.viewmodel.FlowThemeMode.DUSK, "Dusk", "Slate gray"),
                            Triple(com.example.viewmodel.FlowThemeMode.MIDNIGHT, "Midnight", "Amoled dark")
                        )
                        
                        modes.forEach { (mode, title, desc) ->
                            val isSelected = currentThemeMode == mode
                            val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val cardBg = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                            
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 85.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cardBg)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setThemeMode(mode) }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val emoji = when (mode) {
                                        com.example.viewmodel.FlowThemeMode.CIRCADIAN -> "🌿"
                                        com.example.viewmodel.FlowThemeMode.DAWN -> "🌅"
                                        com.example.viewmodel.FlowThemeMode.DUSK -> "🌇"
                                        com.example.viewmodel.FlowThemeMode.MIDNIGHT -> "🌌"
                                    }
                                    Text(text = emoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Time
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(Icons.Filled.Timer, contentDescription = "Total Time", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Focus Time", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(
                            text = if (totalTimeMin >= 60) "${totalTimeMin / 60}h ${totalTimeMin % 60}m" else "${totalTimeMin}m",
                            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Total Sessions
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(Icons.Filled.History, contentDescription = "Sessions", tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sessions", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(
                            text = "${sessions.size}",
                            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Average Flow Score
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(Icons.Filled.Insights, contentDescription = "Flow Score", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Flow Index", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                        Text(
                            text = String.format("%.1f/5", avgFlowScore),
                            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // The Custom Flow Compass Scatter Map (Mind state matrix)
        item {
            val axisColor = MaterialTheme.colorScheme.outline
            val gridColor = MaterialTheme.colorScheme.surfaceVariant
            val primaryAccent = MaterialTheme.colorScheme.primary
            val secondaryAccent = MaterialTheme.colorScheme.secondary

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mind State Mapping (Challenge vs Skill)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Serif
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dots represent logged sessions. Ideally clusters fall in the diagonal Flow channel.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 5x5 Scatter Map Grid
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw axes
                            drawLine(axisColor, Offset(0f, h), Offset(w, h), strokeWidth = 2f)
                            drawLine(axisColor, Offset(0f, 0f), Offset(0f, h), strokeWidth = 2f)

                            // Draw grid lines and labels (5 spaces)
                            val stepX = w / 5f
                            val stepY = h / 5f

                            for (i in 1..4) {
                                val xPos = i * stepX
                                val yPos = i * stepY
                                // Vertical Grid Line
                                drawLine(gridColor.copy(alpha = 0.5f), Offset(xPos, 0f), Offset(xPos, h), strokeWidth = 1f)
                                // Horizontal Grid Line
                                drawLine(gridColor.copy(alpha = 0.5f), Offset(0f, yPos), Offset(w, yPos), strokeWidth = 1f)
                            }

                            // Group sessions by (skill, challenge) to draw weighted nodes
                            val grouped = sessions.groupBy { it.skillLevel to it.challengeLevel }

                            grouped.forEach { (coords, list) ->
                                val (s, c) = coords
                                // Convert 1..5 values to grid coordinates (with 1 in bottom-left and 5 in top-right)
                                val normX = ((s - 1) / 4f) * w
                                val normY = h - (((c - 1) / 4f) * h)

                                // Prevent boundary clip
                                val drawX = normX.coerceIn(20f, w - 20f)
                                val drawY = normY.coerceIn(20f, h - 20f)

                                // Node weight size
                                val nodeWeight = list.size
                                val radius = (12f + nodeWeight * 5f).coerceAtMost(35f)

                                // Draw circular aura glow
                                drawCircle(
                                    color = if (s == c) secondaryAccent.copy(alpha = 0.4f) else primaryAccent.copy(alpha = 0.4f),
                                    radius = radius + 8f,
                                    center = Offset(drawX, drawY)
                                )

                                // Draw solid node center
                                drawCircle(
                                    color = if (s == c) secondaryAccent else primaryAccent,
                                    radius = radius,
                                    center = Offset(drawX, drawY)
                                )
                            }
                        }

                        // Grid Labels overlay
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("Skill Lvl 1", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp, modifier = Modifier.padding(bottom = 2.dp))
                            Text("Flow Channel (Diagonal) ➔", color = MaterialTheme.colorScheme.secondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
                            Text("Skill Lvl 5", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 8.sp, modifier = Modifier.padding(bottom = 2.dp))
                        }
                        
                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                            contentAlignment = Alignment.TopStart
                        ) {
                            Text("Anxiety\n(Challenge Lvl 5)", color = MaterialTheme.colorScheme.primary, fontSize = 8.sp)
                        }

                        Box(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 2.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Text("Boredom\n(Skill Lvl 5)", color = MaterialTheme.colorScheme.primary, fontSize = 8.sp, textAlign = TextAlign.End, modifier = Modifier.padding(bottom = 14.dp))
                        }
                    }
                }
            }
        }

        // Sessions Log Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FOCUS CHRONICLES",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                if (sessions.isNotEmpty()) {
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .clickable { showClearConfirm = true }
                            .padding(4.dp)
                    )
                }
            }
        }

        // Sessions List empty state
        if (sessions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = "No sessions yet",
                            tint = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No focus chronicles recorded yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Complete your first entry ritual to see mirrors of your focus.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                SessionItemRow(session = session, onDelete = { viewModel.deleteSessionItem(session) })
            }
        }
    }

    // Clear History Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Erase Focus Chronicles", color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Serif) },
            text = { Text("Are you absolutely sure you want to clear your local history? This cannot be undone.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Keep", color = MaterialTheme.colorScheme.onSurface)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun SessionItemRow(
    session: FlowSession,
    onDelete: () -> Unit
) {
    val dateString = remember(session.timestamp) {
        val formatter = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault())
        formatter.format(Date(session.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val displayedIntent = if (session.intent.isBlank() || session.intent == "Center" || session.intent == "Vô tâm / Center") {
                        stringResource(R.string.intent_center)
                    } else {
                        session.intent
                    }
                    Text(
                        text = displayedIntent,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = dateString,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("delete_session_${session.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Session",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Focus time tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${session.durationSeconds / 60}m ${session.durationSeconds % 60}s",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                // Metacognitive Flow score
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Flow Index: ",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "${session.flowScore}/5",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (session.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = session.notes,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = FontFamily.Serif
                    )
                )
            }
        }
    }
}
