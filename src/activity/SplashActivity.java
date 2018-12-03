package activity;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android_serialport_api.ListDataSave;
import android_serialport_api.sample.R;
import domain.MachineState;
import utils.MachineStateManager;
import utils.ThreadManager;
import utils.VoiceUtils;

public class SplashActivity extends Activity {

	private ImageView mImageView;
	private ListDataSave dataSave;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_frist);
		initView();
		hideBottomUIMenu();
	}

	private void initView() {
		mImageView = (ImageView) findViewById(R.id.splash_iv);
		Glide.with(SplashActivity.this).load(R.drawable.splash_1).into(mImageView);
		// 清除购物车内容
		dataSave = new ListDataSave(this, "shoppingCart");
		dataSave.ListSave();
		// 图片的点击事件
		mImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
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
	protected void onDestroy() {
		super.onDestroy();
		if (mImageView != null) {
			Glide.clear(mImageView);
		}
	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
	protected void hideBottomUIMenu() {
		// 隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower
																		// api
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else if (Build.VERSION.SDK_INT >= 19) {
			View decorView = getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
					| View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

}
