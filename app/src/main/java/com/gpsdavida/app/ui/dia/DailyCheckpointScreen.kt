package com.gpsdavida.app.ui.dia

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.time.LocalDate

@Composable
fun DailyCheckpointScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("daily_checkpoint", Context.MODE_PRIVATE) }
    val today = remember { LocalDate.now().toString() }
    var available by remember { mutableStateOf(prefs.getString("$today.available", "") ?: "") }
    var energy by remember { mutableStateOf(prefs.getString("$today.energy", "") ?: "") }
    var closing by remember { mutableStateOf(prefs.getString("$today.closing", "") ?: "") }
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Abertura e fechamento do dia", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Abertura")
                OutlinedTextField(available, { available = it }, label = { Text("Disponibilidade de hoje") })
                OutlinedTextField(energy, { energy = it }, label = { Text("Energia (baixa / média / alta)") })
                Button(onClick = { prefs.edit().putString("$today.available", available).putString("$today.energy", energy).apply() }) { Text("Salvar abertura") }
            }
        }
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fechamento")
                OutlinedTextField(closing, { closing = it }, label = { Text("O que ficou para depois?") })
                Button(onClick = { prefs.edit().putString("$today.closing", closing).apply() }) { Text("Encerrar dia") }
            }
        }
    }
}
