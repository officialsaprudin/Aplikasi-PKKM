package id.sch.mialirsyad.pkkm.model

import org.json.JSONObject

enum class PkkmTab(val id: String, val shortLabel: String, val fullTitle: String, val menuId: Int?) {
    ISIAN("isian", "Isian", "ISIAN Data", null),
    USAHA("1", "Usaha", "1. USAHA PENGEMBANGAN MADRASAH", 1),
    MANAJERIAL("2", "Manajerial", "2. PELAKSANAAN TUGAS MANAJERIAL", 2),
    KEWIRAUSAHAAN("3", "Wirausaha", "3. PENGEMBANGAN KEWIRAUSAHAAN", 3),
    SUPERVISI("4", "Supervisi", "D. SUPERVISI KEPADA GURU DAN TENAGA KEPENDIDIKAN", 4)
}

data class IsianData(
    val m_nama: String = "MI AL IRSYAD AL ISLAMIYYAH KOTA BANJARMASIN",
    val m_npsn: String = "70035517",
    val m_telp: String = "-",
    val m_alamat: String = "JL. SULAWESI RT.16 NO.15",
    val m_kelurahan: String = "Pasar Lama",
    val m_kecamatan: String = "Banjarmasin Tengah",
    val m_kabupaten: String = "Kota Banjarmasin",
    val m_provinsi: String = "Kalimantan Selatan",
    val m_email: String = "mi.alirsyadkotabjm@gmail.com",
    val km_nama: String = "M. Rizky Nugraha AB.,S.H",
    val km_jk: String = "Laki-Laki",
    val km_ttl: String = "Banjarmasin, 04 Maret 1999",
    val km_nip: String = "-",
    val km_pangkat: String = "-",
    val km_tmt: String = "-",
    val km_masa: String = "1 Tahun 5 Bulan",
    val km_nuptk: String = "-",
    val km_pendidikan: String = "S1",
    val p1_nama: String = "Husna Mai Sa`adah.,S.Ag,M.A",
    val p1_nip: String = "19710506 199101 2 001",
    val p2_nama: String = "Salafudin Fitri.,S.Ag.,M.Pd",
    val p2_nip: String = "19701227 199402 1 001",
    val p_jabatan: String = "Pengawas",
    val p_pangkat: String = "-",
    val p_unit: String = "Kantor Kementerian Agama",
    val p_kabupaten: String = "Kota Banjarmasin",
    val p_provinsi: String = "Kalimantan Selatan",
    val tp_tempat: String = "",
    val tp_tanggal: String = "",
    val tp_hari: String = "Kamis",
    val tp_tahun: String = "Pertama"
) {
    fun toJson(includeId: Boolean = false): JSONObject = JSONObject().apply {
        if (includeId) put("id", 1)
        put("m_nama", m_nama)
        put("m_npsn", m_npsn)
        put("m_telp", m_telp)
        put("m_alamat", m_alamat)
        put("m_kelurahan", m_kelurahan)
        put("m_kecamatan", m_kecamatan)
        put("m_kabupaten", m_kabupaten)
        put("m_provinsi", m_provinsi)
        put("m_email", m_email)
        put("km_nama", km_nama)
        put("km_jk", km_jk)
        put("km_ttl", km_ttl)
        put("km_nip", km_nip)
        put("km_pangkat", km_pangkat)
        put("km_tmt", km_tmt)
        put("km_masa", km_masa)
        put("km_nuptk", km_nuptk)
        put("km_pendidikan", km_pendidikan)
        put("p1_nama", p1_nama)
        put("p1_nip", p1_nip)
        put("p2_nama", p2_nama)
        put("p2_nip", p2_nip)
        put("p_jabatan", p_jabatan)
        put("p_pangkat", p_pangkat)
        put("p_unit", p_unit)
        put("p_kabupaten", p_kabupaten)
        put("p_provinsi", p_provinsi)
        put("tp_tempat", tp_tempat)
        put("tp_tanggal", tp_tanggal)
        put("tp_hari", tp_hari)
        put("tp_tahun", tp_tahun)
    }

    companion object {
        fun fromJson(obj: JSONObject): IsianData {
            val d = IsianData()
            fun str(k: String, fallback: String) = if (obj.isNull(k)) fallback else obj.optString(k, fallback)
            return IsianData(
                m_nama = str("m_nama", d.m_nama),
                m_npsn = str("m_npsn", d.m_npsn),
                m_telp = str("m_telp", d.m_telp),
                m_alamat = str("m_alamat", d.m_alamat),
                m_kelurahan = str("m_kelurahan", d.m_kelurahan),
                m_kecamatan = str("m_kecamatan", d.m_kecamatan),
                m_kabupaten = str("m_kabupaten", d.m_kabupaten),
                m_provinsi = str("m_provinsi", d.m_provinsi),
                m_email = str("m_email", d.m_email),
                km_nama = str("km_nama", d.km_nama),
                km_jk = str("km_jk", d.km_jk),
                km_ttl = str("km_ttl", d.km_ttl),
                km_nip = str("km_nip", d.km_nip),
                km_pangkat = str("km_pangkat", d.km_pangkat),
                km_tmt = str("km_tmt", d.km_tmt),
                km_masa = str("km_masa", d.km_masa),
                km_nuptk = str("km_nuptk", d.km_nuptk),
                km_pendidikan = str("km_pendidikan", d.km_pendidikan),
                p1_nama = str("p1_nama", d.p1_nama),
                p1_nip = str("p1_nip", d.p1_nip),
                p2_nama = str("p2_nama", d.p2_nama),
                p2_nip = str("p2_nip", d.p2_nip),
                p_jabatan = str("p_jabatan", d.p_jabatan),
                p_pangkat = str("p_pangkat", d.p_pangkat),
                p_unit = str("p_unit", d.p_unit),
                p_kabupaten = str("p_kabupaten", d.p_kabupaten),
                p_provinsi = str("p_provinsi", d.p_provinsi),
                tp_tempat = str("tp_tempat", d.tp_tempat),
                tp_tanggal = str("tp_tanggal", d.tp_tanggal),
                tp_hari = str("tp_hari", d.tp_hari),
                tp_tahun = str("tp_tahun", d.tp_tahun)
            )
        }
    }
}

data class IndicatorItem(
    val indicator_code: String,
    val menu_id: Int,
    val sub_no: String,
    val score: Int
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("indicator_code", indicator_code)
        put("menu_id", menu_id)
        put("sub_no", sub_no)
        put("score", score)
    }

    companion object {
        fun fromJson(obj: JSONObject) = IndicatorItem(
            indicator_code = obj.optString("indicator_code", ""),
            menu_id = obj.optInt("menu_id", 1),
            sub_no = obj.optString("sub_no", ""),
            score = obj.optInt("score", 4)
        )
    }
}

data class EvidenceItem(
    val id: String,
    val indicator_code: String,
    val title: String,
    val file_url: String
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("indicator_code", indicator_code)
        put("title", title)
        put("file_url", file_url)
    }

    companion object {
        fun fromJson(obj: JSONObject) = EvidenceItem(
            id = obj.optString("id", ""),
            indicator_code = obj.optString("indicator_code", ""),
            title = obj.optString("title", ""),
            file_url = obj.optString("file_url", "#")
        )
    }
}

val defaultIndicators = listOf(
    IndicatorItem("1.1.1", 1, "1.1", 4),
    IndicatorItem("1.1.2", 1, "1.1", 4),
    IndicatorItem("1.2.1", 1, "1.2", 4),
    IndicatorItem("2.1.1", 2, "2.1", 4),
    IndicatorItem("2.1.2", 2, "2.1", 3),
    IndicatorItem("3.1.1", 3, "3.1", 4),
    IndicatorItem("4.1.1", 4, "4.1", 4),
    IndicatorItem("4.1.2", 4, "4.1", 4),
    IndicatorItem("4.1.3", 4, "4.1", 4),
    IndicatorItem("4.2.1", 4, "4.2", 4),
    IndicatorItem("4.2.2", 4, "4.2", 4),
    IndicatorItem("4.2.3", 4, "4.2", 4),
    IndicatorItem("4.2.4", 4, "4.2", 3),
    IndicatorItem("4.3.1", 4, "4.3", 4),
    IndicatorItem("4.3.2", 4, "4.3", 1),
    IndicatorItem("4.3.3", 4, "4.3", 3),
    IndicatorItem("4.3.4", 4, "4.3", 2)
)

val defaultEvidence = listOf(
    EvidenceItem("1", "4.1.1", "Program Pengawasan pembelajaran / supervisi", "#"),
    EvidenceItem("2", "4.1.1", "Jadwal pelaksanaan supervisi", "#"),
    EvidenceItem("3", "4.1.1", "SK tim supervisor", "#"),
    EvidenceItem("4", "4.1.2", "Program pengawasan & laporan hasil", "#"),
    EvidenceItem("5", "4.2.1", "Program Pengawasan Pembelajaran", "#"),
    EvidenceItem("6", "4.3.1", "Program remedial dan pengayaan", "#")
)

fun naturalCompare(a: String, b: String): Int {
    val partsA = Regex("\\d+|\\D+").findAll(a.trim()).map { it.value }.toList()
    val partsB = Regex("\\d+|\\D+").findAll(b.trim()).map { it.value }.toList()
    val len = minOf(partsA.size, partsB.size)
    for (i in 0 until len) {
        val pa = partsA[i]
        val pb = partsB[i]
        val na = pa.toLongOrNull()
        val nb = pb.toLongOrNull()
        val cmp = if (na != null && nb != null) na.compareTo(nb) else pa.compareTo(pb, ignoreCase = true)
        if (cmp != 0) return cmp
    }
    return partsA.size.compareTo(partsB.size)
}
