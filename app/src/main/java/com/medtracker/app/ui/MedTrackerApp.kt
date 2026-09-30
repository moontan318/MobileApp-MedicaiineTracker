package com.medtracker.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.medtracker.app.MedViewModel
import com.medtracker.app.ui.components.DisclaimerDialog
import com.medtracker.app.ui.components.SettingsDialog
import com.medtracker.app.ui.screens.EditItemScreen
import com.medtracker.app.ui.screens.ItemDetailScreen
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.app.ui.screens.ItemsScreen
import com.medtracker.app.ui.screens.ReviewScreen
import com.medtracker.app.ui.screens.ScheduleScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    ITEMS("items", "My list", Icons.AutoMirrored.Filled.List),
    SCHEDULE("schedule", "Schedule", Icons.Filled.DateRange),
    REVIEW("review", "Review", Icons.Filled.Warning),
}

private const val EDIT_ROUTE = "edit"
private const val DETAIL_ROUTE = "item"
private const val ARG_ID = "id"

private fun NavHostController.openEditor(id: String? = null) =
    navigate(if (id == null) EDIT_ROUTE else "$EDIT_ROUTE?$ARG_ID=$id")

private fun NavHostController.openDetail(id: String) = navigate("$DETAIL_ROUTE/$id")

@Composable
fun MedTrackerApp(viewModel: MedViewModel = viewModel()) {
    val navController = rememberNavController()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val analysis by viewModel.analysis.collectAsStateWithLifecycle()
    val disclaimerAccepted by viewModel.disclaimerAccepted.collectAsStateWithLifecycle()
    val onlineEnabled by viewModel.onlineEnabled.collectAsStateWithLifecycle()
    val onlineInfo by viewModel.onlineInfo.collectAsStateWithLifecycle()
    val lookupStatus by viewModel.lookupStatus.collectAsStateWithLifecycle()
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = Tab.entries.any { it.route == currentRoute }

    if (!disclaimerAccepted) DisclaimerDialog(onAccept = viewModel::acceptDisclaimer)
    if (showSettings) {
        SettingsDialog(
            onlineEnabled = onlineEnabled,
            onOnlineEnabledChange = viewModel::setOnlineEnabled,
            onClearCache = viewModel::clearOnlineCache,
            onDismiss = { showSettings = false },
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (tab == Tab.REVIEW && analysis.alertCount > 0) {
                                    BadgedBox(badge = { Badge { Text(analysis.alertCount.toString()) } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.ITEMS.route,
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
        ) {
            composable(Tab.ITEMS.route) {
                ItemsScreen(
                    items = items,
                    analysis = analysis,
                    lookupStatus = lookupStatus,
                    onAdd = { navController.openEditor() },
                    onOpen = { navController.openDetail(it) },
                    onOpenReview = { navController.navigate(Tab.REVIEW.route) },
                    onOpenSettings = { showSettings = true },
                )
            }
            composable(Tab.SCHEDULE.route) {
                ScheduleScreen(
                    items = items,
                    analysis = analysis,
                    onEdit = { navController.openDetail(it) },
                    onApplyAll = { viewModel.applySuggestions() },
                )
            }
            composable(Tab.REVIEW.route) {
                ReviewScreen(
                    items = items,
                    analysis = analysis,
                    onApply = { viewModel.applySuggestions(setOf(it)) },
                    onApplyAll = { viewModel.applySuggestions() },
                )
            }
            composable(
                route = "$DETAIL_ROUTE/{$ARG_ID}",
                arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType }),
            ) { entry ->
                val id = entry.arguments?.getString(ARG_ID)
                val item = items.firstOrNull { it.id == id }
                if (item == null) {
                    // Deleted from the editor - leave the detail screen.
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    val key = KnowledgeBase.normalize(item.name)
                    ItemDetailScreen(
                        item = item,
                        analysis = analysis,
                        onlineInfo = onlineInfo[key],
                        lookupStatus = lookupStatus[key],
                        onlineEnabled = onlineEnabled,
                        onEdit = { navController.openEditor(item.id) },
                        onRefresh = { viewModel.refreshLookup(item.name) },
                        onBack = { navController.popBackStack() },
                    )
                }
            }
            composable(
                route = "$EDIT_ROUTE?$ARG_ID={$ARG_ID}",
                arguments = listOf(navArgument(ARG_ID) { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { entry ->
                val id = entry.arguments?.getString(ARG_ID)
                EditItemScreen(
                    existing = id?.let(viewModel::item),
                    knowledgeBase = viewModel.knowledgeBase,
                    onSave = { viewModel.save(it); navController.popBackStack() },
                    onDelete = { viewModel.delete(it); navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
