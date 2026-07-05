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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow

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

    fun observeDebtors(): Flow<List<DebtorModel>> =
        dao.observeDebtors().map { items -> items.map { it.toDebtorModel() } }

    fun observeDebtor(debtorId: Long): Flow<DebtorModel?> =
        dao.observeDebtor(debtorId).map { it?.toDebtorModel() }

    suspend fun getDebtors(): List<DebtorModel> =
        dao.observeDebtors().map { items -> items.map { it.toDebtorModel() } }.first()

    suspend fun getDebt(debtId: Long): DebtModel = dao.getDebt(debtId).toDebtModel()

    fun observeDebts(debtorId: Long = 0): Flow<List<DebtModel>> =
        (if (debtorId == 0L) dao.observeDebts() else dao.observeDebts(debtorId))
            .map { items -> items.map { it.toDebtModel() } }

    suspend fun getDebts(debtorId: Long = 0L): List<DebtModel> =
        observeDebts(debtorId).first()

    fun getContacts(): List<ContactsItemModel> {
        val items = mutableListOf<ContactsItemModel>()
        val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
        cursor?.use {
            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    val avatar = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI))
                    items.add(ContactsItemModel(id, name ?: "", avatar ?: ""))
                }
            }
        }
        return items
    }

    suspend fun createDebtor(
        name: String,
        contactId: Long?,
        avatarUrl: String,
        email: String = "",
        phone: String = "",
    ): Long = dao.insertDebtor(DebtorEntity(INSERT_ID, contactId, name, avatarUrl, email, phone))

    suspend fun updateDebtors(items: List<DebtorModel>) =
        dao.updateDebtors(items.map { it.toDebtorEntity() })

    suspend fun saveDebt(
        debtorId: Long,
        amount: Double,
        currency: String,
        comment: String,
        date: Long,
    ): Long = dao.insertDebt(DebtEntity(INSERT_ID, debtorId, amount, currency, date, comment))

    suspend fun updateDebt(
        id: Long,
        debtorId: Long,
        amount: Double,
        currency: String,
        date: Long,
        comment: String,
    ) = dao.updateDebt(DebtEntity(id, debtorId, amount, currency, date, comment))

    suspend fun clearDebts(debtorId: Long) = dao.clearAllDebts(debtorId)

    suspend fun removeDebt(id: Long) = dao.deleteDebt(id)

    suspend fun updateDebtsCurrency() {
        val currency = getCurrency()
        dao.updateDebtsCurrency(currency)
    }

    suspend fun removeDebtor(debtorId: Long) = dao.deleteDebtor(debtorId)

    // region Preferences

    suspend fun isContactsSynced(): Boolean = preferences.getBoolean(PREFS_IS_CONTACT_SYNCED, false)

    suspend fun setContactsSynced(isSynced: Boolean = true) =
        preferences.putBoolean(PREFS_IS_CONTACT_SYNCED, isSynced)

    suspend fun getCurrency(): String = preferences.getString(PREFS_CURRENCY, "")
    fun observeCurrency(): Flow<String> = preferences.observeString(PREFS_CURRENCY, "").asFlow()
    suspend fun setCurrency(currency: String) = preferences.putString(PREFS_CURRENCY, currency)

    suspend fun isAppFirstStart(): Boolean = preferences.getBoolean(PREFS_IS_FIRST_START, true)

    suspend fun setAppFirstStart(isAppFirstStart: Boolean = true) =
        preferences.putBoolean(PREFS_IS_FIRST_START, isAppFirstStart)

    fun observeSortType(): Flow<SortType> =
        preferences.observeString(PREFS_SORT_KEY, SortType.NOTHING.name)
            .asFlow()
            .map { SortType.valueOf(it) }

    fun setSortType(sortType: SortType) = preferences.putString(PREFS_SORT_KEY, sortType.name)

    fun observeDebtorsFilter(): Flow<String> = preferences.observeString(PREFS_FILTER_KEY, "").asFlow()
    fun setDebtorsFilter(name: String) = preferences.putString(PREFS_FILTER_KEY, name)

    // endregion
}
