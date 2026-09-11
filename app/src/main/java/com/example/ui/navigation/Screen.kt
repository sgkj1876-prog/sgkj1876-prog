package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleEn: String,
    val titleAr: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", "الرئيسية", Icons.Default.Dashboard)
    data object Analysis : Screen("analysis", "DEX & Manifest", "التحليل", Icons.Default.Assessment)
    data object Findings : Screen("findings", "Findings", "النتائج", Icons.Default.Security)
    data object TopTargets : Screen("top_targets", "VIP Targets", "أهداف الشراء", Icons.Default.Star)
    data object Strings : Screen("strings", "Strings", "مجمع النصوص", Icons.Default.FindInPage)
    data object Reports : Screen("reports", "Reports", "التقارير", Icons.Default.Code)
    data object Settings : Screen("settings", "Settings", "الإعدادات", Icons.Default.Settings)

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(
                Dashboard,
                Analysis,
                Findings,
                TopTargets,
                Strings,
                Reports
            )
    }
}
