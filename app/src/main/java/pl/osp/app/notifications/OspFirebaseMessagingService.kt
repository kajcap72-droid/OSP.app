package pl.osp.app.notifications

/* =====================================================================
 * KLASA WYŁĄCZONA Z BUILDU — wymaga zależności Firebase.
 *
 * Jak włączyć obsługę powiadomień push z chmury:
 *
 *  1. W Firebase Console utwórz projekt, dodaj aplikację Android
 *     z packageName = pl.osp.app, pobierz google-services.json
 *     i wrzuć do katalogu OSP/app/
 *
 *  2. W OSP/build.gradle.kts dodaj plugin classpath:
 *     id("com.google.gms.google-services") version "4.4.2" apply false
 *
 *  3. W OSP/app/build.gradle.kts:
 *     plugins { id("com.google.gms.google-services") }
 *     dependencies {
 *         implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
 *         implementation("com.google.firebase:firebase-messaging-ktx")
 *     }
 *
 *  4. W AndroidManifest.xml odkomentuj <service ... OspFirebaseMessagingService>
 *
 *  5. Zmień nazwę tego pliku na OspFirebaseMessagingService.kt i podmień
 *     poniższy szkielet (zakomentowany) na właściwą implementację.
 *
 * ===================================================================== */

/*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import pl.osp.app.data.model.*
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class OspFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notifier: AlarmNotifier

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["type"] != "alarm") return
        val alarm = Alarm(
            id = data["id"].orEmpty(),
            type = runCatching { AlarmType.valueOf(data["alarmType"].orEmpty()) }
                .getOrDefault(AlarmType.OTHER),
            title = data["title"].orEmpty(),
            description = data["description"].orEmpty(),
            address = data["address"].orEmpty(),
            latitude = data["lat"]?.toDoubleOrNull(),
            longitude = data["lon"]?.toDoubleOrNull(),
            dispatchedAt = LocalDateTime.now(),
            finishedAt = null,
            status = AlarmStatus.ACTIVE
        )
        notifier.showAlarm(alarm)
    }

    override fun onNewToken(token: String) {
        // TODO: wyślij token na backend, aby serwer mógł wysyłać push do tego urządzenia
    }
}
*/
