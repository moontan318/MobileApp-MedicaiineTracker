package com.medtracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.medtracker.app.ui.theme.severityColors
import com.medtracker.core.analysis.Finding
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.model.DoseTime

fun severityIcon(severity: Severity): ImageVector = when (severity) {
    Severity.MAJOR, Severity.MODERATE -> Icons.Filled.Warning
    Severity.MINOR -> Icons.Filled.Info
    Severity.INFO -> Icons.Filled.CheckCircle
}

@Composable
fun SeverityBadge(severity: Severity, modifier: Modifier = Modifier) {
    val colors = severityColors(severity)
    Surface(
        color = colors.container,
        contentColor = colors.content,
        shape = RoundedCornerShape(50),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(severityIcon(severity), contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(severity.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FindingCard(finding: Finding, modifier: Modifier = Modifier) {
    val colors = severityColors(finding.severity)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(5.dp).fillMaxHeight().background(colors.content))
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SeverityBadge(finding.severity)
                Text(finding.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(finding.detail, style = MaterialTheme.typography.bodyMedium)
                if (finding.recommendation.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Column(Modifier.padding(10.dp)) {
                            Text("What to do", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Text(finding.recommendation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisclaimerDialog(onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Before you start") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Medicine Tracker helps you keep a list of your medicines and supplements and highlights common interactions, timing and dose issues.")
                Text(
                    "It is NOT a substitute for professional advice. Its database is limited and cannot know your medical history, kidney or liver function, weight, pregnancy or your prescriber's reasons.",
                    fontWeight = FontWeight.SemiBold,
                )
                Text("Never stop or change a prescribed medicine without speaking to your doctor or pharmacist. In an emergency, call your local emergency number.")
                Text("Your list stays on this device. To identify medicines, only their names are looked up online (US FDA and National Library of Medicine) - you can switch this off in Settings.")
            }
        },
        confirmButton = { TextButton(onClick = onAccept) { Text("I understand") } },
    )
}

@Composable
fun SettingsDialog(
    onlineEnabled: Boolean,
    onOnlineEnabledChange: (Boolean) -> Unit,
    onClearCache: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings & data sources") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Look up medicines online", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Identifies items not in the built-in database and checks official drug labels for interactions.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Switch(checked = onlineEnabled, onCheckedChange = onOnlineEnabledChange)
                }
                Text(
                    "Only the medicine name is sent, to the US FDA (openFDA drug labels) and the US National Library of Medicine (RxNorm). " +
                        "Doses, times and notes never leave your device. Results are saved on the device for 30 days.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Text("Data sources", fontWeight = FontWeight.SemiBold)
                Text(
                    "• Built-in database: ~220 common UK medicines and supplements with interaction, timing and dose rules.\n" +
                        "• openFDA: official US prescribing information (drug class, interactions, contraindications, dosing).\n" +
                        "• RxNorm: corrects misspellings and brand names.\n" +
                        "• BNF and NHS: linked from each item - the BNF has no public data feed.",
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(onClick = onClearCache) { Text("Clear saved online results") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(initial: DoseTime, onDismiss: () -> Unit, onConfirm: (DoseTime) -> Unit) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dose time") },
        text = { TimePicker(state = state) },
        confirmButton = { TextButton(onClick = { onConfirm(DoseTime.of(state.hour, state.minute)) }) { Text("OK") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
