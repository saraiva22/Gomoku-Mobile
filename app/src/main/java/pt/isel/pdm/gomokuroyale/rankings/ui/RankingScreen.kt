package pt.isel.pdm.gomokuroyale.rankings.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import pt.isel.pdm.gomokuroyale.R
import pt.isel.pdm.gomokuroyale.http.domain.users.UserRanking
import pt.isel.pdm.gomokuroyale.http.domain.users.unitsConverter
import pt.isel.pdm.gomokuroyale.rankings.domain.RankingScreenState
import pt.isel.pdm.gomokuroyale.rankings.domain.RankingScreenState.FetchedPlayerInfo
import pt.isel.pdm.gomokuroyale.rankings.domain.RankingScreenState.FetchingRankingInfo
import pt.isel.pdm.gomokuroyale.ui.NavigationHandlers
import pt.isel.pdm.gomokuroyale.ui.TopBar
import pt.isel.pdm.gomokuroyale.ui.components.LoadingView
import pt.isel.pdm.gomokuroyale.ui.components.MyIcon
import pt.isel.pdm.gomokuroyale.ui.components.MySearchBar
import pt.isel.pdm.gomokuroyale.ui.components.UserInfoPopUp
import pt.isel.pdm.gomokuroyale.ui.theme.DarkViolet
import pt.isel.pdm.gomokuroyale.ui.theme.GomokuRoyaleTheme
import pt.isel.pdm.gomokuroyale.util.Term
import pt.isel.pdm.gomokuroyale.util.toTermOrNull

const val RankingScreenTestTag = "RankingScreenTestTag"
const val SearchBarTestTag = "SEARCH_BAR_TEST_TAG"
const val RankingListTestTag = "RANKING_LIST_TEST_TAG"
const val FirstPage = 1
const val MAX_USERNAME_LENGTH = 10
const val MAX_USERNAME_LENGTH_SPACER = 13

@Composable
fun RankingScreen(
    vmState: RankingScreenState = FetchingRankingInfo,
    isRequestInProgress: Boolean = false,
    onBackRequested: () -> Unit = { },
    players: List<UserRanking> = listOf(),
    onPagedRequested: (Int) -> Unit = { },
    onMatchHistoryRequested: (Int, String) -> Unit = { _, _ -> },
    onSearchRequested: (Term) -> Unit = { },
    onPlayerSelected: (Int) -> Unit = { },
    onPlayerDismissed: () -> Unit = { },
    initialPage: Int = FirstPage,
    isLastPage: Boolean = false
) {
    GomokuRoyaleTheme {
        var query by rememberSaveable { mutableStateOf("") }
        val listState = rememberLazyListState()
        var currPage by rememberSaveable { mutableIntStateOf(initialPage) }
        var currPlayers by rememberSaveable { mutableStateOf(emptyList<UserRanking>()) }
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag(RankingScreenTestTag),
            topBar = {
                TopBar(
                    title = {
                        Spacer(modifier = Modifier.padding(5.dp))
                        Text(
                            text = stringResource(id = R.string.ranking_title),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigation = NavigationHandlers(onBackRequested = onBackRequested)
                )
            },
        ) { innerPadding ->
            RankingLazyColumn(
                query = query,
                isRequestInProgress = isRequestInProgress,
                listState = listState,
                rank = currPlayers,
                state = vmState,
                modifier = Modifier.padding(innerPadding),
                onMatchHistoryRequested = onMatchHistoryRequested,
                onSearchRequested = { query.toTermOrNull()?.let { onSearchRequested(it) } },
                onQueryChanged = { query = it },
                onClearSearch = { query = "" },
                onPlayerSelected = { playerId -> onPlayerSelected(playerId) },
                onPlayerDismissed = onPlayerDismissed,
                onPagedRequested = { nextPage ->
                    if (nextPage == currPage) return@RankingLazyColumn
                    onPagedRequested(nextPage)
                    currPage = nextPage
                    currPlayers = currPlayers + players
                },
                currentPage = currPage,
                isLastPage = isLastPage
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RankingLazyColumn(
    modifier: Modifier = Modifier,
    query: String,
    isRequestInProgress: Boolean = false,
    listState: LazyListState,
    state: RankingScreenState,
    rank: List<UserRanking>,
    onMatchHistoryRequested: (Int, String) -> Unit = { _, _ -> },
    onSearchRequested: (Term) -> Unit = { },
    onQueryChanged: (String) -> Unit = { },
    onClearSearch: () -> Unit = { },
    onPlayerSelected: (Int) -> Unit = { },
    onPlayerDismissed: () -> Unit = { },
    onPagedRequested: (Int) -> Unit = { },
    currentPage: Int = 1,
    isLastPage: Boolean = false
) {
    LazyColumn(
        userScrollEnabled = true,
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize().testTag(RankingListTestTag),
    ) {
        val mod = if (isRequestInProgress) Modifier.shimmer() else Modifier
        stickyHeader {
            MySearchBar(query, isRequestInProgress, onSearchRequested, onQueryChanged, onClearSearch)
            Spacer(modifier = Modifier.padding(5.dp))
        }
        rank.forEach { player ->
            item {
                PlayerView(mod, player, onPlayerSelected)
               // Spacer(modifier = Modifier.padding(1.dp))
            }
        }
        if (state is FetchingRankingInfo) {

            item {
                LoadingView()   //onLoadingView
            }
        }
        if (state is FetchedPlayerInfo) {
            item {
                UserInfoPopUp(
                    onDismissRequest = { },
                    onMatchHistoryRequested = onMatchHistoryRequested,
                    playerInfo = state.playerInfo,
                )
            }
        }
        val lastIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        if (lastIndex != null && lastIndex >= rank.size && !isRequestInProgress && !isLastPage) {

            onPagedRequested(currentPage + 1)
        }
    }
}

@Composable
fun PlayerView(
    modifier: Modifier,
    playerInfo: UserRanking,
    onPlayerSelected: (Int) -> Unit = { },
) {
    Card(
        shape = RectangleShape,
        modifier = modifier
            .fillMaxWidth(0.8f)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onPlayerSelected(playerInfo.id)
            },
        colors = CardDefaults.cardColors(containerColor = DarkViolet)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            when (val res = playerInfo.rank) {
                in top3.keys -> top3[res]?.invoke()
                else -> IconButton(onClick = { }, enabled = false) {
                    Text(
                        text = "${res}.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
            Text(
                text = if (playerInfo.username.isLimitUsername)
                    playerInfo.username.limitUsername else playerInfo.username.accommodateUsername,

                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = playerInfo.points.unitsConverter(),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            MyIcon(resultId = R.drawable.ic_coins, size = 45.dp)
        }
    }
}


private val String.isLimitUsername: Boolean
    get() = this.length >= MAX_USERNAME_LENGTH

private val String.limitUsername: String
    get() = this.substring(0, MAX_USERNAME_LENGTH)+"..."

private val String.accommodateUsername: String
    get() = this + " ".repeat(MAX_USERNAME_LENGTH_SPACER - this.length)

@Preview(showBackground = true)
@Composable
private fun LeaderboardPreview() {
    RankingScreen(
        vmState = FetchingRankingInfo,
        onBackRequested = {},
        onMatchHistoryRequested = { _, _ -> },
    )
}

private val top3 = mapOf<Int, @Composable () -> Unit>(
    1 to { MyIcon(resultId = R.drawable.first_place) },
    2 to { MyIcon(resultId = R.drawable.second_place) },
    3 to { MyIcon(resultId = R.drawable.third_place) }
)