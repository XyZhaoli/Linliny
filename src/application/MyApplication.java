package application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	private void init() {
		utils.Util.setmContext(getApplicationContext());
		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=5bbaf3a2");
		CrashReport.initCrashReport(getApplicationContext(), "2f1f25f886", true);
	}
}
