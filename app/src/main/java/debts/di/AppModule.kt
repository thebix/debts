package debts.di

import androidx.room.Room
import debts.common.android.DebtsNavigator
import debts.common.android.ScreenContextHolder
import debts.common.android.ScreenContextHolderImpl
import debts.common.android.prefs.AndroidPreferences
import debts.common.android.prefs.Preferences
import debts.db.DebtsDatabase
import debts.db.migrations.migration1To2
import debts.details.mvi.DetailsInteractor
import debts.details.mvi.DetailsViewModel
import debts.home.list.mvi.DebtorsInteractor
import debts.home.list.mvi.DebtorsViewModel
import debts.home.list.mvi.HomeInteractor
import debts.home.list.mvi.HomeViewModel
import debts.preferences.main.mvi.MainSettingsInteractor
import debts.preferences.main.mvi.MainSettingsViewModel
import debts.repository.DebtsRepository
import debts.usecase.*
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module

fun getDebtorsDebtsNavigatorName(page: Int) = "${ScreenContextHolder.FRAGMENT_DEBTORS}$page"
fun getDebtorsInteractorName(page: Int) = "DebtorsInteractor_$page"
fun getDebtorsViewModelName(page: Int) = "DebtorsViewModel_$page"

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
    single { GetDebtsCsvContentUseCase(repository = get()) }
    single { GetShareDebtorContentUseCase(repository = get()) }
}

val interactorModule = module {
    single(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES)) {
        DebtsNavigator(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES
        )
    }
    single(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_DETAILS)) {
        DebtsNavigator(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.FRAGMENT_DETAILS
        )
    }
    single(qualifier = StringQualifier(ScreenContextHolder.ACTIVITY_HOME)) {
        DebtsNavigator(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.ACTIVITY_HOME
        )
    }
    for (page in 0..2) {
        // TODO: factory?
        single(qualifier = StringQualifier(getDebtorsDebtsNavigatorName(page))) {
            DebtsNavigator(
                screenContextHolder = get(),
                applicationContext = androidContext(),
                name = getDebtorsDebtsNavigatorName(page)
            )
        }
        // TODO: factory?
        single(qualifier = StringQualifier(getDebtorsInteractorName(page))) {
            DebtorsInteractor(
                observeDebtorsListItemsUseCase = get(),
                removeDebtorUseCase = get(),
                debtsNavigator = get(qualifier = StringQualifier(getDebtorsDebtsNavigatorName(page))),
                getShareDebtorContentUseCase = get(),
                repository = get()
            )
        }
    }
    factory {
        DetailsInteractor(
            debtsNavigator = get(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_DETAILS)),
            clearHistoryUseCase = get(),
            addDebtUseCase = get(),
            observeDebtorUseCase = get(),
            observeDebtsUseCase = get(),
            removeDebtUseCase = get(),
            removeDebtorUseCase = get(),
            getShareDebtorContentUseCase = get(),
            repository = get()
        )
    }
    factory {
        MainSettingsInteractor(
            debtsNavigator = get(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES)),
            updateDbDebtsCurrencyUseCase = get(),
            syncDebtorsWithContactsUseCase = get()
        )
    }
    factory {
        HomeInteractor(
            getContactsUseCase = get(),
            addDebtUseCase = get(),
            debtsNavigator = get(qualifier = StringQualifier(ScreenContextHolder.ACTIVITY_HOME)),
            getDebtsCsvContentUseCase = get(),
            observeDebtorsListItemsUseCase = get(),
            syncDebtorsWithContactsUseCase = get(),
            updateDbDebtsCurrencyUseCase = get(),
            repository = get()
        )
    }
}

val viewModelModule = module {
    for (page in 0..2) {
        viewModel(qualifier = StringQualifier(getDebtorsViewModelName(page))) {
            DebtorsViewModel(
                interactor = get(
                    qualifier = StringQualifier(getDebtorsInteractorName(page))
                )
            )
        }
    }
    viewModel { DetailsViewModel(interactor = get()) }
    viewModel { MainSettingsViewModel(interactor = get()) }
    viewModel { HomeViewModel(interactor = get()) }
}
