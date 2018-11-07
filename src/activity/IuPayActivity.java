package activity;

import java.math.MathContext;
import java.util.TimerTask;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android_serialport_api.ZXingUtils;
import android_serialport_api.aa;
import android_serialport_api.bb;
import android_serialport_api.sample.R;
import utils.VoiceUtils;

public class IuPayActivity extends Activity {

	Handler handler = new Handler();
	private String Ono = "";
	private String outTradeNo = "";
	private String zfJS;
	private int number = 30; // 秒数
	private Context mContext = IuPayActivity.this;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);
		setContentView(R.layout.ui_pay);

		// 拿到返回过来的二维码url
		Intent intent = getIntent();
		String url = intent.getStringExtra("rs").toString().trim();
		String ZFurl = intent.getStringExtra("ZFurl").toString().trim();
		outTradeNo = intent.getStringExtra("outTradeNo").toString().trim();
		Ono = intent.getStringExtra("Ono").toString().trim();
		// 广播通知
		intent.setAction("action.refreshFriend");
		sendBroadcast(intent);

		// 二维码根据URL生成图片
		Bitmap bitmap = ZXingUtils.createQRImage(url, 150, 150);
		ImageView img = (ImageView) findViewById(R.id.weixin1);
		img.setImageBitmap(bitmap);

		// 二维码根据URL生成图片
		Bitmap bmap = ZXingUtils.createQRImage(ZFurl, 150, 150);
		ImageView zhifubao1 = (ImageView) findViewById(R.id.zhifubao1);
		zhifubao1.setImageBitmap(bmap);

		// handler.postDelayed(runnable, 5000); // 在初始化方法里.
		handler.post(runnable);

	}

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			// 获取微信json 状态 1已下单 2已付款 3已发货 4已收货 5已完成
			try {
				String wx = "http://linliny.com/dingyifeng_web/QueryOrderState1.json?Ono=" + Ono;
				zfJS = bb.getHttpResult(wx);

				// 获取支付宝json FAILED SUCCESS
				String zf = "http://linliny.com/dingyifeng_web/QueryOrderAli.json?outTradeNo=" + outTradeNo;
				new bb();
				zfJS = bb.getHttpResult(zf);

				number--;
				if (number <= 0) {
					// Toast.makeText(IuPayActivity.this, "您已超时！！！", Toast.LENGTH_LONG).show();
					// 销毁计时器
					handler.removeCallbacks(runnable);
					// 页面退出
					IuPayActivity.this.finish();
				}

				handler.postDelayed(this, 3000);

				// Toast.makeText(IuPayActivity.this, zfJS, Toast.LENGTH_SHORT).show();
				if (zfJS.equals("SUCCESS") || zfJS.equals("success") || zfJS.equals("5") || zfJS == "5") {
					AlertDialog.Builder builder = new AlertDialog.Builder(IuPayActivity.this);
					builder.setTitle("提示").setMessage("您已成功付款！等待出货中。。。").create().show();
					// 销毁计时器
					handler.removeCallbacks(runnable);
				}
			} catch (ConnectTimeoutException e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
						VoiceUtils.getInstance().initmTts(mContext, "网络错误，请重试");
					}
				});
				e.printStackTrace();
			}
		}
	};

	/**
	 * 关闭页面时销毁定时器
	 */
	@Override
	protected void onDestroy() {
		handler.removeCallbacks(runnable); // 停止刷新
		super.onDestroy();
	}
}
