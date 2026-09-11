package com.example.ui.dashboard

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analysis.AnalysisState
import com.example.data.model.FindingSeverity
import com.example.ui.components.ApkParsingCircularProgress
import com.example.ui.components.ApkParsingProgressDialog
import com.example.ui.components.CyberCard
import com.example.ui.components.SecurityScoreGauge
import com.example.ui.components.SeverityBadge
import com.example.ui.components.StatMetricCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDark
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SmaliPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val activeTargetFileName by viewModel.activeTargetFileName.collectAsState()
    val isParsingModalVisible by viewModel.isParsingModalVisible.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    // Circular Progress Bar Modal Dialog during APK parsing
    if (isParsingModalVisible && analysisState is AnalysisState.Progress) {
        val progress = (analysisState as AnalysisState.Progress).info
        ApkParsingProgressDialog(
            progress = progress,
            isArabic = isArabic,
            fileName = activeTargetFileName,
            onDismiss = { viewModel.dismissParsingModal() }
        )
    }

    // APK File Picker Launcher
    val apkPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "selected_target.apk"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex) ?: fileName
                    }
                }
            } catch (_: Exception) {}
            viewModel.analyzeSelectedApk(uri, fileName)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(SeverityInfo)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isArabic) "حارس الحزم APK Sentinel" else "APK Sentinel Intelligence",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "منصة التحليل الأمني الاستاتيكي المحلي" else "Local Static Security Analysis Engine",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switch Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleLanguage() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = if (isArabic) "EN" else "عربي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Theme Switch Button
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = CyberCyan
                        )
                    }
                }
            }
        }

        // Active Analysis State Card
        item {
            when (val state = analysisState) {
                is AnalysisState.Progress -> {
                    CyberCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = CyberCyan,
                        glow = true
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ApkParsingCircularProgress(
                                progress = state.info,
                                isArabic = isArabic,
                                fileName = activeTargetFileName
                            )
                        }
                    }
                }
                is AnalysisState.Error -> {
                    CyberCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = SeverityCritical
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = SeverityCritical
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "خطأ في التحليل" else "Analysis Error",
                                    fontWeight = FontWeight.Bold,
                                    color = SeverityCritical
                                )
                                Text(
                                    text = if (isArabic) state.messageAr else state.messageEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.resetAnalysisState() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = CyberCyan)
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // Action CTA Cards
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyberCyan.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isArabic) "بدء فحص حزمة تطبيق جديدة" else "Initialize APK Security Audit",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isArabic) "اختر ملف APK من وحدة التخزين أو شغّل فحصاً على النموذج المدمج للفحص الفوري"
                        else "Select an APK package from device storage or run a full audit on the pre-configured sample target",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                apkPickerLauncher.launch(arrayOf("*/*", "application/vnd.android.package-archive"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("select_apk_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = Color(0xFF080C14)
                            )
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "اختيار APK" else "Select APK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.analyzeSampleApk() },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("sample_apk_button"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan)
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "فحص عينة" else "Sample Target",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Summary of Current Analysis
        currentAnalysis?.let { analysis ->
            item {
                Text(
                    text = if (isArabic) "تقرير الحزمة النشطة حالياً" else "Active Target Intelligence",
                    style = MaterialTheme.typography.titleMedium,
                    color = CyberCyan
                )
            }

            item {
                SecurityScoreGauge(
                    score = analysis.securityScore,
                    riskLevel = analysis.riskLevel,
                    riskLevelAr = analysis.riskLevelAr,
                    isArabic = isArabic,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Quick Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = if (isArabic) "النتائج والثغرات" else "Findings",
                        value = "${analysis.findings.size}",
                        accentColor = if (analysis.findings.any { it.severity == FindingSeverity.CRITICAL }) SeverityCritical else SeverityHigh,
                        subtitle = "${analysis.findings.count { it.severity == FindingSeverity.CRITICAL }} Critical",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "أهداف الشراء" else "VIP Targets",
                        value = "${analysis.purchaseTargets.size}",
                        accentColor = SeverityInfo,
                        subtitle = "${analysis.purchaseTargets.firstOrNull()?.confidence ?: 0}% Top Confidence",
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = if (isArabic) "الكلاسات" else "Classes",
                        value = "${analysis.totalClasses}",
                        accentColor = SmaliPurple,
                        subtitle = "${analysis.dexFiles.size} DEX files",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Target Details Card with Action Links
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = analysis.metadata.fileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = analysis.manifest.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CyberCyan,
                                    fontFamily = CodeFontFamily,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            SeverityBadge(
                                severity = if (analysis.securityScore < 50) FindingSeverity.CRITICAL else FindingSeverity.LOW
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "SDK: ${analysis.manifest.minSdkVersion}..${analysis.manifest.targetSdkVersion}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Methods: ${analysis.totalMethods}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Strings: ${analysis.totalStrings}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Navigation buttons to sub-screens
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onNavigate(Screen.Findings.route) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SeverityHigh.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (isArabic) "النتائج" else "Findings",
                                    fontSize = 11.sp,
                                    color = SeverityHigh
                                )
                            }
                            OutlinedButton(
                                onClick = { onNavigate(Screen.TopTargets.route) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SeverityInfo.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (isArabic) "أهداف الشراء" else "VIP Targets",
                                    fontSize = 11.sp,
                                    color = SeverityInfo
                                )
                            }
                            OutlinedButton(
                                onClick = { onNavigate(Screen.Analysis.route) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = if (isArabic) "نظرة عامة" else "Overview",
                                    fontSize = 11.sp,
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }
            }
        } ?: run {
            // Placeholder when no APK analyzed yet
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = CyberCyan.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isArabic) "لا توجد حزمة APK محللة حالياً" else "No Analyzed APK Active",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "انقر على 'فحص عينة' للبدء فوراً بفحص أمان كامل"
                            else "Click 'Sample Target' to instantly run an in-depth security analysis",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Analysis History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "سجل الفحوصات المحفوظة" else "Saved Analysis History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (historyList.isNotEmpty()) {
                    Text(
                        text = "${historyList.size} ${if (isArabic) "حزمة" else "targets"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (historyList.isEmpty()) {
            item {
                Text(
                    text = if (isArabic) "لا توجد سجلات سابقة بعد" else "No previous analyses recorded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(historyList, key = { it.id }) { item ->
                CyberCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectHistoryItem(item) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.fileName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan,
                                fontFamily = CodeFontFamily,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(item.timestamp))
                            Text(
                                text = "$dateStr • Score: ${item.securityScore}/100 • ${item.criticalCount} Critical",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteHistoryItem(item.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete item",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
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
