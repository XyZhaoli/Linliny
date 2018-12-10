package activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.readystatesoftware.viewbadger.BadgeView;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.MyAdapter;
import android_serialport_api.PersionInfo;
import android_serialport_api.aa;
import android_serialport_api.sample.R;
import domain.AlreadyToBuyGoods;
import domain.ConstantCmd;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.VoiceUtils;
import view.BannerLayout;

/**
 * @author xxy
 */

@SuppressLint("NewApi")
public class GoodsDetailsActivity extends FragmentActivity implements OnItemClickListener, View.OnClickListener {
	private static final int TIME = 420;
	private static final int GET_GOODS_NUM = 4;
	protected static int COUNT_DOWN_TIME = TIME;
	List<PersionInfo> listinfoInfos = new ArrayList<PersionInfo>();
	private ListView listView;
	private MyAdapter adapter;
	private MyFragment myFragment;
	private BadgeView badgeView;
	private Timer countDownTimer;
	// 广播机制
	private ShoppingCarManager shoppingCarManager;
	private List<AlreadyToBuyGoods> shoppingCarGoods = new ArrayList<AlreadyToBuyGoods>();
	private TimerTask countDownTask;
	private static int[] locations;
	private GoodsNumReceiver goodsNumReceiver;
	private TextView tvTime;
	private Context mContext = GoodsDetailsActivity.this;
	private MyHandler handler = new MyHandler(mContext);
	private ImageView ivShoppingCar;
	private boolean isPause;
	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		locations = new int[2];
		badgeView.getLocationInWindow(locations);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);
		setContentView(R.layout.ui);
		initBroadcast();
		initView();
		initData();
		initCountDown();
		hideBottomUIMenu();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		COUNT_DOWN_TIME = TIME;
		return super.dispatchTouchEvent(ev);
	}


	// 开启倒计时
	private void initCountDown() {
		if(countDownTimer != null && countDownTimer != null) {
			countDownTimer.schedule(countDownTask, 1000, 1000);
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		isPause = false;
		COUNT_DOWN_TIME = TIME;
	}

	private void initData() {
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(10);
		httpUtils.send(HttpMethod.GET, ConstantCmd.BASE_URLS + "getfenzhuSan.json",
				new RequestCallBack<String>() {
					@Override
					public void onFailure(HttpException arg0, String arg1) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
								str2Voice("网络错误，请重试");
							}
						});
					}

					@Override
					public void onSuccess(ResponseInfo<String> arg0) {
						if (!TextUtils.isEmpty(arg0.result)) {
							utils.Util.sendMessage(handler, 2, arg0.result);
						} else {
							httpGetFail();
						}
					}
				});

		COUNT_DOWN_TIME = TIME;
		shoppingCarManager = ShoppingCarManager.getInstence();
		countDownTimer = new Timer();
		countDownTask = new TimerTask() {
			@Override
			public void run() {
				if (COUNT_DOWN_TIME >= 0) {
					COUNT_DOWN_TIME--;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tvTime.setText(COUNT_DOWN_TIME + "s");
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							GoodsDetailsActivity.this.finish();
							if(!isPause) {
								GoodsDetailsActivity.this.finish();
							}
						}
					});
				}
			}
		};

		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 购物车的购买数量
				shoppingCarGoods.clear();
				shoppingCarGoods.addAll(shoppingCarManager.getShoppingCarGoods());
				int goodsSum = 0;
				if (shoppingCarGoods.size() > 0 && shoppingCarGoods != null) {
					for (AlreadyToBuyGoods goods : shoppingCarGoods) {
						if (goods != null) {
							goodsSum = goods.getAlreadyToBuyGoodsnum() + goodsSum;
						}
					}
				}
				utils.Util.sendMessage(handler, GET_GOODS_NUM, goodsSum);
			}
		});
	}

	private void str2Voice(final String string) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(string)) {
					VoiceUtils.getInstance().initmTts(string);
				}
			}
		});
	}

	protected void httpGetFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
				str2Voice("网络错误，请重试");
			}
		});
	}

	private void initBroadcast() {
		goodsNumReceiver = new GoodsNumReceiver();
		IntentFilter intentFiltet = new IntentFilter();
		// 设置广播的名字（设置Action，可以添加多个要监听的动作）
		intentFiltet.addAction("getGoodsNumAction");
		// 注册广播,传入两个参数， 实例化的广播接受者对象，intent 动作筛选对象
		registerReceiver(goodsNumReceiver, intentFiltet);
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		tvTime = (TextView) findViewById(R.id.tv_time_goodsdetails_activity);
		final BannerLayout bannerLayout1 = (BannerLayout) findViewById(R.id.banner2);
		listView = (ListView) findViewById(R.id.listview);
		startAnimation((ImageView) findViewById(R.id.iv_arrow_goodsdetail));
		ivShoppingCar = (ImageView) findViewById(R.id.Shopping);
		TextView tvTitle = (TextView) findViewById(R.id.goods_detils_title);
		listView.setOnItemClickListener(this);
		// 返回点击事件
		findViewById(R.id.back).setOnClickListener(this);
		badgeView = new BadgeView(GoodsDetailsActivity.this, ivShoppingCar); // 实例化BadgeView
		ivShoppingCar.setOnClickListener(this);

		tvTitle.setText(getIntent().getStringExtra("title"));
		final List<Integer> res = new ArrayList<Integer>();
		res.add(R.drawable.viewpage_1);
		res.add(R.drawable.viewpage_2);
		res.add(R.drawable.viewpage_3);
		final List<String> titles = new ArrayList<String>();
		titles.add(" ");
		titles.add(" ");
		titles.add(" ");
		if (bannerLayout1 != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					bannerLayout1.setViewRes(res, titles);
				}
			});
		}
	}

	private void startAnimation(ImageView ivArrow) {
		ObjectAnimator animatorTop = ObjectAnimator.ofFloat(ivArrow, "translationY", -100);
		ObjectAnimator animatorButtom = ObjectAnimator.ofFloat(ivArrow, "translationY", 0);
		AnimatorSet set = new AnimatorSet();
		set.play(animatorTop).before(animatorButtom);
		set.setDuration(1000);
		set.start();
	}

	// 列表子类的点击监听事件
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PersionInfo info = listinfoInfos.get(position);
		for (int i = 0; i < listinfoInfos.size(); i++) {
			if (listinfoInfos.get(i).getNameString().equals(info.getNameString())) {
				listinfoInfos.get(i).setChick(true);
			} else {
				listinfoInfos.get(i).setChick(false);
			}
		}
		adapter.notifyDataSetChanged();// 刷新list列表
		// 创建MyFragment对象
		// 即使刷新adapter
		myFragment = new MyFragment();
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_container, myFragment);
		Bundle mBundle = new Bundle();
		mBundle.putSerializable("info", listinfoInfos.get(position));
		myFragment.setArguments(mBundle);
		fragmentTransaction.commitAllowingStateLoss();
	}

	class MyHandler extends Handler {

		WeakReference<Context> mWeakReference;

		public MyHandler(Context activity) {
			mWeakReference = new WeakReference<Context>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2:
				parseMessage(msg.obj.toString());
				break;
			case GET_GOODS_NUM:
				// TODO 注意空指针异常
				if (msg.obj != null) {
					badgeView.setText(String.valueOf(msg.obj.toString()));
					badgeView.setTextSize(20f); // 设置文字的大小
					badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);// 设置在右上角
					badgeView.setTextColor(Color.WHITE); // 字体的设置颜色
					badgeView.show(); // 显示
				}
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 关闭页面时销毁定时器
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (countDownTimer != null && countDownTask != null) {
			countDownTimer.cancel();
			countDownTask.cancel();
		}
		shoppingCarManager.clearShoppingCar();
		if (goodsNumReceiver != null) {
			unregisterReceiver(goodsNumReceiver);
		}
	}


	protected void parseMessage(String str) {
		if (!TextUtils.isEmpty(str) && !GoodsDetailsActivity.this.isDestroyed()) {
			List<Map<String, Object>> list = aa.listMaps(str);
			listinfoInfos.add(new PersionInfo("全部商品"));
			for (Map<String, Object> map : list) {
				listinfoInfos.add(new PersionInfo(map.get("YTname").toString()));
			}
			// 默认
			listinfoInfos.get(0).setChick(true);
			// 传参
			adapter = new MyAdapter(this, listinfoInfos);
			listView.setAdapter(adapter);
			// 子类监听事件
			// 创建MyFragment对象
			myFragment = new MyFragment();
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			fragmentTransaction.replace(R.id.fragment_container, myFragment);
			// 通过bundle传值给MyFragment
			Bundle mBundle = new Bundle();
			mBundle.putSerializable("info", listinfoInfos.get(0));
			myFragment.setArguments(mBundle);
			fragmentTransaction.commitAllowingStateLoss();
		} else {
			Toast.makeText(this, "网络返回数据出错，请检查网络", Toast.LENGTH_SHORT).show();
		}
	}

	public void setGoodsNum(int num) {
		if (badgeView != null && num >= 0) {
			badgeView.setText(String.valueOf(num));
		}
	}

	class GoodsNumReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int carGoodsAllCount = shoppingCarManager.getCarGoodsAllCount();
			setGoodsNum(carGoodsAllCount);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		COUNT_DOWN_TIME = TIME;
	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
	protected void hideBottomUIMenu() {
		// 隐藏虚拟按键，并且全屏
		if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
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

	@Override
	protected void onPause() {
		super.onPause();
		isPause = true;
	}

	public int[] getLocations() {
		return locations;
	}

	@Override
	public void onClick(View v) {
		// TODO
		switch (v.getId()) {
		case R.id.back:
			startActivity(new Intent(GoodsDetailsActivity.this, IuMainActivity.class));
			GoodsDetailsActivity.this.finish();
			break;
		case R.id.Shopping:
			startActivity(new Intent(GoodsDetailsActivity.this, ShoppingCarActivity.class));
			break;
		default:
			break;
		}
	}

}
