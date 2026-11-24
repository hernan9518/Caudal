package com.empresa.caudal.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.empresa.caudal.data.local.dao.CategoryDao
import com.empresa.caudal.data.local.dao.TransactionDao
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionEntity
import com.empresa.caudal.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, TransactionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CaudalDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: CaudalDatabase? = null

        fun getInstance(context: Context): CaudalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaudalDatabase::class.java,
                    "caudal_database"
                )
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedDatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }
        }

        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaults = listOf(
                CategoryEntity(name = "Comida", iconKey = "food", colorHex = 0xFF38BDF8, type = TransactionType.EXPENSE, isDefault = true),
                CategoryEntity(name = "Transporte", iconKey = "transport", colorHex = 0xFFF97316, type = TransactionType.EXPENSE, isDefault = true),
                CategoryEntity(name = "Servicios", iconKey = "utilities", colorHex = 0xFFA855F7, type = TransactionType.EXPENSE, isDefault = true),
                CategoryEntity(name = "Ocio", iconKey = "leisure", colorHex = 0xFFEC4899, type = TransactionType.EXPENSE, isDefault = true),
                CategoryEntity(name = "Salud", iconKey = "health", colorHex = 0xFF22C55E, type = TransactionType.EXPENSE, isDefault = true),
                CategoryEntity(name = "Salario", iconKey = "salary", colorHex = 0xFF14B8A6, type = TransactionType.INCOME, isDefault = true),
                CategoryEntity(name = "Otros ingresos", iconKey = "wallet", colorHex = 0xFF6366F1, type = TransactionType.INCOME, isDefault = true)
            )
            categoryDao.insertAll(defaults)
        }
    }
}