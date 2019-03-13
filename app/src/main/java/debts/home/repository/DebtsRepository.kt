package debts.home.repository

import debts.home.usecase.DebtModel
import debts.home.usecase.DebtorModel
import io.reactivex.Observable
import java.util.*
import kotlin.random.Random

class DebtsRepository {

    // region temporary data

    private var debtorId: Long = 1
    private fun createDebtor() = DebtorModel(debtorId, "Debtor ${debtorId++}")
    private var debtId: Long = 1
    private fun createDebt(dId: Long = debtorId) =
        DebtModel(debtId, dId, Random.nextDouble(), "RUR", Date().time - Random.nextLong(10000, 20000))

    private val debtorsList = listOf(
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor(),
        createDebtor()
    )

    private val debtsList = listOf(
        createDebt(1L), createDebt(1L), createDebt(1L), createDebt(1L), createDebt(1L),
        createDebt(2L), createDebt(2L), createDebt(2L), createDebt(2L), createDebt(2L)
    )

    // endregion

    fun observeDebtors(): Observable<List<DebtorModel>> {
        return Observable.fromCallable {
            debtorsList
        }
    }

    fun observeDebts(): Observable<List<DebtModel>> {
        return Observable.fromCallable {
            debtsList
        }
    }
}
