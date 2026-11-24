package com.empresa.caudal.ui.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import java.time.YearMonth

@Composable
fun AnalyticsScreen(
    allTransactions: List<TransactionWithCategory>,
    monthlyTransactions: List<TransactionWithCategory>,
    selectedMonth: YearMonth,
    currencySymbol: String
) {
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
                selectedMonth = selectedMonth
            )
        }
    }
}