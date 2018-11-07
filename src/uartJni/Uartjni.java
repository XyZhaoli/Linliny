package uartJni;

public class Uartjni {

	public void onNativeCallback(byte[] arg1) {
	}

	public native int UartWriteCmd(byte[] cmd, int len);

	public native void nativeInitilize();

	public native int BoardThreadStart();

	public native void NativeThreadStop();

	static {
		System.loadLibrary("linliny_uartmaster");
	}

}
