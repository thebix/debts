package debts.core.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import debts.core.db.DebtEntity
import debts.core.db.DebtorEntity

private const val DEBTORS_TABLE_NAME_LEGACY = "Debitors"
private const val DEBTS_TABLE_NAME_LEGACY = "Debits"
private const val DEBTORS_TABLE_NAME = DebtorEntity.TABLE_NAME
private const val DEBTS_TABLE_NAME = DebtEntity.TABLE_NAME

@Suppress("LongMethod")
fun migration1To2() = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS
                        $DEBTORS_TABLE_NAME_LEGACY (
                            `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `ContactId` INTEGER,
                            `ContactName` TEXT,
                            `ContactEmail` TEXT,
                            `ContactPhone` TEXT
                        )
                """
        )

        database.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS
                        $DEBTS_TABLE_NAME_LEGACY (
                            `_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `EventDate` NUMERIC,
                            `Sum` DOUBLE NOT NULL,
                            `DebitorId` TEXT NOT NULL
                        )
                """
        )

        database.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS
                        `$DEBTORS_TABLE_NAME` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `contactId` INTEGER,
                            `name` TEXT NOT NULL,
                            `avatar` TEXT NOT NULL,
                            `email` TEXT NOT NULL,
                            `phone` TEXT NOT NULL,
                            `legacy_id` INTEGER
                    )
                """
        )
        database.execSQL(
            """
                    CREATE TABLE IF NOT EXISTS
                        `$DEBTS_TABLE_NAME` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `debtorId` INTEGER NOT NULL,
                            `amount` REAL NOT NULL,
                            `currency` TEXT NOT NULL,
                            `date` INTEGER NOT NULL,
                            `comment` TEXT NOT NULL,
                            FOREIGN KEY(`debtorId`) REFERENCES `$DEBTORS_TABLE_NAME`(`id`)
                            ON UPDATE NO ACTION
                            ON DELETE CASCADE
                    )
                """
        )
        database.execSQL("CREATE INDEX IF NOT EXISTS index_debts_table_debtorId ON $DEBTS_TABLE_NAME(debtorId)")

        database.execSQL(
            """
                    INSERT INTO
                        $DEBTORS_TABLE_NAME (
                            `contactId`,
                            `name`,
                            `avatar`,
                            `email`,
                            `phone`,
                            `legacy_id`
                        )
                    SELECT
                        `ContactId`,
                        IFNULL(`ContactName`, ""),
                        "",
                        IFNULL(`ContactEmail`,""),
                        IFNULL(`ContactPhone`,""),
                        `_id`
                    FROM
                        $DEBTORS_TABLE_NAME_LEGACY
                """
        )

        database.execSQL(
            """
                    INSERT INTO
                        $DEBTS_TABLE_NAME(
                            `debtorId`,
                            `amount`,
                            `currency`,
                            `date`,
                            `comment`
                        )
                    SELECT
                        IFNULL(`id`,0),
                        IFNULL(`Sum`, 0),
                        "",
                        IFNULL(`EventDate`,0),
                        ""
                    FROM
                        (SELECT
                            `id`,
                            `Sum`,
                            `EventDate`
                        FROM
                            $DEBTS_TABLE_NAME_LEGACY debits_legacy
                        INNER JOIN
                            $DEBTORS_TABLE_NAME debitors
                        ON
                            debits_legacy.DebitorId == debitors.legacy_id)
                """
        )
    }
}
