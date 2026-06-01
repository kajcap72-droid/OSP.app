package pl.osp.app.ui.screens.duty

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.osp.app.data.model.CalendarEvent
import pl.osp.app.data.model.EventType
import pl.osp.app.data.repository.RemizaRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val events = repo.observeEvents()
}

@Composable
fun CalendarScreen(vm: CalendarViewModel = hiltViewModel()) {
    val events by vm.events.collectAsState(initial = emptyList())

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events.sortedBy { it.startAt }, key = { it.id }) { ev ->
            EventCard(ev)
        }
    }
}

@Composable
fun EventCard(ev: CalendarEvent) {
    val df = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    val color = when (ev.type) {
        EventType.DUTY -> Color(0xFF1565C0)
        EventType.TRAINING -> Color(0xFF6A1B9A)
        EventType.MEETING -> Color(0xFF00838F)
        EventType.COMPETITION -> Color(0xFFEF6C00)
        EventType.MAINTENANCE -> Color(0xFF558B2F)
        EventType.SOCIAL -> Color(0xFFC62828)
    }
    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(width = 6.dp, height = 56.dp)
                    .clip(RoundedCornerShape(3.dp)).background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ev.title, fontWeight = FontWeight.SemiBold)
                Text(ev.type.label, color = color, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Text("${ev.startAt.format(df)} – ${ev.endAt.format(df)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                ev.location?.let {
                    Text("📍 $it", style = MaterialTheme.typography.bodySmall)
                }
                if (ev.assignedMemberIds.isNotEmpty()) {
                    Text("👥 ${ev.assignedMemberIds.size} przypisanych druhów",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
