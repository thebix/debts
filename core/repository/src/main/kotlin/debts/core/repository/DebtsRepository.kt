package debts.core.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import debts.core.common.android.prefs.Preferences
import debts.core.db.DebtEntity
import debts.core.db.DebtorEntity
import debts.core.db.DebtsDao
import debts.core.repository.data.ContactsItemModel
import debts.core.repository.data.DebtModel
import debts.core.repository.data.DebtorModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle

@Suppress("TooManyFunctions")
class DebtsRepository(
    private val contentResolver: ContentResolver,
    private val dao: DebtsDao,
    private val preferences: Preferences,
) {

    private companion object {
        const val INSERT_ID = 0L
        const val PREFS_IS_CONTACT_SYNCED = "PREFS_IS_CONTACT_SYNCED"
        const val PREFS_IS_FIRST_START = "PREFS_IS_FIRST_START"
        const val PREFS_CURRENCY = "preference_main_settings_currency_custom"
        const val PREFS_SORT_KEY = "PREFS_SORT_KEY"
        const val PREFS_FILTER_KEY = "PREFS_FILTER_KEY"
    }

    fun observeDebtors(): Observable<List<DebtorModel>> =
        dao.observeDebtors()
            .map { items -> items.map { it.toDebtorModel() } }
            .asObservable()

    fun observeDebtor(debtorId: Long): Observable<DebtorModel> =
        dao.observeDebtor(debtorId)
            .filterNotNull()
            .map { it.toDebtorModel() }
            .asObservable()

    fun getDebtors(): Single<List<DebtorModel>> =
        observeDebtors()
            .take(1)
            .single(emptyList())

    fun getDebt(debtId: Long): Single<DebtModel> =
        rxSingle { dao.getDebt(debtId).toDebtModel() }

    fun observeDebts(debtorId: Long = 0): Observable<List<DebtModel>> =
        (if (debtorId == 0L) dao.observeDebts() else dao.observeDebts(debtorId))
            .map { items -> items.map { it.toDebtModel() } }
            .asObservable()

    fun getDebts(debtorId: Long = 0L): Single<List<DebtModel>> =
        observeDebts(debtorId)
            .take(1)
            .single(emptyList())

    fun getContacts(): Single<List<ContactsItemModel>> =
        Single.fromCallable {
            val items = mutableListOf<ContactsItemModel>()
            val cursor =
                contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            cursor?.let {
                if (cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(
                            cursor.getColumnIndex(ContactsContract.Contacts._ID)
                        )
                        val name = cursor.getString(
                            cursor.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME
                            )
                        )
                        val avatar = cursor.getString(
                            cursor.getColumnIndex(
                                ContactsContract.Contacts.PHOTO_URI
                            )
                        )
                        items.add(
                            ContactsItemModel(id, name ?: "", avatar ?: "")
                        )
                    }
                }
            }

            cursor?.close()

            items
        }

    fun createDebtor(
        name: String,
        contactId: Long?,
        avatarUrl: String,
        email: String = "",
        phone: String = "",
    ): Single<Long> =
        rxSingle {
            dao.insertDebtor(
                DebtorEntity(
                    INSERT_ID,
                    contactId,
                    name,
                    avatarUrl,
                    email,
                    phone
                )
            )
        }

    fun updateDebtors(items: List<DebtorModel>): Completable =
        rxCompletable { dao.updateDebtors(items.map { it.toDebtorEntity() }) }

    fun saveDebt(
        debtorId: Long,
        amount: Double,
        currency: String,
        comment: String,
        date: Long,
    ): Single<Long> =
        rxSingle {
            dao.insertDebt(
                DebtEntity(
                    INSERT_ID,
                    debtorId,
                    amount,
                    currency,
                    date,
                    comment
                )
            )
        }

    fun updateDebt(
        id: Long,
        debtorId: Long,
        amount: Double,
        currency: String,
        date: Long,
        comment: String,
    ): Completable =
        rxCompletable { dao.updateDebt(DebtEntity(id, debtorId, amount, currency, date, comment)) }

    fun clearDebts(debtorId: Long): Completable = rxCompletable { dao.clearAllDebts(debtorId) }

    fun removeDebt(id: Long): Completable = rxCompletable { dao.deleteDebt(id) }

    fun updateDebtsCurrency(): Completable = getCurrency()
        .flatMapCompletable { currency ->
            rxCompletable { dao.updateDebtsCurrency(currency) }
        }

    fun removeDebtor(debtorId: Long): Completable = rxCompletable { dao.deleteDebtor(debtorId) }

    // region Preferences

    fun isContactsSynced() =
        Single.fromCallable { preferences.getBoolean(PREFS_IS_CONTACT_SYNCED, false) }

    fun setContactsSynced(isSynced: Boolean = true) =
        Completable.fromCallable { preferences.putBoolean(PREFS_IS_CONTACT_SYNCED, isSynced) }

    fun getCurrency(): Single<String> = Single.fromCallable { preferences.getString(PREFS_CURRENCY, "") }
    fun observeCurrency() = preferences.observeString(PREFS_CURRENCY, "")
    fun setCurrency(currency: String) =
        Completable.fromCallable { preferences.putString(PREFS_CURRENCY, currency) }

    fun isAppFirstStart() =
        Single.fromCallable { preferences.getBoolean(PREFS_IS_FIRST_START, true) }

    fun setAppFirstStart(isAppFirstStart: Boolean = true) =
        Completable.fromCallable { preferences.putBoolean(PREFS_IS_FIRST_START, isAppFirstStart) }

    fun observeSortType(): Observable<SortType> =
        preferences.observeString(PREFS_SORT_KEY, SortType.NOTHING.name)
            .map { SortType.valueOf(it) }

    fun setSortType(sortType: SortType) {
        preferences.putString(PREFS_SORT_KEY, sortType.name)
    }

    fun observeDebtorsFilter(): Observable<String> = preferences.observeString(PREFS_FILTER_KEY, "")
    fun setDebtorsFilter(name: String) {
        preferences.putString(PREFS_FILTER_KEY, name)
    }

    // endregion
}
