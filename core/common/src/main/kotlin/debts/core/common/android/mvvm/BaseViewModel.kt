package debts.core.common.android.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<S, E>(initialState: S) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    protected fun updateState(transform: S.() -> S) {
        _uiState.update(transform)
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
}
