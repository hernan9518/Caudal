package com.empresa.caudal.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object CurrencyFormatter {

    fun format(amount: Double, currencySymbol: String = "$", showSign: Boolean = false): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("es-CO")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,##0", symbols)
        val formattedNumber = formatter.format(abs(amount))

        return when {
            amount < 0 -> "-$currencySymbol$formattedNumber"
            showSign && amount > 0 -> "+$currencySymbol$formattedNumber"
            else -> "$currencySymbol$formattedNumber"
        }
    }

    fun formatCompact(amount: Double): String {
        val absolute = abs(amount)
        return when {
            absolute >= 1_000_000 -> String.format(Locale.US, "%.1fM", amount / 1_000_000)
            absolute >= 1_000 -> String.format(Locale.US, "%.0fk", amount / 1_000)
            else -> String.format(Locale.US, "%.0f", amount)
        }
    }
}