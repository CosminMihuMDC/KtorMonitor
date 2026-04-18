package ro.cosminmihu.ktor.monitor.ui.notification

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import ro.cosminmihu.ktor.monitor.R
import ro.cosminmihu.ktor.monitor.ui.Dimens

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal actual fun NotificationPermissionBanner() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val cameraPermissionState =
        rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)

    AnimatedVisibility(visible = !cameraPermissionState.status.isGranted) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(vertical = Dimens.Small, horizontal = Dimens.Medium)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.ktor_permission_message),
            )
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text(text = stringResource(R.string.ktor_permission_grant))
            }
        }
    }
}