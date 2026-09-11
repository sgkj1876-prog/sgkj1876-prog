package com.example.ui.findings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FindingSeverity
import com.example.data.model.SecurityFinding
import com.example.ui.components.CyberCard
import com.example.ui.components.SeverityBadge
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SmaliPurple
import com.example.ui.theme.TextMuted

@Composable
fun FindingsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    var selectedSeverityFilter by remember { mutableStateOf<FindingSeverity?>(null) }
    var expandedFindingId by remember { mutableStateOf<String?>(null) }
    val clipboard: ClipboardManager = LocalClipboardManager.current

    val allFindings = currentAnalysis?.findings ?: emptyList()
    val filteredFindings = if (selectedSeverityFilter == null) {
        allFindings
    } else {
        allFindings.filter { it.severity == selectedSeverityFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "النتائج والثغرات الأمنية" else "Security Findings & Weaknesses",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${allFindings.size} ${if (isArabic) "ملاحظات مكتشفة استاتيكياً" else "findings identified"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${currentAnalysis?.securityScore ?: 100}/100",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = CodeFontFamily,
                        color = CyberCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Severity Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSeverityFilter == null,
                    onClick = { selectedSeverityFilter = null },
                    label = { Text(if (isArabic) "الكل (${allFindings.size})" else "All (${allFindings.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan.copy(alpha = 0.2f),
                        selectedLabelColor = CyberCyan
                    )
                )
                FindingSeverity.entries.forEach { severity ->
                    val count = allFindings.count { it.severity == severity }
                    if (count > 0) {
                        FilterChip(
                            selected = selectedSeverityFilter == severity,
                            onClick = { selectedSeverityFilter = severity },
                            label = { Text("${severity.name} ($count)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (severity) {
                                    FindingSeverity.CRITICAL -> SeverityCritical.copy(alpha = 0.2f)
                                    FindingSeverity.HIGH -> SeverityHigh.copy(alpha = 0.2f)
                                    FindingSeverity.MEDIUM -> SeverityMedium.copy(alpha = 0.2f)
                                    else -> CyberCyan.copy(alpha = 0.2f)
                                }
                            )
                        )
                    }
                }
            }
        }

        if (filteredFindings.isEmpty()) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = SeverityInfo,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isArabic) "لا توجد نتائج في هذه الفئة" else "No Findings in this Category",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            items(filteredFindings, key = { it.id }) { finding ->
                val isExpanded = expandedFindingId == finding.id

                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            expandedFindingId = if (isExpanded) null else finding.id
                        }
                        .testTag("finding_card_${finding.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SeverityBadge(severity = finding.severity)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (finding.isHeuristic) SmaliPurple.copy(alpha = 0.15f) else CyberCyan.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (finding.isHeuristic) "HEURISTIC" else "FACTUAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CodeFontFamily,
                                        color = if (finding.isHeuristic) SmaliPurple else CyberCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isArabic) finding.titleAr else finding.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isArabic) finding.descriptionAr else finding.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Target: ${finding.targetClass.ifEmpty { "Manifest" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            fontFamily = CodeFontFamily,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Expanded Details
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                // Evidence Box
                                Text(
                                    text = if (isArabic) "الدليل البرمجي (Evidence):" else "Evidence:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                                Text(
                                    text = finding.evidence,
                                    fontSize = 11.sp,
                                    fontFamily = CodeFontFamily,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Why it matters
                                Text(
                                    text = if (isArabic) "لماذا هذا مهم؟ (Risk Assessment):" else "Why it matters:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeverityHigh
                                )
                                Text(
                                    text = if (isArabic) finding.whyItMattersAr else finding.whyItMatters,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Remediation
                                Text(
                                    text = if (isArabic) "الإجراء الدفاعي الموصى به:" else "Remediation & Defense:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeverityInfo
                                )
                                Text(
                                    text = if (isArabic) finding.recommendationAr else finding.recommendation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            val copyText = "${finding.title}\nSeverity: ${finding.severity.name}\nEvidence: ${finding.evidence}\nRemediation: ${finding.recommendation}"
                                            clipboard.setText(AnnotatedString(copyText))
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy Finding",
                                            tint = CyberCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
