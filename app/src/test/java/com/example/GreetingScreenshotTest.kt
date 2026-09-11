package com.example

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import com.example.analysis.AnalysisProgress
import com.example.ui.components.ApkParsingCircularProgress
import com.example.ui.theme.ApkSentinelTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      ApkSentinelTheme {
        Text("APK Sentinel Security Static Analyzer")
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun test_circular_progress_displays_during_parsing() {
    val sampleProgress = AnalysisProgress(
      stageIndex = 4,
      totalStages = 8,
      stageNameEn = "Analyzing bytecode in classes.dex",
      stageNameAr = "تحليل كود Dalvik في classes.dex",
      percentage = 0.5f
    )
    composeTestRule.setContent {
      ApkSentinelTheme {
        ApkParsingCircularProgress(
          progress = sampleProgress,
          isArabic = false,
          fileName = "target_app.apk"
        )
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("circular_progress_bar").assertExists()
  }
}
