package debts.di

import debts.home.list.mvi.DebtorsInteractor
import debts.home.list.mvi.DebtorsViewModel
import debts.home.repository.DebtsRepository
import debts.home.usecase.AddDebtUseCase
import debts.home.usecase.GetContactsUseCase
import debts.home.usecase.ObserveDebtorsListItemsUseCase
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

}

val networkModule = module {

}

val repositoriesModule = module {
    single { DebtsRepository(contentResolver = androidApplication().contentResolver) }
}

val useCasesModule = module {
    single { ObserveDebtorsListItemsUseCase(repository = get()) }
    single { GetContactsUseCase(repository = get()) }
    single { AddDebtUseCase(repository = get()) }
}

val interactorModule = module {
    factory {
        DebtorsInteractor(
            observeDebtorsListItemsUseCase = get(),
            getContactsUseCase = get(),
            addDebtUseCase = get()
        )
    }
}

val viewModelModule = module {
    viewModel { DebtorsViewModel(interactor = get()) }
}
