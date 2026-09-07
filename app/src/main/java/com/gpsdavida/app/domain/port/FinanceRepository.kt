package com.gpsdavida.app.domain.port

import com.gpsdavida.app.domain.model.Account
import com.gpsdavida.app.domain.model.Budget
import com.gpsdavida.app.domain.model.FinancialGoal
import com.gpsdavida.app.domain.model.Payable
import com.gpsdavida.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface FinanceRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeTransactions(): Flow<List<Transaction>>
    fun observePayables(): Flow<List<Payable>>
    fun observeBudgets(): Flow<List<Budget>>
    fun observeGoals(): Flow<List<FinancialGoal>>
    suspend fun saveAccount(account: Account)
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun savePayable(payable: Payable)
    suspend fun saveBudget(budget: Budget)
    suspend fun saveGoal(goal: FinancialGoal)
    suspend fun deleteAccount(id: String)
    suspend fun deleteTransaction(id: String)
    suspend fun deletePayable(id: String)
    suspend fun deleteBudget(id: String)
    suspend fun deleteGoal(id: String)
}
