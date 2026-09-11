package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.analysis.AnalysisState
import com.example.analysis.ApkStaticAnalyzer
import com.example.data.local.AnalysisHistoryDao
import com.example.data.local.AnalysisHistoryEntity
import com.example.data.model.AnalysisResult
import com.example.data.model.FindingSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AnalysisRepository(
    private val context: Context,
    private val historyDao: AnalysisHistoryDao
) {
    private val analyzer = ApkStaticAnalyzer(context)

    private val _currentAnalysis = MutableStateFlow<AnalysisResult?>(null)
    val currentAnalysis: StateFlow<AnalysisResult?> = _currentAnalysis.asStateFlow()

    val allHistory: Flow<List<AnalysisHistoryEntity>> = historyDao.getAllHistory()

    fun setCurrentAnalysis(result: AnalysisResult?) {
        _currentAnalysis.value = result
    }

    fun analyzeApk(uri: Uri, fileName: String): Flow<AnalysisState> {
        return analyzer.analyzeApk(uri, fileName)
    }

    suspend fun saveAnalysisResult(result: AnalysisResult) = withContext(Dispatchers.IO) {
        val criticalCount = result.findings.count { it.severity == FindingSeverity.CRITICAL }
        val highCount = result.findings.count { it.severity == FindingSeverity.HIGH }
        val mediumCount = result.findings.count { it.severity == FindingSeverity.MEDIUM }
        val lowCount = result.findings.count { it.severity == FindingSeverity.LOW }

        val entity = AnalysisHistoryEntity(
            id = result.id,
            fileName = result.metadata.fileName,
            packageName = result.manifest.packageName,
            versionName = result.manifest.versionName,
            versionCode = result.manifest.versionCode,
            fileSize = result.metadata.fileSize,
            sha256Hash = result.metadata.sha256Hash,
            securityScore = result.securityScore,
            riskLevel = result.riskLevel,
            criticalCount = criticalCount,
            highCount = highCount,
            mediumCount = mediumCount,
            lowCount = lowCount,
            dexCount = result.dexFiles.size,
            classCount = result.totalClasses,
            methodCount = result.totalMethods,
            stringCount = result.totalStrings,
            purchaseTargetCount = result.purchaseTargets.size,
            timestamp = result.timestamp
        )
        historyDao.insertHistory(entity)
        _currentAnalysis.value = result
    }

    suspend fun deleteHistory(id: String) = withContext(Dispatchers.IO) {
        historyDao.deleteHistoryById(id)
        if (_currentAnalysis.value?.id == id) {
            _currentAnalysis.value = null
        }
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        historyDao.clearAllHistory()
        _currentAnalysis.value = null
    }

    fun generateJsonReport(result: AnalysisResult): String {
        val root = JSONObject()
        root.put("reportTitle", "APK Sentinel Security Static Intelligence Report")
        root.put("analysisTimestamp", result.timestamp)
        root.put("securityScore", result.securityScore)
        root.put("riskLevel", result.riskLevel)

        val metadataObj = JSONObject().apply {
            put("fileName", result.metadata.fileName)
            put("fileSize", result.metadata.fileSize)
            put("md5", result.metadata.md5Hash)
            put("sha256", result.metadata.sha256Hash)
        }
        root.put("metadata", metadataObj)

        val manifestObj = JSONObject().apply {
            put("packageName", result.manifest.packageName)
            put("versionName", result.manifest.versionName)
            put("versionCode", result.manifest.versionCode)
            put("minSdkVersion", result.manifest.minSdkVersion)
            put("targetSdkVersion", result.manifest.targetSdkVersion)
            put("debuggable", result.manifest.isDebuggable)
            put("allowBackup", result.manifest.allowBackup)
            put("usesCleartextTraffic", result.manifest.usesCleartextTraffic)
        }
        root.put("manifest", manifestObj)

        val findingsArr = JSONArray()
        for (f in result.findings) {
            findingsArr.put(JSONObject().apply {
                put("id", f.id)
                put("severity", f.severity.name)
                put("title", f.title)
                put("description", f.description)
                put("evidence", f.evidence)
                put("confidence", f.confidence)
                put("recommendation", f.recommendation)
                put("isHeuristic", f.isHeuristic)
            })
        }
        root.put("findings", findingsArr)

        val targetsArr = JSONArray()
        for (t in result.purchaseTargets) {
            targetsArr.put(JSONObject().apply {
                put("rank", t.rank)
                put("identifier", t.identifier)
                put("category", t.category)
                put("confidence", t.confidence)
                put("occurrences", t.occurrences)
                put("referenceCount", t.referenceCount)
                put("targetClass", t.targetClass)
            })
        }
        root.put("purchaseTargets", targetsArr)

        return root.toString(2)
    }

    fun generateTextReport(result: AnalysisResult): String {
        val sb = StringBuilder()
        sb.append("=====================================================\n")
        sb.append("   APK SENTINEL - STATIC SECURITY INTELLIGENCE REPORT\n")
        sb.append("=====================================================\n\n")
        sb.append("Target File:     ${result.metadata.fileName}\n")
        sb.append("Package Name:    ${result.manifest.packageName}\n")
        sb.append("Version:         ${result.manifest.versionName} (${result.manifest.versionCode})\n")
        sb.append("File Size:       ${result.metadata.fileSize} bytes\n")
        sb.append("SHA-256:         ${result.metadata.sha256Hash}\n")
        sb.append("Security Score:  ${result.securityScore} / 100 [${result.riskLevel}]\n")
        sb.append("Total Classes:   ${result.totalClasses}\n")
        sb.append("Total Methods:   ${result.totalMethods}\n")
        sb.append("Total Strings:   ${result.totalStrings}\n\n")

        sb.append("-----------------------------------------------------\n")
        sb.append("1. SECURITY FINDINGS (${result.findings.size})\n")
        sb.append("-----------------------------------------------------\n")
        for (f in result.findings) {
            sb.append("[${f.severity.name}] ${f.title}\n")
            sb.append("  Evidence:       ${f.evidence}\n")
            sb.append("  Confidence:     ${f.confidence}%\n")
            sb.append("  Description:    ${f.description}\n")
            sb.append("  Recommendation: ${f.recommendation}\n\n")
        }

        sb.append("-----------------------------------------------------\n")
        sb.append("2. TOP 10 PURCHASE-RELATED TARGETS (HEURISTIC)\n")
        sb.append("-----------------------------------------------------\n")
        for (t in result.purchaseTargets) {
            sb.append("#${t.rank} [${t.confidence}% ${t.confidenceLevel.labelEn}] ${t.identifier}\n")
            sb.append("  Category:   ${t.category}\n")
            sb.append("  References: ${t.referenceCount} | Occurrences: ${t.occurrences}\n")
            sb.append("  Class:      ${t.targetClass}\n\n")
        }

        sb.append("=====================================================\n")
        sb.append("End of Report. Generated locally by APK Sentinel.\n")
        return sb.toString()
    }
}
