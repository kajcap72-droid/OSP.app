package pl.osp.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pl.osp.app.ui.navigation.*
import pl.osp.app.ui.screens.alarms.AlarmDetailsScreen
import pl.osp.app.ui.screens.alarms.AlarmsScreen
import pl.osp.app.ui.screens.documents.DocumentsScreen
import pl.osp.app.ui.screens.duty.CalendarScreen
import pl.osp.app.ui.screens.equipment.EquipmentScreen
import pl.osp.app.ui.screens.login.LoginScreen
import pl.osp.app.ui.screens.map.MapScreen
import pl.osp.app.ui.screens.members.MemberDetailsScreen
import pl.osp.app.ui.screens.members.MembersScreen
import pl.osp.app.ui.screens.settings.SettingsScreen
import pl.osp.app.ui.screens.stats.StatsScreen
import pl.osp.app.ui.screens.vehicles.VehiclesScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OspApp(
    deepLinkAlarmId: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val nav = rememberNavController()
    // Obsługa deep linka z powiadomienia: gdy MainActivity dostanie alarmId,
    // automatycznie otwieramy szczegóły alarmu.
    LaunchedEffect(deepLinkAlarmId) {
        if (!deepLinkAlarmId.isNullOrBlank()) {
            nav.navigate(Dest.AlarmDetails.create(deepLinkAlarmId))
            onDeepLinkConsumed()
        }
    }
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var loggedIn by remember { mutableStateOf(false) }

    if (!loggedIn) {
        LoginScreen(onLoggedIn = { loggedIn = true })
        return
    }

    val showBottomBar = currentRoute in bottomBarDestinations.map { it.route }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                currentRoute = currentRoute,
                onNavigate = { dest ->
                    scope.launch { drawerState.close() }
                    nav.navigate(dest.route) {
                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(drawerDestinations.find { it.route == currentRoute }?.title ?: "OSP") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomBarDestinations.forEach { dest ->
                            NavigationBarItem(
                                selected = currentRoute == dest.route,
                                onClick = {
                                    nav.navigate(dest.route) {
                                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { dest.icon?.let { Icon(it, contentDescription = dest.title) } },
                                label = { Text(dest.title, maxLines = 1) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = Dest.Alarms.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Dest.Alarms.route) {
                    AlarmsScreen(onAlarmClick = { nav.navigate(Dest.AlarmDetails.create(it)) })
                }
                composable(
                    Dest.AlarmDetails.route,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) {
                    AlarmDetailsScreen(
                        alarmId = it.arguments?.getString("id").orEmpty(),
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(Dest.Members.route) {
                    MembersScreen(onMemberClick = { nav.navigate(Dest.MemberDetails.create(it)) })
                }
                composable(
                    Dest.MemberDetails.route,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) {
                    MemberDetailsScreen(
                        memberId = it.arguments?.getString("id").orEmpty(),
                        onBack = { nav.popBackStack() }
                    )
                }
                composable(Dest.Calendar.route) { CalendarScreen() }
                composable(Dest.Equipment.route) { EquipmentScreen() }
                composable(Dest.Vehicles.route) { VehiclesScreen() }
                composable(Dest.Documents.route) { DocumentsScreen() }
                composable(Dest.Stats.route) { StatsScreen() }
                composable(Dest.Map.route) { MapScreen() }
                composable(Dest.Settings.route) { SettingsScreen(onLogout = { loggedIn = false }) }
            }
        }
    }
}

@Composable
private fun DrawerContent(currentRoute: String?, onNavigate: (Dest) -> Unit) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("OSP", style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary)
            Text("Aplikacja jednostki", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            drawerDestinations.forEach { dest ->
                NavigationDrawerItem(
                    label = { Text(dest.title) },
                    selected = currentRoute == dest.route,
                    icon = { dest.icon?.let { Icon(it, contentDescription = null) } },
                    onClick = { onNavigate(dest) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    }
}

