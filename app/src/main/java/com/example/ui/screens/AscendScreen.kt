package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Spa
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.FlowViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AscendScreen(
    viewModel: FlowViewModel,
    modifier: Modifier = Modifier
) {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    val currentReminderId by viewModel.currentReminderId.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Title block
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.ascend_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif,
                        letterSpacing = 4.sp,
                        color = ZenAmber
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.ascend_subtitle),
                    style = MaterialTheme.typography.bodyMedium.copy(color = ZenTextSecondary)
                )
            }
        }

        // STAGE 2: Everyday Presence (Trait Flow)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ZenStoneDark),
                border = BorderStroke(1.dp, ZenStoneGrey)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.stage_trait_flow),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ZenAmber,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ZenStoneGrey)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(stringResource(R.string.active_state), color = ZenTextSecondary, fontSize = 9.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.stage_trait_flow_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary, lineHeight = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // The Prompt Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ZenBlack, RoundedCornerShape(12.dp))
                            .border(1.dp, ZenStoneGrey, RoundedCornerShape(12.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.Spa,
                                contentDescription = "Lotus",
                                tint = ZenMossGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AnimatedContent(
                                targetState = currentReminderId,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(300))
                                },
                                label = "ReminderTransition"
                            ) { id ->
                                Text(
                                    text = stringResource(id),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = ZenTextPrimary,
                                        lineHeight = 24.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Actions
                    Button(
                        onClick = { viewModel.generateReminder() },
                        colors = ButtonDefaults.buttonColors(containerColor = ZenStoneGrey, contentColor = ZenTextPrimary),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.trigger_shift))
                    }
                }
            }
        }

        // STAGE 3: Collective Calming (Group Flow)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ZenStoneDark),
                border = BorderStroke(1.dp, ZenStoneGrey)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.stage_group_flow),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ZenAmber,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.stage_group_flow_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary, lineHeight = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Group Flow Interactive Space
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ZenBlack)
                            .border(1.dp, ZenStoneGrey, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        InteractiveSpacePulse()
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.group_flow_active_count, "1,429"),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ZenTextSecondary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // STAGE 4: Self-Regulation (Paced Breathing & Reflection)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ZenStoneDark),
                border = BorderStroke(1.dp, ZenStoneGrey)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.stage_transcendence),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ZenAmber,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.stage_transcendence_desc),
                        style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary, lineHeight = 18.sp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Guided Non-Dual Pointers Section
                    NonDualPointersWidget()

                    Spacer(modifier = Modifier.height(24.dp))
                    Divider(color = ZenStoneGrey, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // HRV Biofeedback Instructions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.GraphicEq,
                            contentDescription = "HRV",
                            tint = ZenMossGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.hrv_title),
                                style = MaterialTheme.typography.titleSmall.copy(color = ZenTextPrimary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.hrv_desc),
                                style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary, lineHeight = 18.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractiveSpacePulse() {
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenMossGreen = MaterialTheme.colorScheme.secondary
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    var rippleCenter by remember { mutableStateOf(Offset(100f, 100f)) }
    var rippleRadiusScale = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Generate orbiting particles
    val transition = rememberInfiniteTransition(label = "StarOrbit")
    val orbitAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Angle"
    )

    // Faint pulsing for collective stars
    val starPulse by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        rippleCenter = offset
                        coroutineScope.launch {
                            rippleRadiusScale.snapTo(0f)
                            rippleRadiusScale.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(1200, easing = EaseOutQuad)
                            )
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)

            // Draw orbit paths (delicate ellipses)
            drawCircle(
                color = ZenStoneLight.copy(alpha = 0.2f),
                radius = size.width / 4f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )

            // Draw ripple if active
            if (rippleRadiusScale.value > 0f && rippleRadiusScale.value < 0.99f) {
                drawCircle(
                    color = ZenAmber.copy(alpha = 1f - rippleRadiusScale.value),
                    radius = rippleRadiusScale.value * 220f,
                    center = rippleCenter,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                )
                drawCircle(
                    color = ZenAmber.copy(alpha = (1f - rippleRadiusScale.value) * 0.3f),
                    radius = rippleRadiusScale.value * 220f,
                    center = rippleCenter
                )
            }

            // Draw collective souls as orbiting glowing nodes
            val baseRadius = size.width / 4f
            val rads = Math.toRadians(orbitAngle.toDouble())
            
            // Core Star 1
            val s1X = center.x + baseRadius * cos(rads).toFloat()
            val s1Y = center.y + baseRadius * sin(rads).toFloat()
            drawCircle(ZenAmber.copy(alpha = starPulse), radius = 6f, center = Offset(s1X, s1Y))
            drawCircle(ZenAmber.copy(alpha = starPulse * 0.3f), radius = 14f, center = Offset(s1X, s1Y))

            // Core Star 2 (offset)
            val rads2 = Math.toRadians((orbitAngle + 120f).toDouble())
            val s2X = center.x + (baseRadius * 1.3f) * cos(rads2).toFloat()
            val s2Y = center.y + (baseRadius * 0.7f) * sin(rads2).toFloat()
            drawCircle(ZenMossGreen.copy(alpha = 1f - starPulse), radius = 5f, center = Offset(s2X, s2Y))
            drawCircle(ZenMossGreen.copy(alpha = (1f - starPulse) * 0.2f), radius = 12f, center = Offset(s2X, s2Y))

            // Core Star 3 (counter-orbit)
            val rads3 = Math.toRadians((-orbitAngle + 240f).toDouble())
            val s3X = center.x + (baseRadius * 0.8f) * cos(rads3).toFloat()
            val s3Y = center.y + (baseRadius * 1.1f) * sin(rads3).toFloat()
            drawCircle(ZenAmber.copy(alpha = starPulse), radius = 4f, center = Offset(s3X, s3Y))

            // Static scattered stars representing global resonance
            val staticSeeds = listOf(
                Offset(0.15f, 0.25f), Offset(0.85f, 0.15f), Offset(0.3f, 0.8f),
                Offset(0.75f, 0.75f), Offset(0.5f, 0.12f), Offset(0.2f, 0.6f),
                Offset(0.9f, 0.65f), Offset(0.6f, 0.85f)
            )

            staticSeeds.forEachIndexed { idx, seed ->
                val pulseCoeff = if (idx % 2 == 0) starPulse else (1f - starPulse)
                drawCircle(
                    color = ZenAmber.copy(alpha = pulseCoeff * 0.4f),
                    radius = 3f,
                    center = Offset(seed.x * size.width, seed.y * size.height)
                )
            }
        }
        
        Text(
            text = stringResource(R.string.group_flow_instruction),
            color = ZenTextSecondary,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

@Composable
fun NonDualPointersWidget() {
    val ZenBlack = MaterialTheme.colorScheme.background
    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenAmberDim = MaterialTheme.colorScheme.tertiary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenStoneLight = MaterialTheme.colorScheme.outline
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    var isPointerActive by remember { mutableStateOf(false) }
    var currentPointerIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    val pointers = listOf(
        R.string.pointer_1,
        R.string.pointer_2,
        R.string.pointer_3,
        R.string.pointer_4,
        R.string.pointer_5,
        R.string.pointer_6
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Outlined.SelfImprovement,
                contentDescription = "Observer",
                tint = ZenAmber,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (!isPointerActive) {
                Text(
                    text = stringResource(R.string.non_dual_guide_title),
                    style = MaterialTheme.typography.titleMedium.copy(color = ZenTextPrimary, fontFamily = FontFamily.Serif),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.non_dual_guide_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        isPointerActive = true
                        currentPointerIndex = 0
                        scope.launch {
                            while (currentPointerIndex < pointers.size - 1) {
                                delay(10000) // 10 seconds per pointer
                                currentPointerIndex++
                            }
                            delay(10000)
                            isPointerActive = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ZenAmber, contentColor = ZenBlack),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.begin_awareness_guide))
                }
            } else {
                // Active typing/guided state
                AnimatedContent(
                    targetState = currentPointerIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(600))
                    },
                    label = "PointerTransition"
                ) { idx ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(pointers[idx]),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Serif,
                                color = ZenTextPrimary,
                                lineHeight = 26.sp
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.pointer_progress, idx + 1, pointers.size),
                            style = MaterialTheme.typography.labelSmall.copy(color = ZenTextSecondary)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.end_guide),
                    color = ZenAmber,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .clickable { isPointerActive = false }
                        .padding(4.dp)
                )
            }
        }
    }
}
