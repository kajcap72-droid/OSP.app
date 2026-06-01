package pl.osp.app.ui.screens.alarms

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import pl.osp.app.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.osp.app.data.model.Alarm
import pl.osp.app.data.model.Disposition
import pl.osp.app.data.repository.RemizaRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AlarmDetailsViewModel @Inject constructor(
    private val repo: RemizaRepository
) : ViewModel() {
    private val _alarm = MutableStateFlow<Alarm?>(null)
    val alarm: StateFlow<Alarm?> = _alarm

    fun load(id: String) = viewModelScope.launch { _alarm.value = repo.getAlarm(id) }
    fun setDisposition(d: Disposition) = viewModelScope.launch {
        val id = _alarm.value?.id ?: return@launch
        repo.setMyDisposition(id, d)
        _alarm.value = repo.getAlarm(id)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDetailsScreen(
    alarmId: String,
    onBack: () -> Unit,
    vm: AlarmDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(alarmId) { vm.load(alarmId) }
    val alarm by vm.alarm.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły alarmu") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        val a = alarm
        if (a == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(a.type.icon, style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(a.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(a.type.displayName, color = MaterialTheme.colorScheme.primary)
                }
            }

            InfoRow(Icons.Default.LocationOn, a.address)
            InfoRow(Icons.Default.Schedule, a.dispatchedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))

            Text(a.description, style = MaterialTheme.typography.bodyMedium)

            // Mini-mapa lokalizacji zdarzenia (jeśli ma współrzędne)
            if (a.latitude != null && a.longitude != null) {
                val ctx = LocalContext.current
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            factory = { mapCtx ->
                                MapView(mapCtx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                                    controller.setZoom(15.0)
                                    val point = GeoPoint(a.latitude, a.longitude)
                                    controller.setCenter(point)
                                    overlays += Marker(this).apply {
                                        position = point
                                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                        icon = ContextCompat.getDrawable(mapCtx, R.drawable.ic_fire_pin)
                                        title = a.title
                                    }
                                }
                            }
                        )
                        Button(
                            onClick = {
                                val uri = Uri.parse(
                                    "geo:${a.latitude},${a.longitude}" +
                                        "?q=${a.latitude},${a.longitude}(${Uri.encode(a.title)})"
                                )
                                ctx.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_VIEW, uri),
                                        "Nawiguj do zdarzenia"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Nawiguj do zdarzenia")
                        }
                    }
                }
            }

            // Dyspozycja użytkownika
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Twoja dyspozycja", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DispositionButton("Jadę", Icons.Default.Check, Color(0xFF2E7D32),
                            selected = a.myDisposition == Disposition.GOING,
                            onClick = { vm.setDisposition(Disposition.GOING) }, modifier = Modifier.weight(1f))
                        DispositionButton("Później", Icons.Default.Schedule, Color(0xFFEF6C00),
                            selected = a.myDisposition == Disposition.LATER,
                            onClick = { vm.setDisposition(Disposition.LATER) }, modifier = Modifier.weight(1f))
                        DispositionButton("Nie", Icons.Default.Close, Color(0xFFC62828),
                            selected = a.myDisposition == Disposition.NOT_GOING,
                            onClick = { vm.setDisposition(Disposition.NOT_GOING) }, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Pojazdy zadysponowane
            if (a.vehicles.isNotEmpty()) {
                SectionCard(title = "Zadysponowane pojazdy") {
                    a.vehicles.forEach {
                        Text("🚒  $it", modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }

            // Lista reagujących
            if (a.responders.isNotEmpty()) {
                SectionCard(title = "Zadeklarowani druhowie (${a.responders.count { it.disposition == Disposition.GOING }} jedzie)") {
                    a.responders.forEach { r ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)) {
                            Box(
                                Modifier.size(10.dp)
                                    .background(
                                        when (r.disposition) {
                                            Disposition.GOING -> Color(0xFF2E7D32)
                                            Disposition.LATER -> Color(0xFFEF6C00)
                                            Disposition.NOT_GOING -> Color(0xFFC62828)
                                            Disposition.NONE -> Color.Gray
                                        },
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(r.memberName, modifier = Modifier.weight(1f))
                            Text(r.disposition.label, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Czas trwania
            SectionCard(title = "Czas akcji") {
                Text(durationLabel(a.dispatchedAt, a.finishedAt))
            }
        }
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DispositionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) color else color.copy(alpha = 0.12f)
    val fg = if (selected) Color.White else color
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(4.dp))
        Text(label, maxLines = 1)
    }
}
