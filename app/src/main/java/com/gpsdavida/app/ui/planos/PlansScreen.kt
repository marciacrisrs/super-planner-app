@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.superplanner.app.ui.planos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.domain.model.Plan
import com.superplanner.app.domain.model.PlanStatus

@Composable
fun PlansScreen(viewModel: PlansViewModel = hiltViewModel()) {
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Planos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { dialog = true }) { Icon(Icons.Filled.Add, "Novo plano") }
        },
    ) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(plans) { plan -> PlanCard(plan, viewModel) }
        }
    }
    if (dialog) {
        var name by remember { mutableStateOf("") }
        var objective by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { dialog = false },
            title = { Text("Novo plano") },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nome") })
                OutlinedTextField(objective, { objective = it }, label = { Text("Objetivo") })
            } },
            confirmButton = { Button(onClick = { viewModel.create(name, objective); dialog = false }) { Text("Salvar") } },
            dismissButton = { TextButton({ dialog = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun PlanCard(plan: Plan, viewModel: PlansViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(plan.name)
            Text(plan.objective)
            Text("${plan.type.name.lowercase()} · v${plan.version} · ${plan.status.name.lowercase()}")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (plan.status == PlanStatus.ACTIVE) TextButton({ viewModel.setStatus(plan, PlanStatus.PAUSED) }) { Text("Pausar") }
                if (plan.status == PlanStatus.PAUSED) TextButton({ viewModel.setStatus(plan, PlanStatus.ACTIVE) }) { Text("Reativar") }
                if (plan.status != PlanStatus.ENDED) TextButton({ viewModel.setStatus(plan, PlanStatus.ENDED) }) { Text("Encerrar") }
                TextButton({ viewModel.delete(plan.id) }) { Text("Excluir") }
            }
        }
    }
}
