package pl.osp.app.ui.screens.documents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.osp.app.data.repository.RemizaRepository
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(repo: RemizaRepository) : ViewModel() {
    val docs = repo.observeDocuments()
}

@Composable
fun DocumentsScreen(vm: DocumentsViewModel = hiltViewModel()) {
    val docs by vm.docs.collectAsState(initial = emptyList())
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(docs, key = { it.id }) { d ->
            Card(shape = RoundedCornerShape(12.dp)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(d.name, fontWeight = FontWeight.SemiBold)
                        Text("${d.category} • ${"%.1f".format(d.sizeBytes / 1024.0)} KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(d.uploadedAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
