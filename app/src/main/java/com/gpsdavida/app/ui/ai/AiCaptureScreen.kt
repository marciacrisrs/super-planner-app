package com.superplanner.app.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.domain.ai.AiCommand
import com.superplanner.app.ui.theme.SuperPlannerBackground
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerColors

@Composable
fun AiCaptureScreen(
    onBack: () -> Unit,
    viewModel: AiCaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var message by rememberSaveable { mutableStateOf("") }

    SuperPlannerBackground {
        Column(
            modifier = Modifier.fillMaxSize().imePadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("O que você precisa fazer?", color = SuperPlannerColors.Ink)
            Text(
                "Escreva do seu jeito. Eu organizo o pedido antes de qualquer alteração.",
                color = SuperPlannerColors.InkSoft,
            )
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                placeholder = { Text("Ex.: amanhã preciso estudar francês por uma hora depois do trabalho") },
                leadingIcon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) { Text("Voltar") }
                Button(onClick = { viewModel.submit(message) }, enabled = message.isNotBlank() && !state.isLoading) {
                    Text("Organizar")
                }
            }

            if (state.isLoading) CircularProgressIndicator()

            state.error?.let { Text(it, color = SuperPlannerColors.TerracottaDark) }

            state.proposal?.let { proposal ->
                SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Entendi assim", color = SuperPlannerColors.Ink)
                        Text(proposal.explanation, color = SuperPlannerColors.InkSoft)
                        when (val command = proposal.command) {
                            is AiCommand.CreateActivityDraft -> {
                                Text("Atividade: ${command.draft.title}", color = SuperPlannerColors.Ink)
                                command.draft.date?.let { Text("Data: $it", color = SuperPlannerColors.InkSoft) }
                                command.draft.plannedDuration?.let { Text("Duração: ${it.toMinutes()} min", color = SuperPlannerColors.InkSoft) }
                            }
                            is AiCommand.MissingInformation -> Text("Falta: ${command.fields.joinToString()}", color = SuperPlannerColors.Ink)
                            else -> Text("Ação: ${command::class.simpleName}", color = SuperPlannerColors.Ink)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = viewModel::dismissProposal) { Text("Corrigir") }
                            Button(onClick = viewModel::confirm) { Text("Confirmar") }
                        }
                    }
                }
            }

            if (state.execution is com.superplanner.app.domain.ai.AiExecution.Executed) {
                Text("Feito. O Planner recebeu a operação e pode recalcular a rota.", color = SuperPlannerColors.Success)
            }
        }
    }
}
