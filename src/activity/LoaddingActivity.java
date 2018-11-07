package activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.Goods;
import domain.GoodsPosition;
import uartJni.Uartjni;
import utils.ActivityManager;
import utils.CommandPackage;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.VoiceUtils;

public class LoaddingActivity extends BaseAcitivity {

	private TextView tvInfo;
	private Uartjni mUartNative;
	private ShoppingCarManager shoppingCarManager;
	private static AtomicInteger MachineSateCode = new AtomicInteger(0);
	private int cycleCount;
	private List<GoodsPosition> goodsPositions = new ArrayList<GoodsPosition>();
	private static int shipmentCount = 0;
	private Handler handler = new Handler();
	private static final byte[] getMachineStateCmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
	private static int currentFaultCode;
	private String Ono;
	private String outTradeNo;
	private String membershipPayforCode;
	private String payfor;
	private String mid;
	private Context mContext = LoaddingActivity.this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading_dialog);
		initView();
		initSerial();
		initData();
		hideBottomUIMenu();
	}

	private void initSerial() {
		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] arg1) {
				setTime(120);
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (arg1.length >= 4) {
							switch (arg1[2]) {
							// 成功
							case 0:
								str2voice("正在准备出货，请稍等");
								break;
							// 出货失败，发送的命令格式错误
							case (byte) 0xff:
								break;
							// 出货失败机器正忙
							case (byte) 0xFE:
								break;
							// execute_error
							case (byte) 0xFD:
								break;
							case 0x10:
								parseMachineStateCode(arg1[3], arg1[4]);
								break;
							default:
								break;
							}
						}
					}
				});
			}
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	protected void parseMachineFault(byte b) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				str2voice("机器正忙，出货失败，请您稍后再试");
			}
		});
		switch (b) {
		// 定位故障(主电机或位置光电开关故障）
		case 0x01:
			currentFaultCode = 1;
			break;
		// 开门故障
		case 0x02:
			currentFaultCode = 2;
			break;
		// 关门故障
		case 0x03:
			currentFaultCode = 3;
			break;
		// 防夹传感器故障
		case 0x04:
			currentFaultCode = 4;
			break;
		// 制冷故障
		case 0x05:
			currentFaultCode = 5;
			break;
		// 加湿故障（缺水或长期湿度达不到设定值）
		case 0x06:
			currentFaultCode = 6;
			break;
		// 其他故障
		case 0x07:
			currentFaultCode = 7;
			break;
		// 主电机故障
		case 0x08:
			currentFaultCode = 8;
			break;
		default:
			break;
		}
	}

	protected void parseMachineStateCode(final byte cmd, final byte arg1) {
		switch (cmd) {
		// 待机状态
		case 0x01:
			MachineSateCode.set(1);
			break;
		// 待机转动状态
		case 0x02:
			MachineSateCode.set(2);
			break;
		// 正在定位
		case 0x03:
			MachineSateCode.set(3);
			break;
		// 正在开门
		case 0x04:
			if (MachineSateCode.get() != 4) {
				str2voice("正在准备出货，请稍等");
			}
			MachineSateCode.set(4);
			break;
		// 等待取物状态
		case 0x05:
			if (MachineSateCode.get() != 5) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							str2voice(goodsPositions.get(shipmentCount - 1).getYname() + "出货成功，请您注意取货");
							tvInfo.setText(goodsPositions.get(shipmentCount - 1).getYname() + "出货成功，请您注意取货");
						} catch (Exception e) {

						}
					}
				});
			}
			MachineSateCode.set(5);
			break;
		// 正在关门
		case 0x06:
			if (MachineSateCode.get() != 6) {
				str2voice("正在关门，请您注意安全，防止夹手");
			}
			MachineSateCode.set(6);
			break;
		// 补货状态
		case 0x07:
			MachineSateCode.set(7);
			break;
		// 测试动作状态
		case 0x08:
			MachineSateCode.set(8);
			break;
		// 故障状态
		case 0x09:
			MachineSateCode.set(9);
			// TODO 此处提醒用户此时机器处于故障状态；不能出货；
			// 我们需要分析此时的故障的状态，然后上报服务器
			parseMachineFault(arg1);
			break;
		// 还篮子状态
		case 0x10:
			MachineSateCode.set(10);
			break;
		default:
			break;
		}
	}

	protected void removeInventory() {
		// TODO 支付成功的时候 我们要准备出货
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				// 减库存,测试时关闭减库存
				String url = null;
				if (payfor.equals("wechat")) {
					url = "http://linliny.com/dingyifeng_web/shanchukuncuntion.json?model=&request=&MiD=" + mid
							+ "&Ono=" + Ono;
				} else if (payfor.equals("alipay")) {
					url = "http://linliny.com/dingyifeng_web/shanchukuncuntion.json?model=&request=&MiD=" + mid
							+ "&Ono=" + outTradeNo;
				} else if (payfor.equals("membership")) {
					// TODO 这里的会员卡支付减库存的URL 还没有做完
					url = "http://linliny.com/dingyifeng_web/shanchukuncuntion.json?model=&request=&MiD=" + mid
							+ "&Ono=" + membershipPayforCode;
				}
				HttpUtils httpUtils = new HttpUtils();
				try {
					httpUtils.sendSync(HttpMethod.GET, url);
				} catch (HttpException e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							utils.Util.DisplayToast(mContext, "网络错误，请检查网络", R.drawable.warning);
						}
					});
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("onDestroy", "onDestroy");
		if (mUartNative != null) {
			mUartNative.NativeThreadStop();
		}
	}

	protected void str2voice(String string) {
		VoiceUtils.getInstance().initmTts(getApplicationContext(), string);
	}

	// 出货的准备
	private void initData() {
		Ono = getIntent().getStringExtra("Ono");
		outTradeNo = getIntent().getStringExtra("outTradeNo");
		membershipPayforCode = getIntent().getStringExtra("membershipPayforCode");
		payfor = getIntent().getStringExtra("payfor");
		mid = utils.Util.getMid();
		shoppingCarManager = ShoppingCarManager.getInstence();
		parseGoodsInfo(getIntent().getStringExtra("goodsInfo"));
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					shipmentCount = 0;
					for (GoodsPosition goodsPosition : goodsPositions) {
						cycleCount = 0;
						while (MachineSateCode.get() != 1) {
							// 判断机器此时的状态
							mUartNative.UartWriteCmd(getMachineStateCmd, getMachineStateCmd.length);
							if (cycleCount++ > 80) {
								// TODO 注意这里的时，循环次数的问题
								break;
							}
							utils.Util.delay(1000);
						}

						int columnNum = goodsPosition.getColumnNum();
						int rowNum = goodsPosition.getRowNum();
						byte[] command = CommandPackage.getRequestShipment(ConstantCmd.get_request_shipment_cmd, rowNum,
								columnNum);
						mUartNative.UartWriteCmd(command, command.length);
						// 延时100毫秒
						utils.Util.delay(200);
						// TODO 正在出货 发送下一个出货命令的时候 要注意询问机器此时的状态；
						mUartNative.UartWriteCmd(command, command.length);
						// 清除机器此时的状态
						cycleCount = 0;
						MachineSateCode.set(0);
						shipmentCount++;
						// 延时100毫秒
						utils.Util.delay(200);
					}
					cycleCount = 0;
					// TODO
					while (true) {
						utils.Util.delay(1000);
						if (cycleCount++ > 60) {
							break;
						}
						mUartNative.UartWriteCmd(getMachineStateCmd, getMachineStateCmd.length);
						if (shipmentCount == goodsPositions.size() && MachineSateCode.get() == 0x01) {
							break;
						}
					}
					str2voice("机器出货完成, 感谢您的本次购物");
					removeInventory();
					ActivityManager.getInstance().addActivity(LoaddingActivity.this);
					ActivityManager.getInstance().finshAllActivity();
					startActivity(new Intent(LoaddingActivity.this, SplashActivity.class));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void parseGoodsInfo(String goodsInfo) {
		Log.e("goodsInfo", goodsInfo);
		if (!TextUtils.isEmpty(goodsInfo)) {
			try {
				String[] goodsListSArr = goodsInfo.split("\\*");
				for (String goodsInfoStr : goodsListSArr) {
					String[] singleGoodsInfoStr = goodsInfoStr.split("_");
					// 商品的Yid
					String Yid = singleGoodsInfoStr[0];
					// 商品的数量
					int goodsNum = Integer.valueOf(singleGoodsInfoStr[2]);
					Goods goods = shoppingCarManager.getGoods(Yid);
					String untiekes = goods.getUntiekes();
					String goodsName = utils.Util.paseYname(goods.getYname());
					String[] split = untiekes.split(",");
					for (int i = 0; i < goodsNum; i++) {
						String[] rowAndColumnStr = split[i].split("-");
						GoodsPosition goodsPosition = new GoodsPosition(Integer.parseInt(rowAndColumnStr[0], 16),
								Integer.parseInt(rowAndColumnStr[1]), Yid, goodsName);
						Log.e("rowAndColumnStr", goodsPosition.getColumnNum() + ":" + goodsPosition.getRowNum());
						goodsPositions.add(goodsPosition);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initView() {
		tvInfo = (TextView) findViewById(R.id.tv_loadding_title);
		tvInfo.setText("付款成功！正在出货，请稍等");
		tvInfo.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				Intent home = new Intent(Intent.ACTION_MAIN);
				home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				home.addCategory(Intent.CATEGORY_HOME);
				startActivity(home);
				return false;
			}
		});
	}

	@Override
	public void changeTvTime(int time) {

	}
}
