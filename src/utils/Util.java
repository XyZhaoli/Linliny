package utils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.orhanobut.logger.Logger;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import domain.ConstantCmd;
import domain.SoftwareInfo;

/**
 * Created by ${GongWenbo} on 2018/5/18 0018.
 */
public class Util {

	private static final int MIN_CLICK_DELAY_TIME = 1000;
	private static long lastClickTime;
	private static Context mContext;
	private static String mid;
	private static final String TAG = "Util";

	public static String getMid() {
		if (mContext != null) {
			SharedPreferences preferences = mContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			mid = preferences.getString("Mid", "");
		}
		return mid;
	}

	public static void checkSoftVersion(final boolean isAlone) {
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.GET, ConstantCmd.GET_APP_VERSION, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {

			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				Gson gson = new Gson();
				SoftwareInfo versionInfo = gson.fromJson(arg0.result, SoftwareInfo.class);
				int appVersionCode = Util.getAppVersionCode();
				if (versionInfo == null) {
					return;
				}
				if (isAlone) {
					if (Integer.parseInt(versionInfo.getVersionCode()) >= appVersionCode) {
						downloadApk(versionInfo.getUrl());
					}
				} else {
					if (Integer.parseInt(versionInfo.getVersionCode()) > appVersionCode) {
						downloadApk(versionInfo.getUrl());
					}
				}
			}
		});
	}

	protected static void downloadApk(String url) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
					+ "linlinySellApp.apk";
			HttpUtils httpUtils = new HttpUtils();
			httpUtils.download(url, path, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					File file = responseInfo.result;
					installBySlient(getmContext(), file);
				}

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// 下载失败
					Logger.e("onFailure");
				}

				@Override
				public void onStart() {
					super.onStart();
				}

				@Override
				public void onLoading(long total, long current, boolean isUploading) {
					super.onLoading(total, current, isUploading);
				}
			});
		}
	}

	public static Context getmContext() {
		return mContext;
	}

	public static String paseYname(String yname) {
		int start = yname.indexOf("【") + 1;
		int end = yname.indexOf("】");
		String name = yname.substring(start, end);
		return name;
	}

	public static void setmContext(Context context) {
		mContext = context;
	}

	public static boolean installBySlient(Context context, File file) {
		boolean result = false;
		Process process = null;
		OutputStream out = null;
		if (file.exists()) {
			try {
				process = Runtime.getRuntime().exec("su");
				out = process.getOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(out);
				// 获取文件所有权限
				dataOutputStream.writeBytes("chmod 777 " + file.getPath() + "\n");
				// 进行静默安装命令
				dataOutputStream.writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r " + file.getPath());
				dataOutputStream.flush();
				// 关闭流操作
				dataOutputStream.close();
				out.close();
				int value = process.waitFor();
				// 代表成功
				if (value == 0) {
					Log.e(TAG, "安装成功！");
					result = true;
					// 失败
				} else if (value == 1) {
					Log.e(TAG, "安装失败！");
					result = false;
					// 未知情况
				} else {
					Log.e(TAG, "未知情况！");
					result = false;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!result) {
				result = true;
			}
		}
		return result;
	}

	public static boolean isFastClick() {
		boolean flag = false;
		long curClickTime = System.currentTimeMillis();
		if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
			flag = true;
		}
		lastClickTime = curClickTime;
		return flag;
	}

	public static void delay(int time) {
		if (time > 0) {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getArrayLength(byte[] array) {
		int flag = 0;
		int index = 0;
		for (int i = 0; i < array.length; i++) {
			if (array[i] == 0) {
				flag++;
			} else {
				flag = 0;
				index = i;
			}
		}
		if (flag >= 10) {
			return index;
		}
		return array.length;
	}

	public static String parseBasketCode(byte[] cmd) {
		/**
		 * 20 00 00 08 04 00 00 00 F0 6D A1 7E B1 03；每个字节的含义如下： 20：起始符 00：包头
		 * 00：状态位—表示数据正常 08：表示后面8个字节为有效数据位 04 00：表示卡片属性为S50卡 00 00：此2个字节无意义 F0 6D A1 7E
		 * B1：表示卡片序列号 B1：校验和 03：帧结束符
		 */
		// 数据长度
		// int dataLength = cmd[3];
		byte[] data = new byte[5];
		System.arraycopy(cmd, 8, data, 0, 5);
		Long valueOf = Long.valueOf(utils.Util.byteToHexstringNo(data, data.length), 16);
		return valueOf.toString();
	}

	public static String[] parseImageUrl(String url) throws Exception {
		String[] urls = null;
		if (url.contains("+-+")) {
			urls = url.split("\\+-\\+");
		} else {
			throw new Exception("这是单个URL，不需要解析");
		}
		return urls;
	}

	// 下划线
	public static void drawUnderline(TextView textView) {
		textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
	}

	// 中划线
	public static void drawStrikethrough(TextView textView) {
		textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
	}

	// dp2px
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static void sendMessage(Handler handler, int what, Object obj) {
		if (handler != null && obj != null) {
			Message message = Message.obtain();
			message.what = what;
			message.obj = obj;
			handler.sendMessage(message);
		}
	}

	public static void sendMessage(Handler handler, int what) {
		handler.sendEmptyMessage(what);
	}

	@SuppressLint("DefaultLocale")
	public static String byteToHexstring(byte[] buff, int length) {
		StringBuffer HexString = new StringBuffer();
		for (int i = 0; i < length; i++) {
			StringBuffer hex = new StringBuffer(Integer.toHexString(buff[i] & 0xff));
			if (hex.length() == 1) {
				hex.insert(0, "0");
			}
			HexString.append(hex.toString().toUpperCase() + " ");
		}
		return HexString.toString();
	}

	public static String byteToHexstringNo(byte[] buff, int length) {
		String HexString = "";
		for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(buff[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			HexString += hex.toUpperCase() + "";
		}
		return HexString;
	}

	/**
	 * 函数说明：自定义的Toast显示
	 * 
	 * @param str
	 *            所要显示的字符串
	 * @param resID
	 *            要显示的提示图片 暂时屏蔽掉这个显示自定义Toast的方法，目前这个自定义toast的显示不是很稳定
	 */
	@SuppressLint("NewApi")
	public static void DisplayToast(Context context, String str, int resID) {
		Activity activity = (Activity) context;
		if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
			return;
		}
		// UniversalToast.makeText(context, str, UniversalToast.LENGTH_SHORT,
		// UniversalToast.EMPHASIZE).setIcon(resID)
		// .setGravity(Gravity.CENTER_VERTICAL, 0, 500).show();
		Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 函数说明：自定义的Toast显示
	 * 
	 * @param str
	 *            所要显示的字符串
	 * @param resID
	 *            要显示的提示图片 暂时屏蔽掉这个显示自定义Toast的方法，目前这个自定义toast的显示不是很稳定
	 */
	@SuppressLint("NewApi")
	public static void DisplayToast(Context context, String str) {
		Activity activity = (Activity) context;
		if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
			return;
		}
		Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 解析json的返回值
	 * 
	 * @param string
	 * @param key
	 * @return
	 */
	public static int parseJson(String string, String key) {
		int value = -1;
		try {
			JSONObject object = new JSONObject(string);
			value = object.getInt(key);
		} catch (Exception e) {
			value = -1;
		}
		return value;
	}

	/**
	 * 解析json的返回值
	 * 
	 * @param string
	 * @param key
	 * @return
	 */
	public static String parseJsonStr(String string, String key) {
		String value = "";
		try {
			JSONObject object = new JSONObject(string);
			value = object.getString(key);
		} catch (Exception e) {
			value = "null";
		}
		return value;
	}

	public static Map<String, Object> getMap(String jsonString) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
			@SuppressWarnings("unchecked")
			Iterator<String> keyIter = jsonObject.keys();
			String key;
			Object value;
			Map<String, Object> valueMap = new HashMap<String, Object>();
			while (keyIter.hasNext()) {
				key = (String) keyIter.next();
				value = jsonObject.get(key);
				valueMap.put(key, value);
			}
			return valueMap;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressLint("NewApi")
	public static void disMissDialog(Dialog dialog, Activity activity) {
		if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
			return;
		}
		if (dialog != null && !activity.isDestroyed()) {
			dialog.dismiss();
		}
	}

	@SuppressLint("NewApi")
	public static void showCustomDialog(Dialog dialog, Activity activity) {
		if (activity == null || activity.isDestroyed() || activity.isFinishing()) {
			return;
		}
		if (!activity.isDestroyed() && dialog != null) {
			dialog.show();
		}
	}

	public static int getAppVersionCode() {
		if (mContext != null) {
			try {
				return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	public static String getVersionName() {
		if (mContext != null) {
			try {
				return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
