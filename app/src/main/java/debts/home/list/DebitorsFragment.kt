package debts.home.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import debts.common.android.BaseFragment
import debts.home.details.DetailsFragment
import net.thebix.debts.R

class DebitorsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.home_debitors_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<View>(R.id.home_debitors_details)?.setOnClickListener {
            addFragment(DetailsFragment(), R.id.home_root)
        }
    }
}
