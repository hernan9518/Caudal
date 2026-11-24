package com.empresa.caudal.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.ui.theme.CaudalBlue
import com.empresa.caudal.ui.theme.IncomeGreen
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.DateUtils
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private data class MonthBarPoint(
    val yearMonth: YearMonth,
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
fun MonthlyComparisonChart(
    allTransactions: List<TransactionWithCategory>,
    selectedMonth: YearMonth,
    currencySymbol: String
) {
    val spanishLocale = remember { Locale.forLanguageTag("es-CO") }

    val points = remember(allTransactions, selectedMonth) {
        (5 downTo 0).map { offset ->
            val month = selectedMonth.minusMonths(offset.toLong())
            val (start, end) = DateUtils.getMonthBounds(month)
            val inMonth = allTransactions.filter { it.transaction.dateMillis in start..end }

            val income = inMonth.filter { it.transaction.type == TransactionType.INCOME }
                .sumOf { it.transaction.amount }
            val expense = inMonth.filter { it.transaction.type == TransactionType.EXPENSE }
                .sumOf { it.transaction.amount }

            val rawLabel = month.month.getDisplayName(TextStyle.SHORT, spanishLocale)
                .replace(".", "")
                .replaceFirstChar { it.uppercase() }

            MonthBarPoint(
                yearMonth = month,
                label = rawLabel,
                income = income,
                expense = expense
            )
        }
    }

    val currentPoint = points.last()
    val maxValue = remember(points) {
        points.maxOfOrNull { maxOf(it.income, it.expense) }?.coerceAtLeast(1.0) ?: 1.0
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                LegendMetric(
                    dotColor = IncomeGreen,
                    title = "Ingresos",
                    amount = CurrencyFormatter.format(currentPoint.income, currencySymbol)
                )
                LegendMetric(
                    dotColor = CaudalBlue,
                    title = "Gastos",
                    amount = CurrencyFormatter.format(currentPoint.expense, currencySymbol)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                points.forEach { point ->
                    val isSelected = point.yearMonth == selectedMonth
                    val incomeFraction = (point.income / maxValue).toFloat().coerceIn(0.03f, 1f)
                    val expenseFraction = (point.expense / maxValue).toFloat().coerceIn(0.03f, 1f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.height(132.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight(incomeFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(IncomeGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .width(10.dp)
                                    .fillMaxHeight(expenseFraction)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    .background(CaudalBlue)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                        ) {
                            Text(
                                text = point.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendMetric(
    dotColor: Color,
    title: String,
    amount: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = amount,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}