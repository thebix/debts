package net.thebix.debts;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DBHelper extends SQLiteOpenHelper {
	static final String DB_NAME = "dc.db";
	static final int DB_VERSION = 2;

	// Таблица пользователей
	public static abstract class DebitorsEntry implements BaseColumns {
		private DebitorsEntry() {
		}

		static final String TABLE_TITLE = "Debitors";

		public static final String COL_CONTACT_ID = "ContactId";
        public static final String COL_NAME = "ContactName";
        public static final String COL_PHONE = "ContactPhone";
        public static final String COL_EMAIL = "ContactEmail";

		static final String CREATE_TABLE = "create table "
				+ DebitorsEntry.TABLE_TITLE + " (" + DebitorsEntry._ID
				+ " integer primary key autoincrement, "
				+ DebitorsEntry.COL_NAME + " text, " + DebitorsEntry.COL_PHONE
				+ " text, " + DebitorsEntry.COL_EMAIL + " text, "
				+ DebitorsEntry.COL_CONTACT_ID + " integer " + ")";

	}

	// Таблица долгов
	public static abstract class DebitsEntry implements BaseColumns {
		private DebitsEntry() {
		}

		static final String TABLE_TITLE = "Debits";

        public static final String COL_EVENTDATE = "EventDate";
        public static final String COL_SUM = "Sum";
		static final String COL_DEBITOR_ID = "DebitorId";

		static final String CREATE_TABLE = "create table "
				+ DebitsEntry.TABLE_TITLE + " (" + DebitsEntry._ID
				+ " integer primary key autoincrement, "
				+ DebitsEntry.COL_EVENTDATE + " numeric, "
				+ DebitsEntry.COL_SUM + " double, "
				+ DebitsEntry.COL_DEBITOR_ID + " integer " + ")";
	}

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DebitorsEntry.CREATE_TABLE);
		db.execSQL(DebitsEntry.CREATE_TABLE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// WTF?
//		db.execSQL("DROP TABLE IF EXISTS " + DebitsEntry.TABLE_TITLE);
//		db.execSQL("DROP TABLE IF EXISTS " + DebitorsEntry.TABLE_TITLE);
//		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
