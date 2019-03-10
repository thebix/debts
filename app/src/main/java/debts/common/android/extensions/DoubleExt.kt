package debts.common.android.extensions

fun Double.toDecimal(): Double {
    return Math.round(this * 100.0) / 100.0
}
