package com.example.analysis.heuristics

import com.example.data.model.FindingSeverity
import com.example.data.model.ManifestSummary
import com.example.data.model.MethodDetail
import com.example.data.model.SecurityFinding
import com.example.data.model.StringEntry

object SecurityRulesEngine {

    fun evaluate(
        manifest: ManifestSummary,
        strings: List<StringEntry>,
        methods: List<MethodDetail>
    ): Pair<Int, List<SecurityFinding>> {
        val findings = mutableListOf<SecurityFinding>()

        // 1. Debuggable check
        if (manifest.isDebuggable) {
            findings.add(
                SecurityFinding(
                    id = "SEC-001",
                    severity = FindingSeverity.CRITICAL,
                    title = "Debuggable Flag Enabled",
                    titleAr = "وضع تصحيح الأخطاء (Debuggable) مفعّل",
                    description = "The application package is compiled with android:debuggable='true'. This enables JDWP debuggers to attach directly to the runtime process.",
                    descriptionAr = "تم تجميع التطبيق بخاصية android:debuggable='true'. يتيح ذلك ربط مصحح أخطاء JDWP بالعملية وفحص الذاكرة وتعديل التدفق البرمجي أثناء التشغيل.",
                    evidence = "AndroidManifest.xml -> <application android:debuggable='true'>",
                    targetClass = "AndroidManifest.xml",
                    confidence = 100,
                    whyItMatters = "Any user with adb access can dump memory, inspect cryptographic keys, and hijack method return values.",
                    whyItMattersAr = "يمكن لأي شخص عبر ADB قراءة الذاكرة بالكامل واستخراج المفاتيح وتجاوز شاشات التحقق.",
                    recommendation = "Disable debugging in release builds: set 'android:debuggable=\"false\"' or ensure 'isMinifyEnabled = true / isDebuggable = false' in Gradle.",
                    recommendationAr = "عطّل وضع التصحيح في نسخ الإنتاج داخل build.gradle عبر التأكد من debuggable = false.",
                    isHeuristic = false
                )
            )
        }

        // 2. Cleartext traffic
        if (manifest.usesCleartextTraffic) {
            findings.add(
                SecurityFinding(
                    id = "SEC-002",
                    severity = FindingSeverity.HIGH,
                    title = "Cleartext HTTP Traffic Allowed",
                    titleAr = "السماح بحركة مرور غير مشفرة (Cleartext HTTP)",
                    description = "The application allows unencrypted HTTP network communications via android:usesCleartextTraffic='true'.",
                    descriptionAr = "يسمح التطبيق بإجراء اتصالات شبكية غير مشفرة عبر HTTP عبر تعيين usesCleartextTraffic='true'.",
                    evidence = "AndroidManifest.xml -> <application android:usesCleartextTraffic='true'>",
                    targetClass = "AndroidManifest.xml",
                    confidence = 100,
                    whyItMatters = "Network traffic is vulnerable to eavesdropping and Man-in-the-Middle (MitM) content injection on untrusted Wi-Fi networks.",
                    whyItMattersAr = "تكون البيانات المنقولة عرضة للتنصت وهجمات رجل في المنتصف (MitM) على الشبكات غير الموثوقة.",
                    recommendation = "Enforce HTTPS by setting 'android:usesCleartextTraffic=\"false\"' and implementing a restrictive Network Security Config.",
                    recommendationAr = "افرض بروتوكول HTTPS المشفر وقم بتعيين usesCleartextTraffic='false'.",
                    isHeuristic = false
                )
            )
        }

        // 3. AllowBackup enabled
        if (manifest.allowBackup) {
            findings.add(
                SecurityFinding(
                    id = "SEC-003",
                    severity = FindingSeverity.MEDIUM,
                    title = "Application Backup Enabled",
                    titleAr = "النسخ الاحتياطي للتطبيق مفعّل (allowBackup)",
                    description = "The application allows system backup of its private sandbox storage via 'adb backup' without explicit exclusion rules.",
                    descriptionAr = "يسمح التطبيق بأخذ نسخة احتياطية من ملفاته وبياناته الخاصة عبر ADB Backup دون تحديد قواعد استثناء.",
                    evidence = "AndroidManifest.xml -> <application android:allowBackup='true'>",
                    targetClass = "AndroidManifest.xml",
                    confidence = 95,
                    whyItMatters = "Attackers with physical access can extract private databases, SharedPreferences, and cached tokens.",
                    whyItMattersAr = "يمكن استخراج قواعد البيانات وملفات التخزين المؤقتة والمفاتيح عبر النسخ الاحتياطي لجهاز متصل.",
                    recommendation = "Set 'android:allowBackup=\"false\"' or define an XML data_extraction_rules to exclude sensitive credentials.",
                    recommendationAr = "قم بتعيين allowBackup='false' أو حدد ملف data_extraction_rules لاستثناء البيانات الحساسة.",
                    isHeuristic = false
                )
            )
        }

        // 4. Exported components without permission
        val riskyComponents = manifest.components.filter { it.isRisky }
        if (riskyComponents.isNotEmpty()) {
            val names = riskyComponents.take(4).joinToString(", ") { "${it.type}: ${it.name.substringAfterLast('.')}" }
            findings.add(
                SecurityFinding(
                    id = "SEC-004",
                    severity = FindingSeverity.HIGH,
                    title = "Exported Components Without Permission Barrier",
                    titleAr = "مكونات معلنة للتصدير دون حماية أذونات",
                    description = "Detected ${riskyComponents.size} exported components (Activities, Services, or Receivers) accessible by third-party apps without permission restrictions.",
                    descriptionAr = "تم اكتشاف ${riskyComponents.size} مكوناً قابلاً للاستدعاء الخارجي دون فرض متطلبات صلاحيات أمان.",
                    evidence = names,
                    targetClass = riskyComponents.first().name,
                    confidence = 90,
                    whyItMatters = "Malicious applications installed on the same device can invoke these components to trigger unintended workflows or access internal state.",
                    whyItMattersAr = "تستطيع تطبيقات خبيثة على نفس الهاتف استدعاء هذه المكونات وتنفيذ عمليات داخلية دون إذن المستخدم.",
                    recommendation = "Add 'android:exported=\"false\"' or protect exported components with custom 'android:permission' attributes.",
                    recommendationAr = "عيّن exported='false' أو أضف إذن حماية مخصص android:permission للمكونات المفتوحة.",
                    isHeuristic = false
                )
            )
        }

        // 5. Dangerous permissions
        val dangerousPerms = manifest.permissions.filter { it.isDangerous }
        if (dangerousPerms.size >= 4) {
            val samplePerms = dangerousPerms.take(4).joinToString(", ") { it.name.substringAfterLast('.') }
            findings.add(
                SecurityFinding(
                    id = "SEC-005",
                    severity = FindingSeverity.MEDIUM,
                    title = "High Surface of Sensitive Permissions",
                    titleAr = "عدد مرتفع من الأذونات الحساسة المطلوبة",
                    description = "The application requests ${dangerousPerms.size} high-privilege Android runtime permissions.",
                    descriptionAr = "يطلب التطبيق ${dangerousPerms.size} أذونات أمان حساسة وعالية الصلاحيات.",
                    evidence = samplePerms,
                    targetClass = "AndroidManifest.xml",
                    confidence = 85,
                    whyItMatters = "Excessive permissions increase the attack blast radius if the application is compromised.",
                    whyItMattersAr = "كثرة الصلاحيات توسّع نطاق الأضرار في حال اختراق أي مكتبة أو مكوّن داخل التطبيق.",
                    recommendation = "Apply the Principle of Least Privilege: remove unused permissions and rely on system photo/document pickers.",
                    recommendationAr = "طبق مبدأ الحد الأدنى من الصلاحيات واستخدم منتقي الملفات والصور المدمج بالنظام بدلاً من طلب الأذونات المباشرة.",
                    isHeuristic = false
                )
            )
        }

        // 6. Hardcoded Secrets in Strings
        val apiKeyRegex = Regex("(?i)(api[_-]?key|secret|private[_-]?key|access[_-]?token|bearer)[\\s:=]{1,4}[\"']([A-Za-z0-9_\\-\\.\\=]{16,})[\"']")
        val googleKeyRegex = Regex("AIza[0-9A-Za-z-_]{35}")
        val awsKeyRegex = Regex("AKIA[0-9A-Z]{16}")

        for (entry in strings) {
            val str = entry.value
            val gMatch = googleKeyRegex.find(str)
            val aMatch = awsKeyRegex.find(str)
            val sMatch = apiKeyRegex.find(str)

            if (gMatch != null) {
                findings.add(
                    SecurityFinding(
                        id = "SEC-006",
                        severity = FindingSeverity.CRITICAL,
                        title = "Embedded Google Cloud API Key Pattern",
                        titleAr = "اكتشاف نمط مفتاح Google Cloud API مضمّن",
                        description = "Detected potential embedded Google API key pattern directly in string pool.",
                        descriptionAr = "تم العثور على نمط يشبه مفتاح Google API مضمناً ومكشوفاً داخل مجمع نصوص DEX.",
                        evidence = "Value: ${str.take(12)}... (Dex: ${entry.dexFile})",
                        targetClass = entry.xrefLocations.firstOrNull() ?: "DEX String Pool",
                        confidence = 92,
                        whyItMatters = "Embedded keys can be extracted and abused for unauthorized billing or service quotas.",
                        whyItMattersAr = "يمكن استخراج المفاتيح المكشوفة واستغلالها لاستهلاك الحصص أو الوصول للخدمات السحابية.",
                        recommendation = "Store sensitive keys securely in server backend or use Secrets Gradle plugin with API key restrictions in Google Cloud Console.",
                        recommendationAr = "قيّد المفتاح بنطاق حزمة التطبيق وبصمة SHA-1 داخل لوحة تحكم Google Cloud Console.",
                        isHeuristic = true
                    )
                )
                break
            } else if (aMatch != null) {
                findings.add(
                    SecurityFinding(
                        id = "SEC-007",
                        severity = FindingSeverity.CRITICAL,
                        title = "Embedded AWS Access Key ID Pattern",
                        titleAr = "اكتشاف نمط مفتاح وصول AWS مضمّن",
                        description = "Detected potential hardcoded AWS credential identifier in bytecode.",
                        descriptionAr = "تم العثور على نمط معرّف وصول AWS Access Key داخل كود التطبيق.",
                        evidence = "AWS Key: ${aMatch.value.take(8)}... (Dex: ${entry.dexFile})",
                        targetClass = entry.xrefLocations.firstOrNull() ?: "DEX String Pool",
                        confidence = 95,
                        whyItMatters = "Direct AWS keys can expose cloud infrastructure if associated secret keys are also recovered.",
                        whyItMattersAr = "مفاتيح AWS المباشرة تعرض الموارد السحابية للخطر في حال تسريبها.",
                        recommendation = "Never store root or IAM credentials in client APKs; use Cognito or backend authentication tokens.",
                        recommendationAr = "لا تضع مفاتيح IAM أو AWS داخل كود العميل واستخدم خوادم وسيطة أو Amazon Cognito.",
                        isHeuristic = true
                    )
                )
                break
            }
        }

        // 7. Weak Cryptography
        val weakCryptoStrings = strings.filter {
            val lower = it.value.lowercase()
            lower == "des" || lower == "desede" || lower == "rc4" || lower.contains("des/cbc") || lower.contains("aes/ecb")
        }
        if (weakCryptoStrings.isNotEmpty()) {
            val cSample = weakCryptoStrings.first()
            findings.add(
                SecurityFinding(
                    id = "SEC-008",
                    severity = FindingSeverity.HIGH,
                    title = "Weak / Deprecated Cipher Usage",
                    titleAr = "استخدام خوارزمية تشفير ضعيفة أو متقادمة",
                    description = "Detected references to deprecated or insecure cryptographic transformations ('${cSample.value}').",
                    descriptionAr = "تم العثور على مراجع لخوارزمية تشفير متقادمة أو غير آمنة ('${cSample.value}').",
                    evidence = "Cipher: ${cSample.value} in ${cSample.dexFile}",
                    targetClass = cSample.xrefLocations.firstOrNull() ?: "DEX Cryptography",
                    confidence = 88,
                    whyItMatters = "Legacy ciphers like DES and ECB mode do not provide semantic security and are susceptible to pattern leakage or practical cryptanalysis.",
                    whyItMattersAr = "خوارزميات مثل DES ووضع ECB تسرب أنماط البيانات وسهلة الكسر مقارنة بالمعايير الحديثة.",
                    recommendation = "Upgrade to standard AES-GCM (256-bit) or ChaCha20-Poly1305 with authenticated encryption.",
                    recommendationAr = "اعتمد على خوارزمية AES-GCM (256-bit) أو Tink Library للعمليات التشفيرية الموثوقة.",
                    isHeuristic = true
                )
            )
        }

        // 8. Insecure WebView configurations
        val webViewMethods = methods.filter { it.smaliCode.contains("setJavaScriptEnabled") || it.smaliCode.contains("addJavascriptInterface") }
        if (webViewMethods.isNotEmpty()) {
            val m = webViewMethods.first()
            findings.add(
                SecurityFinding(
                    id = "SEC-009",
                    severity = FindingSeverity.MEDIUM,
                    title = "Dynamic JavaScript Bridge in WebView",
                    titleAr = "تفعيل جسر جافاسكريبت داخل واجهة WebView",
                    description = "Method '${m.methodName}' configures WebView with JavaScript bridge or enabled JavaScript execution.",
                    descriptionAr = "الدالة '${m.methodName}' تقوم بتفعيل JavaScript أو إضافة واجهة تفاعل جافاسكريبت داخل WebView.",
                    evidence = "${m.className}->${m.methodName}()",
                    targetClass = m.className,
                    targetMethod = m.methodName,
                    confidence = 80,
                    whyItMatters = "If the WebView loads remote or untrusted URLs, XSS vulnerabilities can lead to Cross-App Scripting or unauthorized reflection.",
                    whyItMattersAr = "إذا تم فتح روابط خارجية غير موثوقة، فإن ثغرات XSS قد تؤدي للوصول لميزات التطبيق المحلية.",
                    recommendation = "Restrict WebView URLs with strict origin verification and disable file access APIs unless strictly required.",
                    recommendationAr = "تحقق من النطاقات المسموح بفتحها واقصر WebView على الروابط الداخلية المشفرة.",
                    isHeuristic = true
                )
            )
        }

        // 9. Info-level: Google Play Billing Library detected
        val billingStrings = strings.filter {
            it.value.contains("com.android.vending.billing") || it.value.contains("com.android.billingclient")
        }
        if (billingStrings.isNotEmpty()) {
            findings.add(
                SecurityFinding(
                    id = "SEC-010",
                    severity = FindingSeverity.INFO,
                    title = "Google Play Billing SDK Present",
                    titleAr = "وجود حزمة Google Play Billing الرسمية",
                    description = "The application integrates the official Google Play Billing library for in-app purchases and subscriptions.",
                    descriptionAr = "التطبيق يدمج مكتبة Google Play Billing الرسمية للتعامل مع المشتريات والاشتراكات الرقمية.",
                    evidence = billingStrings.first().value,
                    targetClass = "com.android.billingclient.api",
                    confidence = 98,
                    whyItMatters = "Confirms that purchase workflows exist; verify that purchases are validated server-side rather than relying strictly on client-side state.",
                    whyItMattersAr = "يؤكد وجود تدفقات دفع؛ يجب التحقق من صحة المشتريات على الخادم (Server-side Verification).",
                    recommendation = "Always validate purchase tokens via Google Play Developer API on a secure backend server.",
                    recommendationAr = "تحقق دوماً من صحة رموز الشراء (Purchase Tokens) عبر خادم خلفي آمن وليس على جهاز العميل فقط.",
                    isHeuristic = false
                )
            )
        }

        // Calculate security score
        // Base 100
        var penalty = 0
        for (f in findings) {
            penalty += when (f.severity) {
                FindingSeverity.CRITICAL -> 25
                FindingSeverity.HIGH -> 15
                FindingSeverity.MEDIUM -> 8
                FindingSeverity.LOW -> 4
                FindingSeverity.INFO -> 0
            }
        }
        val score = maxOf(20, minOf(100, 100 - penalty))

        return score to findings
    }
}
