package com.empresa.caudal.ui.screens.activity

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.ui.theme.ExpenseRed
import com.empresa.caudal.ui.theme.IncomeGreen
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.DateUtils

private enum class TypeFilter(val label: String) {
    ALL("Todos"),
    EXPENSES("Gastos"),
    INCOMES("Ingresos")
}

@Composable
fun ActivityScreen(
    transactions: List<TransactionWithCategory>,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    onTransactionClick: (TransactionWithCategory) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchOpen by remember { mutableStateOf(false) }
    var typeFilter by remember { mutableStateOf(TypeFilter.ALL) }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }

    val totalIncome = remember(transactions) {
        transactions.filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }
    }
    val totalExpense = remember(transactions) {
        transactions.filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
    }
    val netBalance = totalIncome - totalExpense

    val filteredTransactions = remember(transactions, searchQuery, typeFilter, selectedCategoryId) {
        transactions.filter { item ->
            val matchesType = when (typeFilter) {
                TypeFilter.ALL -> true
                TypeFilter.EXPENSES -> item.transaction.type == TransactionType.EXPENSE
                TypeFilter.INCOMES -> item.transaction.type == TransactionType.INCOME
            }
            val matchesCategory = selectedCategoryId == 0L || item.category.id == selectedCategoryId
            val matchesQuery = searchQuery.isBlank() ||
                    item.transaction.note.contains(searchQuery, ignoreCase = true) ||
                    item.category.name.contains(searchQuery, ignoreCase = true)

            matchesType && matchesCategory && matchesQuery
        }
    }

    val groupedByDay = remember(filteredTransactions) {
        filteredTransactions.groupBy { DateUtils.toLocalDate(it.transaction.dateMillis) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Cabecera de Saldo del Mes (estilo Quanto)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Balance del mes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.format(netBalance, currencySymbol),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = if (netBalance < 0) ExpenseRed else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Ingresos: ${CurrencyFormatter.format(totalIncome, currencySymbol)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = IncomeGreen
                )
                Text(
                    text = "Gastos: ${CurrencyFormatter.format(totalExpense, currencySymbol)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Barra horizontal de búsqueda y filtros rápidos
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item {
                Surface(
                    onClick = {
                        isSearchOpen = !isSearchOpen
                        if (!isSearchOpen) searchQuery = ""
                    },
                    shape = CircleShape,
                    color = if (isSearchOpen || searchQuery.isNotBlank()) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ) {
                    Icon(
                        imageVector = if (isSearchOpen) Icons.Rounded.Close else Icons.Rounded.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.padding(10.dp).size(18.dp)
                    )
                }
            }

            items(TypeFilter.entries) { filter ->
                FilterChip(
                    selected = typeFilter == filter,
                    onClick = { typeFilter = filter },
                    label = { Text(filter.label) },
                    shape = CircleShape
                )
            }

            items(categories, key = { it.id }) { category ->
                val isSelected = selectedCategoryId == category.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedCategoryId = if (isSelected) 0L else category.id
                    },
                    label = { Text(category.name) },
                    shape = CircleShape
                )
            }
        }

        AnimatedVisibility(visible = isSearchOpen) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por nota o categoría...") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin movimientos en este periodo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Toca el botón + para registrar tu primer gasto o ingreso",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedByDay.forEach { (_, itemsForDay) ->
                    val firstMillis = itemsForDay.first().transaction.dateMillis
                    val dayExpenses = itemsForDay
                        .filter { it.transaction.type == TransactionType.EXPENSE }
                        .sumOf { it.transaction.amount }

                    item(key = "header_$firstMillis") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = DateUtils.formatRelativeDate(firstMillis),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (dayExpenses > 0.0) {
                                Text(
                                    text = CurrencyFormatter.format(dayExpenses, currencySymbol),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(itemsForDay, key = { it.transaction.id }) { item ->
                        TransactionRowCard(
                            item = item,
                            currencySymbol = currencySymbol,
                            onClick = { onTransactionClick(item) }
                        )
                    }
                }
            }
        }
    }
}