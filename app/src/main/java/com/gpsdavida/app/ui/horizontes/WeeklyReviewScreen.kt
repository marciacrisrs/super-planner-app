package com.superplanner.app.ui.horizontes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.domain.model.ReviewSuggestion
import com.superplanner.app.ui.review.WeeklyReviewViewModelV2

@Composable
fun WeeklyReviewScreen(viewModel: WeeklyReviewViewModelV2 = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val decisions = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Revisão da semana", style = MaterialTheme.typography.headlineSmall)
            Text("Veja o que avançou, o que está ficando para trás e escolha o próximo ajuste.", style = MaterialTheme.typography.bodyMedium)
        }
        item { ReviewSection("Prioridades que avançaram", state.advancedPriorities) }
        item { ReviewSection("Ficou repetidamente para trás", state.repeatedlyDeferred) }
        item { ReviewSection("O que mudar na próxima semana", state.nextWeekChanges) }
        item { ReviewSection("O que merece atenção", state.neglectedAreas) }
        state.suggestions.forEach { suggestion ->
            item { SuggestionCard(suggestion, decisions[suggestion.id]) { accepted -> decisions[suggestion.id] = accepted } }
        }
    }
}

@Composable
private fun ReviewSection(title: String, items: List<String>) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (items.isEmpty()) Text("Nada relevante para reorganizar agora.", style = MaterialTheme.typography.bodyMedium)
            items.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: ReviewSuggestion, decision: Boolean?, onDecision: (Boolean) -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(suggestion.title, style = MaterialTheme.typography.titleMedium)
            Text(suggestion.explanation, style = MaterialTheme.typography.bodyMedium)
            if (decision == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { onDecision(true) }) { Text(suggestion.actionLabel) }
                    TextButton(onClick = { onDecision(false) }) { Text("Não agora") }
                }
            } else {
                Text(if (decision) "Sugestão aceita." else "Sugestão descartada.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
