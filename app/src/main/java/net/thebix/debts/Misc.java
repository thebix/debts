package net.thebix.debts;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Date;
import net.thebix.debts.DBHelper.DebitsEntry;
import net.thebix.debts.enums.Constants;
import net.thebix.debts.models.DebitorsList;
import net.thebix.debts.models.DebitorsListItem;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.text.format.DateFormat;
import android.util.Log;

public final class Misc {
	private static final int MIN_VALUE_ROUND_1_POINT = 10000; //Минимальное значение числа, с которого округление идет до 1 знака после запятой
	private static final int MIN_VALUE_ROUND_0_POINT = 100000; //Минимальное значение числа, с которого округление идет до целого числа (0 знаков)

	public static final Bitmap getContactPhoto(Context context, long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
				contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri,
				Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { Contacts.Photo.PHOTO }, null, null, null);

		if (cursor == null) {
			return null;
		}
		try {
			if (cursor.moveToFirst()) {
				byte[] data = cursor.getBlob(0);
				if (data != null) {
					return BitmapFactory.decodeStream(new ByteArrayInputStream(
							data));
				}
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	public static final String getContactDisplayName(Context context,
			long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI,
				contactId);
		String[] projection = new String[] { Contacts.DISPLAY_NAME };

		Cursor c = context.getContentResolver().query(contactUri, projection,
				null, null, null);

		try {
			if (c.moveToFirst()) {
				return c.getString(c
						.getColumnIndex(Contacts.DISPLAY_NAME));
			} else
				return context.getString(R.string.text_contact_not_found);
		} finally {
			c.close();
		}
	}

	public static final double getDebitsSum(Cursor contactDebits) {
		final int columnIndexSum = contactDebits
				.getColumnIndexOrThrow(DebitsEntry.COL_SUM);
		double debitsSum = 0;

		if (contactDebits.moveToFirst()) {
			do {
				debitsSum += contactDebits.getDouble(columnIndexSum);
			} while (contactDebits.moveToNext());

		}

		return new BigDecimal(debitsSum).setScale(2, BigDecimal.ROUND_HALF_UP)
				.doubleValue();
	}

    public static final double getDebitsSum(DebitorsList list) {
        double debitsSum = 0;
        for(DebitorsListItem item : list.getItems()){
            debitsSum += item.getSum();
        }

        return new BigDecimal(debitsSum).setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

	///isRound -- округление с учетом MIN_VALUE_ROUND_1_POINT и MIN_VALUE_ROUND_0_POINT
	public static final String getSumText(double sum, Boolean isRound) {
		if ((long) sum == sum) { //Если число без запятой
			long res = (long) sum;
			return Long.toString(res);
		} else {
			if(isRound){
				if(sum > MIN_VALUE_ROUND_1_POINT)
				{
					if(sum > MIN_VALUE_ROUND_0_POINT){
						return (new BigDecimal(sum).setScale(0, BigDecimal.ROUND_HALF_UP))
								.toString();
					}
					return (new BigDecimal(sum).setScale(1, BigDecimal.ROUND_HALF_UP))
							.toString();	
				}
			}
			return (new BigDecimal(sum).setScale(2, BigDecimal.ROUND_HALF_UP))
					.toString();
		}
	}

	public static final String getDateFormattedString(Date date, Context context) {
		return DateFormat.getDateFormat(context).format(date);
	}

	public static final String getTimeFormattedString(Date date, Context context) {
		return DateFormat.getTimeFormat(context).format(date);
	}

    public static final String getDateTimeFormattedString(Date date, Context context) {
        return getDateFormattedString(date, context) + " " + getTimeFormattedString(date, context);
    }

	// Запись в лог ошибки
	public static final void WriteLog(Exception ex) {
		Log.e(Constants.LOG_TAG, ex.getMessage());
	}

	public static final void WriteLog(String infoMessage) {
		Log.i(Constants.LOG_TAG, infoMessage);
	}
}
