package activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.orhanobut.logger.Logger;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.Goods;
import domain.GoodsPosition;
import uartJni.Uartjni;
import utils.CommandPackage;
import utils.ShoppingCarManager;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;

public class LoaddingActivity extends BaseActivity {

	protected static final int SEND_SHIPMENT_CMD = 0;
	private TextView tvInfo;
	private Uartjni mUartNative;
	private ShoppingCarManager shoppingCarManager;
	private AtomicInteger MachineSateCode;
	private int cycleCount;
	private List<GoodsPosition> goodsPositions;
	private int shipmentCount = 0;
	private Myhandler handler;
	private String Ono;
	private String outTradeNo;
	private String membershipPayforCode;
	private String payfor;
	private String mid;
	private Context mContext;

	class Myhandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SEND_SHIPMENT_CMD:
				sendShipmentCmd();
				break;
			default:
				break;
			}
		}
	}

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
				setTime(240);
				if (arg1.length >= 4) {
					switch (arg1[2]) {
					// 成功
					case 0:
						removeListGoods();
						break;
					// 出货失败，发送的命令格式错误
					case (byte) 0xff:
						break;
					// 出货失败机器正忙
					case (byte) 0xFE:
						// TODO 出货失败，这种情况基本是机器出故障了

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
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	protected void removeListGoods() {
		GoodsPosition goodsPosition = goodsPositions.get(shipmentCount);
		goodsPosition.setRemoveGoods(true);
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
				str2Voice("正在准备出货，请稍等");
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
							str2Voice(goodsPositions.get(shipmentCount - 1).getYname() + "出货成功，请您注意取货");
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
				str2Voice("正在关门，请您注意安全，防止夹手");
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
			// parseMachineFault(arg1);
			break;
		// 还篮子状态
		case 0x10:
			MachineSateCode.set(10);
			break;
		// 机器回零状态
		case 0x11:
			MachineSateCode.set(11);
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
				if (TextUtils.isEmpty(payfor)) {
					return;
				}
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
		JniThreadStop();
	}

	private void JniThreadStop() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (mUartNative != null) {
					mUartNative.NativeThreadStop();
					mUartNative = null;
				}
			}
		});
	}

	// 出货的准备
	private void initData() {
		goodsPositions = new ArrayList<GoodsPosition>();
		MachineSateCode = new AtomicInteger(0);
		MachineSateCode.set(-1);
		mContext = LoaddingActivity.this;

		handler = new Myhandler();
		membershipPayforCode = getIntent().getStringExtra("membershipPayforCode");
		outTradeNo = getIntent().getStringExtra("outTradeNo");
		payfor = getIntent().getStringExtra("payfor");
		Ono = getIntent().getStringExtra("Ono");
		mid = utils.Util.getMid();
		shoppingCarManager = ShoppingCarManager.getInstence();
		parseGoodsInfo(getIntent().getStringExtra("goodsInfo"));
	}

	public void sendShipmentCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (mUartNative == null) {
						str2Voice("机器出错，请联系客服处理");
						return;
					}
					shipmentCount = 0;
					for (GoodsPosition goodsPosition : goodsPositions) {
						cycleCount = 0;
						while (true) {
							if (MachineSateCode.get() == 1) {
								break;
							}
							if (MachineSateCode.get() == 2) {
								break;
							}
							// 判断机此时的状态
							mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd,
									ConstantCmd.getMachineStateCmd.length);
							if (cycleCount++ > 150) {
								// TODO 注意这里的时，循环次数的问题
								break;
							}
							if (cycleCount == 120) {
								str2Voice("请立即将货物取出");
							}
							Util.delay(1000);
						}

						int columnNum = goodsPosition.getColumnNum();
						int rowNum = goodsPosition.getRowNum();
						byte[] command = CommandPackage.getRequestShipment(ConstantCmd.get_request_shipment_cmd, rowNum,
								columnNum);
						// 延时100毫秒
						Util.delay(200);
						// TODO 正在出货 发送下一个出货命令的时候 要注意询问机器此时的状态；
						mUartNative.UartWriteCmd(command, command.length);
						// 清除机器此时的状态
						cycleCount = 0;
						MachineSateCode.set(0);
						shipmentCount++;
						// 延时100毫秒
						Util.delay(200);
					}
					cycleCount = 0;
					while (true) {
						Util.delay(1000);
						if (cycleCount++ > 120) {
							break;
						}
						mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd, ConstantCmd.getMachineStateCmd.length);
						if (shipmentCount == goodsPositions.size() && MachineSateCode.get() == 0x01) {
							break;
						}
					}
					// 判断货物是否出出完
					str2Voice("机器出货完成, 感谢您的本次购物");
					removeInventory();
					startActivity(new Intent(LoaddingActivity.this, SplashActivity.class));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 是否完成出货
	 * 
	 * @return 完成出货 返回true，反之返回false
	 */
	public boolean isCompleteShipment() {
		for (GoodsPosition goodsPosition : goodsPositions) {
			if (!goodsPosition.isRemoveGoods()) {
				return true;
			}
		}
		return true;
	}

	private void parseGoodsInfo(final String goodsInfo) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Logger.e(goodsInfo);
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
								GoodsPosition goodsPosition = new GoodsPosition(
										Integer.parseInt(rowAndColumnStr[0], 16), Integer.parseInt(rowAndColumnStr[1]),
										Yid, goodsName, false);
								Logger.e(goodsPosition.getColumnNum() + ":" + goodsPosition.getRowNum());
								goodsPositions.add(goodsPosition);
							}
						}
						Util.sendMessage(handler, SEND_SHIPMENT_CMD);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
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

	@Override
	public void onActivityDectory() {
		LoaddingActivity.this.finish();
	}
}
