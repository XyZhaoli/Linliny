package activity;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.sample.R;
import domain.GoodsPosition;
import domain.MachineState;
import uartJni.Uartjni;
import utils.ApManager;
import utils.Util;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.IOException;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

public class UartActivity extends Activity implements View.OnClickListener {

	private final static String TAG = "UartActivity";

	private static final int GET_TEM_CMD = 1;
	private static final int GET_HUM_CMD = 2;
	private static final int GET_SETUP_ROTATE_TIME = 3;
	private static final int GET_SETUP_LOCAL_PARAM = 4;
	private static final int GET_SETUP_PARAM = 5;
	private static final int GET_SETUP_PULSE = 6;

	private Uartjni mUartNative = null;
	private Context mContext;

	private TextView tv_tem;
	private TextView tv_hum;
	private TextView tv_machine_state;
	private TextView tv_machine_fault_state;
	private TextView tv_local;
	private TextView tv_current_set_tem;
	private TextView tv_current_set_hum;
	private TextView tv_machine_current_set_rotate_time;
	private TextView tv_machine_current_setup_param;
	private TextView tv_local_current_set;
	private TextView tv_current_set_pulse;
	private TextView tv_devices_num;

	private EditText et_setup_tem;
	private EditText et_setup_hum;
	private EditText et_setup_local_param;
	private EditText et_setup_param;
	private EditText et_setup_pulse;
	private EditText et_setup_rotate_time;
	private EditText et_setup_device_num;
	private EditText et_show_cmd_str;

	private Button bt_setup_tem;
	private Button bt_setup_hum;
	private Button bt_setup_local_param;
	private Button bt_setup_param;
	private Button bt_setup_pulse;
	private Button bt_replenishment;
	private Button bt_replenishment_finish;
	private Button bt_setup_rotate_time;
	private Button bt_clear_basket;
	private Button bt_refresh_param;
	private Button bt_return_luncher;
	private Button bt_setup_device_num;
	private Button bt_restart;
	private Button bt_open_wifi;

	private MachineState machineState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initData();
		initSerial();
		initCmd();
	}

	private void initCmd() {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				sendGetMachineStateCmd();
			};
		}.start();
	}

	private void sendGetMachineParam() {
		byte[] cmd = new byte[] { 0x02, 0x03, 0x51, 0x56 };
		sendCmd(cmd, cmd.length);
	}

	private void sendGetMachineStateCmd() {
		byte[] cmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
		sendCmd(cmd, cmd.length);
	}

	private void initData() {
		mContext = UartActivity.this;
		tv_devices_num.setText(Util.getMid());
	}

	private void initView() {
		tv_tem = (TextView) findViewById(R.id.tv_tem);
		tv_devices_num = (TextView) findViewById(R.id.tv_devices_num);
		tv_hum = (TextView) findViewById(R.id.tv_hum);
		tv_machine_state = (TextView) findViewById(R.id.tv_machine_state);
		tv_machine_fault_state = (TextView) findViewById(R.id.tv_machine_fault_state);
		tv_local = (TextView) findViewById(R.id.tv_local);
		tv_current_set_tem = (TextView) findViewById(R.id.tv_current_set_tem);
		tv_current_set_hum = (TextView) findViewById(R.id.tv_current_set_hum);
		tv_machine_current_set_rotate_time = (TextView) findViewById(R.id.tv_machine_current_set_rotate_time);
		tv_machine_current_setup_param = (TextView) findViewById(R.id.tv_machine_current_setup_param);
		tv_local_current_set = (TextView) findViewById(R.id.tv_local_current_set);
		tv_current_set_pulse = (TextView) findViewById(R.id.tv_current_set_pulse);

		et_setup_tem = (EditText) findViewById(R.id.et_setup_tem);
		et_setup_hum = (EditText) findViewById(R.id.et_setup_hum);
		et_setup_local_param = (EditText) findViewById(R.id.et_setup_local_param);
		et_setup_param = (EditText) findViewById(R.id.et_setup_param);
		et_setup_pulse = (EditText) findViewById(R.id.et_setup_pulse);
		et_setup_rotate_time = (EditText) findViewById(R.id.et_setup_rotate_time);
		et_setup_device_num = (EditText) findViewById(R.id.et_setup_device_num);
		et_show_cmd_str = (EditText) findViewById(R.id.et_show_cmd_str);
		et_show_cmd_str.setFocusableInTouchMode(false);
		bt_setup_tem = (Button) findViewById(R.id.bt_setup_tem);
		bt_setup_hum = (Button) findViewById(R.id.bt_setup_hum);
		bt_setup_local_param = (Button) findViewById(R.id.bt_setup_local_param);
		bt_setup_param = (Button) findViewById(R.id.bt_setup_param);
		bt_setup_pulse = (Button) findViewById(R.id.bt_setup_pulse);
		bt_replenishment = (Button) findViewById(R.id.bt_replenishment);
		bt_replenishment_finish = (Button) findViewById(R.id.bt_replenishment_finish);
		bt_setup_rotate_time = (Button) findViewById(R.id.bt_setup_rotate_time);
		bt_refresh_param = (Button) findViewById(R.id.bt_refresh_param);
		bt_setup_device_num = (Button) findViewById(R.id.bt_setup_device_num);
		bt_clear_basket = (Button) findViewById(R.id.bt_clear_basket);
		bt_return_luncher = (Button) findViewById(R.id.bt_return_luncher);
		bt_restart = (Button) findViewById(R.id.bt_restart);
		bt_open_wifi = (Button) findViewById(R.id.bt_open_wifi);

		findViewById(R.id.bt_back_desktop).setOnClickListener(this);

		bt_restart.setOnClickListener(this);
		bt_open_wifi.setOnClickListener(this);
		bt_setup_tem.setOnClickListener(this);
		bt_setup_hum.setOnClickListener(this);
		bt_setup_local_param.setOnClickListener(this);
		bt_setup_param.setOnClickListener(this);
		bt_setup_pulse.setOnClickListener(this);
		bt_replenishment.setOnClickListener(this);
		bt_replenishment_finish.setOnClickListener(this);
		bt_setup_rotate_time.setOnClickListener(this);
		bt_refresh_param.setOnClickListener(this);
		bt_setup_device_num.setOnClickListener(this);
		bt_clear_basket.setOnClickListener(this);
		bt_return_luncher.setOnClickListener(this);
	}

	private void initSerial() {
		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] cmd) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						et_show_cmd_str.append("接收的命令:" + byteToHexstring(cmd, cmd.length) + "\n");
					}
				});
				switch (cmd[2]) {
				case 0x10:
					parseMachineState(cmd);
					sendGetMachineParam();
					break;
				case 0x51:
					parseMachineParam(cmd);
					break;
				default:
					break;
				}
			}
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	protected void parseMachineParam(final byte[] cmd) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				float tem = getParam(cmd[3], cmd[4]) / 10;
				int hum = (int) getParam(cmd[5], cmd[6]) / 10;
				int rotateTime = (int) getParam(cmd[7], cmd[8]);
				int param = (int) getParam(cmd[9], cmd[10]);
				int local = (int) getParam(cmd[11], cmd[12]);
				int pulse = (int) getParam(cmd[13], cmd[14]);
				tv_current_set_tem.setText(tem + "℃");
				tv_current_set_hum.setText(hum + "%Rh");
				tv_machine_current_set_rotate_time.setText(rotateTime + "");
				tv_machine_current_setup_param.setText(param + "");
				tv_local_current_set.setText(local + "");
				tv_current_set_pulse.setText(pulse + "");
			}
		});
	}

	private float getParam(int high, int low) {
		return ((high & 0xff) << 8 | (low & 0xff));
	}

	private void setViewData() {
		// TODO
		tv_tem.setText(machineState.getTemper() + "℃");
		tv_hum.setText(machineState.getHumidity() + "%Rh");
		tv_machine_state.setText(Util.parseMachineStateCode(machineState.getMachineStateCode()));

	}

	private void sendCmd(final byte[] cmd, final int length) {
		synchronized (UartActivity.this) {
			if (cmd != null && length > 0) {
				new Thread() {
					public void run() {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mUartNative.UartWriteCmd(cmd, length);
					};
				}.start();
			}
		}
	}

	private void refreshMachineState() {
		initCmd();
		tv_devices_num.setText(Util.getMid());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mUartNative != null) {
			mUartNative.NativeThreadStop();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_setup_tem:
			String temStr = et_setup_tem.getText().toString();
			if (!TextUtils.isEmpty(temStr)) {
				parseTemStrWithSendCmd(GET_TEM_CMD, temStr);
			}
			break;

		case R.id.bt_setup_hum:
			String humStr = et_setup_hum.getText().toString();
			if (!TextUtils.isEmpty(humStr)) {
				parseTemStrWithSendCmd(GET_HUM_CMD, humStr);
			}
			break;

		case R.id.bt_setup_rotate_time:
			String timeStr = et_setup_rotate_time.getText().toString();
			if (!TextUtils.isEmpty(timeStr)) {
				parseTemStrWithSendCmd(GET_SETUP_ROTATE_TIME, timeStr);
			}
			break;

		case R.id.bt_setup_local_param:
			String paramStr = et_setup_local_param.getText().toString();
			if (!TextUtils.isEmpty(paramStr)) {
				parseTemStrWithSendCmd(GET_SETUP_PARAM, paramStr);
			}
			break;
		case R.id.bt_setup_param:
			String paraStr = et_setup_param.getText().toString();
			if (!TextUtils.isEmpty(paraStr)) {
				parseTemStrWithSendCmd(GET_SETUP_LOCAL_PARAM, paraStr);
			}
			break;
		case R.id.bt_setup_pulse:
			String pulseStr = et_setup_pulse.getText().toString();
			if (!TextUtils.isEmpty(pulseStr)) {
				parseTemStrWithSendCmd(GET_SETUP_PULSE, pulseStr);
			}
			break;
		case R.id.bt_replenishment:
			byte[] replenismentCmd = new byte[] { 0x02, 0x03, 0x30, 0x35 };
			mUartNative.UartWriteCmd(replenismentCmd, replenismentCmd.length);
			break;
		case R.id.bt_replenishment_finish:
			byte[] replenismentFinshCmd = new byte[] { 0x02, 0x03, 0x31, 0x36 };
			mUartNative.UartWriteCmd(replenismentFinshCmd, replenismentFinshCmd.length);
			break;
		case R.id.bt_refresh_param:
			refreshMachineState();
			break;
		case R.id.bt_setup_device_num:
			setupDeviceNum();
			break;
		case R.id.bt_clear_basket:
			clearBasket();
			break;
		case R.id.bt_return_luncher:
			startActivity(new Intent(UartActivity.this, SplashActivity.class));
			UartActivity.this.finish();
			break;
		case R.id.bt_restart:
			reboot(getApplicationContext());
			break;
		case R.id.bt_open_wifi:
			boolean openAp = ApManager.openAp(mContext, "邻里农园鲜果", "llny.4008270755#");
			if (openAp) {
				Toast.makeText(mContext, "打开成功", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(mContext, "打开失败", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.bt_back_desktop:
			startActivity(new Intent(Settings.ACTION_SETTINGS));
			break;
		default:
			break;
		}
	}

	public void reboot(Context context) {
		try {
			Runtime.getRuntime().exec("su -c reboot");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setupDeviceNum() {
		String devicesNumStr = et_setup_device_num.getText().toString().trim();
		if (!TextUtils.isEmpty(devicesNumStr)) {
			setSharePerferenceStr(devicesNumStr);
		} else {
			Toast.makeText(mContext, "输入的设备号的不能为空", Toast.LENGTH_LONG).show();
		}
	}

	private void setSharePerferenceStr(String devicesNumStr) {
		SharedPreferences preferences = mContext.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
		preferences.edit().putString("Mid", devicesNumStr).commit();
	}

	private String getSharePerferenceStr(String key) {
		SharedPreferences preferences = mContext.getSharedPreferences("devicesInfo", Context.MODE_PRIVATE);
		return preferences.getString(key, "0");
	}

	private void clearBasket() {
		AlertDialog alertDialog = new AlertDialog.Builder(UartActivity.this).setMessage("确认清空篮子吗？")
				.setPositiveButton("确认", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread() {
							public void run() {
								if (!Util.getMid().equals("0")) {
									String url = "http://linliny.com/dingyifeng_web/updateGridState.json?mid="
											+ Util.getMid();
									HttpUtils httpUtils = new HttpUtils();
									httpUtils.configCurrentHttpCacheExpiry(0);
									httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {
										@Override
										public void onFailure(HttpException arg0, String arg1) {
											showAlertDialog("清空篮子失败,请重试");
										}

										@Override
										public void onSuccess(final ResponseInfo<String> arg0) {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													int res = Util.parseJson(arg0.result, "checkGrid");
													if (res == 1) {
														showAlertDialog("清空篮子成功");
													} else if(res == 0){
														showAlertDialog("清空篮子失败,机器格子未满");
													}
												}
											});
										}
									});
								} else {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(mContext, "请输入机器设备号", Toast.LENGTH_LONG).show();
										}
									});
								}
							};
						}.start();
						dialog.dismiss();
					}
				}).setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).create();
		alertDialog.show();
	}

	private void parseTemStrWithSendCmd(int flag, String temStr) {
		synchronized (UartActivity.this) {
			if (!TextUtils.isEmpty(temStr)) {
				Integer value = Integer.valueOf(temStr);
				String valueStr = "";
				Integer setupValue = 0;
				switch (flag) {
				case GET_TEM_CMD:
					valueStr = String.valueOf(value * 10);
					setupValue = Integer.valueOf(valueStr);
					break;
				case GET_HUM_CMD:
					valueStr = String.valueOf(value * 10);
					setupValue = Integer.valueOf(valueStr);
					break;
				case GET_SETUP_ROTATE_TIME:
					valueStr = String.valueOf(value);
					setupValue = Integer.valueOf(valueStr);
				case GET_SETUP_LOCAL_PARAM:
					valueStr = String.valueOf(value);
					setupValue = Integer.valueOf(valueStr);
				case GET_SETUP_PARAM:
					valueStr = String.valueOf(value);
					setupValue = Integer.valueOf(valueStr);
				case GET_SETUP_PULSE:
					valueStr = String.valueOf(value);
					setupValue = Integer.valueOf(valueStr);
					break;
				default:
					valueStr = String.valueOf(value);
					setupValue = Integer.valueOf(valueStr);
					break;
				}
				try {
					int valueH = (setupValue >> 8) & 0xff;
					int valueL = (setupValue & 0xff);
					byte[] CMD = getSetupTemCmd(flag, valueH, valueL);
					sendCmd(CMD, CMD.length);
				} catch (Exception e) {
					// 输入的整数的摄氏度
					e.printStackTrace();
				}
			}
		}
	}

	private void showAlertDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("提示");
		builder.setMessage(message);
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	private byte[] getSetupTemCmd(int flag, int temperatureH, int temperatureL) {
		int high = temperatureH;
		int low = temperatureL;
		byte[] cmd = new byte[7];
		cmd[0] = 0x02;
		cmd[1] = 0x06;
		cmd[2] = 0x50;
		switch (flag) {
		case GET_TEM_CMD:
			cmd[3] = 0x01;
			break;
		case GET_HUM_CMD:
			cmd[3] = 0x02;
			break;
		case GET_SETUP_ROTATE_TIME:
			cmd[3] = 0x03;
			break;
		case GET_SETUP_PARAM:
			cmd[3] = 0x04;
			break;
		case GET_SETUP_LOCAL_PARAM:
			cmd[3] = 0x05;
			break;
		case GET_SETUP_PULSE:
			cmd[3] = 0x06;
			break;
		default:
			break;
		}
		cmd[4] = (byte) high;
		cmd[5] = (byte) low;
		cmd[6] = getFCC(cmd);
		return cmd;
	}

	private void parseMachineState(byte[] obj) {
		// 防止数组出现越界的异常
		try {
			// 机器状态码
			int machineStateCode = obj[3];
			// 机器故障码
			int machineMalfunctionCode = obj[4];
			// 温度值
			int temH = obj[5] & 0xff;
			int temL = obj[6] & 0xff;
			float temper = ((float) ((temH << 8) | temL) / 10);
			// 湿度值
			int humidityH = obj[7] & 0xff;
			int humidityL = obj[8] & 0xff;
			float humidity = ((float) ((humidityH << 8) | humidityL) / 10);
			// 当前位置
			GoodsPosition goodsPosition = new GoodsPosition(obj[9], obj[10]);
			machineState = new MachineState(machineStateCode, machineMalfunctionCode, temper, humidity, goodsPosition);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setViewData();
				}
			});
		} catch (Exception e) {
		}
	}

	private byte getFCC(byte[] cmd) {
		byte fcc = 0;
		for (int i = 0; i < cmd.length - 1; i++) {
			fcc += cmd[i];
		}
		return fcc;
	}

	public String parseMachineFaultCode(int machineMalfunctionCode) {
		/**
		 * 0X01:定位故障(主电机或位置光电开关故障)； 0X02:开门故障； 0X03:关门故障； 0X04:防夹传感器故障； 0X05:制冷故障；
		 * 0X06:加湿故障（缺水或长期湿度达不到设定值）； 0X07:其他故障； 0X08:主电机故障。
		 */
		String faultCodeStr = "";
		switch (machineMalfunctionCode) {
		case 0X01:
			faultCodeStr = "定位故障(主电机或位置光电开关故障)";
			break;
		case 0X02:
			faultCodeStr = "开门故障";
			break;
		case 0X03:
			faultCodeStr = "关门故障";
			break;
		case 0X04:
			faultCodeStr = "防夹传感器故障";
			break;
		case 0X05:
			faultCodeStr = "制冷故障";
			break;
		case 0X06:
			faultCodeStr = "加湿故障(缺水或长期湿度达不到设定值)";
			break;
		case 0X07:
			faultCodeStr = "其他故障";
			break;
		case 0X08:
			faultCodeStr = "主电机故障";
			break;
		default:
			faultCodeStr = "正常运行";
			break;
		}
		return faultCodeStr;
	}

	@SuppressLint("DefaultLocale")
	public static String byteToHexstring(byte[] buff, int length) {
		String HexString = "";
		for (int i = 0; i < length; i++) {
			String hex = Integer.toHexString(buff[i] & 0xff);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			HexString += hex.toUpperCase() + " ";
		}
		return HexString;
	}

}
