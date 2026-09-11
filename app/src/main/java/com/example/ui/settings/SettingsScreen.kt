package com.example.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CyberCard
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.theme.CodeFontFamily
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityInfo

@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isArabic by viewModel.isArabic.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isArabic) "الإعدادات وبيان الأمان" else "Settings & Security Policy",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isArabic) "تخصيص الواجهة واللغة والتحكم بالبيانات المحلية"
                else "Appearance, language preferences, and local data controls",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Appearance & Preferences
        item {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "التفضيلات والمظهر" else "Preferences & Localization",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Dark Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = CyberCyan)
                            Spacer(modifier = Modifier.size(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "الوضع الداكن السيبراني" else "Cyber Dark Mode",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isArabic) "واجهة داكنة عالية التباين" else "High-contrast security palette",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { viewModel.toggleTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberCyan.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Arabic RTL Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = SeverityInfo)
                            Spacer(modifier = Modifier.size(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "اللغة العربية (RTL)" else "Arabic Language (RTL)",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isArabic) "دعم كامل للواجهة من اليمين لليسار" else "Full Arabic RTL layout support",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isArabic,
                            onCheckedChange = { viewModel.toggleLanguage() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SeverityInfo,
                                checkedTrackColor = SeverityInfo.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }

        // Data Storage & Privacy
        item {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "إدارة البيانات والذاكرة المحلية" else "Data Management & Storage",
                        style = MaterialTheme.typography.titleMedium,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "جميع عمليات التحليل تُنفّذ محلياً داخل المعالج والذاكرة المؤقتة. لا يتم إرسال أي ملفات إلى أي خادم خارجي."
                        else "All analysis is processed strictly offline and locally on-device. No telemetry or APK bytes are transmitted to any cloud servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.clearAllHistory() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SeverityCritical.copy(alpha = 0.15f),
                            contentColor = SeverityCritical
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_history_btn")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (isArabic) "مسح سجل الفحوصات بالكامل" else "Clear All Analysis History",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Responsible Use & Legal Disclosure
        item {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = SeverityInfo.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = SeverityInfo)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = if (isArabic) "الاستخدام القانوني والمسؤول" else "Legal & Responsible Research",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SeverityInfo
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic)
                            "تطبيق APK Sentinel مُصمم لأغراض الفحص الأمني الساكن، والتدقيق الدفاعي، وتحليل قابلية التأثر، والتعليم الأكاديمي. التطبيق لا يقوم بتشغيل كود التطبيقات المحللة ولا يقوم بتعديل أو إعادة تجميع الحزم ولا يتجاوز أي أنظمة فوترة أو اشتراكات. يرجى التأكد دائماً من امتلاكك لحقوق فحص التطبيقات أو الحصول على تصريح كتابي مسبق."
                        else
                            "APK Sentinel is engineered exclusively for static defensive audits, vulnerability research, and security education. It strictly never executes target APK code, modifies binaries, or bypasses monetization/licensing barriers. Always ensure you have legitimate authorization to analyze software artifacts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // Engine Specification
        item {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Engine Architecture Specifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Dalvik Executable (DEX 035-039) Direct Stream Parser\n• AXML Binary Resource Decoder\n• Heuristic Purchase & VIP Identifier Classifier\n• Room 2.6 Local Persistent Storage\n• Jetpack Compose Material 3 Dark-First Architecture",
                        fontSize = 11.sp,
                        fontFamily = CodeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
