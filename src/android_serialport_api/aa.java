package android_serialport_api;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * 解析json
 * 
 * @author Administrator
 *
 */

public class aa {

	/**
	 * 解析json为List集合
	 * 
	 * @param jsonString
	 * @return
	 */
	public static List<Map<String, Object>> listKeyMaps(String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			JSONObject jsonObject = new JSONObject(jsonString.trim());
			JSONArray jsonArray = jsonObject.getJSONArray("pro");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				Iterator iterator = jsonObject2.keys();
				while (iterator.hasNext()) {
					String json_key = (String) iterator.next();
					Object json_value = jsonObject2.get(json_key);
					if (json_key.equals("Price")) {
						DecimalFormat df = new DecimalFormat("0.00");
						json_value = "￥" + df.format(json_value);
					}
					if (json_value == null) {
						json_value = "";
					}
					map.put(json_key, json_value);
				}
				list.add(map);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return list;
	}

	/**
	 * 解析商品分类
	 * 
	 * @param jsonString
	 * @return
	 */
	public static List<Map<String, Object>> listMaps(String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		try {
			JSONObject jsonObject = new JSONObject(jsonString.trim());
			JSONArray jsonArray = jsonObject.getJSONArray("Order");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject2 = jsonArray.getJSONObject(i);
				Map<String, Object> map = new HashMap<String, Object>();
				Iterator iterator = jsonObject2.keys();
				while (iterator.hasNext()) {
					String json_key = (String) iterator.next();
					Object json_value = jsonObject2.get(json_key);
					if (json_value == null) {
						json_value = "";
					}
					map.put(json_key, json_value);
				}
				list.add(map);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}
		return list;
	}

	/**
	 * 解析json为 JSONObject 获取微信支付路径
	 * 
	 * @param jsonString
	 * @return
	 */
	public static String weixinStr(String rs) {
		JSONObject demoJson;
		String url = "";
		try {
			demoJson = new JSONObject(rs);
			String demoStr = demoJson.getString("url");
			JSONObject paraJsonObject = new JSONObject(demoStr);
			url = paraJsonObject.getString("code_url");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * 解析json为 JSONObject 获取支付宝支付路径
	 * 
	 * @param jsonString
	 * @return
	 */
	public static String AlipayStr(String rs) {
		JSONObject demoJson;
		String url = "";
		try {
			demoJson = new JSONObject(rs);
			url = demoJson.getString("code");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * 解析json为 JSONObject 获取支付宝订单编号
	 * 
	 * @param jsonString
	 * @return
	 */
	public static String AlipayOrder(String rs) {
		JSONObject demoJson;
		String url = "";
		try {
			demoJson = new JSONObject(rs);
			url = demoJson.getString("outTradeNo");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

	/**
	 * 解析json为 JSONObject 获取wx订单编号
	 * 
	 * @param jsonString
	 * @return
	 */
	public static String AlipayWXOrder(String rs) {
		JSONObject demoJson;
		String url = "";
		try {
			demoJson = new JSONObject(rs);
			url = demoJson.getString("Ono");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}

}
