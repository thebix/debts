package debts.common.android.extensions

import java.text.NumberFormat
import java.util.*

fun Double.toDecimal(): Double {
    return Math.round(this * 100.0) / 100.0
}

fun Double.toFormattedCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return format.format(this).replace(format.currency.symbol, "", true)
}
