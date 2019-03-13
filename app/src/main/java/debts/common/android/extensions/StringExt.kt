package debts.common.android.extensions

import java.util.*

fun Date.toSimpleDateString(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this
//    return String.format(Locale.US,"%1\$tA %1\$tB %1\$tb %1\$td %1\$tY at %1\$tI:%1\$tM %1\$Tp", calendar)
    return String.format(Locale.US, "%1\$td %1\$tB %1\$tY", calendar)
}
