package uartJni;

public class UartJniCard {

	public void onCardNativeCallback(byte[] arg1) {
	}

	public native int UartCardWriteCmd(byte[] cmd, int len);

	public native void nativeCardInitilize();

	public native int BoardCardThreadStart();

	public native void NativeCardThreadStop();

	static {
		System.loadLibrary("linliny_uartcardmaster");
	}
}
