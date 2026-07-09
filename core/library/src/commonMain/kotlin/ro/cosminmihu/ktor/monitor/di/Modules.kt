package ro.cosminmihu.ktor.monitor.di

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.factory
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import ro.cosminmihu.ktor.monitor.core.ClipboardManager
import ro.cosminmihu.ktor.monitor.core.ShareManager
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
    factory { create(::createDatabaseDriver) }
    single { create(::createDatabase) }
    factory<LibraryDao>()
}

internal val coroutineModule = module {
    single {
        val exceptionHandler = CoroutineExceptionHandler { _, _ -> }
        CoroutineScope(Dispatchers.Default + SupervisorJob() + exceptionHandler)
    }
}

internal val notificationModule = module {
    factory<NotificationManager>()
}

internal val viewModelModule = module {
    viewModel<MainViewModel>()
    viewModel<ListViewModel>()
    viewModel<DetailViewModel>()
}

internal val domainModule = module {
    single<ConfigUseCase>()

    single<ListenByRecentCallsUseCase>()
    factory<RetentionUseCase>()

    factory<GetCallsUseCase>()
    factory<GetCallUseCase>()
    factory<DeleteCallsUseCase>()

    factory<ClipboardManager>()
    factory<ShareManager>()

    factory<ExportCallUrlUseCase>()
    factory<ExportCallRequestAsCurlUseCase>()
    factory<ExportCallRequestAsWgetUseCase>()
    factory<ExportCallAsTextUseCase>()
}