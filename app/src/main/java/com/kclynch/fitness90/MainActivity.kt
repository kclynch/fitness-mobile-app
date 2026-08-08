package com.kclynch.fitness90

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kclynch.fitness90.ui.home.ChallengeUiState
import com.kclynch.fitness90.ui.home.ChallengeViewModel
import com.kclynch.fitness90.ui.home.HomeScreen
import com.kclynch.fitness90.ui.setup.SetupScreen
import com.kclynch.fitness90.ui.theme.Fitness90Theme
import com.kclynch.fitness90.ui.weight.WeightScreen
import com.kclynch.fitness90.ui.weight.WeightViewModel

class MainActivity : ComponentActivity() {

    private val challengeViewModel: ChallengeViewModel by viewModels()
    private val weightViewModel: WeightViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Fitness90Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Fitness90App(challengeViewModel, weightViewModel)
                }
            }
        }
    }
}

private enum class AppTab { Challenge, Weight }

@Composable
fun Fitness90App(challengeViewModel: ChallengeViewModel, weightViewModel: WeightViewModel) {
    val state by challengeViewModel.uiState.collectAsState()

    when (val current = state) {
        is ChallengeUiState.Loading -> {
            // Brief moment while the first Room emission arrives; nothing to show yet.
        }
        is ChallengeUiState.NotStarted -> {
            SetupScreen(onStartChallenge = challengeViewModel::startChallenge)
        }
        is ChallengeUiState.Active -> {
            var selectedTab by remember { mutableStateOf(AppTab.Challenge) }

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == AppTab.Challenge,
                            onClick = { selectedTab = AppTab.Challenge },
                            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null) },
                            label = { Text("Challenge") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == AppTab.Weight,
                            onClick = { selectedTab = AppTab.Weight },
                            icon = { Icon(Icons.Filled.ShowChart, contentDescription = null) },
                            label = { Text("Weight") }
                        )
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    when (selectedTab) {
                        AppTab.Challenge -> HomeScreen(state = current, viewModel = challengeViewModel)
                        AppTab.Weight -> WeightScreen(viewModel = weightViewModel)
                    }
                }
            }
        }
    }
}
