package com.superplanner.app.ui.notas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

@Composable
fun NotesScreen(viewModel: NotesViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    var targetType by remember { mutableStateOf("planejamento") }
    var targetId by remember { mutableStateOf("geral") }
    var body by remember { mutableStateOf("") }
    Scaffold(topBar = { TopAppBar(title = { Text("Notas contextuais") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(targetType, { targetType = it }, label = { Text("Tipo do contexto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(targetId, { targetId = it }, label = { Text("ID do contexto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(body, { body = it }, label = { Text("Nota") }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.save(targetType, targetId, body); body = "" }, modifier = Modifier.fillMaxWidth()) { Text("Salvar nota") }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { Text("${note.targetType}/${note.targetId}"); Text(note.body); Text("Excluir", modifier = Modifier.padding(top = 4.dp)) } } }
            }
        }
    }
}
