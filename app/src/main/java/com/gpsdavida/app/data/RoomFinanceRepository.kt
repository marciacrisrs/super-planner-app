package com.superplanner.app.data

import com.superplanner.app.data.local.AccountEntity
import com.superplanner.app.data.local.BudgetEntity
import com.superplanner.app.data.local.FinanceDao
import com.superplanner.app.data.local.FinancialGoalEntity
import com.superplanner.app.data.local.PayableEntity
import com.superplanner.app.data.local.TransactionEntity
import com.superplanner.app.domain.model.*
import com.superplanner.app.domain.port.FinanceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomFinanceRepository @Inject constructor(private val dao: FinanceDao) : FinanceRepository {
    override fun observeAccounts(): Flow<List<Account>> = dao.accounts().map { it.map { e -> Account(e.id,e.name,e.initialBalanceCents) } }
    override fun observeTransactions(): Flow<List<Transaction>> = dao.transactions().map { it.map { e -> Transaction(e.id,e.accountId,TransactionType.valueOf(e.type),e.amountCents,java.time.LocalDate.ofEpochDay(e.epochDay),e.category,e.activityId,e.plannedAmountCents) } }
    override fun observePayables(): Flow<List<Payable>> = dao.payables().map { it.map { e -> Payable(e.id,e.title,java.time.LocalDate.ofEpochDay(e.dueEpochDay),e.amountCents,e.recurrenceInterval,e.recurrenceUnit?.let(RecurrenceUnit::valueOf),e.paid) } }
    override fun observeBudgets(): Flow<List<Budget>> = dao.budgets().map { it.map { e -> Budget(e.id,e.month,e.area,e.limitCents) } }
    override fun observeGoals(): Flow<List<FinancialGoal>> = dao.goals().map { it.map { e -> FinancialGoal(e.id,e.title,FinancialGoalType.valueOf(e.type),e.targetCents,e.dueEpochDay?.let(java.time.LocalDate::ofEpochDay),e.currentCents) } }
    override suspend fun saveAccount(a: Account) = dao.upsertAccount(AccountEntity(a.id,a.name,a.initialBalanceCents))
    override suspend fun saveTransaction(t: Transaction) = dao.upsertTransaction(TransactionEntity(t.id,t.accountId,t.type.name,t.amountCents,t.date.toEpochDay(),t.category,t.activityId,t.plannedAmountCents))
    override suspend fun savePayable(p: Payable) = dao.upsertPayable(PayableEntity(p.id,p.title,p.dueDate.toEpochDay(),p.amountCents,p.recurrenceInterval,p.recurrenceUnit?.name,p.paid))
    override suspend fun saveBudget(b: Budget) = dao.upsertBudget(BudgetEntity(b.id,b.month,b.area,b.limitCents))
    override suspend fun saveGoal(g: FinancialGoal) = dao.upsertGoal(FinancialGoalEntity(g.id,g.title,g.type.name,g.targetCents,g.dueDate?.toEpochDay(),g.currentCents))
    override suspend fun deleteAccount(id: String) = dao.deleteAccount(id)
    override suspend fun deleteTransaction(id: String) = dao.deleteTransaction(id)
    override suspend fun deletePayable(id: String) = dao.deletePayable(id)
    override suspend fun deleteBudget(id: String) = dao.deleteBudget(id)
    override suspend fun deleteGoal(id: String) = dao.deleteGoal(id)
}
