package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPage by remember { mutableStateOf(0) }

    var isNotificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationGranted = isGranted
    }

    val ZenAmber = MaterialTheme.colorScheme.primary
    val ZenTextPrimary = MaterialTheme.colorScheme.onSurface
    val ZenTextSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val ZenStoneDark = MaterialTheme.colorScheme.surface
    val ZenStoneGrey = MaterialTheme.colorScheme.surfaceVariant
    val ZenMossGreen = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header Skip Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (currentPage < 2) {
                OutlinedButton(
                    onClick = { currentPage = 2 },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenStoneGrey)
                ) {
                    Text("Skip")
                }
            }
        }

        // Pager Content
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingPager",
            modifier = Modifier.weight(1f)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (page) {
                    0 -> OnboardingPageOne(
                        ZenAmber = ZenAmber,
                        ZenTextPrimary = ZenTextPrimary,
                        ZenTextSecondary = ZenTextSecondary,
                        ZenStoneDark = ZenStoneDark,
                        ZenStoneGrey = ZenStoneGrey
                    )
                    1 -> OnboardingPageTwo(
                        ZenAmber = ZenAmber,
                        ZenTextPrimary = ZenTextPrimary,
                        ZenTextSecondary = ZenTextSecondary,
                        ZenStoneDark = ZenStoneDark,
                        ZenStoneGrey = ZenStoneGrey
                    )
                    else -> OnboardingPageThree(
                        isNotificationGranted = isNotificationGranted,
                        onRequestPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                isNotificationGranted = true
                            }
                        },
                        ZenAmber = ZenAmber,
                        ZenMossGreen = ZenMossGreen,
                        ZenTextPrimary = ZenTextPrimary,
                        ZenTextSecondary = ZenTextSecondary,
                        ZenStoneDark = ZenStoneDark,
                        ZenStoneGrey = ZenStoneGrey
                    )
                }
            }
        }

        // Bottom Controls (Dots + Navigation Buttons)
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicators
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (currentPage == index) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (currentPage == index) ZenAmber else ZenStoneGrey)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = { currentPage-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZenTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenStoneGrey)
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                if (currentPage < 2) {
                    Button(
                        onClick = { currentPage++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenAmber,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Next", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onFinishOnboarding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenAmber,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text("Begin Flow Journey", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageOne(
    ZenAmber: androidx.compose.ui.graphics.Color,
    ZenTextPrimary: androidx.compose.ui.graphics.Color,
    ZenTextSecondary: androidx.compose.ui.graphics.Color,
    ZenStoneDark: androidx.compose.ui.graphics.Color,
    ZenStoneGrey: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(ZenStoneDark)
                .border(1.dp, ZenAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = ZenAmber,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome to Flow",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ZenTextPrimary,
                fontFamily = FontFamily.Serif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Master deep focus with Csikszentmihalyi's flow state theory and science-backed soundscapes.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ZenTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureCard(
            title = "Challenge vs Skill Compass",
            description = "Calibrate difficulty and expertise to stay cleanly in the flow channel.",
            ZenStoneDark = ZenStoneDark,
            ZenStoneGrey = ZenStoneGrey,
            ZenTextPrimary = ZenTextPrimary,
            ZenTextSecondary = ZenTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        FeatureCard(
            title = "Binaural Gamma Soundscapes",
            description = "40Hz cognitive entrainment and organic brown rain noise.",
            ZenStoneDark = ZenStoneDark,
            ZenStoneGrey = ZenStoneGrey,
            ZenTextPrimary = ZenTextPrimary,
            ZenTextSecondary = ZenTextSecondary
        )
    }
}

@Composable
private fun OnboardingPageTwo(
    ZenAmber: androidx.compose.ui.graphics.Color,
    ZenTextPrimary: androidx.compose.ui.graphics.Color,
    ZenTextSecondary: androidx.compose.ui.graphics.Color,
    ZenStoneDark: androidx.compose.ui.graphics.Color,
    ZenStoneGrey: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(ZenStoneDark)
                .border(1.dp, ZenAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = ZenAmber,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Threshold Rituals",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ZenTextPrimary,
                fontFamily = FontFamily.Serif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Prepare your mind before work begins through tactile breathing gestures and harmonic bowl chimes.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ZenTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        FeatureCard(
            title = "3-Breath Holding Circle",
            description = "Touch and hold the glowing ritual ring to ground yourself before deep work.",
            ZenStoneDark = ZenStoneDark,
            ZenStoneGrey = ZenStoneGrey,
            ZenTextPrimary = ZenTextPrimary,
            ZenTextSecondary = ZenTextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))
        FeatureCard(
            title = "Singing Bowl Resonance",
            description = "Harmonic Tibetan bell strikes signal focus intervals and warnings.",
            ZenStoneDark = ZenStoneDark,
            ZenStoneGrey = ZenStoneGrey,
            ZenTextPrimary = ZenTextPrimary,
            ZenTextSecondary = ZenTextSecondary
        )
    }
}

@Composable
private fun OnboardingPageThree(
    isNotificationGranted: Boolean,
    onRequestPermission: () -> Unit,
    ZenAmber: androidx.compose.ui.graphics.Color,
    ZenMossGreen: androidx.compose.ui.graphics.Color,
    ZenTextPrimary: androidx.compose.ui.graphics.Color,
    ZenTextSecondary: androidx.compose.ui.graphics.Color,
    ZenStoneDark: androidx.compose.ui.graphics.Color,
    ZenStoneGrey: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(ZenStoneDark)
                .border(1.dp, ZenAmber, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = ZenAmber,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Background & Lock Screen",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = ZenTextPrimary,
                fontFamily = FontFamily.Serif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Flow plays binaural audio and counts down timer smoothly even when your phone is locked or idle.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ZenTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = ZenStoneDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isNotificationGranted) ZenMossGreen else ZenAmber),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isNotificationGranted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ZenMossGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Notification Permission Granted",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = ZenTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "System drop-down controls & lock screen timer display enabled.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = "System Drop-Down & Lock Screen Access",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ZenTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Allow notifications so you can view session progress and stop audio from your lock screen or notification shade.",
                        style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ZenAmber,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Grant Notification Permission")
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    description: String,
    ZenStoneDark: androidx.compose.ui.graphics.Color,
    ZenStoneGrey: androidx.compose.ui.graphics.Color,
    ZenTextPrimary: androidx.compose.ui.graphics.Color,
    ZenTextSecondary: androidx.compose.ui.graphics.Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ZenStoneDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, ZenStoneGrey),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = ZenTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = ZenTextSecondary)
            )
        }
    }
}
