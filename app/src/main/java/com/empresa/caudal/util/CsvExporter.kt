package com.empresa.caudal.util

import android.content.Context
import android.net.Uri
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    suspend fun writeTransactionsToUri(
        context: Context,
        uri: Uri,
        transactions: List<TransactionWithCategory>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                ?: return@withContext false

            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF") // BOM UTF-8 para que Excel abra tildes y ñ correctamente
                writer.write("ID,Fecha,Tipo,Categoria,Monto,Nota\n")
                transactions.forEach { item ->
                    val tx = item.transaction
                    val dateStr = dateFormatter.format(Instant.ofEpochMilli(tx.dateMillis))
                    val typeStr = if (tx.type == TransactionType.INCOME) "Ingreso" else "Gasto"
                    val categoryName = escapeCsv(item.category.name)
                    val noteText = escapeCsv(tx.note)
                    writer.write("${tx.id},$dateStr,$typeStr,$categoryName,${tx.amount},$noteText\n")
                }
                writer.flush()
            }
            true
        } catch (t: Throwable) {
            t.printStackTrace()
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