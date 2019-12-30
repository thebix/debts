package debts.db

import androidx.room.*

@Entity(
    tableName = DebtorEntity.TABLE_NAME
)
data class DebtorEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long,

    @ColumnInfo(name = CONTACT_ID)
    val contactId: Long?,

    @ColumnInfo(name = NAME)
    val name: String,

    @ColumnInfo(name = AVATAR)
    val avatarUrl: String,

    // region legacy columns for backward compatibility

    @ColumnInfo(name = EMAIL)
    val email: String,

    @ColumnInfo(name = PHONE)
    val phone: String,

    // INFO: just to migrate from old DB. So can be removed with migration 2_3
    @ColumnInfo(name = LEGACY_ID)
    val legacyId: Long? = null

    // endregion

) {
    companion object {
        const val TABLE_NAME = "debtors_table"
        const val ID = "id"
        const val CONTACT_ID = "contactId"
        const val NAME = "name"
        const val AVATAR = "avatar"
        const val EMAIL = "email"
        const val PHONE = "phone"
        const val LEGACY_ID = "legacy_id"
    }
}

@Entity(
    tableName = DebtEntity.TABLE_NAME,
    indices = [
        Index(value = [DebtEntity.DEBTOR_ID])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DebtorEntity::class,
            parentColumns = [(DebtorEntity.ID)],
            childColumns = [(DebtEntity.DEBTOR_ID)],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ID)
    val id: Long,

    @ColumnInfo(name = DEBTOR_ID)
    val debtorId: Long,

    @ColumnInfo(name = AMOUNT)
    val amount: Double,

    @ColumnInfo(name = CURRENCY)
    val currency: String,

    @ColumnInfo(name = DATE)
    val date: Long,

    @ColumnInfo(name = COMMENT)
    val comment: String
) {
    companion object {
        const val TABLE_NAME = "debts_table"
        const val ID = "id"
        const val DEBTOR_ID = "debtorId"
        const val AMOUNT = "amount"
        const val CURRENCY = "currency"
        const val DATE = "date"
        const val COMMENT = "comment"
    }
}
