package com.example.analysis.dex

object SmaliDisassembler {

    fun disassemble(
        classDesc: String,
        methodName: String,
        returnType: String,
        paramTypes: List<String>,
        accessFlags: Int,
        insns: ShortArray?,
        registers: Int,
        stringPool: List<String>
    ): String {
        val sb = StringBuilder()
        val formattedAccess = formatAccess(accessFlags)

        sb.append("# Smali Bytecode Representation\n")
        sb.append("# Disassembled by APK Sentinel Static Engine\n\n")
        sb.append(".method $formattedAccess $methodName(${paramTypes.joinToString("")}):$returnType\n")
        sb.append("    .registers $registers\n\n")

        if (insns == null || insns.isEmpty()) {
            sb.append("    # Native or Abstract method - No bytecode instructions\n")
            sb.append(".end method\n")
            return sb.toString()
        }

        var pc = 0
        var line = 1
        while (pc < insns.size) {
            val insn = insns[pc].toInt() and 0xFFFF
            val opcode = insn and 0xFF
            val hexOffset = String.format("0x%04x", pc * 2)

            when (opcode) {
                0x00 -> {
                    sb.append("    :$hexOffset nop\n")
                    pc += 1
                }
                0x01 -> { // move vA, vB
                    val vA = (insn shr 8) and 0xF
                    val vB = (insn shr 12) and 0xF
                    sb.append("    :$hexOffset move v$vA, v$vB\n")
                    pc += 1
                }
                0x0e -> { // return-void
                    sb.append("    :$hexOffset return-void\n")
                    pc += 1
                }
                0x0f -> { // return vAA
                    val reg = (insn shr 8) and 0xFF
                    sb.append("    :$hexOffset return v$reg\n")
                    pc += 1
                }
                0x12 -> { // const/4 vA, #+B
                    val reg = (insn shr 8) and 0xF
                    val lit = (insn shr 12) and 0xF
                    sb.append("    :$hexOffset const/4 v$reg, 0x$lit\n")
                    pc += 1
                }
                0x13 -> { // const/16 vAA, #+BBBB
                    val reg = (insn shr 8) and 0xFF
                    val lit = if (pc + 1 < insns.size) insns[pc + 1].toInt() else 0
                    sb.append("    :$hexOffset const/16 v$reg, 0x${Integer.toHexString(lit and 0xFFFF)}\n")
                    pc += 2
                }
                0x1a -> { // const-string vAA, string@BBBB
                    val reg = (insn shr 8) and 0xFF
                    val strIdx = if (pc + 1 < insns.size) insns[pc + 1].toInt() and 0xFFFF else 0
                    val strVal = if (strIdx in stringPool.indices) escapeString(stringPool[strIdx]) else "<?>"
                    sb.append("    :$hexOffset const-string v$reg, \"$strVal\"\n")
                    pc += 2
                }
                0x1b -> { // const-string/jumbo vAA, string@BBBBBBBB
                    val reg = (insn shr 8) and 0xFF
                    val low = if (pc + 1 < insns.size) insns[pc + 1].toInt() and 0xFFFF else 0
                    val high = if (pc + 2 < insns.size) insns[pc + 2].toInt() and 0xFFFF else 0
                    val strIdx = (high shl 16) or low
                    val strVal = if (strIdx in stringPool.indices) escapeString(stringPool[strIdx]) else "<?>"
                    sb.append("    :$hexOffset const-string/jumbo v$reg, \"$strVal\"\n")
                    pc += 3
                }
                0x28 -> { // goto +AA
                    val off = (insn shr 8).toByte().toInt()
                    sb.append("    :$hexOffset goto :target_${pc + off}\n")
                    pc += 1
                }
                0x32 -> { // if-eq vA, vB, +CCCC
                    val vA = (insn shr 8) and 0xF
                    val vB = (insn shr 12) and 0xF
                    val branch = if (pc + 1 < insns.size) insns[pc + 1].toInt() else 0
                    sb.append("    :$hexOffset if-eq v$vA, v$vB, :cond_${pc + branch}\n")
                    pc += 2
                }
                0x38 -> { // if-eqz vAA, +BBBB
                    val reg = (insn shr 8) and 0xFF
                    val branch = if (pc + 1 < insns.size) insns[pc + 1].toInt() else 0
                    sb.append("    :$hexOffset if-eqz v$reg, :cond_${pc + branch}\n")
                    pc += 2
                }
                0x39 -> { // if-nez vAA, +BBBB
                    val reg = (insn shr 8) and 0xFF
                    val branch = if (pc + 1 < insns.size) insns[pc + 1].toInt() else 0
                    sb.append("    :$hexOffset if-nez v$reg, :cond_${pc + branch}\n")
                    pc += 2
                }
                0x52 -> { // iget vA, vB, field@CCCC
                    val vA = (insn shr 8) and 0xF
                    val vB = (insn shr 12) and 0xF
                    val fieldIdx = if (pc + 1 < insns.size) insns[pc + 1].toInt() and 0xFFFF else 0
                    sb.append("    :$hexOffset iget v$vA, v$vB, field@$fieldIdx\n")
                    pc += 2
                }
                0x6e, 0x6f, 0x70, 0x71, 0x72 -> { // invoke-kind
                    val kind = when (opcode) {
                        0x6e -> "invoke-virtual"
                        0x6f -> "invoke-super"
                        0x70 -> "invoke-direct"
                        0x71 -> "invoke-static"
                        0x72 -> "invoke-interface"
                        else -> "invoke"
                    }
                    val methodIdx = if (pc + 1 < insns.size) insns[pc + 1].toInt() and 0xFFFF else 0
                    sb.append("    :$hexOffset $kind {v0..v1}, method@$methodIdx\n")
                    pc += 3
                }
                0x0c -> { // move-result-object
                    val reg = (insn shr 8) and 0xFF
                    sb.append("    :$hexOffset move-result-object v$reg\n")
                    pc += 1
                }
                0x0a -> { // move-result
                    val reg = (insn shr 8) and 0xFF
                    sb.append("    :$hexOffset move-result v$reg\n")
                    pc += 1
                }
                0x22 -> { // new-instance
                    val reg = (insn shr 8) and 0xFF
                    val typeIdx = if (pc + 1 < insns.size) insns[pc + 1].toInt() and 0xFFFF else 0
                    sb.append("    :$hexOffset new-instance v$reg, type@$typeIdx\n")
                    pc += 2
                }
                else -> {
                    val name = getOpcodeName(opcode)
                    sb.append("    :$hexOffset $name\n")
                    pc += 1
                }
            }
            line++
            if (line > 150) {
                sb.append("    # ... [truncated for performance, ${insns.size - pc} more instructions]\n")
                break
            }
        }

        sb.append(".end method\n")
        return sb.toString()
    }

    private fun escapeString(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .take(80)
    }

    private fun formatAccess(flags: Int): String {
        val list = mutableListOf<String>()
        if ((flags and 0x0001) != 0) list.add("public")
        if ((flags and 0x0002) != 0) list.add("private")
        if ((flags and 0x0004) != 0) list.add("protected")
        if ((flags and 0x0008) != 0) list.add("static")
        if ((flags and 0x0010) != 0) list.add("final")
        return if (list.isEmpty()) "public" else list.joinToString(" ")
    }

    private fun getOpcodeName(op: Int): String {
        return when (op) {
            0x00 -> "nop"
            0x01 -> "move"
            0x02 -> "move/from16"
            0x03 -> "move/16"
            0x04 -> "move-wide"
            0x07 -> "move-object"
            0x0a -> "move-result"
            0x0b -> "move-result-wide"
            0x0c -> "move-result-object"
            0x0d -> "move-exception"
            0x0e -> "return-void"
            0x0f -> "return"
            0x10 -> "return-wide"
            0x11 -> "return-object"
            0x12 -> "const/4"
            0x13 -> "const/16"
            0x14 -> "const"
            0x15 -> "const/high16"
            0x16 -> "const-wide/16"
            0x17 -> "const-wide/32"
            0x18 -> "const-wide"
            0x19 -> "const-wide/high16"
            0x1a -> "const-string"
            0x1b -> "const-string/jumbo"
            0x1c -> "const-class"
            0x1d -> "monitor-enter"
            0x1e -> "monitor-exit"
            0x1f -> "check-cast"
            0x20 -> "instance-of"
            0x21 -> "array-length"
            0x22 -> "new-instance"
            0x23 -> "new-array"
            0x24 -> "filled-new-array"
            0x27 -> "throw"
            0x28 -> "goto"
            0x29 -> "goto/16"
            0x2a -> "goto/32"
            0x32 -> "if-eq"
            0x33 -> "if-ne"
            0x34 -> "if-lt"
            0x35 -> "if-ge"
            0x36 -> "if-gt"
            0x37 -> "if-le"
            0x38 -> "if-eqz"
            0x39 -> "if-nez"
            0x3a -> "if-ltz"
            0x3b -> "if-gez"
            0x3c -> "if-gtz"
            0x3d -> "if-lez"
            0x52 -> "iget"
            0x53 -> "iget-wide"
            0x54 -> "iget-object"
            0x59 -> "iput"
            0x60 -> "sget"
            0x67 -> "sput"
            0x6e -> "invoke-virtual"
            0x6f -> "invoke-super"
            0x70 -> "invoke-direct"
            0x71 -> "invoke-static"
            0x72 -> "invoke-interface"
            else -> "op_0x" + Integer.toHexString(op)
        }
    }
}
