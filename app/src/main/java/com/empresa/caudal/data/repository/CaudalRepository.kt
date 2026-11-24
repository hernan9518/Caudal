package com.empresa.caudal.data.repository

import com.empresa.caudal.data.local.UserPreferences
import com.empresa.caudal.data.local.UserSettings
import com.empresa.caudal.data.local.dao.CategoryDao
import com.empresa.caudal.data.local.dao.TransactionDao
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionEntity
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

class CaudalRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val userPreferences: UserPreferences
) {
    val userSettings: Flow<UserSettings> = userPreferences.settingsFlow

    fun getAllTransactions(): Flow<List<TransactionWithCategory>> =
        transactionDao.getAllTransactionsWithCategory()

    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionWithCategory>> =
        transactionDao.getTransactionsBetween(startMillis, endMillis)

    suspend fun getAllTransactionsSnapshot(): List<TransactionWithCategory> =
        transactionDao.getAllTransactionsSnapshot()

    suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    fun getCategoriesByType(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByType(type)

    suspend fun insertCategory(category: CategoryEntity): Long =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategorySafely(category: CategoryEntity): Boolean {
        val count = categoryDao.countTransactionsForCategory(category.id)
        if (count > 0) return false
        categoryDao.deleteCategory(category)
        return true
    }

    suspend fun setMonthlyBudget(amount: Double) =
        userPreferences.updateMonthlyBudget(amount)

    suspend fun setDarkTheme(enabled: Boolean) =
        userPreferences.updateDarkTheme(enabled)

    suspend fun setCurrencySymbol(symbol: String) =
        userPreferences.updateCurrencySymbol(symbol)

    suspend fun setBiometricEnabled(enabled: Boolean) =
        userPreferences.updateBiometricEnabled(enabled)

    suspend fun setDailyReminderEnabled(enabled: Boolean) =
        userPreferences.updateDailyReminderEnabled(enabled)
}