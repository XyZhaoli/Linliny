package android_serialport_api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

@SuppressLint("CommitPrefEdits")
public class ListDataSave {
	private SharedPreferences preferences;
	private SharedPreferences.Editor editor;

	public ListDataSave(Context mContext, String preferenceName) {
		if(preferences == null) {
			synchronized (this) {
				if(preferences == null) {
					preferences = mContext.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
					editor = preferences.edit();
				}
			}
		}
	}

	/**
	 * 清空购物车
	 */
	public void ListSave() {
		editor.clear();
		editor.commit();
	}

	// 用于List<Map<String,Object>>
	public void setDataList(String tag, List<Map<String, Object>> datalist) {
		if (null == datalist || datalist.size() <= 0)
			return;

		Gson gson = new Gson();
		// 转换成json数据，再保存
		String strJson = gson.toJson(datalist);
		editor.putString(tag, strJson);
		editor.commit();
	}

	/**
	 * 获取List
	 * @param tag
	 * @return
	 */
	public List<Map<String, Object>> getDataList(String tag) {
		List<Map<String, Object>> datalist = new ArrayList<Map<String, Object>>();
		String strJson = preferences.getString(tag, null);
		if (null == strJson) {
			return datalist;
		}
		Gson gson = new Gson();
		datalist = gson.fromJson(strJson, new TypeToken<List<Map<String, Object>>>() {
		}.getType());
		return datalist;
	}

}