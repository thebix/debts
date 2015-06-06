package net.thebix.debts;

import java.util.Date;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {
	DBHelper DBHelper;

	public DBAdapter(Context ctx) {
		DBHelper = new DBHelper(ctx);
	}

	// Получить пользователя из базы
	public long getDebitorIdByContactId(long contactId) throws SQLException {
		String[] columns = new String[] { net.thebix.debts.DBHelper.DebitorsEntry._ID,
				net.thebix.debts.DBHelper.DebitorsEntry.COL_NAME, net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID };
		String predicate = net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID + "=" + contactId;
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		Cursor c = db.query(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, columns, predicate,
				null, null, null, null);

		long res = 0;
		if (c.moveToFirst()) // Юзер найден
			res = c.getLong(c.getColumnIndex(net.thebix.debts.DBHelper.DebitorsEntry._ID));
		return res;
	}

	// Получить пользователя из базы
	public long getDebitorIdByNameAndPhoneAndEmail(String name, String phone,
			String email) throws SQLException {
		String[] columns = new String[] { net.thebix.debts.DBHelper.DebitorsEntry._ID,
				net.thebix.debts.DBHelper.DebitorsEntry.COL_NAME, net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID };
		String predicate = net.thebix.debts.DBHelper.DebitorsEntry.COL_NAME + "=\"" + name + "\" AND "
				+ net.thebix.debts.DBHelper.DebitorsEntry.COL_PHONE + "=\"" + phone + "\" AND "
				+ net.thebix.debts.DBHelper.DebitorsEntry.COL_EMAIL + "=\"" + email + "\"";
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		Cursor c = db.query(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, columns, predicate,
				null, null, null, null);

		long res = 0;
		if (c.moveToFirst()) // Юзер найден
			res = c.getLong(c.getColumnIndex(net.thebix.debts.DBHelper.DebitorsEntry._ID));
		return res;
	}

	// Получить пользователя из базы
	public Cursor getDebitorById(long Id) throws SQLException {
		String[] columns = new String[] { net.thebix.debts.DBHelper.DebitorsEntry._ID,
				net.thebix.debts.DBHelper.DebitorsEntry.COL_NAME, net.thebix.debts.DBHelper.DebitorsEntry.COL_PHONE,
				net.thebix.debts.DBHelper.DebitorsEntry.COL_EMAIL, net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID };
		String predicate = net.thebix.debts.DBHelper.DebitorsEntry._ID + "=" + Id;
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		return db.query(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, columns, predicate, null,
				null, null, null);
	}

	// Вставляет пользователя (если существует, возвращает его id)
	public long insertDebitor(long contactId) {
		// Проверим наличие пользователя, если что, вернем
		long debitorId = getDebitorIdByContactId(contactId);

		if (debitorId <= 0) // Пользователя нет, заводим
		{
			ContentValues initialValues = new ContentValues();
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID, contactId);
			SQLiteDatabase db = DBHelper.getWritableDatabase();
			debitorId = db.insert(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, null,
					initialValues);
		}
		return debitorId;
	}

	public long insertDebitor(String debitorName, String debitorPhone,
			String debitorEmail) {
		// Проверим наличие пользователя, если что, вернем
		long debitorId = getDebitorIdByNameAndPhoneAndEmail(debitorName,
				debitorPhone, debitorEmail);

		if (debitorId <= 0) // Пользователя нет, заводим
		{
			ContentValues initialValues = new ContentValues();
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_NAME, debitorName);
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_PHONE, debitorPhone);
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_EMAIL, debitorEmail);
			SQLiteDatabase db = DBHelper.getWritableDatabase();
			debitorId = db.insert(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, null,
					initialValues);
		}
		return debitorId;
	}

	public Boolean updateDebitor(long Id, String debitorName,
			String debitorPhone, String debitorEmail) {
		// Проверим наличие пользователя, если что, вернем
		if (Id > 0) // Пользователя нет, заводим
		{
			String where = net.thebix.debts.DBHelper.DebitorsEntry._ID + "=" + Id;
			ContentValues initialValues = new ContentValues();
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_NAME, debitorName);
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_PHONE, debitorPhone);
			initialValues.put(net.thebix.debts.DBHelper.DebitorsEntry.COL_EMAIL, debitorEmail);
			SQLiteDatabase db = DBHelper.getWritableDatabase();
			return db.update(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, initialValues, where,
					null) > 0;
		}
		return false;
	}

	// Удаляет пользователя
	public boolean removeDebitorById(long debitorId) throws SQLException {
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		Boolean res = db.delete(net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE, net.thebix.debts.DBHelper.DebitorsEntry._ID
				+ "=" + debitorId, null) > 0;
		return res;
	}

	// Вставляет новую запись о долге (если надо, вставляет и пользователя)
	public long insertDebt(long debitorId, Date eventDate, double sum) {
		long res = 0;
		if (debitorId > 0) {
			ContentValues initialValues = new ContentValues();
			initialValues.put(net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE, eventDate.getTime());
			initialValues.put(net.thebix.debts.DBHelper.DebitsEntry.COL_SUM, sum);
			initialValues.put(net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID, debitorId);
			SQLiteDatabase db = DBHelper.getWritableDatabase();
			res = db.insert(net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE, null, initialValues);
		}
		return res;
	}

	// Получает все записи о долгах пользователя
	public Cursor getAllDebitsByDebitorId(long debitorId) throws SQLException {
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		String query = "SELECT " + net.thebix.debts.DBHelper.DebitsEntry._ID + ", "
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE + ", " + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + ", "
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + " as " + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + "_SIGN ,"
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + " as " + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + "_ALIGN "
				
				+ " FROM " + net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE + " WHERE "
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID + " == " + debitorId
				
				+ " ORDER BY " + net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE + " DESC";
				

		Cursor c = db.rawQuery(query, null);

		return c;
	}

	// Удаляет все записи о долгах пользователя
	public boolean removeAllDebitsByDebitorId(long debitorId)
			throws SQLException {
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		Boolean res = db.delete(net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE,
				net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID + "=" + debitorId, null) > 0;
		return res;
	}

	// Удаляет все записи о долгах пользователя
	public boolean removeDebitById(long debitId) throws SQLException {
		SQLiteDatabase db = DBHelper.getWritableDatabase();
		Boolean res = db.delete(net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE, net.thebix.debts.DBHelper.DebitsEntry._ID + "="
				+ debitId, null) > 0;
		return res;
	}

	// Получение всех должников + тех у кого долг 0 и полной суммы их долга
	public Cursor getAllDebitorsWithDebitSums() {
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		String query = "SELECT " + net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID + " as "
				+ net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID + ", " + " us."
				+ net.thebix.debts.DBHelper.DebitorsEntry._ID + ", "

				+ " (SELECT SUM(deb." + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + ") FROM "
				+ net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE + " deb " + "WHERE deb."
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID + "== us." + net.thebix.debts.DBHelper.DebitorsEntry._ID
				+ ") as " + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + ", "

				+ " (SELECT MAX(deb." + net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE + ") FROM "
				+ net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE + " deb " + "WHERE deb."
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID + "== us." + net.thebix.debts.DBHelper.DebitorsEntry._ID
				+ ") as " + net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE

				+ " FROM " + net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE + " us " + " WHERE "
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + " > 0 OR " + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM
				+ " is NULL ";

				//+ " ORDER BY " + DebitsEntry.COL_SUM + " DESC";

		Cursor c = db.rawQuery(query, null);

		return c;
	}

	// Получение всех долгов пользователя приложением
	public Cursor getAllMyDebtsWithDebitSums() {
		SQLiteDatabase db = DBHelper.getReadableDatabase();
		String query = "SELECT " + net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID + " as "
				+ net.thebix.debts.DBHelper.DebitorsEntry.COL_CONTACT_ID + ", "

				+ " us." + net.thebix.debts.DBHelper.DebitorsEntry._ID + ", "

				+ " (SELECT SUM(deb." + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + ") FROM "
				+ net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE + " deb " + "WHERE deb."
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID + "== us." + net.thebix.debts.DBHelper.DebitorsEntry._ID
				+ ") as " + net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + ", "

				+ " (SELECT MAX(deb." + net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE + ") FROM "
				+ net.thebix.debts.DBHelper.DebitsEntry.TABLE_TITLE + " deb " + "WHERE deb."
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_DEBITOR_ID + "== us." + net.thebix.debts.DBHelper.DebitorsEntry._ID
				+ ") as " + net.thebix.debts.DBHelper.DebitsEntry.COL_EVENTDATE

				+ " FROM " + net.thebix.debts.DBHelper.DebitorsEntry.TABLE_TITLE + " us " + " WHERE "
				+ net.thebix.debts.DBHelper.DebitsEntry.COL_SUM + " <= 0 ";

				//+ " ORDER BY " + DebitsEntry.COL_SUM + " ASC";

		Cursor c = db.rawQuery(query, null);

		return c;

	}

	public void close() {
		DBHelper.close();
	}
}
