package application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import utils.CrashHandler;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	private void init() {
		utils.Util.setmContext(getApplicationContext());
		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=5bbaf3a2");
//		CrashHandler crashHandler = CrashHandler.getInstance();
//		crashHandler.init(getApplicationContext());
	}
}
