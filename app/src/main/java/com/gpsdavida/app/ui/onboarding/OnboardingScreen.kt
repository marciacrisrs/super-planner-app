package com.superplanner.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.ui.theme.SuperPlannerBackground
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerPrimaryButton
import androidx.compose.material3.MaterialTheme

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) { if (finished) onFinished() }

    SuperPlannerBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.sp_logo_full),
                contentDescription = "Super Planner",
                modifier = Modifier.size(220.dp),
                contentScale = ContentScale.Fit,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Primeira rota",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SuperPlannerColors.Ink,
                )
                Text(
                    "Em poucos minutos, o Super Planner pode preparar uma primeira rota para hoje.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SuperPlannerColors.InkSoft,
                )
                Text(
                    "Vamos criar disponibilidade, um compromisso, uma tarefa e um hábito de exemplo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SuperPlannerColors.InkSoft,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SuperPlannerPrimaryButton(
                    text = "Criar minha primeira rota",
                    onClick = viewModel::createFirstRoute,
                )
                TextButton(onClick = viewModel::skip) {
                    Text(
                        "Pular por enquanto",
                        style = MaterialTheme.typography.labelLarge,
                        color = SuperPlannerColors.InkSoft,
                    )
                }
            }
        }
    }
}
