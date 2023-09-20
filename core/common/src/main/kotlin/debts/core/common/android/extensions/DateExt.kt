package debts.core.common.android.extensions

import java.util.Calendar
import java.util.Date
import java.util.Locale

fun Date.toSimpleDateString(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this
//    return String.format(Locale.US,"%1\$tA %1\$tB %1\$tb %1\$td %1\$tY at %1\$tI:%1\$tM %1\$Tp", calendar)
    return String.format(Locale.getDefault(), "%1\$td %1\$tB %1\$tY", calendar)
}

fun Date.toSimpleDateTimeString(): String {
    val calendar = Calendar.getInstance()
    calendar.time = this
    // TODO: format time to %1\$tI:%1\$tM %1\$Tp based on system locale
    return String.format(Locale.getDefault(), "%1\$td %1\$tB %1\$tY %1\$tH:%1\$tM", calendar)
}
