package com.example.ui.scanner

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AnalysisResult
import com.example.data.model.AnalysisType
import com.example.data.repository.AnalysisRepository
import com.example.domain.engine.DemoSample
import com.example.domain.ocr.ScreenshotOcrProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScannerUiState(
    val selectedType: AnalysisType = AnalysisType.TEXT,
    val inputText: String = "",
    val selectedImageUri: Uri? = null,
    val isAnalyzing: Boolean = false,
    val isOcrProcessing: Boolean = false,
    val currentResult: AnalysisResult? = null,
    val isSavedToHistory: Boolean = false,
    val preferOnlineBackend: Boolean = false,
    val isBackendConnected: Boolean = false,
    val errorMessage: String? = null,
    val successToast: String? = null
)

class ScannerViewModel(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        checkBackendHealth()
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            val connected = repository.testBackendConnection()
            _uiState.update { it.copy(isBackendConnected = connected) }
        }
    }

    fun selectType(type: AnalysisType) {
        _uiState.update {
            it.copy(
                selectedType = type,
                errorMessage = null
            )
        }
    }

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    fun clearInput() {
        _uiState.update {
            it.copy(
                inputText = "",
                selectedImageUri = null,
                currentResult = null,
                isSavedToHistory = false,
                errorMessage = null
            )
        }
    }

    fun toggleBackendMode(preferOnline: Boolean) {
        _uiState.update { it.copy(preferOnlineBackend = preferOnline) }
        if (preferOnline) {
            checkBackendHealth()
        }
    }

    fun loadDemoSample(sample: DemoSample) {
        _uiState.update {
            it.copy(
                selectedType = sample.type,
                inputText = sample.content,
                currentResult = null,
                isSavedToHistory = false,
                errorMessage = null
            )
        }
        analyze()
    }

    fun onScreenshotSelected(context: Context, uri: Uri) {
        _uiState.update {
            it.copy(
                selectedType = AnalysisType.SCREENSHOT,
                selectedImageUri = uri,
                isOcrProcessing = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            val result = ScreenshotOcrProcessor.extractTextFromUri(context, uri)
            result.onSuccess { extractedText ->
                if (extractedText.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isOcrProcessing = false,
                            errorMessage = "No readable text detected in screenshot. Try a clearer image or paste text manually."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            inputText = extractedText,
                            isOcrProcessing = false
                        )
                    }
                    analyze()
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isOcrProcessing = false,
                        errorMessage = "Could not process image: ${err.localizedMessage ?: "OCR failure"}"
                    )
                }
            }
        }
    }

    fun analyze() {
        val state = _uiState.value
        val textToAnalyze = state.inputText.trim()

        if (textToAnalyze.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter text or a URL to analyze.") }
            return
        }

        _uiState.update { it.copy(isAnalyzing = true, errorMessage = null, isSavedToHistory = false) }

        viewModelScope.launch {
            try {
                val result = when (state.selectedType) {
                    AnalysisType.URL -> {
                        repository.analyzeUrl(
                            url = textToAnalyze,
                            preferRemote = state.preferOnlineBackend
                        )
                    }
                    AnalysisType.TEXT, AnalysisType.SCREENSHOT -> {
                        repository.analyzeText(
                            text = textToAnalyze,
                            type = state.selectedType,
                            preferRemote = state.preferOnlineBackend
                        )
                    }
                }

                // Automatically save each analysis result to history database!
                val savedId = repository.saveAnalysis(result)
                val finalResultWithId = result.copy(id = savedId)

                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        currentResult = finalResultWithId,
                        isSavedToHistory = true,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        errorMessage = "Analysis error: ${e.localizedMessage ?: "Unknown failure"}"
                    )
                }
            }
        }
    }

    fun dismissToast() {
        _uiState.update { it.copy(successToast = null) }
    }

    class Factory(private val repository: AnalysisRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScannerViewModel(repository) as T
        }
    }
}
