package com.empresa.caudal.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.empresa.caudal.data.local.entity.TransactionWithCategory
import com.empresa.caudal.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val dateFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    private fun buildCsvContent(transactions: List<TransactionWithCategory>): String {
        val sb = StringBuilder()
        sb.append("\uFEFF") // BOM UTF-8 para Excel
        sb.append("ID,Fecha,Tipo,Categoria,Monto,Nota\n")
        transactions.forEach { item ->
            val tx = item.transaction
            val dateStr = dateFormatter.format(Instant.ofEpochMilli(tx.dateMillis))
            val typeStr = if (tx.type == TransactionType.INCOME) "Ingreso" else "Gasto"
            val categoryName = escapeCsv(item.category.name)
            val noteText = escapeCsv(tx.note)
            sb.append("${tx.id},$dateStr,$typeStr,$categoryName,${tx.amount},$noteText\n")
        }
        return sb.toString()
    }

    suspend fun writeTransactionsToUri(
        context: Context,
        uri: Uri,
        transactions: List<TransactionWithCategory>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(uri, "wt")
                ?: return@withContext false

            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(buildCsvContent(transactions))
                writer.flush()
            }
            true
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        }
    }

    suspend fun exportToCacheAndShare(
        context: Context,
        transactions: List<TransactionWithCategory>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val csvFile = File(exportDir, "caudal_movimientos.csv")
            csvFile.writeText(buildCsvContent(transactions), Charsets.UTF_8)

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Historial de movimientos - Caudal")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Exportar historial CSV").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
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