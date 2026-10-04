package debts.feature.home

import debts.core.repository.SortType
import debts.core.usecase.data.DebtorsListItemModel
import debts.core.usecase.data.TabTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeListBuilderTest {

    private val anna = debtor(id = 1, name = "Anna", amount = 30.0)
    private val bob = debtor(id = 2, name = "Bob", amount = -50.0)
    private val carl = debtor(id = 3, name = "Carl", amount = 10.0)
    private val dave = debtor(id = 4, name = "Dave", amount = -5.0)
    private val all = listOf(carl, bob, anna, dave)

    @Test
    fun `all tab splits debtors and creditors into sections with headers`() {
        val tab = buildHomeTab(all, TabTypes.All, "", SortType.NOTHING)

        assertEquals(
            listOf("header:Debtors", "Carl", "Anna", "header:Creditors", "Bob", "Dave"),
            tab.items.labels(),
        )
    }

    @Test
    fun `all tab has no header for an empty section`() {
        val tab = buildHomeTab(listOf(bob, dave), TabTypes.All, "", SortType.NOTHING)

        assertEquals(listOf("header:Creditors", "Bob", "Dave"), tab.items.labels())
    }

    @Test
    fun `zero amount belongs to debtors`() {
        val zero = debtor(id = 5, name = "Zero", amount = 0.0)

        assertEquals(listOf("Zero"), buildHomeTab(listOf(zero), TabTypes.Debtors, "", SortType.NOTHING).items.labels())
        assertTrue(buildHomeTab(listOf(zero), TabTypes.Creditors, "", SortType.NOTHING).items.isEmpty())
    }

    @Test
    fun `debtors and creditors tabs have no headers`() {
        assertEquals(listOf("Carl", "Anna"), buildHomeTab(all, TabTypes.Debtors, "", SortType.NOTHING).items.labels())
        assertEquals(listOf("Bob", "Dave"), buildHomeTab(all, TabTypes.Creditors, "", SortType.NOTHING).items.labels())
    }

    @Test
    fun `amounts are shown as absolute values`() {
        val tab = buildHomeTab(all, TabTypes.Creditors, "", SortType.NOTHING)

        assertEquals(listOf(50.0, 5.0), tab.items.filterIsInstance<HomeListItem.Debtor>().map { it.amount })
    }

    @Test
    fun `search ignores case and surrounding spaces`() {
        val tab = buildHomeTab(all, TabTypes.All, "  aN ", SortType.NOTHING)

        assertEquals(listOf("header:Debtors", "Anna"), tab.items.labels())
    }

    @Test
    fun `blank search keeps all items`() {
        assertEquals(4, buildHomeTab(all, TabTypes.All, "   ", SortType.NOTHING).debtorsCount())
    }

    @Test
    fun `sorting by name is applied inside each section`() {
        assertEquals(
            listOf("header:Debtors", "Anna", "Carl", "header:Creditors", "Bob", "Dave"),
            buildHomeTab(all, TabTypes.All, "", SortType.NAME_ASC).items.labels(),
        )
        assertEquals(
            listOf("header:Debtors", "Carl", "Anna", "header:Creditors", "Dave", "Bob"),
            buildHomeTab(all, TabTypes.All, "", SortType.NAME_DESC).items.labels(),
        )
    }

    @Test
    fun `sorting by amount uses absolute values`() {
        assertEquals(
            listOf("Dave", "Bob"),
            buildHomeTab(all, TabTypes.Creditors, "", SortType.AMOUNT_ASC).items.labels(),
        )
        assertEquals(
            listOf("Bob", "Dave"),
            buildHomeTab(all, TabTypes.Creditors, "", SortType.AMOUNT_DESC).items.labels(),
        )
        assertEquals(
            listOf("Anna", "Carl"),
            buildHomeTab(all, TabTypes.Debtors, "", SortType.AMOUNT_DESC).items.labels(),
        )
    }

    @Test
    fun `total is signed on all and debtors tabs and absolute on creditors tab`() {
        assertEquals(-15.0, buildHomeTab(all, TabTypes.All, "", SortType.NOTHING).total, 0.0)
        assertEquals(40.0, buildHomeTab(all, TabTypes.Debtors, "", SortType.NOTHING).total, 0.0)
        assertEquals(55.0, buildHomeTab(all, TabTypes.Creditors, "", SortType.NOTHING).total, 0.0)
    }

    @Test
    fun `total counts only items matching the search`() {
        assertEquals(-50.0, buildHomeTab(all, TabTypes.All, "bob", SortType.NOTHING).total, 0.0)
    }

    @Test
    fun `empty input gives empty tabs with zero total`() {
        val tabs = buildHomeTabs(emptyList(), "", SortType.NOTHING)

        assertEquals(TabTypes.entries.toSet(), tabs.keys)
        tabs.values.forEach { tab ->
            assertTrue(tab.items.isEmpty())
            assertEquals(0.0, tab.total, 0.0)
        }
    }

    private fun HomeTabUiState.debtorsCount() = items.count { it is HomeListItem.Debtor }

    private fun List<HomeListItem>.labels() = map { item ->
        when (item) {
            is HomeListItem.Header -> "header:${item.section}"
            is HomeListItem.Debtor -> item.name
        }
    }

    private fun debtor(id: Long, name: String, amount: Double) = DebtorsListItemModel.Debtor(
        id = id,
        name = name,
        amount = amount,
        currency = "$",
        lastDate = 0L,
        avatarUrl = "",
    )
}
