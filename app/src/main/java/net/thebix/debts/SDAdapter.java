package net.thebix.debts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Date;
import android.content.Context;
import android.os.Environment;

public class SDAdapter {
	private static final String FILE_DEBITS = "debits.csv";

	private static final File getOrCreateApplicationFile(Context context, String filename)
	{
		File file = null;
		try
		{
			File sdCard = Environment.getExternalStorageDirectory();
			File directory = new File (sdCard.getAbsolutePath() + "/" + context.getString(R.string.app_name) + "/");
			directory.mkdirs();
			file = new File(directory, filename);
			file.createNewFile();
			return file;
		}
		catch (IOException ioe)
		{
			Misc.WriteLog(ioe);
		}
		
		return file;
	}

	public static void writeDebit(Context context, long contactId, Date currentDate, double sum)
	{
		try
		{
			File file = getOrCreateApplicationFile(context, FILE_DEBITS);
			Throws.ifNull(file, context.getString(R.string.err_cant_save_to_file));
			FileOutputStream fOut = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			
			String debitorName = Misc.getContactDisplayName(context, contactId);
			
			String res = String.format(Locale.getDefault(), "%s;\t%s;\t%.2f\n", 
										debitorName, Misc.getDateFormattedString(currentDate, context) + " "
																+ Misc.getTimeFormattedString(currentDate, context),
																sum);
			osw.write(res);
			osw.flush();
			osw.close();		
		}
		catch (Exception ex)
		{
			Misc.WriteLog(ex);
		}
	}
}
