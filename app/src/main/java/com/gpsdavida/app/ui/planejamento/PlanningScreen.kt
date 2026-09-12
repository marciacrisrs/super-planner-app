package com.superplanner.app.ui.planejamento

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.superplanner.app.domain.model.InboxStatus
import com.superplanner.app.domain.model.Project
import com.superplanner.app.ui.lazer.LeisureScreenV2
import com.superplanner.app.ui.notas.NotesScreenV2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    onOpenHorizons: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenLifeAreas: () -> Unit,
    onOpenDayCheckpoint: () -> Unit,
    onOpenPlans: () -> Unit,
    viewModel: PlanningViewModel = hiltViewModel(),
) {
    var showBackup by remember { mutableStateOf(false) }
    var showLeisure by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }
    if (showBackup) { com.superplanner.app.ui.backup.BackupScreen(); return }
    if (showLeisure) { LeisureScreenV2(); return }
    if (showNotes) { NotesScreenV2(); return }

    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var addDialog by remember { mutableStateOf<String?>(null) }
    var projectForStep by remember { mutableStateOf<Project?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Planejamento") }, actions = {
                TextButton(onClick = onOpenHorizons) { Text("Horizontes") }
                TextButton(onClick = onOpenReview) { Text("Revisão") }
                TextButton(onClick = onOpenFinance) { Text("Finanças") }
                TextButton(onClick = onOpenLifeAreas) { Text("Áreas") }
                TextButton(onClick = onOpenDayCheckpoint) { Text("Hoje") }
                TextButton(onClick = onOpenPlans) { Text("Planos") }
                TextButton(onClick = { showLeisure = true }) { Text("Lazer") }
                TextButton(onClick = { showNotes = true }) { Text("Notas") }
                TextButton(onClick = { showBackup = true }) { Text("Backup") }
            })
        },
        floatingActionButton = { FloatingActionButton(onClick = { addDialog = when (tab) { 0 -> "goal"; 1 -> "project"; else -> "inbox" } }) { Icon(Icons.Filled.Add, "Adicionar") } },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("Metas") })
                Tab(tab == 1, { tab = 1 }, text = { Text("Projetos") })
                Tab(tab == 2, { tab = 2 }, text = { Text("Inbox") })
            }
            LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (tab) {
                    0 -> items(state.goals) { goal -> Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(goal.title); TextButton({ viewModel.deleteGoal(goal.id.value) }) { Text("Excluir") } } } }
                    1 -> items(state.projects) { project -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(project.title); Text("${project.steps.size} etapas"); project.steps.forEach { Text("• ${it.title} — ${it.plannedDuration.toMinutes()} min") }; Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { TextButton({ projectForStep = project }) { Text("Adicionar etapa") }; TextButton({ viewModel.deleteProject(project.id.value) }) { Text("Excluir") } } } } }
                    else -> items(state.inbox.filter { it.status != InboxStatus.DISCARDED }) { item -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { Text(item.text); Text(item.status.name.lowercase().replace('_', ' ')); Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TextButton({ viewModel.setInboxStatus(item, InboxStatus.SOMEDAY) }) { Text("Someday") }; TextButton({ viewModel.setInboxStatus(item, InboxStatus.WAITING) }) { Text("Aguardando") }; TextButton({ viewModel.setInboxStatus(item, InboxStatus.PROCESSED) }) { Text("Processado") } }; TextButton({ viewModel.deleteInbox(item.id.value) }) { Text("Excluir") } } } }
                }
            }
        }
    }
    addDialog?.let { type ->
        var text by remember(type) { mutableStateOf("") }
        AlertDialog(onDismissRequest = { addDialog = null }, title = { Text(when (type) { "goal" -> "Nova meta"; "project" -> "Novo projeto"; else -> "Capturar ideia" }) }, text = { OutlinedTextField(text, { text = it }, Modifier.fillMaxWidth(), singleLine = true) }, confirmButton = { Button({ when (type) { "goal" -> viewModel.createGoal(text); "project" -> viewModel.createProject(text, null); else -> viewModel.capture(text) }; addDialog = null }) { Text("Salvar") } }, dismissButton = { TextButton({ addDialog = null }) { Text("Cancelar") } })
    }
    projectForStep?.let { project ->
        var title by remember(project.id) { mutableStateOf("") }
        var minutes by remember(project.id) { mutableStateOf("30") }
        AlertDialog(onDismissRequest = { projectForStep = null }, title = { Text("Nova etapa") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(title, { title = it }, label = { Text("Etapa") }); OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Minutos") }) } }, confirmButton = { Button({ viewModel.addProjectStep(project, title, minutes.toLongOrNull() ?: 30); projectForStep = null }) { Text("Salvar") } }, dismissButton = { TextButton({ projectForStep = null }) { Text("Cancelar") } })
    }
}
