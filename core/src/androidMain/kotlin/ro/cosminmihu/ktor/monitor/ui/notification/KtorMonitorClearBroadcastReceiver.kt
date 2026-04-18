package ro.cosminmihu.ktor.monitor.ui.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.get
import ro.cosminmihu.ktor.monitor.di.LibraryKoinComponent
import ro.cosminmihu.ktor.monitor.domain.DeleteCallsUseCase

internal class KtorMonitorClearBroadcastReceiver : BroadcastReceiver(), LibraryKoinComponent {

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        get<CoroutineScope>().launch {
            get<DeleteCallsUseCase>()()
        }
    }
}
