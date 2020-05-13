package debts.common.android.prefs

import android.content.Context
import androidx.preference.PreferenceManager
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject

interface Preferences {

    fun getBoolean(key: String, defValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun observeBoolean(key: String, defValue: Boolean): Observable<Boolean>

    fun getString(key: String, defValue: String): String
    fun putString(key: String, value: String)
    fun observeString(key: String, defValue: String): Observable<String>

    operator fun contains(key: String): Boolean
    fun remove(key: String)
    fun clear()
}

class AndroidPreferences(
    context: Context,
    preferencesName: String = ""
) : Preferences {

    private val prefs = if (preferencesName.isBlank()) {
        PreferenceManager.getDefaultSharedPreferences(context)
    } else {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE)
    }
    private val publishSubject = PublishSubject.create<String>()

    override fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)
    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun observeBoolean(key: String, defValue: Boolean): Observable<Boolean> {
        return Observable.merge(
            Single.fromCallable { getBoolean(key, defValue) }
                .toObservable(),
            publishSubject
                .filter { it == key }
                .map { getBoolean(it, defValue) }
        )
    }

    override fun getString(key: String, defValue: String): String = prefs.getString(key, defValue) ?: ""
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
        publishSubject.onNext(key)
    }

    override fun observeString(key: String, defValue: String): Observable<String> {
        return Observable.merge(
            Single.fromCallable { getString(key, defValue) }
                .toObservable(),
            publishSubject
                .filter { it == key }
                .map { getString(it, defValue) }
        )
    }

    override fun contains(key: String): Boolean = prefs.contains(key)

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
