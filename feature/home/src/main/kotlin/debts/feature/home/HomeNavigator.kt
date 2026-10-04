package debts.feature.home

import android.content.Context
import android.content.Intent

/**
 * Screens Home opens that live in feature modules it does not depend on.
 */
interface HomeNavigator {

    fun detailsIntent(context: Context, debtorId: Long): Intent

    fun settingsIntent(context: Context): Intent
}
