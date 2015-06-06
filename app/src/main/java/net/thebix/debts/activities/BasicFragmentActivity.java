package net.thebix.debts.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import net.thebix.debts.enums.Constants;

public class BasicFragmentActivity extends FragmentActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			public void uncaughtException(Thread paramThread,
					Throwable paramThrowable) {
				Log.e(Constants.LOG_TAG,
						"Uncaught Program Exception:"
								+ paramThrowable.getMessage());

			}
		});
	}
}
