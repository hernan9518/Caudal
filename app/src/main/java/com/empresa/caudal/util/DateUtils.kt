package com.empresa.caudal.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val spanishLocale: Locale = Locale.forLanguageTag("es-CO")

    fun todayMillis(): Long = System.currentTimeMillis()

    fun getMonthBounds(yearMonth: YearMonth = YearMonth.now()): Pair<Long, Long> {
        val start = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999_000_000)
            .atZone(zoneId).toInstant().toEpochMilli()
        return start to end
    }

    fun formatRelativeDate(epochMillis: Long): String {
        val date = Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
        val today = LocalDate.now(zoneId)

        return when (date) {
            today -> "Hoy"
            today.minusDays(1) -> "Ayer"
            else -> date.format(DateTimeFormatter.ofPattern("d MMM", spanishLocale))
        }
    }

    fun formatMonthYear(yearMonth: YearMonth): String {
        val raw = yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", spanishLocale))
        return raw.replaceFirstChar { it.uppercase() }
    }

    fun toLocalDate(epochMillis: Long): LocalDate =
        Instant.ofEpochMilli(epochMillis).atZone(zoneId).toLocalDate()
}