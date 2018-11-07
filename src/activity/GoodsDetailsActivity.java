package activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.conn.ConnectTimeoutException;

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
import android_serialport_api.bb;
import android_serialport_api.sample.R;
import domain.AlreadyToBuyGoods;
import uartJni.Uartjni;
import utils.ActivityManager;
import utils.SharePreferenceUtils;
import utils.ShoppingCarManager;
import utils.VoiceUtils;
import view.BannerLayout;

/**
 * @author xxy
 */

@SuppressLint("NewApi")
public class GoodsDetailsActivity extends FragmentActivity implements OnItemClickListener {
	private static final int TIME = 420;
	protected static final int MACHINE_HAVE_FAULT = 0;
	protected static int COUNT_DOWN_TIME = TIME;
	List<PersionInfo> listinfoInfos = new ArrayList<PersionInfo>();
	private ListView listView;
	private MyAdapter adapter;
	private MyFragment myFragment;
	BadgeView badgeView;
	// 倒计时
	TextView txtView;
	private Timer timer = new Timer();
	private Timer countDownTimer;
	// 广播机制
	private ShoppingCarManager shoppingCarManager;
	private List<AlreadyToBuyGoods> shoppingCarGoods;
	private TimerTask countDownTask;
	private static int[] locations;
	private int[] layoutLocations;
	private GoodsNumReceiver goodsNumReceiver;
	private TextView tvTime;
	private Uartjni mUartNative;
	private static byte[] getMachineStateCmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
	private Context mContext = GoodsDetailsActivity.this;
	private boolean isTakeGoods;

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
		initSerial();
		initCountDown();
		hideBottomUIMenu();
		SharePreferenceUtils.clear(getApplicationContext());
		
		
		// 倒计时
		txtView = (TextView) findViewById(R.id.txttime);
		// 返回点击事件
		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// showExitGameAlert();
				Intent intent = new Intent(GoodsDetailsActivity.this, IuMainActivity.class);
				startActivity(intent);
				GoodsDetailsActivity.this.finish();
			}
		});

		// 购物车
		ImageView Shopping = (ImageView) findViewById(R.id.Shopping);
		// 购物车的购买数量
		badgeView = new BadgeView(GoodsDetailsActivity.this, Shopping); // 实例化BadgeView
		shoppingCarGoods = shoppingCarManager.getShoppingCarGoods();
		int kk = 0;
		if (shoppingCarGoods.size() > 0) {
			for (int e = 0; e < shoppingCarGoods.size(); e++) {
				// 订单总数 第一次没有num总数 只有订单那里有zonnum属性
				kk = kk + shoppingCarGoods.get(e).getAlreadyToBuyGoodsnum();
			}
		}
		badgeView.setText(String.valueOf(kk));
		badgeView.setTextSize(20f); // 设置文字的大小
		badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);// 设置在右上角
		badgeView.setTextColor(Color.WHITE); // 字体的设置颜色
		badgeView.show(); // 显示
		// 购物车的点击事件
		Shopping.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(GoodsDetailsActivity.this, ShoppingCarActivity.class);
				startActivity(intent);
			}
		});
	}

	private void sendCmd() {
		mUartNative.UartWriteCmd(getMachineStateCmd, getMachineStateCmd.length);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		COUNT_DOWN_TIME = TIME;
		return super.dispatchTouchEvent(ev);
	}

	private void initSerial() {
		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] arg1) {
				if (arg1.length >= 4) {
					switch (arg1[2]) {
					case 0x10:
						parseMachineStateCode(arg1[3], arg1[4]);
						break;
					default:
						break;
					}
				}
				handler.sendEmptyMessage(3);
			}
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	protected void parseMachineStateCode(final byte cmd, final byte arg1) {
		switch (cmd) {
		// 待机状态
		case 0x01:
			isTakeGoods = true;
			break;
		// 待机转动状态
		case 0x02:
			isTakeGoods = false;
			break;
		// 正在定位
		case 0x03:
			isTakeGoods = false;
			break;
		// 正在开门
		case 0x04:
			isTakeGoods = false;
			break;
		// 等待取物状态
		case 0x05:
			isTakeGoods = false;
			break;
		// 正在关门
		case 0x06:
			isTakeGoods = false;
			break;
		// 补货状态
		case 0x07:
			isTakeGoods = false;
			break;
		// 测试动作状态
		case 0x08:
			isTakeGoods = false;
			break;
		// 故障状态
		case 0x09:
			// TODO 此处提醒用户此时机器处于故障状态；不能出货；
			// 我们需要分析此时的故障的状态，然后上报服务器
			parseMachineFault(arg1);
			break;
		// 还篮子状态
		case 0x10:
			isTakeGoods = false;
			break;
		default:
			break;
		}
		if (!isTakeGoods) {
			utils.Util.sendMessage(handler, MACHINE_HAVE_FAULT);
		}
	}

	protected void parseMachineFault(byte b) {
		switch (b) {
		// 定位故障(主电机或位置光电开关故障）
		case 0x01:
			isTakeGoods = false;
			break;
		// 开门故障
		case 0x02:
			isTakeGoods = false;
			break;
		// 关门故障
		case 0x03:
			isTakeGoods = false;
			break;
		// 防夹传感器故障
		case 0x04:
			isTakeGoods = false;
			break;
		// 制冷故障
		case 0x05:
			isTakeGoods = true;
			break;
		// 加湿故障（缺水或长期湿度达不到设定值）
		case 0x06:
			isTakeGoods = true;
			break;
		// 其他故障
		case 0x07:
			isTakeGoods = false;
			break;
		// 主电机故障
		case 0x08:
			isTakeGoods = false;
			break;
		default:
			break;
		}
	}

	// 开启倒计时
	private void initCountDown() {
		countDownTimer.schedule(countDownTask, 1000, 1000);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		COUNT_DOWN_TIME = TIME;
	}

	private void initData() {
		ActivityManager.getInstance().addActivity(GoodsDetailsActivity.this);
		new Thread() {
			public void run() {
				String str = new String();
				try {
					str = bb.getHttpResult("http://linliny.com/dingyifeng_web/getfenzhuSan.json");
					if (str != null) {
						utils.Util.sendMessage(handler, 2, str);
					} else {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), "网络错误", Toast.LENGTH_LONG).show();
							}
						});
					}
				} catch (ConnectTimeoutException e) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
							VoiceUtils.getInstance().initmTts(mContext, "网络错误，请重试");
						}
					});
				}
			};
		}.start();

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
							ActivityManager.getInstance().finshAllActivity();
						}
					});
				}
			}
		};
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
		TextView tvTitle = (TextView) findViewById(R.id.goods_detils_title);
		BannerLayout bannerLayout1 = (BannerLayout) findViewById(R.id.banner2);
		listView = (ListView) findViewById(R.id.listview);
		ImageView ivArrow = (ImageView) findViewById(R.id.iv_arrow_goodsdetail);
		startAnimation(ivArrow);
		Intent intent = getIntent();
		String title = intent.getStringExtra("title");
		tvTitle.setText(title);
		List<Integer> res = new ArrayList<Integer>();
		listView.setOnItemClickListener(this);
		res.add(R.drawable.viewpage_1);
		res.add(R.drawable.viewpage_2);
		res.add(R.drawable.viewpage_3);
		
		List<String> titles = new ArrayList<String>();
		titles.add(" ");
		titles.add(" ");
		titles.add(" ");
		if (bannerLayout1 != null) {
			bannerLayout1.setViewRes(res, titles);
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

	// 计时器
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MACHINE_HAVE_FAULT:
				utils.Util.DisplayToast(GoodsDetailsActivity.this, "机器处于故障状态", R.drawable.fail);
				VoiceUtils.getInstance().initmTts(getApplicationContext(), "机器处于故障状态, 请你稍后进行购买");
				break;
			case 2:
				parseMessage(msg.obj.toString());
				break;
			case 3:
				if (!isTakeGoods) {
					VoiceUtils.getInstance().initmTts(getApplicationContext(), "机器故障，正在调试");
				}
				if (mUartNative != null) {
					mUartNative.NativeThreadStop();
				}
				break;
			default:
				break;
			}
		}
	};
	private AnimatorSet set;

	/**
	 * 关闭页面时销毁定时器
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		countDownTimer.cancel();
		countDownTask.cancel();
		shoppingCarManager.clearShoppingCar();
		if(goodsNumReceiver != null) {
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

	class LocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 这里写接收到广播后要执行的操作
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
		}
	}

	public void setGoodsNum(int num) {
		if (badgeView != null) {
			badgeView.setText(String.valueOf(num));
		}
	}

	public int[] getLocations() {
		return locations;
	}

	public int[] getLayoutLocations() {
		return layoutLocations;
	}

	public GoodsDetailsActivity getGoodsDetilsActivity() {
		if (this.isDestroyed()) {
			return null;
		}
		return this;
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
		sendCmd();
		COUNT_DOWN_TIME = TIME;
	}

	/**
	 * 隐藏虚拟按键，并且全屏
	 */
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

	@Override
	protected void onPause() {
		super.onPause();
		if (mUartNative != null) {
			mUartNative.NativeThreadStop();
		}
	}

}
