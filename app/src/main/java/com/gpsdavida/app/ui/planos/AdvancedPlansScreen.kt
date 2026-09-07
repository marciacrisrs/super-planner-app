package com.gpsdavida.app.ui.planos

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
import java.time.LocalTime

@Composable
fun AdvancedPlansScreen(viewModel: AdvancedPlansViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showItem by remember { mutableStateOf(false) }
    val openDocument = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        state.selectedId?.let { id -> state.plans.firstOrNull { it.id == id }?.let { viewModel.attachDocument(it, uri) } }
    }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Planos")
        state.plans.forEach { plan ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(plan.name)
                    Text("${plan.objective} · v${plan.version} · ${plan.status.name.lowercase()}")
                    plan.sourceDocument?.let { Text("Documento: $it") }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton({ viewModel.select(plan.id) }) { Text("Selecionar") }
                        TextButton({ openDocument.launch(arrayOf("application/pdf", "image/*", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain")) }) { Text("Importar") }
                        TextButton({ showItem = true; viewModel.select(plan.id) }) { Text("Adicionar item") }
                        TextButton({ viewModel.generate(plan) }) { Text("Gerar programação") }
                        TextButton({ viewModel.newVersion(plan) }) { Text("Nova versão") }
                    }
                }
            }
        }
    }

    if (showItem) {
        var title by remember { mutableStateOf("") }
        var minutes by remember { mutableStateOf("30") }
        AlertDialog(
            onDismissRequest = { showItem = false },
            title = { Text("Proposta de atividade") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Revise os dados antes de confirmar a programação.")
                OutlinedTextField(title, { title = it }, label = { Text("Atividade") })
                OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Duração") })
            } },
            confirmButton = {
                Button(onClick = {
                    state.selectedId?.let { viewModel.addItem(it, title, minutes.toIntOrNull() ?: 30, null, emptySet()) }
                    showItem = false
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton({ showItem = false }) { Text("Cancelar") } },
        )
    }
}
