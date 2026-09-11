package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_history")
data class AnalysisHistoryEntity(
    @PrimaryKey val id: String,
    val fileName: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val fileSize: Long,
    val sha256Hash: String,
    val securityScore: Int,
    val riskLevel: String,
    val criticalCount: Int,
    val highCount: Int,
    val mediumCount: Int,
    val lowCount: Int,
    val dexCount: Int,
    val classCount: Int,
    val methodCount: Int,
    val stringCount: Int,
    val purchaseTargetCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
