package utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;

public class ActivityManager {

	private static ActivityManager activityManager;

	private List<Activity> activities = new ArrayList<Activity>();

	public static ActivityManager getInstance() {
		if (activityManager == null) {
			synchronized (ActivityManager.class) {
				if (activityManager == null) {
					activityManager = new ActivityManager();
				}
			}
		}
		return activityManager;
	}

	public void addActivity(@NonNull Activity activity) {
		activities.add(activity);
	}

	public void removeActivity(@NonNull Activity activity) {
		activities.remove(activity);
	}

	public void finshActivity(@NonNull Activity activity) {
		for (Activity inActivity : activities) {
			if (inActivity.getLocalClassName().equals(activity.getLocalClassName()) && !inActivity.isDestroyed()) {
				inActivity.finish();
			}
		}
	}

	public void finshAllActivity() {
		if (activities.size() == 0) {
			return;
		} else {
			for (Activity activity : activities) {
				activity.finish();
			}
		}
	}

}
