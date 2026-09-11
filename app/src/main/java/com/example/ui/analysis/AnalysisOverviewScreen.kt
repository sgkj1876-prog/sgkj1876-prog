package com.example.ui.analysis

import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.analysis.AnalysisState
import com.example.ui.components.ApkParsingCircularProgress
import com.example.ui.components.CyberCard
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityInfo
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SmaliPurple

@Composable
fun AnalysisOverviewScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val analysisState by viewModel.analysisState.collectAsState()
    val activeTargetFileName by viewModel.activeTargetFileName.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

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
                text = if (isArabic) "نظرة عامة على الـ DEX والـ Manifest" else "DEX Architecture & Manifest",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isArabic) "تشريح بنيوي للملفات التنفيذية وحزم المكونات والصلاحيات"
                else "Structural breakdown of DEX bytecode modules, exported components, and permissions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (analysis == null) {
            item {
                CyberCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (analysisState is AnalysisState.Progress) CyberCyan else CyberCardBorder,
                    glow = analysisState is AnalysisState.Progress
                ) {
                    if (analysisState is AnalysisState.Progress) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ApkParsingCircularProgress(
                                progress = (analysisState as AnalysisState.Progress).info,
                                isArabic = isArabic,
                                fileName = activeTargetFileName
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Layers,
                                contentDescription = null,
                                tint = CyberCyan.copy(alpha = 0.5f),
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (isArabic) "لا توجد حزمة محللة حالياً" else "No Analysis Active",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            // Package & Manifest Identity
            item {
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "هوية الحزمة والـ Manifest" else "Manifest Identity & Security Flags",
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ManifestFlagRow(label = "Package Name", value = analysis.manifest.packageName)
                        ManifestFlagRow(label = "Version", value = "${analysis.manifest.versionName} (${analysis.manifest.versionCode})")
                        ManifestFlagRow(label = "SDK Range", value = "Min: ${analysis.manifest.minSdkVersion} | Target: ${analysis.manifest.targetSdkVersion}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlagStatusChip(
                                label = "Debuggable",
                                isSafe = !analysis.manifest.isDebuggable,
                                modifier = Modifier.weight(1f)
                            )
                            FlagStatusChip(
                                label = "Allow Backup",
                                isSafe = !analysis.manifest.allowBackup,
                                modifier = Modifier.weight(1f)
                            )
                            FlagStatusChip(
                                label = "Cleartext HTTP",
                                isSafe = !analysis.manifest.usesCleartextTraffic,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // DEX Modules List
            item {
                Text(
                    text = if (isArabic) "حزم الـ DEX المكتشفة (${analysis.dexFiles.size})" else "DEX Bytecode Modules (${analysis.dexFiles.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(analysis.dexFiles) { dex ->
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dex.fileName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontFamily = CodeFontFamily
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = dex.headerVersion,
                                    fontSize = 11.sp,
                                    color = CyberCyan,
                                    fontFamily = CodeFontFamily,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Classes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${dex.classCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = CodeFontFamily)
                            }
                            Column {
                                Text(text = "Methods", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${dex.methodCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = CodeFontFamily)
                            }
                            Column {
                                Text(text = "Fields", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${dex.fieldCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = CodeFontFamily)
                            }
                            Column {
                                Text(text = "Strings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "${dex.stringCount}", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = CodeFontFamily)
                            }
                        }
                    }
                }
            }

            // Permissions List
            item {
                Text(
                    text = if (isArabic) "الأذونات المصرح بها (${analysis.manifest.permissions.size})" else "Declared Permissions (${analysis.manifest.permissions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(analysis.manifest.permissions) { perm ->
                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (perm.isDangerous) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (perm.isDangerous) SeverityHigh else SeverityInfo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = perm.name.substringAfterLast('.'),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                fontFamily = CodeFontFamily,
                                color = if (perm.isDangerous) SeverityHigh else MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (isArabic && perm.riskDescriptionAr.isNotEmpty()) perm.riskDescriptionAr else perm.riskDescription.ifEmpty { perm.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (perm.isDangerous) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = SeverityHigh.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "DANGEROUS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SeverityHigh,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
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

@Composable
private fun ManifestFlagRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CodeFontFamily,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun FlagStatusChip(
    label: String,
    isSafe: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .border(
                1.dp,
                if (isSafe) SeverityInfo.copy(alpha = 0.4f) else SeverityCritical.copy(alpha = 0.5f),
                RoundedCornerShape(6.dp)
            ),
        shape = RoundedCornerShape(6.dp),
        color = if (isSafe) SeverityInfo.copy(alpha = 0.08f) else SeverityCritical.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isSafe) "SAFE" else "RISKY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CodeFontFamily,
                color = if (isSafe) SeverityInfo else SeverityCritical
            )
        }
    }
}
