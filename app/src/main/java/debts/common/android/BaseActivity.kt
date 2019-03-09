package debts.common.android

import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {

    fun replaceFragment(fragment: Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .replace(rootId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commitNow()
    }

    fun addFragment(fragment: Fragment, @IdRes rootId: Int, addToBackStack: Boolean = true) {
        val fragmentTransaction = supportFragmentManager
            .beginTransaction()
            .add(rootId, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }
}
