package com.superplanner.app.ui.notas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

@Composable
fun NotesScreenV2(viewModel: NotesViewModel = hiltViewModel()) {
    var targetType by remember { mutableStateOf("planejamento") }
    var targetId by remember { mutableStateOf("geral") }
    var body by remember { mutableStateOf("") }
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    LaunchedEffect(targetType, targetId) { viewModel.setContext(targetType, targetId) }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(targetType, { targetType = it }, label = { Text("Tipo") }, modifier = Modifier.weight(1f))
            OutlinedTextField(targetId, { targetId = it }, label = { Text("ID") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(body, { body = it }, label = { Text("Nota") }, modifier = Modifier.fillMaxWidth())
        Button({ viewModel.save(targetType, targetId, body); body = "" }, modifier = Modifier.fillMaxWidth()) { Text("Salvar nota") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(notes) { note ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(note.body, modifier = Modifier.weight(1f))
                        Button({ viewModel.delete(note.id) }) { Text("Excluir") }
                    }
                }
            }
        }
    }
}
