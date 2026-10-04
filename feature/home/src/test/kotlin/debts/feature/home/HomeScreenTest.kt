package debts.feature.home

import androidx.compose.material3.SnackbarHostState
import com.github.takahirom.roborazzi.captureRoboImage
import debts.core.repository.SortType
import debts.core.resource.theme.AppTheme
import debts.core.usecase.data.DebtorsListItemModel
import debts.core.usecase.data.TabTypes
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SCREENSHOTS = "src/test/screenshots"
private const val CLASS = "debts.feature.home.HomeScreenTest"

// 14 November 2023, noon UTC: the same day in every time zone the tests may run in.
private const val LAST_DATE = 1_699_963_200_000L

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-normal-notlong-notround-port-notnight-mdpi-finger")
class HomeScreenTest {

    private val debtors = listOf(
        debtor(1, "John Zorn", 132.23),
        debtor(2, "Anna", 9898.0),
        debtor(3, "Dad", -9873.0),
        debtor(4, "No debts yet", 0.0, lastDate = Long.MIN_VALUE),
    )

    @Test
    fun homeScreen_allTab() = capture("homeScreen_allTab", uiState(debtors))

    @Test
    fun homeScreen_empty() = capture("homeScreen_empty", uiState(emptyList()))

    @Test
    fun homeScreen_debtorsTab() = capture(
        "homeScreen_debtorsTab",
        uiState(debtors, sortType = SortType.NAME_ASC),
        initialTab = TabTypes.Debtors,
    )

    @Test
    fun homeScreen_search() = capture("homeScreen_search", uiState(debtors, searchQuery = "an", isSearchActive = true))

    private fun capture(name: String, uiState: HomeUiState, initialTab: TabTypes = TabTypes.All) {
        captureRoboImage("$SCREENSHOTS/$CLASS.$name.png") {
            AppTheme {
                HomeScreen(
                    uiState = uiState,
                    snackbarHostState = SnackbarHostState(),
                    actions = HomeActions(),
                    initialTab = initialTab,
                )
            }
        }
    }

    private fun uiState(
        debtors: List<DebtorsListItemModel.Debtor>,
        sortType: SortType = SortType.NOTHING,
        searchQuery: String = "",
        isSearchActive: Boolean = false,
    ) = HomeUiState(
        tabs = buildHomeTabs(debtors, searchQuery, sortType),
        currency = "€",
        sortType = sortType,
        searchQuery = searchQuery,
        isSearchActive = isSearchActive,
    )

    private fun debtor(id: Long, name: String, amount: Double, lastDate: Long = LAST_DATE) =
        DebtorsListItemModel.Debtor(
            id = id,
            name = name,
            amount = amount,
            currency = "€",
            lastDate = lastDate,
            avatarUrl = "",
        )
}
