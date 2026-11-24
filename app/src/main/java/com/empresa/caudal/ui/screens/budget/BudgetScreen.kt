package com.empresa.caudal.ui.screens.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.ui.components.BudgetRing
import com.empresa.caudal.ui.theme.ExpenseRed
import com.empresa.caudal.ui.theme.WarningAmber
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.IconMapper

@Composable
fun BudgetScreen(
    monthlyBudget: Double,
    categories: List<CategoryEntity>,
    transactions: List<TransactionWithCategory>,
    currencySymbol: String,
    onUpdateGlobalBudget: (Double) -> Unit,
    onUpdateCategoryBudget: (CategoryEntity, Double) -> Unit,
    onManageCategoriesClick: () -> Unit
) {
    var showGlobalBudgetDialog by remember { mutableStateOf(false) }
    var editingCategoryForBudget by remember { mutableStateOf<CategoryEntity?>(null) }

    val expenseTransactions = remember(transactions) {
        transactions.filter { it.transaction.type == TransactionType.EXPENSE }
    }

    val totalSpent = remember(expenseTransactions) {
        expenseTransactions.sumOf { it.transaction.amount }
    }

    val spentByCategory = remember(expenseTransactions) {
        expenseTransactions.groupBy { it.category.id }
            .mapValues { (_, list) -> list.sumOf { it.transaction.amount } }
    }

    val expenseCategories = remember(categories) {
        categories.filter { it.type == TransactionType.EXPENSE }
    }

    val budgetedCategories = remember(expenseCategories) {
        expenseCategories.filter { it.monthlyBudget > 0.0 }
    }

    if (monthlyBudget <= 0.0 && budgetedCategories.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetRing(
                progress = 0.72f,
                ringColor = MaterialTheme.colorScheme.primary,
                ringSize = 160.dp,
                strokeWidth = 16.dp
            ) {
                Text(
                    text = "0%",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Presupuesto",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Establece límites de cuánto quieres gastar al mes de forma general o por categoría",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { showGlobalBudgetDialog = true },
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Crear presupuesto mensual", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onManageCategoriesClick,
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Asignar topes por categoría")
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(2) }) {
                GlobalBudgetCard(
                    monthlyBudget = monthlyBudget,
                    totalSpent = totalSpent,
                    currencySymbol = currencySymbol,
                    onEditClick = { showGlobalBudgetDialog = true }
                )
            }

            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Por categoría",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onManageCategoriesClick) {
                        Text("Configurar")
                    }
                }
            }

            items(expenseCategories, key = { it.id }) { category ->
                val spent = spentByCategory[category.id] ?: 0.0
                CategoryBudgetCard(
                    category = category,
                    spent = spent,
                    currencySymbol = currencySymbol,
                    onClick = { editingCategoryForBudget = category }
                )
            }
        }
    }

    if (showGlobalBudgetDialog) {
        BudgetAmountDialog(
            title = "Presupuesto mensual global",
            initialAmount = monthlyBudget,
            onDismiss = { showGlobalBudgetDialog = false },
            onConfirm = { newAmount ->
                onUpdateGlobalBudget(newAmount)
                showGlobalBudgetDialog = false
            }
        )
    }

    editingCategoryForBudget?.let { category ->
        BudgetAmountDialog(
            title = "Tope para ${category.name}",
            initialAmount = category.monthlyBudget,
            onDismiss = { editingCategoryForBudget = null },
            onConfirm = { newAmount ->
                onUpdateCategoryBudget(category, newAmount)
                editingCategoryForBudget = null
            }
        )
    }
}

@Composable
private fun GlobalBudgetCard(
    monthlyBudget: Double,
    totalSpent: Double,
    currencySymbol: String,
    onEditClick: () -> Unit
) {
    val hasBudget = monthlyBudget > 0.0
    val remaining = (monthlyBudget - totalSpent).coerceAtLeast(0.0)
    val ratio = if (hasBudget) (totalSpent / monthlyBudget).toFloat() else 0f
    val ringColor = when {
        !hasBudget -> MaterialTheme.colorScheme.primary
        ratio >= 1f -> ExpenseRed
        ratio >= 0.8f -> WarningAmber
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Límite general del mes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalIconButton(
                    onClick = onEditClick,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Editar presupuesto",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BudgetRing(
                progress = if (hasBudget) (1f - ratio).coerceIn(0f, 1f) else 0f,
                ringColor = ringColor,
                ringSize = 200.dp,
                strokeWidth = 20.dp
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hasBudget) {
                            if (totalSpent > monthlyBudget) "Excedido" else "Disponible este mes"
                        } else {
                            "Sin límite global"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (hasBudget) {
                            if (totalSpent > monthlyBudget) {
                                CurrencyFormatter.format(totalSpent - monthlyBudget, currencySymbol)
                            } else {
                                CurrencyFormatter.format(remaining, currencySymbol)
                            }
                        } else {
                            CurrencyFormatter.format(totalSpent, currencySymbol)
                        },
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasBudget && totalSpent > monthlyBudget) ExpenseRed
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (hasBudget) {
                        Text(
                            text = "${CurrencyFormatter.format(totalSpent, currencySymbol)} / ${CurrencyFormatter.format(monthlyBudget, currencySymbol)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ringColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    category: CategoryEntity,
    spent: Double,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val catColor = Color(category.colorHex)
    val hasLimit = category.monthlyBudget > 0.0
    val remaining = (category.monthlyBudget - spent).coerceAtLeast(0.0)
    val ratio = if (hasLimit) (spent / category.monthlyBudget).toFloat() else 0f
    val ringColor = if (hasLimit && ratio >= 1f) ExpenseRed else catColor

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetRing(
                progress = if (hasLimit) (1f - ratio).coerceIn(0f, 1f) else 0f,
                ringColor = ringColor,
                ringSize = 62.dp,
                strokeWidth = 7.dp
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(category.iconKey),
                    contentDescription = category.name,
                    tint = catColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (hasLimit) {
                    if (spent > category.monthlyBudget) {
                        "-${CurrencyFormatter.format(spent - category.monthlyBudget, currencySymbol)}"
                    } else {
                        CurrencyFormatter.format(remaining, currencySymbol)
                    }
                } else {
                    CurrencyFormatter.format(spent, currencySymbol)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (hasLimit && spent > category.monthlyBudget) ExpenseRed
                else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = if (hasLimit) "${category.name} libre" else "${category.name} (sin tope)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BudgetAmountDialog(
    title: String,
    initialAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var input by remember {
        mutableStateOf(if (initialAmount > 0.0) initialAmount.toLong().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { raw -> input = raw.filter { it.isDigit() } },
                label = { Text("Monto mensual (0 para quitar)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = input.toDoubleOrNull() ?: 0.0
                    onConfirm(value)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}