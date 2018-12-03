package activity;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public abstract class BaseAcitivity extends Activity {

	protected int COUNT_DOWN_TIME = 240;
	private Timer countDownTimer;
	private TimerTask countDownTask;

	public int getTime() {
		return COUNT_DOWN_TIME;
	}

	public void setTime(int cOUNT_DOWN_TIME) {
		COUNT_DOWN_TIME = cOUNT_DOWN_TIME;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initData();
		initCountDown();
		hideBottomUIMenu();
	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
	protected void hideBottomUIMenu() {
		// 隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {
			// for new api versions.
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	private void initCountDown() {
		countDownTimer.schedule(countDownTask, 0, 1000);
	}

	private void initData() {
		COUNT_DOWN_TIME = 240;
		countDownTimer = new Timer();
		countDownTask = new TimerTask() {
			@Override
			public void run() {
				if (COUNT_DOWN_TIME > 0) {
					COUNT_DOWN_TIME--;
					runOnUiThread(new Runnable() {
						public void run() {
							changeTvTime(COUNT_DOWN_TIME);
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							onActivityDectory();
						}
					});
				}
			}
		};
	}

	public abstract void changeTvTime(int time);

	public abstract void onActivityDectory();

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		COUNT_DOWN_TIME = 240;
		return super.onTouchEvent(event);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		COUNT_DOWN_TIME = 240;
	}

	@Override
	protected void onResume() {
		super.onResume();
		COUNT_DOWN_TIME = 240;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (countDownTask != null) {
			countDownTimer.cancel();
			countDownTask.cancel();
		}
	}

}
