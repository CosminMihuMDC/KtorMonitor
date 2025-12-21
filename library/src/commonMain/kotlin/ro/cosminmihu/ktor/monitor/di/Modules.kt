package ro.cosminmihu.ktor.monitor.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import ro.cosminmihu.ktor.monitor.core.ClipboardManager
import ro.cosminmihu.ktor.monitor.db.LibraryDao
import ro.cosminmihu.ktor.monitor.db.createDatabase
import ro.cosminmihu.ktor.monitor.db.createDatabaseDriver
import ro.cosminmihu.ktor.monitor.domain.ConfigUseCase
import ro.cosminmihu.ktor.monitor.domain.DeleteCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallAsTextUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallRequestAsCurlUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallRequestAsWgetUseCase
import ro.cosminmihu.ktor.monitor.domain.ExportCallUrlUseCase
import ro.cosminmihu.ktor.monitor.domain.GetCallUseCase
import ro.cosminmihu.ktor.monitor.domain.GetCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.ListenByRecentCallsUseCase
import ro.cosminmihu.ktor.monitor.domain.RetentionUseCase
import ro.cosminmihu.ktor.monitor.ui.detail.DetailViewModel
import ro.cosminmihu.ktor.monitor.ui.list.ListViewModel
import ro.cosminmihu.ktor.monitor.ui.main.MainViewModel
import ro.cosminmihu.ktor.monitor.ui.notification.NotificationManager

internal fun libraryModule() = listOf(
    coroutineModule,
    databaseModule,
    domainModule,
    notificationModule,
    viewModelModule,
)

internal val databaseModule = module {
    factory {
        createDatabaseDriver()
    }
    single {
        createDatabase(get())
    }
    factoryOf(::LibraryDao)
}

internal val coroutineModule = module {
    single {
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    }
}

internal val notificationModule = module {
    factoryOf(::NotificationManager)
}

internal val viewModelModule = module {
    viewModelOf(::MainViewModel)
    viewModelOf(::ListViewModel)
    viewModelOf(::DetailViewModel)
}

internal val domainModule = module {
    singleOf(::ConfigUseCase)

    singleOf(::ListenByRecentCallsUseCase)
    factoryOf(::RetentionUseCase)

    factoryOf(::GetCallsUseCase)
    factoryOf(::GetCallUseCase)
    factoryOf(::DeleteCallsUseCase)

    factoryOf(::ClipboardManager)
    factoryOf(::ExportCallUrlUseCase)
    factoryOf(::ExportCallRequestAsCurlUseCase)
    factoryOf(::ExportCallRequestAsWgetUseCase)
    factoryOf(::ExportCallAsTextUseCase)
}