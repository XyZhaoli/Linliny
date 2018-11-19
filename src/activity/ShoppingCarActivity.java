package activity;

import java.util.List;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
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
import domain.AlreadyToBuyGoods;
import utils.ActivityManager;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.VoiceUtils;

public class ShoppingCarActivity extends BaseAcitivity implements AdapterView.OnItemClickListener {
	public static final String BUY_GOODS_DATA = "shoppingCart";
	private static final int WECHAT_FLAG = 1;
	// 取消按钮
	private Button cancel;
	// 购买按钮
	private Button btnPayfor;
	private MyHandler handler = new MyHandler();
	private ShoppingCarManager shoppingCarManager;
	private StringBuffer goodsTotalPrice;
	// 微信的支付信息
	private StringBuffer wechatArgu;
	// 阿里的支付信息
	private StringBuffer alipayArgu;
	private Context mContext = ShoppingCarActivity.this;

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
		initView();
		initData();
	}

	public void parseResponse(Object obj) {
		payUrls urls = (payUrls) obj;
		// 解析json，获取微信支付的路径
		String WXurl = aa.weixinStr(urls.getWechatUrls());
		String ZFurl = aa.AlipayStr(urls.getAlipayUrls());
		String outTradeNo = aa.AlipayOrder(urls.getAlipayUrls());
		String Ono = aa.AlipayWXOrder(urls.getWechatUrls());

		Intent intent = new Intent(ShoppingCarActivity.this, IuPayListActivity.class);
		intent.putExtra("rs", WXurl);
		intent.putExtra("ZFurl", ZFurl);
		intent.putExtra("outTradeNo", outTradeNo);
		intent.putExtra("Ono", Ono);
		intent.putExtra("totalPrice", goodsTotalPrice.toString());
		intent.putExtra("goodsInfo", wechatArgu.toString());
		startActivity(intent);
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
		MyAdapterCart adapter = new MyAdapterCart();
		listView1.setAdapter(adapter);

		btnPayfor.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (shoppingCarManager.getShoppingCarGoods().size() == 0) {
					Toast.makeText(ShoppingCarActivity.this, "购物车空空如也，快去添加商品到购物车里卖来吧！", Toast.LENGTH_LONG).show();
				} else {
					final payUrls urls = getPackageUrls();
					ThreadManager.getThreadPool().execute(new Runnable() {
						@Override
						public void run() {
							try {
								HttpUtils httpUtils = new HttpUtils();
								httpUtils.configCurrentHttpCacheExpiry(1000);
								String wechatResponse;
								wechatResponse = httpUtils.sendSync(HttpMethod.GET, urls.wechatUrls).readString();
								String alipayReponse = httpUtils.sendSync(HttpMethod.GET, urls.alipayUrls).readString();
								if (!TextUtils.isEmpty(wechatResponse) && !TextUtils.isEmpty(alipayReponse)) {
									payUrls urls = new payUrls(wechatResponse, alipayReponse);
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

	private payUrls getPackageUrls() {
		// 购买商品的总价
		double totalPrices = 0;
		// 购买商品的总数
		int totalCount = 0;
		int flag = 0;
		wechatArgu = new StringBuffer();
		alipayArgu = new StringBuffer();
		goodsTotalPrice = new StringBuffer();
		SharedPreferences preferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		String Mid = preferences.getString("Mid", "");

		List<AlreadyToBuyGoods> shoppingCarGoods = shoppingCarManager.getShoppingCarGoods();
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
		return new payUrls(wechatUrls, alipayUrls);
	}

	private void initData() {
		shoppingCarManager = ShoppingCarManager.getInstence();
		ActivityManager.getInstance().addActivity(ShoppingCarActivity.this);
	}

	class payUrls {
		String wechatUrls;
		String alipayUrls;

		public payUrls(String wechatUrls, String alipayUrls) {
			this.wechatUrls = wechatUrls;
			this.alipayUrls = alipayUrls;
		}

		public String getWechatUrls() {
			return wechatUrls;
		}

		public void setWechatUrls(String wechatUrls) {
			this.wechatUrls = wechatUrls;
		}

		public String getAlipayUrls() {
			return alipayUrls;
		}

		public void setAlipayUrls(String alipayUrls) {
			this.alipayUrls = alipayUrls;
		}

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

}