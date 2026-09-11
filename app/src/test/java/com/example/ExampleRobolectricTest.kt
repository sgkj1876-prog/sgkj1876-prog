package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.analysis.heuristics.PurchaseTargetClassifier
import com.example.analysis.heuristics.SecurityRulesEngine
import com.example.data.model.ManifestSummary
import com.example.data.model.MethodDetail
import com.example.data.model.StringCategory
import com.example.data.model.StringEntry
import com.example.data.model.TargetConfidenceLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("APK Sentinel", appName)
    }

    @Test
    fun `test launch MainActivity`() {
        org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    }

    @Test
    fun `test purchase target classifier discovers top billing targets`() {
        val testStrings = listOf(
            StringEntry(1, "onPurchasesUpdated", StringCategory.BILLING, 3, "classes.dex", 4, listOf("com.target.Billing->onPurchasesUpdated")),
            StringEntry(2, "PurchaseState", StringCategory.BILLING, 2, "classes.dex", 3, listOf("com.target.Billing->getState")),
            StringEntry(3, "PURCHASED", StringCategory.SUBSCRIPTION, 4, "classes.dex", 2, listOf("com.target.Billing->isPurchased"))
        )
        val targets = PurchaseTargetClassifier.discoverTargets(testStrings, emptyList())
        assertTrue("Targets list should not be empty", targets.isNotEmpty())
        val topTarget = targets.first()
        assertEquals("onPurchasesUpdated", topTarget.identifier)
        assertEquals(TargetConfidenceLevel.CRITICAL, topTarget.confidenceLevel)
        assertTrue("Confidence should be >= 90", topTarget.confidence >= 90)
    }

    @Test
    fun `test security rules engine flags debuggable and cleartext`() {
        val manifest = ManifestSummary(
            packageName = "com.vuln.target",
            versionName = "1.0",
            versionCode = 1,
            minSdkVersion = 24,
            targetSdkVersion = 34,
            isDebuggable = true,
            allowBackup = true,
            usesCleartextTraffic = true,
            permissions = emptyList(),
            components = emptyList()
        )
        val (score, findings) = SecurityRulesEngine.evaluate(manifest, emptyList(), emptyList())
        assertTrue("Security score should be degraded", score < 100)
        assertTrue("Should detect debuggable", findings.any { it.id == "SEC-001" })
        assertTrue("Should detect cleartext traffic", findings.any { it.id == "SEC-002" })
    }
}
