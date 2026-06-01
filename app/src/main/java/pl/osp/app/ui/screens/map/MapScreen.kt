package pl.osp.app.ui.screens.map

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.osp.app.R
import pl.osp.app.data.model.Alarm
import pl.osp.app.data.model.AlarmStatus
import pl.osp.app.data.model.AlarmType
import pl.osp.app.data.repository.RemizaRepository
import pl.osp.app.util.StationLocation
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val alarms = repo.observeAlarms()
}

@Composable
fun MapScreen(vm: MapViewModel = hiltViewModel()) {
    val alarms by vm.alarms.collectAsState(initial = emptyList())
    val withGeo = alarms.filter { it.latitude != null && it.longitude != null }
    val context = LocalContext.current
    var selectedAlarmId by remember { mutableStateOf<String?>(null) }
    val selected = withGeo.find { it.id == selectedAlarmId }
    var mapRef by remember { mutableStateOf<MapView?>(null) }

    // Konfiguracja osmdroid (User-Agent + cache)
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(
            context, PreferenceManager.getDefaultSharedPreferences(context)
        )
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).also { mapRef = it }.apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    controller.setZoom(13.0)
                    controller.setCenter(GeoPoint(StationLocation.LAT, StationLocation.LON))

                    // Pin remizy
                    overlays += Marker(this).apply {
                        position = GeoPoint(StationLocation.LAT, StationLocation.LON)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = ContextCompat.getDrawable(ctx, R.drawable.ic_station_pin)
                        title = StationLocation.NAME
                        snippet = "Tutaj jest twoja jednostka"
                    }
                }
            },
            update = { map ->
                // Wyczyść stare pinki alarmów (pierwszy overlay = remiza, zostawiamy)
                val keep = map.overlays.firstOrNull()
                map.overlays.clear()
                keep?.let { map.overlays.add(it) }

                withGeo.forEach { alarm ->
                    val marker = Marker(map).apply {
                        position = GeoPoint(alarm.latitude!!, alarm.longitude!!)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = pinForType(map.context, alarm.type)
                        title = "${alarm.type.icon} ${alarm.title}"
                        snippet = alarm.address
                        setOnMarkerClickListener { m, _ ->
                            selectedAlarmId = alarm.id
                            map.controller.animateTo(m.position)
                            true
                        }
                    }
                    map.overlays += marker
                }
                map.invalidate()
            }
        )

        // Legenda u góry
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            shadowElevation = 4.dp
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(legendItems()) { (label, color) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                        Spacer(Modifier.width(4.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Floating action: wycentruj na remizę
        FloatingActionButton(
            onClick = {
                mapRef?.controller?.animateTo(GeoPoint(StationLocation.LAT, StationLocation.LON))
                mapRef?.controller?.setZoom(14.0)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = if (selected != null) 200.dp else 16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Wycentruj")
        }

        // Karta szczegółów wybranego alarmu (slide-in od dołu)
        if (selected != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selected.type.icon, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(selected.title, fontWeight = FontWeight.Bold)
                            Text(selected.address, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { selectedAlarmId = null }) { Text("✕") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val uri = Uri.parse(
                                    "geo:${selected.latitude},${selected.longitude}" +
                                        "?q=${selected.latitude},${selected.longitude}" +
                                        "(${Uri.encode(selected.title)})"
                                )
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(Intent.createChooser(intent, "Nawiguj do zdarzenia"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Nawiguj")
                        }
                        OutlinedButton(
                            onClick = { selectedAlarmId = null },
                            modifier = Modifier.weight(1f)
                        ) { Text("Zamknij") }
                    }
                }
            }
        }
    }
}

private fun pinForType(ctx: android.content.Context, type: AlarmType): Drawable? {
    val resId = when (type) {
        AlarmType.FIRE -> R.drawable.ic_fire_pin
        AlarmType.LOCAL -> R.drawable.ic_hazard_pin
        AlarmType.EXERCISE, AlarmType.FALSE_ALARM, AlarmType.OTHER -> R.drawable.ic_hazard_pin
    }
    return ContextCompat.getDrawable(ctx, resId)
}

private fun legendItems(): List<Pair<String, Color>> = listOf(
    "🚒 Remiza" to Color(0xFF1565C0),
    "🔥 Pożar" to Color(0xFFC8102E),
    "⚠️ Zagrożenie" to Color(0xFFEF6C00),
    "✓ Zakończone" to Color(0xFF2E7D32)
)

@Suppress("UNUSED_PARAMETER")
private fun unused(status: AlarmStatus) {} // unikamy ostrzeżenia o nieużywanym imporcie
