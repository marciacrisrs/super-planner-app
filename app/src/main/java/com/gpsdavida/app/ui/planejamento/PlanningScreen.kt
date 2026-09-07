package com.gpsdavida.app.ui.planejamento

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsdavida.app.domain.model.InboxStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(viewModel: PlanningViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var addDialog by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Planejamento") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { addDialog = when (tab) { 0 -> "goal"; 1 -> "project"; else -> "inbox" } }) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar")
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("Metas") })
                Tab(tab == 1, { tab = 1 }, text = { Text("Projetos") })
                Tab(tab == 2, { tab = 2 }, text = { Text("Inbox") })
            }
            LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (tab) {
                    0 -> items(state.goals) { goal ->
                        Card(Modifier.fillMaxWidth()) { Text(goal.title, Modifier.padding(16.dp)) }
                    }
                    1 -> items(state.projects) { project ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(project.title)
                                Text("${project.steps.size} etapas")
                                if (project.someday) Text("Someday")
                                project.waitingFor?.let { Text("Aguardando: $it") }
                            }
                        }
                    }
                    else -> items(state.inbox.filter { it.status != InboxStatus.DISCARDED }) { item ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.text)
                                Text(item.status.name.lowercase().replace('_', ' '))
                                TextButton(onClick = { viewModel.setInboxStatus(item, InboxStatus.SOMEDAY) }) { Text("Someday") }
                                TextButton(onClick = { viewModel.setInboxStatus(item, InboxStatus.WAITING) }) { Text("Aguardando") }
                                TextButton(onClick = { viewModel.setInboxStatus(item, InboxStatus.PROCESSED) }) { Text("Processado") }
                                IconButton(onClick = { viewModel.deleteInbox(item.id.value) }) { Text("×") }
                            }
                        }
                    }
                }
            }
        }
    }

    addDialog?.let { type ->
        var text by remember(type) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addDialog = null },
            title = { Text(when (type) { "goal" -> "Nova meta"; "project" -> "Novo projeto"; else -> "Capturar ideia" }) },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), singleLine = true) },
            confirmButton = {
                Button(onClick = {
                    when (type) {
                        "goal" -> viewModel.createGoal(text)
                        "project" -> viewModel.createProject(text, null)
                        else -> viewModel.capture(text)
                    }
                    addDialog = null
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { addDialog = null }) { Text("Cancelar") } },
        )
    }
}
