package debts.core.repository

import debts.core.db.DebtEntity
import debts.core.db.DebtorEntity
import debts.core.repository.data.DebtModel
import debts.core.repository.data.DebtorModel

fun DebtorEntity.toDebtorModel() =
    DebtorModel(
        id,
        name,
        contactId,
        avatarUrl
    )

fun DebtEntity.toDebtModel() =
    DebtModel(
        id,
        debtorId,
        amount,
        currency,
        date,
        comment
    )

fun DebtorModel.toDebtorEntity() =
    DebtorEntity(
        id,
        contactId,
        name,
        avatarUrl,
        "",
        ""
    )
