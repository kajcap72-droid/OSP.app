package pl.osp.app.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import pl.osp.app.data.model.YearStats
import pl.osp.app.data.repository.RemizaRepository
import java.time.Year
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(private val repo: RemizaRepository) : ViewModel() {
    private val _stats = MutableStateFlow<YearStats?>(null)
    val stats: StateFlow<YearStats?> = _stats
    init {
        viewModelScope.launch { _stats.value = repo.getYearStats(Year.now().value) }
    }
}

@Composable
fun StatsScreen(vm: StatsViewModel = hiltViewModel()) {
    val s by vm.stats.collectAsState()
    if (s == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val st = s!!
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text("Statystyki ${st.year}", style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            BigStatCard(modifier = Modifier.weight(1f), title = "Wszystkich",
                value = st.totalAlarms.toString(), color = MaterialTheme.colorScheme.primary)
            BigStatCard(modifier = Modifier.weight(1f), title = "Śr. czas",
                value = "${st.averageResponseSeconds / 60}m ${st.averageResponseSeconds % 60}s",
                color = Color(0xFF6A1B9A))
        }

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Podział na typy", fontWeight = FontWeight.SemiBold)
                BarRow("🔥 Pożary", st.fires, st.totalAlarms, Color(0xFFC62828))
                BarRow("⚠️ Miejscowe zagrożenia", st.localThreats, st.totalAlarms, Color(0xFFEF6C00))
                BarRow("❓ Fałszywe alarmy", st.falseAlarms, st.totalAlarms, Color(0xFF757575))
                BarRow("🎯 Ćwiczenia", st.exercises, st.totalAlarms, Color(0xFF1565C0))
            }
        }

        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(16.dp)) {
                Text("TOP druhowie (liczba wyjazdów)",
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                st.topResponders.forEachIndexed { idx, (name, n) ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Text("${idx + 1}.", modifier = Modifier.width(28.dp),
                            color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(name, modifier = Modifier.weight(1f))
                        Text("$n", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BigStatCard(modifier: Modifier, title: String, value: String, color: Color) {
    Card(shape = RoundedCornerShape(18.dp), modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))) {
        Column(Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black, color = color)
            Text(title, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BarRow(label: String, value: Int, total: Int, color: Color) {
    val fraction = if (total == 0) 0f else value / total.toFloat()
    Column {
        Row {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            Text("$value", fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodySmall)
        }
        Box(
            Modifier.fillMaxWidth().height(6.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
        ) {
            Box(
                Modifier.fillMaxWidth(fraction).height(6.dp)
                    .background(color, RoundedCornerShape(3.dp))
            )
        }
    }
}
