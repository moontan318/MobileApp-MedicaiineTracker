package com.medtracker.app.data

import android.content.Context
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Stores the user's regimen as a small JSON file in app-private storage. */
class RegimenRepository(context: Context) {

    private val file = File(context.filesDir, FILE_NAME)
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<RegimenItem>> = _items.asStateFlow()

    var disclaimerAccepted: Boolean
        get() = prefs.getBoolean(KEY_DISCLAIMER, false)
        set(value) = prefs.edit().putBoolean(KEY_DISCLAIMER, value).apply()

    suspend fun upsert(item: RegimenItem) {
        _items.update { list ->
            val index = list.indexOfFirst { it.id == item.id }
            if (index >= 0) list.toMutableList().also { it[index] = item } else list + item
        }
        persist()
    }

    suspend fun delete(id: String) {
        _items.update { list -> list.filterNot { it.id == id } }
        persist()
    }

    suspend fun updateTimes(changes: Map<String, List<DoseTime>>) {
        _items.update { list -> list.map { item -> changes[item.id]?.let { item.copy(times = it) } ?: item } }
        persist()
    }

    private suspend fun persist() = withContext(Dispatchers.IO) {
        val json = JSONArray()
        _items.value.forEach { json.put(it.toJson()) }
        val tmp = File(file.parentFile, "$FILE_NAME.tmp")
        tmp.writeText(json.toString())
        tmp.renameTo(file)
    }

    private fun load(): List<RegimenItem> = runCatching {
        if (!file.exists()) return emptyList()
        val array = JSONArray(file.readText())
        (0 until array.length()).mapNotNull { runCatching { array.getJSONObject(it).toItem() }.getOrNull() }
    }.getOrDefault(emptyList())

    private fun RegimenItem.toJson() = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("type", type.name)
        put("doseAmount", doseAmount)
        put("doseUnit", doseUnit.name)
        put("times", JSONArray(times.map { it.format() }))
        put("food", food.name)
        put("notes", notes)
    }

    private fun JSONObject.toItem(): RegimenItem {
        val timesJson = getJSONArray("times")
        return RegimenItem(
            id = getString("id"),
            name = getString("name"),
            type = enumValueOrDefault(optString("type"), ItemType.OTHER_SUPPLEMENT),
            doseAmount = getDouble("doseAmount"),
            doseUnit = enumValueOrDefault(optString("doseUnit"), DoseUnit.MG),
            times = (0 until timesJson.length()).mapNotNull { DoseTime.parse(timesJson.getString(it)) },
            food = enumValueOrDefault(optString("food"), FoodRelation.ANY),
            notes = optString("notes", ""),
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    companion object {
        private const val FILE_NAME = "regimen.json"
        private const val PREFS = "settings"
        private const val KEY_DISCLAIMER = "disclaimer_accepted"
    }
}
