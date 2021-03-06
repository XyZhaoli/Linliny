package dialog;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.orhanobut.logger.Logger;

import activity.BaseActivity;
import activity.BasketMainActitvty;
import activity.SplashActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.GoodsPosition;
import uartJni.UartJniCard;
import uartJni.Uartjni;
import utils.CommandPackage;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;

public class ByPhoneNumReturnBasketDialog extends Dialog implements android.view.View.OnClickListener {
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
	private Uartjni mUartNative;
	private UartJniCard mUartNativeCard = null;
	private static final int GET_MACHINE_STATE = 0;
	private static final int CHECK_PHONE_IS_MEMBERSHIP = 1;
	private static final int GET_BASKET_LOCATION = 2;
	private static final int CHECK_BASKET_RFID_CODE = 3;
	private static final int RETURN_BASKET_SUCCESS = 5;
	private static final int GET_BASKET_CODE = 7;
	private static final int RETURN_BASKET_FAIL = 6;
	private Context mContext;
	private MyHandler handler = new MyHandler();
	private String mid;
	private EditText myCourse_roomId_input;
	private String phoneNum;
	private int gid;
	private Dialog alertDialog;
	private int MachineSateCode = 0;
	private byte[] tempCardCmd = new byte[64];
	private int sum = 0;
	// 是否完成还篮子，不管失败或者成功；
	private boolean isComplteWork = false;
	private StringBuffer basketCode;
	private StringBuffer serialCode;

	private SoundPool soundPool; // 播放声音是使用的声音池
	private HashMap<Integer, Integer> spMap;

	// 记录发送查询还篮子命令的次数，如果用户一开始查询就得到上一次未读到的还篮子成功的命令，那么不认为此次还篮子是成功的;
	private static int sendReturnBasketCmdCount = 0;

	public ByPhoneNumReturnBasketDialog(Context context) {
		super(context);
	}

	public ByPhoneNumReturnBasketDialog(Context context, int theme) {
		super(context, theme);
		this.mContext = context;
	}

	public ByPhoneNumReturnBasketDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basket_shouji);
		initView();
		initData();
		showNumberKeyboard();
		initSerialJni();
		initSoundPool();
	}

	/**
	 * 函数说明：初始化关于声音操作的函数
	 */
	@SuppressLint("UseSparseArrays")
	public void initSoundPool() {
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		spMap = new HashMap<Integer, Integer>();
		spMap.put(1, soundPool.load("/system/media/audio/alarms/Alarm_Beep_02.ogg", 1));
	}

	/**
	 * 函数说明：播放声音
	 * 
	 * @param sound
	 *            播放声音文件的序号
	 * @param num
	 *            循环的次数
	 */
	public void playSound(int sound, int num) {
		if (soundPool != null) {
			soundPool.play(spMap.get(sound), 1, 1, 1, num, 1);
		}
	}

	private void initData() {
		mid = utils.Util.getMid();
		basketCode = new StringBuffer();
		serialCode = new StringBuffer();
	}

	private void initView() {
		myCourse_roomId_input = (EditText) findViewById(R.id.editText1);
		myCourse_roomId_input.setInputType(InputType.TYPE_NULL);
		str2Voice("请输入手机号码");
	}

	private void initSerialJni() {
		// 初始化读取会员卡的串口
		mUartNativeCard = new UartJniCard() {
			@Override
			public void onCardNativeCallback(final byte[] cmd) {
				for (int i = 0; i < cmd.length; i++) {
					tempCardCmd[i + sum] = cmd[i];
				}
				sum += cmd.length;
				if (isHaveFullCmd(tempCardCmd, sum)) {
					playSound(1, 0);
					byte[] fullCmd = getFullCmd(tempCardCmd, sum);
					getCardCodeWithToServer(fullCmd);
					clearArray(tempCardCmd);
				}
			}
		};
		mUartNativeCard.nativeCardInitilize();
		mUartNativeCard.BoardCardThreadStart();

		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(final byte[] cmd) {
				if (mContext != null) {
					((BasketMainActitvty) mContext).setTime(120);
				}
				// 5.开门以后，关门验证，验证篮子是否已经到位，到位以后发送服务器告知目前已经还篮子成功；
				switch (cmd[2]) {
				case 0:
					// TODO 此时表示发送还篮子命令成功 2018/12/20 还是将这里的检查还篮子情况移动到 我们一发送还篮子命令后，就开始检查还篮子成功与否的情况
					// sendGetMachineBasketCodeCmd();
					break;

				case (byte) 0xff:
					// 指令不完整
					break;
				case 0x10:
					// 这是查询机器状态的指令
					parseMachineStateCode(cmd[3]);
					break;

				case 0x71:
					// 获取到机器中格子中篮子的编码
					parseMachineBasketCmd(cmd);
					break;
				default:
					break;
				}
			}
		};
		mUartNative.nativeInitilize();
		mUartNative.BoardThreadStart();
	}

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_MACHINE_STATE:
				break;

			case CHECK_PHONE_IS_MEMBERSHIP:
				// 2.收到如果确认有此会员，弹窗提示请将篮子放置在感应区,然后我们的editText会获取到篮子的编码，然后得到篮子的编码
				checkResonse(msg.obj.toString());
				break;

			case GET_BASKET_LOCATION:
				break;

			case CHECK_BASKET_RFID_CODE:
				// 4.根据返回的验证篮子的信息，成功就发送开门信息，失败则弹窗提示
				// 发送还篮子指令
				// 成功获取到篮子的位置以后，我们给串口发送信息，开始还篮子
				int response = parseJson(msg.obj.toString(), "check");
				if (response == 1) {
					// 如果是我们的篮子，开始还篮子
					if (!TextUtils.isEmpty(basketCode.toString())) {
						// 如果basketCode不为空的话，说明是用户多次感应篮子的结果
						int length = basketCode.length();
						basketCode.delete(0, length);
					} else if (gid >= 1 && gid <= 16) {
						// 如果是用户第一次感应篮子,我们提示用户开门，发送还篮子命令，还篮子命令不能发送多次
						str2Voice("感应成功，请等候开门");
						sendReturnBasketCmd();
					} else if (gid == -1) {
						str2Voice("请您先前往商城注册会员");
					} else if (gid == 0) {
						str2Voice("机器格子不足,请您稍后再来");
					}
					basketCode = basketCode.append(serialCode);
				} else {
					if (!TextUtils.isEmpty(basketCode.toString())) {
						int length = basketCode.length();
						basketCode.delete(0, length);
					}
					str2Voice("篮子编码有误,请重试");
				}
				break;

			case GET_BASKET_CODE:
				// 这个时候还篮子成功，通知服务器可以进行退款及其后续的操作
				str2Voice("开始准备还篮子");
				break;

			case RETURN_BASKET_FAIL:
				// 这个时候还篮子失败，通知服务器
				Util.DisplayToast(mContext, "还篮子失败");
				str2Voice("还篮子失败，请将篮子取出，关门后再试");
				if (mContext != null) {
					isComplteWork = true;
					((Activity) mContext).startActivity(new Intent(mContext, SplashActivity.class));
				}
				break;
			case RETURN_BASKET_SUCCESS:
				// TODO 还篮子成功以后 通知服务器退款
				if (sendReturnBasketCmdCount > 10) {
					isComplteWork = true;
					returnBasketMoney();
				}
				break;

			default:
				break;
			}
		}
	}

	// 通过串口发送还篮子的指令
	public void sendReturnBasketCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (mUartNative == null) {
					str2Voice("机器维护，请稍后再试");
					if (mContext != null) {
						mContext.startActivity(new Intent(mContext, SplashActivity.class));
					}
				}
				int cycleCount = 0;
				// 查询机器状态
				while (true) {
					if (MachineSateCode == 1 || MachineSateCode == 2) {
						break;
					}
					if (MachineSateCode == 11) {
						str2Voice("机器正在调整位置，请您稍后再试");
						mContext.startActivity(new Intent(mContext, SplashActivity.class));
						return;
					}
					// 判断机器此时的状态
					if (mUartNative == null) {
						str2Voice("机器维护，请联系客服处理退款");
						if (mContext != null) {
							mContext.startActivity(new Intent(mContext, SplashActivity.class));
						}
						return;
					}
					mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd, ConstantCmd.getMachineStateCmd.length);
					Util.delay(1000);
					if (cycleCount++ > 120) {
						Logger.e("还篮子的时候查询机器状态，超过次数，还篮子失败");
						str2Voice("机器出错");
						return;
					}
				}
				// 延时100毫秒
				Util.delay(100);
				GoodsPosition basketPosition = new GoodsPosition(Integer.parseInt("0E", 16), gid);
				byte[] returnBasketCmd = CommandPackage.getRequestShipment(ConstantCmd.get_return_basket_cmd,
						basketPosition.getRowNum(), basketPosition.getColumnNum());
				mUartNative.UartWriteCmd(returnBasketCmd, returnBasketCmd.length);
				// 将检查还篮子成功与否，放在这里，防止出现通信失败的问题出现（check error）
				byte[] cmd = new byte[] { 0x02, 0x03, 0x71, 0x76 };
				sendReturnBasketCmdCount = 0;
				while (!isComplteWork) {
					Util.delay(500);
					sendReturnBasketCmdCount++;
					if (sendReturnBasketCmdCount > 300) {
						break;
					}
					if (mUartNative == null) {
						return;
					}
					mUartNative.UartWriteCmd(cmd, cmd.length);
				}
			}
		});
	}

	public void checkResonse(String string) {
		gid = parseJson(string, "gid");
		Logger.e(string);
		if (gid >= 1 && gid <= 16) {
			showAlertDialog(mContext, "提示");
			str2Voice("请您将篮子放置在感应区");
		} else if (gid == 0) {
			str2Voice("机器格子不足，请您稍后再来");
			Util.DisplayToast(mContext, "机器格子不足");
		} else if (gid == -1) {
			str2Voice("您还不是我们的会员，请您前往商城注册会员");
			Util.DisplayToast(mContext, "您还不是我们的会员，请您前往商城注册会员");
		}
	}

	public void returnBasketMoney() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(basketCode.toString())) {
					StringBuilder url = new StringBuilder(ConstantCmd.BASE_URLS).append("returnBasket.json?gid=")
							.append(gid).append("&phone=").append(phoneNum).append("&Frid=").append(basketCode)
							.append("&mid=").append(mid).append("&cardSerial=");
					HttpUtils httpUtils = new HttpUtils();
					httpUtils.configCurrentHttpCacheExpiry(0);
					httpUtils.configRequestRetryCount(20);
					try {
						httpUtils.send(HttpMethod.GET, url.toString(), new RequestCallBack<String>() {
							@Override
							public void onFailure(HttpException arg0, String arg1) {
								str2Voice("还篮子失败");
								Logger.e("还篮子退款接口，访问失败");
							}

							@Override
							public void onSuccess(ResponseInfo<String> arg0) {
								if (!TextUtils.isEmpty(arg0.result.toString())) {
									str2Voice("还篮子成功,请注意微信商城退款通知");
									Logger.e("还篮子success");
									if (mContext != null) {
										mContext.startActivity(new Intent(mContext, SplashActivity.class));
									}
								} else {
									Logger.e("还篮子返回值为空");
								}
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
						httpGetFail();
					}
				} else {
					str2Voice("还篮子失败,请联系客服处理");
					if (mContext != null) {
						mContext.startActivity(new Intent(mContext, SplashActivity.class));
					}
					Logger.e("BasketCode值为空");
				}
			}
		});
	}

	public int parseJson(String string, String key) {
		int value = -1;
		try {
			JSONObject object = new JSONObject(string);
			value = object.getInt(key);
		} catch (Exception e) {
			value = -1;
		}
		return value;
	}

	// 从服务器获取篮子的位置
	public void getBasketLocationFromServer(final String basketCodeStr) {
		if (!TextUtils.isEmpty(serialCode.toString())) {
			int length = serialCode.length();
			serialCode.delete(0, length);
		}
		serialCode = serialCode.append(basketCodeStr);
		String url = "http://linliny.com/checkBasket.json?Frid=" + basketCodeStr;
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(0);
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				httpGetFail();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				if (!TextUtils.isEmpty(arg0.result)) {
					utils.Util.sendMessage(handler, CHECK_BASKET_RFID_CODE, arg0.result);
				} else {
					httpGetFail();
				}
			}
		});
	}

	/**
	 * 这是整个APP中的AlertDialog的一个模板，后面显示的自定义的弹出对话框，都是以此为模板
	 * 
	 * @param message栏中所要显示的内容
	 */
	public void showAlertDialog(Context context, String titleName) {
		if (context != null && mContext != null && alertDialog == null) {
			View view = View.inflate(context, R.layout.dialog, null);
			TextView tv_title = (TextView) view.findViewById(R.id.tv_title1);
			view.findViewById(R.id.iv_cancel).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Util.disMissDialog(alertDialog, (Activity) mContext);
					alertDialog = null;
				}
			});
			tv_title.setText(titleName);
			alertDialog = new AlertDialog.Builder(context, R.style.MyDialogStyle).setView(view).create();
			alertDialog.setCanceledOnTouchOutside(false);
			if (alertDialog != null && mContext != null) {
				alertDialog.show();
			}
		}
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
		findViewById(R.id.btn_confirm_phonelog_activity).setOnClickListener(this);
		findViewById(R.id.btn_cancel_phonelog_activity).setOnClickListener(this);

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

	protected void parseMachineStateCode(byte cmd) {
		switch (cmd) {
		case 0x01:
			MachineSateCode = 1;
			break;
		case 0x02:
			MachineSateCode = 2;
			break;
		case 0x03:
			MachineSateCode = 3;
			break;
		case 0x04:
			MachineSateCode = 4;
			break;
		case 0x05:
			MachineSateCode = 5;
			break;
		case 0x06:
			MachineSateCode = 6;
			break;
		case 0x07:
			MachineSateCode = 7;
			break;
		case 0x08:
			MachineSateCode = 8;
			break;
		case 0x09:
			MachineSateCode = 9;
			break;
		case 0x10:
			MachineSateCode = 10;
			break;
		case 0x11:
			MachineSateCode = 11;
			break;
		default:
			break;
		}
	}

	protected void parseMachineBasketCmd(byte[] cmd) {
		if (isComplteWork) {
			return;
		}
		if ((cmd[3] & 0xff) == 0xFF) {
			// 此时机器未检测到篮子的编码，也就是说机器中没有篮子
			Util.sendMessage(handler, RETURN_BASKET_FAIL);
		} else if (cmd[1] == 0x0E) {
			Util.sendMessage(handler, RETURN_BASKET_SUCCESS);
		}
	}

	/**
	 * 发送获取机器篮子编码的命令
	 */
	protected void sendGetMachineBasketCodeCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				while (!isComplteWork) {
					byte[] cmd = new byte[] { 0x02, 0x03, 0x71, 0x76 };
					Util.delay(500);
					mUartNative.UartWriteCmd(cmd, cmd.length);
				}
			}
		});
	}

	/**
	 * 获取命令中的篮子的编号，并且将篮子的编号发送到服务器
	 * 
	 * @param fullCmd
	 */
	protected void getCardCodeWithToServer(byte[] fullCmd) {
		try {
			// 获取篮子的编码
			long code = getCardCode(fullCmd);
			Logger.e(code + "");
			// 将篮子的编码发送到服务器中，验证是否是我们的篮子，如果是，获取还篮子的位置
			getBasketLocationFromServer(String.valueOf(code));
		} catch (Exception e) {
			str2Voice("读取数据失败，请稍后再试");
			if (mContext != null) {
				((Activity) mContext).startActivity(new Intent(mContext, SplashActivity.class));
			}
			e.printStackTrace();
		}
	}

	private long getCardCode(byte[] fullCmd) throws Exception {
		// 在这里要将编码改变一下
		if (fullCmd.length >= 14 && fullCmd[2] == 0x00) {
			int length = fullCmd[3] - 4;
			byte[] code = new byte[length];
			System.arraycopy(fullCmd, 8, code, 0, length);
			String byteToHexstring = utils.Util.byteToHexstring(code, length);
			String cardCode = "";
			String[] split = byteToHexstring.split(" ");
			for (int i = split.length - 1; i >= 0; i--) {
				cardCode += split[i];
			}
			long cardCodelong = Long.valueOf(cardCode, 16);
			return cardCodelong;
		} else {
			throw new Exception("篮子编码解析错误");
		}
	}

	protected boolean isHaveFullCmd(byte[] array, int length) {
		for (int i = 0; i < length; i++) {
			if (array[i] == 0x20) {
				try {
					if (array[i + 3] == 0x08 && array[i + 13] == 0x03) {
						return true;
					}
				} catch (Exception e) {
					return false;
				}
			}
		}
		return false;
	}

	protected byte[] getFullCmd(byte[] array, int length) {
		int start = 0;
		byte[] temp = new byte[14];
		for (int i = 0; i < length; i++) {
			if (array[i] == 0x20) {
				start = i;
				break;
			}
		}
		System.arraycopy(array, start, temp, 0, 14);
		return temp;
	}

	protected void clearArray(byte[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
		sum = 0;
	}

	@Override
	protected void onStop() {
		super.onStop();
		JniThreadStop();
		if (alertDialog != null && mContext != null) {
			alertDialog.dismiss();
		}
		if (soundPool != null) {
			soundPool.release();
		}
		mContext = null;
	}

	private void JniThreadStop() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (mUartNative != null && mUartNativeCard != null) {
					mUartNative.NativeThreadStop();
					mUartNativeCard.NativeCardThreadStop();
					mUartNative = null;
					mUartNativeCard = null;
				}
			}
		});
	}

	private void httpGetFail() {
		if (mContext != null) {
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					utils.Util.DisplayToast(mContext, "网络错误，请联系客服", R.drawable.warning);
					str2Voice("网络错误，请重试");
				}
			});
		}
	}

	// 还篮子失败的时候，我们发送出货命令，将篮子退换出来
	public void sendOutBasketCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (mUartNative == null) {
					str2Voice("还篮子失败，请联系客服处理");
					return;
				}
				int cycleCount = 0;
				// 查询机器状态
				while (true) {
					// 判断机器此时的状态
					if (MachineSateCode == 1 || MachineSateCode == 2) {
						break;
					}
					mUartNative.UartWriteCmd(ConstantCmd.getMachineStateCmd, ConstantCmd.getMachineStateCmd.length);
					Util.delay(1000);
					if (cycleCount++ > 60) {
						str2Voice("机器出错");
						return;
					}
				}
				// 延时100毫秒
				Util.delay(100);
				String baseketLocation = "0E-" + gid;
				String[] rowAndColumnStr = baseketLocation.split("-");
				GoodsPosition basketPosition = new GoodsPosition(Integer.parseInt(rowAndColumnStr[0], 16),
						Integer.parseInt(rowAndColumnStr[1]));
				byte[] returnBasketCmd = CommandPackage.getRequestShipment(ConstantCmd.get_request_shipment_cmd,
						basketPosition.getRowNum(), basketPosition.getColumnNum());
				mUartNative.UartWriteCmd(returnBasketCmd, returnBasketCmd.length);
				if (mContext != null) {
					str2Voice("请您将篮子取出，稍后再试");
					mContext.startActivity(new Intent(mContext, SplashActivity.class));
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		String phoneNumStr = myCourse_roomId_input.getText().toString();
		switch (v.getId()) {
		case R.id.number_1:
			myCourse_roomId_input.setText(phoneNumStr + number_1.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_2:
			myCourse_roomId_input.setText(phoneNumStr + number_2.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_3:
			myCourse_roomId_input.setText(phoneNumStr + number_3.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_4:
			myCourse_roomId_input.setText(phoneNumStr + number_4.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_5:
			myCourse_roomId_input.setText(phoneNumStr + number_5.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_6:
			myCourse_roomId_input.setText(phoneNumStr + number_6.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_7:
			myCourse_roomId_input.setText(phoneNumStr + number_7.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_8:
			myCourse_roomId_input.setText(phoneNumStr + number_8.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_9:
			myCourse_roomId_input.setText(phoneNumStr + number_9.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_0:
			myCourse_roomId_input.setText(phoneNumStr + number_0.getText().toString());
			myCourse_roomId_input.setSelection(phoneNumStr.length() + 1);
			break;
		case R.id.number_enter:
			myCourse_roomId_input.setText("");
			break;
		case R.id.number_clear_last:
			if (phoneNumStr.length() > 0) {
				myCourse_roomId_input.setText(phoneNumStr.substring(0, phoneNumStr.length() - 1));
				myCourse_roomId_input.setSelection(phoneNumStr.length() - 1);
			}
			break;
		case R.id.btn_confirm_phonelog_activity:
			if (TextUtils.isEmpty(phoneNumStr)) {
				str2Voice("请输入手机号");
			} else {
				if (phoneNumStr.trim().length() != 11) {
					str2Voice("手机号格式输入错误，请重新输入");
					myCourse_roomId_input.setText("");
				} else {
					checkPuoneNumIsMembership(phoneNumStr);
				}
			}
			break;
		case R.id.btn_cancel_phonelog_activity:
			ByPhoneNumReturnBasketDialog.this.dismiss();
			break;
		default:
			break;
		}
	}

	private void checkPuoneNumIsMembership(String phoneNumStr) {
		String url = "http://linliny.com/checkPhoneVipCard.json?phone=" + phoneNumStr + "&CcardId=" + "&mid=" + mid;
		phoneNum = phoneNumStr;
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.configCurrentHttpCacheExpiry(0);
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				httpGetFail();
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				if (!TextUtils.isEmpty(arg0.result)) {
					Util.sendMessage(handler, CHECK_PHONE_IS_MEMBERSHIP, arg0.result);
				} else {
					httpGetFail();
				}
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

}
