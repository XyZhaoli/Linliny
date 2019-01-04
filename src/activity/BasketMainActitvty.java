package activity;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android_serialport_api.sample.R;
import dialog.ByCardReturnBasketDialog;
import dialog.ByPhoneNumReturnBasketDialog;
import dialog.LinliUrlQrDialog;
import utils.Util;
import view.BannerLayout;

public class BasketMainActitvty extends BaseActivity implements OnClickListener {

	private LinliUrlQrDialog linliUrlQrDialog;
	private ByPhoneNumReturnBasketDialog phoneNumReturnBasketDialog;
	private ByCardReturnBasketDialog cardReturnBasketDialog;
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basket_main);
		initView();
	}

	private void initView() {
		handler = new Handler();
		String title = getIntent().getStringExtra("title");
		TextView tvTitle = (TextView) findViewById(R.id.return_goods_title);
		tvTitle.setText(title);
		final BannerLayout bannerLayout1 = (BannerLayout) findViewById(R.id.banner_in_getgoods);
		final List<Integer> res = new ArrayList<Integer>();
		res.add(R.drawable.viewpage_4);
		res.add(R.drawable.viewpage_2);
		res.add(R.drawable.viewpage_3);
		final List<String> titles = new ArrayList<String>();
		titles.add(" ");
		titles.add(" ");
		titles.add(" ");

		if (bannerLayout1 != null) {
			handler.post(new Runnable() {
				@SuppressLint("NewApi")
				@Override
				public void run() {
					if (!BasketMainActitvty.this.isDestroyed()) {
						bannerLayout1.setViewRes(res, titles);
					}
				}
			});
		}

		// 返回点击事件
		findViewById(R.id.back).setOnClickListener(this);
		// 注册会员
		findViewById(R.id.register).setOnClickListener(this);
		// 手机号登陆
		findViewById(R.id.phone_log_in).setOnClickListener(this);
		// 会员卡登陆
		findViewById(R.id.vip_log_in).setOnClickListener(this);
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
				linliUrlQrDialog = new LinliUrlQrDialog(BasketMainActitvty.this, R.style.MyDialogStyle);
				Util.showCustomDialog(linliUrlQrDialog, BasketMainActitvty.this);
				break;
			case R.id.phone_log_in:
				phoneNumReturnBasketDialog = new ByPhoneNumReturnBasketDialog(BasketMainActitvty.this,
						R.style.MyDialogStyle);
				Util.showCustomDialog(phoneNumReturnBasketDialog, BasketMainActitvty.this);
				break;
			case R.id.vip_log_in:
				cardReturnBasketDialog = new ByCardReturnBasketDialog(BasketMainActitvty.this, R.style.MyDialogStyle);
				Util.showCustomDialog(cardReturnBasketDialog, BasketMainActitvty.this);
				break;
			default:
				break;
			}
		} else {
			Log.e("isFastClick", "isFastClick");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(linliUrlQrDialog != null) {
			linliUrlQrDialog.dismiss();
			linliUrlQrDialog = null;
		}
		if(phoneNumReturnBasketDialog != null) {
			phoneNumReturnBasketDialog.dismiss();
			phoneNumReturnBasketDialog = null;
		}
		if(cardReturnBasketDialog != null) {
			cardReturnBasketDialog.dismiss();
			cardReturnBasketDialog = null;
		}
//		Util.disMissDialog(linliUrlQrDialog, BasketMainActitvty.this);
//		Util.disMissDialog(phoneNumReturnBasketDialog, BasketMainActitvty.this);
//		Util.disMissDialog(cardReturnBasketDialog, BasketMainActitvty.this);
	}

	@Override
	public void onActivityDectory() {
		BasketMainActitvty.this.finish();
	}

}
