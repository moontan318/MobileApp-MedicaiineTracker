package com.medtracker.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.medtracker.app.ui.components.TimePickerDialog
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem
import java.util.UUID

private val PRESETS = listOf(
    "Waking" to DoseTime.of(7),
    "Breakfast" to DoseTime.of(8),
    "Lunch" to DoseTime.of(13),
    "Dinner" to DoseTime.of(18),
    "Bedtime" to DoseTime.of(22),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditItemScreen(
    existing: RegimenItem?,
    knowledgeBase: KnowledgeBase,
    onSave: (RegimenItem) -> Unit,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var type by rememberSaveable { mutableStateOf(existing?.type ?: ItemType.MEDICINE) }
    var amountText by rememberSaveable {
        mutableStateOf(existing?.doseAmount?.let(RegimenItem::formatAmount) ?: "")
    }
    var unit by rememberSaveable { mutableStateOf(existing?.doseUnit ?: DoseUnit.MG) }
    var food by rememberSaveable { mutableStateOf(existing?.food ?: FoodRelation.ANY) }
    var notes by rememberSaveable { mutableStateOf(existing?.notes ?: "") }
    val times = remember { mutableStateListOf<DoseTime>().apply { addAll(existing?.sortedTimes ?: emptyList()) } }
    var typeTouched by rememberSaveable { mutableStateOf(existing != null) }
    var showSuggestions by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    val amount = amountText.replace(',', '.').toDoubleOrNull()
    val nameError = attemptedSave && name.isBlank()
    val amountError = attemptedSave && (amount == null || amount <= 0)
    val timesError = attemptedSave && times.isEmpty()
    val recognised = remember(name) { knowledgeBase.identify(name) }
    val suggestions = remember(name) { knowledgeBase.suggest(name) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Add item" else "Edit item") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Filled.Delete, contentDescription = "Delete") }
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ---- Name with autocomplete ----
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showSuggestions = true },
                    label = { Text("Name (generic or brand)") },
                    isError = nameError,
                    supportingText = {
                        when {
                            nameError -> Text("Enter a name")
                            name.isBlank() -> Text("e.g. Levothyroxine, Vitamin D3, Magnesium glycinate")
                            recognised.isNotEmpty() -> Text("Recognised: " + recognised.joinToString(", ") { it.displayName })
                            else -> Text("Not in the built-in database - it will be looked up online after saving")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (showSuggestions && suggestions.isNotEmpty() && suggestions.none { it.second.displayName.equals(name, true) }) {
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        suggestions.forEach { (label, substance) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    name = substance.displayName
                                    if (!typeTouched) type = substance.defaultType
                                    showSuggestions = false
                                },
                            )
                        }
                    }
                }
            }

            // ---- Type ----
            Section("Type") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ItemType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t; typeTouched = true },
                            label = { Text(t.label) },
                        )
                    }
                }
            }

            // ---- Dose ----
            Section("Dose per time taken") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount") },
                        isError = amountError,
                        supportingText = { if (amountError) Text("Enter a number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    UnitDropdown(unit, onSelect = { unit = it }, modifier = Modifier.weight(1f))
                }
                Text(
                    "Tip: enter the strength in mg, mcg or IU (from the label) so the dose can be checked against safe limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ---- Times ----
            Section("Times of day") {
                if (times.isEmpty()) {
                    Text(
                        "No times added yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (timesError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    times.sorted().forEach { time ->
                        InputChip(
                            selected = true,
                            onClick = { times.remove(time) },
                            label = { Text(time.format()) },
                            trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Remove", modifier = Modifier.size(18.dp)) },
                        )
                    }
                }
                Text("Quick add", style = MaterialTheme.typography.labelMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PRESETS.forEach { (label, time) ->
                        SuggestionChip(
                            onClick = { if (time !in times) times.add(time) },
                            label = { Text("$label ${time.format()}") },
                        )
                    }
                }
                OutlinedButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Add a specific time", modifier = Modifier.padding(start = 8.dp))
                }
            }

            // ---- Food ----
            Section("Taken") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FoodRelation.entries.forEach { f ->
                        FilterChip(selected = food == f, onClick = { food = f }, label = { Text(f.label) })
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. prescribed by Dr Smith, for blood pressure") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )

            Button(
                onClick = {
                    attemptedSave = true
                    if (name.isNotBlank() && amount != null && amount > 0 && times.isNotEmpty()) {
                        onSave(
                            RegimenItem(
                                id = existing?.id ?: UUID.randomUUID().toString(),
                                name = name.trim(),
                                type = type,
                                doseAmount = amount,
                                doseUnit = unit,
                                times = times.sorted(),
                                food = food,
                                notes = notes.trim(),
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save") }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initial = times.maxOrNull() ?: DoseTime.of(8),
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                if (time !in times) times.add(time)
                showTimePicker = false
            },
        )
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${existing.name}?") },
            text = { Text("This removes it from your list and schedule.") },
            confirmButton = { TextButton(onClick = { onDelete(existing.id) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(selected: DoseUnit, onSelect: (DoseUnit) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DoseUnit.entries.forEach { unit ->
                DropdownMenuItem(text = { Text(unit.label) }, onClick = { onSelect(unit); expanded = false })
            }
        }
    }
}
