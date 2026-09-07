package com.gpsdavida.app.ui.financas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gpsdavida.app.domain.model.FinancialGoalType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var add by remember { mutableStateOf(false) }
    Scaffold(topBar = { TopAppBar(title = { Text("Finanças") }) }, floatingActionButton = { FloatingActionButton(onClick = { add = true }) { Icon(Icons.Filled.Add, "Adicionar") } }) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("Contas") })
                Tab(tab == 1, { tab = 1 }, text = { Text("Movimentos") })
                Tab(tab == 2, { tab = 2 }, text = { Text("Compromissos") })
                Tab(tab == 3, { tab = 3 }, text = { Text("Metas") })
            }
            LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (tab) {
                    0 -> items(state.accounts) { account ->
                        val balance = account.initialBalanceCents + state.transactions.filter { it.accountId == account.id }.sumOf { if (it.type.name == "INCOME") it.amountCents else -it.amountCents }
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(account.name); Text("Saldo: R$ ${formatCents(balance)}") } }
                    }
                    1 -> items(state.transactions) { tx -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(tx.category.ifBlank { "Lançamento" }); Text("${tx.date} · ${if (tx.type.name == "INCOME") "+" else "-"} R$ ${formatCents(tx.amountCents)}"); tx.plannedAmountCents?.let { Text("Planejado: R$ ${formatCents(it)}") } } } }
                    2 -> items(state.payables) { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(p.title); Text("Vence ${p.dueDate} · R$ ${formatCents(p.amountCents)}"); if (p.paid) Text("Pago") } } }
                    3 -> items(state.goals) { g -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(g.title); Text("${if (g.type == FinancialGoalType.SAVING) "Poupar" else "Quitar dívida"} · alvo R$ ${formatCents(g.targetCents)}"); Text("${g.currentCents * 100 / g.targetCents.coerceAtLeast(1)}%") } } }
                }
            }
        }
    }
    if (add) {
        var name by remember(tab) { mutableStateOf("") }
        var value by remember(tab) { mutableStateOf("") }
        var category by remember(tab) { mutableStateOf("") }
        var accountId by remember(tab) { mutableStateOf(state.accounts.firstOrNull()?.id.orEmpty()) }
        var goalType by remember(tab) { mutableStateOf(FinancialGoalType.SAVING) }
        AlertDialog(
            onDismissRequest = { add = false },
            title = { Text(when (tab) { 0 -> "Nova conta"; 1 -> "Nova despesa"; 2 -> "Novo compromisso"; else -> "Nova meta financeira" }) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(name, { name = it }, label = { Text(if (tab == 1) "Categoria" else "Nome") }, singleLine = true)
                    if (tab != 0) OutlinedTextField(value, { value = it.filter(Char::isDigit) }, label = { Text("Valor em centavos") }, singleLine = true)
                    if (tab == 1) OutlinedTextField(category, { category = it }, label = { Text("Categoria") }, singleLine = true)
                    if (tab == 1) OutlinedTextField(accountId, { accountId = it }, label = { Text("ID da conta") }, singleLine = true)
                    if (tab == 3) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TextButton({ goalType = FinancialGoalType.SAVING }) { Text("Poupança") }; TextButton({ goalType = FinancialGoalType.DEBT }) { Text("Dívida") } }
                }
            },
            confirmButton = {
                Button(onClick = {
                    when (tab) {
                        0 -> viewModel.addAccount(name, value.toLongOrNull() ?: 0)
                        1 -> viewModel.addExpense(accountId, value.toLongOrNull() ?: 0, category.ifBlank { name })
                        2 -> viewModel.addPayable(name, LocalDate.now().plusDays(1), value.toLongOrNull() ?: 0)
                        else -> viewModel.addGoal(name, goalType, value.toLongOrNull() ?: 0)
                    }
                    add = false
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton({ add = false }) { Text("Cancelar") } },
        )
    }
}

private fun formatCents(cents: Long): String = "${cents / 100},${(cents % 100).toString().padStart(2, '0')}"
