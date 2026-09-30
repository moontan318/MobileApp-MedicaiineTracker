package com.medtracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medtracker.app.ui.components.EmptyState
import com.medtracker.core.analysis.AnalysisResult
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.RegimenItem

private data class Dose(val item: RegimenItem, val time: DoseTime, val changedFrom: List<DoseTime>?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    items: List<RegimenItem>,
    analysis: AnalysisResult,
    onEdit: (String) -> Unit,
    onApplyAll: () -> Unit,
) {
    var showSuggested by rememberSaveable { mutableStateOf(false) }
    val hasSuggestions = analysis.scheduleChanges.isNotEmpty()
    val useSuggested = showSuggested && hasSuggestions

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily schedule") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                title = "No schedule yet",
                message = "Add items on the My list tab and they will appear here in time order.",
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            return@Scaffold
        }

        val doses = items.flatMap { item ->
            val change = analysis.changeFor(item.id)
            val times = if (useSuggested && change != null) change.suggestedTimes else item.sortedTimes
            times.map { Dose(item, it, if (useSuggested && change != null) change.currentTimes else null) }
        }.sortedWith(compareBy({ it.time }, { it.item.name.lowercase() }))
        val slots = doses.groupBy { it.time }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (hasSuggestions) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "${analysis.scheduleChanges.size} item(s) could be taken at better times",
                                fontWeight = FontWeight.SemiBold,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(selected = !showSuggested, onClick = { showSuggested = false }, label = { Text("Current") })
                                FilterChip(selected = showSuggested, onClick = { showSuggested = true }, label = { Text("Suggested") })
                            }
                            if (useSuggested) {
                                Button(onClick = { onApplyAll(); showSuggested = false }) { Text("Use suggested schedule") }
                            }
                        }
                    }
                }
            }
            slots.forEach { (time, list) ->
                item(key = "slot_${time.minuteOfDay}_$useSuggested") {
                    TimeSlot(time, list, onEdit)
                }
            }
            item {
                Text(
                    "Suggested times are based on general guidance. Check with your pharmacist before changing when you take prescribed medicines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimeSlot(time: DoseTime, doses: List<Dose>, onEdit: (String) -> Unit) {
    Row(verticalAlignment = Alignment.Top) {
        Text(
            time.format(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(64.dp).padding(top = 12.dp),
        )
        Card(Modifier.fillMaxWidth()) {
            doses.forEachIndexed { index, dose ->
                if (index > 0) HorizontalDivider()
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(dose.item.id) }
                        .padding(12.dp),
                ) {
                    Text(dose.item.name, fontWeight = FontWeight.SemiBold)
                    val extra = if (dose.item.food != FoodRelation.ANY) " · ${dose.item.food.label.lowercase()}" else ""
                    Text(
                        dose.item.doseLabel() + extra,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (dose.changedFrom != null) {
                        Text(
                            "Moved from ${dose.changedFrom.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}
