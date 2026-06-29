package debts

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import debts.common.TimberCrashlyticsTree
import debts.di.appModule
import debts.di.interactorModule
import debts.di.repositoriesModule
import debts.di.useCasesModule
import debts.di.viewModelModule
import net.thebix.debts.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class DebtsApp : Application() {

    override fun onCreate() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(TimberCrashlyticsTree())
        }
        startKoin {
            androidLogger()
            androidContext(this@DebtsApp)
            modules(
                listOf(
                    appModule,
                    repositoriesModule,
                    useCasesModule,
                    interactorModule,
                    viewModelModule
                )
            )
        }
    }
}
