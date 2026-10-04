package debts.di

import androidx.room.Room
import debts.common.android.navigation.HomeNavigatorImpl
import debts.core.common.android.navigation.ScreenContextHolderImpl
import debts.common.android.buildconfig.BuildConfigDataImpl
import debts.core.common.android.buildconfig.BuildConfigData
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
import debts.feature.details.DetailsViewModel
import debts.feature.home.HomeNavigator
import debts.feature.home.HomeViewModel
import debts.feature.adddebt.AddDebtViewModel
import debts.feature.preferences.PreferencesViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
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
    single<HomeNavigator> { HomeNavigatorImpl() }
}

val viewModelModule = module {
    viewModel { params ->
        DetailsViewModel(
            debtorId = params.get(),
            clearHistoryUseCase = get(),
            addDebtUseCase = get(),
            observeDebtorUseCase = get(),
            observeDebtsUseCase = get(),
            removeDebtUseCase = get(),
            getDebtUseCase = get(),
            updateDebtUseCase = get(),
            removeDebtorUseCase = get(),
            getShareDebtorContentUseCase = get(),
            repository = get(),
        )
    }
    viewModel {
        PreferencesViewModel(
            updateDbDebtsCurrencyUseCase = get(),
            syncDebtorsWithContactsUseCase = get(),
            repository = get(),
            buildConfigData = get(),
        )
    }
    viewModel {
        HomeViewModel(
            getContactsUseCase = get(),
            addDebtUseCase = get(),
            getDebtsCsvContentUseCase = get(),
            observeDebtorsListItemsUseCase = get(),
            syncDebtorsWithContactsUseCase = get(),
            updateDbDebtsCurrencyUseCase = get(),
            removeDebtorUseCase = get(),
            getShareDebtorContentUseCase = get(),
            repository = get(),
        )
    }
    viewModel { AddDebtViewModel() }
}
