package com.superplanner.app.ui.agora

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.superplanner.app.R
import com.superplanner.app.ui.theme.SuperPlannerColors

@Composable
fun AgoraRecoveryCard(
    hasCurrentActivity: Boolean,
    onReplan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.agora_reality_title),
                style = MaterialTheme.typography.titleMedium,
                color = SuperPlannerColors.Ink,
            )
            Text(
                text = stringResource(
                    if (hasCurrentActivity) R.string.agora_reality_current_body else R.string.agora_reality_empty_body,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = SuperPlannerColors.InkSoft,
            )
            TextButton(onClick = onReplan) {
                Text(stringResource(R.string.agora_replan_action))
            }
        }
    }
}
