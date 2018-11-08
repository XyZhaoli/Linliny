package activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android_serialport_api.ZXingUtils;
import android_serialport_api.sample.R;
import utils.ActivityManager;

@SuppressLint("NewApi")
public class BaskethuiyuanActitvty extends BaseAcitivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);
		setContentView(R.layout.basket_huiyuan);
		final ImageView ima = (ImageView) findViewById(R.id.ima);
		// 二维码
		Bitmap bmap = ZXingUtils.createQRImage("http://linliny.com/WeChatpn/index.html#/mall", 200, 200);
		ima.setImageBitmap(bmap);
		ActivityManager.getInstance().addActivity(BaskethuiyuanActitvty.this);
	}

	@Override
	public void changeTvTime(int time) {
		// TODO Auto-generated method stub
		
	}

}
