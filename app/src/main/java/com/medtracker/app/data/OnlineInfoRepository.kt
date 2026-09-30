package com.medtracker.app.data

import android.content.Context
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.online.DrugInfoClient
import com.medtracker.core.online.LookupException
import com.medtracker.core.online.OnlineDrugInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

enum class LookupStatus { IN_PROGRESS, FAILED }

/**
 * Fetches medicine information from online sources and caches it on the device, keyed by the
 * normalised item name. Only the medicine name is ever sent.
 */
class OnlineInfoRepository(context: Context, private val client: DrugInfoClient = DrugInfoClient()) {

    private val file = File(context.filesDir, FILE_NAME)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val fileLock = Mutex()

    private val _cache = MutableStateFlow(load())
    val cache: StateFlow<Map<String, OnlineDrugInfo>> = _cache.asStateFlow()

    private val _status = MutableStateFlow<Map<String, LookupStatus>>(emptyMap())
    val status: StateFlow<Map<String, LookupStatus>> = _status.asStateFlow()

    var enabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** Looks up any names that aren't cached (or are stale). With [force], always re-fetches. */
    suspend fun ensure(names: Collection<String>, force: Boolean = false) = withContext(Dispatchers.IO) {
        for (name in names.distinctBy(KnowledgeBase::normalize)) {
            val key = KnowledgeBase.normalize(name)
            if (key.isEmpty()) continue
            val existing = _cache.value[key]
            if (!force && existing != null && isFresh(existing)) continue
            if (!claim(key)) continue
            try {
                val info = client.lookup(name)
                _cache.update { it + (key to info) }
                persist()
                _status.update { it - key }
            } catch (e: LookupException) {
                _status.update { it + (key to LookupStatus.FAILED) }
            } catch (e: RuntimeException) {
                _status.update { it + (key to LookupStatus.FAILED) }
            }
        }
    }

    suspend fun clear() {
        _cache.value = emptyMap()
        _status.value = emptyMap()
        persist()
    }

    /** Marks a key as in progress; false if another lookup for it is already running. */
    private fun claim(key: String): Boolean {
        var claimed = false
        _status.update { current ->
            claimed = current[key] != LookupStatus.IN_PROGRESS
            if (claimed) current + (key to LookupStatus.IN_PROGRESS) else current
        }
        return claimed
    }

    private fun isFresh(info: OnlineDrugInfo): Boolean {
        val age = System.currentTimeMillis() - info.fetchedAtMillis
        val maxAge = if (info.found) TimeUnit.DAYS.toMillis(30) else TimeUnit.DAYS.toMillis(7)
        return age in 0..maxAge
    }

    private suspend fun persist() = withContext(Dispatchers.IO) {
        fileLock.withLock {
            val tmp = File(file.parentFile, "$FILE_NAME.tmp")
            tmp.writeText(OnlineDrugInfo.encodeCache(_cache.value))
            tmp.renameTo(file)
        }
    }

    private fun load(): Map<String, OnlineDrugInfo> =
        if (file.exists()) OnlineDrugInfo.decodeCache(runCatching { file.readText() }.getOrDefault("")) else emptyMap()

    companion object {
        private const val FILE_NAME = "online_cache.json"
        private const val PREFS = "settings"
        private const val KEY_ENABLED = "online_lookup_enabled"
    }
}
