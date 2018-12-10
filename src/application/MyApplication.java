package application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;
import cn.jpush.android.api.JPushInterface;
import utils.Util;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}

	private void init() {
		Util.setmContext(getApplicationContext());
		SpeechUtility.createUtility(getApplicationContext(), SpeechConstant.APPID + "=5bbaf3a2");
		CrashReport.initCrashReport(getApplicationContext(), "2f1f25f886", false);
		Logger.addLogAdapter(new AndroidLogAdapter());
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(getApplicationContext()); // 初始化 JPush
	}
}
