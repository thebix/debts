@file:Suppress("TooGenericExceptionCaught")

package debts.feature.details

import androidx.lifecycle.viewModelScope
import debts.core.common.android.mvvm.BaseViewModel
import debts.core.repository.DebtsRepository
import debts.core.usecase.AddDebtUseCase
import debts.core.usecase.ClearHistoryUseCase
import debts.core.usecase.GetDebtUseCase
import debts.core.usecase.GetShareDebtorContentUseCase
import debts.core.usecase.ObserveDebtorUseCase
import debts.core.usecase.ObserveDebtsUseCase
import debts.core.usecase.RemoveDebtUseCase
import debts.core.usecase.RemoveDebtorUseCase
import debts.core.usecase.UpdateDebtUseCase
import debts.feature.adddebt.DebtLayoutData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.absoluteValue

@Suppress("LongParameterList")
class DetailsViewModel(
    private val debtorId: Long,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val addDebtUseCase: AddDebtUseCase,
    private val observeDebtorUseCase: ObserveDebtorUseCase,
    private val observeDebtsUseCase: ObserveDebtsUseCase,
    private val removeDebtUseCase: RemoveDebtUseCase,
    private val getDebtUseCase: GetDebtUseCase,
    private val updateDebtUseCase: UpdateDebtUseCase,
    private val removeDebtorUseCase: RemoveDebtorUseCase,
    private val getShareDebtorContentUseCase: GetShareDebtorContentUseCase,
    private val repository: DebtsRepository,
) : BaseViewModel<DetailsUiState, DetailsEvent>(DetailsUiState()) {

    init {
        observeDebtor()
        observeDebts()
    }

    fun onChangeClicked() {
        with(uiState.value) { sendEvent(DetailsEvent.ShowAddDebtDialog(name, avatarUrl)) }
    }

    fun onDebtClicked(debtId: Long) {
        launchLogging {
            val debt = getDebtUseCase.execute(debtId)
            with(uiState.value) {
                sendEvent(
                    DetailsEvent.ShowEditDebtDialog(
                        name = name,
                        avatarUrl = avatarUrl,
                        debtId = debtId,
                        amount = debt.amount,
                        comment = debt.comment,
                        date = debt.date,
                    )
                )
            }
        }
    }

    fun onDebtDialogConfirmed(data: DebtLayoutData) {
        if (data.amount == 0.0) {
            sendEvent(DetailsEvent.EmptyDebtAmount)
            return
        }
        val existingDebtId = data.existingDebtId
        if (existingDebtId == null) addDebt(data) else updateDebt(existingDebtId, data)
    }

    fun onRemoveDebtConfirmed(debtId: Long) {
        launchLogging { removeDebtUseCase.execute(debtId) }
    }

    fun onClearHistoryConfirmed() {
        launchLogging { clearHistoryUseCase.execute(debtorId) }
    }

    fun onRemoveDebtorConfirmed() {
        launchLogging {
            removeDebtorUseCase.execute(debtorId)
            sendEvent(DetailsEvent.Close)
        }
    }

    fun onShareClicked(borrowedTemplate: String, lentTemplate: String) {
        launchLogging {
            sendEvent(
                DetailsEvent.ShareDebtor(
                    getShareDebtorContentUseCase.execute(debtorId, borrowedTemplate, lentTemplate)
                )
            )
        }
    }

    // Two collectors, not one combine: the history must show up even if the debtor row never arrives.
    private fun observeDebtor() {
        viewModelScope.launch {
            observeDebtorUseCase.execute(debtorId)
                .catch { Timber.e(it) }
                .collect { debtor ->
                    updateState {
                        copy(
                            name = debtor.name,
                            avatarUrl = debtor.avatarUrl,
                            amount = debtor.amount.absoluteValue,
                            currency = debtor.currency,
                        )
                    }
                }
        }
    }

    private fun observeDebts() {
        viewModelScope.launch {
            observeDebtsUseCase.execute(debtorId)
                .catch { Timber.e(it) }
                .collect { debts ->
                    updateState { copy(debts = debts.sortedByDescending { it.date }) }
                }
        }
    }

    private fun addDebt(data: DebtLayoutData) {
        launchLogging {
            addDebtUseCase.execute(
                debtorId,
                null,
                "",
                data.amount,
                repository.getCurrency(),
                data.comment,
                data.date,
            )
            sendEvent(DetailsEvent.DebtAdded)
        }
    }

    private fun updateDebt(debtId: Long, data: DebtLayoutData) {
        launchLogging {
            val debt = getDebtUseCase.execute(debtId)
            updateDebtUseCase.execute(
                id = debt.id,
                debtorId = debt.debtorId,
                amount = data.amount,
                date = data.date,
                currency = debt.currency,
                comment = data.comment,
            )
            sendEvent(DetailsEvent.DebtChanged)
        }
    }

    private fun launchLogging(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}
