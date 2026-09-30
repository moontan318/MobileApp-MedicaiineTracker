package com.medtracker.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.medtracker.app.data.RegimenRepository
import com.medtracker.core.analysis.AnalysisResult
import com.medtracker.core.analysis.RegimenAnalyzer
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.model.RegimenItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RegimenRepository(application)
    val knowledgeBase = KnowledgeBase()
    private val analyzer = RegimenAnalyzer(knowledgeBase)

    val items: StateFlow<List<RegimenItem>> = repository.items

    val analysis: StateFlow<AnalysisResult> = repository.items
        .map { analyzer.analyze(it) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, AnalysisResult.EMPTY)

    private val _disclaimerAccepted = MutableStateFlow(repository.disclaimerAccepted)
    val disclaimerAccepted: StateFlow<Boolean> = _disclaimerAccepted.asStateFlow()

    fun acceptDisclaimer() {
        repository.disclaimerAccepted = true
        _disclaimerAccepted.value = true
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
