package com.empresa.caudal.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.ui.CaudalViewModel
import com.empresa.caudal.util.DateUtils

@Composable
fun CaudalScaffold(viewModel: CaudalViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    var showTransactionSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionWithCategory?>(null) }

    val showMainBars = currentRoute in bottomNavItems.map { it.screen.route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showMainBars) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilledTonalIconButton(
                        onClick = { navController.navigate(Screen.Settings.route) },
                        shape = CircleShape
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Ajustes")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.selectPreviousMonth() }) {
                            Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior")
                        }
                        Text(
                            text = DateUtils.formatMonthYear(selectedMonth),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { viewModel.selectNextMonth() }) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Mes siguiente")
                        }
                    }

                    FilledTonalIconButton(
                        onClick = { navController.navigate(Screen.Categories.route) },
                        shape = CircleShape
                    ) {
                        Icon(Icons.Rounded.Category, contentDescription = "Categorías")
                    }
                }
            }
        },
        bottomBar = {
            if (showMainBars) {
                CaudalBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onAddClick = {
                        editingTransaction = null
                        showTransactionSheet = true
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Activity.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Activity.route) {
                PlaceholderScreen("Actividad (Próximo paso)")
            }
            composable(Screen.Summary.route) {
                PlaceholderScreen("Resumen (Mañana)")
            }
            composable(Screen.Budget.route) {
                PlaceholderScreen("Presupuesto (Mañana)")
            }
            composable(Screen.Analytics.route) {
                PlaceholderScreen("Análisis (Mañana)")
            }
            composable(Screen.Categories.route) {
                PlaceholderScreen("Categorías (Commit #7)")
            }
            composable(Screen.Settings.route) {
                PlaceholderScreen("Ajustes (Mañana)")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
    }
}