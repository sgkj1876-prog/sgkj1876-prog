package com.example.analysis.dex

import com.example.data.model.DexSummary
import com.example.data.model.MethodDetail
import com.example.data.model.MethodXref
import com.example.data.model.StringCategory
import com.example.data.model.StringEntry
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class RawMethod(
    val methodIndex: Int,
    val classDescriptor: String,
    val name: String,
    val returnType: String,
    val paramTypes: List<String>,
    val accessFlags: Int,
    val codeOffset: Int,
    val insns: ShortArray? = null,
    val registersSize: Int = 0
)

data class DexParseResult(
    val summary: DexSummary,
    val strings: List<StringEntry>,
    val methods: List<MethodDetail>,
    val stringXrefs: Map<String, List<MethodXref>>,
    val allClasses: List<String>
)

object DexParser {

    private const val DEX_MAGIC_PREFIX = 0x6465780a // "dex\n"

    fun parse(dexBytes: ByteArray, dexFileName: String): DexParseResult {
        val buf = ByteBuffer.wrap(dexBytes).order(ByteOrder.LITTLE_ENDIAN)

        if (dexBytes.size < 0x70) {
            throw IllegalArgumentException("Invalid DEX file size: ${dexBytes.size} bytes")
        }

        val magicPrefix = buf.int
        if (magicPrefix != DEX_MAGIC_PREFIX && magicPrefix != 0x0a786564) {
            // Check byte signature
            val magicStr = String(dexBytes, 0, minOf(8, dexBytes.size))
            if (!magicStr.startsWith("dex\n")) {
                throw IllegalArgumentException("Not a valid Dalvik Executable (DEX) header")
            }
        }

        buf.position(0x20)
        val fileSize = buf.int.toLong() and 0xFFFFFFFFL
        val headerSize = buf.int

        buf.position(0x38)
        val stringIdsSize = buf.int
        val stringIdsOff = buf.int
        val typeIdsSize = buf.int
        val typeIdsOff = buf.int
        val protoIdsSize = buf.int
        val protoIdsOff = buf.int
        val fieldIdsSize = buf.int
        val fieldIdsOff = buf.int
        val methodIdsSize = buf.int
        val methodIdsOff = buf.int
        val classDefsSize = buf.int
        val classDefsOff = buf.int

        // Read string IDs
        val stringOffsets = IntArray(minOf(stringIdsSize, 200_000))
        buf.position(stringIdsOff)
        for (i in stringOffsets.indices) {
            stringOffsets[i] = buf.int
        }

        // Parse Strings safely
        val stringPool = ArrayList<String>(stringOffsets.size)
        for (off in stringOffsets) {
            if (off in 0 until dexBytes.size) {
                buf.position(off)
                val str = readMutf8(buf, dexBytes)
                stringPool.add(str)
            } else {
                stringPool.add("")
            }
        }

        // Read Type IDs
        val typeIds = IntArray(minOf(typeIdsSize, 100_000))
        if (typeIdsOff in 0 until dexBytes.size) {
            buf.position(typeIdsOff)
            for (i in typeIds.indices) {
                typeIds[i] = buf.int
            }
        }

        fun getTypeDescriptor(typeIdx: Int): String {
            return if (typeIdx in typeIds.indices) {
                val strIdx = typeIds[typeIdx]
                if (strIdx in stringPool.indices) stringPool[strIdx] else "Lunknown/Type;"
            } else "Lunknown/Type;"
        }

        // Read Protos
        data class ProtoInfo(val returnType: String, val params: List<String>)
        val protos = ArrayList<ProtoInfo>(minOf(protoIdsSize, 50_000))
        if (protoIdsOff in 0 until dexBytes.size) {
            buf.position(protoIdsOff)
            for (i in 0 until minOf(protoIdsSize, 50_000)) {
                val shortyIdx = buf.int
                val returnTypeIdx = buf.int
                val paramsOff = buf.int
                val retType = getTypeDescriptor(returnTypeIdx)
                val params = mutableListOf<String>()
                if (paramsOff > 0 && paramsOff < dexBytes.size) {
                    val savedPos = buf.position()
                    buf.position(paramsOff)
                    val paramCount = buf.int
                    for (p in 0 until minOf(paramCount, 50)) {
                        val pTypeIdx = buf.short.toInt() and 0xFFFF
                        params.add(getTypeDescriptor(pTypeIdx))
                    }
                    buf.position(savedPos)
                }
                protos.add(ProtoInfo(retType, params))
            }
        }

        // Read Method IDs
        data class MethodIdInfo(val classDesc: String, val name: String, val proto: ProtoInfo)
        val methodIds = ArrayList<MethodIdInfo>(minOf(methodIdsSize, 100_000))
        if (methodIdsOff in 0 until dexBytes.size) {
            buf.position(methodIdsOff)
            for (i in 0 until minOf(methodIdsSize, 100_000)) {
                val classIdx = buf.short.toInt() and 0xFFFF
                val protoIdx = buf.short.toInt() and 0xFFFF
                val nameIdx = buf.int
                val classDesc = getTypeDescriptor(classIdx)
                val name = if (nameIdx in stringPool.indices) stringPool[nameIdx] else "method$i"
                val proto = if (protoIdx in protos.indices) protos[protoIdx] else ProtoInfo("V", emptyList())
                methodIds.add(MethodIdInfo(classDesc, name, proto))
            }
        }

        // Parse Classes & Methods
        val parsedMethods = mutableListOf<MethodDetail>()
        val stringXrefs = mutableMapOf<String, MutableList<MethodXref>>()
        val allClasses = mutableListOf<String>()

        if (classDefsOff in 0 until dexBytes.size) {
            for (i in 0 until minOf(classDefsSize, 20_000)) {
                val defPos = classDefsOff + i * 32
                if (defPos + 32 > dexBytes.size) break
                buf.position(defPos)
                val classIdx = buf.int
                val accessFlags = buf.int
                val superclassIdx = buf.int
                val interfacesOff = buf.int
                val sourceFileIdx = buf.int
                val annotationsOff = buf.int
                val classDataOff = buf.int
                val staticValuesOff = buf.int

                val classDesc = getTypeDescriptor(classIdx)
                allClasses.add(formatClassName(classDesc))

                if (classDataOff > 0 && classDataOff < dexBytes.size) {
                    buf.position(classDataOff)
                    val staticFieldsSize = readUleb128(buf)
                    val instanceFieldsSize = readUleb128(buf)
                    val directMethodsSize = readUleb128(buf)
                    val virtualMethodsSize = readUleb128(buf)

                    // Skip fields
                    for (f in 0 until staticFieldsSize) {
                        readUleb128(buf) // field_idx_diff
                        readUleb128(buf) // access_flags
                    }
                    for (f in 0 until instanceFieldsSize) {
                        readUleb128(buf)
                        readUleb128(buf)
                    }

                    // Direct methods
                    var methodIdx = 0
                    for (m in 0 until directMethodsSize) {
                        val diff = readUleb128(buf)
                        methodIdx += diff
                        val mAccess = readUleb128(buf)
                        val codeOff = readUleb128(buf)
                        inspectMethod(
                            buf, dexBytes, methodIdx, mAccess, codeOff,
                            methodIds, stringPool, dexFileName, parsedMethods, stringXrefs
                        )
                    }

                    // Virtual methods
                    methodIdx = 0
                    for (m in 0 until virtualMethodsSize) {
                        val diff = readUleb128(buf)
                        methodIdx += diff
                        val mAccess = readUleb128(buf)
                        val codeOff = readUleb128(buf)
                        inspectMethod(
                            buf, dexBytes, methodIdx, mAccess, codeOff,
                            methodIds, stringPool, dexFileName, parsedMethods, stringXrefs
                        )
                    }
                }
            }
        }

        // Categorize Strings & Map XREFs
        val stringFrequency = mutableMapOf<String, Int>()
        for (m in parsedMethods) {
            for (s in m.stringReferences) {
                stringFrequency[s] = (stringFrequency[s] ?: 0) + 1
            }
        }

        val categorizedStrings = mutableListOf<StringEntry>()
        var strCounter = 0
        for ((idx, str) in stringPool.withIndex()) {
            if (str.isBlank() || str.length < 2) continue
            val cat = categorizeString(str)
            val freq = stringFrequency[str] ?: 1
            val xrefs = stringXrefs[str] ?: emptyList()
            categorizedStrings.add(
                StringEntry(
                    id = strCounter++,
                    value = str,
                    category = cat,
                    occurrences = freq,
                    dexFile = dexFileName,
                    xrefCount = xrefs.size,
                    xrefLocations = xrefs.map { "${it.sourceClass}->${it.sourceMethod}" }.distinct().take(15)
                )
            )
            if (categorizedStrings.size >= 15_000) break
        }

        val summary = DexSummary(
            fileName = dexFileName,
            fileSize = dexBytes.size.toLong(),
            classCount = classDefsSize,
            methodCount = methodIdsSize,
            fieldCount = fieldIdsSize,
            stringCount = stringIdsSize,
            headerVersion = "DEX 035"
        )

        return DexParseResult(
            summary = summary,
            strings = categorizedStrings,
            methods = parsedMethods,
            stringXrefs = stringXrefs,
            allClasses = allClasses
        )
    }

    private fun inspectMethod(
        buf: ByteBuffer,
        bytes: ByteArray,
        methodIdx: Int,
        accessFlags: Int,
        codeOff: Int,
        methodIds: List<*>,
        stringPool: List<String>,
        dexFileName: String,
        outMethods: MutableList<MethodDetail>,
        stringXrefs: MutableMap<String, MutableList<MethodXref>>
    ) {
        val mInfo = if (methodIdx in methodIds.indices) methodIds[methodIdx] as? Any else null
        var classDesc = "LUnknown;"
        var name = "method$methodIdx"
        var retType = "V"
        var params = emptyList<String>()

        if (mInfo != null) {
            val mClass = mInfo::class.java
            try {
                classDesc = mClass.getDeclaredField("classDesc").apply { isAccessible = true }.get(mInfo) as String
                name = mClass.getDeclaredField("name").apply { isAccessible = true }.get(mInfo) as String
                val proto = mClass.getDeclaredField("proto").apply { isAccessible = true }.get(mInfo)
                if (proto != null) {
                    val pClass = proto::class.java
                    retType = pClass.getDeclaredField("returnType").apply { isAccessible = true }.get(proto) as String
                    @Suppress("UNCHECKED_CAST")
                    params = pClass.getDeclaredField("params").apply { isAccessible = true }.get(proto) as List<String>
                }
            } catch (_: Exception) {}
        }

        val formattedClass = formatClassName(classDesc)
        val stringRefs = mutableListOf<String>()
        val calledMethods = mutableListOf<String>()
        var insnsShorts: ShortArray? = null
        var regCount = 2

        if (codeOff > 0 && codeOff + 16 <= bytes.size) {
            val savedPos = buf.position()
            buf.position(codeOff)
            regCount = buf.short.toInt() and 0xFFFF
            val insSize = buf.short.toInt() and 0xFFFF
            val outsSize = buf.short.toInt() and 0xFFFF
            val triesSize = buf.short.toInt() and 0xFFFF
            val debugOff = buf.int
            val insnsSize = buf.int

            if (insnsSize in 1..250_000 && buf.position() + insnsSize * 2 <= bytes.size) {
                insnsShorts = ShortArray(insnsSize)
                for (i in 0 until insnsSize) {
                    insnsShorts[i] = buf.short
                }

                // Disassemble instructions to find strings and method calls
                var pc = 0
                while (pc < insnsSize) {
                    val insn = insnsShorts[pc].toInt() and 0xFFFF
                    val opcode = insn and 0xFF

                    when (opcode) {
                        0x1a -> { // const-string vAA, string@BBBB
                            if (pc + 1 < insnsSize) {
                                val strIdx = insnsShorts[pc + 1].toInt() and 0xFFFF
                                if (strIdx in stringPool.indices) {
                                    val str = stringPool[strIdx]
                                    stringRefs.add(str)
                                    val list = stringXrefs.getOrPut(str) { mutableListOf() }
                                    list.add(MethodXref(formattedClass, name, pc, "const-string"))
                                }
                            }
                            pc += 2
                        }
                        0x1b -> { // const-string/jumbo vAA, string@BBBBBBBB
                            if (pc + 2 < insnsSize) {
                                val low = insnsShorts[pc + 1].toInt() and 0xFFFF
                                val high = insnsShorts[pc + 2].toInt() and 0xFFFF
                                val strIdx = (high shl 16) or low
                                if (strIdx in stringPool.indices) {
                                    val str = stringPool[strIdx]
                                    stringRefs.add(str)
                                    val list = stringXrefs.getOrPut(str) { mutableListOf() }
                                    list.add(MethodXref(formattedClass, name, pc, "const-string/jumbo"))
                                }
                            }
                            pc += 3
                        }
                        0x6e, 0x6f, 0x70, 0x71, 0x72, 0x74, 0x75, 0x76, 0x77, 0x78 -> { // invoke-kind
                            if (pc + 1 < insnsSize) {
                                val targetMethodIdx = insnsShorts[pc + 1].toInt() and 0xFFFF
                                if (targetMethodIdx in methodIds.indices) {
                                    val called = methodIds[targetMethodIdx]
                                    try {
                                        val cClass = called?.javaClass?.getDeclaredField("classDesc")?.apply { isAccessible = true }?.get(called) as? String ?: ""
                                        val cName = called?.javaClass?.getDeclaredField("name")?.apply { isAccessible = true }?.get(called) as? String ?: ""
                                        calledMethods.add("${formatClassName(cClass)}->$cName()")
                                    } catch (_: Exception) {}
                                }
                            }
                            pc += 3
                        }
                        else -> {
                            pc += 1
                        }
                    }
                }
            }
            buf.position(savedPos)
        }

        val smaliCode = SmaliDisassembler.disassemble(
            classDesc = classDesc,
            methodName = name,
            returnType = retType,
            paramTypes = params,
            accessFlags = accessFlags,
            insns = insnsShorts,
            registers = regCount,
            stringPool = stringPool
        )

        val methodDetail = MethodDetail(
            className = formattedClass,
            methodName = name,
            signature = "$name(${params.joinToString(", ") { formatDescriptor(it) }}): ${formatDescriptor(retType)}",
            accessFlags = formatAccessFlags(accessFlags),
            returnType = formatDescriptor(retType),
            parameterTypes = params.map { formatDescriptor(it) },
            dexName = dexFileName,
            methodIndex = methodIdx,
            smaliCode = smaliCode,
            stringReferences = stringRefs.distinct(),
            calledMethods = calledMethods.distinct(),
            callers = emptyList()
        )

        outMethods.add(methodDetail)
    }

    private fun readMutf8(buf: ByteBuffer, bytes: ByteArray): String {
        val utf16Size = readUleb128(buf)
        if (utf16Size <= 0) return ""

        val start = buf.position()
        var len = 0
        while (start + len < bytes.size && bytes[start + len] != 0.toByte()) {
            len++
        }

        val strBytes = ByteArray(minOf(len, 4096))
        val copyLen = minOf(len, 4096)
        System.arraycopy(bytes, start, strBytes, 0, copyLen)
        buf.position(minOf(start + len + 1, bytes.size))

        return try {
            String(strBytes, 0, copyLen, Charsets.UTF_8)
        } catch (_: Exception) {
            String(strBytes, 0, copyLen)
        }
    }

    private fun readUleb128(buf: ByteBuffer): Int {
        var result = 0
        var shift = 0
        while (buf.hasRemaining()) {
            val b = buf.get().toInt() and 0xFF
            result = result or ((b and 0x7F) shl shift)
            if ((b and 0x80) == 0) break
            shift += 7
            if (shift >= 35) break
        }
        return result
    }

    private fun formatClassName(desc: String): String {
        return if (desc.startsWith("L") && desc.endsWith(";")) {
            desc.substring(1, desc.length - 1).replace('/', '.')
        } else {
            desc
        }
    }

    private fun formatDescriptor(desc: String): String {
        return when (desc) {
            "V" -> "void"
            "Z" -> "boolean"
            "B" -> "byte"
            "S" -> "short"
            "C" -> "char"
            "I" -> "int"
            "J" -> "long"
            "F" -> "float"
            "D" -> "double"
            else -> formatClassName(desc)
        }
    }

    private fun formatAccessFlags(flags: Int): String {
        val list = mutableListOf<String>()
        if ((flags and 0x0001) != 0) list.add("public")
        if ((flags and 0x0002) != 0) list.add("private")
        if ((flags and 0x0004) != 0) list.add("protected")
        if ((flags and 0x0008) != 0) list.add("static")
        if ((flags and 0x0010) != 0) list.add("final")
        if ((flags and 0x0020) != 0) list.add("synchronized")
        if ((flags and 0x0100) != 0) list.add("native")
        if ((flags and 0x0400) != 0) list.add("abstract")
        return if (list.isEmpty()) "public" else list.joinToString(" ")
    }

    private fun categorizeString(str: String): StringCategory {
        val lower = str.lowercase()
        return when {
            lower.startsWith("http://") || lower.startsWith("https://") -> StringCategory.URL
            lower.contains("api.") || lower.contains("/v1/") || lower.contains("/v2/") || lower.contains("/api/") -> StringCategory.API
            lower.contains("@") && lower.contains(".") && !lower.contains(" ") -> StringCategory.EMAIL
            lower.endsWith(".com") || lower.endsWith(".org") || lower.endsWith(".net") || lower.endsWith(".io") -> StringCategory.DOMAIN
            lower.contains("purchase") || lower.contains("billing") || lower.contains("inapp") || lower.contains("order_id") || lower.contains("product_id") -> StringCategory.BILLING
            lower.contains("subscribe") || lower.contains("subscription") || lower.contains("premium") || lower.contains("vip") || lower.contains("pro_status") -> StringCategory.SUBSCRIPTION
            lower.contains("password") || lower.contains("secret") || lower.contains("token") || lower.contains("jwt") || lower.contains("bearer") || lower.contains("auth") -> StringCategory.SECURITY
            lower.startsWith("android.") || lower.startsWith("java.") || lower.startsWith("androidx.") -> StringCategory.SYSTEM
            else -> StringCategory.GENERAL
        }
    }
}
