package debts.di

import androidx.room.Room
import debts.common.android.ScreenContextHolder
import debts.common.android.ScreenContextHolderImpl
import debts.common.android.prefs.AndroidPreferences
import debts.common.android.prefs.Preferences
import debts.db.DebtsDatabase
import debts.db.migrations.migration1To2
import debts.home.details.mvi.DetailsInteractor
import debts.home.details.mvi.DetailsViewModel
import debts.common.android.DebtsNavigator
import debts.home.list.mvi.DebtorsInteractor
import debts.home.list.mvi.DebtorsViewModel
import debts.preferences.main.mvi.MainSettingsInteractor
import debts.preferences.main.mvi.MainSettingsViewModel
import debts.repository.DebtsRepository
import debts.usecase.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room
            .databaseBuilder(
                androidApplication(),
                DebtsDatabase::class.java,
                DebtsDatabase.DB_NAME
            )
            .addMigrations(
                migration1To2()
            )
            .build()
    }
    single<Preferences> {
        AndroidPreferences(androidContext())
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
            dao = debtsDatabase.debtsDao(),
            preferences = get()
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
    single { SyncDebtorsWithContactsUseCase(repository = get()) }
    single { UpdateDbDebtsCurrencyUseCase(repository = get()) }
}

val interactorModule = module {

    // TODO: scope this navigator to the Debtors screen
    single(name = ScreenContextHolder.FRAGMENT_DEBTORS) {
        DebtsNavigator(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.FRAGMENT_DEBTORS
        )
    }

    single(name = ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES) {
        DebtsNavigator(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES
        )
    }

    factory {
        DebtorsInteractor(
            observeDebtorsListItemsUseCase = get(),
            getContactsUseCase = get(),
            addDebtUseCase = get(),
            removeDebtorUseCase = get(),
            debtsNavigator = get(ScreenContextHolder.FRAGMENT_DEBTORS),
            syncDebtorsWithContactsUseCase = get(),
            repository = get()
        )
    }
    factory {
        DetailsInteractor(
            clearHistoryUseCase = get(),
            addDebtUseCase = get(),
            observeDebtorUseCase = get(),
            observeDebtsUseCase = get(),
            removeDebtUseCase = get(),
            removeDebtorUseCase = get(),
            repository = get()
        )
    }
    factory {
        MainSettingsInteractor(
            debtsNavigator = get(ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES),
            updateDbDebtsCurrencyUseCase = get(),
            syncDebtorsWithContactsUseCase = get()
        )
    }
}

val viewModelModule = module {
    viewModel { DebtorsViewModel(interactor = get()) }
    viewModel { DetailsViewModel(interactor = get()) }
    viewModel { MainSettingsViewModel(interactor = get()) }
}
