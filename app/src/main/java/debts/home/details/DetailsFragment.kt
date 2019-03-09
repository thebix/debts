package debts.home.details

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import debts.common.android.BaseFragment
import net.thebix.debts.R
import net.thebix.debts.activities.MainActivity

class DetailsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflater.inflate(R.layout.home_details_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<View>(R.id.home_details_legacy)?.setOnClickListener {
            startActivity(Intent(activity?.applicationContext, MainActivity::class.java))
        }
    }
}
