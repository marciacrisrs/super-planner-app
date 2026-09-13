package com.superplanner.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.ui.agora.AgoraViewModel
import com.superplanner.app.ui.next.NextActionCard
import com.superplanner.app.ui.next.NextActionUiModel
import com.superplanner.app.ui.tasks.labelRes
import com.superplanner.app.ui.theme.SuperPlannerBackground
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerSectionHeader
import com.superplanner.app.ui.theme.SuperPlannerSpacing
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/** Calm editorial home: one primary decision, one look-ahead, no vertical scrolling. */
@Composable
fun HomeScreen(
    viewModel: AgoraViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale("pt", "BR"))
    val greeting = when (state.currentTime.hour) {
        in 5..11 -> "Bom dia"
        in 12..17 -> "Boa tarde"
        else -> "Boa noite"
    }

    SuperPlannerBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SuperPlannerSpacing.Page, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.sp_logo_mark),
                        contentDescription = "Super Planner",
                        modifier = Modifier.size(42.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(greeting, style = MaterialTheme.typography.headlineLarge, color = SuperPlannerColors.Ink)
                        Text(
                            state.currentDate.format(dateFormatter).replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuperPlannerColors.InkSoft,
                        )
                    }
                }
                Text("♡", style = MaterialTheme.typography.headlineMedium, color = SuperPlannerColors.Terracotta)
            }

            SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Seu dia", style = MaterialTheme.typography.titleLarge, color = SuperPlannerColors.Ink)
                        Text(
                            "Um passo de cada vez. O Super Planner organiza o próximo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SuperPlannerColors.InkSoft,
                        )
                    }
                    Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.sp_decor_heart),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                SuperPlannerSectionHeader(
                    title = "O que faço agora?",
                    supportingText = state.currentTime.format(timeFormatter),
                )
                NextActionCard(
                    modifier = Modifier.fillMaxWidth(),
                    model = NextActionUiModel(
                        title = state.title,
                        durationMinutes = state.durationMinutes,
                        scheduledTime = state.scheduledTime,
                        priorityLabel = state.priority?.let { stringResource(it.labelRes()) },
                        reasonLabels = state.reasons.map { reason ->
                            when (reason) {
                                com.superplanner.app.domain.model.NextActionReason.CURRENTLY_ACTIVE -> stringResource(R.string.reason_currently_active)
                                com.superplanner.app.domain.model.NextActionReason.DUE_NOW -> stringResource(R.string.reason_due_now)
                                com.superplanner.app.domain.model.NextActionReason.FIXED_COMMITMENT -> stringResource(R.string.reason_fixed)
                                com.superplanner.app.domain.model.NextActionReason.AVAILABLE_IN_WINDOW -> stringResource(R.string.reason_available)
                                com.superplanner.app.domain.model.NextActionReason.ENERGY_MATCH -> stringResource(R.string.reason_energy)
                                com.superplanner.app.domain.model.NextActionReason.CONTEXT_MATCH -> stringResource(R.string.reason_context)
                                com.superplanner.app.domain.model.NextActionReason.DEPENDENCIES_SATISFIED -> stringResource(R.string.reason_dependencies)
                                com.superplanner.app.domain.model.NextActionReason.FLEXIBLE_SLOT -> stringResource(R.string.reason_flexible)
                                com.superplanner.app.domain.model.NextActionReason.TRAVEL_FITS -> stringResource(R.string.reason_travel)
                            }
                        },
                        state = state.state,
                    ),
                    oneTapComplete = true,
                    onSnooze = viewModel::deferCurrent,
                    onComplete = viewModel::completeCurrent,
                    onSwap = viewModel::skipCurrent,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            state.nextUpcoming?.let { upcoming ->
                SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape)) {
                            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                                drawCircle(SuperPlannerColors.Sage)
                            }
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Depois", style = MaterialTheme.typography.labelMedium, color = SuperPlannerColors.InkSoft)
                            Text(upcoming.title, style = MaterialTheme.typography.titleMedium, color = SuperPlannerColors.Ink)
                        }
                        Text(
                            upcoming.scheduledTime.format(timeFormatter),
                            style = MaterialTheme.typography.labelLarge,
                            color = SuperPlannerColors.TerracottaDark,
                        )
                    }
                }
            }
        }
    }
}
