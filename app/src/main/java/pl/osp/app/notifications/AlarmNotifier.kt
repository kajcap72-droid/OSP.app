package pl.osp.app.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import pl.osp.app.MainActivity
import pl.osp.app.R
import pl.osp.app.data.model.Alarm
import pl.osp.app.data.model.Disposition
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wyświetla powiadomienie o alarmie — z dźwiękiem syreny, wibracją,
 * full-screen intent (wybudza ekran) i akcjami „Jadę / Nie jadę".
 */
@Singleton
class AlarmNotifier @Inject constructor(
    private val context: Context
) {

    fun showAlarm(alarm: Alarm) {
        val nm = context.getSystemService<NotificationManager>() ?: return

        // Klik w treść powiadomienia → otwórz szczegóły alarmu
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ALARM_ID, alarm.id)
        }
        val openPending = PendingIntent.getActivity(
            context, alarm.id.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Akcje „Jadę / Nie jadę"
        fun dispositionPending(d: Disposition, requestCode: Int): PendingIntent {
            val intent = Intent(context, DispositionActionReceiver::class.java).apply {
                action = DispositionActionReceiver.ACTION_SET
                putExtra(EXTRA_ALARM_ID, alarm.id)
                putExtra(EXTRA_DISPOSITION, d.name)
            }
            return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
        val goingPending = dispositionPending(Disposition.GOING, alarm.id.hashCode() + 1)
        val notPending = dispositionPending(Disposition.NOT_GOING, alarm.id.hashCode() + 2)

        val notification = NotificationCompat.Builder(context, NotificationChannels.ALARM)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🚨 ${alarm.type.icon} ${alarm.type.displayName}")
            .setContentText(alarm.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${alarm.title}\n\n📍 ${alarm.address}\n\n${alarm.description}")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setColor(0xFFC8102E.toInt())
            .setColorized(true)
            .setAutoCancel(true)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openPending)
            .setFullScreenIntent(openPending, true) // wybudza ekran
            .addAction(
                R.drawable.ic_notification, "✓ Jadę", goingPending
            )
            .addAction(
                R.drawable.ic_notification, "✕ Nie jadę", notPending
            )
            .build()

        nm.notify(alarm.id.hashCode(), notification)
    }

    fun cancel(alarmId: String) {
        context.getSystemService<NotificationManager>()?.cancel(alarmId.hashCode())
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_DISPOSITION = "extra_disposition"
    }
}
