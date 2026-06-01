package pl.osp.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Dest(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Login : Dest("login", "Logowanie")
    data object Alarms : Dest("alarms", "Alarmy", Icons.Default.Notifications)
    data object Members : Dest("members", "Druhowie", Icons.Default.Group)
    data object Calendar : Dest("calendar", "Kalendarz", Icons.Default.CalendarMonth)
    data object Equipment : Dest("equipment", "Sprzęt", Icons.Default.Build)
    data object Vehicles : Dest("vehicles", "Pojazdy", Icons.Default.LocalShipping)
    data object Documents : Dest("documents", "Dokumenty", Icons.Default.Folder)
    data object Stats : Dest("stats", "Statystyki", Icons.Default.QueryStats)
    data object Map : Dest("map", "Mapa", Icons.Default.Map)
    data object Settings : Dest("settings", "Ustawienia", Icons.Default.Settings)
    data object AlarmDetails : Dest("alarms/{id}", "Szczegóły alarmu") {
        fun create(id: String) = "alarms/$id"
    }
    data object MemberDetails : Dest("members/{id}", "Druh") {
        fun create(id: String) = "members/$id"
    }
}

val bottomBarDestinations = listOf(
    Dest.Alarms,
    Dest.Members,
    Dest.Calendar,
    Dest.Equipment,
    Dest.Settings
)

val drawerDestinations = listOf(
    Dest.Alarms,
    Dest.Members,
    Dest.Calendar,
    Dest.Equipment,
    Dest.Vehicles,
    Dest.Documents,
    Dest.Stats,
    Dest.Map,
    Dest.Settings
)
