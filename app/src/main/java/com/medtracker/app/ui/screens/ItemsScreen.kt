package com.medtracker.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medtracker.app.ui.components.EmptyState
import com.medtracker.app.ui.components.SeverityBadge
import com.medtracker.app.ui.components.severityIcon
import com.medtracker.app.ui.theme.severityColors
import com.medtracker.core.analysis.AnalysisResult
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    items: List<RegimenItem>,
    analysis: AnalysisResult,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenReview: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My medicines & supplements") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add") },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                title = "Nothing added yet",
                message = "Tap Add to record a medicine, vitamin, mineral or herbal supplement, with its dose and the times you take it.",
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            return@Scaffold
        }
        val grouped = ItemType.entries.associateWith { type -> items.filter { it.type == type } }.filterValues { it.isNotEmpty() }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { SummaryBanner(analysis, onOpenReview) }
            grouped.forEach { (type, list) ->
                item(key = "header_$type") {
                    Text(
                        pluralLabel(type),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(list, key = { it.id }) { item ->
                    ItemCard(item, analysis, onClick = { onEdit(item.id) })
                }
            }
        }
    }
}

private fun pluralLabel(type: ItemType) = when (type) {
    ItemType.MEDICINE -> "Medicines"
    ItemType.VITAMIN -> "Vitamins"
    ItemType.MINERAL -> "Minerals"
    ItemType.HERBAL -> "Herbal supplements"
    ItemType.OTHER_SUPPLEMENT -> "Other supplements"
}

@Composable
private fun SummaryBanner(analysis: AnalysisResult, onOpenReview: () -> Unit) {
    val major = analysis.count(Severity.MAJOR)
    val moderate = analysis.count(Severity.MODERATE)
    val severity = when {
        major > 0 -> Severity.MAJOR
        moderate > 0 -> Severity.MODERATE
        else -> Severity.INFO
    }
    val colors = severityColors(severity)
    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenReview),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(severityIcon(severity), contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column {
                val headline = when {
                    major + moderate == 0 -> "No major or moderate issues found"
                    else -> listOfNotNull(
                        if (major > 0) "$major major" else null,
                        if (moderate > 0) "$moderate moderate" else null,
                    ).joinToString(", ") + " issue(s) to review"
                }
                Text(headline, fontWeight = FontWeight.SemiBold)
                val extra = buildList {
                    if (analysis.scheduleChanges.isNotEmpty()) add("${analysis.scheduleChanges.size} timing suggestion(s)")
                    val minor = analysis.count(Severity.MINOR) + analysis.count(Severity.INFO)
                    if (minor > 0) add("$minor tip(s)")
                }
                Text(
                    if (extra.isEmpty()) "Tap to see the full review" else extra.joinToString(" · ") + " - tap to review",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCard(item: RegimenItem, analysis: AnalysisResult, onClick: () -> Unit) {
    val findings = analysis.findingsFor(item.id)
    val worst = findings.minByOrNull { it.severity.ordinal }?.severity
    val resolved = analysis.resolved.firstOrNull { it.item.id == item.id }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${item.doseLabel()} · ${item.times.size}x daily" +
                            if (item.food != FoodRelation.ANY) " · ${item.food.label.lowercase()}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (worst != null && worst != Severity.INFO) SeverityBadge(worst)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item.sortedTimes.forEach { time -> AssistChip(onClick = onClick, label = { Text(time.format()) }) }
            }
            val recognisedText = when {
                resolved == null -> null
                resolved.recognised -> "Recognised as: " + resolved.substances.joinToString(", ") { it.displayName }
                else -> "Not in database - interactions can't be checked"
            }
            if (recognisedText != null) {
                Text(recognisedText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.notes.isNotBlank()) {
                Text(item.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
