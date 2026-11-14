package id.sch.mialirsyad.pkkm.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.sch.mialirsyad.pkkm.model.EvidenceItem
import id.sch.mialirsyad.pkkm.model.IndicatorItem
import id.sch.mialirsyad.pkkm.model.PkkmTab
import id.sch.mialirsyad.pkkm.model.naturalCompare
import java.util.Locale

private val Emerald50 = Color(0xFFECFDF5)
private val Emerald200 = Color(0xFFA7F3D0)
private val Emerald600 = Color(0xFF059669)
private val Emerald700 = Color(0xFF047857)
private val Slate50 = Color(0xFFF8FAFC)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate500 = Color(0xFF64748B)
private val Slate800 = Color(0xFF1E293B)
private val Slate900 = Color(0xFF0F172A)
private val Rose500 = Color(0xFFF43F5E)

@Composable
fun MatrixScreen(
    tab: PkkmTab,
    indicators: List<IndicatorItem>,
    evidence: List<EvidenceItem>,
    onUpdateScore: (String, Int) -> Unit,
    onSaveIndicator: (origCode: String?, subNo: String, rawCode: String, score: Int) -> Unit,
    onAddEvidenceTitle: (code: String, title: String) -> Unit,
    onSaveEvidenceFile: (id: String, fileName: String?, mimeType: String?, bytes: ByteArray?, url: String) -> Unit,
    onDeleteEvidence: (String) -> Unit
) {
    val menuId = tab.menuId ?: return
    val filteredInd = remember(indicators, menuId) {
        indicators.filter { it.menu_id == menuId }.sortedWith { a, b ->
            val subCmp = naturalCompare(a.sub_no, b.sub_no)
            if (subCmp != 0) subCmp else naturalCompare(a.indicator_code, b.indicator_code)
        }
    }

    val totalScore = filteredInd.sumOf { it.score }
    val avgScore = if (filteredInd.isNotEmpty()) {
        String.format(Locale.US, "%.2f", totalScore.toDouble() / filteredInd.size)
    } else "0.00"

    var showIndicatorDialog by remember { mutableStateOf(false) }
    var editingIndicator by remember { mutableStateOf<IndicatorItem?>(null) }
    var activeEvidenceCode by remember { mutableStateOf<String?>(null) }
    var activeUploadEvidenceId by remember { mutableStateOf<String?>(null) }

    val existingSubNos = remember(filteredInd) {
        filteredInd.map { it.sub_no.trim() }.filter { it.isNotEmpty() }.distinct()
            .sortedWith { a, b -> naturalCompare(a, b) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header & Score Summary
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Slate200)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tab.fullTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                Text(
                    "Kelola skor kinerja dan dokumen bukti fisik pendukung.",
                    fontSize = 12.sp,
                    color = Slate500
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            editingIndicator = null
                            showIndicatorDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Indikator", fontSize = 12.sp)
                    }

                    Surface(
                        color = Emerald50,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Emerald200)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Total Skor / Nilai", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Emerald700)
                            Text("$totalScore / $avgScore", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Emerald700)
                        }
                    }
                }
            }
        }

        // Indicators List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredInd, key = { it.indicator_code }) { ind ->
                val indEvidence = evidence.filter { it.indicator_code == ind.indicator_code }
                IndicatorCard(
                    indicator = ind,
                    evidenceList = indEvidence,
                    onScoreChange = { onUpdateScore(ind.indicator_code, it) },
                    onEdit = {
                        editingIndicator = ind
                        showIndicatorDialog = true
                    },
                    onManageEvidence = { activeEvidenceCode = ind.indicator_code },
                    onUploadFile = { evId -> activeUploadEvidenceId = evId },
                    onDeleteEvidence = onDeleteEvidence
                )
            }
        }
    }

    if (showIndicatorDialog) {
        IndicatorDialog(
            editing = editingIndicator,
            existingSubNos = existingSubNos,
            onDismiss = { showIndicatorDialog = false },
            onSave = { subNo, rawCode, score ->
                onSaveIndicator(editingIndicator?.indicator_code, subNo, rawCode, score)
                showIndicatorDialog = false
            }
        )
    }

    activeEvidenceCode?.let { code ->
        EvidenceDialog(
            indicatorCode = code,
            evidenceList = evidence.filter { it.indicator_code == code },
            onDismiss = { activeEvidenceCode = null },
            onAddTitle = { title -> onAddEvidenceTitle(code, title) },
            onUploadClick = { evId -> activeUploadEvidenceId = evId },
            onDelete = onDeleteEvidence
        )
    }

    activeUploadEvidenceId?.let { evId ->
        UploadFileDialog(
            onDismiss = { activeUploadEvidenceId = null },
            onSave = { fileName, mime, bytes, url ->
                onSaveEvidenceFile(evId, fileName, mime, bytes, url)
                activeUploadEvidenceId = null
            }
        )
    }
}

@Composable
private fun IndicatorCard(
    indicator: IndicatorItem,
    evidenceList: List<EvidenceItem>,
    onScoreChange: (Int) -> Unit,
    onEdit: () -> Unit,
    onManageEvidence: () -> Unit,
    onUploadFile: (String) -> Unit,
    onDeleteEvidence: (String) -> Unit
) {
    val context = LocalContext.current
    var scoreExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (indicator.sub_no.isNotBlank()) {
                        Surface(color = Slate50, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, Slate200)) {
                            Text(
                                "Sub ${indicator.sub_no}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        indicator.indicator_code,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Emerald700
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Slate500, modifier = Modifier.size(16.dp))
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = { scoreExpanded = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Skor: ${indicator.score}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                    }
                    DropdownMenu(expanded = scoreExpanded, onDismissRequest = { scoreExpanded = false }) {
                        listOf(4, 3, 2, 1).forEach { s ->
                            DropdownMenuItem(
                                text = { Text("$s") },
                                onClick = {
                                    scoreExpanded = false
                                    onScoreChange(s)
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Slate200)

            evidenceList.forEach { ev ->
                val hasFile = ev.file_url.isNotBlank() && ev.file_url != "#"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate50, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(enabled = hasFile) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(ev.file_url)))
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (hasFile) "${ev.title} ✓" else ev.title,
                            fontSize = 12.sp,
                            color = if (hasFile) Color(0xFF2563EB) else Slate500,
                            fontStyle = if (hasFile) FontStyle.Normal else FontStyle.Italic,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (hasFile) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(14.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { onUploadFile(ev.id) }) {
                            Text(
                                if (hasFile) "Ganti" else "+ Unggah",
                                fontSize = 11.sp,
                                color = Emerald700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(onClick = { onDeleteEvidence(ev.id) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Rose500, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            TextButton(onClick = onManageEvidence) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tambah Berkas Bukti Fisik", fontSize = 12.sp, color = Emerald600, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun IndicatorDialog(
    editing: IndicatorItem?,
    existingSubNos: List<String>,
    onDismiss: () -> Unit,
    onSave: (subNo: String, rawCode: String, score: Int) -> Unit
) {
    var subNo by remember { mutableStateOf(editing?.sub_no ?: "") }
    var codeInput by remember { mutableStateOf(editing?.indicator_code ?: "") }
    var score by remember { mutableIntStateOf(editing?.score ?: 4) }
    var subExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Tambah Indikator Baru" else "Edit Indikator", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (existingSubNos.isNotEmpty()) {
                        Box {
                            OutlinedButton(onClick = { subExpanded = true }) {
                                Text("Pilih Sub", fontSize = 12.sp)
                            }
                            DropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                                existingSubNos.forEach { s ->
                                    DropdownMenuItem(text = { Text(s) }, onClick = {
                                        subNo = s
                                        subExpanded = false
                                    })
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = subNo,
                        onValueChange = { subNo = it },
                        label = { Text("Nomor Sub (Mis: 4.1)", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = {
                        Text(
                            if (editing == null) "Kode Indikator (pisahkan koma/enter)" else "Kode Indikator",
                            fontSize = 12.sp
                        )
                    },
                    minLines = if (editing == null) 3 else 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Skor:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    listOf(4, 3, 2, 1).forEach { s ->
                        val selected = score == s
                        Surface(
                            color = if (selected) Emerald600 else Slate50,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, if (selected) Emerald600 else Slate200),
                            modifier = Modifier.clickable { score = s }
                        ) {
                            Text(
                                "$s",
                                color = if (selected) Color.White else Slate800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(subNo, codeInput, score) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}

@Composable
private fun EvidenceDialog(
    indicatorCode: String,
    evidenceList: List<EvidenceItem>,
    onDismiss: () -> Unit,
    onAddTitle: (String) -> Unit,
    onUploadClick: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Berkas Bukti ($indicatorCode)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Nama Dokumen (Mis: SK Tim)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            onAddTitle(titleInput)
                            titleInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Tambah Judul Saja", fontSize = 12.sp) }

                HorizontalDivider()

                Column(
                    modifier = Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (evidenceList.isEmpty()) {
                        Text("Belum ada dokumen bukti.", fontSize = 12.sp, color = Slate500, fontStyle = FontStyle.Italic)
                    } else {
                        evidenceList.forEach { ev ->
                            val hasFile = ev.file_url.isNotBlank() && ev.file_url != "#"
                            Row(
                                modifier = Modifier.fillMaxWidth().background(Slate50, RoundedCornerShape(6.dp)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ev.title, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(
                                        if (hasFile) "File terlampir" else "Belum ada file",
                                        fontSize = 10.sp,
                                        color = if (hasFile) Emerald600 else Slate500
                                    )
                                }
                                TextButton(onClick = { onUploadClick(ev.id) }) {
                                    Text(if (hasFile) "Ganti" else "Unggah", fontSize = 11.sp, color = Emerald700)
                                }
                                IconButton(onClick = { onDelete(ev.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Rose500, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Emerald600)) {
                Text("Selesai / Tutup")
            }
        }
    )
}

@Composable
private fun UploadFileDialog(
    onDismiss: () -> Unit,
    onSave: (fileName: String?, mimeType: String?, bytes: ByteArray?, url: String) -> Unit
) {
    val context = LocalContext.current
    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedMime by remember { mutableStateOf<String?>(null) }
    var selectedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var urlInput by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val cr = context.contentResolver
            selectedMime = cr.getType(uri)
            selectedName = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            } ?: "dokumen_${System.currentTimeMillis()}"
            selectedBytes = cr.openInputStream(uri)?.use { it.readBytes() }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Unggah File / Tautan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { picker.launch("*/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedName ?: "Pilih File dari HP", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Atau Tautan URL Eksternal (https://...)", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedName, selectedMime, selectedBytes, urlInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
            ) { Text("Simpan File") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )
}
