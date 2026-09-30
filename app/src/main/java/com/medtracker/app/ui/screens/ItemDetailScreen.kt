package com.medtracker.app.ui.screens

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medtracker.app.data.LookupStatus
import com.medtracker.app.ui.components.FindingCard
import com.medtracker.core.analysis.AnalysisResult
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.RegimenItem
import com.medtracker.core.online.OnlineDrugInfo
import com.medtracker.core.online.PharmClassMapper

private val US_TO_UK = PharmClassMapper.UK_TO_US.entries.associate { (uk, us) -> us to uk }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ItemDetailScreen(
    item: RegimenItem,
    analysis: AnalysisResult,
    onlineInfo: OnlineDrugInfo?,
    lookupStatus: LookupStatus?,
    onlineEnabled: Boolean,
    onEdit: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    val resolved = analysis.resolved.firstOrNull { it.item.id == item.id }
    val findings = analysis.findingsFor(item.id)
    val uriHandler = LocalUriHandler.current
    val open: (String) -> Unit = { url -> runCatching { uriHandler.openUri(url) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = { IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Edit") } },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ---- Summary ----
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${item.type.label} · ${item.doseLabel()}", style = MaterialTheme.typography.titleMedium)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.sortedTimes.forEach { AssistChip(onClick = onEdit, label = { Text(it.format()) }) }
                        }
                        if (item.food != FoodRelation.ANY) Text(item.food.label)
                        if (item.notes.isNotBlank()) Text(item.notes, style = MaterialTheme.typography.bodySmall)
                        val identified = when {
                            resolved == null || !resolved.recognised -> "Not recognised"
                            resolved.identifiedOnline -> "Identified online as " + resolved.substances.joinToString(", ") { it.displayName }
                            else -> "Recognised (built-in database) as " + resolved.substances.joinToString(", ") { it.displayName }
                        }
                        Text(identified, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ---- Findings ----
            if (findings.isNotEmpty()) {
                item { Header("Findings for this item") }
                items(findings, key = { it.id }) { FindingCard(it) }
            }

            // ---- Online information ----
            item { Header("Online drug information") }
            item {
                OnlineSection(onlineInfo, lookupStatus, onlineEnabled, onRefresh, onOpen = open)
            }

            // ---- Reference links ----
            item { Header("Look it up") }
            item {
                val searchName = referenceName(item, resolved?.substances?.firstOrNull()?.aliases?.firstOrNull(), onlineInfo)
                val encoded = Uri.encode(searchName)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LinkButton("BNF (NICE)", "https://bnf.nice.org.uk/search/?q=$encoded", open)
                    LinkButton("NHS medicines A-Z", "https://www.nhs.uk/search/results?q=$encoded", open)
                    onlineInfo?.dailyMedUrl?.let { LinkButton("Full US label (DailyMed)", it, open) }
                    Text(
                        "The BNF is licensed by NICE and has no public data feed, so the app links to it rather than copying its content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/** Best name to search reference sites with: the UK generic name where we know it. */
private fun referenceName(item: RegimenItem, builtInAlias: String?, info: OnlineDrugInfo?): String {
    builtInAlias?.let { return it }
    info?.genericName?.let { generic -> return US_TO_UK[KnowledgeBase.normalize(generic)] ?: generic }
    return PharmClassMapper.lookupCandidates(item.name).lastOrNull() ?: item.name
}

@Composable
private fun Header(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun LinkButton(label: String, url: String, open: (String) -> Unit) {
    OutlinedButton(onClick = { open(url) }, modifier = Modifier.fillMaxWidth()) { Text(label) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OnlineSection(
    info: OnlineDrugInfo?,
    status: LookupStatus?,
    enabled: Boolean,
    onRefresh: () -> Unit,
    onOpen: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                !enabled -> Text("Online lookup is switched off. Turn it on in Settings (My list → ⚙) to fetch label information.")
                status == LookupStatus.IN_PROGRESS -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Looking up…")
                }
                status == LookupStatus.FAILED && info == null -> {
                    Text("Couldn't connect to the online sources. Check your internet connection.")
                    TextButton(onClick = onRefresh) { Text("Try again") }
                }
                info == null -> Text("Not looked up yet.")
                !info.found -> {
                    Text("No matching record was found in the US FDA label database or RxNorm. Many supplements and UK-only brands aren't listed there.")
                    TextButton(onClick = onRefresh) { Text("Search again") }
                }
                else -> FoundInfo(info, onRefresh)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FoundInfo(info: OnlineDrugInfo, onRefresh: () -> Unit) {
    info.genericName?.let { Text("Generic name: $it", fontWeight = FontWeight.SemiBold) }
    if (info.brandNames.isNotEmpty()) {
        Text("US brand names: " + info.brandNames.joinToString(", "), style = MaterialTheme.typography.bodySmall)
    }
    if (info.pharmClasses.isNotEmpty()) {
        Text("Drug class: " + info.pharmClasses.joinToString(", ") { it.substringBefore(" [") }, style = MaterialTheme.typography.bodyMedium)
    }
    val sections = listOf(
        "Contraindications" to info.contraindicationsText,
        "Drug interactions" to info.interactionsText,
        "Warnings" to info.warningsText,
        "Dosage (US label)" to info.dosageText,
    ).filter { !it.second.isNullOrBlank() }
    sections.forEach { (title, text) ->
        HorizontalDivider()
        ExpandableText(title, text!!)
    }
    HorizontalDivider()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Source: " + info.sources.joinToString(", ") + ". US dosing and brands may differ from the UK.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRefresh) { Icon(Icons.Filled.Refresh, contentDescription = "Refresh") }
    }
}

@Composable
private fun ExpandableText(title: String, text: String) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    Column {
        TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        Text(
            if (expanded) text else text.take(PREVIEW_CHARS) + if (text.length > PREVIEW_CHARS) "…" else "",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private const val PREVIEW_CHARS = 220
