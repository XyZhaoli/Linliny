package activity;

import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.thirdparty.i;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.bb;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.GoodsPosition;
import uartJni.Uartjni;
import utils.ActivityManager;
import utils.CommandPackage;
import utils.VoiceUtils;

public class quhuoActitvty extends BaseAcitivity {
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
	private Button btnConfirm;
	private Button btnCancel;
	private MyHandler handler;
	private static final int TAKE_GOODS_CODE = 0;
	private Uartjni mUartNative;
	private static int MachineSateCode;
	private int cycleCount;
	private List<GoodsPosition> goodsPositions;
	private static final byte[] getMachineStateCmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
	private String ono;
	private Context mContext = quhuoActitvty.this;
	private AlertDialog dialog;

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
					VoiceUtils.getInstance().initmTts(mContext, "准备出货，请稍等");
					// 发送出货命令
					sendShipmentCmd();
				}
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
		showNumberKeyboard();
		initView();
		initData();
		initSerial();
	}

	public void parseFaultCode(String string) {
		if (utils.Util.parseJson(string, "checkFCode") == 0) {
			utils.Util.DisplayToast(mContext, "取货码错误", R.drawable.warning);
			VoiceUtils.getInstance().initmTts(mContext, "您输入的取货码有误");
		} else if (utils.Util.parseJson(string, "checkOrder") == 0) {
			utils.Util.DisplayToast(mContext, "订单有误", R.drawable.warning);
			VoiceUtils.getInstance().initmTts(mContext, "订单不存在");
		} else if (utils.Util.parseJson(string, "checkMid") == 0) {
			utils.Util.DisplayToast(mContext, "不是该机器设备", R.drawable.warning);
			VoiceUtils.getInstance().initmTts(mContext, "提货的机器错误");
		} else if (utils.Util.parseJson(string, "orderDetailed") == 0) {
			utils.Util.DisplayToast(mContext, "订单有误", R.drawable.warning);
			VoiceUtils.getInstance().initmTts(mContext, "订单有误");
		}
	}

	public void parseTakeGoodsCode(String str) {
		try {
			JSONObject jsonObject = new JSONObject(str);
			JSONArray js = jsonObject.getJSONArray("returnData");
			String string = js.getString(0);
			Map<String, Object> map = getMap(string);
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
			sendShipmentCmd();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static Map<String, Object> getMap(String jsonString) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jsonString);
			@SuppressWarnings("unchecked")
			Iterator<String> keyIter = jsonObject.keys();
			String key;
			Object value;
			Map<String, Object> valueMap = new HashMap<String, Object>();
			while (keyIter.hasNext()) {
				key = (String) keyIter.next();
				value = jsonObject.get(key);
				valueMap.put(key, value);
			}
			return valueMap;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void initData() {
		ActivityManager.getInstance().addActivity(quhuoActitvty.this);
		goodsPositions = new ArrayList<GoodsPosition>();
	}

	private void initSerial() {
		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] arg1) {
				if (arg1.length >= 4) {
					switch (arg1[2]) {
					// 成功
					case 0:
						str2voice("机器马上准备出货了，请稍等");
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
		EditText quhuo = (EditText) findViewById(R.id.editText1);
		quhuo.setInputType(InputType.TYPE_NULL);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mUartNative != null) {
			mUartNative.NativeThreadStop();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mUartNative != null) {
			mUartNative.NativeThreadStop();
		}
	}

	// 发送出货命令
	private void sendShipmentCmd() {
		new Thread() {
			public void run() {
				if (goodsPositions.size() > 0) {
					for (GoodsPosition goodsPosition : goodsPositions) {
						cycleCount = 0;
						while (MachineSateCode != 1) {
							// 判断机器此时的状态
							mUartNative.UartWriteCmd(getMachineStateCmd, getMachineStateCmd.length);
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							if (cycleCount++ > 60) {
								str2voice("机器正忙，付款失败，请您稍后再试");
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
						MachineSateCode = 0;
						// 延时100毫秒
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							str2voice("感谢您的本次购物,欢迎下次再来哦");
							minusInventory();
						}
					});
					cycleCount = 0;
					while (MachineSateCode != 1) {
						// 判断机器此时的状态
						mUartNative.UartWriteCmd(getMachineStateCmd, getMachineStateCmd.length);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (cycleCount++ > 60) {
							str2voice("机器正忙，付款失败，请您稍后再试");
							return;
						}
					}
					ActivityManager.getInstance().addActivity(quhuoActitvty.this);
					ActivityManager.getInstance().finshAllActivity();
					startActivity(new Intent(quhuoActitvty.this, SplashActivity.class));
				}
			};
		}.start();
	}

	private void minusInventory() {
		new Thread() {
			public void run() {
				if (ono != null) {
					String url = "http://linliny.com/dingyifeng_web/shanchukuncuntion.json?model=&request=&MiD="
							+ utils.Util.getMid() + "&Ono=" + ono;
					String httpResult;
					try {
						httpResult = bb.getHttpResult(url);
					} catch (ConnectTimeoutException e) {
						try {
							httpResult = bb.getHttpResult(url);
						} catch (ConnectTimeoutException e1) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(mContext, "网络错误, 减库存失败", Toast.LENGTH_LONG).show();
								}
							});
						}
					}
				}
			};
		}.start();
	}

	// 显示数字键盘
	public void showNumberKeyboard() {
		// 数字键盘点击监听
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
		number_enter = (TextView) findViewById(R.id.number_enter);
		number_clear_last = (ImageView) findViewById(R.id.number_clear_last);
		myCourse_roomId_input = (EditText) findViewById(R.id.editText1);

		number_1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_1.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_2.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_3.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_4.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_5.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_6.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_7.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_7.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_8.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_8.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_9.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				myCourse_roomId_input.setText(roomInput + number_9.getText().toString());
				myCourse_roomId_input.setSelection(roomInput.length() + 1);
			}
		});
		number_0.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomInput = myCourse_roomId_input.getText().toString();
				// 0不能在第一位
				if (null != roomInput && !"".equals(roomInput)) {
					myCourse_roomId_input.setText(roomInput + number_0.getText().toString());
					myCourse_roomId_input.setSelection(roomInput.length() + 1);
				}
			}
		});
		number_enter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				myCourse_roomId_input.setText("");
			}
		});
		number_clear_last.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String roomIdInput = myCourse_roomId_input.getText().toString();
				if (roomIdInput.length() > 0) {
					myCourse_roomId_input.setText(roomIdInput.substring(0, roomIdInput.length() - 1));
					myCourse_roomId_input.setSelection(roomIdInput.length() - 1);
				}
			}
		});
		// 长按删除键
		number_clear_last.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				myCourse_roomId_input.setText("");
				return false;
			}
		});
		// 完成
		btnConfirm = (Button) findViewById(R.id.btn_confirm);
		btnConfirm.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				
				final String roomId = myCourse_roomId_input.getText().toString();
				if (!TextUtils.isEmpty(utils.Util.getMid()) && !TextUtils.isEmpty(roomId)) {
					final String url = "http://linliny.com/dingyifeng_web/checkFCode.json?carryFruitcode=" + roomId
							+ "&mid=" + utils.Util.getMid();
					showProgressDialog();
					new Thread() {
						public void run() {
							String res;
							try {
								res = bb.getHttpResult(url);
								if (!TextUtils.isEmpty(res)) {
									utils.Util.sendMessage(handler, TAKE_GOODS_CODE, res);
								} else {
									runOnUiThread(new Runnable() {
										
										@Override
										public void run() {
											utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
											VoiceUtils.getInstance().initmTts(mContext, "网络错误，请重试");
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
				} else {
					VoiceUtils.getInstance().initmTts(mContext, "请输入您的取货码");
					utils.Util.DisplayToast(mContext, "请输入您的取货码", R.drawable.warning);
				}
			}
		});
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				quhuoActitvty.this.finish();
			}
		});
	}

	protected void str2voice(String string) {
		VoiceUtils.getInstance().initmTts(getApplicationContext(), string);
	}

	
	private void showProgressDialog() {
		dialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
		dialog.getWindow().setDimAmount(0.3f);//设置昏暗度为0
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
				str2voice("正在开门，请注意安全");
			}
			MachineSateCode = 4;
			break;
		// 等待取物状态
		case 0x05:
			if (MachineSateCode != 5) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							str2voice("出货成功，请您注意取货");
						} catch (Exception e) {

						}
					}
				});
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
		// TODO Auto-generated method stub

	}

}
