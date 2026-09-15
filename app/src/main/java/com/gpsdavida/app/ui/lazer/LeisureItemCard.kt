package com.superplanner.app.ui.lazer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.superplanner.app.domain.model.LeisureItem
import com.superplanner.app.domain.model.LeisureStatus

@Composable
fun LeisureItemCard(item: LeisureItem, onStatus: (LeisureStatus) -> Unit, onDelete: () -> Unit, onSchedule: (() -> Unit)? = null) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.title)
            Text(item.status.name.lowercase())
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                LeisureStatus.entries.forEach { status -> TextButton(onClick = { onStatus(status) }) { Text(status.label()) } }
            }
            onSchedule?.let { TextButton(onClick = it) { Text("Programar") } }
            TextButton(onClick = onDelete) { Text("Excluir") }
        }
    }
}

private fun LeisureStatus.label(): String = when (this) {
    LeisureStatus.WANT -> "Quero"
    LeisureStatus.NEXT -> "Próximo"
    LeisureStatus.ACTIVE -> "Ativo"
    LeisureStatus.PAUSED -> "Pausado"
    LeisureStatus.COMPLETED -> "Concluído"
}
