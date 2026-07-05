package debts.core.common.android.mvvm

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private data class TestState(val value: Int = 0)

    private sealed class TestEvent {
        data class Message(val text: String) : TestEvent()
    }

    private class TestViewModel : BaseViewModel<TestState, TestEvent>(TestState()) {
        fun increment() = updateState { copy(value = value + 1) }
        fun emitMessage(text: String) = sendEvent(TestEvent.Message(text))
    }

    @Test
    fun `initial state is set correctly`() {
        val vm = TestViewModel()
        assertEquals(TestState(0), vm.uiState.value)
    }

    @Test
    fun `updateState transforms state`() {
        val vm = TestViewModel()
        vm.increment()
        assertEquals(TestState(1), vm.uiState.value)
    }

    @Test
    fun `multiple state updates accumulate correctly`() {
        val vm = TestViewModel()
        vm.increment()
        vm.increment()
        vm.increment()
        assertEquals(TestState(3), vm.uiState.value)
    }

    @Test
    fun `sendEvent delivers event to collector`() = runTest {
        val vm = TestViewModel()
        vm.events.test {
            vm.emitMessage("hello")
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(TestEvent.Message("hello"), awaitItem())
        }
    }

    @Test
    fun `multiple events are delivered in order`() = runTest {
        val vm = TestViewModel()
        vm.events.test {
            vm.emitMessage("first")
            vm.emitMessage("second")
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(TestEvent.Message("first"), awaitItem())
            assertEquals(TestEvent.Message("second"), awaitItem())
        }
    }

    @Test
    fun `uiState reflects latest update`() {
        val vm = TestViewModel()
        vm.increment()
        vm.increment()
        assertEquals(2, vm.uiState.value.value)
        vm.increment()
        assertEquals(3, vm.uiState.value.value)
    }
}
