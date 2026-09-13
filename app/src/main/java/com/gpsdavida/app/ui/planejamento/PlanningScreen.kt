package com.superplanner.app.ui.planejamento

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.superplanner.app.R
import com.superplanner.app.domain.model.InboxStatus
import com.superplanner.app.domain.model.Project
import com.superplanner.app.ui.backup.BackupScreen
import com.superplanner.app.ui.lazer.LeisureScreenV2
import com.superplanner.app.ui.notas.NotesScreenV2
import com.superplanner.app.ui.theme.SuperPlannerColors
import com.superplanner.app.ui.theme.SuperPlannerSoftBackground

@Composable
fun PlanningScreen(
    onOpenHorizons: () -> Unit,
    onOpenReview: () -> Unit,
    onOpenFinance: () -> Unit,
    onOpenLifeAreas: () -> Unit,
    onOpenDayCheckpoint: () -> Unit,
    onOpenPlans: () -> Unit,
    onOpenRoutines: () -> Unit,
    viewModel: PlanningViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var addType by remember { mutableStateOf<AddType?>(null) }
    var projectForStep by remember { mutableStateOf<Project?>(null) }
    var showMore by remember { mutableStateOf(false) }
    var secondaryScreen by remember { mutableStateOf<SecondaryScreen?>(null) }

    when (secondaryScreen) {
        SecondaryScreen.BACKUP -> { BackupScreen(); return }
        SecondaryScreen.LEISURE -> { LeisureScreenV2(); return }
        SecondaryScreen.NOTES -> { NotesScreenV2(); return }
        null -> Unit
    }

    SuperPlannerSoftBackground(modifier = Modifier.fillMaxSize()) {
        EditorialPlanningDecorations()

        Scaffold(
            containerColor = Color.Transparent,
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Planejar", style = MaterialTheme.typography.headlineMedium)
                            Box(Modifier.weight(1f))
                            IconButton(onClick = { showMore = !showMore }) { Icon(Icons.Outlined.MoreHoriz, contentDescription = "Mais opções") }
                        }
                        Text("Organize o que importa. Depois, deixe a Rota transformar isso em ação.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item { SectionLabel("O que importa") }
                item { PlanningCard(Icons.Outlined.Timeline, "Horizontes", "Onde quero chegar?", onOpenHorizons) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallPlanningCard(Modifier.weight(1f), Icons.Outlined.Flag, "Áreas", "O que cuidar", onOpenLifeAreas)
                        SmallPlanningCard(Modifier.weight(1f), Icons.Outlined.Flag, "Metas", "O que conquistar") { addType = AddType.GOAL }
                    }
                }
                item { SectionLabel("O que estou construindo") }
                item { PlanningCard(Icons.Outlined.FolderOpen, "Projetos", if (state.projects.isEmpty()) "Transforme uma meta em resultado concreto" else "${state.projects.size} projeto(s) em andamento") { addType = AddType.PROJECT } }
                if (state.projects.isNotEmpty()) {
                    items(state.projects.take(3), key = { it.id.value }) { project -> ProjectPreview(project, { projectForStep = project }, { viewModel.deleteProject(project.id.value) }) }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallPlanningCard(Modifier.weight(1f), Icons.Outlined.EditNote, "Planos", "Como fazer", onOpenPlans)
                        SmallPlanningCard(Modifier.weight(1f), Icons.Outlined.Loop, "Rotinas", "O que se repete", onOpenRoutines)
                    }
                }
                item { SectionLabel("O que ainda precisa de atenção") }
                item { InboxCard(state.inbox.count { it.status != InboxStatus.DISCARDED }) { addType = AddType.INBOX } }
                item {
                    Card(Modifier.fillMaxWidth().clickable(onClick = onOpenReview), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = SuperPlannerColors.TerracottaSoft)) {
                        Box(Modifier.fillMaxWidth()) {
                            Image(
                                painter = painterResource(R.drawable.sp_illustration_compass),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.CenterEnd).size(112.dp).alpha(0.34f),
                                contentScale = ContentScale.Fit,
                            )
                            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Icon(Icons.Outlined.Refresh, null, Modifier.size(28.dp), tint = SuperPlannerColors.Terracotta)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text("Revisar minha vida", style = MaterialTheme.typography.titleMedium, color = SuperPlannerColors.Terracotta); Text("Pare, olhe e ajuste o que mudou.", style = MaterialTheme.typography.bodyMedium, color = SuperPlannerColors.Terracotta) }
                                Icon(Icons.Outlined.ArrowForward, null, tint = SuperPlannerColors.Terracotta)
                            }
                        }
                    }
                }
                if (showMore) {
                    item { SectionLabel("Outras ferramentas") }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onOpenFinance, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Paid, null); Text(" Finanças") }
                            OutlinedButton(onClick = onOpenDayCheckpoint, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.CalendarToday, null); Text(" Hoje") }
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { secondaryScreen = SecondaryScreen.NOTES }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.EditNote, null); Text(" Notas") }
                            OutlinedButton(onClick = { secondaryScreen = SecondaryScreen.BACKUP }, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Backup, null); Text(" Backup") }
                        }
                    }
                    item { OutlinedButton(onClick = { secondaryScreen = SecondaryScreen.LEISURE }, modifier = Modifier.fillMaxWidth()) { Text("Lazer") } }
                }
            }
        }
    }

    addType?.let { type ->
        var text by remember(type) { mutableStateOf("") }
        AlertDialog(onDismissRequest = { addType = null }, title = { Text(when (type) { AddType.GOAL -> "Nova meta"; AddType.PROJECT -> "Novo projeto"; AddType.INBOX -> "Capturar ideia" }) }, text = { OutlinedTextField(text, { text = it }, modifier = Modifier.fillMaxWidth(), singleLine = true) }, confirmButton = { TextButton(onClick = { when (type) { AddType.GOAL -> viewModel.createGoal(text); AddType.PROJECT -> viewModel.createProject(text, null); AddType.INBOX -> viewModel.capture(text) }; addType = null }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = { addType = null }) { Text("Cancelar") } })
    }

    projectForStep?.let { project ->
        var title by remember(project.id) { mutableStateOf("") }
        var minutes by remember(project.id) { mutableStateOf("30") }
        AlertDialog(onDismissRequest = { projectForStep = null }, title = { Text("Nova etapa") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(title, { title = it }, label = { Text("Etapa") }); OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Minutos") }) } }, confirmButton = { TextButton(onClick = { viewModel.addProjectStep(project, title, minutes.toLongOrNull() ?: 30); projectForStep = null }) { Text("Salvar") } }, dismissButton = { TextButton(onClick = { projectForStep = null }) { Text("Cancelar") } })
    }
}

@Composable
private fun EditorialPlanningDecorations() {
    Image(
        painter = painterResource(R.drawable.sp_decor_leaves_right),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().size(180.dp).alpha(0.16f),
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopEnd,
    )
    Image(
        painter = painterResource(R.drawable.sp_decor_pink_blob),
        contentDescription = null,
        modifier = Modifier.size(230.dp).alpha(0.10f).padding(top = 210.dp),
        contentScale = ContentScale.Fit,
        alignment = Alignment.TopStart,
    )
    Image(
        painter = painterResource(R.drawable.sp_decor_leaves_left),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().size(180.dp).alpha(0.11f).padding(top = 420.dp),
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomStart,
    )
}

private enum class AddType { GOAL, PROJECT, INBOX }
private enum class SecondaryScreen { BACKUP, LEISURE, NOTES }

@Composable private fun SectionLabel(text: String) { Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, start = 4.dp)) }

@Composable private fun PlanningCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) { Text(title, style = MaterialTheme.typography.titleLarge); Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Outlined.ArrowForward, null)
        }
    }
}

@Composable private fun SmallPlanningCard(modifier: Modifier, icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Icon(icon, null, Modifier.size(26.dp), tint = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.titleMedium); Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun ProjectPreview(project: Project, onAddStep: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(project.title, style = MaterialTheme.typography.titleMedium)
            Text(if (project.steps.isEmpty()) "Ainda sem etapas" else "${project.steps.size} etapa(s)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            project.steps.take(2).forEach { Text("• ${it.title}", style = MaterialTheme.typography.bodyMedium) }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TextButton(onClick = onAddStep) { Text("Adicionar etapa") }; TextButton(onClick = onDelete) { Text("Excluir") } }
        }
    }
}

@Composable private fun InboxCard(count: Int, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(Icons.Outlined.EditNote, null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) { Text("Inbox", style = MaterialTheme.typography.titleMedium); Text(if (count == 0) "Está tudo organizado." else "$count item(ns) aguardando organização.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Icon(Icons.Outlined.ArrowForward, null)
        }
    }
}
