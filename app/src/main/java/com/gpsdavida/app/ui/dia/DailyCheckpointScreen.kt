package com.superplanner.app.ui.dia

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.domain.model.ReviewSuggestion
import com.superplanner.app.ui.review.DailyReviewViewModel

@Composable
fun DailyCheckpointScreen(viewModel: DailyReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("daily_checkpoint", Context.MODE_PRIVATE) }
    val today = remember { state.date.toString() }
    var energy by remember { mutableStateOf(prefs.getString("$today.energy", "") ?: "") }

    LazyColumn(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Revisão de hoje", style = MaterialTheme.typography.headlineSmall)
            Text("Recupere direção: não é para medir desempenho.", style = MaterialTheme.typography.bodyMedium)
        }
        item { ReviewCard("O que importa hoje?", state.priorityTitle ?: "Nenhuma prioridade identificada ainda.") }
        item { ReviewCard("O que já aconteceu?", "${state.completedCount} de ${state.plannedCount} atividades planejadas foram concluídas.") }
        item {
            ReviewCard(
                "O que mudou?",
                state.changed.ifEmpty { listOf("Nada relevante apareceu na rota.") },
            )
        }
        item {
            ReviewCard(
                "O que precisa ser reorganizado?",
                state.toReorganize.ifEmpty { listOf("Nada precisa ser reorganizado agora.") },
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Como está sua energia?", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = energy,
                        onValueChange = { energy = it },
                        label = { Text("Baixa / média / alta") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    TextButton(onClick = { prefs.edit().putString("$today.energy", energy).apply() }) { Text("Salvar") }
                }
            }
        }
        state.suggestions.forEach { suggestion ->
            item { DailySuggestion(suggestion) }
        }
    }
}

@Composable
private fun ReviewCard(title: String, value: String) {
    ReviewCard(title, listOf(value))
}

@Composable
private fun ReviewCard(title: String, values: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            values.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun DailySuggestion(suggestion: ReviewSuggestion) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(suggestion.title, style = MaterialTheme.typography.titleMedium)
            Text(suggestion.explanation, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = { }) { Text(suggestion.actionLabel) }
        }
    }
}
