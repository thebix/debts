package debts.core.common.android.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Double.toDecimal(): Double {
    @Suppress("MagicNumber")
    return Math.round(this * 100.0) / 100.0
}

fun Double.toFormattedCurrency(): String {
    val decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault())
    decimalFormatSymbols.groupingSeparator = ' '
    val formatPattern = when {
        this.isFinite() && this == Math.floor(this) -> "###,###,###"
        else -> "###,###,###.00"
    }

    val decimalFormat = DecimalFormat(formatPattern, decimalFormatSymbols)
    return decimalFormat.format(this)
}
