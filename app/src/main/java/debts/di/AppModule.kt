package debts.di

import org.koin.dsl.module

val appModule = module {

}

val networkModule = module {

}

val repositoriesModule = module {
    single { Repository() }
}

val useCasesModule = module {
    single { UseCase1(get()) }
    single { UseCase2(get()) }
}

// region just to show approach

class Repository
class UseCase1(val repository: Repository)
class UseCase2(val repository: Repository)

// endregion
