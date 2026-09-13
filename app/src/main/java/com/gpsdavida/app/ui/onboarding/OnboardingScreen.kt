package com.superplanner.app.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
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
import com.superplanner.app.ui.theme.SuperPlannerSpacing

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
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = SuperPlannerSpacing.Page, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(modifier = Modifier.size(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.sp_illustration_compass),
                    contentDescription = "Bússola do Super Planner",
                    modifier = Modifier.size(210.dp),
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painterResource(R.drawable.sp_logo_full),
                    contentDescription = "Super Planner",
                    modifier = Modifier.size(width = 220.dp, height = 92.dp),
                    contentScale = ContentScale.Fit,
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Primeira rota",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SuperPlannerColors.Ink,
                )
                Text(
                    "Vamos transformar o que importa para você em uma primeira rota simples para hoje.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SuperPlannerColors.InkSoft,
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
