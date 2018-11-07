package view;
import android.app.Application;
import android.util.Log;
import uartJni.Uartjni;
import utils.SerialManager;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// 在这里加载我们的
		SerialManager manager = SerialManager.getInstance();
		manager.initSerial();
		Log.e("MyApplication", "onCreate");
	}
	
	
}
