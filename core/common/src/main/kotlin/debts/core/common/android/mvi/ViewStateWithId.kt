package debts.core.common.android.mvi

import java.util.concurrent.atomic.AtomicLong

open class ViewStateWithId {

    val viewStateId: Long = getNextId()

    companion object {

        private val lastViewStateId = AtomicLong(1L)

        fun getNextId() = lastViewStateId.getAndIncrement()
    }
}
