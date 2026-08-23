package com.gpsdavida.app.ui.agora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsdavida.app.R
import com.gpsdavida.app.ui.next.NextActionCard
import com.gpsdavida.app.ui.next.NextActionUiModel
import com.gpsdavida.app.ui.tasks.labelRes
import com.gpsdavida.app.ui.theme.GpsDaVidaColors
import com.gpsdavida.app.ui.widget.AgoraWidgetProvider
import com.gpsdavida.app.ui.widget.AgoraWidgetSnapshot
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun AgoraScreen(
    viewModel: AgoraViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    val dateFmt = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale("pt", "BR"))

    LaunchedEffect(state.title, state.scheduledTime, state.durationMinutes, state.currentActivity) {
        val context = androidx.compose.ui.platform.LocalContext.current
        AgoraWidgetSnapshot.write(
            context = context,
            snapshot = AgoraWidgetSnapshot(
                title = state.title,
                scheduledTime = state.scheduledTime?.format(timeFmt).orEmpty(),
                durationMinutes = state.durationMinutes?.toInt() ?: 0,
                isEmpty = state.currentActivity == null,
            ),
        )
        AgoraWidgetProvider.updateAll(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.nav_agora),
                style = MaterialTheme.typography.labelLarge,
                color = GpsDaVidaColors.TerracottaDark,
            )
            Text(
                text = state.currentTime.format(timeFmt),
                style = MaterialTheme.typography.displaySmall,
                color = GpsDaVidaColors.Ink,
            )
            Text(
                text = state.currentDate.format(dateFmt),
                style = MaterialTheme.typography.bodyMedium,
                color = GpsDaVidaColors.InkSoft,
            )
        }

        NextActionCard(
            modifier = Modifier.fillMaxWidth(),
            model = NextActionUiModel(
                title = state.title,
                durationMinutes = state.durationMinutes,
                scheduledTime = state.scheduledTime,
                priorityLabel = state.priority?.let { stringResource(it.labelRes()) },
                state = state.state,
            ),
            oneTapComplete = true,
            onSnooze = viewModel::deferCurrent,
            onComplete = viewModel::completeCurrent,
            onSwap = viewModel::skipCurrent,
        )

        if (state.nextUpcoming != null || state.laterUpcoming.isNotEmpty()) {
            AgoraUpcomingSection(
                next = state.nextUpcoming,
                later = state.laterUpcoming,
            )
        }
    }
}
