package android_serialport_api;

public class ButtonUtils {
	private static long lastClickTime = 0;

	/**
	 * 判断两次点击的间隔，如果小于2000，则认为是多次无效点击
	 * 
	 * @return
	 */
	public static boolean filter() {
		long time = System.currentTimeMillis();
		if ((time - lastClickTime) > 500) {
			lastClickTime = System.currentTimeMillis();
			return true;
		}
		return false;
	}

}
