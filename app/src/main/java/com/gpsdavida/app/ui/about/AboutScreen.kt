package com.gpsdavida.app.ui.about

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.superplanner.app.BuildConfig
import com.superplanner.app.ui.theme.SuperPlannerBackground
import com.superplanner.app.ui.theme.SuperPlannerCard
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerSpacing

private const val GITHUB_URL = "https://github.com/marciacrisrs/super-planner-app"

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    SuperPlannerBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SuperPlannerSpacing.Page, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Voltar")
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Sobre",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SuperPlannerColors.Ink,
                )
                Text(
                    "Super Planner",
                    style = MaterialTheme.typography.titleLarge,
                    color = SuperPlannerColors.TerracottaDark,
                )
                Text(
                    "Um planner que entende que sua vida não cabe numa lista de tarefas.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SuperPlannerColors.InkSoft,
                )
            }

            SuperPlannerCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Projeto",
                        style = MaterialTheme.typography.labelLarge,
                        color = SuperPlannerColors.InkSoft,
                    )
                    Text(
                        "O Super Planner ajuda a transformar planos em uma rota adaptativa, com foco no que fazer agora.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuperPlannerColors.Ink,
                    )
                }
            }

            SuperPlannerCard(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                androidx.compose.material3.TextButton(
                    onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Outlined.Code, contentDescription = null)
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(horizontal = 4.dp))
                    Text("Ver projeto no GitHub")
                }
            }

            Text(
                "Versão ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = SuperPlannerColors.InkSoft,
            )
        }
    }
}
