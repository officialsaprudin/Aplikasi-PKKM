package id.sch.mialirsyad.pkkm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import id.sch.mialirsyad.pkkm.data.SupabaseRepository
import id.sch.mialirsyad.pkkm.model.EvidenceItem
import id.sch.mialirsyad.pkkm.model.IndicatorItem
import id.sch.mialirsyad.pkkm.model.IsianData
import id.sch.mialirsyad.pkkm.model.PkkmTab
import id.sch.mialirsyad.pkkm.model.defaultEvidence
import id.sch.mialirsyad.pkkm.model.defaultIndicators
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PkkmViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SupabaseRepository(app)

    private val _currentTab = MutableStateFlow(PkkmTab.ISIAN)
    val currentTab: StateFlow<PkkmTab> = _currentTab.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(true)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    private val _isianData = MutableStateFlow(IsianData())
    val isianData: StateFlow<IsianData> = _isianData.asStateFlow()

    private val _indicators = MutableStateFlow<List<IndicatorItem>>(emptyList())
    val indicators: StateFlow<List<IndicatorItem>> = _indicators.asStateFlow()

    private val _evidence = MutableStateFlow<List<EvidenceItem>>(emptyList())
    val evidence: StateFlow<List<EvidenceItem>> = _evidence.asStateFlow()

    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    init {
        loadData()
    }

    fun selectTab(tab: PkkmTab) {
        _currentTab.value = tab
    }

    fun updateIsian(updated: IsianData) {
        _isianData.value = updated
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isian = repo.fetchIsian()
                if (isian != null) _isianData.value = isian

                val ind = repo.fetchIndicators()
                _indicators.value = if (ind.isNotEmpty()) ind else defaultIndicators

                val ev = repo.fetchEvidence()
                _evidence.value = if (ev.isNotEmpty()) ev else defaultEvidence

                _isOnlineMode.value = true
                repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
            } catch (_: Exception) {
                _isOnlineMode.value = false
                _isianData.value = repo.loadCachedIsian() ?: IsianData()
                _indicators.value = repo.loadCachedIndicators() ?: defaultIndicators
                _evidence.value = repo.loadCachedEvidence() ?: defaultEvidence
                _messages.emit("Gagal memuat dari Supabase, beralih ke cache lokal.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveIsianData() {
        viewModelScope.launch {
            repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
            try {
                repo.upsertIsian(_isianData.value)
                _isOnlineMode.value = true
                _messages.emit("Data berhasil disimpan ke Supabase Cloud!")
            } catch (_: Exception) {
                _messages.emit("Gagal menyimpan ke server Supabase (tersimpan lokal)")
            }
        }
    }

    fun updateScore(code: String, newScore: Int) {
        val current = _indicators.value.toMutableList()
        val idx = current.indexOfFirst { it.indicator_code == code }
        if (idx == -1) return
        val updated = current[idx].copy(score = newScore)
        current[idx] = updated
        _indicators.value = current
        repo.saveLocalCache(_isianData.value, current, _evidence.value)

        viewModelScope.launch {
            try {
                repo.upsertIndicator(updated)
            } catch (_: Exception) {
                _messages.emit("Gagal memperbarui skor di Supabase")
            }
        }
    }

    fun saveIndicator(origCode: String?, subNo: String, rawCodeInput: String, score: Int) {
        val menuId = _currentTab.value.menuId ?: return
        val trimmedCode = rawCodeInput.trim()
        if (trimmedCode.isEmpty()) {
            viewModelScope.launch { _messages.emit("Kode indikator wajib diisi!") }
            return
        }

        viewModelScope.launch {
            if (!origCode.isNullOrEmpty()) {
                val updatedInd = IndicatorItem(trimmedCode, menuId, subNo.trim(), score)
                _indicators.value = _indicators.value.map {
                    if (it.indicator_code == origCode) updatedInd else it
                }
                _evidence.value = _evidence.value.map {
                    if (it.indicator_code == origCode) it.copy(indicator_code = trimmedCode) else it
                }
                repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
                try {
                    repo.deleteIndicator(origCode)
                    repo.insertIndicators(listOf(updatedInd))
                    _messages.emit("Indikator berhasil diperbarui!")
                } catch (_: Exception) {
                    _messages.emit("Gagal memperbarui indikator di Supabase")
                }
            } else {
                val codes = trimmedCode.split(Regex("[\n,;]+"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                if (codes.isEmpty()) {
                    _messages.emit("Kode indikator tidak valid!")
                    return@launch
                }

                val existing = _indicators.value.map { it.indicator_code }.toSet()
                val newEntries = mutableListOf<IndicatorItem>()
                var skipped = 0
                for (c in codes) {
                    if (c in existing || newEntries.any { it.indicator_code == c }) {
                        skipped++
                    } else {
                        newEntries.add(IndicatorItem(c, menuId, subNo.trim(), score))
                    }
                }

                if (newEntries.isEmpty()) {
                    _messages.emit("Semua indikator yang dimasukkan sudah ada sebelumnya!")
                    return@launch
                }

                _indicators.value = _indicators.value + newEntries
                repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
                try {
                    repo.insertIndicators(newEntries)
                    val extra = if (skipped > 0) " ($skipped dilewati karena sudah ada)" else ""
                    _messages.emit("${newEntries.size} indikator berhasil ditambahkan!$extra")
                } catch (_: Exception) {
                    _messages.emit("Gagal menambah indikator ke Supabase")
                }
            }
        }
    }

    fun addEvidenceTitle(indicatorCode: String, title: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) {
            viewModelScope.launch { _messages.emit("Judul dokumen wajib diisi!") }
            return
        }
        viewModelScope.launch {
            try {
                val inserted = repo.insertEvidence(indicatorCode, cleanTitle)
                _evidence.value = _evidence.value + inserted
                repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
                _messages.emit("Judul berkas berhasil ditambahkan!")
            } catch (_: Exception) {
                val local = EvidenceItem(System.currentTimeMillis().toString(), indicatorCode, cleanTitle, "#")
                _evidence.value = _evidence.value + local
                repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
                _messages.emit("Ditambahkan secara lokal (gagal sinkron Supabase)")
            }
        }
    }

    fun saveEvidenceFileOrUrl(
        evidenceId: String,
        fileName: String?,
        mimeType: String?,
        fileBytes: ByteArray?,
        externalUrl: String
    ) {
        viewModelScope.launch {
            try {
                val finalUrl = when {
                    fileBytes != null && !fileName.isNullOrBlank() -> {
                        repo.uploadEvidenceFile(fileName, mimeType ?: "application/octet-stream", fileBytes)
                    }
                    externalUrl.isNotBlank() -> externalUrl.trim()
                    else -> {
                        _messages.emit("Pilih file atau masukkan URL tautan!")
                        return@launch
                    }
                }
                _evidence.value = _evidence.value.map {
                    if (it.id == evidenceId) it.copy(file_url = finalUrl) else it
                }
                repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
                repo.updateEvidenceUrl(evidenceId, finalUrl)
                _messages.emit("File bukti berhasil disimpan!")
            } catch (e: Exception) {
                _messages.emit("Gagal mengunggah file: ${e.message ?: "Error"}")
            }
        }
    }

    fun deleteEvidence(id: String) {
        _evidence.value = _evidence.value.filterNot { it.id == id }
        repo.saveLocalCache(_isianData.value, _indicators.value, _evidence.value)
        viewModelScope.launch {
            try {
                repo.deleteEvidence(id)
                _messages.emit("Berkas bukti dihapus.")
            } catch (_: Exception) {
                _messages.emit("Berkas dihapus lokal (gagal hapus di Supabase)")
            }
        }
    }
}
