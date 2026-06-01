package pl.osp.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import dagger.hilt.android.AndroidEntryPoint
import pl.osp.app.notifications.AlarmNotifier
import pl.osp.app.ui.OspApp
import pl.osp.app.ui.theme.OspTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Stan deep linka — id alarmu do otwarcia po kliknięciu powiadomienia. */
    private val pendingAlarmId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingAlarmId.value = intent?.getStringExtra(AlarmNotifier.EXTRA_ALARM_ID)
        setContent {
            OspTheme {
                OspApp(
                    deepLinkAlarmId = pendingAlarmId.value,
                    onDeepLinkConsumed = { pendingAlarmId.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingAlarmId.value = intent.getStringExtra(AlarmNotifier.EXTRA_ALARM_ID)
    }
}
