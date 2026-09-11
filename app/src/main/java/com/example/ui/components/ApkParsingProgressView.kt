package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.analysis.AnalysisProgress
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SmaliPurple

/**
 * Circular progress bar component that displays during the APK parsing process
 * to provide rich visual user feedback when analysis is underway.
 */
@Composable
fun ApkParsingCircularProgress(
    progress: AnalysisProgress,
    isArabic: Boolean,
    modifier: Modifier = Modifier,
    fileName: String? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.percentage.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "apkParsingProgressAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulseRingTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotateAngle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("circular_progress_bar_container"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (fileName != null) {
            Text(
                text = fileName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = CodeFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Circular Progress Bar with Inner Stats and Ambient Radar
        Box(
            modifier = Modifier
                .size(160.dp)
                .testTag("circular_progress_bar_box"),
            contentAlignment = Alignment.Center
        ) {
            // Ambient radar/glow background ring
            Box(
                modifier = Modifier
                    .size(156.dp * pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CyberCyan.copy(alpha = 0.12f),
                                SmaliPurple.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Rotating accent dash ring
            Box(
                modifier = Modifier
                    .size(144.dp)
                    .rotate(rotateAngle)
                    .border(
                        width = 1.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                CyberCyan.copy(alpha = 0.6f),
                                Color.Transparent,
                                SmaliPurple.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Background track for Circular Progress Bar
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(132.dp),
                color = CyberCyan.copy(alpha = 0.15f),
                strokeWidth = 9.dp,
                strokeCap = StrokeCap.Round
            )

            // Primary active Circular Progress Bar
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .size(132.dp)
                    .testTag("circular_progress_bar"),
                color = CyberCyan,
                strokeWidth = 9.dp,
                strokeCap = StrokeCap.Round
            )

            // Inner content: Percentage & Stage
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Scanner",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CodeFontFamily,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (isArabic) "${progress.stageIndex}/${progress.totalStages} المرحلة"
                           else "Stage ${progress.stageIndex}/${progress.totalStages}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = CyberCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Active Stage Name
        Text(
            text = if (isArabic) progress.stageNameAr else progress.stageNameEn,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isArabic) "جاري تفكيك وفحص حزمة الـ APK استاتيكياً..."
                   else "Decompiling and performing static byte-level security audit...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Stage Pills Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stagesList = listOf(
                Pair("Metadata", "البيانات"),
                Pair("Manifest", "المانيفست"),
                Pair("DEX", "الـ DEX"),
                Pair("Bytecode", "الكود"),
                Pair("Strings", "النصوص"),
                Pair("Security", "الأمان"),
                Pair("VIP", "الشراء"),
                Pair("Report", "التقرير")
            )

            stagesList.forEachIndexed { index, pair ->
                val stageNum = index + 1
                val isCompleted = stageNum < progress.stageIndex
                val isCurrent = stageNum == progress.stageIndex

                val pillColor = when {
                    isCompleted -> SeverityInfo
                    isCurrent -> CyberCyan
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.5.dp)
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(pillColor)
                )
            }
        }
    }
}

/**
 * High-visibility modal dialog displaying the circular progress bar during APK parsing.
 */
@Composable
fun ApkParsingProgressDialog(
    progress: AnalysisProgress,
    isArabic: Boolean,
    fileName: String? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, CyberCyan.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .testTag("parsing_progress_dialog"),
            color = CyberSurface,
            tonalElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isArabic) "فحص وتحليل حزمة APK" else "APK Static Parsing Engine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )

                Spacer(modifier = Modifier.height(16.dp))

                ApkParsingCircularProgress(
                    progress = progress,
                    isArabic = isArabic,
                    fileName = fileName
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.testTag("dismiss_parsing_dialog_btn")
                ) {
                    Text(
                        text = if (isArabic) "متابعة في الخلفية" else "Run In Background",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
