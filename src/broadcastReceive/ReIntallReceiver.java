package broadcastReceive;

import activity.SplashActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class ReIntallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String packageName = context.getPackageName();
		Uri data = intent.getData();
		String installedPkgName = data.getSchemeSpecificPart();
		if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
			System.out.println("升级了:" + packageName + "包名的程序");
		}
		if ((action.equals(Intent.ACTION_PACKAGE_ADDED) || action.equals(Intent.ACTION_PACKAGE_REPLACED))
				&& installedPkgName.equals(packageName)) {
			Intent launchIntent = new Intent(context, SplashActivity.class);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(launchIntent);
		}
	}
}
