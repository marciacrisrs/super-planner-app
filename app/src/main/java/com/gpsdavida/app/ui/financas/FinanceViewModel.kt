package com.gpsdavida.app.ui.financas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsdavida.app.domain.model.*
import com.gpsdavida.app.domain.port.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class FinanceViewModel @Inject constructor(private val repo: FinanceRepository) : ViewModel() {
    data class State(
        val accounts: List<Account> = emptyList(), val transactions: List<Transaction> = emptyList(),
        val payables: List<Payable> = emptyList(), val budgets: List<Budget> = emptyList(), val goals: List<FinancialGoal> = emptyList(),
    )
    val state: StateFlow<State> = combine(repo.observeAccounts(), repo.observeTransactions(), repo.observePayables(), repo.observeBudgets(), repo.observeGoals()) { a,t,p,b,g -> State(a,t,p,b,g) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())
    fun addAccount(name: String, cents: Long) { if (name.isBlank()) return; viewModelScope.launch { repo.saveAccount(Account(UUID.randomUUID().toString(), name.trim(), cents)) } }
    fun addIncome(accountId: String, cents: Long, category: String) { if (cents <= 0) return; viewModelScope.launch { repo.saveTransaction(Transaction(UUID.randomUUID().toString(), accountId, TransactionType.INCOME, cents, LocalDate.now(), category)) } }
    fun addExpense(accountId: String, cents: Long, category: String) { if (cents <= 0) return; viewModelScope.launch { repo.saveTransaction(Transaction(UUID.randomUUID().toString(), accountId, TransactionType.EXPENSE, cents, LocalDate.now(), category, plannedAmountCents = cents)) } }
    fun addPayable(title: String, dueDate: LocalDate, cents: Long) { if (title.isBlank() || cents <= 0) return; viewModelScope.launch { repo.savePayable(Payable(UUID.randomUUID().toString(), title.trim(), dueDate, cents)) } }
    fun addBudget(month: String, area: String, cents: Long) { if (area.isBlank() || cents < 0) return; viewModelScope.launch { repo.saveBudget(Budget(UUID.randomUUID().toString(), month, area.trim(), cents)) } }
    fun addGoal(title: String, type: FinancialGoalType, targetCents: Long) { if (title.isBlank() || targetCents <= 0) return; viewModelScope.launch { repo.saveGoal(FinancialGoal(UUID.randomUUID().toString(), title.trim(), type, targetCents)) } }
}
