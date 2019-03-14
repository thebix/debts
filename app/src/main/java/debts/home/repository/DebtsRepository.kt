package debts.home.repository

import android.content.ContentResolver
import android.provider.ContactsContract
import debts.home.usecase.ContactsItemModel
import debts.home.usecase.DebtModel
import debts.home.usecase.DebtorModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.ReplaySubject
import java.util.*

class DebtsRepository(
    private val contentResolver: ContentResolver
) {

    // region temporary storage

    private val debtors = ReplaySubject.create<List<DebtorModel>>(1)
    private val debts = ReplaySubject.create<List<DebtModel>>(1)

    private var debtorsList = emptyList<DebtorModel>()
    private var debtsList = emptyList<DebtModel>()

    init {
        debtors.onNext(
            debtorsList
        )
        debts.onNext(
            debtsList
        )
    }

    // endregion

    fun observeDebtors(): Observable<List<DebtorModel>> = debtors

    fun getDebtors(): Single<List<DebtorModel>> =
        Single.fromCallable { debtorsList }

    fun observeDebts(): Observable<List<DebtModel>> = debts

    fun getDebts(): Single<List<DebtModel>> =
        Single.fromCallable { debtsList }

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
        avatarUrl: String
    ): Single<Long> = getDebtors()
        .map { items ->
            val list: MutableList<DebtorModel> = mutableListOf()
            list.addAll(items)
            list.add(
                DebtorModel(
                    items.size.toLong(),
                    name,
                    contactId,
                    avatarUrl
                )
            )
            list.toList()
        }
        .doOnSuccess {
            debtorsList = it
            debtors.onNext(it)
        }
        .map { it.size.toLong() - 1 }


    fun saveDebt(
        debtorId: Long,
        amount: Double,
        currency: String,
        comment: String
    ): Completable = getDebts()
        .map { items ->
            val list: MutableList<DebtModel> = mutableListOf()
            list.addAll(items)
            list.add(
                DebtModel(
                    items.size.toLong(),
                    debtorId,
                    amount,
                    currency,
                    Date().time,
                    comment
                )
            )
            list.toList()
        }
        .doOnSuccess {
            debtsList = it
            debts.onNext(it)
        }
        .ignoreElement()
}
