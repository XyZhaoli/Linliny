package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 专门访问和设置SharePreference的工具类, 保存和配置一些设置信息
 */
public class SharePreferenceUtils {

	private static final String SHARE_PREFS_NAME = "APPLICATION";
	private static SharedPreferences mSharedPreferences;

	public static void putBoolean(Context ctx, String key, boolean value) {
		if (mSharedPreferences == null) {
			mSharedPreferences = ctx.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}

		mSharedPreferences.edit().putBoolean(key, value).commit();
	}

	public static boolean getBoolean(Context ctx, String key, boolean defaultValue) {
		if (mSharedPreferences == null) {
			mSharedPreferences = ctx.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}

		return mSharedPreferences.getBoolean(key, defaultValue);
	}

	public static void putString(Context ctx, String key, String value) {
		if (mSharedPreferences == null) {
			mSharedPreferences = ctx.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().putString(key, value).commit();
	}

	public static String getString(Context ctx, String key, String defaultValue) {
		if (mSharedPreferences == null) {
			mSharedPreferences = ctx.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}

		return mSharedPreferences.getString(key, defaultValue);
	}

	// 用于List<Map<String,Object>>
	public static void setDataList(String tag, List<Map<String, Object>> datalist, Context ctx) {
		if (mSharedPreferences == null) {
			mSharedPreferences = ctx.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}
		if (null == datalist || datalist.size() <= 0)
			return;
		Gson gson = new Gson();
		// 转换成json数据，再保存
		String strJson = gson.toJson(datalist);
		mSharedPreferences.edit().putString(tag, strJson).commit();
	}

	/**
	 * 获取List
	 * @param tag
	 * @return
	 */
	public static List<Map<String, Object>> getDataList(Context ctx, String tag) {
		if (mSharedPreferences == null) {
			mSharedPreferences = ctx.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}
		List<Map<String, Object>> datalist = new ArrayList<Map<String, Object>>();
		String strJson = mSharedPreferences.getString(tag, null);
		if (null == strJson) {
			return datalist;
		}
		Gson gson = new Gson();
		datalist = gson.fromJson(strJson, new TypeToken<List<Map<String, Object>>>() {
		}.getType());
		return datalist;
	}

	/*
	 * 清除SharedPreferences的数据
	 */
	public static void clear(Context context) {
		if (mSharedPreferences == null) {
			mSharedPreferences = context.getSharedPreferences(SHARE_PREFS_NAME, Context.MODE_PRIVATE);
		}
		mSharedPreferences.edit().clear();
		mSharedPreferences.edit().commit();
		File[] files = new File("/data/data/" + context.getPackageName() + "/shared_prefs").listFiles();
		deleteCache(files);
	}

	public static void deleteCache(File[] files) {
		for (File itemFile : files) {
			if (itemFile.getName().equals(SHARE_PREFS_NAME)) {
				itemFile.delete();
			}
		}
	}

}
