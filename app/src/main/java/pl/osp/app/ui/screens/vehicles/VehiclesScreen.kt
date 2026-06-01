package pl.osp.app.ui.screens.vehicles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.osp.app.data.model.Vehicle
import pl.osp.app.data.repository.RemizaRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class VehiclesViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val vehicles = repo.observeVehicles()
}

@Composable
fun VehiclesScreen(vm: VehiclesViewModel = hiltViewModel()) {
    val list by vm.vehicles.collectAsState(initial = emptyList())
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(list, key = { it.id }) { v -> VehicleCard(v) }
    }
}

@Composable
fun VehicleCard(v: Vehicle) {
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("🚒", style = MaterialTheme.typography.displaySmall)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(v.name, fontWeight = FontWeight.Bold)
                    Text("${v.model} • ${v.productionYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                AssistChip(onClick = {}, label = { Text(v.registration) })
            }
            Spacer(Modifier.height(10.dp))
            InfoLine("Przegląd techniczny", v.technicalInspectionExpiry.format(df))
            InfoLine("OC", v.insuranceExpiry.format(df))
            InfoLine("Przebieg", "${v.mileage} km")
            Spacer(Modifier.height(6.dp))
            Text("Paliwo: ${v.fuelLevel}%", style = MaterialTheme.typography.bodySmall)
            LinearProgressIndicator(
                progress = { v.fuelLevel / 100f },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
