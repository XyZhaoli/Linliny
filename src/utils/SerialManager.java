package utils;

import org.greenrobot.eventbus.EventBus;

import domain.MessageEvent;
import uartJni.Uartjni;

public class SerialManager {

	private Uartjni mUartjni;
	private static SerialManager mSerialManager;
	private RegisterCallback mRegisterCallback;
	private UnRegisterCallback mUnRegisterCallback;
	private static int TAG;
	
	private SerialManager() {
		mUartjni = new Uartjni() {
			@Override
			public void onNativeCallback(byte[] arg1) {
				EventBus.getDefault().postSticky(new MessageEvent(arg1, TAG));
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
	
	public void unregister() {
		if(mRegisterCallback != null) {
			mRegisterCallback.onRegisterCallback();
		}
	}

	public void writeCmd(int TAG, byte[] cmd, int len) {
		synchronized (SerialManager.class) {
			this.TAG = TAG;
			if (len > 0 && cmd.length > 0) {
				mUartjni.UartWriteCmd(cmd, len);
			}
		}
	}

	public void closeSerial() {
		synchronized (SerialManager.class) {
			if (mUartjni != null) {
				mUartjni.NativeThreadStop();
			}
			if(mUnRegisterCallback != null) {
				mUnRegisterCallback.onUnRegisterCallback();
			}
		}
	}

	public void setRegisterCallback(RegisterCallback mRegisterCallback) {
		this.mRegisterCallback = mRegisterCallback;
	}
	
	public void setUnRegisterCallback(UnRegisterCallback mUnRegisterCallback) {
		this.mUnRegisterCallback = mUnRegisterCallback;
	}

	public interface RegisterCallback {
		void onRegisterCallback();
	}
	
	public interface UnRegisterCallback {
		void onUnRegisterCallback();
	} 

}
