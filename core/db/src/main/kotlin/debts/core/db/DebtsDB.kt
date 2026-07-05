package debts.core.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

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
    abstract suspend fun insertDebtor(debtorEntity: DebtorEntity): Long

    @Insert
    abstract suspend fun insertDebt(debtEntity: DebtEntity): Long

    @Update
    abstract suspend fun updateDebtor(debtorEntity: DebtorEntity)

    @Query("UPDATE ${DebtorEntity.TABLE_NAME} SET ${DebtorEntity.NAME} = :name, ${DebtorEntity.AVATAR} = :avatar WHERE ${DebtorEntity.ID} = :id")
    abstract suspend fun updateDebtor(id: Long, name: String, avatar: String)

    @Transaction
    open suspend fun updateDebtors(debtors: List<DebtorEntity>) {
        for (item in debtors) {
            updateDebtor(item.id, item.name, item.avatarUrl)
        }
    }

    @Update
    abstract suspend fun updateDebt(debtEntity: DebtEntity)

    @Query("UPDATE ${DebtEntity.TABLE_NAME} SET ${DebtEntity.CURRENCY} = :currency")
    abstract suspend fun updateDebtsCurrency(currency: String)

    @Delete
    abstract suspend fun deleteDebtor(debtorEntity: DebtorEntity)

    @Delete
    abstract suspend fun deleteDebt(debtEntity: DebtEntity)

    @Query("DELETE FROM ${DebtorEntity.TABLE_NAME} WHERE ${DebtorEntity.ID} = :id")
    abstract suspend fun deleteDebtor(id: Long)

    @Query("DELETE FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.DEBTOR_ID} = :debtorId")
    abstract suspend fun clearAllDebts(debtorId: Long)

    @Query("SELECT * FROM ${DebtorEntity.TABLE_NAME} WHERE ${DebtorEntity.ID} = :id")
    abstract fun observeDebtor(id: Long): Flow<DebtorEntity?>

    @Query("SELECT * FROM ${DebtorEntity.TABLE_NAME}")
    abstract fun observeDebtors(): Flow<List<DebtorEntity>>

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME}")
    abstract fun observeDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.ID} = :id")
    abstract suspend fun getDebt(id: Long): DebtEntity

    @Query("SELECT * FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.DEBTOR_ID} = :debtorId")
    abstract fun observeDebts(debtorId: Long): Flow<List<DebtEntity>>

    @Query("DELETE FROM ${DebtEntity.TABLE_NAME} WHERE ${DebtEntity.ID} = :id")
    abstract suspend fun deleteDebt(id: Long)
}
