package debts.feature.contacts.adapter

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContactsItemViewModel(
    val id: Long,
    val name: String,
    val avatarUrl: String,
) : Parcelable
