package com.superplanner.app.ui.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.Event
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: EventsListViewModel = hiltViewModel(),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_eventos)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.cd_add_event))
            }
        },
    ) { padding ->
        if (events.isEmpty()) {
            Text(
                text = stringResource(R.string.events_empty),
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            Column(modifier = Modifier.padding(padding)) {
                events.forEach { event ->
                    EventRow(event = event, onClick = { onOpen(event.id.value) })
                }
            }
        }
    }
}

@Composable
fun EventRow(
    event: Event,
    onClick: () -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale("pt", "BR"))
    val start = event.range.start.atZone(zone).format(formatter)
    val end = event.range.end.atZone(zone).format(formatter)
    ListItem(
        headlineContent = { Text(event.title) },
        supportingContent = { Text("$start – $end") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}
