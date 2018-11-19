package activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android_serialport_api.ZXingUtils;
import android_serialport_api.sample.R;
import utils.ActivityManager;
import utils.ThreadManager;
import utils.Util;

@SuppressLint("NewApi")
public class BaskethuiyuanActitvty extends BaseAcitivity {

	private ImageView ima;
	private MyHandler handler;
	
	class MyHandler extends Handler{
		public void dispatchMessage(android.os.Message msg) {
			ima.setImageBitmap((Bitmap) msg.obj);
		};
	}
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);
		setContentView(R.layout.basket_huiyuan);
		handler = new MyHandler();
		ima = (ImageView) findViewById(R.id.ima);
		
		// 二维码
		ThreadManager.getThreadPool().execute(new Runnable() {
			
			@Override
			public void run() {
				Bitmap bmap = ZXingUtils.createQRImage("http://linliny.com/WeChatpn/index.html#/mall", 200, 200);
				Util.sendMessage(handler, 1, bmap);
			}
		});
	
		ActivityManager.getInstance().addActivity(BaskethuiyuanActitvty.this);
	}

	@Override
	public void changeTvTime(int time) {
		
	}

}
