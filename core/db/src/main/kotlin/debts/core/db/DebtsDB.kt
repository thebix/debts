package debts.core.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
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
        const val DB_VERSION = 2
        const val DB_NAME = "dc.db"
    }

    abstract fun debtsDao(): DebtsDao
}

@Dao
@Suppress("TooManyMethods", "TooManyFunctions")
abstract class DebtsDao {

    @Insert
    abstract fun insertDebtor(debtorEntity: DebtorEntity): Single<Long>

    @Insert
    abstract fun insertDebt(debtEntity: DebtEntity): Single<Long>

    @Update
    abstract fun updateDebtor(debtorEntity: DebtorEntity): Completable

    @Query("UPDATE ${DebtorEntity.TABLE_NAME} SET ${DebtorEntity.NAME} = :name, ${DebtorEntity.AVATAR} = :avatar WHERE ${DebtorEntity.ID} = :id")
    abstract fun updateDebtor(id: Long, name: String, avatar: String)

    @Transaction
    open fun updateDebtors(debtors: List<DebtorEntity>) {
        for (item in debtors) {
            updateDebtor(item.id, item.name, item.avatarUrl)
        }
    }

    @Update
    abstract fun updateDebt(debtEntity: DebtEntity): Completable

    @Query("UPDATE ${DebtEntity.TABLE_NAME} SET ${DebtEntity.CURRENCY} = :currency")
    abstract fun updateDebtsCurrency(currency: String)

    @Delete
    abstract fun deleteDebtor(debtorEntity: DebtorEntity): Completable

    @Delete
    abstract fun deleteDebt(debtEntity: DebtEntity): Completable

    @Query("DELETE FROM ${DebtorEntity.TABLE_NAME} WHERE ${DebtorEntity.ID} = :id")
    abstract fun deleteDebtor(id: Long): Completable

    @Query("DELETE FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.DEBTOR_ID} = :debtorId")
    abstract fun clearAllDebts(debtorId: Long): Completable

    @Query("SELECT * FROM ${DebtorEntity.TABLE_NAME} WHERE ${DebtorEntity.ID} = :id")
    abstract fun observeDebtor(id: Long): Observable<DebtorEntity>

    @Query("SELECT * FROM ${DebtorEntity.TABLE_NAME}")
    abstract fun observeDebtors(): Observable<List<DebtorEntity>>

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME}")
    abstract fun observeDebts(): Observable<List<DebtEntity>>

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.ID} = :id")
    abstract fun getDebt(id: Long): Single<DebtEntity>

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.DEBTOR_ID} = :debtorId")
    abstract fun observeDebts(debtorId: Long): Observable<List<DebtEntity>>

    @Query("DELETE FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.ID} = :id")
    abstract fun deleteDebt(id: Long): Completable
}
