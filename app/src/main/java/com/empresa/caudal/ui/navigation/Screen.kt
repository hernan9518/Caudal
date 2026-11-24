package com.empresa.caudal.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Activity : Screen("activity")
    data object Summary : Screen("summary")
    data object Budget : Screen("budget")
    data object Analytics : Screen("analytics")
    data object Categories : Screen("categories")
    data object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Activity, "Actividad", Icons.Rounded.ReceiptLong),
    BottomNavItem(Screen.Summary, "Resumen", Icons.Rounded.PieChart),
    BottomNavItem(Screen.Budget, "Presupuesto", Icons.Rounded.AccountBalanceWallet),
    BottomNavItem(Screen.Analytics, "Análisis", Icons.Rounded.BarChart)
)