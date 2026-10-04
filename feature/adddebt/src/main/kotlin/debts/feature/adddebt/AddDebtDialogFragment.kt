package debts.feature.adddebt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import android.view.WindowManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import debts.core.resource.theme.AppTheme
import debts.feature.contacts.adapter.ContactsItemViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddDebtDialogFragment : DialogFragment() {

    private val viewModel: AddDebtViewModel by viewModel()

    var onConfirmListener: ((DebtLayoutData) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    AddDebtContent(
                        uiState = uiState,
                        isEdit = requireArguments().getBoolean(ARG_IS_EDIT, false),
                        onNameChanged = viewModel::onNameChanged,
                        onContactSelected = viewModel::onContactSelected,
                        onAmountChanged = viewModel::onAmountChanged,
                        onSubtractChanged = viewModel::onSubtractChanged,
                        onCommentChanged = viewModel::onCommentChanged,
                        onCalendarClicked = viewModel::onCalendarClicked,
                        onDateSelected = viewModel::onDateSelected,
                        onDatePickerDismissed = viewModel::onDatePickerDismissed,
                        onConfirm = {
                            onConfirmListener?.invoke(viewModel.buildResult())
                            dismiss()
                        },
                        onDismiss = ::dismiss,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            initViewModel()
        }
    }

    @Suppress("DEPRECATION")
    private fun initViewModel() {
        val args = requireArguments()
        val contacts: List<ContactsItemViewModel> =
            args.getParcelableArrayList(ARG_CONTACTS) ?: emptyList()
        val existingId = args.getLong(ARG_EXISTING_DEBT_ID, -1L).let { if (it == -1L) null else it }
        viewModel.init(
            name = args.getString(ARG_NAME, ""),
            avatarUrl = args.getString(ARG_AVATAR_URL, ""),
            amount = args.getDouble(ARG_AMOUNT, 0.0),
            comment = args.getString(ARG_COMMENT, ""),
            dateMs = args.getLong(ARG_DATE_MS, System.currentTimeMillis()),
            contacts = contacts,
            existingDebtId = existingId,
            canChangeDebtor = args.getBoolean(ARG_CAN_CHANGE_DEBTOR, true),
        )
    }

    companion object {
        const val TAG = "AddDebtDialogFragment"

        private const val ARG_IS_EDIT = "is_edit"
        private const val ARG_NAME = "name"
        private const val ARG_AVATAR_URL = "avatar_url"
        private const val ARG_AMOUNT = "amount"
        private const val ARG_COMMENT = "comment"
        private const val ARG_DATE_MS = "date_ms"
        private const val ARG_EXISTING_DEBT_ID = "existing_debt_id"
        private const val ARG_CAN_CHANGE_DEBTOR = "can_change_debtor"
        private const val ARG_CONTACTS = "contacts"

        fun newAddDebt(
            name: String = "",
            avatarUrl: String = "",
            contacts: List<ContactsItemViewModel> = emptyList(),
            canChangeDebtor: Boolean = true,
        ): AddDebtDialogFragment = AddDebtDialogFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_IS_EDIT, false)
                putString(ARG_NAME, name)
                putString(ARG_AVATAR_URL, avatarUrl)
                putBoolean(ARG_CAN_CHANGE_DEBTOR, canChangeDebtor)
                putLong(ARG_DATE_MS, System.currentTimeMillis())
                putDouble(ARG_AMOUNT, 0.0)
                putString(ARG_COMMENT, "")
                putParcelableArrayList(ARG_CONTACTS, ArrayList(contacts))
            }
        }

        fun newEditDebt(
            name: String,
            avatarUrl: String,
            amount: Double,
            comment: String,
            date: Long,
            existingDebtId: Long,
        ): AddDebtDialogFragment = AddDebtDialogFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_IS_EDIT, true)
                putString(ARG_NAME, name)
                putString(ARG_AVATAR_URL, avatarUrl)
                putDouble(ARG_AMOUNT, amount)
                putString(ARG_COMMENT, comment)
                putLong(ARG_DATE_MS, date)
                putLong(ARG_EXISTING_DEBT_ID, existingDebtId)
                putBoolean(ARG_CAN_CHANGE_DEBTOR, false)
                putParcelableArrayList(ARG_CONTACTS, ArrayList<ContactsItemViewModel>())
            }
        }
    }
}
