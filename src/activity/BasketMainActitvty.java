package activity;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android_serialport_api.sample.R;
import utils.ActivityManager;
import view.BannerLayout;

public class BasketMainActitvty extends BaseAcitivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basket_main);
		initView();
	}

	private void initView() {
		String title = getIntent().getStringExtra("title");
		TextView tvTitle = (TextView) findViewById(R.id.return_goods_title);
		tvTitle.setText(title);
		BannerLayout bannerLayout1 = (BannerLayout) findViewById(R.id.banner_in_getgoods);
		List<Integer> res = new ArrayList<Integer>();
		res.add(R.drawable.iumain04);
		res.add(R.drawable.iumain02);
		res.add(R.drawable.iumain03);
		List<String> titles = new ArrayList<String>();
		titles.add(" ");
		titles.add(" ");
		titles.add(" ");
		if (bannerLayout1 != null) {
			bannerLayout1.setViewRes(res, titles);
		}
		// 返回点击事件
		findViewById(R.id.back).setOnClickListener(this);
		// 注册\n会员
		findViewById(R.id.register).setOnClickListener(this);
		// 手机号\n登陆
		findViewById(R.id.phone_log_in).setOnClickListener(this);
		// 会员卡\n登陆
		findViewById(R.id.vip_log_in).setOnClickListener(this);
		ActivityManager.getInstance().addActivity(BasketMainActitvty.this);
	}

	@Override
	public void changeTvTime(int time) {

	}

	@Override
	public void onClick(View v) {
		if (utils.Util.isFastClick()) {
			switch (v.getId()) {
			case R.id.back:
				BasketMainActitvty.this.finish();
				startActivity(new Intent(BasketMainActitvty.this, IuMainActivity.class));
				break;
			case R.id.register:
				Intent intent = new Intent(BasketMainActitvty.this, BaskethuiyuanActitvty.class);
				startActivity(intent);
				break;
			case R.id.phone_log_in:
				Intent intent1 = new Intent(BasketMainActitvty.this, BasketShoujiActitvty.class);
				startActivity(intent1);
				break;
			case R.id.vip_log_in:
				Intent intent2 = new Intent(BasketMainActitvty.this, BaskethuiyuankaActitvty.class);
				startActivity(intent2);
				break;
			default:
				break;
			}
		} else {
			Log.e("isFastClick", "isFastClick");
		}
	}
}
