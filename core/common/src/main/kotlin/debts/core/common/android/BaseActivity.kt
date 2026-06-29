package debts.core.common.android

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment

abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // Disable edge-to-edge for the transitional period while Views still lack insets handling.
        // Must be called AFTER super.onCreate(): Android 15 (API 35) auto-calls
        // setDecorFitsSystemWindows(false) inside Activity.onCreate() for targetSdk 35+,
        // which would override a pre-super call. Remove in U2.6 when all screens are on Compose.
        WindowCompat.setDecorFitsSystemWindows(window, true)
    }

    fun replaceFragment(
        fragment: Fragment,
        @IdRes rootId: Int,
        addToBackStack: Boolean = true,
        animations: List<Int> = listOf()
    ) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
        if (animations.isNotEmpty()) {
            when (animations.size) {
                2 -> fragmentTransaction.setCustomAnimations(animations[0], animations[1])
                @Suppress("MagicNumber")
                4 -> fragmentTransaction.setCustomAnimations(
                    animations[0],
                    animations[1],
                    animations[2],
                    @Suppress("MagicNumber")
                    animations[3]
                )
                else -> {
                    // no-op
                }
            }
        }
        fragmentTransaction.replace(rootId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }

    fun addFragment(
        fragment: Fragment,
        @IdRes rootId: Int,
        addToBackStack: Boolean = true,
        animations: List<Int> = listOf()
    ) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
        if (animations.isNotEmpty()) {
            when (animations.size) {
                2 -> fragmentTransaction.setCustomAnimations(animations[0], animations[1])
                @Suppress("MagicNumber")
                4 -> fragmentTransaction.setCustomAnimations(
                    animations[0],
                    animations[1],
                    animations[2],
                    @Suppress("MagicNumber")
                    animations[3]
                )
                else -> {
                    // no-op
                }
            }
        }
        fragmentTransaction.add(rootId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }
}
