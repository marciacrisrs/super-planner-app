package com.superplanner.app.ui.agora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.superplanner.app.R
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerCard
import java.time.format.DateTimeFormatter

@Composable
fun AgoraUpcomingSection(
    next: AgoraUpcomingItem?,
    later: List<AgoraUpcomingItem>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        next?.let {
            SectionLabel(stringResource(R.string.agora_up_next))
            UpcomingRow(item = it, emphasized = true)
        }
        if (later.isNotEmpty()) {
            SectionLabel(stringResource(R.string.agora_later))
            later.forEach { item ->
                UpcomingRow(item = item, emphasized = false)
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = SuperPlannerColors.InkSoft,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun UpcomingRow(
    item: AgoraUpcomingItem,
    emphasized: Boolean,
) {
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
    SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (emphasized) 20.dp else 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = item.title,
                style = if (emphasized) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = SuperPlannerColors.Ink,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.scheduledTime.format(timeFmt),
                    style = MaterialTheme.typography.labelLarge,
                    color = SuperPlannerColors.TerracottaDark,
                )
                Text(
                    text = stringResource(R.string.next_action_duration, item.durationMinutes),
                    style = MaterialTheme.typography.labelMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            }
        }
    }
}
