package activity;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.sample.R;
import utils.ActivityManager;
import view.BannerLayout;

public class BasketMainActitvty extends BaseAcitivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basket_main);
		initView();
	}

	private void initView() {
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
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
		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				BasketMainActitvty.this.finish();
				startActivity(new Intent(BasketMainActitvty.this, IuMainActivity.class));
			}
		});
		// 注册\n会员
		TextView textView1 = (TextView) findViewById(R.id.register);
		textView1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(BasketMainActitvty.this, BaskethuiyuanActitvty.class);
				startActivity(intent);
			}
		});
		// 手机号\n登陆
		TextView textView2 = (TextView) findViewById(R.id.phone_log_in);
		textView2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(BasketMainActitvty.this, BasketShoujiActitvty.class);
				startActivity(intent);
			}
		});
		// 会员卡\n登陆
		final TextView textView3 = (TextView) findViewById(R.id.vip_log_in);
		textView3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(BasketMainActitvty.this, BaskethuiyuankaActitvty.class);
				startActivity(intent);
			}
		});
		ActivityManager.getInstance().addActivity(BasketMainActitvty.this);
	}

	@Override
	public void changeTvTime(int time) {
		// TODO Auto-generated method stub

	}

}
