package activity;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.ListDataSave;
import android_serialport_api.sample.R;
import utils.SharePreferenceUtils;

public class SplashActivity extends Activity {

	private ImageView mImageView;
	private TextView tvMachineState;

	private ListDataSave dataSave;
	private MachineUpdateReceiver machineUpdateReceiver;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_frist);
		initData();
		initView();
		hideBottomUIMenu();
	}

	private void initData() {
		machineUpdateReceiver = new MachineUpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("setMachineStatus");
		registerReceiver(machineUpdateReceiver, filter);
	}

	private void initView() {
		mImageView = (ImageView) findViewById(R.id.splash_iv);
		tvMachineState = (TextView) findViewById(R.id.tv_machine_state);
		tvMachineState.setVisibility(View.INVISIBLE);
		mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		Glide.with(SplashActivity.this).load(R.drawable.splash_1).into(mImageView);
		// 清除购物车内容
		dataSave = new ListDataSave(this, "shoppingCart");
		dataSave.ListSave();
		// 图片的点击事件
		mImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (SharePreferenceUtils.getBoolean(getApplicationContext(), "MACHINE_MAINTENANCE", false)) {
					mImageView.setScaleType(ImageView.ScaleType.CENTER);
					tvMachineState.setVisibility(View.VISIBLE);
					tvMachineState.setText("机器正在升级，停止服务");
					Glide.with(SplashActivity.this).load(R.drawable.server_update).into(mImageView);
					return;
				}
				SharedPreferences userInfo = getSharedPreferences("userInfo", MODE_PRIVATE);
				// 获取相应的值,如果没有该值,说明还未写入，程序第一次运行 用false作为默认值
				Boolean shebeihao = userInfo.getBoolean("shebeihao1", false);
				if (shebeihao) {
					Intent intent = new Intent(SplashActivity.this, IuMainActivity.class);
					intent.putExtra("isToVoice", true);
					startActivity(intent);
				} else {
					startActivity(new Intent(SplashActivity.this, shebeihaoActitvty.class));
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SharePreferenceUtils.getBoolean(getApplicationContext(), "MACHINE_MAINTENANCE", false)) {
			mImageView.setScaleType(ImageView.ScaleType.CENTER);
			tvMachineState.setVisibility(View.VISIBLE);
			tvMachineState.setText("机器正在升级，停止服务");
			Glide.with(SplashActivity.this).load(R.drawable.server_update).into(mImageView);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mImageView != null) {
			Glide.clear(mImageView);
		}
		if (machineUpdateReceiver != null) {
			unregisterReceiver(machineUpdateReceiver);
		}
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
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	class MachineUpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			tvMachineState.setVisibility(View.INVISIBLE);
			mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Glide.with(SplashActivity.this).load(R.drawable.splash_1).into(mImageView);
		}
	}

}
