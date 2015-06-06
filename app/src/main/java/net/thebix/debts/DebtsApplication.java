package net.thebix.debts;

import org.acra.*;
import org.acra.annotation.*;
import android.app.Application;

@ReportsCrashes(formKey = "",
mailTo = "DebitorsControlla@thebix.net",
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.err_crash_text)
public class DebtsApplication extends Application {
	@Override
	  public void onCreate() {
	      super.onCreate();
	      ACRA.init(this);
	  }
}
