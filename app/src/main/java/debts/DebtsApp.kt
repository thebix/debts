package debts

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.squareup.leakcanary.LeakCanary
import debts.common.TimberCrashlyticsTree
import debts.di.*
import io.fabric.sdk.android.Fabric
import net.thebix.debts.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class DebtsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Fabric.with(this, Crashlytics())
        } else {
            Timber.plant(TimberCrashlyticsTree())
            Fabric.with(this, Crashlytics())
        }
        // start Koin!
        startKoin {
            // use AndroidLogger as Koin Logger - default Level.INFO
            androidLogger()

            // use the Android context given there
            androidContext(this@DebtsApp)

            // module list
            modules(
                appModule,
                repositoriesModule,
                useCasesModule,
                interactorModule,
                viewModelModule
            )
        }
    }
}
