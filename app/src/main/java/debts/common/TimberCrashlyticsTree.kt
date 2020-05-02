package debts.common

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class TimberCrashlyticsTree : Timber.Tree() {

    private companion object {
        const val CRASHLYTICS_KEY_PRIORITY = "priority"
        const val CRASHLYTICS_KEY_TAG = "tag"
        const val CRASHLYTICS_KEY_MESSAGE = "message"
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }

        val crashlytics = FirebaseCrashlytics.getInstance()

        crashlytics.setCustomKey(CRASHLYTICS_KEY_PRIORITY, priority)
        tag?.let { crashlytics.setCustomKey(CRASHLYTICS_KEY_TAG, it) }
        crashlytics.setCustomKey(CRASHLYTICS_KEY_MESSAGE, message)

        if (t == null) {
            crashlytics.recordException(Exception(message))
        } else {
            crashlytics.recordException(t)
        }
    }
}
