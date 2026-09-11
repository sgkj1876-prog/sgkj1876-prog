package com.example.ui.targets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PurchaseTarget
import com.example.ui.components.ConfidenceBadge
import com.example.ui.components.CyberCard
import com.example.ui.components.SmaliViewerDialog
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SmaliPurple
import com.example.ui.theme.TextMuted

@Composable
fun TopTargetsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val clipboard: ClipboardManager = LocalClipboardManager.current

    var selectedSmaliTarget by remember { mutableStateOf<PurchaseTarget?>(null) }
    var expandedXrefTargetId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "أفضل 10 أهداف مشتريات (VIP TARGETS)" else "TOP 10 PURCHASE TARGETS",
                        style = MaterialTheme.typography.titleLarge,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CodeFontFamily
                    )
                    Text(
                        text = if (isArabic) "ترشيح أفضل الأهداف بحسب معدل الثقة والصلة البرمجية"
                        else "Static heuristic ranking of billing, subscriptions, and privilege checks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, SeverityInfo.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    color = SeverityInfo.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "HEURISTIC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SeverityInfo,
                        fontFamily = CodeFontFamily
                    )
                }
            }
        }

        val targets = currentAnalysis?.purchaseTargets ?: emptyList()

        if (targets.isEmpty()) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = CyberCyan.copy(alpha = 0.5f),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isArabic) "لم يتم فحص أي حزمة APK بعد" else "No Targets Discovered",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "ابدأ فحص APK من لوحة التحكم لعرض أفضل الأهداف"
                            else "Run an APK static analysis from Dashboard to populate targets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(targets, key = { it.identifier }) { target ->
                TargetItemCard(
                    target = target,
                    isArabic = isArabic,
                    isXrefExpanded = expandedXrefTargetId == target.identifier,
                    onToggleXref = {
                        expandedXrefTargetId = if (expandedXrefTargetId == target.identifier) null else target.identifier
                    },
                    onViewSmali = {
                        selectedSmaliTarget = target
                    },
                    onCopy = {
                        clipboard.setText(AnnotatedString(target.identifier))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Smali Code Dialog
    selectedSmaliTarget?.let { target ->
        SmaliViewerDialog(
            title = "Smali: ${target.identifier}",
            className = target.targetClass,
            methodName = target.targetMethod,
            smaliCode = target.sampleSmali,
            onDismiss = { selectedSmaliTarget = null }
        )
    }
}

@Composable
fun TargetItemCard(
    target: PurchaseTarget,
    isArabic: Boolean,
    isXrefExpanded: Boolean,
    onToggleXref: () -> Unit,
    onViewSmali: () -> Unit,
    onCopy: () -> Unit
) {
    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("target_card_${target.rank}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Rank, Identifier and Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.dp, CyberCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${target.rank}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            fontFamily = CodeFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = target.identifier,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontFamily = CodeFontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                ConfidenceBadge(
                    level = target.confidenceLevel,
                    confidence = target.confidence,
                    isArabic = isArabic
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = target.category,
                        fontSize = 11.sp,
                        color = SmaliPurple,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "${if (isArabic) "التكرار:" else "Occurrences:"} ${target.occurrences}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (isArabic) "المراجع:" else "XREFs:"} ${target.referenceCount}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${target.targetClass}->${target.targetMethod}()",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontFamily = CodeFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons matching reference screenshots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleXref,
                    modifier = Modifier.weight(1.1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                ) {
                    Icon(
                        if (isXrefExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CyberCyan
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "استدعاء XREF" else "XREF Callers",
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                }

                OutlinedButton(
                    onClick = onViewSmali,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SmaliPurple.copy(alpha = 0.5f))
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmaliPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "كود Smali" else "Smali Code",
                        fontSize = 11.sp,
                        color = SmaliPurple
                    )
                }

                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(0.7f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCardBorder)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isArabic) "نسخ" else "Copy",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded XREF section
            AnimatedVisibility(visible = isXrefExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (isArabic) "دوال الاستدعاء المباشرة (Callers):" else "Referencing Call Sites:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (target.callerMethods.isEmpty()) {
                        Text(
                            text = if (isArabic) "لا توجد استدعاءات إضافية مسجلة" else "No direct callers indexed",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        target.callerMethods.forEach { caller ->
                            Text(
                                text = "• $caller",
                                fontSize = 11.sp,
                                fontFamily = CodeFontFamily,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
