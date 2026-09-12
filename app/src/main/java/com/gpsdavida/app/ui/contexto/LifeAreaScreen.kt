@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.superplanner.app.ui.contexto

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.superplanner.app.domain.model.LifeArea
import com.superplanner.app.domain.port.LifeAreaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Composable
fun LifeAreaScreen(viewModel: LifeAreaViewModel = hiltViewModel()) {
    val areas by viewModel.areas.collectAsStateWithLifecycle()
    var add by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("Áreas da vida") }) }, floatingActionButton = { FloatingActionButton({ add = true }) { Icon(Icons.Filled.Add, "Adicionar área") } }) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(areas, key = { it.id }) { area ->
                val visual = lifeAreaVisual(area.name)
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(visual.icon, contentDescription = area.name, tint = visual.accent)
                        Text(area.name, modifier = Modifier.weight(1f))
                        TextButton({ viewModel.delete(area.id) }) { Text("Excluir") }
                    }
                }
            }
        }
    }
    if (add) {
        var name by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { add = false }, title = { Text("Nova área") }, text = { OutlinedTextField(name, { name = it }, label = { Text("Nome") }) }, confirmButton = { Button({ viewModel.add(name); add = false }) { Text("Salvar") } }, dismissButton = { TextButton({ add = false }) { Text("Cancelar") } })
    }
}

@HiltViewModel
class LifeAreaViewModel @Inject constructor(private val repo: LifeAreaRepository) : ViewModel() {
    val areas: StateFlow<List<LifeArea>> = repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    fun add(name: String) { if (name.isBlank()) return; viewModelScope.launch { repo.save(LifeArea(UUID.randomUUID().toString(), name.trim())) } }
    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
}
