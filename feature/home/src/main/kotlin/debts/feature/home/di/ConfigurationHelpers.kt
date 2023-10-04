package debts.feature.home.di

import debts.core.common.android.navigation.ScreenContextHolder

fun getDebtorsDebtsNavigatorName(page: Int) = "${ScreenContextHolder.FRAGMENT_DEBTORS}$page"
fun getDebtorsInteractorName(page: Int) = "DebtorsInteractor_$page"
fun getDebtorsViewModelName(page: Int) = "DebtorsViewModel_$page"
