package com.empresa.caudal.ui.screens.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.IconMapper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    currencySymbol: String,
    onNavigateBack: () -> Unit,
    onSaveCategory: (id: Long, name: String, iconKey: String, colorHex: Long, type: TransactionType, budget: Double, isDefault: Boolean) -> Unit,
    onDeleteCategory: (CategoryEntity, onResult: (Boolean) -> Unit) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showEditor by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    val expenseCategories = remember(categories) {
        categories.filter { it.type == TransactionType.EXPENSE }
    }
    val incomeCategories = remember(categories) {
        categories.filter { it.type == TransactionType.INCOME }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Categorías", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    showEditor = true
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Crear categoría")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Gastos",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(expenseCategories, key = { it.id }) { category ->
                CategoryRowCard(
                    category = category,
                    currencySymbol = currencySymbol,
                    onClick = {
                        editingCategory = category
                        showEditor = true
                    },
                    onDeleteClick = {
                        onDeleteCategory(category) { deleted ->
                            if (!deleted) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No se puede eliminar: tiene movimientos registrados")
                                }
                            }
                        }
                    }
                )
            }

            item {
                Text(
                    text = "Ingresos",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                )
            }

            items(incomeCategories, key = { it.id }) { category ->
                CategoryRowCard(
                    category = category,
                    currencySymbol = currencySymbol,
                    onClick = {
                        editingCategory = category
                        showEditor = true
                    },
                    onDeleteClick = {
                        onDeleteCategory(category) { deleted ->
                            if (!deleted) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No se puede eliminar: tiene movimientos registrados")
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (showEditor) {
        CategoryEditorDialog(
            existingCategory = editingCategory,
            onDismiss = {
                showEditor = false
                editingCategory = null
            },
            onSave = onSaveCategory
        )
    }
}

@Composable
private fun CategoryRowCard(
    category: CategoryEntity,
    currencySymbol: String,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val catColor = Color(category.colorHex)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp)
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
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(catColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(category.iconKey),
                    contentDescription = null,
                    tint = catColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (category.type == TransactionType.EXPENSE && category.monthlyBudget > 0.0) {
                    Text(
                        text = "Tope mensual: ${CurrencyFormatter.format(category.monthlyBudget, currencySymbol)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!category.isDefault) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = "Eliminar categoría",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}