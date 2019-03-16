package debts.home.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import debts.db.DebtEntity
import debts.db.DebtorEntity
import debts.db.DebtsDao
import debts.home.usecase.ContactsItemModel
import debts.home.usecase.DebtModel
import debts.home.usecase.DebtorModel
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*

class DebtsRepository(
    private val contentResolver: ContentResolver,
    private val dao: DebtsDao
) {

    private companion object {
        const val INSERT_ID = 0L
    }

    fun observeDebtors(): Observable<List<DebtorModel>> = dao.observeDebtors()
        .map { items -> items.map { it.toDebtorModel() } }


    fun getDebtors(): Single<List<DebtorModel>> =
        observeDebtors()
            .take(1)
            .single(emptyList())

    fun observeDebts(): Observable<List<DebtModel>> = dao.observeDebts()
        .map { items -> items.map { it.toDebtModel() } }

    fun getDebts(): Single<List<DebtModel>> =
        observeDebts()
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
}
