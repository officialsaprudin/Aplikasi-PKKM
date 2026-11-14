package id.sch.mialirsyad.pkkm.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.sch.mialirsyad.pkkm.model.IsianData

private val Emerald600 = Color(0xFF059669)
private val Slate900 = Color(0xFF0F172A)
private val Slate200 = Color(0xFFE2E8F0)

@Composable
fun IsianScreen(
    data: IsianData,
    onChange: (IsianData) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard(title = "A. Data Madrasah", icon = Icons.Default.Info) {
            Field("Nama Madrasah", data.m_nama) { onChange(data.copy(m_nama = it)) }
            Field("NPSN", data.m_npsn) { onChange(data.copy(m_npsn = it)) }
            Field("Alamat", data.m_alamat) { onChange(data.copy(m_alamat = it)) }
            Field("Telp / HP", data.m_telp) { onChange(data.copy(m_telp = it)) }
            Field("Email / Website", data.m_email) { onChange(data.copy(m_email = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Field("Kelurahan", data.m_kelurahan, Modifier.weight(1f)) {
                    onChange(data.copy(m_kelurahan = it))
                }
                Field("Kecamatan", data.m_kecamatan, Modifier.weight(1f)) {
                    onChange(data.copy(m_kecamatan = it))
                }
            }
            Field("Kabupaten / Kota", data.m_kabupaten) { onChange(data.copy(m_kabupaten = it)) }
            Field("Provinsi", data.m_provinsi) { onChange(data.copy(m_provinsi = it)) }
        }

        SectionCard(title = "B. Data Kepala Madrasah", icon = Icons.Default.Person) {
            Field("Nama Kepala Madrasah", data.km_nama) { onChange(data.copy(km_nama = it)) }
            Field("Jenis Kelamin", data.km_jk) { onChange(data.copy(km_jk = it)) }
            Field("Tempat / Tgl Lahir", data.km_ttl) { onChange(data.copy(km_ttl = it)) }
            Field("NIP", data.km_nip) { onChange(data.copy(km_nip = it)) }
            Field("Pangkat / Golongan", data.km_pangkat) { onChange(data.copy(km_pangkat = it)) }
            Field("TMT Kepala Madrasah", data.km_tmt) { onChange(data.copy(km_tmt = it)) }
            Field("Masa Kerja", data.km_masa) { onChange(data.copy(km_masa = it)) }
            Field("NUPTK", data.km_nuptk) { onChange(data.copy(km_nuptk = it)) }
            Field("Pendidikan Terakhir", data.km_pendidikan) { onChange(data.copy(km_pendidikan = it)) }
        }

        SectionCard(title = "C. Data Penilai & D. Waktu Penilaian", icon = Icons.Default.People) {
            Field("Nama Penilai I", data.p1_nama) { onChange(data.copy(p1_nama = it)) }
            Field("NIP Penilai I", data.p1_nip) { onChange(data.copy(p1_nip = it)) }
            Field("Nama Penilai II", data.p2_nama) { onChange(data.copy(p2_nama = it)) }
            Field("NIP Penilai II", data.p2_nip) { onChange(data.copy(p2_nip = it)) }
            Field("Jabatan Penilai", data.p_jabatan) { onChange(data.copy(p_jabatan = it)) }
            Field("Unit Kerja Penilai", data.p_unit) { onChange(data.copy(p_unit = it)) }
            Field("Tempat Penilaian", data.tp_tempat) { onChange(data.copy(tp_tempat = it)) }
            Field("Hari Penilaian", data.tp_hari) { onChange(data.copy(tp_hari = it)) }
        }

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Simpan Data ke Supabase", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = Emerald600)
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
            }
            HorizontalDivider(color = Slate200)
            content()
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodyMedium
    )
}
