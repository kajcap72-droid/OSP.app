package pl.osp.app.ui.screens.equipment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.osp.app.data.model.Equipment
import pl.osp.app.data.repository.RemizaRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class EquipmentViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val equipment = repo.observeEquipment()
}

@Composable
fun EquipmentScreen(vm: EquipmentViewModel = hiltViewModel()) {
    val list by vm.equipment.collectAsState(initial = emptyList())

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list, key = { it.id }) { eq -> EquipmentCard(eq) }
    }
}

@Composable
fun EquipmentCard(eq: Equipment) {
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val danger = !eq.operational || (eq.nextInspectionDate?.isBefore(LocalDate.now()) == true)
    val warn = eq.needsInspectionSoon()
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                danger -> Color(0xFFFFEBEE)
                warn -> Color(0xFFFFF3E0)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (danger || warn) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = when { danger -> Color(0xFFC62828); warn -> Color(0xFFEF6C00); else -> Color(0xFF2E7D32) }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(eq.name, fontWeight = FontWeight.SemiBold)
                Text(eq.category.label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
                eq.serialNumber?.let { Text("S/N: $it", style = MaterialTheme.typography.bodySmall) }
                eq.nextInspectionDate?.let {
                    Text("Następny przegląd: ${it.format(df)}",
                        style = MaterialTheme.typography.bodySmall)
                }
                eq.notes?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
