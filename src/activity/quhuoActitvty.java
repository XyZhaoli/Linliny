package activity;

import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.GoodsPosition;
import uartJni.Uartjni;
import utils.ActivityManager;
import utils.CommandPackage;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;

public class quhuoActitvty extends BaseActivity implements OnClickListener {
	EditText myCourse_roomId_input;
	TextView number_1;
	TextView number_2;
	TextView number_3;
	TextView number_4;
	TextView number_5;
	TextView number_6;
	TextView number_7;
	TextView number_8;
	TextView number_9;
	TextView number_0;
	TextView number_enter;
	ImageView number_clear_last;
	private MyHandler handler;
	private static final int TAKE_GOODS_CODE = 0;
	private static final int SEND_CMD = 1;
	private Uartjni mUartNative;
	private static int MachineSateCode;
	private int cycleCount;
	private List<GoodsPosition> goodsPositions;
	private String ono;
	private Context mContext = quhuoActitvty.this;
	private AlertDialog dialog;
	private AlertDialog loadingDialog;
	private static int shipmentCount;

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TAKE_GOODS_CODE:
				// TODO 进行服务器验证以后，进行出货，初始化goodsPositions
				dialog.dismiss();
				if (msg.obj.toString().length() < 20 && !TextUtils.isEmpty(msg.obj.toString())) {
					parseFaultCode(msg.obj.toString());
				} else if (!TextUtils.isEmpty(msg.obj.toString())) {
					parseTakeGoodsCode(msg.obj.toString());
				}
				break;
			case SEND_CMD:
				showLoadingDialog();
				str2voice("准备出货，请稍等");
				// 发送出货命令
				sendShipmentCmd();
				break;
			default:
				break;
			}
		}
	}

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quhuo);
		initView();
		initData();
		initSerial();
	}

	public void parseFaultCode(String string) {
		if (utils.Util.parseJson(string, "checkFCode") == 0) {
			utils.Util.DisplayToast(mContext, "取货码错误", R.drawable.warning);
			str2voice("您输入的取货码有误");
		} else if (utils.Util.parseJson(string, "checkOrder") == 0) {
			utils.Util.DisplayToast(mContext, "订单有误", R.drawable.warning);
			str2voice("订单不存在");
		} else if (utils.Util.parseJson(string, "checkMid") == 0) {
			utils.Util.DisplayToast(mContext, "不是该机器设备", R.drawable.warning);
			str2voice("提货的机器错误");
		} else if (utils.Util.parseJson(string, "orderDetailed") == 0) {
			utils.Util.DisplayToast(mContext, "订单有误", R.drawable.warning);
			str2voice("订单有误");
		} else if (utils.Util.parseJson(string, "checkGrid") == 0) {
			utils.Util.DisplayToast(mContext, "未知错误，请联系客服处理", R.drawable.warning);
			str2voice("未知错误，请联系客服处理");
		}
	}

	public void parseTakeGoodsCode(final String str) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject jsonObject = new JSONObject(str);
					JSONArray js = jsonObject.getJSONArray("returnData");
					String string = js.getString(0);
					Map<String, Object> map = utils.Util.getMap(string);
					ono = map.get("Ono").toString();
					String goodsPositionsStr = map.get("Untiekes").toString();
					if (!TextUtils.isEmpty(goodsPositionsStr)) {
						String[] positionStrArray = goodsPositionsStr.split(",");
						for (int i = 0; i < positionStrArray.length; i++) {
							String[] rowAndColumnStr = positionStrArray[i].split("-");
							GoodsPosition goodsPosition = new GoodsPosition(Integer.parseInt(rowAndColumnStr[0], 16),
									Integer.parseInt(rowAndColumnStr[1]));
							goodsPositions.add(goodsPosition);
						}
					}
					Util.sendMessage(handler, SEND_CMD);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void initData() {
		ActivityManager.getInstance().addActivity(quhuoActitvty.this);
		goodsPositions = new ArrayList<GoodsPosition>();
	}

	private void initSerial() {
		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] arg1) {
				setTime(140);
				if (arg1.length >= 4) {
					switch (arg1[2]) {
					// 成功
					case 0:
						str2voice("正在开门，请稍等");
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
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	private void initView() {
		handler = new MyHandler();
		// 点击EditText禁止出现数字键盘
		number_1 = (TextView) findViewById(R.id.number_1);
		number_2 = (TextView) findViewById(R.id.number_2);
		number_3 = (TextView) findViewById(R.id.number_3);
		number_4 = (TextView) findViewById(R.id.number_4);
		number_5 = (TextView) findViewById(R.id.number_5);
		number_6 = (TextView) findViewById(R.id.number_6);
		number_7 = (TextView) findViewById(R.id.number_7);
		number_8 = (TextView) findViewById(R.id.number_8);
		number_9 = (TextView) findViewById(R.id.number_9);
		number_0 = (TextView) findViewById(R.id.number_0);

		number_1.setOnClickListener(this);
		number_2.setOnClickListener(this);
		number_3.setOnClickListener(this);
		number_4.setOnClickListener(this);
		number_5.setOnClickListener(this);
		number_6.setOnClickListener(this);
		number_7.setOnClickListener(this);
		number_8.setOnClickListener(this);
		number_9.setOnClickListener(this);
		number_0.setOnClickListener(this);

		findViewById(R.id.number_enter).setOnClickListener(this);
		findViewById(R.id.btn_confirm).setOnClickListener(this);
		findViewById(R.id.btn_cancel).setOnClickListener(this);

		number_clear_last = (ImageView) findViewById(R.id.number_clear_last);
		myCourse_roomId_input = (EditText) findViewById(R.id.editText1);
		number_clear_last.setOnClickListener(this);
		// 长按删除键
		number_clear_last.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				myCourse_roomId_input.setText("");
				return false;
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		JniThreadStop();
		if (dialog != null) {
			dialog.dismiss();
		}
		if (loadingDialog != null) {
			loadingDialog.dismiss();
		}
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

	// 发送出货命令
	private void sendShipmentCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (goodsPositions.size() > 0) {
					shipmentCount = 0;
					for (GoodsPosition goodsPosition : goodsPositions) {
						cycleCount = 0;
						while (true) {
							if (MachineSateCode == 1) {
								break;
							}
							if (MachineSateCode == 2) {
								break;
							}
							if(MachineSateCode == 11) {
								str2voice("机器维护，请稍后再来");
								startActivity(new Intent(quhuoActitvty.this, SplashActivity.class));
								return;
							}
							// 判断机器此时的状态
							mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd,
									ConstantCmd.getMachineStateCmd.length);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (cycleCount++ > 120) {
								str2voice("出货失败，请联系客服处理");
								break;
							}
						}

						int columnNum = goodsPosition.getColumnNum();
						int rowNum = goodsPosition.getRowNum();
						byte[] command = CommandPackage.getRequestShipment(ConstantCmd.get_request_shipment_cmd, rowNum,
								columnNum);
						// 延时100毫秒
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// TODO 正在出货 发送下一个出货命令的时候 要注意询问机器此时的状态；
						mUartNative.UartWriteCmd(command, command.length);
						// 判断机器此时的状态
						cycleCount = 0;
						shipmentCount++;
						MachineSateCode = 0;
						// 延时100毫秒
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					cycleCount = 0;
					while (MachineSateCode != 1) {
						// 判断机器此时的状态
						mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd, ConstantCmd.getMachineStateCmd.length);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (shipmentCount == goodsPositions.size() && MachineSateCode == 0x01) {
							break;
						}
						if (cycleCount++ > 120) {
							break;
						}
					}
					minusInventory();
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							loadingDialog.dismiss();
						}
					});
					str2voice("出货完成，感谢您的本次购物");
					ActivityManager.getInstance().addActivity(quhuoActitvty.this);
					ActivityManager.getInstance().finshAllActivity();
					startActivity(new Intent(quhuoActitvty.this, SplashActivity.class));
				}
			}
		});
	}

	private void minusInventory() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (ono != null) {
					String url = "http://linliny.com/dingyifeng_web/shanchukuncuntion.json?model=&request=&MiD="
							+ utils.Util.getMid() + "&Ono=" + ono;
					Log.e("url", url);
					HttpUtils httpUtils = new HttpUtils();
					try {
						httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

							@Override
							public void onFailure(HttpException arg0, String arg1) {
								Log.e("onFailure", "onFailure");
							}

							@Override
							public void onSuccess(ResponseInfo<String> arg0) {
								Log.e("success", "success");
							}
						});
					} catch (Exception e) {
						try {
							httpUtils.sendSync(HttpMethod.GET, url);
						} catch (HttpException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
	}

	protected void str2voice(final String string) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(string)) {
					VoiceUtils.getInstance().initmTts(string);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	private void showProgressDialog() {
		dialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
		dialog.getWindow().setDimAmount(0.3f);// 设置昏暗度为0
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		dialog.getWindow().setContentView(R.layout.loading_dialog_dialog);
	}

	protected void parseMachineStateCode(final byte cmd, final byte arg1) {
		switch (cmd) {
		// 待机状态
		case 0x01:
			MachineSateCode = 1;
			break;
		// 待机转动状态
		case 0x02:
			MachineSateCode = 2;
			break;
		// 正在定位
		case 0x03:
			MachineSateCode = 3;
			break;
		// 正在开门
		case 0x04:
			if (MachineSateCode != 4) {
				str2voice("正在准备出货，请稍等");
			}
			MachineSateCode = 4;
			break;
		// 等待取物状态
		case 0x05:
			if (MachineSateCode != 5) {
				str2voice("出货成功，请您注意取货");
			}
			MachineSateCode = 5;
			break;
		// 正在关门
		case 0x06:
			if (MachineSateCode != 6) {
				str2voice("正在关门，请您注意安全，防止夹手");
			}
			MachineSateCode = 6;
			break;
		// 补货状态
		case 0x07:
			MachineSateCode = 7;
			break;
		// 测试动作状态
		case 0x08:
			MachineSateCode = 8;
			break;
		// 故障状态
		case 0x09:
			MachineSateCode = 9;
			// TODO 此处提醒用户此时机器处于故障状态；不能出货；
			if (MachineSateCode != 9) {
				str2voice("机器故障，出货失败");
			}
			// 我们需要分析此时的故障的状态，然后上报服务器
			parseMachineFault(arg1);
			break;
		// 还篮子状态
		case 0x10:
			MachineSateCode = 10;
			break;
		// 还篮子状态
		case 0x11:
			MachineSateCode = 11;
			break;
		default:
			break;
		}
	}

	protected void parseMachineFault(byte b) {
		switch (b) {
		// 定位故障(主电机或位置光电开关故障）
		case 0x01:

			break;
		// 开门故障
		case 0x02:

			break;

		// 关门故障
		case 0x03:

			break;
		// 防夹传感器故障
		case 0x04:

			break;
		// 制冷故障
		case 0x05:

			break;
		// 加湿故障（缺水或长期湿度达不到设定值）
		case 0x06:

			break;
		// 其他故障
		case 0x07:

			break;
		// 主电机故障
		case 0x08:

			break;
		default:
			break;
		}
	}

	@Override
	public void changeTvTime(int time) {

	}

	private void httpGetFail() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(mContext, "网络错误, 减库存失败", Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public void onClick(View v) {
		String roomInput = myCourse_roomId_input.getText().toString();
		switch (v.getId()) {
		case R.id.number_1:
			myCourse_roomId_input.setText(roomInput + number_1.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_2:
			myCourse_roomId_input.setText(roomInput + number_2.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_3:
			myCourse_roomId_input.setText(roomInput + number_3.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_4:
			myCourse_roomId_input.setText(roomInput + number_4.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_5:
			myCourse_roomId_input.setText(roomInput + number_5.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_6:
			myCourse_roomId_input.setText(roomInput + number_6.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_7:
			myCourse_roomId_input.setText(roomInput + number_7.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_8:
			myCourse_roomId_input.setText(roomInput + number_8.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_9:
			myCourse_roomId_input.setText(roomInput + number_9.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_0:
			myCourse_roomId_input.setText(roomInput + number_0.getText().toString());
			myCourse_roomId_input.setSelection(roomInput.length() + 1);
			break;
		case R.id.number_enter:
			myCourse_roomId_input.setText("");
			break;
		case R.id.number_clear_last:
			if (roomInput.length() > 0) {
				myCourse_roomId_input.setText(roomInput.substring(0, roomInput.length() - 1));
				myCourse_roomId_input.setSelection(roomInput.length() - 1);
			}
			break;
		case R.id.btn_confirm:
			if (!TextUtils.isEmpty(utils.Util.getMid()) && !TextUtils.isEmpty(roomInput)) {
				showProgressDialog();
				sendTakeGoodCode(roomInput);
			} else {
				str2voice("请输入您的取货码");
				utils.Util.DisplayToast(mContext, "请输入您的取货码", R.drawable.warning);
			}
			break;
		case R.id.btn_cancel:
			quhuoActitvty.this.finish();
			break;
		default:
			break;
		}
	}

	private void sendTakeGoodCode(String roomInput) {
		final String url = "http://linliny.com/checkFCode.json?carryFruitcode=" + roomInput + "&mid="
				+ utils.Util.getMid();
		Log.e("url", url);
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				httpGetFail();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				if (!TextUtils.isEmpty(arg0.result)) {
					Log.e("TAKE_GOODS_CODE", arg0.result);
					utils.Util.sendMessage(handler, TAKE_GOODS_CODE, arg0.result);
				}
			}
		});
	}

	@SuppressLint("NewApi")
	private void showLoadingDialog() {
		if (mContext != null) {
			loadingDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
			loadingDialog.getWindow().setDimAmount(0.3f);// 设置昏暗度为0
			loadingDialog.setCanceledOnTouchOutside(false);
			loadingDialog.show();
			loadingDialog.getWindow().setContentView(R.layout.loading_dialog);
		}
	}

	@Override
	public void onActivityDectory() {
		quhuoActitvty.this.finish();
		startActivity(new Intent(quhuoActitvty.this, SplashActivity.class));
	}

}
