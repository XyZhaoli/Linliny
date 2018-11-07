package utils;

import uartJni.Uartjni;

public class SerialManager {

	private Uartjni mUartjni;
	private static SerialManager mSerialManager;
	private onCallback callback;

	private SerialManager() {
		mUartjni = new Uartjni() {
			@Override
			public void onNativeCallback(byte[] arg1) {
				callback.onResponse(arg1);
			}
		};
	}

	public static SerialManager getInstance() {
		if (mSerialManager == null) {
			synchronized (SerialManager.class) {
				if (mSerialManager == null) {
					mSerialManager = new SerialManager();
				}
			}
		}
		return mSerialManager;
	}

	public void initSerial() {
		if (mUartjni != null) {
			mUartjni.BoardThreadStart();
			mUartjni.nativeInitilize();
		}
	}

	public void writeCmd(byte[] cmd, int len) {
		synchronized (SerialManager.class) {
			mUartjni.UartWriteCmd(cmd, len);
		}
	}

	public void setOnCallback(onCallback callback) {
		this.callback = callback;
	}

	public interface onCallback {
		public void onResponse(byte[] response);
	}

	public void closeSerial() {
		mUartjni.NativeThreadStop();
	}

}
