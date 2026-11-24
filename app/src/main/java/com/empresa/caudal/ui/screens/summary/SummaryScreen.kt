package com.empresa.caudal.ui.screens.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.ui.components.DonutChart
import com.empresa.caudal.ui.components.DonutSlice
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.IconMapper
import kotlin.math.roundToInt

private data class CategoryBreakdown(
    val category: CategoryEntity,
    val totalAmount: Double,
    val count: Int,
    val percentage: Int
)

@Composable
fun SummaryScreen(
    transactions: List<TransactionWithCategory>,
    currencySymbol: String
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }

    val filtered = remember(transactions, selectedType) {
        transactions.filter { it.transaction.type == selectedType }
    }

    val totalAmount = remember(filtered) {
        filtered.sumOf { it.transaction.amount }
    }

    val breakdowns = remember(filtered, totalAmount) {
        filtered
            .groupBy { it.category }
            .map { (category, items) ->
                val sum = items.sumOf { it.transaction.amount }
                val pct = if (totalAmount > 0.0) ((sum / totalAmount) * 100.0).roundToInt() else 0
                CategoryBreakdown(
                    category = category,
                    totalAmount = sum,
                    count = items.size,
                    percentage = pct
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    val slices = remember(breakdowns) {
        breakdowns.map {
            DonutSlice(
                label = it.category.name,
                amount = it.totalAmount,
                color = Color(it.category.colorHex)
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            DonutChart(
                slices = slices,
                centerSubtitle = if (selectedType == TransactionType.EXPENSE) "Gastos del mes" else "Ingresos del mes",
                centerTitle = CurrencyFormatter.format(totalAmount, currencySymbol)
            )
            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("Gastos") },
                    shape = CircleShape
                )
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("Ingresos") },
                    shape = CircleShape
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (breakdowns.isEmpty()) {
            item {
                Text(
                    text = if (selectedType == TransactionType.EXPENSE) {
                        "No hay gastos registrados en este mes"
                    } else {
                        "No hay ingresos registrados en este mes"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }
        } else {
            items(breakdowns, key = { it.category.id }) { item ->
                SummaryCategoryRow(
                    breakdown = item,
                    currencySymbol = currencySymbol
                )
            }
        }
    }
}

@Composable
private fun SummaryCategoryRow(
    breakdown: CategoryBreakdown,
    currencySymbol: String
) {
    val catColor = Color(breakdown.category.colorHex)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.16f))
                    .border(2.dp, catColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(breakdown.category.iconKey),
                    contentDescription = breakdown.category.name,
                    tint = catColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = breakdown.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = breakdown.count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = CurrencyFormatter.format(breakdown.totalAmount, currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${breakdown.percentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}