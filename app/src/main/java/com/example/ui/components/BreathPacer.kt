package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Breathing phase state
 */
enum class PacerPhase(val label: String, val instruction: String) {
    IN("INHALE", "Breathe in deeply through your nose..."),
    HOLD_IN("HOLD", "Hold breath calmly..."),
    OUT("EXHALE", "Release air slowly through your mouth..."),
    HOLD_OUT("PAUSE", "Rest in stillness...")
}

/**
 * Breathing Pattern configuration
 */
data class BreathPattern(
    val id: String,
    val name: String,
    val description: String,
    val inhaleSeconds: Int,
    val holdInSeconds: Int,
    val exhaleSeconds: Int,
    val holdOutSeconds: Int
)

val PRESET_BREATH_PATTERNS = listOf(
    BreathPattern("box", "Box (4-4-4-4)", "Equal duration for focus & grounding", 4, 4, 4, 4),
    BreathPattern("relax", "4-7-8 Relax", "Calms nervous system for deep focus", 4, 7, 8, 0),
    BreathPattern("calm", "Coherent (5-5)", "Balanced heart rate variability", 5, 0, 5, 0),
    BreathPattern("quick", "Quick Centering", "Fast 3-3-3 reset before work", 3, 3, 3, 0)
)

/**
 * Compose Breath Pacer Component:
 * Features a minimalist animated circle expanding and contracting to guide
 * users through deep breathing exercises for pre-focus sessions.
 */
@Composable
fun BreathPacer(
    modifier: Modifier = Modifier,
    onSessionCompleted: (() -> Unit)? = null,
    patterns: List<BreathPattern> = PRESET_BREATH_PATTERNS
) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant

    var selectedPattern by remember { mutableStateOf(patterns.first()) }
    var isPacingRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(PacerPhase.IN) }
    var currentCycleCount by remember { mutableIntStateOf(0) }
    val totalCyclesTarget = 4

    // Animated Scale of the Circle (0.75f to 1.3f)
    val circleScale = remember { Animatable(0.75f) }
    // Animated Progress Arc (0f to 1f per phase)
    var phaseProgressFraction by remember { mutableFloatStateOf(0f) }

    // Ambient background idle pulsing when paused
    val infiniteTransition = rememberInfiniteTransition(label = "IdleGlowPulse")
    val idlePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    // Main Breathing Loop Engine
    LaunchedEffect(isPacingRunning, selectedPattern) {
        if (!isPacingRunning) {
            circleScale.snapTo(0.75f)
            phaseProgressFraction = 0f
            currentPhase = PacerPhase.IN
            return@LaunchedEffect
        }

        while (isActive && isPacingRunning) {
            // Phase 1: INHALE (Expand from 0.75f to 1.30f)
            currentPhase = PacerPhase.IN
            val inhaleMs = selectedPattern.inhaleSeconds * 1000
            if (inhaleMs > 0) {
                // Animate progress arc concurrently
                val progressAnim = Animatable(0f)
                launch {
                    progressAnim.animateTo(1f, tween(inhaleMs, easing = LinearEasing)) {
                        phaseProgressFraction = value
                    }
                }
                circleScale.animateTo(
                    targetValue = 1.30f,
                    animationSpec = tween(durationMillis = inhaleMs, easing = EaseInOutSine)
                )
            }

            // Phase 2: HOLD IN
            if (selectedPattern.holdInSeconds > 0 && isPacingRunning) {
                currentPhase = PacerPhase.HOLD_IN
                val holdInMs = selectedPattern.holdInSeconds * 1000
                val progressAnim = Animatable(0f)
                launch {
                    progressAnim.animateTo(1f, tween(holdInMs, easing = LinearEasing)) {
                        phaseProgressFraction = value
                    }
                }
                delay(holdInMs.toLong())
            }

            // Phase 3: EXHALE (Contract from 1.30f to 0.75f)
            if (isPacingRunning) {
                currentPhase = PacerPhase.OUT
                val exhaleMs = selectedPattern.exhaleSeconds * 1000
                if (exhaleMs > 0) {
                    val progressAnim = Animatable(0f)
                    launch {
                        progressAnim.animateTo(1f, tween(exhaleMs, easing = LinearEasing)) {
                            phaseProgressFraction = value
                        }
                    }
                    circleScale.animateTo(
                        targetValue = 0.75f,
                        animationSpec = tween(durationMillis = exhaleMs, easing = EaseInOutSine)
                    )
                }
            }

            // Phase 4: HOLD OUT / PAUSE
            if (selectedPattern.holdOutSeconds > 0 && isPacingRunning) {
                currentPhase = PacerPhase.HOLD_OUT
                val holdOutMs = selectedPattern.holdOutSeconds * 1000
                val progressAnim = Animatable(0f)
                launch {
                    progressAnim.animateTo(1f, tween(holdOutMs, easing = LinearEasing)) {
                        phaseProgressFraction = value
                    }
                }
                delay(holdOutMs.toLong())
            }

            if (isPacingRunning) {
                currentCycleCount++
                if (currentCycleCount >= totalCyclesTarget) {
                    onSessionCompleted?.invoke()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pattern Selection Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            patterns.forEach { pattern ->
                val isSelected = selectedPattern.id == pattern.id
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSelected) ZenAmber else ZenStoneGrey)
                        .clickable {
                            selectedPattern = pattern
                            isPacingRunning = false
                            currentCycleCount = 0
                        }
                        .padding(horizontal = 14.dp)
                        .wrapContentSize(Alignment.Center)
                        .testTag("breath_pattern_chip_${pattern.id}")
                ) {
                    Text(
                        text = pattern.name,
                        color = if (isSelected) ZenBlack else ZenTextPrimary,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Animated Breathing Sphere Frame
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("breath_pacer_circle"),
            contentAlignment = Alignment.Center
        ) {
            val effectiveScale = if (isPacingRunning) circleScale.value else idlePulseScale

            // Outer Soft Aura Blur Glow
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .graphicsLayer(
                        scaleX = effectiveScale * 1.35f,
                        scaleY = effectiveScale * 1.35f
                    )
                    .blur(36.dp)
                    .clip(CircleShape)
                    .background(ZenAmber.copy(alpha = if (isPacingRunning) 0.28f else 0.12f))
            )

            // Outer Circular Track & Phase Sweep Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 6.dp.toPx()
                val radius = (size.minDimension - strokeWidth - 12.dp.toPx()) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Outer boundary track
                drawCircle(
                    color = ZenStoneGrey,
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Current Phase Sweep Arc
                if (isPacingRunning) {
                    val sweepAngle = phaseProgressFraction * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(ZenAmber, ZenAmberDim, ZenAmber)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2f, radius * 2f),
                        style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Expanding & Contracting Inner Breath Core
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .graphicsLayer(
                        scaleX = effectiveScale,
                        scaleY = effectiveScale
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ZenAmber.copy(alpha = 0.85f),
                                ZenAmberDim.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.radialGradient(listOf(ZenAmber, Color.Transparent)),
                        shape = CircleShape
                    )
            )

            // Central Instruction & Text Overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (isPacingRunning) currentPhase.label else "READY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = ZenTextPrimary
                    )
                )

                if (isPacingRunning) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cycle $currentCycleCount / $totalCyclesTarget",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ZenTextSecondary,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phase Guidance Text Box
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = ZenStoneDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, ZenStoneGrey),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Air,
                    contentDescription = null,
                    tint = ZenAmber,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isPacingRunning) currentPhase.instruction else selectedPattern.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ZenTextPrimary,
                        fontSize = 13.sp
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Play / Pause / Reset Interactive Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    isPacingRunning = false
                    currentCycleCount = 0
                },
                modifier = Modifier
                    .size(46.dp)
                    .background(ZenStoneDark, CircleShape)
                    .border(1.dp, ZenStoneGrey, CircleShape)
                    .testTag("reset_breath_pacer_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Pacer",
                    tint = ZenTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = { isPacingRunning = !isPacingRunning },
                modifier = Modifier
                    .size(56.dp)
                    .background(ZenAmber, CircleShape)
                    .testTag("play_pause_breath_pacer_button")
            ) {
                Icon(
                    imageVector = if (isPacingRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPacingRunning) "Pause Pacer" else "Start Pacer",
                    tint = ZenBlack,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}
