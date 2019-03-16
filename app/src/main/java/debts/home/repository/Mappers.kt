package debts.home.repository

import debts.db.DebtEntity
import debts.db.DebtorEntity
import debts.home.usecase.DebtModel
import debts.home.usecase.DebtorModel

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
