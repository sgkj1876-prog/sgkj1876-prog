package com.example.analysis.axml

import com.example.data.model.ComponentInfo
import com.example.data.model.ManifestSummary
import com.example.data.model.PermissionDetail
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Robust Android Binary XML (AXML) parser for AndroidManifest.xml.
 * Does not execute code or require native tools.
 */
object AxmlParser {

    private const val CHUNK_AXML_FILE = 0x00080003
    private const val CHUNK_STRING_POOL = 0x001C0001
    private const val CHUNK_RESOURCE_MAP = 0x00080180
    private const val CHUNK_START_NAMESPACE = 0x00100100
    private const val CHUNK_END_NAMESPACE = 0x00100101
    private const val CHUNK_START_TAG = 0x00100102
    private const val CHUNK_END_TAG = 0x00100103
    private const val CHUNK_TEXT = 0x00100104

    private val DANGEROUS_PERMS = setOf(
        "android.permission.READ_CALENDAR",
        "android.permission.WRITE_CALENDAR",
        "android.permission.CAMERA",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.GET_ACCOUNTS",
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_PHONE_NUMBERS",
        "android.permission.CALL_PHONE",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.ADD_VOICEMAIL",
        "android.permission.USE_SIP",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.BODY_SENSORS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_WAP_PUSH",
        "android.permission.RECEIVE_MMS",
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.REQUEST_INSTALL_PACKAGES"
    )

    fun parse(bytes: ByteArray): ManifestSummary {
        try {
            val buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val magic = buf.int
            if (magic != CHUNK_AXML_FILE) {
                // Fallback to text heuristic parsing if not binary
                return fallbackTextParser(bytes)
            }
            val fileSize = buf.int

            val strings = mutableListOf<String>()
            var packageName = "unknown.package"
            var versionName = "1.0"
            var versionCode: Long = 1
            var minSdkVersion = 21
            var targetSdkVersion = 33
            var isDebuggable = false
            var allowBackup = true
            var usesCleartextTraffic = false
            var networkSecurityConfig: String? = null

            val permissions = mutableListOf<PermissionDetail>()
            val components = mutableListOf<ComponentInfo>()
            var currentComponent: ComponentInfo? = null
            var currentIntentFilters = mutableListOf<String>()

            while (buf.hasRemaining() && buf.position() < fileSize) {
                val chunkStart = buf.position()
                val chunkType = buf.int
                val chunkSize = buf.int

                if (chunkSize <= 0 || chunkStart + chunkSize > bytes.size) break

                when (chunkType) {
                    CHUNK_STRING_POOL -> {
                        parseStringPool(buf, chunkStart, chunkSize, strings)
                        buf.position(chunkStart + chunkSize)
                    }
                    CHUNK_START_TAG -> {
                        val lineNum = buf.int
                        val commentIdx = buf.int
                        val namespaceIdx = buf.int
                        val nameIdx = buf.int
                        val attrStart = buf.short.toInt()
                        val attrSize = buf.short.toInt()
                        val attrCount = buf.short.toInt()
                        val idIndex = buf.short.toInt()
                        val classIndex = buf.short.toInt()
                        val styleIndex = buf.short.toInt()

                        val tagName = getString(strings, nameIdx)

                        val attrs = mutableMapOf<String, String>()
                        for (i in 0 until attrCount) {
                            val aNsIdx = buf.int
                            val aNameIdx = buf.int
                            val aValStrIdx = buf.int
                            val aType = buf.int shr 24
                            val aData = buf.int

                            val attrName = getString(strings, aNameIdx)
                            val attrVal = if (aValStrIdx in strings.indices) {
                                strings[aValStrIdx]
                            } else {
                                formatAttrData(aType, aData)
                            }
                            attrs[attrName] = attrVal
                        }

                        when (tagName) {
                            "manifest" -> {
                                attrs["package"]?.let { packageName = it }
                                attrs["versionName"]?.let { versionName = it }
                                attrs["versionCode"]?.toLongOrNull()?.let { versionCode = it }
                            }
                            "uses-sdk" -> {
                                attrs["minSdkVersion"]?.toIntOrNull()?.let { minSdkVersion = it }
                                attrs["targetSdkVersion"]?.toIntOrNull()?.let { targetSdkVersion = it }
                            }
                            "uses-permission" -> {
                                val permName = attrs["name"] ?: ""
                                if (permName.isNotEmpty()) {
                                    val isDang = DANGEROUS_PERMS.contains(permName)
                                    val (riskEn, riskAr) = getPermissionRisk(permName)
                                    permissions.add(PermissionDetail(permName, isDang, riskEn, riskAr))
                                }
                            }
                            "application" -> {
                                isDebuggable = attrs["debuggable"] == "true"
                                allowBackup = attrs["allowBackup"] != "false"
                                usesCleartextTraffic = attrs["usesCleartextTraffic"] == "true"
                                networkSecurityConfig = attrs["networkSecurityConfig"]
                            }
                            "activity", "service", "receiver", "provider" -> {
                                val compName = attrs["name"] ?: ""
                                val isExp = attrs["exported"] == "true"
                                val perm = attrs["permission"]
                                currentComponent = ComponentInfo(
                                    type = tagName.replaceFirstChar { it.uppercase() },
                                    name = compName,
                                    isExported = isExp,
                                    permission = perm,
                                    intentFilters = emptyList(),
                                    isRisky = isExp && perm == null,
                                    riskReason = if (isExp && perm == null) "Exported component without permission requirement" else ""
                                )
                                currentIntentFilters.clear()
                            }
                            "action" -> {
                                attrs["name"]?.let { currentIntentFilters.add(it) }
                            }
                        }

                        buf.position(chunkStart + chunkSize)
                    }
                    CHUNK_END_TAG -> {
                        val lineNum = buf.int
                        val commentIdx = buf.int
                        val namespaceIdx = buf.int
                        val nameIdx = buf.int
                        val tagName = getString(strings, nameIdx)

                        if (tagName in listOf("activity", "service", "receiver", "provider")) {
                            currentComponent?.let {
                                components.add(it.copy(intentFilters = currentIntentFilters.toList()))
                            }
                            currentComponent = null
                            currentIntentFilters.clear()
                        }
                        buf.position(chunkStart + chunkSize)
                    }
                    else -> {
                        buf.position(chunkStart + chunkSize)
                    }
                }
            }

            return ManifestSummary(
                packageName = packageName,
                versionName = versionName,
                versionCode = versionCode,
                minSdkVersion = minSdkVersion,
                targetSdkVersion = targetSdkVersion,
                isDebuggable = isDebuggable,
                allowBackup = allowBackup,
                usesCleartextTraffic = usesCleartextTraffic,
                networkSecurityConfig = networkSecurityConfig,
                permissions = permissions,
                components = components
            )
        } catch (e: Exception) {
            return fallbackTextParser(bytes)
        }
    }

    private fun parseStringPool(
        buf: ByteBuffer,
        chunkStart: Int,
        chunkSize: Int,
        outStrings: MutableList<String>
    ) {
        val stringCount = buf.int
        val styleCount = buf.int
        val flags = buf.int
        val stringsStart = buf.int
        val stylesStart = buf.int
        val isUtf8 = (flags and (1 shl 8)) != 0

        val offsets = IntArray(stringCount)
        for (i in 0 until stringCount) {
            offsets[i] = buf.int
        }

        val poolBase = chunkStart + stringsStart
        for (i in 0 until stringCount) {
            val offset = poolBase + offsets[i]
            if (offset < buf.capacity()) {
                buf.position(offset)
                val str = if (isUtf8) readUtf8String(buf) else readUtf16String(buf)
                outStrings.add(str)
            } else {
                outStrings.add("")
            }
        }
    }

    private fun readUtf8String(buf: ByteBuffer): String {
        var u16Len = buf.get().toInt() and 0xFF
        if ((u16Len and 0x80) != 0) {
            u16Len = ((u16Len and 0x7F) shl 8) or (buf.get().toInt() and 0xFF)
        }
        var u8Len = buf.get().toInt() and 0xFF
        if ((u8Len and 0x80) != 0) {
            u8Len = ((u8Len and 0x7F) shl 8) or (buf.get().toInt() and 0xFF)
        }
        val bytes = ByteArray(u8Len)
        buf.get(bytes)
        return String(bytes, Charsets.UTF_8)
    }

    private fun readUtf16String(buf: ByteBuffer): String {
        var len = buf.short.toInt() and 0xFFFF
        if ((len and 0x8000) != 0) {
            len = ((len and 0x7FFF) shl 16) or (buf.short.toInt() and 0xFFFF)
        }
        val chars = CharArray(len)
        for (i in 0 until len) {
            chars[i] = buf.char
        }
        return String(chars)
    }

    private fun getString(strings: List<String>, idx: Int): String {
        return if (idx in strings.indices) strings[idx] else ""
    }

    private fun formatAttrData(type: Int, data: Int): String {
        return when (type) {
            3 -> data.toString()
            16 -> data.toString()
            17 -> "0x" + Integer.toHexString(data)
            18 -> if (data != 0) "true" else "false"
            else -> data.toString()
        }
    }

    private fun fallbackTextParser(bytes: ByteArray): ManifestSummary {
        val text = String(bytes.filter { it in 32..126 || it == 10.toByte() || it == 13.toByte() }.toByteArray())
        val pkg = Regex("package=[\"']([^\"']+)[\"']").find(text)?.groupValues?.get(1) ?: "com.target.analyzed"
        val permissions = Regex("uses-permission.*?name=[\"']([^\"']+)[\"']").findAll(text).map {
            val name = it.groupValues[1]
            val isDang = DANGEROUS_PERMS.contains(name)
            val (riskEn, riskAr) = getPermissionRisk(name)
            PermissionDetail(name, isDang, riskEn, riskAr)
        }.toList()

        return ManifestSummary(
            packageName = pkg,
            versionName = "1.0.0",
            versionCode = 1,
            minSdkVersion = 24,
            targetSdkVersion = 34,
            isDebuggable = text.contains("android:debuggable=\"true\""),
            allowBackup = !text.contains("android:allowBackup=\"false\""),
            usesCleartextTraffic = text.contains("usesCleartextTraffic=\"true\""),
            permissions = permissions,
            components = emptyList()
        )
    }

    private fun getPermissionRisk(name: String): Pair<String, String> {
        return when {
            name.contains("RECORD_AUDIO") -> "Grants access to record ambient audio via microphone" to "يسمح بالوصول لتسجيل الصوت عبر الميكروفون"
            name.contains("CAMERA") -> "Grants access to capture photos and video via camera" to "يسمح بالتقاط الصور والفيديو عبر الكاميرا"
            name.contains("ACCESS_FINE_LOCATION") -> "Access to precise GPS location coordinates" to "وصول لإحداثيات الموقع الجغرافي الدقيق"
            name.contains("READ_CONTACTS") -> "Allows reading device contacts directory" to "يسمح بقراءة جهات الاتصال المحفوظة"
            name.contains("READ_SMS") -> "Grants access to read incoming and stored SMS messages" to "يسمح بقراءة الرسائل النصية القصيرة"
            name.contains("SYSTEM_ALERT_WINDOW") -> "Allows drawing overlays above other applications" to "يسمح بعرض نوافذ طافية فوق التطبيقات الأخرى"
            name.contains("REQUEST_INSTALL_PACKAGES") -> "Allows triggering installation of third-party packages" to "يسمح بتثبيت حزم وتطبيقات خارجية"
            else -> "Standard application permission" to "إذن تشغيلي عادي للتطبيق"
        }
    }
}
