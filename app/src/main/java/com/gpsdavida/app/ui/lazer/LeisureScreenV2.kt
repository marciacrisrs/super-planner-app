@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.superplanner.app.ui.lazer

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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
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
import com.superplanner.app.domain.model.LeisureKind
import com.superplanner.app.domain.model.LeisureStatus

@Composable
fun LeisureScreenV2(viewModel: LeisureViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var add by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Lazer e leitura") }, actions = { TextButton({ reading = true }) { Text("Sessões") } }) },
        floatingActionButton = { FloatingActionButton({ add = true }) { Icon(Icons.Filled.Add, "Adicionar") } },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("Séries") })
                Tab(tab == 1, { tab = 1 }, text = { Text("Livros") })
            }
            LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items.filter { (tab == 0 && it.kind == LeisureKind.SERIES) || (tab == 1 && it.kind == LeisureKind.BOOK) }) { item ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.title)
                            Text(item.status.name.lowercase())
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton({ viewModel.setStatus(item, LeisureStatus.WANT) }) { Text("Quero") }
                                TextButton({ viewModel.setStatus(item, LeisureStatus.NEXT) }) { Text("Próximo") }
                                TextButton({ viewModel.setStatus(item, LeisureStatus.ACTIVE) }) { Text("Ativo") }
                                TextButton({ viewModel.setStatus(item, LeisureStatus.PAUSED) }) { Text("Pausado") }
                                TextButton({ viewModel.setStatus(item, LeisureStatus.COMPLETED) }) { Text("Concluído") }
                            }
                            TextButton({ viewModel.schedule(item) }) { Text("Programar") }
                            TextButton({ viewModel.delete(item.id) }) { Text("Excluir") }
                        }
                    }
                }
            }
        }
    }
    if (add) {
        var title by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { add = false }, title = { Text(if (tab == 0) "Nova série" else "Novo livro") }, text = { OutlinedTextField(title, { title = it }, label = { Text("Título") }) }, confirmButton = { Button({ viewModel.add(title, if (tab == 0) LeisureKind.SERIES else LeisureKind.BOOK); add = false }) { Text("Salvar") } }, dismissButton = { TextButton({ add = false }) { Text("Cancelar") } })
    }
    if (reading) {
        var minutes by remember { mutableStateOf(state.readingMinutes.toString()) }
        var sessions by remember { mutableStateOf(state.readingSessions.toString()) }
        AlertDialog(onDismissRequest = { reading = false }, title = { Text("Sessões de leitura") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Minutos por sessão") }); OutlinedTextField(sessions, { sessions = it.filter(Char::isDigit) }, label = { Text("Sessões por semana") }) } }, confirmButton = { Button({ viewModel.saveReading(minutes.toIntOrNull() ?: 20, sessions.toIntOrNull() ?: 7); reading = false }) { Text("Ativar") } }, dismissButton = { TextButton({ reading = false }) { Text("Cancelar") } })
    }
}
