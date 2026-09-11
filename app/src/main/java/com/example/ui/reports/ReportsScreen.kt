package com.example.ui.reports

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.components.SecurityScoreGauge
import com.example.ui.components.StatMetricCard
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SmaliPurple

@Composable
fun ReportsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()
    val context = LocalContext.current
    val clipboard: ClipboardManager = LocalClipboardManager.current

    val analysis = currentAnalysis

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isArabic) "تقارير الاستخبارات الأمنية" else "Security Intelligence Reports",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isArabic) "ملخص التدقيق الأمني وخيارات التصدير بصيغتي JSON و TXT"
                else "Executive static audit summary and structured export options",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (analysis == null) {
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Assessment,
                            contentDescription = null,
                            tint = CyberCyan.copy(alpha = 0.5f),
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isArabic) "لا توجد حزمة جاهزة للتقرير" else "No Active Analysis Report",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "قم بفحص ملف APK أولاً لإنشاء التقرير"
                            else "Analyze an APK from the Dashboard to generate a report",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Security Score Gauge
            item {
                SecurityScoreGauge(
                    score = analysis.securityScore,
                    riskLevel = analysis.riskLevel,
                    riskLevelAr = analysis.riskLevelAr,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // High Level Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = if (isArabic) "النتائج الكلية" else "Findings",
                        value = "${analysis.findings.size}",
                        accentColor = if (analysis.findings.any { it.severity == com.example.data.model.FindingSeverity.CRITICAL }) SeverityCritical else SeverityHigh,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "الأهداف المرشحة" else "VIP Targets",
                        value = "${analysis.purchaseTargets.size}",
                        accentColor = SeverityInfo,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "الأذونات" else "Permissions",
                        value = "${analysis.manifest.permissions.size}",
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Export Actions
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "تصدير ومشاركة التقرير" else "Report Export & Distribution",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val json = viewModel.getExportJson()
                                    clipboard.setText(AnnotatedString(json))
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share JSON Report"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("export_json_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberCyan,
                                    contentColor = Color(0xFF080C14)
                                )
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = if (isArabic) "مشاركة JSON" else "Share JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val textReport = viewModel.getExportText()
                                    clipboard.setText(AnnotatedString(textReport))
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, textReport)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Text Report"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("export_txt_btn"),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyberCyan)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = if (isArabic) "مشاركة TXT" else "Share TXT", color = CyberCyan, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Raw Text Report Preview
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (isArabic) "معاينة التقرير النصي الكامل" else "Executive Report Preview",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.getExportText().take(1500) + "\n... [truncated in preview]",
                            fontSize = 11.sp,
                            fontFamily = CodeFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
