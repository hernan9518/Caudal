package com.empresa.caudal.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun DailyExpenseCalendar(
    monthlyTransactions: List<TransactionWithCategory>,
    selectedMonth: YearMonth,
    onDayClick: (LocalDate) -> Unit
) {
    val weekHeaders = listOf("L", "M", "M", "J", "V", "S", "D")

    val dailyExpenseMap = remember(monthlyTransactions) {
        monthlyTransactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .groupBy { DateUtils.toLocalDate(it.transaction.dateMillis).dayOfMonth }
            .mapValues { (_, list) -> list.sumOf { it.transaction.amount } }
    }

    val dailyIncomeMap = remember(monthlyTransactions) {
        monthlyTransactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .groupBy { DateUtils.toLocalDate(it.transaction.dateMillis).dayOfMonth }
            .mapValues { (_, list) -> list.sumOf { it.transaction.amount } }
    }

    val calendarCells = remember(selectedMonth) {
        val firstDayOfWeek = selectedMonth.atDay(1).dayOfWeek.value
        val leadingEmpty = firstDayOfWeek - 1
        val daysInMonth = selectedMonth.lengthOfMonth()

        val cells = MutableList<Int?>(leadingEmpty) { null }
        for (day in 1..daysInMonth) {
            cells.add(day)
        }
        while (cells.size % 7 != 0) {
            cells.add(null)
        }
        cells.chunked(7)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendario de gastos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Toca un día",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekHeaders.forEach { dayLabel ->
                    Text(
                        text = dayLabel,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                calendarCells.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        week.forEach { day ->
                            if (day == null) {
                                Box(modifier = Modifier.weight(1f).height(48.dp))
                            } else {
                                val spent = dailyExpenseMap[day] ?: 0.0
                                val hasExpense = spent > 0.0
                                val hasAnyActivity = hasExpense || (dailyIncomeMap[day] ?: 0.0) > 0.0

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (hasExpense) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                        )
                                        .then(
                                            if (hasAnyActivity) Modifier.border(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(10.dp)
                                            ) else Modifier
                                        )
                                        .clickable { onDayClick(selectedMonth.atDay(day)) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (hasAnyActivity) FontWeight.Bold else FontWeight.Normal,
                                            color = if (hasExpense) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = if (hasExpense) CurrencyFormatter.formatCompact(spent) else "0",
                                            fontSize = 10.sp,
                                            fontWeight = if (hasExpense) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (hasExpense) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}