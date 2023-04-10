@file:Suppress("TooManyFunctions")

package debts.common.android.extensions

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.ViewCompat

// region Helpers

@Suppress("PropertyName", "unused")
val View.MATCH_PARENT
    get() = ViewGroup.LayoutParams.MATCH_PARENT

@Suppress("PropertyName", "unused")
val View.WRAP_CONTENT
    get() = ViewGroup.LayoutParams.WRAP_CONTENT

/**
 * Applies [width] & [height] to existing [View.getLayoutParams] and performs [View.setLayoutParams].
 *
 * If [View.getLayoutParams] == null, then [ViewGroup.MarginLayoutParams] with [width] & [height] will be created.
 */
fun View.applyLayoutParams(width: Int = MATCH_PARENT, height: Int = WRAP_CONTENT) {
    layoutParams = this.layoutParams?.apply {
        this.width = width
        this.height = height
    } ?: ViewGroup.MarginLayoutParams(width, height)
}

fun <V : View?> View.findView(@IdRes resId: Int): V = findViewById<V>(resId)

inline fun View.doInRuntime(code: () -> Unit) {
    if (!isInEditMode) code()
}

fun View.getActivity(): Activity? = context.tryToFindActivity()

// endregion

// region Visibility

fun View.isVisible() = visibility == View.VISIBLE
fun View.setVisible() {
    if (!isVisible()) visibility = View.VISIBLE
}

fun View.isInvisible() = visibility == View.INVISIBLE
fun View.setInvisible() {
    if (!isInvisible()) visibility = View.INVISIBLE
}

fun View.isGone() = visibility == View.GONE
fun View.setGone() {
    if (!isGone()) visibility = View.GONE
}

/**
 * set:
 * true will set visibility to VISIBLE.
 * false will set visibility GONE
 *
 * get: returns true if visibility == VISIBLE, false otherwise
 */
var View.visible
    get() = isVisible()
    set(value) = if (value) setVisible() else setGone()

// endregion

// region Margins

fun View.setMarginStartResCompat(@DimenRes marginRes: Int) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    MarginLayoutParamsCompat.setMarginStart(
        params,
        context.getDimensionPixelOffsetCompat(marginRes)
    )
    layoutParams = params
}

fun View.setMarginTopResCompat(@DimenRes marginRes: Int) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    params.topMargin = context.getDimensionPixelOffsetCompat(marginRes)
    layoutParams = params
}

fun View.setMarginEndResCompat(@DimenRes marginRes: Int) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    MarginLayoutParamsCompat.setMarginEnd(params, context.getDimensionPixelOffsetCompat(marginRes))
    layoutParams = params
}

fun View.setMarginBottomResCompat(@DimenRes marginRes: Int) {
    val params = layoutParams as? ViewGroup.MarginLayoutParams ?: return
    params.bottomMargin = context.getDimensionPixelOffsetCompat(marginRes)
    layoutParams = params
}

// endregion

// region Paddings

fun View.setPaddingStartResCompat(@DimenRes paddingRes: Int) {
    setPaddingStartCompat(context.getDimensionPixelOffsetCompat(paddingRes))
}

fun View.setPaddingStartCompat(padding: Int) {
    ViewCompat.setPaddingRelative(
        this,
        padding,
        paddingTop,
        ViewCompat.getPaddingEnd(this),
        paddingBottom
    )
}

fun View.setPaddingTopResCompat(@DimenRes paddingRes: Int) {
    setPaddingTopCompat(context.getDimensionPixelOffsetCompat(paddingRes))
}

fun View.setPaddingTopCompat(padding: Int) {
    ViewCompat.setPaddingRelative(
        this,
        ViewCompat.getPaddingStart(this),
        padding,
        ViewCompat.getPaddingEnd(this),
        paddingBottom
    )
}

fun View.setPaddingEndResCompat(@DimenRes paddingRes: Int) {
    setPaddingEndCompat(context.getDimensionPixelOffsetCompat(paddingRes))
}

fun View.setPaddingEndCompat(padding: Int) {
    ViewCompat.setPaddingRelative(
        this,
        ViewCompat.getPaddingStart(this),
        paddingTop,
        padding,
        paddingBottom
    )
}

fun View.setPaddingBottomResCompat(@DimenRes paddingRes: Int) {
    setPaddingBottomCompat(context.getDimensionPixelOffsetCompat(paddingRes))
}

fun View.setPaddingBottomCompat(padding: Int) {
    ViewCompat.setPaddingRelative(
        this,
        ViewCompat.getPaddingStart(this),
        paddingTop,
        ViewCompat.getPaddingEnd(this),
        padding
    )
}

// endregion

// region Background

fun View.setSelectableItemBackground() {
    val outValue = TypedValue()
    context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
    this.setBackgroundResource(outValue.resourceId)
}

// endregion

// region Keyboard management

fun View.hideKeyboard(delay: Long = 64L) {
    getActivity()?.let { activity ->
        val focusedView = activity.currentFocus ?: this
        focusedView.clearFocus()

        if (delay > 0) {
            focusedView.postDelayed(
                {
                    val inputMethodManager =
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(
                        windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                },
                delay
            )
        } else {
            val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

/**
 * This method should be called on view to which keyboard will be attached.
 * So user will be able to immediately write content into view.
 */
fun EditText.showKeyboard(delay: Long = 64L) {
    getActivity()?.let { activity ->
        val focusedView = if (isFocusable) this else activity.currentFocus ?: this
        if (!focusedView.isFocused) {
            requestFocus()
        }
        if (delay > 0) {
            focusedView.postDelayed(
                {
                    val inputMethodManager =
                        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(focusedView, InputMethodManager.SHOW_IMPLICIT)
                },
                delay
            )
        } else {
            val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(focusedView, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

// endregion

fun View.showPopup(@MenuRes menuId: Int, menuItemClickListener: PopupMenu.OnMenuItemClickListener) {
    PopupMenu(this.context, this).apply {
        setOnMenuItemClickListener(menuItemClickListener)
        inflate(menuId)
        show()
    }
}

@JvmOverloads
fun View.setThrottlingClickListener(
    thresholdInterval: Int = 1000,
    onSafeClick: (View) -> Unit
) {

    class OnceClickListener(
        private val interval: Int,
        private val onCLick: (View) -> Unit
    ) : View.OnClickListener {
        private var lastTimeClicked: Long = 0
        override fun onClick(view: View) {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - lastTimeClicked < interval) {
                return
            }
            lastTimeClicked = currentTime
            onCLick(view)
        }
    }

    val safeClickListener = OnceClickListener(thresholdInterval) {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
