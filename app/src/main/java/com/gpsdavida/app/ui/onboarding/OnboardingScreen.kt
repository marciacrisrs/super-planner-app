package com.superplanner.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.ui.theme.SuperPlannerBackground
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerPrimaryButton
import com.superplanner.app.ui.theme.SuperPlannerSpacing

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var intent by remember { mutableStateOf("") }
    var wakeTime by remember { mutableStateOf("07:00") }
    var sleepTime by remember { mutableStateOf("23:00") }
    var fixedTitle by remember { mutableStateOf("") }
    var fixedStart by remember { mutableStateOf("09:00") }
    var fixedEnd by remember { mutableStateOf("18:00") }
    var availabilityStart by remember { mutableStateOf("07:00") }
    var availabilityEnd by remember { mutableStateOf("21:00") }
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    SuperPlannerBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SuperPlannerSpacing.Page, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Vamos começar pelo que importa", style = MaterialTheme.typography.headlineLarge, color = SuperPlannerColors.Ink)
            Text("Em poucos passos, você dá ao planner contexto suficiente para sugerir o que pode fazer agora. Depois, você ajusta o restante.", color = SuperPlannerColors.InkSoft)
            Text("${step + 1} de 3", style = MaterialTheme.typography.labelLarge, color = SuperPlannerColors.TerracottaDark)

            when (step) {
                0 -> StepContent(
                    title = "O que você quer colocar em movimento?",
                    explanation = "Conte uma coisa que é importante para você neste momento. Isso orienta as primeiras sugestões.",
                ) {
                    OutlinedTextField(
                        value = intent,
                        onValueChange = { intent = it },
                        label = { Text("Quero avançar em") },
                        placeholder = { Text("Ex.: organizar meu trabalho") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }

                1 -> StepContent(
                    title = "Como é o seu dia de verdade?",
                    explanation = "Use horários aproximados. Eles ajudam a evitar sugestões que não cabem na sua rotina.",
                ) {
                    TimePair("Acordo", wakeTime) { wakeTime = it }
                    TimePair("Durmo", sleepTime) { sleepTime = it }
                    TimePair("Tenho tempo a partir de", availabilityStart) { availabilityStart = it }
                    TimePair("Até", availabilityEnd) { availabilityEnd = it }
                }

                2 -> StepContent(
                    title = "Tem alguma âncora importante hoje?",
                    explanation = "Se quiser, adicione um compromisso que não pode ser deslocado. Você também pode deixar para depois.",
                ) {
                    OutlinedTextField(
                        value = fixedTitle,
                        onValueChange = { fixedTitle = it },
                        label = { Text("Compromisso fixo") },
                        placeholder = { Text("Ex.: trabalho") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    if (fixedTitle.isNotBlank()) {
                        TimePair("Começa", fixedStart) { fixedStart = it }
                        TimePair("Termina", fixedEnd) { fixedEnd = it }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    onClick = if (step < 2) ({ step += 1 }) else viewModel::skip,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (step < 2) "Pular" else "Começar sem isso")
                }
                SuperPlannerPrimaryButton(
                    text = if (step < 2) "Continuar" else "Criar meu primeiro dia",
                    onClick = {
                        if (step < 2) {
                            step += 1
                        } else {
                            viewModel.createFirstRoute(
                                intent = intent,
                                wakeTime = wakeTime,
                                sleepTime = sleepTime,
                                fixedTitle = fixedTitle,
                                fixedStart = fixedStart,
                                fixedEnd = fixedEnd,
                                availabilityStart = availabilityStart,
                                availabilityEnd = availabilityEnd,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StepContent(
    title: String,
    explanation: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SuperPlannerColors.Surface, MaterialTheme.shapes.extraLarge)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = SuperPlannerColors.Ink)
        Text(explanation, style = MaterialTheme.typography.bodyMedium, color = SuperPlannerColors.InkSoft)
        content()
    }
}

@Composable
private fun TimePair(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("HH:mm") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}
