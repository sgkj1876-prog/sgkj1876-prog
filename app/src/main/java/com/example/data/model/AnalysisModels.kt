package com.example.data.model

enum class FindingSeverity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    INFO
}

data class SecurityFinding(
    val id: String,
    val severity: FindingSeverity,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val evidence: String,
    val targetClass: String = "",
    val targetMethod: String = "",
    val confidence: Int, // 0 - 100
    val whyItMatters: String,
    val whyItMattersAr: String,
    val recommendation: String,
    val recommendationAr: String,
    val isHeuristic: Boolean = false
)

enum class TargetConfidenceLevel(val labelEn: String, val labelAr: String) {
    CRITICAL("CRITICAL RELEVANCE", "صلة حرجة"),
    HIGH("HIGH RELEVANCE", "صلة عالية"),
    MEDIUM("MEDIUM RELEVANCE", "صلة متوسطة"),
    LOW("LOW RELEVANCE", "صلة منخفضة"),
    INFO("INFORMATIONAL", "معلوماتية")
}

data class PurchaseTarget(
    val rank: Int,
    val identifier: String,
    val category: String, // "VIP / PRO", "Google Billing / IAP", "Ads & Premium", "Subscriptions"
    val confidence: Int, // percentage 0 - 100
    val confidenceLevel: TargetConfidenceLevel,
    val occurrences: Int,
    val referenceCount: Int,
    val targetClass: String = "",
    val targetMethod: String = "",
    val sampleSmali: String = "",
    val callerMethods: List<String> = emptyList()
)

enum class StringCategory(val labelEn: String, val labelAr: String) {
    ALL("All", "الكل"),
    URL("URL / Network", "روابط وعناوين"),
    DOMAIN("Domain", "نطاقات"),
    EMAIL("Email", "بريد إلكتروني"),
    API("API Endpoint", "نقاط نهاية API"),
    BILLING("Billing / IAP", "مشتريات وفواتير"),
    SUBSCRIPTION("Subscription", "اشتراكات"),
    SECURITY("Security / Auth", "أمان ومصادقة"),
    SYSTEM("System / OS", "نظام وأندرويد"),
    GENERAL("General", "عام")
}

data class StringEntry(
    val id: Int,
    val value: String,
    val category: StringCategory,
    val occurrences: Int,
    val dexFile: String,
    val xrefCount: Int = 0,
    val xrefLocations: List<String> = emptyList()
)

data class MethodXref(
    val sourceClass: String,
    val sourceMethod: String,
    val instructionOffset: Int,
    val opcode: String
)

data class MethodDetail(
    val className: String,
    val methodName: String,
    val signature: String,
    val accessFlags: String,
    val returnType: String,
    val parameterTypes: List<String>,
    val dexName: String,
    val methodIndex: Int,
    val smaliCode: String,
    val stringReferences: List<String> = emptyList(),
    val calledMethods: List<String> = emptyList(),
    val callers: List<MethodXref> = emptyList()
)

data class ComponentInfo(
    val type: String, // Activity, Service, Receiver, Provider
    val name: String,
    val isExported: Boolean,
    val permission: String? = null,
    val intentFilters: List<String> = emptyList(),
    val isRisky: Boolean = false,
    val riskReason: String = ""
)

data class ManifestSummary(
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val compileSdkVersion: Int = 0,
    val isDebuggable: Boolean,
    val allowBackup: Boolean,
    val usesCleartextTraffic: Boolean,
    val networkSecurityConfig: String? = null,
    val permissions: List<PermissionDetail> = emptyList(),
    val components: List<ComponentInfo> = emptyList()
)

data class PermissionDetail(
    val name: String,
    val isDangerous: Boolean,
    val riskDescription: String = "",
    val riskDescriptionAr: String = ""
)

data class DexSummary(
    val fileName: String,
    val fileSize: Long,
    val classCount: Int,
    val methodCount: Int,
    val fieldCount: Int,
    val stringCount: Int,
    val headerVersion: String
)

data class ApkMetadata(
    val fileName: String,
    val fileSize: Long,
    val md5Hash: String,
    val sha256Hash: String,
    val certificateIssuer: String = "",
    val isSigned: Boolean = true
)

data class AnalysisResult(
    val id: String,
    val metadata: ApkMetadata,
    val manifest: ManifestSummary,
    val dexFiles: List<DexSummary>,
    val totalClasses: Int,
    val totalMethods: Int,
    val totalStrings: Int,
    val securityScore: Int, // 0 - 100
    val riskLevel: String,
    val riskLevelAr: String,
    val findings: List<SecurityFinding>,
    val purchaseTargets: List<PurchaseTarget>,
    val strings: List<StringEntry>,
    val analyzedMethods: List<MethodDetail>,
    val timestamp: Long = System.currentTimeMillis()
)
