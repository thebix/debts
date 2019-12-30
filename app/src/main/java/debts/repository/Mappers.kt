package debts.repository

import debts.db.DebtEntity
import debts.db.DebtorEntity
import debts.usecase.DebtModel
import debts.usecase.DebtorModel

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
