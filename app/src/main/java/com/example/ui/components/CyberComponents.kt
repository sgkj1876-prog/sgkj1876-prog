package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FindingSeverity
import com.example.data.model.TargetConfidenceLevel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDark
import com.example.ui.theme.CyberGlowBorder
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityCriticalBg
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityHighBg
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SeverityInfoBg
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityLowBg
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SeverityMediumBg
import com.example.ui.theme.SmaliComment
import com.example.ui.theme.SmaliKeyword
import com.example.ui.theme.SmaliPurple
import com.example.ui.theme.SmaliRegister
import com.example.ui.theme.SmaliString
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberCardBorder,
    glow: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (glow) CyberCyan else borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        content()
    }
}

@Composable
fun SeverityBadge(
    severity: FindingSeverity,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (severity) {
        FindingSeverity.CRITICAL -> Triple(SeverityCriticalBg, SeverityCritical, "CRITICAL")
        FindingSeverity.HIGH -> Triple(SeverityHighBg, SeverityHigh, "HIGH")
        FindingSeverity.MEDIUM -> Triple(SeverityMediumBg, SeverityMedium, "MEDIUM")
        FindingSeverity.LOW -> Triple(SeverityLowBg, SeverityLow, "LOW")
        FindingSeverity.INFO -> Triple(SeverityInfoBg, SeverityInfo, "INFO")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CodeFontFamily,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun ConfidenceBadge(
    level: TargetConfidenceLevel,
    confidence: Int,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val color = when (level) {
        TargetConfidenceLevel.CRITICAL -> SeverityCritical
        TargetConfidenceLevel.HIGH -> SeverityHigh
        TargetConfidenceLevel.MEDIUM -> SeverityMedium
        TargetConfidenceLevel.LOW -> SeverityLow
        TargetConfidenceLevel.INFO -> SeverityInfo
    }

    val label = if (isArabic) level.labelAr else level.labelEn

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = "$confidence% $label",
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CodeFontFamily
        )
    }
}

@Composable
fun StatMetricCard(
    title: String,
    value: String,
    accentColor: Color = CyberCyan,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    CyberCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = CodeFontFamily
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SecurityScoreGauge(
    score: Int,
    riskLevel: String,
    riskLevelAr: String,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateFloatAsState(targetValue = score.toFloat() / 100f, label = "scoreAnim")
    val gaugeColor = when {
        score >= 80 -> SeverityInfo
        score >= 60 -> SeverityMedium
        score >= 40 -> SeverityHigh
        else -> SeverityCritical
    }

    CyberCard(
        modifier = modifier,
        borderColor = gaugeColor.copy(alpha = 0.4f),
        glow = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) "مؤشر السلامة ومستوى الخطورة" else "Security Posture Index",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isArabic) riskLevelAr else riskLevel,
                    style = MaterialTheme.typography.titleLarge,
                    color = gaugeColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { animatedScore },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = gaugeColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(gaugeColor.copy(alpha = 0.15f))
                    .border(2.dp, gaugeColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = gaugeColor,
                        fontFamily = CodeFontFamily
                    )
                    Text(
                        text = "/100",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SmaliSyntaxText(
    code: String,
    modifier: Modifier = Modifier
) {
    val lines = code.lines()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B12))
            .padding(12.dp)
            .horizontalScroll(scrollState)
    ) {
        lines.forEachIndexed { idx, line ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format("%3d  ", idx + 1),
                    color = Color(0xFF4A5568),
                    fontSize = 12.sp,
                    fontFamily = CodeFontFamily
                )
                Text(
                    text = highlightSmaliLine(line),
                    fontSize = 12.sp,
                    fontFamily = CodeFontFamily
                )
            }
        }
    }
}

private fun highlightSmaliLine(line: String): AnnotatedString {
    return buildAnnotatedString {
        when {
            line.trim().startsWith("#") -> {
                pushStyle(SpanStyle(color = SmaliComment))
                append(line)
                pop()
            }
            line.trim().startsWith(".") -> {
                val parts = line.split(" ", limit = 2)
                pushStyle(SpanStyle(color = SmaliKeyword, fontWeight = FontWeight.Bold))
                append(parts[0])
                pop()
                if (parts.size > 1) {
                    append(" ")
                    append(parts[1])
                }
            }
            line.contains("const-string") -> {
                val beforeQuote = line.substringBefore("\"", "")
                val quote = line.substringAfter("\"", "").substringBeforeLast("\"", "")
                val afterQuote = line.substringAfterLast("\"", "")
                append(beforeQuote)
                if (beforeQuote.isNotEmpty()) {
                    pushStyle(SpanStyle(color = SmaliString))
                    append("\"$quote\"")
                    pop()
                    append(afterQuote)
                } else {
                    append(line)
                }
            }
            line.contains("invoke-") -> {
                pushStyle(SpanStyle(color = SmaliPurple, fontWeight = FontWeight.SemiBold))
                append(line)
                pop()
            }
            line.contains("return") -> {
                pushStyle(SpanStyle(color = CyberCyan, fontWeight = FontWeight.Bold))
                append(line)
                pop()
            }
            else -> {
                append(line)
            }
        }
    }
}

@Composable
fun SmaliViewerDialog(
    title: String,
    className: String,
    methodName: String,
    smaliCode: String,
    onDismiss: () -> Unit
) {
    val clipboard: ClipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
            color = CyberSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan
                        )
                        Text(
                            text = "$className->$methodName",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontFamily = CodeFontFamily
                        )
                    }
                    Row {
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(smaliCode))
                            },
                            modifier = Modifier.testTag("copy_smali_btn")
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy Smali",
                                tint = CyberCyan
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SmaliSyntaxText(code = smaliCode)
                }
            }
        }
    }
}
