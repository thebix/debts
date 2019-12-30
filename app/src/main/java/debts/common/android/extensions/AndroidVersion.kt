package debts.common.android.extensions

import android.os.Build

/**
 * >= android-7.1, Api 25
 */
fun atLeastNougatMr1() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1

/**
 * >= android-7.1, Api 25
 */
fun ifAtLeastNougatMr1(callback: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
        callback()
}
