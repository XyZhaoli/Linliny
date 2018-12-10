package broadcastReceive;

import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import cn.jpush.android.api.JPushInterface;
import domain.ConstantCmd;
import domain.ServerMessage;
import utils.GsonUtils;
import utils.SharePreferenceUtils;
import utils.Util;

public class MessageReceiver extends BroadcastReceiver {

	public static final String MESSAGE_RECEIVED_ACTION = "android_serialport_api.sample.MESSAGE_RECEIVED_ACTION";
	public static final String KEY_TITLE = "title";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_EXTRAS = "extras";
	private static final String TAG = "MessageReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		try {

			Bundle bundle = intent.getExtras();

			if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
				String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
				Log.e(TAG, "[MyReceiver] 接收Registration Id : " + regId);
				// send the Registration Id to your server...

			} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 接收到推送下来的自定义消息(内容为): " + bundle.getString(JPushInterface.EXTRA_MESSAGE));
				//设置进入服务器维护模式
				if(!TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_MESSAGE))) {
					ServerMessage message = GsonUtils.parseJsonWithGson(bundle.getString(JPushInterface.EXTRA_MESSAGE), ServerMessage.class);
					if(message.getFlag() == ConstantCmd.MACHINE_UPDATE) {
						SharePreferenceUtils.putBoolean(context, "MACHINE_MAINTENANCE", true);
						ConstantCmd.currentStatus = ConstantCmd.MACHINE_UPDATE;
					} else if(message.getFlag() == ConstantCmd.MACHINE_NORMAL) {
						SharePreferenceUtils.putBoolean(context, "MACHINE_MAINTENANCE", false);
						Intent statusIntent = new Intent();
						statusIntent.setAction("setMachineStatus");
						context.sendBroadcast(statusIntent);
						ConstantCmd.currentStatus = ConstantCmd.MACHINE_NORMAL;
					}
				}
				
				// 自定义消息不是通知，默认不会被SDK展示到通知栏上，极光推送仅负责透传给SDK。其内容和展示形式完全由开发者自己定义。
				// 自定义消息主要用于应用的内部业务逻辑和特殊展示需求
			} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 接收到推送下来的通知");

				String extra_json = bundle.getString(JPushInterface.EXTRA_EXTRA);
				if (!TextUtils.isEmpty(extra_json))
					Log.d(TAG, "[MyReceiver] 接收到推送下来的通知附加字段" + extra_json);

				// 可以利用附加字段来区别Notication,指定不同的动作,extra_json是个json字符串
				// 通知（Notification），指在手机的通知栏（状态栏）上会显示的一条通知信息
			} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 用户点击打开了通知");

				// 在这里根据 JPushInterface.EXTRA_EXTRA(附加字段) 的内容处理代码，
				// 比如打开新的Activity， 打开一个网页等..

			} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
				Log.e(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
				// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

			} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
				boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
				Log.e(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
			} else {
				Log.e(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
