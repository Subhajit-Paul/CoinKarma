package com.coinkarma.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coinkarma.app.CoinKarmaApp
import com.coinkarma.app.ui.challenges.ChallengesScreen
import com.coinkarma.app.ui.history.HistoryScreen
import com.coinkarma.app.ui.home.HomeScreen
import com.coinkarma.app.ui.insights.InsightsScreen
import com.coinkarma.app.ui.log.LogSheet
import com.coinkarma.app.ui.profile.ProfileScreen
import com.coinkarma.app.ui.theme.LocalCkPalette

sealed class NavRoute(val route: String, val label: String, val icon: ImageVector) {
    object Home       : NavRoute("home",       "Home",      Icons.Filled.Home)
    object History    : NavRoute("history",    "History",   Icons.Filled.History)
    object Insights   : NavRoute("insights",   "Insights",  Icons.Filled.BarChart)
    object Challenges : NavRoute("challenges", "Quests",    Icons.Filled.EmojiEvents)
    object Profile    : NavRoute("profile",    "Profile",   Icons.Filled.Person)
}

private val tabs = listOf(
    NavRoute.Home, NavRoute.History, NavRoute.Insights,
    NavRoute.Challenges, NavRoute.Profile,
)

@Composable
fun CoinKarmaApp() {
    val ck = LocalCkPalette.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination
    var showLog by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    val db = (ctx.applicationContext as CoinKarmaApp).db

    Scaffold(
        containerColor = ck.bg,
        bottomBar = {
            NavigationBar(containerColor = ck.surface) {
                tabs.forEach { tab ->
                    val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ck.primary,
                            selectedTextColor = ck.primary,
                            unselectedIconColor = ck.textMuted,
                            unselectedTextColor = ck.textMuted,
                            indicatorColor = ck.surfaceStrong,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(NavRoute.Home.route)       { HomeScreen(db, onLogClick = { showLog = true }) }
            composable(NavRoute.History.route)    { HistoryScreen(db) }
            composable(NavRoute.Insights.route)   { InsightsScreen(db) }
            composable(NavRoute.Challenges.route) { ChallengesScreen(db) }
            composable(NavRoute.Profile.route)    { ProfileScreen(db) }
        }

        if (showLog) {
            LogSheet(db = db, onDismiss = { showLog = false })
        }
    }
}
