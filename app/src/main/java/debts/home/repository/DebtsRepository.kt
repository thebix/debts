package debts.home.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import debts.common.android.prefs.Preferences
import debts.db.DebtEntity
import debts.db.DebtorEntity
import debts.db.DebtsDao
import debts.home.usecase.ContactsItemModel
import debts.home.usecase.DebtModel
import debts.home.usecase.DebtorModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class DebtsRepository(
    private val contentResolver: ContentResolver,
    private val dao: DebtsDao,
    private val preferences: Preferences
) {

    private companion object {
        const val INSERT_ID = 0L
        const val PREFS_IS_CONTACT_SYNCED = "PREFS_IS_CONTACT_SYNCED"
    }

    fun observeDebtors(): Observable<List<DebtorModel>> = dao.observeDebtors()
        .map { items -> items.map { it.toDebtorModel() } }

    fun observeDebtor(debtorId: Long): Observable<DebtorModel> = dao.observeDebtor(debtorId)
        .map { items -> items.toDebtorModel() }

    fun getDebtors(): Single<List<DebtorModel>> =
        observeDebtors()
            .take(1)
            .single(emptyList())

    fun observeDebts(debtorId: Long = 0): Observable<List<DebtModel>> =
        (if (debtorId == 0L) dao.observeDebts() else dao.observeDebts(debtorId))
            .map { items -> items.map { it.toDebtModel() } }

    fun getDebts(debtorId: Long = 0L): Single<List<DebtModel>> =
        observeDebts(debtorId)
            .take(1)
            .single(emptyList())

    @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun getContacts(): Single<List<ContactsItemModel>> =
        Single.fromCallable {
            val items = mutableListOf<ContactsItemModel>()
            val cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cursor?.count ?: 0 > 0) {
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
                        ContactsItemModel(
                            id, name ?: "", avatar ?: ""
                        )
                    )
                }
            }
            cursor.close()

            items
        }

    fun createDebtor(
        name: String,
        contactId: Long?,
        avatarUrl: String,
        email: String = "",
        phone: String = ""
    ): Single<Long> =
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

    fun updateDebtors(items: List<DebtorModel>): Completable =
        Completable.fromCallable {
            dao.updateDebtors(items.map { it.toDebtorEntity() })
        }

    fun saveDebt(
        debtorId: Long,
        amount: Double,
        currency: String,
        comment: String
    ): Single<Long> =
        dao.insertDebt(
            DebtEntity(
                INSERT_ID,
                debtorId,
                amount,
                currency,
                Date().time,
                comment
            )
        )

    fun clearDebts(debtorId: Long): Completable = dao.clearAllDebts(debtorId)

    fun removeDebt(id: Long): Completable = dao.deleteDebt(id)

    fun removeDebtor(debtorId: Long): Completable = dao.deleteDebtor(debtorId)

    // region Helpers
    ///////////////////////////////////////////////////////////////////////////

    fun isContactsSynced() = Single.fromCallable { preferences.getBoolean(PREFS_IS_CONTACT_SYNCED, false) }
    fun setContactsSynced(isSynced: Boolean = true) =
        Completable.fromCallable { preferences.putBoolean(PREFS_IS_CONTACT_SYNCED, isSynced) }

    // endregion
}
