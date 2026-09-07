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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var add by remember { mutableStateOf(false) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Finanças") }) },
        floatingActionButton = { FloatingActionButton(onClick = { add = true }) { Icon(Icons.Filled.Add, "Adicionar") } },
    ) { padding ->
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
                        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(account.name); Text("Saldo: R$ ${balance / 100},${(balance % 100).toString().padStart(2,'0')}") } }
                    }
                    1 -> items(state.transactions) { tx -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(tx.category.ifBlank { "Lançamento" }); Text("${tx.date} · ${if (tx.type.name == "INCOME") "+" else "-"} R$ ${tx.amountCents / 100},${(tx.amountCents % 100).toString().padStart(2,'0')}") } } }
                    2 -> items(state.payables) { p -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(p.title); Text("Vence ${p.dueDate} · R$ ${p.amountCents / 100},${(p.amountCents % 100).toString().padStart(2,'0')}") } } }
                    3 -> items(state.goals) { g -> Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text(g.title); Text("${g.type.name.lowercase()} · alvo R$ ${g.targetCents / 100},${(g.targetCents % 100).toString().padStart(2,'0')}") } } }
                }
            }
        }
    }
    if (add) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { add = false },
            title = { Text(when (tab) { 0 -> "Nova conta"; 1 -> "Nova despesa"; 2 -> "Novo compromisso"; else -> "Nova meta" }) },
            text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(name, { name = it }, label = { Text("Nome") }); OutlinedTextField("", {}, label = { Text("Valor em centavos") }) } },
            confirmButton = { Button(onClick = { add = false; if (tab == 0) viewModel.addAccount(name, 0) }) { Text("Salvar") } },
            dismissButton = { TextButton({ add = false }) { Text("Cancelar") } },
        )
    }
}
