package com.empresa.caudal.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.empresa.caudal.domain.model.TransactionType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorHex: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val monthlyBudget: Double = 0.0,
    val isDefault: Boolean = false
)