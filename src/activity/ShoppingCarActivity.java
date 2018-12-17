package activity;

import java.util.List;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android_serialport_api.MyAdapterCart;
import android_serialport_api.aa;
import android_serialport_api.sample.R;
import dialog.CheckOutDialog;
import domain.AlreadyToBuyGoods;
import domain.PayforResponse;
import utils.ActivityManager;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;

public class ShoppingCarActivity extends BaseActivity implements AdapterView.OnItemClickListener {
	public static final String BUY_GOODS_DATA = "shoppingCart";
	private static final int WECHAT_FLAG = 1;
	private Button cancel;
	private Button btnPayfor;
	private MyHandler handler = new MyHandler();
	private ShoppingCarManager shoppingCarManager;
	private StringBuffer goodsTotalPrice;
	// 微信的支付信息
	private StringBuffer wechatArgu;
	// 阿里的支付信息
	private StringBuffer alipayArgu;
	private Context mContext = ShoppingCarActivity.this;
	private StringBuilder ccard;
	private List<AlreadyToBuyGoods> shoppingCarGoods;
	private CheckOutDialog checkOutDialog;

	class MyHandler extends Handler {
		@Override
		public void dispatchMessage(Message msg) {
			parseResponse(msg.obj);
		}
	}

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);// 点外面，弹窗消失
		setContentView(R.layout.ui_shoppingcart);
		initData();
		initView();
	}

	public void parseResponse(Object obj) {
		PayforResponse response = (PayforResponse) obj;
		checkOutDialog = new CheckOutDialog(mContext, R.style.MyDialogStyle, response,
				ccard.toString(), String.valueOf(goodsTotalPrice.toString()), wechatArgu.toString());
		Util.showCustomDialog(checkOutDialog, (Activity) mContext);
	}

	// 订单信息表
	private void initView() {
		cancel = (Button) findViewById(R.id.btn_cancel_shopping_car_activity);
		final ListView listView1 = (ListView) findViewById(R.id.list1);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				ShoppingCarActivity.this.finish();
			}
		});
		btnPayfor = (Button) findViewById(R.id.btn_pay_for);
		listView1.setAdapter(new MyAdapterCart(ShoppingCarManager.getInstence().getShoppingCarGoods()));

		btnPayfor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (shoppingCarManager.getShoppingCarGoods().size() == 0) {
					Toast.makeText(ShoppingCarActivity.this, "购物车空空如也，快去添加商品到购物车里卖来吧！", Toast.LENGTH_LONG).show();
				} else {
					ThreadManager.getThreadPool().execute(new Runnable() {
						@Override
						public void run() {
							try {
								final PayforResponse response = getPackageUrls();
								HttpUtils httpUtils = new HttpUtils();
								httpUtils.configCurrentHttpCacheExpiry(1000);
								String wechatResponse;
								wechatResponse = httpUtils.sendSync(HttpMethod.GET, response.getWechatRes()).readString();
								String alipayReponse = httpUtils.sendSync(HttpMethod.GET, response.getAliPayRes()).readString();
								if (!TextUtils.isEmpty(wechatResponse) && !TextUtils.isEmpty(alipayReponse)) {
									PayforResponse urls = new PayforResponse(wechatResponse, alipayReponse);
									utils.Util.sendMessage(handler, WECHAT_FLAG, urls);
								} else {
									httpGetFail();
								}
							} catch (Exception e) {
								httpGetFail();
								e.printStackTrace();
							}
						}
					});
				}
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

	}

	private PayforResponse getPackageUrls() {
		// 购买商品的总价
		double totalPrices = 0;
		// 购买商品的总数
		int totalCount = 0;
		int flag = 0;
		ccard = new StringBuilder();
		wechatArgu = new StringBuffer();
		alipayArgu = new StringBuffer();
		goodsTotalPrice = new StringBuffer();
		String Mid = Util.getMid();
		
		for (AlreadyToBuyGoods alreadyToBuyGoods : shoppingCarGoods) {
			flag++;
			int num = alreadyToBuyGoods.getAlreadyToBuyGoodsnum();
			// 单个商品的总价
			double price = alreadyToBuyGoods.getAlreadyToBuyGoodsPrice();
			String Yid = alreadyToBuyGoods.getAleardyBuyGoods().getYid();
			String Yname = alreadyToBuyGoods.getAleardyBuyGoods().getYname().replaceAll(" ", "");
			// 商品的单价
			String signalPrice = alreadyToBuyGoods.getAleardyBuyGoods().getPrice().trim();
			totalCount += num;
			totalPrices += price;

			wechatArgu.append(Yid).append('_').append(signalPrice).append('_').append(num);
			alipayArgu.append(Yid).append('_').append(Yname).append('_').append(signalPrice).append('_').append(num);

			if (flag < shoppingCarGoods.size()) {
				wechatArgu.append("*");
				alipayArgu.append("*");
			}
		}
		goodsTotalPrice.append(totalPrices);
		String wechatUrls = "http://linliny.com/dingyifeng_web/AddOrd.json?Mid=" + Mid + "&Total=" + totalPrices
				+ "&Goods=" + wechatArgu;
		String alipayUrls = "http://linliny.com/dingyifeng_web/AddOrdALiPay.json?Mid=" + Mid + "&Total=" + totalPrices
				+ "&Num=" + totalCount + "&Goods=" + alipayArgu;
		
		ccard.append("Mid=").append(Mid).append("&Total=").append(totalPrices).append("&Goods=").append(wechatArgu);
		return new PayforResponse(wechatUrls, alipayUrls);
	}

	private void initData() {
		shoppingCarManager = ShoppingCarManager.getInstence();
		shoppingCarGoods = shoppingCarManager.getShoppingCarGoods();
	}

	@Override
	public void changeTvTime(int time) {
		// TODO Auto-generated method stub

	}
	
	
	private void httpGetFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
				VoiceUtils.getInstance().initmTts("网络错误，请重试");
			}
		});
	}

	@Override
	public void onActivityDectory() {
		Util.disMissDialog(checkOutDialog, ShoppingCarActivity.this);
		ShoppingCarActivity.this.finish();
	}

}