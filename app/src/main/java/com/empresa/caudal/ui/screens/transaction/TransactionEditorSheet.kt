package com.empresa.caudal.ui.screens.transaction

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.empresa.caudal.data.local.entity.CategoryEntity
import com.empresa.caudal.data.local.entity.TransactionEntity
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import com.empresa.caudal.ui.components.NumericKeypad
import com.empresa.caudal.util.CurrencyFormatter
import com.empresa.caudal.util.DateUtils
import com.empresa.caudal.util.IconMapper
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorSheet(
    categories: List<CategoryEntity>,
    currencySymbol: String,
    existingTransaction: TransactionWithCategory? = null,
    onDismiss: () -> Unit,
    onSave: (id: Long, amount: Double, note: String, dateMillis: Long, type: TransactionType, categoryId: Long) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null,
    onManageCategoriesClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedType by remember {
        mutableStateOf(existingTransaction?.transaction?.type ?: TransactionType.EXPENSE)
    }
    var rawAmount by remember {
        mutableStateOf(
            existingTransaction?.transaction?.amount?.let { amount ->
                if (amount % 1.0 == 0.0) amount.toLong().toString() else amount.toString()
            } ?: ""
        )
    }
    var note by remember { mutableStateOf(existingTransaction?.transaction?.note ?: "") }
    var dateMillis by remember {
        mutableLongStateOf(existingTransaction?.transaction?.dateMillis ?: DateUtils.todayMillis())
    }
    var selectedCategoryId by remember {
        mutableLongStateOf(existingTransaction?.category?.id ?: 0L)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val filteredCategories = remember(categories, selectedType) {
        categories.filter { it.type == selectedType }
    }

    LaunchedEffect(filteredCategories) {
        if (filteredCategories.none { it.id == selectedCategoryId }) {
            selectedCategoryId = filteredCategories.firstOrNull()?.id ?: 0L
        }
    }

    val numericValue = rawAmount.replace(",", ".").toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera: Selector Gasto / Ingreso + botón eliminar si es edición
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    SegmentedButton(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Gasto")
                    }
                    SegmentedButton(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Ingreso")
                    }
                }

                if (existingTransaction != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(existingTransaction.transaction)
                            onDismiss()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Eliminar movimiento",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display gigante del monto + Backspace
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(48.dp))
                Text(
                    text = if (rawAmount.isEmpty()) "${currencySymbol}0"
                    else CurrencyFormatter.format(numericValue, currencySymbol),
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(
                    onClick = {
                        if (rawAmount.isNotEmpty()) {
                            rawAmount = rawAmount.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Borrar dígito",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Chips rápidos de Fecha y Nota (estilo Quanto)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = { showDatePicker = true },
                    label = { Text(DateUtils.formatRelativeDate(dateMillis)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = CircleShape
                )

                FilterChip(
                    selected = note.isNotBlank(),
                    onClick = { showNoteDialog = true },
                    label = {
                        Text(
                            text = note.ifBlank { "Nota" },
                            maxLines = 1
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = CircleShape
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Carrusel horizontal de categorías + acceso directo a gestionar categorías
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { category ->
                        val isSelected = category.id == selectedCategoryId
                        val catColor = Color(category.colorHex)

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = IconMapper.getIcon(category.iconKey),
                                    contentDescription = null,
                                    tint = if (isSelected) catColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = catColor.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = catColor
                            )
                        )
                    }
                }

                Surface(
                    onClick = {
                        onDismiss()
                        onManageCategoriesClick()
                    },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Nueva categoría",
                        modifier = Modifier.padding(10.dp).size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Teclado numérico personalizado
            NumericKeypad(
                onDigitClick = { digit ->
                    if (rawAmount.length < 11) {
                        rawAmount = if (rawAmount == "0") digit else rawAmount + digit
                    }
                },
                onDecimalClick = {
                    if (!rawAmount.contains(".")) {
                        rawAmount = if (rawAmount.isEmpty()) "0." else "$rawAmount."
                    }
                },
                onConfirmClick = {
                    if (numericValue > 0.0 && selectedCategoryId != 0L) {
                        onSave(
                            existingTransaction?.transaction?.id ?: 0L,
                            numericValue,
                            note,
                            dateMillis,
                            selectedType,
                            selectedCategoryId
                        )
                        onDismiss()
                    }
                },
                isConfirmEnabled = numericValue > 0.0 && selectedCategoryId != 0L
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            val localDate = Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
                            dateMillis = localDate.atStartOfDay(java.time.ZoneId.systemDefault())
                                .toInstant().toEpochMilli()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showNoteDialog) {
        var tempNote by remember { mutableStateOf(note) }
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Nota del movimiento") },
            text = {
                OutlinedTextField(
                    value = tempNote,
                    onValueChange = { tempNote = it },
                    placeholder = { Text("Ej. Almuerzo, Bus, Arriendo...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        note = tempNote
                        showNoteDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}