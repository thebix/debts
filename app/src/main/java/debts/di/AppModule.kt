package debts.di

import androidx.room.Room
import debts.common.android.DebtsNavigatorImpl
import debts.common.android.ScreenContextHolderImpl
import debts.common.android.buildconfig.BuildConfigDataImpl
import debts.core.common.android.buildconfig.BuildConfigData
import debts.core.common.android.navigation.DebtsNavigator
import debts.core.common.android.navigation.ScreenContextHolder
import debts.core.common.android.prefs.AndroidPreferences
import debts.core.common.android.prefs.Preferences
import debts.core.db.DebtsDatabase
import debts.core.db.migrations.migration1To2
import debts.core.repository.DebtsRepository
import debts.core.usecase.AddDebtUseCase
import debts.core.usecase.ClearHistoryUseCase
import debts.core.usecase.CreateDebtorUseCase
import debts.core.usecase.GetContactsUseCase
import debts.core.usecase.GetDebtUseCase
import debts.core.usecase.GetDebtsCsvContentUseCase
import debts.core.usecase.GetShareDebtorContentUseCase
import debts.core.usecase.ObserveDebtorUseCase
import debts.core.usecase.ObserveDebtorsListItemsUseCase
import debts.core.usecase.ObserveDebtsUseCase
import debts.core.usecase.RemoveDebtUseCase
import debts.core.usecase.RemoveDebtorUseCase
import debts.core.usecase.SyncDebtorsWithContactsUseCase
import debts.core.usecase.UpdateDbDebtsCurrencyUseCase
import debts.core.usecase.UpdateDebtUseCase
import debts.feature.details.mvi.DetailsInteractor
import debts.feature.details.mvi.DetailsViewModel
import debts.feature.home.di.getDebtorsDebtsNavigatorName
import debts.feature.home.di.getDebtorsInteractorName
import debts.feature.home.di.getDebtorsViewModelName
import debts.feature.home.list.mvi.DebtorsInteractor
import debts.feature.home.list.mvi.DebtorsViewModel
import debts.feature.home.list.mvi.HomeInteractor
import debts.feature.home.list.mvi.HomeViewModel
import debts.feature.preferences.mvi.MainSettingsInteractor
import debts.feature.preferences.mvi.MainSettingsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.StringQualifier
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
    single<BuildConfigData> {
        BuildConfigDataImpl()
    }
}

val networkModule = module {}

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
    single { UpdateDebtUseCase(repository = get()) }
    single { CreateDebtorUseCase(repository = get()) }
    single { ClearHistoryUseCase(repository = get()) }
    single { ObserveDebtorUseCase(repository = get()) }
    single { ObserveDebtsUseCase(repository = get()) }
    single { RemoveDebtUseCase(repository = get()) }
    single { GetDebtUseCase(repository = get()) }
    single { RemoveDebtorUseCase(repository = get()) }
    single { SyncDebtorsWithContactsUseCase(repository = get()) }
    single { UpdateDbDebtsCurrencyUseCase(repository = get()) }
    single { GetDebtsCsvContentUseCase(repository = get()) }
    single { GetShareDebtorContentUseCase(repository = get()) }
}

val interactorModule = module {
    single<DebtsNavigator>(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES)) {
        DebtsNavigatorImpl(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES
        )
    }
    single<DebtsNavigator>(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_DETAILS)) {
        DebtsNavigatorImpl(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.FRAGMENT_DETAILS
        )
    }
    single<DebtsNavigator>(qualifier = StringQualifier(ScreenContextHolder.ACTIVITY_HOME)) {
        DebtsNavigatorImpl(
            screenContextHolder = get(),
            applicationContext = androidContext(),
            name = ScreenContextHolder.ACTIVITY_HOME
        )
    }
    for (page in 0..2) {
        // TODO: factory?
        single<DebtsNavigator>(qualifier = StringQualifier(getDebtorsDebtsNavigatorName(page))) {
            DebtsNavigatorImpl(
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
            getDebtUseCase = get(),
            updateDebtUseCase = get(),
            removeDebtorUseCase = get(),
            getShareDebtorContentUseCase = get(),
            repository = get()
        )
    }
    factory {
        MainSettingsInteractor(
            debtsNavigator = get(qualifier = StringQualifier(ScreenContextHolder.FRAGMENT_MAIN_PREFERENCES)),
            updateDbDebtsCurrencyUseCase = get(),
            syncDebtorsWithContactsUseCase = get(),
            repository = get()
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
    viewModel { debts.feature.details.mvi.DetailsViewModel(interactor = get()) }
    viewModel { MainSettingsViewModel(interactor = get()) }
    viewModel { HomeViewModel(interactor = get()) }
}
