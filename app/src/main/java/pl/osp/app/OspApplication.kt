package pl.osp.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration
import pl.osp.app.notifications.NotificationChannels

@HiltAndroidApp
class OspApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Rejestracja kanałów powiadomień (Android 8+)
        NotificationChannels.register(this)
        // Konfiguracja osmdroid (User-Agent dla cache OSM zgodnie z zasadami OSM)
        Configuration.getInstance().userAgentValue = packageName
    }
}
