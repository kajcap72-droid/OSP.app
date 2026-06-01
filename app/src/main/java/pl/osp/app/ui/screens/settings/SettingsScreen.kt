package pl.osp.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.osp.app.data.repository.RemizaRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val user = repo.currentUser()
}

@Composable
fun SettingsScreen(onLogout: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val user by vm.user.collectAsState(initial = null)
    var pushAlarms by remember { mutableStateOf(true) }
    var pushEvents by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        // Profil
        Card(shape = RoundedCornerShape(16.dp)) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(user?.name?.split(" ")?.mapNotNull { it.firstOrNull()?.uppercase() }
                        ?.take(2)?.joinToString("") ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(user?.name ?: "—", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Text(user?.unit ?: "—", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    user?.rank?.label?.let { Text(it, color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium) }
                }
            }
        }

        // Powiadomienia
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(8.dp)) {
                Text("Powiadomienia", fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                SwitchRow(Icons.Default.Notifications, "Alarmy (dźwięk syreny)",
                    pushAlarms) { pushAlarms = it }
                SwitchRow(Icons.Default.Event, "Przypomnienia o wydarzeniach",
                    pushEvents) { pushEvents = it }
            }
        }

        // Wygląd
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(8.dp)) {
                Text("Wygląd", fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp))
                SwitchRow(Icons.Default.DarkMode, "Tryb ciemny",
                    darkMode) { darkMode = it }
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Wyloguj")
        }
        Text("OSP v0.1.0 • Aplikacja demonstracyjna",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
    }
}

@Composable
private fun SwitchRow(icon: ImageVector, label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
