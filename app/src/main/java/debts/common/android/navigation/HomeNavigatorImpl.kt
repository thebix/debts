package debts.common.android.navigation

import android.content.Context
import android.content.Intent
import debts.feature.details.DetailsActivity
import debts.feature.home.HomeNavigator
import debts.feature.preferences.PreferencesActivity

class HomeNavigatorImpl : HomeNavigator {

    override fun detailsIntent(context: Context, debtorId: Long): Intent =
        DetailsActivity.createIntent(context, debtorId)

    override fun settingsIntent(context: Context): Intent =
        PreferencesActivity.createIntent(context)
}
