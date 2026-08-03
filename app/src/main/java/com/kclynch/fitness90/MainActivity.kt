package com.kclynch.fitness90

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.kclynch.fitness90.ui.home.ChallengeUiState
import com.kclynch.fitness90.ui.home.ChallengeViewModel
import com.kclynch.fitness90.ui.home.HomeScreen
import com.kclynch.fitness90.ui.setup.SetupScreen
import com.kclynch.fitness90.ui.theme.Fitness90Theme

class MainActivity : ComponentActivity() {

    private val viewModel: ChallengeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Fitness90Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Fitness90App(viewModel)
                }
            }
        }
    }
}

@Composable
fun Fitness90App(viewModel: ChallengeViewModel) {
    val state by viewModel.uiState.collectAsState()

    when (val current = state) {
        is ChallengeUiState.Loading -> {
            // Brief moment while the first Room emission arrives; nothing to show yet.
        }
        is ChallengeUiState.NotStarted -> {
            SetupScreen(onStartChallenge = viewModel::startChallenge)
        }
        is ChallengeUiState.Active -> {
            HomeScreen(state = current, viewModel = viewModel)
        }
    }
}
