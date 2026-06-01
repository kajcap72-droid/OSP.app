package pl.osp.app.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
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
import pl.osp.app.data.model.Member
import pl.osp.app.data.repository.RemizaRepository
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MembersViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val members = repo.observeMembers()
}

@Composable
fun MembersScreen(onMemberClick: (String) -> Unit, vm: MembersViewModel = hiltViewModel()) {
    val members by vm.members.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Szukaj druha...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filtered = members.filter { query.isBlank() ||
                it.fullName.contains(query, ignoreCase = true) }
            items(filtered, key = { it.id }) { m ->
                MemberRow(m, onClick = { onMemberClick(m.id) })
            }
        }
    }
}

@Composable
fun MemberRow(member: Member, onClick: () -> Unit) {
    val warnExpiring = member.medicalExamExpiry?.isBefore(LocalDate.now().plusDays(60)) == true ||
        member.qualifications.any { it.isExpiringSoon() }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    member.firstName.firstOrNull()?.uppercase().orEmpty() +
                        member.lastName.firstOrNull()?.uppercase().orEmpty(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(member.fullName, fontWeight = FontWeight.SemiBold)
                Text(member.rank.label, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (warnExpiring) {
                Icon(Icons.Default.Warning, contentDescription = "Kończą się terminy",
                    tint = Color(0xFFEF6C00))
                Spacer(Modifier.width(8.dp))
            }
            Icon(Icons.Default.Phone, contentDescription = "Telefon",
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}
