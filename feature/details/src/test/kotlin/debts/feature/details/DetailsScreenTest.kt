package debts.feature.details

import androidx.compose.material3.SnackbarHostState
import coil.Coil
import coil.ImageLoader
import com.github.takahirom.roborazzi.captureRoboImage
import debts.core.resource.theme.AppTheme
import debts.core.usecase.data.DebtItemModel
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Calendar

private const val SCREENSHOTS = "src/test/screenshots"
private const val CLASS = "debts.feature.details.DetailsScreenTest"

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33], qualifiers = "w360dp-h800dp-normal-notlong-notround-port-notnight-mdpi-finger")
class DetailsScreenTest {

    private val debts = listOf(
        DebtItemModel(1, 132.23, "€", localTime(day = 14, hour = 18, minute = 5), "Dinner at the place near the office"),
        DebtItemModel(2, -50.0, "€", localTime(day = 9, hour = 9, minute = 30), ""),
        DebtItemModel(3, 9898.0, "€", localTime(day = 2, hour = 12, minute = 0), "Rent"),
    )

    /**
     * Coil loads images on background dispatchers, and the capture does not wait for them:
     * a snapshot could be taken before the avatar appears. Loading on the calling thread
     * makes the avatar part of every snapshot.
     */
    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        Coil.setImageLoader(
            ImageLoader.Builder(context)
                .dispatcher(Dispatchers.Unconfined)
                .interceptorDispatcher(Dispatchers.Unconfined)
                .build()
        )
    }

    @After
    fun tearDown() {
        Coil.reset()
    }

    @Test
    fun detailsScreen_expanded() = capture("detailsScreen_expanded", uiState())

    @Test
    fun detailsScreen_collapsed() = capture("detailsScreen_collapsed", uiState(), collapseFraction = 1f)

    @Test
    fun detailsScreen_emptyHistory() = capture("detailsScreen_emptyHistory", uiState(debts = emptyList(), amount = 0.0))

    @Test
    fun detailsScreen_collapsedLongName() = capture(
        "detailsScreen_collapsedLongName",
        uiState(name = "John Zorn John Zorn John Zorn John Zorn John Zorn"),
        collapseFraction = 1f,
    )

    @Test
    fun detailsScreen_expandedLongName() = capture(
        "detailsScreen_expandedLongName",
        uiState(name = "John Zorn John Zorn John Zorn John Zorn John Zorn"),
    )

    private fun capture(name: String, uiState: DetailsUiState, collapseFraction: Float = 0f) {
        captureRoboImage("$SCREENSHOTS/$CLASS.$name.png") {
            AppTheme {
                DetailsScreen(
                    uiState = uiState,
                    snackbarHostState = SnackbarHostState(),
                    actions = DetailsActions(),
                    initialCollapseFraction = collapseFraction,
                )
            }
        }
    }

    private fun uiState(
        name: String = "John Zorn",
        debts: List<DebtItemModel> = this.debts,
        amount: Double = 9980.23,
    ) = DetailsUiState(
        name = name,
        avatarUrl = "",
        amount = amount,
        currency = "€",
        debts = debts,
    )

    // Rows show the time of day, so the timestamps are built in the default time zone to render the same everywhere.
    private fun localTime(day: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(2023, Calendar.NOVEMBER, day, hour, minute)
        }.timeInMillis
}
