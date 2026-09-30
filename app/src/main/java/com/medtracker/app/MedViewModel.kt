package com.medtracker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medtracker.app.data.LookupStatus
import com.medtracker.app.data.OnlineInfoRepository
import com.medtracker.app.data.RegimenRepository
import com.medtracker.core.analysis.AnalysisResult
import com.medtracker.core.analysis.RegimenAnalyzer
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.model.RegimenItem
import com.medtracker.core.online.OnlineDrugInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RegimenRepository(application)
    private val onlineRepository = OnlineInfoRepository(application)
    val knowledgeBase = KnowledgeBase()
    private val analyzer = RegimenAnalyzer(knowledgeBase)

    val items: StateFlow<List<RegimenItem>> = repository.items

    private val _onlineEnabled = MutableStateFlow(onlineRepository.enabled)
    val onlineEnabled: StateFlow<Boolean> = _onlineEnabled.asStateFlow()

    val onlineInfo: StateFlow<Map<String, OnlineDrugInfo>> = onlineRepository.cache
    val lookupStatus: StateFlow<Map<String, LookupStatus>> = onlineRepository.status

    val analysis: StateFlow<AnalysisResult> =
        combine(repository.items, onlineRepository.cache, _onlineEnabled) { items, online, enabled ->
            analyzer.analyze(items, if (enabled) online else emptyMap())
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.Eagerly, AnalysisResult.EMPTY)

    private val _disclaimerAccepted = MutableStateFlow(repository.disclaimerAccepted)
    val disclaimerAccepted: StateFlow<Boolean> = _disclaimerAccepted.asStateFlow()

    init {
        // Look up new or changed item names whenever the list changes (if enabled).
        viewModelScope.launch {
            combine(repository.items, _onlineEnabled) { items, enabled -> if (enabled) items.map { it.name } else emptyList() }
                .collect { names -> if (names.isNotEmpty()) launch { onlineRepository.ensure(names) } }
        }
    }

    fun acceptDisclaimer() {
        repository.disclaimerAccepted = true
        _disclaimerAccepted.value = true
    }

    fun setOnlineEnabled(enabled: Boolean) {
        onlineRepository.enabled = enabled
        _onlineEnabled.value = enabled
    }

    fun refreshLookup(name: String) {
        if (!_onlineEnabled.value) return
        viewModelScope.launch { onlineRepository.ensure(listOf(name), force = true) }
    }

    fun clearOnlineCache() {
        viewModelScope.launch { onlineRepository.clear() }
    }

    fun item(id: String): RegimenItem? = items.value.firstOrNull { it.id == id }

    fun save(item: RegimenItem) {
        viewModelScope.launch { repository.upsert(item) }
    }

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }

    /** Applies suggested times for the given items (or all suggestions when [itemIds] is null). */
    fun applySuggestions(itemIds: Set<String>? = null) {
        val changes = analysis.value.scheduleChanges
            .filter { itemIds == null || it.itemId in itemIds }
            .associate { it.itemId to it.suggestedTimes }
        if (changes.isEmpty()) return
        viewModelScope.launch { repository.updateTimes(changes) }
    }
}
