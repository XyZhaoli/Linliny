package activity;

import java.util.List;

import com.bumptech.glide.Glide;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.aa;
import android_serialport_api.sample.R;
import domain.AlreadyToBuyGoods;
import domain.Goods;
import utils.ActivityManager;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.VoiceUtils;
import domain.PayforResponse;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@SuppressLint("InlinedApi")
public class DialogActivity extends Activity implements OnClickListener {

	private static final int GET_RESPONSE = 1;
	private static final int INIT_VIEW = 2;
	private Context mContext;
	private TextView shoppingNumber;
	private String itemYid;
	// 获取图片
	private MyHandler handler;
	private double Price = 0;
	private double tol = 0;
	private String num;
	private AlreadyToBuyGoods buyGoods;
	private List<Goods> fromNetWorkGoods;
	protected String goodsInfo;
	private AlertDialog dialog;
	private ImageButton minus;
	private TextView tvGoodsFormat;
	private TextView tvGoodsBarcode;
	private TextView tvGoodsInfo;
	private TextView tvGoodsInventory;
	private TextView tvGoodsPrice;
	private TextView tvGoodsName;
	private ImageView imView;
	private ImageView ivCancel;
	private ImageButton plus;
	private Button btBuyGoods;
	private Button btAddToShoppingCar;
	private String Mid = "";
	private String yname;
	private int zongshu;
	private Goods currentGoods;

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_RESPONSE:
				parseMessage(msg);
				break;
			case INIT_VIEW:
				parseGoodsInfo();
				break;
			default:
				break;
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);
		hideBottomUIMenu();
		setContentView(R.layout.ui_details);
		initData();
		initView();
	}

	private void initData() {
		itemYid = getIntent().getStringExtra("Yid");
		handler = new MyHandler();
		mContext = DialogActivity.this;
		Mid = utils.Util.getMid();
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				ActivityManager.getInstance().addActivity(DialogActivity.this);
				fromNetWorkGoods = ShoppingCarManager.getInstence().getFromNetWorkGoods();
				for (Goods goods : fromNetWorkGoods) {
					if (goods.getYid().equals(itemYid)) {
						currentGoods = goods;
						handler.sendEmptyMessage(INIT_VIEW);
						break;
					}
				}
			}
		});
	}

	public void parseGoodsInfo() {
		yname = currentGoods.getYname().replaceAll(" ", "");
		zongshu = Integer.valueOf(currentGoods.getZongshu());
		// 单价
		Price = Double.parseDouble(currentGoods.getPrice());
		buyGoods = new AlreadyToBuyGoods(currentGoods, 1);
		tvGoodsFormat.setText("商品规格: " + currentGoods.getCompany());// 商品规格
		tvGoodsBarcode.setText("商品条码: " + currentGoods.getBarCode());// 条码
		tvGoodsInfo.setText(currentGoods.getTitle());// 商品介绍
		tvGoodsInventory.setText("商品库存: " + zongshu);// 库存
		tvGoodsName.setText(currentGoods.getYname());
		setPrice(tvGoodsPrice, "￥" + currentGoods.getPrice());
	}

	private void initView() {
		tvGoodsFormat = (TextView) findViewById(R.id.tv_goods_format_dialog_activity);
		tvGoodsBarcode = (TextView) findViewById(R.id.tv_goods_barcode_dialog_activity);
		tvGoodsInfo = (TextView) findViewById(R.id.tv_goods_info_dialog_activity);
		tvGoodsInventory = (TextView) findViewById(R.id.tv_goods_inventory_dialog_activity);
		tvGoodsPrice = (TextView) findViewById(R.id.tv_goods_price_dialog_activity);
		tvGoodsName = (TextView) findViewById(R.id.tv_goods_name_dialog_activity);
		imView = (ImageView) findViewById(R.id.iv_topic);
		shoppingNumber = (TextView) findViewById(R.id.shoppingNumber);

		ivCancel = (ImageView) findViewById(R.id.iv_cancel_dialogactivity);
		minus = (ImageButton) findViewById(R.id.minus_in_goods_detils);
		plus = (ImageButton) findViewById(R.id.plus_in_goods_detils);
		btBuyGoods = (Button) findViewById(R.id.btn_cancel_goods_detils_activity);
		btAddToShoppingCar = (Button) findViewById(R.id.button1);

		minus.setOnClickListener(this);
		plus.setOnClickListener(this);
		btBuyGoods.setOnClickListener(this);
		btAddToShoppingCar.setOnClickListener(this);
		ivCancel.setOnClickListener(this);

		Glide.with(mContext).load((String) getIntent().getStringExtra("Picture")).into(imView);
	}

	@Override
	public void onClick(View v) {
		int Number = Integer.parseInt(shoppingNumber.getText().toString());
		switch (v.getId()) {
		case R.id.iv_cancel_dialogactivity:
			DialogActivity.this.finish();
			break;
		case R.id.minus_in_goods_detils:
			if (Number > 1) {
				Number--;
				shoppingNumber.setText(String.valueOf(Number));
				try {
					buyGoods.setAlreadyToBuyGoodsnum(Number);
				} catch (Exception e) {
					try {
						buyGoods.setAlreadyToBuyGoodsnum(1);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				}
			}
			break;
		case R.id.plus_in_goods_detils:
			if (Number < zongshu) {
				Number++;
				shoppingNumber.setText(String.valueOf(Number));
				try {
					buyGoods.setAlreadyToBuyGoodsnum(Number);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(DialogActivity.this, "亲，已达到商品可购买最大数量了哦", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btn_cancel_goods_detils_activity:
			if (utils.Util.isFastClick()) {
				showProgressDialog();
				createQRCode();
			}
			break;
		case R.id.button1:
			try {
				ShoppingCarManager.getInstence().addGoodsToCar(buyGoods);
				// TODO 测试
				Toast.makeText(DialogActivity.this, "已加入购物车", Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(DialogActivity.this, "亲，购物车中已存在该商品哦", Toast.LENGTH_SHORT).show();
			}
			sendBroadcast();
			break;

		default:
			break;
		}
	}

	private void createQRCode() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					HttpUtils httpUtils = new HttpUtils();
					httpUtils.configCurrentHttpCacheExpiry(0 * 1000);
					// 发送商品信息给后台入库Total=0.55&Goods=222222254_0.11_2"
					num = (String) shoppingNumber.getText();
					// 总价
					tol = Integer.parseInt(num) * Price;
					goodsInfo = itemYid + "_" + Price + "_" + num;
					// 获取微信json
					String wx = "http://linliny.com/dingyifeng_web/AddOrd.json?Mid=" + Mid + "&Total=" + tol + "&Goods="
							+ itemYid + "_" + Price + "_" + num;

					String wechatRes = httpUtils.sendSync(HttpMethod.GET, wx).readString();

					// 获取支付宝json
					String zf = "http://linliny.com/dingyifeng_web/AddOrdALiPay.json?Mid=" + Mid + "&Total=" + tol
							+ "&Num=" + num + "&Goods=" + itemYid + "_" + yname + "_" + Price + "_" + num;
					String aliPayRes = httpUtils.sendSync(HttpMethod.GET, zf).readString();
					utils.Util.sendMessage(handler, GET_RESPONSE, new PayforResponse(wechatRes, aliPayRes));
				} catch (Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
							VoiceUtils.getInstance().initmTts(getApplicationContext(), "网络错误，请重试");
						}
					});
				}
			}
		});
	}

	private void setPrice(TextView tvPrice, String stringExtra) {
		int start = stringExtra.indexOf(".");
		int end = stringExtra.length();
		SpannableString textSpan = new SpannableString(stringExtra);
		textSpan.setSpan(new AbsoluteSizeSpan(24), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textSpan.setSpan(new AbsoluteSizeSpan(50), 1, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		textSpan.setSpan(new AbsoluteSizeSpan(24), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		tvPrice.setText(textSpan);
	}

	private void sendBroadcast() {
		Intent intent = new Intent();
		intent.setAction("getGoodsNumAction");
		this.sendBroadcast(intent);
	}

	public void parseMessage(Message msg) {
		dialog.dismiss();
		PayforResponse response = (PayforResponse) msg.obj;
		if (!TextUtils.isEmpty(response.getAliPayRes()) && !TextUtils.isEmpty(response.getWechatRes())) {
			// 解析json，获取微信支付的路径
			Intent intents = new Intent(DialogActivity.this, IuPayListActivity.class);
			intents.putExtra("rs", aa.weixinStr(response.getWechatRes()));
			intents.putExtra("ZFurl", aa.AlipayStr(response.getAliPayRes()));
			intents.putExtra("outTradeNo", aa.AlipayOrder(response.getAliPayRes()));
			intents.putExtra("Ono", aa.AlipayWXOrder(response.getWechatRes()));
			intents.putExtra("Ccard", "Mid=" + Mid + "&Total=" + tol + "&Goods=" + getIntent().getStringExtra("Yid")
					+ "_" + Price + "_" + num);
			intents.putExtra("totalPrice", String.valueOf(tol));
			intents.putExtra("goodsInfo", goodsInfo);
			startActivity(intents);
			DialogActivity.this.finish();
		} else {
			Toast.makeText(mContext, "网络错误", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mContext = null;
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	private void showProgressDialog() {
		dialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
		dialog.getWindow().setDimAmount(0.3f);// 设置昏暗度为0
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		dialog.setCanceledOnTouchOutside(false);
		dialog.getWindow().setContentView(R.layout.loading_dialog_dialog);
	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
	@SuppressLint("InlinedApi")
	protected void hideBottomUIMenu() {
		// 隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
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

}
