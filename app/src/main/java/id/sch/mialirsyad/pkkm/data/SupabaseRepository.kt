package id.sch.mialirsyad.pkkm.data

import android.content.Context
import id.sch.mialirsyad.pkkm.model.EvidenceItem
import id.sch.mialirsyad.pkkm.model.IndicatorItem
import id.sch.mialirsyad.pkkm.model.IsianData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SupabaseRepository(context: Context) {

    companion object {
        private const val SUPABASE_URL = "https://qnliwpdpcvifnzvddkep.supabase.co"
        private const val SUPABASE_ANON_KEY = "sb_publishable_sYwTfi7cvdY8FD7qtJk-Lw_7LLbTJRe"
        private const val PREFS_NAME = "pkkm_cache"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun Request.Builder.authHeaders(): Request.Builder = this
        .header("apikey", SUPABASE_ANON_KEY)
        .header("Authorization", "Bearer $SUPABASE_ANON_KEY")

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    suspend fun fetchIsian(): IsianData? = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_isian?select=*&limit=1")
            .authHeaders()
            .header("Accept", "application/vnd.pgrst.object+json")
            .get()
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) {
                if (res.code == 406) return@withContext null // empty table
                throw IllegalStateException("HTTP ${res.code}")
            }
            val body = res.body?.string().orEmpty()
            if (body.isBlank()) null else IsianData.fromJson(JSONObject(body))
        }
    }

    suspend fun upsertIsian(data: IsianData) = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_isian")
            .authHeaders()
            .header("Prefer", "resolution=merge-duplicates")
            .post(data.toJson(includeId = true).toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}: ${res.body?.string()}")
        }
    }

    suspend fun fetchIndicators(): List<IndicatorItem> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_indicators?select=*")
            .authHeaders()
            .get()
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
            val arr = JSONArray(res.body?.string() ?: "[]")
            List(arr.length()) { i -> IndicatorItem.fromJson(arr.getJSONObject(i)) }
        }
    }

    suspend fun upsertIndicator(item: IndicatorItem) = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_indicators?on_conflict=indicator_code")
            .authHeaders()
            .header("Prefer", "resolution=merge-duplicates")
            .post(item.toJson().toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
        }
    }

    suspend fun insertIndicators(items: List<IndicatorItem>) = withContext(Dispatchers.IO) {
        val arr = JSONArray().apply { items.forEach { put(it.toJson()) } }
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_indicators")
            .authHeaders()
            .post(arr.toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
        }
    }

    suspend fun deleteIndicator(code: String) = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_indicators?indicator_code=eq.${encode(code)}")
            .authHeaders()
            .delete()
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
        }
    }

    suspend fun fetchEvidence(): List<EvidenceItem> = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_evidence?select=*")
            .authHeaders()
            .get()
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
            val arr = JSONArray(res.body?.string() ?: "[]")
            List(arr.length()) { i -> EvidenceItem.fromJson(arr.getJSONObject(i)) }
        }
    }

    suspend fun insertEvidence(code: String, title: String): EvidenceItem = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("indicator_code", code)
            put("title", title)
            put("file_url", "#")
        }
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_evidence")
            .authHeaders()
            .header("Prefer", "return=representation")
            .post(payload.toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
            val arr = JSONArray(res.body?.string() ?: "[]")
            if (arr.length() > 0) EvidenceItem.fromJson(arr.getJSONObject(0))
            else EvidenceItem(System.currentTimeMillis().toString(), code, title, "#")
        }
    }

    suspend fun uploadEvidenceFile(fileName: String, mimeType: String, bytes: ByteArray): String =
        withContext(Dispatchers.IO) {
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val filePath = "evidence/${System.currentTimeMillis()}_$safeName"
            val req = Request.Builder()
                .url("$SUPABASE_URL/storage/v1/object/pkkm-evidence/$filePath")
                .authHeaders()
                .post(bytes.toRequestBody(mimeType.toMediaType()))
                .build()
            client.newCall(req).execute().use { res ->
                if (!res.isSuccessful) throw IllegalStateException("Upload gagal (${res.code})")
            }
            "$SUPABASE_URL/storage/v1/object/public/pkkm-evidence/$filePath"
        }

    suspend fun updateEvidenceUrl(id: String, fileUrl: String) = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply { put("file_url", fileUrl) }
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_evidence?id=eq.${encode(id)}")
            .authHeaders()
            .patch(payload.toString().toRequestBody(jsonMediaType))
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
        }
    }

    suspend fun deleteEvidence(id: String) = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$SUPABASE_URL/rest/v1/pkkm_evidence?id=eq.${encode(id)}")
            .authHeaders()
            .delete()
            .build()
        client.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IllegalStateException("HTTP ${res.code}")
        }
    }

    // Local cache fallback
    fun saveLocalCache(isian: IsianData, indicators: List<IndicatorItem>, evidence: List<EvidenceItem>) {
        prefs.edit()
            .putString("isian", isian.toJson().toString())
            .putString("indicators", JSONArray().apply { indicators.forEach { put(it.toJson()) } }.toString())
            .putString("evidence", JSONArray().apply { evidence.forEach { put(it.toJson()) } }.toString())
            .apply()
    }

    fun loadCachedIsian(): IsianData? = prefs.getString("isian", null)?.let { IsianData.fromJson(JSONObject(it)) }

    fun loadCachedIndicators(): List<IndicatorItem>? = prefs.getString("indicators", null)?.let { raw ->
        val arr = JSONArray(raw)
        List(arr.length()) { i -> IndicatorItem.fromJson(arr.getJSONObject(i)) }
    }

    fun loadCachedEvidence(): List<EvidenceItem>? = prefs.getString("evidence", null)?.let { raw ->
        val arr = JSONArray(raw)
        List(arr.length()) { i -> EvidenceItem.fromJson(arr.getJSONObject(i)) }
    }
}
