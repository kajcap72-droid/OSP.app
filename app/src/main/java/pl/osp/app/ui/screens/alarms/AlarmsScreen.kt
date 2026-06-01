package pl.osp.app.ui.screens.alarms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.osp.app.data.model.Alarm
import pl.osp.app.data.model.AlarmStatus
import pl.osp.app.data.repository.MockRemizaRepository
import pl.osp.app.data.repository.RemizaRepository
import pl.osp.app.notifications.AlarmNotifier
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AlarmsViewModel @Inject constructor(
    private val repo: RemizaRepository,
    private val notifier: AlarmNotifier
) : ViewModel() {
    val alarms = repo.observeAlarms()

    /**
     * Symulacja alarmu: dodaje nowy alarm do repozytorium z opóźnieniem
     * 1,5 s (żebyś zdążył zminimalizować apkę i zobaczyć powiadomienie),
     * a następnie pokazuje powiadomienie z dźwiękiem syreny.
     */
    fun simulateAlarm() {
        val mock = repo as? MockRemizaRepository ?: return
        viewModelScope.launch {
            delay(1500)
            val alarm = mock.pushFakeAlarm()
            notifier.showAlarm(alarm)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    onAlarmClick: (String) -> Unit,
    vm: AlarmsViewModel = hiltViewModel()
) {
    val alarms by vm.alarms.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Prośba o uprawnienie POST_NOTIFICATIONS (Android 13+)
    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.simulateAlarm()
            scope.launch {
                snackbarHostState.showSnackbar(
                    "🚨 Alarm zostanie wysłany za 1,5 s — możesz zminimalizować apkę"
                )
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    "Bez uprawnienia nie pokażę powiadomienia"
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    val needsPerm = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    if (needsPerm) {
                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.simulateAlarm()
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "🚨 Alarm za 1,5 s — zminimalizuj apkę aby zobaczyć powiadomienie"
                            )
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) },
                text = { Text("Symuluj alarm") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(alarm = alarm, onClick = { onAlarmClick(alarm.id) })
            }
        }
    }
}

@Composable
fun AlarmCard(alarm: Alarm, onClick: () -> Unit) {
    val statusColor = when (alarm.status) {
        AlarmStatus.ACTIVE, AlarmStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        AlarmStatus.FINISHED -> Color(0xFF2E7D32)
        AlarmStatus.CANCELLED -> Color.Gray
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(alarm.type.icon, style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(alarm.title, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(alarm.address, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(alarm.type.displayName, style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = statusColor.copy(alpha = 0.12f),
                            labelColor = statusColor
                        )
                    )
                    Text("• ${alarm.dispatchedAt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (alarm.status != AlarmStatus.FINISHED) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(statusColor))
            }
        }
    }
}

fun durationLabel(start: LocalDateTime, end: LocalDateTime?): String {
    val d = Duration.between(start, end ?: LocalDateTime.now())
    val h = d.toHours()
    val m = d.toMinutes() % 60
    return if (h > 0) "${h}h ${m}min" else "${m}min"
}
