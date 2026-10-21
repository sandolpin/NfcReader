package com.sandolpin.nfcreader.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sandolpin.nfcreader.di.AppContainer
import com.sandolpin.nfcreader.ui.components.FloatingNavBar
import com.sandolpin.nfcreader.ui.navigation.AppNavHost
import com.sandolpin.nfcreader.ui.navigation.Routes
import com.sandolpin.nfcreader.ui.navigation.TopLevelDestination

/**
 * 画面全体の骨組み。
 *
 * Scaffold は、ステータスバーぶん(上)と、下部ナビの高さぶん(下)を innerPadding として返す。
 * 背景は edge-to-edge でバーの下まで広がり、中身だけが innerPadding で避ける。
 * Snackbar は下部ナビの上に出る。
 */
@Composable
fun AppRoot(
    container: AppContainer,
    mainViewModel: MainViewModel
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selected = Routes.tabOf(backStackEntry?.destination?.route)

    // ViewModel からの一度きりの出来事(結果画面を開く / メッセージを出す)を受け取る
    LaunchedEffect(mainViewModel, navController) {
        mainViewModel.events.collect { event ->
            when (event) {
                MainEvent.ShowResult -> navController.openResult(Routes.RESULT)
                MainEvent.ShowOtherResult -> navController.openResult(Routes.OTHER_RESULT)
                is MainEvent.ShowMessage -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(event.text)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            FloatingNavBar(
                selected = selected,
                onSelect = { destination ->
                    if (destination == selected) {
                        // いまのタブをもう一度押したら、そのタブの最初の画面まで戻る
                        navController.popBackStack(destination.route, inclusive = false)
                    } else {
                        navController.navigate(destination.route) {
                            // タブを切り替えても、戻る操作の履歴が積み上がらないようにし、各タブの状態は残す
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            container = container,
            mainViewModel = mainViewModel,
            snackbarHostState = snackbarHostState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * 読み取り結果の画面を開く。結果画面が開いている最中に別のタグをかざしたときは、
 * 画面を積み重ねず、今の結果画面を新しい結果に置き換える(戻る操作で結果が何枚も出てこないように)。
 */
private fun NavHostController.openResult(route: String) {
    val current = currentDestination?.route
    if (current == Routes.RESULT || current == Routes.OTHER_RESULT) {
        if (current == route) return // 同じ種類の結果画面は、状態が更新されるだけで画面はそのまま
        navigate(route) { popUpTo(current) { inclusive = true } }
    } else {
        navigate(route) { launchSingleTop = true }
    }
}