package pl.osp.app.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import pl.osp.app.data.model.Disposition
import pl.osp.app.data.repository.RemizaRepository
import javax.inject.Inject

/**
 * Reaguje na kliknięcie „Jadę / Nie jadę" w powiadomieniu alarmowym —
 * zapisuje dyspozycję w repozytorium i usuwa powiadomienie.
 */
@AndroidEntryPoint
class DispositionActionReceiver : BroadcastReceiver() {

    @Inject lateinit var repo: RemizaRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_SET) return
        val alarmId = intent.getStringExtra(AlarmNotifier.EXTRA_ALARM_ID) ?: return
        val dispositionName = intent.getStringExtra(AlarmNotifier.EXTRA_DISPOSITION) ?: return
        val disposition = runCatching { Disposition.valueOf(dispositionName) }.getOrNull() ?: return

        val pending = goAsync()
        scope.launch {
            try {
                repo.setMyDisposition(alarmId, disposition)
                context.getSystemService<NotificationManager>()?.cancel(alarmId.hashCode())
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_SET = "pl.osp.app.action.SET_DISPOSITION"
    }
}
