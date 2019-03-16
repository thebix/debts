package debts.db

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Database(
    version = DebtsDatabase.DB_VERSION,
    exportSchema = true,
    entities = [
        DebtorEntity::class,
        DebtEntity::class
    ]
)
abstract class DebtsDatabase : RoomDatabase() {

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "debts-db"
    }

    abstract fun debtsDao(): DebtsDao
}

@Dao
abstract class DebtsDao {

    @Insert
    abstract fun insertDebtor(debtorEntity: DebtorEntity): Single<Long>

    @Insert
    abstract fun insertDebt(debtEntity: DebtEntity): Single<Long>

    @Update
    abstract fun updateDebtor(debtorEntity: DebtorEntity): Completable

    @Update
    abstract fun updateDebtor(debtEntity: DebtEntity): Completable

    @Delete
    abstract fun deleteDebtor(debtorEntity: DebtorEntity): Completable

    @Delete
    abstract fun deleteDebtor(debtEntity: DebtEntity): Completable

    @Query("DELETE FROM ${DebtorEntity.TABLE_NAME} WHERE ${DebtorEntity.ID} = :id")
    abstract fun deleteDebtor(id: Long)

    @Query("DELETE FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.DEBTOR_ID} = :debtorId")
    abstract fun clearAllDebts(debtorId: Long)

    @Query("SELECT * FROM ${DebtorEntity.TABLE_NAME}")
    abstract fun observeDebtors(): Observable<List<DebtorEntity>>

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME}")
    abstract fun observeDebts(): Observable<List<DebtEntity>>
}
