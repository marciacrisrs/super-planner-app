package com.superplanner.app.ui.planos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdvancedPlansScreen(
    plansViewModel: AdvancedPlansViewModel = hiltViewModel(),
    importViewModel: PlanImportViewModel = hiltViewModel(),
) {
    val state by plansViewModel.state.collectAsStateWithLifecycle()
    val importState by importViewModel.state.collectAsStateWithLifecycle()
    var addItem by remember { mutableStateOf(false) }
    var importTargetId by remember { mutableStateOf<String?>(null) }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        state.plans.firstOrNull { it.id == importTargetId }?.let { plansViewModel.attachDocument(it, uri) }
        importViewModel.interpret(uri)
        importTargetId = null
    }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Planos")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.plans) { plan ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(plan.name)
                        Text("${plan.objective} · v${plan.version} · ${plan.status.name.lowercase()}")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton({ plansViewModel.select(plan.id) }) { Text("Selecionar") }
                            TextButton({ importTargetId = plan.id; openDocument.launch(arrayOf("application/pdf", "image/*", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain")) }) { Text("Importar") }
                            TextButton({ plansViewModel.select(plan.id); addItem = true }) { Text("Item") }
                            TextButton({ plansViewModel.generate(plan) }) { Text("Programar") }
                            TextButton({ plansViewModel.newVersion(plan) }) { Text("Nova versão") }
                        }
                        plan.sourceDocument?.let { Text("Fonte: $it") }
                    }
                }
            }
        }
    }

    if (addItem) {
        var title by remember { mutableStateOf("") }
        var minutes by remember { mutableStateOf("30") }
        AlertDialog(
            onDismissRequest = { addItem = false },
            title = { Text("Novo item do plano") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(title, { title = it }, label = { Text("Atividade") })
                OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Minutos") })
            } },
            confirmButton = { Button({ state.selectedId?.let { plansViewModel.addItem(it, title, minutes.toIntOrNull() ?: 30, null, emptySet()) }; addItem = false }) { Text("Salvar") } },
            dismissButton = { TextButton({ addItem = false }) { Text("Cancelar") } },
        )
    }

    importState.proposal?.let { proposal ->
        AlertDialog(
            onDismissRequest = { importViewModel.cancel() },
            title = { Text("Revisar importação") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(proposal.displayName)
                proposal.uncertainReason?.let { Text("Atenção: $it") }
                Text("Itens encontrados: ${proposal.items.size}")
                proposal.items.take(12).forEach { item -> Text("• ${item.title} · ${item.durationMinutes} min${if (item.uncertain) " · verificar" else ""}") }
            } },
            confirmButton = { Button({ state.selectedId?.let(importViewModel::confirm) }) { Text("Confirmar") } },
            dismissButton = { TextButton({ importViewModel.cancel() }) { Text("Cancelar") } },
        )
    }
}
