package com.superplanner.app.ui.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
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

@Composable
fun BackupScreen(viewModel: BackupViewModel = hiltViewModel()) {
    var pendingRestore by remember { mutableStateOf<Uri?>(null) }
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri -> uri?.let(viewModel::export) }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> pendingRestore = uri }

    Scaffold(topBar = { TopAppBar(title = { Text("Backup e restauração") }) }) { padding ->
        Column(
            Modifier.fillMaxWidth().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("O backup fica local e contém o banco do Super Planner.")
            Button(onClick = { create.launch("super-planner-backup.db") }, modifier = Modifier.fillMaxWidth()) { Text("Exportar backup") }
            OutlinedButton(onClick = { open.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Restaurar backup") }
            viewModel.error?.let { Text("Erro: $it") }
        }
    }

    pendingRestore?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("Substituir dados?") },
            text = { Text("O restore substituirá os dados locais atuais. Faça um backup antes de continuar.") },
            confirmButton = {
                Button(onClick = { viewModel.restore(uri); pendingRestore = null }) { Text("Restaurar") }
            },
        )
    }
}
