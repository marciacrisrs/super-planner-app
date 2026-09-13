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
    var recurringActivity by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }

    LaunchedEffect(state.finished) { if (state.finished) onFinished() }

    SuperPlannerBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SuperPlannerSpacing.Page, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Sua primeira rota", style = MaterialTheme.typography.headlineLarge, color = SuperPlannerColors.Ink)
            Text("Vamos configurar só o necessário. O restante pode entrar depois.", color = SuperPlannerColors.InkSoft)
            Text("${step + 1} de 6", style = MaterialTheme.typography.labelLarge, color = SuperPlannerColors.TerracottaDark)

            when (step) {
                0 -> StepContent("O que você quer conseguir organizar primeiro?", "Isso dá uma direção para o planner priorizar o que realmente importa.") {
                    OutlinedTextField(intent, { intent = it }, label = { Text("Minha prioridade agora") }, placeholder = { Text("Ex.: organizar meu trabalho") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                1 -> StepContent("Quando seu dia começa e termina?", "Esses horários ajudam a não planejar tarefas fora da sua vida real.") {
                    TimePair("Acordo", wakeTime) { wakeTime = it }
                    TimePair("Durmo", sleepTime) { sleepTime = it }
                }
                2 -> StepContent("Você tem algum compromisso fixo importante?", "Compromissos fixos viram âncoras para o restante do dia. Você pode pular.") {
                    OutlinedTextField(fixedTitle, { fixedTitle = it }, label = { Text("Compromisso") }, placeholder = { Text("Ex.: trabalho") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    TimePair("Começa", fixedStart) { fixedStart = it }
                    TimePair("Termina", fixedEnd) { fixedEnd = it }
                }
                3 -> StepContent("Quando você normalmente está disponível?", "O planner usa essa janela para decidir o que realmente cabe.") {
                    TimePair("Disponível a partir de", availabilityStart) { availabilityStart = it }
                    TimePair("Disponível até", availabilityEnd) { availabilityEnd = it }
                }
                4 -> StepContent("Existe algo que você faz com frequência?", "Uma atividade recorrente evita que você precise cadastrar tudo manualmente depois. Você pode pular.") {
                    OutlinedTextField(recurringActivity, { recurringActivity = it }, label = { Text("Atividade recorrente") }, placeholder = { Text("Ex.: caminhar") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                5 -> StepContent("Tudo pronto para o primeiro dia", "Vamos usar essas informações para montar uma rota inicial. Você poderá ajustar tudo depois.") {
                    SummaryRow("Prioridade", intent.ifBlank { "Não definida" })
                    SummaryRow("Acorda / dorme", "$wakeTime / $sleepTime")
                    SummaryRow("Compromisso", fixedTitle.ifBlank { "Nenhum" })
                    SummaryRow("Disponibilidade", "$availabilityStart–$availabilityEnd")
                    SummaryRow("Recorrente", recurringActivity.ifBlank { "Nenhuma" })
                }
            }

            if (step < 5) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = { step += 1 }, modifier = Modifier.weight(1f)) { Text("Pular") }
                    SuperPlannerPrimaryButton(text = "Continuar", onClick = { step += 1 }, modifier = Modifier.weight(1f))
                }
            } else {
                SuperPlannerPrimaryButton(
                    text = "Criar minha primeira rota",
                    onClick = { viewModel.createFirstRoute(intent, wakeTime, sleepTime, fixedTitle, fixedStart, fixedEnd, availabilityStart, availabilityEnd, recurringActivity) },
                    modifier = Modifier.fillMaxWidth(),
                )
                TextButton(onClick = viewModel::skip, modifier = Modifier.fillMaxWidth()) { Text("Pular por enquanto") }
            }
        }
    }
}

@Composable
private fun StepContent(title: String, explanation: String, content: @Composable () -> Unit) {
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
    OutlinedTextField(value, onValueChange, label = { Text(label) }, placeholder = { Text("HH:mm") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = SuperPlannerColors.InkSoft)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = SuperPlannerColors.Ink)
    }
}
