package com.superplanner.app.ui.events

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.Event
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerSoftBackground
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun EventsListScreen(
    onAdd: () -> Unit,
    onOpen: (String) -> Unit,
    viewModel: EventsListViewModel = hiltViewModel(),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val isImporting by viewModel.isImporting.collectAsStateWithLifecycle()
    val importedCount by viewModel.importedCount.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingCalendarImport by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted && pendingCalendarImport) viewModel.importFromGoogleCalendar()
        pendingCalendarImport = false
    }

    fun importGoogleCalendar() {
        pendingCalendarImport = true
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            viewModel.importFromGoogleCalendar()
            pendingCalendarImport = false
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    val groupedEvents = events
        .sortedBy { it.range.start }
        .groupBy { event ->
            event.range.start.atZone(ZoneId.systemDefault()).toLocalDate()
        }

    SuperPlannerSoftBackground {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                EditorialEventsDecorations()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp,
                        top = padding.calculateTopPadding() + 28.dp,
                        end = 20.dp,
                        bottom = 32.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = stringResource(R.string.nav_eventos),
                                style = MaterialTheme.typography.headlineLarge,
                                color = SuperPlannerColors.Ink,
                            )
                            Text(
                                text = stringResource(R.string.events_subtitle),
                                style = MaterialTheme.typography.bodyLarge,
                                color = SuperPlannerColors.InkSoft,
                            )
                        }
                    }

                    item {
                        CalendarImportCard(
                            isImporting = isImporting,
                            importedCount = importedCount,
                            onImport = ::importGoogleCalendar,
                            onCreate = onAdd,
                        )
                    }

                    if (groupedEvents.isEmpty()) {
                        item { EventsEmptyState(onAdd = onAdd) }
                    } else {
                        groupedEvents.forEach { (date, dayEvents) ->
                            item { EventDayHeader(date) }
                            items(
                                items = dayEvents,
                                key = { it.id.value },
                            ) { event ->
                                EventRow(
                                    event = event,
                                    onClick = { onOpen(event.id.value) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorialEventsDecorations() {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.sp_decor_leaves_right),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopEnd).size(170.dp).padding(top = 18.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.18f,
        )
        Image(
            painter = painterResource(R.drawable.sp_decor_pink_blob),
            contentDescription = null,
            modifier = Modifier.align(Alignment.BottomStart).size(150.dp).padding(bottom = 18.dp),
            contentScale = ContentScale.Fit,
            alpha = 0.16f,
        )
    }
}

@Composable
private fun CalendarImportCard(
    isImporting: Boolean,
    importedCount: Int?,
    onImport: () -> Unit,
    onCreate: () -> Unit,
) {
    SuperPlannerCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.sp_category_appointments),
                contentDescription = null,
                modifier = Modifier.size(82.dp),
                contentScale = ContentScale.Fit,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = stringResource(R.string.events_agenda_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = SuperPlannerColors.Ink,
                )
                Text(
                    text = stringResource(R.string.events_agenda_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            }
        }
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = onImport,
                enabled = !isImporting,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuperPlannerColors.Terracotta,
                    contentColor = SuperPlannerColors.Surface,
                ),
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = SuperPlannerColors.Surface,
                    )
                    Spacer(Modifier.width(10.dp))
                } else {
                    Icon(Icons.Outlined.CloudDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.events_import_google))
            }
            TextButton(
                onClick = onCreate,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.events_create_manual))
            }
            importedCount?.let { count ->
                Text(
                    text = if (count == 0) stringResource(R.string.events_import_none) else stringResource(R.string.events_import_success, count),
                    style = MaterialTheme.typography.bodySmall,
                    color = SuperPlannerColors.InkSoft,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Composable
private fun EventsEmptyState(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SuperPlannerColors.SurfaceWarm),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.sp_empty_events),
                contentDescription = null,
                modifier = Modifier.size(150.dp),
                contentScale = ContentScale.Fit,
            )
            Text(
                text = stringResource(R.string.events_empty),
                style = MaterialTheme.typography.titleMedium,
                color = SuperPlannerColors.Ink,
            )
            Text(
                text = stringResource(R.string.events_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = SuperPlannerColors.InkSoft,
            )
            Button(
                onClick = onAdd,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuperPlannerColors.Terracotta),
            ) { Text(stringResource(R.string.events_create_manual)) }
        }
    }
}

@Composable
private fun EventDayHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> stringResource(R.string.events_today)
        today.plusDays(1) -> stringResource(R.string.events_tomorrow)
        else -> date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale("pt", "BR")))
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Outlined.Event,
            contentDescription = null,
            tint = SuperPlannerColors.Terracotta,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = SuperPlannerColors.Ink)
    }
}

@Composable
fun EventRow(
    event: Event,
    onClick: () -> Unit,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm", Locale("pt", "BR"))
    val start = event.range.start.atZone(zone).format(formatter)
    val end = event.range.end.atZone(zone).format(formatter)
    SuperPlannerCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(start, style = MaterialTheme.typography.labelLarge, color = SuperPlannerColors.Terracotta)
                Spacer(Modifier.height(4.dp))
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .background(SuperPlannerColors.Rose, androidx.compose.foundation.shape.CircleShape),
                )
            }
            Column(
                modifier = Modifier.padding(start = 16.dp).weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, color = SuperPlannerColors.Ink)
                Text(
                    text = "$start – $end",
                    style = MaterialTheme.typography.bodySmall,
                    color = SuperPlannerColors.InkSoft,
                )
            }
        }
    }
}