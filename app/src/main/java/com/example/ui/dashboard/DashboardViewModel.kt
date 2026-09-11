package com.example.ui.dashboard

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.analysis.AnalysisProgress
import com.example.analysis.AnalysisState
import com.example.data.local.AnalysisHistoryEntity
import com.example.data.local.AppDatabase
import com.example.data.model.AnalysisResult
import com.example.data.repository.AnalysisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AnalysisRepository(application, db.analysisHistoryDao())

    val currentAnalysis: StateFlow<AnalysisResult?> = repository.currentAnalysis

    val historyList: StateFlow<List<AnalysisHistoryEntity>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()

    private val _activeTargetFileName = MutableStateFlow<String?>(null)
    val activeTargetFileName: StateFlow<String?> = _activeTargetFileName.asStateFlow()

    private val _isParsingModalVisible = MutableStateFlow(false)
    val isParsingModalVisible: StateFlow<Boolean> = _isParsingModalVisible.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isArabic = MutableStateFlow(true) // Arabic RTL default as requested
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    fun setLanguage(arabic: Boolean) {
        _isArabic.value = arabic
    }

    fun dismissParsingModal() {
        _isParsingModalVisible.value = false
    }

    fun analyzeSelectedApk(uri: Uri, fileName: String) {
        _activeTargetFileName.value = fileName
        _isParsingModalVisible.value = true
        viewModelScope.launch {
            repository.analyzeApk(uri, fileName).collect { state ->
                _analysisState.value = state
                if (state is AnalysisState.Success) {
                    repository.saveAnalysisResult(state.result)
                    _isParsingModalVisible.value = false
                } else if (state is AnalysisState.Error) {
                    _isParsingModalVisible.value = false
                }
            }
        }
    }

    fun analyzeSampleApk() {
        val fileName = "sample_target_security.apk"
        _activeTargetFileName.value = fileName
        _isParsingModalVisible.value = true
        viewModelScope.launch {
            val context = getApplication<Application>()
            // Use the application's installed APK file from sourceDir, which is a genuine, complete APK!
            val appApkPath = context.applicationInfo.sourceDir
            val apkFile = File(appApkPath)
            val uri = Uri.fromFile(apkFile)

            repository.analyzeApk(uri, fileName).collect { state ->
                _analysisState.value = state
                if (state is AnalysisState.Success) {
                    repository.saveAnalysisResult(state.result)
                    _isParsingModalVisible.value = false
                } else if (state is AnalysisState.Error) {
                    _isParsingModalVisible.value = false
                }
            }
        }
    }

    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun selectHistoryItem(entity: AnalysisHistoryEntity) {
        // If re-clicking an item in history, re-run or inspect
        analyzeSampleApk()
    }

    fun resetAnalysisState() {
        _analysisState.value = AnalysisState.Idle
    }

    fun getExportJson(): String {
        val current = repository.currentAnalysis.value ?: return "{}"
        return repository.generateJsonReport(current)
    }

    fun getExportText(): String {
        val current = repository.currentAnalysis.value ?: return "No analysis active."
        return repository.generateTextReport(current)
    }
}
