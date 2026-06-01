package pl.osp.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import androidx.core.content.getSystemService
import pl.osp.app.R

object NotificationChannels {
    /** Kanał alarmów bojowych — najwyższy priorytet, bypass DND. */
    const val ALARM = "osp_alarm_channel"
    /** Kanał przypomnień (terminy badań, przeglądów, wydarzenia w kalendarzu). */
    const val REMINDERS = "osp_reminders_channel"

    fun register(context: Context) {
        val nm = context.getSystemService<NotificationManager>() ?: return
        val sirenUri: Uri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.packageName + "/" + R.raw.siren
        )
        val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val alarmChannel = NotificationChannel(
            ALARM, "Alarmy bojowe", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Dyspozycje do akcji – dźwięk syreny i wibracja"
            enableLights(true)
            lightColor = 0xFFC8102E.toInt()
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 800, 300, 800, 300, 800, 300, 800)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            setSound(sirenUri, audioAttrs)
            setShowBadge(true)
        }

        val remindersChannel = NotificationChannel(
            REMINDERS, "Przypomnienia", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Terminy badań, przeglądów, wydarzeń w kalendarzu"
            enableVibration(true)
        }

        nm.createNotificationChannel(alarmChannel)
        nm.createNotificationChannel(remindersChannel)
    }
}
