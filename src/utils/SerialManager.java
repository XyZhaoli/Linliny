package utils;

import uartJni.Uartjni;

public class SerialManager {

	private Uartjni mUartjni;
	private static SerialManager mSerialManager;

	private SerialManager() {
		mUartjni = new Uartjni() {
			@Override
			public void onNativeCallback(byte[] arg1) {
				
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
			if(len > 0 && cmd.length > 0) {
				mUartjni.UartWriteCmd(cmd, len);
			}
		}
	}

	public void closeSerial() {
		synchronized (SerialManager.class) {
			if(mUartjni != null) {
				mUartjni.NativeThreadStop();
			}
		}
	}

}
