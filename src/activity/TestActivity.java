package activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import uartJni.Uartjni;
import utils.ThreadManager;
import utils.Util;

public class TestActivity extends Activity {

	private Uartjni mUartNative;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < 10000; i++) {
					initSerial();
					for (int j = 0; j < 100; j++) {
						sendCmd();
					}
					serialStop();
				}
			}
		});
	}

	private void sendCmd() {
		mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd, ConstantCmd.getMachineStateCmd.length);
		Util.delay(200);
	}

	private void initSerial() {
		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] arg1) {
				Log.e("onNativeCallback", Util.byteToHexstring(arg1, arg1.length));
			}
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	private void serialStop() {
		mUartNative.NativeThreadStop();
	}

}
