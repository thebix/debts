package debts.common.android

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    fun replaceFragment(fragment: androidx.fragment.app.Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .replace(rootId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }

    fun addFragment(fragment: androidx.fragment.app.Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .add(rootId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }
}
