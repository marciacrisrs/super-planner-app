package com.superplanner.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished) onFinished() }
    Scaffold(topBar = { TopAppBar(title = { Text("Primeira rota") }) }) { padding ->
        Column(
            Modifier.fillMaxWidth().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Em poucos minutos, o Super Planner pode preparar uma primeira rota para hoje.")
            Text("Vamos criar disponibilidade, um compromisso, uma tarefa e um hábito de exemplo.")
            Button(onClick = viewModel::createFirstRoute, modifier = Modifier.fillMaxWidth()) {
                Text("Criar minha primeira rota")
            }
            OutlinedButton(onClick = viewModel::skip, modifier = Modifier.fillMaxWidth()) {
                Text("Pular por enquanto")
            }
        }
    }
}
