package com.superplanner.app.ui.horizontes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizonsScreen(viewModel: HorizonsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMilestone by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Horizontes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showMilestone = true }) { Icon(Icons.Filled.Add, "Novo marco") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Mês", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            }
            items(state.months, key = { it.month.toString() }) { month ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(month.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")).withLocale(Locale("pt", "BR"))).replaceFirstChar { it.uppercase() })
                        Text("${month.activityCount} atividades · ${month.plannedMinutes} min planejados")
                        Text("${month.completionRatio}% concluído")
                    }
                }
            }
            item { Text("Marcos", style = androidx.compose.material3.MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp)) }
            items(state.milestones, key = { it.id }) { milestone ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(milestone.title)
                            Text(milestone.targetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                        }
                        IconButton(onClick = { viewModel.deleteMilestone(milestone.id) }) { Icon(Icons.Filled.Delete, "Excluir") }
                    }
                }
            }
        }
    }

    if (showMilestone) {
        var title by remember { mutableStateOf("") }
        var dateText by remember { mutableStateOf(LocalDate.now().plusMonths(1).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))) }
        AlertDialog(
            onDismissRequest = { showMilestone = false },
            title = { Text("Novo marco") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(title, { title = it }, label = { Text("Marco") })
                    OutlinedTextField(dateText, { dateText = it }, label = { Text("Data (dd/MM/yyyy)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    runCatching { LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()?.let { viewModel.addMilestone(title, it) }
                    showMilestone = false
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton({ showMilestone = false }) { Text("Cancelar") } },
        )
    }
}
