package com.sandolpin.nfcreader.ui.navigation

import android.net.Uri
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sandolpin.nfcreader.di.AppContainer
import com.sandolpin.nfcreader.ui.MainViewModel
import com.sandolpin.nfcreader.ui.history.CardDetailScreen
import com.sandolpin.nfcreader.ui.history.CardDetailViewModel
import com.sandolpin.nfcreader.ui.history.HistoryScreen
import com.sandolpin.nfcreader.ui.history.HistoryViewModel
import com.sandolpin.nfcreader.ui.home.HomeScreen
import com.sandolpin.nfcreader.ui.home.HomeViewModel
import com.sandolpin.nfcreader.ui.result.OtherResultScreen
import com.sandolpin.nfcreader.ui.result.ResultScreen
import com.sandolpin.nfcreader.ui.settings.SettingsScreen
import com.sandolpin.nfcreader.ui.settings.SettingsViewModel
import com.sandolpin.nfcreader.ui.result.ResultViewModel

/** 画面の宛先名。 */
object Routes {
    const val RESULT = "result"
    /** 交通系IC以外(クレジットカード・NFCタグ)の結果。 */
    const val OTHER_RESULT = "result/other"

    private const val ARG_CARD_ID = "cardId"
    /** 保存済みカードの詳細。宛先の定義に使う形。 */
    const val DETAIL = "detail/{$ARG_CARD_ID}"
    const val ARG_CARD_ID_NAME = ARG_CARD_ID

    /** カードID(01:01:03:… のように記号を含む)を、宛先に入れられる形にエンコードして作る。 */
    fun detail(cardId: String): String = "detail/${Uri.encode(cardId)}"

    /** 今の宛先に対応する下部ナビのタブ。結果画面は「読み取り」、詳細画面は「履歴」タブの一部として扱う。 */
    fun tabOf(route: String?): TopLevelDestination? = when (route) {
        TopLevelDestination.HOME.route, RESULT, OTHER_RESULT -> TopLevelDestination.HOME
        TopLevelDestination.HISTORY.route, DETAIL -> TopLevelDestination.HISTORY
        TopLevelDestination.SETTINGS.route -> TopLevelDestination.SETTINGS
        else -> null
    }
}

/**
 * 画面の遷移。[modifier] には、Scaffold が計算した余白(ステータスバー・ナビゲーションバー・下部ナビ)を渡すこと。
 *
 * NavHost 全体を SharedTransitionLayout で包み、履歴一覧のカード画像が、詳細画面へ飛んでいくように動かす。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    mainViewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SharedTransitionLayout(modifier = modifier) {
        val sharedScope = this

        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.HOME.route
        ) {
            composable(TopLevelDestination.HOME.route) {
                val viewModel: HomeViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { HomeViewModel(container.nfcStatusProvider) }
                    }
                )
                HomeScreen(viewModel = viewModel)
            }

            composable(Routes.RESULT) {
                val result by mainViewModel.readResult.collectAsStateWithLifecycle()
                val current = result
                if (current == null) {
                    // 結果がない(プロセスが再作成されたなど)ときは、前の画面に戻る
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    val viewModel: ResultViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer { ResultViewModel(container.cardRepository) }
                        }
                    )
                    ResultScreen(
                        result = current,
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Routes.OTHER_RESULT) {
                val result by mainViewModel.otherResult.collectAsStateWithLifecycle()
                val current = result
                if (current == null) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    OtherResultScreen(
                        result = current,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable(TopLevelDestination.HISTORY.route) {
                val viewModel: HistoryViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { HistoryViewModel(container.cardRepository, container.settingsRepository) }
                    }
                )
                HistoryScreen(
                    viewModel = viewModel,
                    onOpenCard = { cardId -> navController.navigate(Routes.detail(cardId)) },
                    transition = CardTransition(sharedScope, this)
                )
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument(Routes.ARG_CARD_ID_NAME) { type = NavType.StringType })
            ) { entry ->
                val cardId = entry.arguments?.getString(Routes.ARG_CARD_ID_NAME).orEmpty()
                // カードごとに別の ViewModel にする(key にカードIDを使う)
                val viewModel: CardDetailViewModel = viewModel(
                    key = "detail-$cardId",
                    factory = viewModelFactory {
                        initializer { CardDetailViewModel(cardId, container.cardRepository) }
                    }
                )
                CardDetailScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onBack = { navController.popBackStack() },
                    transition = CardTransition(sharedScope, this)
                )
            }

            composable(TopLevelDestination.SETTINGS.route) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { SettingsViewModel(container.settingsRepository) }
                    }
                )
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}