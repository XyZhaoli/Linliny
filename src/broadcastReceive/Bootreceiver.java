package broadcastReceive;

import activity.SplashActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import utils.ApManager;

public class Bootreceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			boolean openAp = ApManager.openAp(context, "邻里农园鲜果", "llny.4008270755#");
			if(openAp) {
				Toast.makeText(context, "打开成功", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "打开失败", Toast.LENGTH_LONG).show();
			}
			/* 应用开机自启动 */
			Intent intent_n = new Intent(context, SplashActivity.class);
			intent_n.setAction("android.intent.action.MAIN");
			intent_n.addCategory("android.intent.category.LAUNCHER");
			intent_n.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent_n);
		}
	}
}
