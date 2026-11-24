package com.empresa.caudal.util

import android.content.Context
import android.net.Uri
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    fun writeTransactionsToUri(
        context: Context,
        uri: Uri,
        transactions: List<TransactionWithCategory>
    ): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
                writer.write("ID,Fecha,Tipo,Categoria,Monto,Nota\n")
                transactions.forEach { item ->
                    val tx = item.transaction
                    val dateStr = dateFormatter.format(Instant.ofEpochMilli(tx.dateMillis))
                    val typeStr = if (tx.type == TransactionType.INCOME) "Ingreso" else "Gasto"
                    val categoryName = escapeCsv(item.category.name)
                    val noteText = escapeCsv(tx.note)
                    writer.write("${tx.id},$dateStr,$typeStr,$categoryName,${tx.amount},$noteText\n")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun escapeCsv(value: String): String {
        val sanitized = value.replace("\"", "\"\"")
        return if (sanitized.contains(",") || sanitized.contains("\n") || sanitized.contains("\"")) {
            "\"$sanitized\""
        } else {
            sanitized
        }
    }
}