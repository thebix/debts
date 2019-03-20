package debts.common.android

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

abstract class BaseActivity : AppCompatActivity() {

    fun replaceFragment(
        fragment: Fragment, @IdRes rootId: Int,
        addToBackStack: Boolean = true,
        animations: List<Int> = listOf()
    ) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
        if (animations.isNotEmpty()) {
            when (animations.size) {
                2 -> fragmentTransaction.setCustomAnimations(animations[0], animations[1])
                4 -> fragmentTransaction.setCustomAnimations(
                    animations[0],
                    animations[1],
                    animations[2],
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
        fragment: Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true,
        animations: List<Int> = listOf()
    ) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
        if (animations.isNotEmpty()) {
            when (animations.size) {
                2 -> fragmentTransaction.setCustomAnimations(animations[0], animations[1])
                4 -> fragmentTransaction.setCustomAnimations(
                    animations[0],
                    animations[1],
                    animations[2],
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
