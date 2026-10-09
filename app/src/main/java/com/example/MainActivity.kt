package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.PhishLensDatabase
import com.example.data.repository.AnalysisRepository
import com.example.ui.MainScreen
import com.example.ui.history.HistoryViewModel
import com.example.ui.scanner.ScannerViewModel
import com.example.ui.theme.PhishLensTheme

class MainActivity : ComponentActivity() {

    private val database by lazy { PhishLensDatabase.getInstance(this) }
    private val repository by lazy { AnalysisRepository(database.analysisDao()) }

    private val scannerViewModel: ScannerViewModel by viewModels {
        ScannerViewModel.Factory(repository)
    }

    private val historyViewModel: HistoryViewModel by viewModels {
        HistoryViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PhishLensTheme {
                MainScreen(
                    scannerViewModel = scannerViewModel,
                    historyViewModel = historyViewModel,
                    repository = repository
                )
            }
        }
    }
}
