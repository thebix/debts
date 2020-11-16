@file:Suppress("TooManyFunctions")

package debts.common.android.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.BoolRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import net.thebix.debts.R
import timber.log.Timber

// region Resources

@ColorInt
fun Context.getColorCompat(@ColorRes colorRes: Int): Int = ContextCompat.getColor(this, colorRes)

fun Context.getDimensionCompat(@DimenRes resId: Int): Float = resources.getDimension(resId)

/**
 * Size conversion involves rounding the base value, and ensuring that a non-zero base value
 * is at least one pixel in size.
 */
fun Context.getDimensionPixelSizeCompat(@DimenRes resId: Int): Int = resources.getDimensionPixelSize(resId)

/**
 * An offset conversion involves simply truncating the base value to an integer.
 */
fun Context.getDimensionPixelOffsetCompat(@DimenRes resId: Int): Int = resources.getDimensionPixelOffset(resId)

fun Context.getIntCompat(@IntegerRes resId: Int): Int = resources.getInteger(resId)

fun Context.getIntArrayCompat(@ArrayRes resId: Int): IntArray = resources.getIntArray(resId)

fun Context.getDrawableCompat(@DrawableRes resId: Int): Drawable = AppCompatResources.getDrawable(this, resId)!!

fun Context.getBoolean(@BoolRes resId: Int): Boolean = resources.getBoolean(resId)

// endregion

// region Activity

fun Context.tryToFindActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        } else {
            context = context.baseContext
        }
    }

    return null
}

fun Context.tryToGoBack() {
    val activity = this.tryToFindActivity()
    activity?.onBackPressed()
}

// endregion

// region Permissions
/**
 * Returns true if passed [permission] is granted.
 */
fun Context.isPermissionGranted(permission: String): Boolean {
    val permissionResult = try {
        ContextCompat.checkSelfPermission(this, permission)
    } catch (unexpected: Throwable) { // Unknown exception code: 1 msg null
        // issue discussions:
        //    https://github.com/permissions-dispatcher/PermissionsDispatcher/issues/107
        //    https://github.com/Karumi/Dexter/issues/86
        Timber.e(UnknownError("Unexpected exception occurred while checking $permission permission."))
        PackageManager.PERMISSION_DENIED
    }
    return permissionResult == PackageManager.PERMISSION_GRANTED
}
// endregion

// region AlertDialog

@Suppress("LongParameterList")
fun Context.showAlert(
    @StringRes messageId: Int = 0,
    isCancelable: Boolean = true,
    @StringRes titleResId: Int = R.string.default_dialog_title,
    customView: View? = null,
    @StringRes positiveButtonResId: Int = R.string.default_positive_button,
    @StringRes negativeButtonResId: Int = R.string.default_negative_button,
    actionNegative: (() -> Unit)? = null,
    actionPositive: (() -> Unit)? = null
): AlertDialog {
    val builder = AlertDialog
        .Builder(this)
    if (customView != null)
        builder.setView(customView)
    if (titleResId != 0) {
        builder.setTitle(titleResId)
    }
    if (messageId != 0) {
        builder.setMessage(messageId)
    }
    if (positiveButtonResId != 0) {
        builder.setPositiveButton(positiveButtonResId) { _, _ ->
            actionPositive?.invoke()
        }
    }
    if (negativeButtonResId != 0) {
        builder.setNegativeButton(negativeButtonResId) { _, _ ->
            actionNegative?.invoke()
        }
    }
    val dialog = builder.show()
    dialog.setCancelable(isCancelable)
    dialog.setCanceledOnTouchOutside(isCancelable)
    return dialog
}

// endregion

// region Explicit intentions

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
        .apply {
            data = Uri.parse(url)
        }
    ContextCompat.startActivity(this, intent, null)
}

fun Context.openUrl(@StringRes urlResId: Int) {
    val url = this.getString(urlResId)
    openUrl(url)
}

// endregion
