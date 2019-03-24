package debts.di

import androidx.room.Room
import debts.db.DebtsDatabase
import debts.home.ScreenContextHolder
import debts.home.ScreenContextHolderImpl
import debts.home.details.mvi.DetailsInteractor
import debts.home.details.mvi.DetailsViewModel
import debts.home.list.DebtorsNavigator
import debts.home.list.mvi.DebtorsInteractor
import debts.home.list.mvi.DebtorsViewModel
import debts.home.repository.DebtsRepository
import debts.home.usecase.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room
            .databaseBuilder(
                androidApplication(),
                DebtsDatabase::class.java,
                DebtsDatabase.DB_NAME
            ).build()
    }

    single<ScreenContextHolder> {
        ScreenContextHolderImpl()
    }

}

val networkModule = module {

}

val repositoriesModule = module {
    single {
        val debtsDatabase: DebtsDatabase = get()
        DebtsRepository(
            contentResolver = androidApplication().contentResolver,
            dao = debtsDatabase.debtsDao()
        )
    }
}

val useCasesModule = module {
    single { ObserveDebtorsListItemsUseCase(repository = get()) }
    single { GetContactsUseCase(repository = get()) }
    single {
        AddDebtUseCase(
            repository = get(),
            createDebtorUseCase = get()
        )
    }
    single { CreateDebtorUseCase(repository = get()) }
    single { ClearHistoryUseCase(repository = get()) }
    single { ObserveDebtorUseCase(repository = get()) }
    single { ObserveDebtsUseCase(repository = get()) }
    single { RemoveDebtUseCase(repository = get()) }
    single { RemoveDebtorUseCase(repository = get()) }
}

val interactorModule = module {

    // TODO: scope this navigator to the Debtors screen
    single {
        DebtorsNavigator(
            get()
        )
    }

    factory {
        DebtorsInteractor(
            observeDebtorsListItemsUseCase = get(),
            getContactsUseCase = get(),
            addDebtUseCase = get(),
            removeDebtorUseCase = get(),
            debtorsNavigator = get()
        )
    }
    factory {
        DetailsInteractor(
            clearHistoryUseCase = get(),
            addDebtUseCase = get(),
            observeDebtorUseCase = get(),
            observeDebtsUseCase = get(),
            removeDebtUseCase = get(),
            removeDebtorUseCase = get()
        )
    }
}

val viewModelModule = module {
    viewModel { DebtorsViewModel(interactor = get()) }
    viewModel { DetailsViewModel(interactor = get()) }
}
