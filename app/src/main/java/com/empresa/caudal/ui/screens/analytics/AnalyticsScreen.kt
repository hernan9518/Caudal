package com.empresa.caudal.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.ui.screens.activity.TransactionRowCard
import com.empresa.caudal.ui.theme.ExpenseRed
import com.empresa.caudal.ui.theme.IncomeGreen
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    allTransactions: List<TransactionWithCategory>,
    monthlyTransactions: List<TransactionWithCategory>,
    selectedMonth: YearMonth,
    currencySymbol: String
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            MonthlyComparisonChart(
                allTransactions = allTransactions,
                selectedMonth = selectedMonth,
                currencySymbol = currencySymbol
            )
        }

        item {
            DailyExpenseCalendar(
                monthlyTransactions = monthlyTransactions,
                selectedMonth = selectedMonth,
                onDayClick = { date -> selectedDate = date }
            )
        }
    }

    selectedDate?.let { date ->
        val dayTransactions = remember(monthlyTransactions, date) {
            monthlyTransactions.filter { DateUtils.toLocalDate(it.transaction.dateMillis) == date }
        }
        val dayExpense = dayTransactions
            .filter { it.transaction.type == TransactionType.EXPENSE }
            .sumOf { it.transaction.amount }
        val dayIncome = dayTransactions
            .filter { it.transaction.type == TransactionType.INCOME }
            .sumOf { it.transaction.amount }

        val formattedTitle = remember(date) {
            val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es-CO"))
            date.format(formatter).replaceFirstChar { it.uppercase() }
        }

        ModalBottomSheet(
            onDismissRequest = { selectedDate = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = formattedTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gastos: ${CurrencyFormatter.format(dayExpense, currencySymbol)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (dayExpense > 0.0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (dayIncome > 0.0) {
                        Text(
                            text = "Ingresos: ${CurrencyFormatter.format(dayIncome, currencySymbol)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = IncomeGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (dayTransactions.isEmpty()) {
                    Text(
                        text = "No se registraron movimientos en esta fecha.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dayTransactions, key = { it.transaction.id }) { item ->
                            TransactionRowCard(
                                item = item,
                                currencySymbol = currencySymbol,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}