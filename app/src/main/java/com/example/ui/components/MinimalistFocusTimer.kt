package com.example.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimalist Focus Timer Component featuring:
 * - High precision circular progress bar with animated smooth sweep
 * - Pulsing central ambient core
 * - Customizable interval quick presets & fine-grained duration slider
 * - Play/Pause/Reset interactive controls
 */
@Composable
fun MinimalistFocusTimer(
    totalSeconds: Int,
    remainingSeconds: Int,
    isRunning: Boolean,
    onTogglePlayPause: () -> Unit,
    onReset: () -> Unit,
    onSelectInterval: (minutes: Int) -> Unit,
    modifier: Modifier = Modifier,
    presetIntervals: List<Int> = listOf(5, 10, 15, 25, 45, 60, 90, 120)
) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant

    var showCustomIntervalSlider by remember { mutableStateOf(false) }

    // Progress fraction (0.0f to 1.0f)
    val progressFraction = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 600, easing = EaseInOutSine),
        label = "TimerProgress"
    )

    // Format time display
    val formattedTime = remember(remainingSeconds) {
        val hours = remainingSeconds / 3600
        val mins = (remainingSeconds % 3600) / 60
        val secs = remainingSeconds % 60
        if (hours > 0) {
            String.format("%d:%02d:%02d", hours, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    // Breathing pulse for visual centering when running
    val infiniteTransition = rememberInfiniteTransition(label = "TimerGlowPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Circular Progress Ring Frame
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("circular_focus_timer"),
            contentAlignment = Alignment.Center
        ) {
            // Ambient Outer Glow (Smooth Canvas Radial Gradient - no square bounds!)
            if (isRunning) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(scaleX = pulseScale * 1.15f, scaleY = pulseScale * 1.15f)
                ) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                0.0f to ZenAmber.copy(alpha = 0.25f),
                                0.6f to ZenAmberDim.copy(alpha = 0.12f),
                                1.0f to Color.Transparent
                            ),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )
                }
            }

            // Canvas drawing Circular Progress Bar
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Background Track Ring
                drawCircle(
                    color = ZenStoneGrey,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Active Elapsed Progress Arc (360 -> 0 degrees clockwise)
                val sweepAngle = animatedProgress * 360f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(ZenAmber, ZenAmberDim, ZenAmber)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2f, radius * 2f),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central Time & Status Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (isRunning) "FLOW IN PROGRESS" else "PAUSED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 2.sp,
                        color = if (isRunning) ZenAmber else ZenTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        color = ZenTextPrimary,
                        fontSize = if (remainingSeconds >= 3600) 42.sp else 50.sp
                    ),
                    modifier = Modifier.testTag("timer_countdown_text")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Play / Pause / Reset Quick Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier
                            .size(42.dp)
                            .background(ZenStoneDark, CircleShape)
                            .border(1.dp, ZenStoneGrey, CircleShape)
                            .testTag("reset_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = ZenTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(52.dp)
                            .background(ZenAmber, CircleShape)
                            .testTag("play_pause_timer_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause Timer" else "Play Timer",
                            tint = ZenBlack,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Customizable Intervals Section
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ZenStoneDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, ZenStoneGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = ZenAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Custom Interval: ${totalSeconds / 60} min",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = ZenTextPrimary
                            )
                        )
                    }

                    TextButton(
                        onClick = { showCustomIntervalSlider = !showCustomIntervalSlider },
                        modifier = Modifier.testTag("toggle_custom_interval_button")
                    ) {
                        Text(
                            text = if (showCustomIntervalSlider) "Presets" else "Adjust",
                            color = ZenAmber,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (showCustomIntervalSlider) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val currentMinutes = (totalSeconds / 60).coerceIn(1, 180)
                        Slider(
                            value = currentMinutes.toFloat(),
                            onValueChange = { onSelectInterval(it.toInt()) },
                            valueRange = 1f..180f,
                            colors = SliderDefaults.colors(
                                thumbColor = ZenAmber,
                                activeTrackColor = ZenAmber,
                                inactiveTrackColor = ZenStoneGrey
                            ),
                            modifier = Modifier.testTag("custom_interval_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("1m", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                            Text("45m", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                            Text("90m", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                            Text("180m (3h)", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetIntervals.forEach { mins ->
                            val isSelected = (totalSeconds / 60) == mins
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .widthIn(min = 50.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(if (isSelected) ZenAmber else ZenStoneGrey)
                                    .clickable { onSelectInterval(mins) }
                                    .padding(horizontal = 12.dp)
                                    .wrapContentSize(Alignment.Center)
                                    .testTag("interval_chip_$mins")
                            ) {
                                Text(
                                    text = "${mins}m",
                                    color = if (isSelected) ZenBlack else ZenTextPrimary,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
