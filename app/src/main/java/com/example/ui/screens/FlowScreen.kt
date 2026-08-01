package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.BreathPhase
import com.example.viewmodel.FocusState
import com.example.viewmodel.FlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FlowScreen(
    viewModel: FlowViewModel,
    modifier: Modifier = Modifier
) {
    val focusState by viewModel.focusState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = focusState,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "FocusStateTransition"
        ) { state ->
            when (state) {
                FocusState.NONE -> IdleSetupView(viewModel)
                FocusState.COMPASS -> FlowCompassView(viewModel)
                FocusState.RITUAL -> ThresholdRitualView(viewModel)
                FocusState.DEEP_SHIELD -> DeepShieldView(viewModel)
                FocusState.REFLECTION -> MetacognitiveMirrorView(viewModel)
            }
        }

        SmartPromptOverlay(viewModel)
    }
}

@Composable
fun IdleSetupView(viewModel: FlowViewModel) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    val currentIntent by viewModel.currentIntent.collectAsState()
    val selectedDuration by viewModel.selectedDurationMinutes.collectAsState()
    val suggestedIntents by viewModel.suggestedIntents.collectAsState()
    val communityStats by viewModel.communityStats.collectAsState()
    val aiMantra by viewModel.aiMantra.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val presetIntents = suggestedIntents.ifEmpty {
        listOf("Deep Reading", "Creative Writing", "Zen Meditation", "Strategic Planning")
    }

    val durations = listOf(5, 10, 15, 25, 45, 60, 90, 120)
    var showCustomSlider by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "FLOW",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 12.sp,
                    color = ZenAmber
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Stillness in Action.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 3.sp,
                    color = ZenTextSecondary
                ),
                textAlign = TextAlign.Center
            )
        }

        // Setup Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What is your single intent?",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ZenTextPrimary,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentIntent,
                onValueChange = { viewModel.setIntent(it) },
                placeholder = {
                    Text(
                        stringResource(R.string.intent_placeholder),
                        color = ZenTextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ZenTextPrimary,
                    unfocusedTextColor = ZenTextPrimary,
                    focusedBorderColor = ZenAmber,
                    unfocusedBorderColor = ZenStoneGrey,
                    focusedContainerColor = ZenStoneDark,
                    unfocusedContainerColor = ZenBlack
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                presetIntents.forEach { intent ->
                    val isSelected = currentIntent == intent
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) ZenAmber else ZenStoneGrey)
                            .border(1.dp, if (isSelected) ZenAmber else ZenStoneLight, RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.setIntent(intent)
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = intent,
                            color = if (isSelected) ZenBlack else ZenTextPrimary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }

            // Firebase AI Focus Mentor Card
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ZenStoneDark,
                border = BorderStroke(1.dp, ZenStoneGrey),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ZenAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Firebase AI Focus Mentor",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ZenTextPrimary
                                )
                            )
                        }

                        TextButton(
                            onClick = { viewModel.generateAiMantra() },
                            enabled = !isAiLoading
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = ZenAmber,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Spark Mantra",
                                    color = ZenAmber,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    if (!aiMantra.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${aiMantra}\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Serif,
                                color = ZenAmberDim
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Duration Selector Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Focus Duration: $selectedDuration min",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ZenTextPrimary,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    )
                )

                TextButton(onClick = { showCustomSlider = !showCustomSlider }) {
                    Text(
                        text = if (showCustomSlider) "Presets" else "Custom",
                        color = ZenAmber,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            if (showCustomSlider) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = selectedDuration.toFloat(),
                        onValueChange = { viewModel.setDuration(it.toInt().coerceIn(1, 180)) },
                        valueRange = 1f..180f,
                        colors = SliderDefaults.colors(
                            thumbColor = ZenAmber,
                            activeTrackColor = ZenAmber,
                            inactiveTrackColor = ZenStoneGrey
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 min", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                        Text("90 min", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                        Text("180 min (3h)", style = MaterialTheme.typography.labelSmall, color = ZenTextSecondary)
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    durations.forEach { mins ->
                        val isSelected = selectedDuration == mins
                        Box(
                            modifier = Modifier
                                .height(46.dp)
                                .widthIn(min = 52.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .background(if (isSelected) ZenAmber else ZenStoneDark)
                                .border(1.dp, if (isSelected) ZenAmber else ZenStoneGrey, RoundedCornerShape(23.dp))
                                .clickable { viewModel.setDuration(mins) }
                                .padding(horizontal = 14.dp)
                                .wrapContentSize(Alignment.Center)
                        ) {
                            Text(
                                text = when (mins) {
                                    5 -> "5m"
                                    10 -> "10m"
                                    15 -> "15m"
                                    25 -> "25m"
                                    45 -> "45m"
                                    60 -> "60m"
                                    90 -> "90m"
                                    120 -> "120m"
                                    else -> "${mins}m"
                                },
                                color = if (isSelected) ZenBlack else ZenTextPrimary,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Firebase Cloud Focus Pulse Banner
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ZenStoneDark.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, ZenStoneGrey.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = ZenMossGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Firebase Cloud Sync: Active",
                            style = MaterialTheme.typography.labelSmall.copy(color = ZenTextSecondary)
                        )
                    }

                    Text(
                        text = "🌐 ${communityStats.totalCloudFocusMinutes / 60}h Global Flow",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ZenAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Action Button
        Button(
            onClick = { viewModel.startCalibration() },
            colors = ButtonDefaults.buttonColors(
                containerColor = ZenAmber,
                contentColor = ZenBlack
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.enter_threshold),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}

@Composable
fun FlowCompassView(viewModel: FlowViewModel) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    val challenge by viewModel.challengeLevel.collectAsState()
    val skill by viewModel.skillLevel.collectAsState()

    val guidanceText = remember(challenge, skill) {
        when {
            challenge > skill -> {
                "Challenge > Skill (Anxiety)\n\nBoredom is quiet, but Anxiety is a storm. Break your first step into a simple, 5-minute task to calm the mind."
            }
            skill > challenge -> {
                "Skill > Challenge (Boredom)\n\nA quiet lake gathers dust. Add a time constraint or increase the speed to spark the competitive flame in your focus."
            }
            else -> {
                "Challenge = Skill (Flow State)\n\nPerfect equilibrium. Your skills match the obstacle. The mind is ready to dissolve into the work. Proceed."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "FLOW COMPASS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                    color = ZenAmber
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Calibrating the Mind",
                style = MaterialTheme.typography.bodyMedium.copy(color = ZenTextSecondary)
            )
        }

        // Calibration Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compass Diagram placeholder (simple elegant design)
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(1.dp, ZenStoneGrey, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    // Draw compass lines
                    drawLine(ZenStoneLight, Offset(0f, center.y), Offset(size.width, center.y))
                    drawLine(ZenStoneLight, Offset(center.x, 0f), Offset(center.x, size.height))
                    
                    // Draw user position dot based on skill vs challenge
                    val x = center.x + (skill - 3) * (size.width / 10f)
                    val y = center.y - (challenge - 3) * (size.height / 10f)
                    
                    drawCircle(
                        color = if (challenge == skill) ZenMossGreen else ZenAmber,
                        radius = 8f,
                        center = Offset(x, y)
                    )
                }
                Text(
                    text = "Skill ➔\nChallenge ↑",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = ZenTextSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 8.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Challenge Slider
            Text(
                text = "Challenge Level: $challenge",
                style = MaterialTheme.typography.bodyLarge.copy(color = ZenTextPrimary)
            )
            Slider(
                value = challenge.toFloat(),
                onValueChange = { viewModel.setChallengeLevel(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = ZenAmber,
                    activeTrackColor = ZenAmber,
                    inactiveTrackColor = ZenStoneGrey
                ),
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Skill Slider
            Text(
                text = "Your Skill Level: $skill",
                style = MaterialTheme.typography.bodyLarge.copy(color = ZenTextPrimary)
            )
            Slider(
                value = skill.toFloat(),
                onValueChange = { viewModel.setSkillLevel(it.toInt()) },
                valueRange = 1f..5f,
                steps = 3,
                colors = SliderDefaults.colors(
                    thumbColor = ZenMossGreen,
                    activeTrackColor = ZenMossGreen,
                    inactiveTrackColor = ZenStoneGrey
                ),
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Zen Advice Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZenStoneDark, RoundedCornerShape(16.dp))
                    .border(1.dp, ZenStoneGrey, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Text(
                    text = guidanceText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = ZenTextPrimary,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.cancelSessionFlow() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTextSecondary),
                border = BorderStroke(1.dp, ZenStoneGrey),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = { viewModel.proceedToRitual() },
                colors = ButtonDefaults.buttonColors(containerColor = ZenAmber, contentColor = ZenBlack),
                modifier = Modifier.weight(2f).height(50.dp)
            ) {
                Text("Proceed to Ritual")
            }
        }
    }
}

@Composable
fun ThresholdRitualView(viewModel: FlowViewModel) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    val isHolding by viewModel.holdingCircle.collectAsState()
    val phase by viewModel.breathPhase.collectAsState()
    val progress by viewModel.breathProgress.collectAsState()
    val count by viewModel.breathCount.collectAsState()

    val scope = rememberCoroutineScope()

    // Smooth pulsing values
    val infiniteTransition = rememberInfiniteTransition(label = "PulsingCircle")
    val idlePulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    // Dynamic scale depending on breathing phase & holding state
    val targetScale = when {
        !isHolding -> idlePulseScale
        phase == BreathPhase.IN -> 1.0f + (progress * 0.8f) // Expand to 1.8x
        phase == BreathPhase.HOLD -> 1.8f // Keep expanded
        phase == BreathPhase.OUT -> 1.8f - (progress * 0.8f) // Contract back to 1.0x
        else -> 1.0f
    }

    // Dynamic color glowing overlay
    val glowIntensity = if (isHolding && phase == BreathPhase.HOLD) progress else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "THE THRESHOLD",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                    color = ZenAmber
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sever the ties of the noisy world.",
                style = MaterialTheme.typography.bodyMedium.copy(color = ZenTextSecondary),
                textAlign = TextAlign.Center
            )
        }

        // Center Breathing Circle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = when {
                    !isHolding -> stringResource(R.string.breath_press_hold)
                    phase == BreathPhase.IN -> stringResource(R.string.breath_in)
                    phase == BreathPhase.HOLD -> stringResource(R.string.breath_hold)
                    phase == BreathPhase.OUT -> stringResource(R.string.breath_out)
                    else -> stringResource(R.string.breath_centering)
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Light,
                    color = if (isHolding) ZenAmber else ZenTextPrimary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isHolding) stringResource(R.string.breath_count_format, count) else stringResource(R.string.breath_anchor),
                style = MaterialTheme.typography.bodyMedium.copy(color = ZenTextSecondary)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // The Physical Touch Circle
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                viewModel.startHoldingBreath()
                                tryAwaitRelease()
                                viewModel.releaseHoldingBreath()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // outer ring progress
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val baseRadius = size.width / 2f - 20f

                    // Draw outer ring
                    drawCircle(
                        color = ZenStoneGrey,
                        radius = baseRadius,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )

                    // Draw complete ritual progress (overall progress based on counts 1..3 and phase)
                    val overallProgress = ((count - 1) + when (phase) {
                        BreathPhase.IN -> progress * 0.33f
                        BreathPhase.HOLD -> 0.33f + progress * 0.33f
                        BreathPhase.OUT -> 0.66f + progress * 0.33f
                        else -> 1f
                    }) / 3f

                    drawArc(
                        color = ZenAmber,
                        startAngle = -90f,
                        sweepAngle = overallProgress * 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
                        size = androidx.compose.ui.geometry.Size(baseRadius * 2, baseRadius * 2),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f)
                    )
                }

                // Inner breathing sphere
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(
                            scaleX = targetScale,
                            scaleY = targetScale
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    ZenAmber.copy(alpha = 0.9f + (glowIntensity * 0.1f)),
                                    ZenAmberDim.copy(alpha = 0.6f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.SelfImprovement,
                        contentDescription = "Zen Breath",
                        tint = ZenBlack,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Cancel Button
        OutlinedButton(
            onClick = { viewModel.cancelSessionFlow() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTextSecondary),
            border = BorderStroke(1.dp, ZenStoneGrey),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Cancel Intent")
        }
    }
}

@Composable
fun DeepShieldView(viewModel: FlowViewModel) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val gammaEnabled by viewModel.gammaEnabled.collectAsState()
    val rainEnabled by viewModel.rainEnabled.collectAsState()
    val intent by viewModel.currentIntent.collectAsState()

    val formattedTime = remember(remainingSeconds) {
        val mins = remainingSeconds / 60
        val secs = remainingSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    // Breathing pulse for visual centering
    val infiniteTransition = rememberInfiniteTransition(label = "ShieldPulse")
    val pulseSize by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine), // 6 seconds respiratory period
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShieldPulsing"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ZenBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Minimal Top Intent
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Spa,
                contentDescription = "Intent",
                tint = ZenAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (intent.isBlank() || intent == "Center" || intent == "Vô tâm / Center") stringResource(R.string.intent_center) else intent,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = ZenTextSecondary,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Serif
                )
            )
        }

        // Center Pulsing Breath Light & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                // Outer Blur Glow
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(scaleX = pulseSize * 1.5f, scaleY = pulseSize * 1.5f)
                        .blur(30.dp)
                        .clip(CircleShape)
                        .background(ZenAmber.copy(alpha = 0.15f))
                )

                // Actual Pulsing Sphere
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(scaleX = pulseSize, scaleY = pulseSize)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(ZenAmber.copy(alpha = 0.8f), ZenBlack)
                            )
                        )
                        .border(1.dp, ZenAmber.copy(alpha = 0.3f), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = formattedTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraLight,
                    color = ZenTextPrimary,
                    fontSize = 54.sp
                )
            )
        }

        // Soundscape Synthesizer Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Generative Soundscapes",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ZenTextSecondary,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gamma Wave Toggle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.toggleGamma(!gammaEnabled) },
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (gammaEnabled) ZenAmber.copy(alpha = 0.2f) else ZenStoneDark, CircleShape)
                            .border(1.dp, if (gammaEnabled) ZenAmber else ZenStoneGrey, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = "Gamma Waves",
                            tint = if (gammaEnabled) ZenAmber else ZenTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Gamma 40Hz", style = MaterialTheme.typography.labelSmall.copy(color = ZenTextSecondary))
                }

                // Brownian Rain Toggle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.toggleRain(!rainEnabled) },
                        modifier = Modifier
                            .size(54.dp)
                            .background(if (rainEnabled) ZenAmber.copy(alpha = 0.2f) else ZenStoneDark, CircleShape)
                            .border(1.dp, if (rainEnabled) ZenAmber else ZenStoneGrey, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MusicNote,
                            contentDescription = "Brown Noise",
                            tint = if (rainEnabled) ZenAmber else ZenTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Zen Rain", style = MaterialTheme.typography.labelSmall.copy(color = ZenTextSecondary))
                }

                // Singing Bowl Direct Strike
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.strikeBowl() },
                        modifier = Modifier
                            .size(54.dp)
                            .background(ZenStoneDark, CircleShape)
                            .border(1.dp, ZenStoneGrey, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = "Bowl strike",
                            tint = ZenAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Bowl Strike", style = MaterialTheme.typography.labelSmall.copy(color = ZenTextSecondary))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Exit Session (requires a mindful Long Press to prevent accidental cancel)
            var clickCount by remember { mutableStateOf(0) }
            val coroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(ZenStoneDark)
                    .border(1.dp, ZenStoneGrey, RoundedCornerShape(20.dp))
                    .clickable {
                        clickCount++
                        if (clickCount >= 3) {
                            viewModel.cancelSessionFlow()
                        } else {
                            coroutineScope.launch {
                                delay(1500)
                                if (clickCount > 0) clickCount--
                            }
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    text = if (clickCount == 0) "Tap 3 times to exit" else "Tap ${3 - clickCount} more times",
                    color = if (clickCount > 0) ZenAmber else ZenTextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
        }
    }
}

@Composable
fun MetacognitiveMirrorView(viewModel: FlowViewModel) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    var rating by remember { mutableStateOf(4) }
    var note by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "METACOGNITIVE MIRROR",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp,
                    color = ZenAmber
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Reflect on your focus state",
                style = MaterialTheme.typography.bodyMedium.copy(color = ZenTextSecondary)
            )
        }

        // Reflection Questions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "To what extent did you 'disappear' into the work?",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    color = ZenTextPrimary
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Rating Selector (1 to 5)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { score ->
                    val isSelected = rating == score
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ZenAmber else ZenStoneDark)
                            .border(1.dp, if (isSelected) ZenAmber else ZenStoneGrey, CircleShape)
                            .clickable { rating = score }
                            .wrapContentSize(Alignment.Center)
                    ) {
                        Text(
                            text = "$score",
                            color = if (isSelected) ZenBlack else ZenTextPrimary,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (rating) {
                    1 -> stringResource(R.string.flow_score_1)
                    2 -> stringResource(R.string.flow_score_2)
                    3 -> stringResource(R.string.flow_score_3)
                    4 -> stringResource(R.string.flow_score_4)
                    5 -> stringResource(R.string.flow_score_5)
                    else -> ""
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = ZenAmber, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Notes input
            Text(
                text = "Record post-session reflections",
                style = MaterialTheme.typography.bodyLarge.copy(color = ZenTextSecondary),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = {
                    Text(
                        "Capture observations of your mind state... (e.g., breath, subtle distractions)",
                        color = ZenTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ZenTextPrimary,
                    unfocusedTextColor = ZenTextPrimary,
                    focusedBorderColor = ZenAmber,
                    unfocusedBorderColor = ZenStoneGrey,
                    focusedContainerColor = ZenStoneDark,
                    unfocusedContainerColor = ZenBlack
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.discardSession() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTextSecondary),
                border = BorderStroke(1.dp, ZenStoneGrey),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("Discard")
            }

            Button(
                onClick = { viewModel.saveSession(rating, note) },
                colors = ButtonDefaults.buttonColors(containerColor = ZenAmber, contentColor = ZenBlack),
                modifier = Modifier.weight(2f).height(54.dp)
            ) {
                Text("Save to Mirror", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun SmartPromptOverlay(viewModel: FlowViewModel) {
    val activePrompt by viewModel.activePrompt.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    if (activePrompt.type == com.example.data.SmartPromptType.NONE) return

    AlertDialog(
        onDismissRequest = { viewModel.dismissSmartPrompt() },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when (activePrompt.type) {
                        com.example.data.SmartPromptType.RATING -> Icons.Default.Star
                        com.example.data.SmartPromptType.SHARE -> Icons.Default.Share
                        com.example.data.SmartPromptType.UPDATE -> Icons.Default.SystemUpdate
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = activePrompt.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Text(
                text = activePrompt.message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            when (activePrompt.type) {
                com.example.data.SmartPromptType.RATING -> {
                    Button(
                        onClick = {
                            viewModel.onRatedApp()
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=com.soloprono.flow")
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.soloprono.flow")
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(webIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Rate 5 Stars", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                com.example.data.SmartPromptType.SHARE -> {
                    Button(
                        onClick = {
                            viewModel.dismissSmartPrompt()
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    android.content.Intent.EXTRA_TEXT,
                                    "I'm cultivating deep focus with FLOW — Stillness in Action. Check it out: https://play.google.com/store/apps/details?id=com.soloprono.flow"
                                )
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share FLOW"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Share App", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                com.example.data.SmartPromptType.UPDATE -> {
                    Button(
                        onClick = {
                            viewModel.dismissSmartPrompt()
                            try {
                                val intent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=com.soloprono.flow")
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = android.content.Intent(
                                    android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("https://play.google.com/store/apps/details?id=com.soloprono.flow")
                                ).apply {
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(webIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Update Now", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                else -> {}
            }
        },
        dismissButton = {
            if (activePrompt.type == com.example.data.SmartPromptType.RATING) {
                TextButton(onClick = { viewModel.onNeverAskRating() }) {
                    Text("Don't Ask Again", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                TextButton(onClick = { viewModel.dismissSmartPrompt() }) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
