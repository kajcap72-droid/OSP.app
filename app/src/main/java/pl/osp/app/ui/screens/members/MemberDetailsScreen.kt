package pl.osp.app.ui.screens.members

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pl.osp.app.data.model.Member
import pl.osp.app.data.repository.RemizaRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class MemberDetailsViewModel @Inject constructor(
    private val repo: RemizaRepository
) : ViewModel() {
    private val _m = MutableStateFlow<Member?>(null)
    val member: StateFlow<Member?> = _m
    fun load(id: String) = viewModelScope.launch { _m.value = repo.getMember(id) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailsScreen(
    memberId: String, onBack: () -> Unit,
    vm: MemberDetailsViewModel = hiltViewModel()
) {
    LaunchedEffect(memberId) { vm.load(memberId) }
    val m by vm.member.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(m?.fullName ?: "Druh") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
        val member = m ?: return@Scaffold
        val df = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(80.dp).clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(member.firstName.first().uppercase() + member.lastName.first().uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(member.fullName, style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text(member.rank.label, color = MaterialTheme.colorScheme.primary)
                    val years = ChronoUnit.YEARS.between(member.joinDate, LocalDate.now())
                    Text("Druh od $years lat", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconText(Icons.Default.Phone, member.phone)
                    member.email?.let { IconText(Icons.Default.Email, it) }
                    IconText(Icons.Default.Cake, "Ur. ${member.birthDate.format(df)}")
                    IconText(Icons.Default.Today, "W jednostce od ${member.joinDate.format(df)}")
                }
            }

            member.medicalExamExpiry?.let { exp ->
                val danger = exp.isBefore(LocalDate.now().plusDays(60))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (danger) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Badania lekarskie", fontWeight = FontWeight.SemiBold)
                        Text("Ważne do ${exp.format(df)}",
                            color = if (danger) Color(0xFFC62828) else Color.Unspecified)
                    }
                }
            }

            if (member.qualifications.isNotEmpty()) {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Uprawnienia / szkolenia", fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        member.qualifications.forEach { q ->
                            val expiring = q.isExpiringSoon()
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 6.dp)) {
                                Icon(
                                    if (expiring) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (expiring) Color(0xFFEF6C00) else Color(0xFF2E7D32)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(q.name)
                                    Text(
                                        q.expiresAt?.let { "Ważne do ${it.format(df)}" }
                                            ?: "Bezterminowe (od ${q.acquiredAt.format(df)})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}
