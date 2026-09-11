package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.analysis.AnalysisOverviewScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.findings.FindingsScreen
import com.example.ui.navigation.Screen
import com.example.ui.reports.ReportsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.strings.StringPoolScreen
import com.example.ui.targets.TopTargetsScreen
import com.example.ui.theme.ApkSentinelTheme
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private val dashboardViewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by dashboardViewModel.isDarkMode.collectAsState()
            val isArabic by dashboardViewModel.isArabic.collectAsState()

            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                ApkSentinelTheme(darkTheme = isDarkMode) {
                    ApkSentinelApp(viewModel = dashboardViewModel, isArabic = isArabic)
                }
            }
        }
    }
}

@Composable
fun ApkSentinelApp(
    viewModel: DashboardViewModel,
    isArabic: Boolean
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.titleEn,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = if (isArabic) screen.titleAr else screen.titleEn,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            selectedTextColor = CyberCyan,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { route -> navController.navigate(route) }
                    )
                }
                composable(Screen.Analysis.route) {
                    AnalysisOverviewScreen(viewModel = viewModel)
                }
                composable(Screen.Findings.route) {
                    FindingsScreen(viewModel = viewModel)
                }
                composable(Screen.TopTargets.route) {
                    TopTargetsScreen(viewModel = viewModel)
                }
                composable(Screen.Strings.route) {
                    StringPoolScreen(viewModel = viewModel)
                }
                composable(Screen.Reports.route) {
                    ReportsScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
