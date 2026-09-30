package com.medtracker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.medtracker.app.ui.components.EmptyState
import com.medtracker.app.ui.components.FindingCard
import com.medtracker.app.ui.theme.severityColors
import com.medtracker.core.analysis.AnalysisResult
import com.medtracker.core.analysis.FindingCategory
import com.medtracker.core.analysis.ScheduleChange
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.model.RegimenItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    items: List<RegimenItem>,
    analysis: AnalysisResult,
    onApply: (String) -> Unit,
    onApplyAll: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Review") }) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                title = "Nothing to review",
                message = "Add your medicines and supplements to check for interactions, timing and dose issues.",
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { SeveritySummary(analysis) }

            if (analysis.scheduleChanges.isNotEmpty()) {
                item { SectionHeader("Suggested timing changes") }
                item { ScheduleChangesCard(analysis.scheduleChanges, onApply, onApplyAll) }
            }

            FindingCategory.entries.forEach { category ->
                val findings = analysis.findings.filter { it.category == category }
                if (findings.isNotEmpty()) {
                    item(key = "h_$category") { SectionHeader(category.label) }
                    items(findings, key = { it.id }) { FindingCard(it) }
                }
            }

            if (analysis.findings.isEmpty()) {
                item {
                    Text(
                        "No interactions, timing or dose issues were found for the items in the built-in database.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            item {
                Text(
                    "This review uses a built-in summary of common interactions and general adult dosing guidance. It can't account for your " +
                        "medical conditions, age, weight, kidney/liver function or pregnancy, and it doesn't know why your prescriber chose a dose. " +
                        "Always talk to your doctor or pharmacist before changing or stopping a medicine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun SeveritySummary(analysis: AnalysisResult) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Severity.entries.forEach { severity ->
            val colors = severityColors(severity)
            Surface(
                color = colors.container,
                contentColor = colors.content,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.weight(1f),
            ) {
                Column(Modifier.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(analysis.count(severity).toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(severity.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ScheduleChangesCard(changes: List<ScheduleChange>, onApply: (String) -> Unit, onApplyAll: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            changes.forEachIndexed { index, change ->
                if (index > 0) HorizontalDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(change.itemName, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${change.currentTimes.joinToString(", ")}  →  ${change.suggestedTimes.joinToString(", ")}",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        change.reasons.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                    }
                    FilledTonalButton(onClick = { onApply(change.itemId) }) { Text("Apply") }
                }
            }
            if (changes.size > 1) {
                Button(onClick = onApplyAll, modifier = Modifier.fillMaxWidth()) { Text("Apply all") }
            }
            Text(
                "Check with your pharmacist before moving prescribed medicines.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
