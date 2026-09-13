package com.superplanner.app.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.superplanner.app.domain.model.Account
import com.superplanner.app.domain.model.Budget
import com.superplanner.app.domain.model.FinancialGoal
import com.superplanner.app.domain.model.FinancialGoalType
import com.superplanner.app.domain.model.Payable
import com.superplanner.app.domain.model.RecurrenceUnit
import com.superplanner.app.domain.model.Transaction
import com.superplanner.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "finance_accounts")
data class AccountEntity(@PrimaryKey val id: String, val name: String, val initialBalanceCents: Long)
@Entity(tableName = "finance_transactions")
data class TransactionEntity(@PrimaryKey val id: String, val accountId: String, val type: String, val amountCents: Long, val epochDay: Long, val category: String, val activityId: String?, val plannedAmountCents: Long?)
@Entity(tableName = "finance_payables")
data class PayableEntity(@PrimaryKey val id: String, val title: String, val dueEpochDay: Long, val amountCents: Long, val recurrenceInterval: Int, val recurrenceUnit: String?, val paid: Boolean)
@Entity(tableName = "finance_budgets")
data class BudgetEntity(@PrimaryKey val id: String, val month: String, val area: String, val limitCents: Long)
@Entity(tableName = "finance_goals")
data class FinancialGoalEntity(@PrimaryKey val id: String, val title: String, val type: String, val targetCents: Long, val dueEpochDay: Long?, val currentCents: Long)

@Dao interface FinanceDao {
    @androidx.room.Query("SELECT * FROM finance_accounts ORDER BY name") fun accounts(): Flow<List<AccountEntity>>
    @androidx.room.Query("SELECT * FROM finance_transactions ORDER BY epochDay DESC") fun transactions(): Flow<List<TransactionEntity>>
    @androidx.room.Query("SELECT * FROM finance_payables ORDER BY dueEpochDay") fun payables(): Flow<List<PayableEntity>>
    @androidx.room.Query("SELECT * FROM finance_budgets ORDER BY month, area") fun budgets(): Flow<List<BudgetEntity>>
    @androidx.room.Query("SELECT * FROM finance_goals ORDER BY dueEpochDay") fun goals(): Flow<List<FinancialGoalEntity>>
    @androidx.room.Upsert suspend fun upsertAccount(e: AccountEntity)
    @androidx.room.Upsert suspend fun upsertTransaction(e: TransactionEntity)
    @androidx.room.Upsert suspend fun upsertPayable(e: PayableEntity)
    @androidx.room.Upsert suspend fun upsertBudget(e: BudgetEntity)
    @androidx.room.Upsert suspend fun upsertGoal(e: FinancialGoalEntity)
    @androidx.room.Query("DELETE FROM finance_accounts WHERE id = :id") suspend fun deleteAccount(id: String)
    @androidx.room.Query("DELETE FROM finance_transactions WHERE id = :id") suspend fun deleteTransaction(id: String)
    @androidx.room.Query("DELETE FROM finance_payables WHERE id = :id") suspend fun deletePayable(id: String)
    @androidx.room.Query("DELETE FROM finance_budgets WHERE id = :id") suspend fun deleteBudget(id: String)
    @androidx.room.Query("DELETE FROM finance_goals WHERE id = :id") suspend fun deleteGoal(id: String)
}
