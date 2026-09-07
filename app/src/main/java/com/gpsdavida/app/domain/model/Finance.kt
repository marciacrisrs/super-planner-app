package com.gpsdavida.app.domain.model

import java.time.LocalDate

enum class TransactionType { INCOME, EXPENSE }
enum class FinancialGoalType { SAVING, DEBT }

data class Account(
    val id: String,
    val name: String,
    val initialBalanceCents: Long = 0,
)

data class Transaction(
    val id: String,
    val accountId: String,
    val type: TransactionType,
    val amountCents: Long,
    val date: LocalDate,
    val category: String = "",
    val activityId: String? = null,
    val plannedAmountCents: Long? = null,
)

data class Payable(
    val id: String,
    val title: String,
    val dueDate: LocalDate,
    val amountCents: Long,
    val recurrenceInterval: Int = 1,
    val recurrenceUnit: com.gpsdavida.app.domain.model.RecurrenceUnit? = null,
    val paid: Boolean = false,
)

data class Budget(
    val id: String,
    val month: String,
    val area: String,
    val limitCents: Long,
)

data class FinancialGoal(
    val id: String,
    val title: String,
    val type: FinancialGoalType,
    val targetCents: Long,
    val dueDate: LocalDate? = null,
    val currentCents: Long = 0,
)
