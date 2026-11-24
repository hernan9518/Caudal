package com.empresa.caudal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.empresa.caudal.data.local.UserSettings
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionEntity
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.data.repository.CaudalRepository
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import android.content.Context
import com.empresa.caudal.notifications.ReminderScheduler

@OptIn(ExperimentalCoroutinesApi::class)
class CaudalViewModel(
    private val repository: CaudalRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTransactions: StateFlow<List<TransactionWithCategory>> = repository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyTransactions: StateFlow<List<TransactionWithCategory>> = _selectedMonth
        .flatMapLatest { month ->
            val (start, end) = DateUtils.getMonthBounds(month)
            repository.getTransactionsBetween(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectPreviousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun selectNextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun resetToCurrentMonth() {
        _selectedMonth.value = YearMonth.now()
    }

    fun saveTransaction(
        id: Long = 0,
        amount: Double,
        note: String,
        dateMillis: Long,
        type: TransactionType,
        categoryId: Long
    ) {
        if (amount <= 0.0) return
        viewModelScope.launch {
            val entity = TransactionEntity(
                id = id,
                amount = amount,
                note = note.trim(),
                dateMillis = dateMillis,
                type = type,
                categoryId = categoryId
            )
            if (id == 0L) {
                repository.insertTransaction(entity)
            } else {
                repository.updateTransaction(entity)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun saveCategory(
        id: Long = 0,
        name: String,
        iconKey: String,
        colorHex: Long,
        type: TransactionType,
        monthlyBudget: Double = 0.0,
        isDefault: Boolean = false
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entity = CategoryEntity(
                id = id,
                name = name.trim(),
                iconKey = iconKey,
                colorHex = colorHex,
                type = type,
                monthlyBudget = monthlyBudget,
                isDefault = isDefault
            )
            if (id == 0L) {
                repository.insertCategory(entity)
            } else {
                repository.updateCategory(entity)
            }
        }
    }

    fun deleteCategory(category: CategoryEntity, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val deleted = repository.deleteCategorySafely(category)
            onResult(deleted)
        }
    }

    fun updateMonthlyBudget(amount: Double) {
        viewModelScope.launch { repository.setMonthlyBudget(amount.coerceAtLeast(0.0)) }
    }

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkTheme(enabled) }
    }

    fun updateCurrencySymbol(symbol: String) {
        viewModelScope.launch { repository.setCurrencySymbol(symbol) }
    }

    fun updateBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricEnabled(enabled) }
    }

    fun updateDailyReminderEnabled(context: Context, enabled: Boolean) {
        viewModelScope.launch {
            repository.setDailyReminderEnabled(enabled)
            if (enabled) {
                ReminderScheduler.scheduleDailyReminder(context)
            } else {
                ReminderScheduler.cancelDailyReminder(context)
            }
        }
    }

    suspend fun getExportSnapshot(): List<TransactionWithCategory> =
        repository.getAllTransactionsSnapshot()

    class Factory(private val repository: CaudalRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CaudalViewModel(repository) as T
        }
    }
}