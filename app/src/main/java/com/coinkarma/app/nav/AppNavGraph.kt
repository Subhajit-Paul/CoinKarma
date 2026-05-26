package com.coinkarma.app.nav

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.coinkarma.app.CoinKarmaApp
import com.coinkarma.app.ui.atoms.CkTabBar
import com.coinkarma.app.ui.screens.challenges.ChallengesScreen
import com.coinkarma.app.ui.screens.history.HistoryScreen
import com.coinkarma.app.ui.screens.home.HomeScreen
import com.coinkarma.app.ui.screens.log.LogSheet
import com.coinkarma.app.ui.screens.onboarding.OnboardingScreen
import com.coinkarma.app.ui.screens.profile.ProfileScreen
import com.coinkarma.app.ui.screens.splash.SplashScreen
import com.coinkarma.app.ui.theme.LocalCkPalette

private val TAB_ORDER = listOf("home", "history", "challenges", "profile")

private enum class RootState { SPLASH, LOADING, ONBOARDING, MAIN }

@Composable
fun AppNavGraph() {
    val ck = LocalCkPalette.current
    val ctx = LocalContext.current
    val db = (ctx.applicationContext as CoinKarmaApp).db

    val profile by db.profile().observe().collectAsState(initial = null)
    var splashDone by remember { mutableStateOf(false) }

    val rootState = when {
        !splashDone                          -> RootState.SPLASH
        profile == null                      -> RootState.LOADING
        !profile!!.onboardingCompleted       -> RootState.ONBOARDING
        else                                 -> RootState.MAIN
    }

    AnimatedContent(
        targetState = rootState,
        transitionSpec = {
            fadeIn(tween(500, easing = FastOutSlowInEasing)) togetherWith
            fadeOut(tween(380, easing = FastOutSlowInEasing))
        },
        label = "root_nav",
    ) { state ->
        when (state) {
            RootState.SPLASH    -> SplashScreen(onComplete = { splashDone = true })
            RootState.LOADING   -> Box(modifier = Modifier.fillMaxSize().background(ck.bg))
            RootState.ONBOARDING -> OnboardingScreen(db = db, onComplete = {})
            RootState.MAIN      -> MainAppContent(db)
        }
    }
}

@Composable
private fun MainAppContent(db: com.coinkarma.app.data.CoinKarmaDatabase) {
    val ck = LocalCkPalette.current
    var tab by remember { mutableStateOf("home") }
    var prevTab by remember { mutableStateOf("home") }
    var showLog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ck.bg)
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = tab,
                    transitionSpec = {
                        val fromIdx = TAB_ORDER.indexOf(initialState)
                        val toIdx   = TAB_ORDER.indexOf(targetState)
                        val forward = toIdx >= fromIdx
                        
                        if (forward) {
                            (slideInHorizontally(tween(400, easing = FastOutSlowInEasing)) { (it * 0.1f).toInt() } +
                                fadeIn(tween(300, easing = FastOutSlowInEasing))) togetherWith
                            (slideOutHorizontally(tween(400, easing = FastOutSlowInEasing)) { -(it * 0.1f).toInt() } +
                                fadeOut(tween(250, easing = FastOutSlowInEasing)))
                        } else {
                            (slideInHorizontally(tween(400, easing = FastOutSlowInEasing)) { -(it * 0.1f).toInt() } +
                                fadeIn(tween(300, easing = FastOutSlowInEasing))) togetherWith
                            (slideOutHorizontally(tween(400, easing = FastOutSlowInEasing)) { (it * 0.1f).toInt() } +
                                fadeOut(tween(250, easing = FastOutSlowInEasing)))
                        }
                    },
                    label = "tab_content",
                ) { currentTab ->
                    when (currentTab) {
                        "home"       -> HomeScreen(db, onLogClick = { showLog = true }, onProfileClick = { tab = "profile" })
                        "history"    -> HistoryScreen(db)
                        "challenges" -> ChallengesScreen(db)
                        "profile"    -> ProfileScreen(db)
                        else         -> HomeScreen(db, onLogClick = { showLog = true }, onProfileClick = { tab = "profile" })
                    }
                }
            }

            CkTabBar(
                selected = tab,
                onSelect = { newTab ->
                    if (newTab != tab) { prevTab = tab; tab = newTab }
                },
                onLogTap = { showLog = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (showLog) {
            LogSheet(db = db, onDismiss = { showLog = false })
        }
    }
}
