package com.superplanner.app.ui.agora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.superplanner.app.R
import com.superplanner.app.ui.theme.SuperPlannerColors

@Composable
fun CapacityContextCard(
    remainingMinutes: Long?,
    nextWindowMinutes: Long?,
    modifier: Modifier = Modifier,
) {
    if (remainingMinutes == null && nextWindowMinutes == null) return

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.capacity_context_title),
                style = MaterialTheme.typography.titleMedium,
                color = SuperPlannerColors.Ink,
            )
            remainingMinutes?.let {
                Text(
                    text = stringResource(R.string.capacity_remaining, formatMinutes(it)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            }
            nextWindowMinutes?.let {
                Text(
                    text = stringResource(R.string.capacity_next_window, formatMinutes(it)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            }
            if (remainingMinutes != null && nextWindowMinutes != null && nextWindowMinutes < remainingMinutes) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.capacity_window_guidance),
                        style = MaterialTheme.typography.bodySmall,
                        color = SuperPlannerColors.InkSoft,
                    )
                }
            }
        }
    }
}

private fun formatMinutes(totalMinutes: Long): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0L -> "$minutes min"
        minutes == 0L -> "$hours h"
        else -> "$hours h $minutes min"
    }
}
