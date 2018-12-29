package broadcastReceive;

import activity.UartActivity;
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

			if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
				// 设置进入服务器维护模式
				Log.e(TAG, bundle.getString(JPushInterface.EXTRA_MESSAGE));
				if (!TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_MESSAGE))) {
					ServerMessage message = GsonUtils.parseJsonWithGson(bundle.getString(JPushInterface.EXTRA_MESSAGE),
							ServerMessage.class);
					// 所有机器进行升级
					if (message.getFlag() == ConstantCmd.MACHINE_UPDATE && message.getMid() == 0) {
						SharePreferenceUtils.putBoolean(context, "MACHINE_MAINTENANCE", true);
						ConstantCmd.currentStatus = ConstantCmd.MACHINE_UPDATE;
					} else if (message.getFlag() == ConstantCmd.MACHINE_NORMAL) {
						SharePreferenceUtils.putBoolean(context, "MACHINE_MAINTENANCE", false);
						Intent statusIntent = new Intent();
						statusIntent.setAction("setMachineStatus");
						context.sendBroadcast(statusIntent);
						ConstantCmd.currentStatus = ConstantCmd.MACHINE_NORMAL;
					} else if (message.getFlag() == ConstantCmd.MAHINE_ALONE_UPDATE
							&& message.getMid() == Integer.parseInt(Util.getMid())) {
						// 指定这台机器进行升级
						Util.checkSoftVersion(true);
					} else if (message.getFlag() == ConstantCmd.OPEN_REPLENISHMENT_APP
							&& message.getMid() == Integer.parseInt(Util.getMid())) {
						Intent intents = new Intent(context, UartActivity.class);
						intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intents);
					}
				}
				// 自定义消息不是通知，默认不会被SDK展示到通知栏上，极光推送仅负责透传给SDK。其内容和展示形式完全由开发者自己定义。
				// 自定义消息主要用于应用的内部业务逻辑和特殊展示需求
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
