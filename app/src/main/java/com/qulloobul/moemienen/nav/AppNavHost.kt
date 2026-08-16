package com.qulloobul.moemienen.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.qulloobul.moemienen.R
import com.qulloobul.moemienen.data.AppViewModel
import com.qulloobul.moemienen.screens.ComingSoonScreen
import com.qulloobul.moemienen.screens.EventsScreen
import com.qulloobul.moemienen.screens.HomeScreen
import com.qulloobul.moemienen.screens.LoginScreen
import com.qulloobul.moemienen.screens.NotificationsScreen
import com.qulloobul.moemienen.screens.RequestsScreen
import com.qulloobul.moemienen.screens.TasksScreen
import com.qulloobul.moemienen.ui.theme.StatusDenied
import kotlinx.coroutines.launch

private object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val REQUESTS = "requests"
    const val MEETINGS = "meetings"
    const val USERS = "users"
    const val EVENTS = "events"
    const val NOTIFICATIONS = "notifications"
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Top-level composable: shows LoginScreen while no role is selected
 * (mirrors MainLayout.razor's `currentRole is null` branch), otherwise
 * a drawer-based Scaffold whose visible items depend on the current
 * role, same gating as NavMenu.razor / MainLayout.razor.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    viewModel: AppViewModel,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val role = viewModel.currentRole

    if (role == null) {
        LoginScreen(
            isDarkMode = isDarkMode,
            onToggleDarkMode = onToggleDarkMode,
            onLogin = { name, selectedRole -> viewModel.login(name, selectedRole) }
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navItems = buildList {
        add(NavItem(Routes.HOME, "Home", Icons.Filled.Home))
        add(NavItem(Routes.TASKS, "Daily Tasks", Icons.Filled.CheckBox))
        add(NavItem(Routes.REQUESTS, "Resource Requests", Icons.Filled.Inbox))
        if (role.canSeeMeetings) add(NavItem(Routes.MEETINGS, "Meetings", Icons.Filled.Groups))
        if (role.canSeeUserManagement) add(NavItem(Routes.USERS, "User Management", Icons.Filled.ManageAccounts))
        if (role.canSeeEvents) add(NavItem(Routes.EVENTS, "Event Planning", Icons.Filled.CalendarMonth))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Company Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "QULOOBUL MOE'MIENEEN",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                val currentRoute = backStackEntry?.destination

                navItems.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) },
                        selected = currentRoute?.let {
                            var d: androidx.navigation.NavDestination? = it
                            var found = false
                            while (d != null) {
                                if (d.route == item.route) {
                                    found = true
                                    break
                                }
                                d = d.parent
                            }
                            found
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    label = { Text("Log out") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.logout()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("QULOOBUL MOE'MIENEEN") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { navController.navigate(Routes.NOTIFICATIONS) }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                            }
                            if (viewModel.unreadNotificationCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                        .size(16.dp)
                                        .background(StatusDenied, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (viewModel.unreadNotificationCount > 9) "9+" else viewModel.unreadNotificationCount.toString(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onToggleDarkMode) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle dark mode"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Routes.HOME) { HomeScreen(viewModel) }
                composable(Routes.TASKS) { TasksScreen(viewModel) }
                composable(Routes.REQUESTS) { RequestsScreen(viewModel) }
                composable(Routes.MEETINGS) { ComingSoonScreen("Meetings") }
                composable(Routes.USERS) { ComingSoonScreen("User Management") }
                composable(Routes.EVENTS) { EventsScreen(viewModel) }
                composable(Routes.NOTIFICATIONS) {
                    NotificationsScreen(
                        viewModel = viewModel,
                        // Clicking a notification marks it read and, since
                        // this is a prototype without per-item deep links,
                        // sends the user to the page that owns that kind of
                        // data - same behaviour as Notifications.razor's
                        // OpenNotification.
                        onNavigateToRequests = {
                            navController.navigate(Routes.REQUESTS) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToEvents = {
                            navController.navigate(Routes.EVENTS) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}
