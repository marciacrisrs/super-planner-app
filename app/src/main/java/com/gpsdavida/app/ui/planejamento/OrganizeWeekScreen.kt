package com.superplanner.app.ui.planejamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.domain.ai.OrganizeWeekResponse
import com.superplanner.app.ui.theme.SuperPlannerColors

@Composable
fun OrganizeWeekScreen(
    onBack: () -> Unit,
    viewModel: OrganizeWeekViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state is OrganizeWeekState.Idle) viewModel.organize()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Organize sua semana", style = MaterialTheme.typography.headlineMedium, color = SuperPlannerColors.Ink)
                Text("Uma proposta para revisar antes de aplicar.", style = MaterialTheme.typography.bodyMedium, color = SuperPlannerColors.InkSoft)
            }
            OutlinedButton(onClick = onBack) { Text("Voltar") }
        }

        when (val current = state) {
            OrganizeWeekState.Idle, OrganizeWeekState.Loading -> {
                CircularProgressIndicator()
                Text("Analisando agenda, prioridades e restrições…", color = SuperPlannerColors.InkSoft)
            }
            is OrganizeWeekState.Error -> {
                Text("Não consegui organizar a semana.", style = MaterialTheme.typography.titleMedium)
                Text(current.message, color = SuperPlannerColors.InkSoft)
                Button(onClick = { viewModel.organize() }) { Text("Tentar novamente") }
            }
            is OrganizeWeekState.Ready -> ProposalContent(current.response, onApply = viewModel::apply)
            is OrganizeWeekState.Applied -> ProposalContent(current.response, onApply = {}, applied = true)
        }
    }
}

@Composable
private fun ProposalContent(
    response: OrganizeWeekResponse,
    onApply: () -> Unit,
    applied: Boolean = false,
) {
    val summary = response.summary
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = SuperPlannerColors.TerracottaSoft), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = SuperPlannerColors.Terracotta)
                        Text("Análise da semana", style = MaterialTheme.typography.titleMedium, color = SuperPlannerColors.TerracottaDark)
                    }
                    Text(
                        "Considerei ${summary.fixedCommitmentsConsidered} fixos, ${summary.desiresConsidered} desejos, " +
                            "${summary.commuteMinutesConsidered} min de deslocamento e ${summary.preparationMinutesConsidered} min de preparação. " +
                            "Encontrei ${summary.conflictsFound} conflitos e ${summary.opportunitiesFound} oportunidades.",
                        color = SuperPlannerColors.Ink,
                    )
                }
            }
        }
        if (response.conflicts.isNotEmpty()) {
            item { Text("Conflitos", style = MaterialTheme.typography.titleMedium) }
            items(response.conflicts) { conflict ->
                Text("• ${conflict.title}: ${conflict.reason}", color = SuperPlannerColors.InkSoft)
            }
        }
        if (response.explanations.isNotEmpty()) {
            item { Text("Decisões explicadas", style = MaterialTheme.typography.titleMedium) }
            items(response.explanations) { explanation ->
                Text("• ${explanation.message}", color = SuperPlannerColors.InkSoft)
            }
        }
        item {
            if (applied) {
                Text("Organização marcada como aplicada. O estado original continua disponível para comparação nesta revisão.", color = SuperPlannerColors.TerracottaDark)
            } else {
                Button(onClick = onApply, modifier = Modifier.fillMaxWidth()) {
                    Text("✨ Aplicar organização")
                }
            }
        }
    }
}
