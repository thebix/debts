package debts.home.list.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import debts.home.list.DebtorsFragment

class DebtsPagerAdapter(
    fm: FragmentManager,
    private val titles: List<String>
) : FragmentStatePagerAdapter(fm) {

    private companion object {
        const val NUM_PAGES = 3
    }

    override fun getCount(): Int = NUM_PAGES

    override fun getItem(position: Int): Fragment {
        return DebtorsFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? = if (position > titles.size - 1) "" else titles[position]
}
