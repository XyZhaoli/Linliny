package activity;

import java.io.IOException;

import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.GoodsPosition;
import uartJni.UartJniCard;
import uartJni.Uartjni;
import utils.ActivityManager;
import utils.CommandPackage;
import utils.ThreadManager;
import utils.VoiceUtils;

/**
 * 提示会员卡登录页面（dialog）
 */
@SuppressLint("NewApi")
public class BaskethuiyuankaActitvty extends BaseAcitivity {
	private static final int GET_MACHINE_STATE = 0;
	private static final int CHECK_CARD_IS_MEMBERSHIP = 1;
	private static final int GET_BASKET_LOCATION = 2;
	private static final int CHECK_BASKET_RFID_CODE = 3;
	private static final int RETURN_BASKET_SUCCESS = 5;
	private static final int RETURN_BASKET_FAIL = 6;
	private static final int GET_CODE_FROM_SERIAL = 7;
	private static final int SEND_RETURN_BASKET_CMD_SUCCESS = 8;
	private MyHandler handler = new MyHandler();
	private Uartjni mUartNative = null;
	private UartJniCard mUartNativeCard = null;
	public int gid;
	protected Dialog alertDialog;
	private String mid;

	protected static final String TAG = "会员卡";
	private byte[] tempCardCmd = new byte[64];
	private static int sum = 0;
	private Context mContext = BaskethuiyuankaActitvty.this;
	private boolean isHaveCardCode = false;
	private static int MachineSateCode = 0;
	private String cardCode = "";
	private String serialCode = "";
	protected String BasketCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(true);
		setContentView(R.layout.basket_huiyuanka);
		initData();
		initSerialJni();
	}

	class MyHandler extends Handler {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CHECK_CARD_IS_MEMBERSHIP:
				checkCardIsMembership(msg.obj.toString());
				break;
			case CHECK_BASKET_RFID_CODE:
				// 4.根据返回的验证篮子的信息，成功就发送开门信息，失败则弹窗提示
				// 发送还篮子指令
				// 成功获取到篮子的位置以后，我们给串口发送信息，开始还篮子
				int response = parseJson(msg.obj.toString(), "check");
				if (response == 1) {
					// 是我们的篮子，开始还篮子
					VoiceUtils.getInstance().initmTts("感应成功");
					sendReturnBasketCmd();
					isHaveCardCode = false;
					BasketCode = serialCode;
				} else {
					VoiceUtils.getInstance().initmTts("篮子编码有误");
				}
				break;
			case RETURN_BASKET_FAIL:
				// 这个时候还篮子失败，通知服务器
				utils.Util.DisplayToast(mContext, "还篮子失败", R.drawable.fail);
				VoiceUtils.getInstance().initmTts("还篮子失败，请您将篮子正确放入机器中");
				break;
			case SEND_RETURN_BASKET_CMD_SUCCESS:
				VoiceUtils.getInstance().initmTts("请您等候机器开门");
				break;
			case RETURN_BASKET_SUCCESS:
				returnBasketMoney();
				VoiceUtils.getInstance().initmTts("还篮子成功，准备退款");
				break;
			default:
				break;
			}
		};
	}

	public void returnBasketMoney() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (!TextUtils.isEmpty(BasketCode)) {
					StringBuilder url = new StringBuilder("http://linliny.com/returnBasket.json?gid=").append(gid).append("&phone=&Frid=").append(BasketCode).append("&mid=")
							.append(mid).append("&cardSerial=").append(cardCode);
					HttpUtils httpUtils = new HttpUtils();
					try {
						String httpResult = httpUtils.sendSync(HttpMethod.GET, url.toString()).readString();
						if (!TextUtils.isEmpty(httpResult)) {
							VoiceUtils.getInstance().initmTts("还篮子成功,请注意微信商城退款通知");
							ActivityManager.getInstance().finshAllActivity();
							startActivity(new Intent(mContext, SplashActivity.class));
						}
					} catch (HttpException e) {
						e.printStackTrace();
						httpGetFail();
						sendOutBasketCmd();
						// TODO 这个时候注意将篮子退还出来
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		});

	}

	public void checkCardIsMembership(String string) {
		// 2.收到如果确认有此会员，弹窗提示请将篮子放置在感应区,然后我们的editText会获取到篮子的编码，然后得到篮子的编码
		gid = parseJson(string, "gid");
		switch (gid) {
		case 0:
			VoiceUtils.getInstance().initmTts("机器格子不足，请您稍后再来");
			utils.Util.DisplayToast(mContext, "机器格子不足", R.drawable.smile);
			break;
		case -1:
			VoiceUtils.getInstance().initmTts("会员卡不存在");
			utils.Util.DisplayToast(mContext, "会员卡不存在", R.drawable.smile);
			break;
		case -2:
			VoiceUtils.getInstance().initmTts("您还不是我们的会员，请您前往商城注册会员");
			utils.Util.DisplayToast(mContext, "请您前往商城注册会员", R.drawable.smile);
			break;
		default:
			cardCode = serialCode;
			showAlertDialog(BaskethuiyuankaActitvty.this, "提示");
			VoiceUtils.getInstance().initmTts("请您将篮子放置在感应区");
			isHaveCardCode = true;
			break;
		}
	}

	// 通过串口发送还篮子的指令
	public void sendReturnBasketCmd() {
		new Thread() {
			public void run() {
				int cycleCount = 0;
				// 查询机器状态
				byte[] cmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
				while (MachineSateCode != 1) {
					// 判断机器此时的状态
					mUartNative.UartWriteCmd(cmd, cmd.length);
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (cycleCount++ > 20) {
						VoiceUtils.getInstance().initmTts("机器正忙，付款失败，请您稍后再试");
						return;
					}
				}
				// 延时100毫秒
				utils.Util.delay(100);

				StringBuilder baseketLocation = new StringBuilder("0E-").append(gid);
				String[] rowAndColumnStr = baseketLocation.toString().split("-");
				GoodsPosition basketPosition = new GoodsPosition(Integer.parseInt(rowAndColumnStr[0], 16),
						Integer.parseInt(rowAndColumnStr[1]));
				byte[] returnBasketCmd = CommandPackage.getRequestShipment(ConstantCmd.get_return_basket_cmd,
						basketPosition.getRowNum(), basketPosition.getColumnNum());
				mUartNative.UartWriteCmd(returnBasketCmd, returnBasketCmd.length);
			};
		}.start();
	}

	private void initData() {
		ActivityManager.getInstance().addActivity(BaskethuiyuankaActitvty.this);
		VoiceUtils.getInstance().initmTts("请将会员卡放置在感应区");
		SharedPreferences preferences = getSharedPreferences("userInfo", MODE_PRIVATE);
		mid = preferences.getString("Mid", "");
	}

	private void initSerialJni() {
		// 初始化读取会员卡的串口
		mUartNativeCard = new UartJniCard() {
			@Override
			public void onCardNativeCallback(final byte[] cmd) {
				setTime(120);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < cmd.length; i++) {
							tempCardCmd[i + sum] = cmd[i];
						}
						sum += cmd.length;
						if (isHaveFullCmd(tempCardCmd, sum)) {
							byte[] fullCmd = getFullCmd(tempCardCmd, sum);
							getCardCodeWithToServer(fullCmd);
							clearArray(tempCardCmd);
						}
					}
				});
			}
		};
		mUartNativeCard.nativeCardInitilize();
		mUartNativeCard.BoardCardThreadStart();

		mUartNative = new Uartjni() {
			@Override
			public void onNativeCallback(byte[] cmd) {
				setTime(120);
				// 5.开门以后，关门验证，验证篮子是否已经到位，到位以后发送服务器告知目前已经还篮子成功；
				switch (cmd[2]) {
				case 0x00:
					// TODO 此时表示还篮子成功，我们将还篮子成功的消息发送给服务器
					// 换篮子成功以后，获取机器检测到的篮子编码
					sendGetMachineBasketCodeCmd();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mUartNative != null) {
			mUartNative.NativeThreadStop();
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
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
			// 将篮子的编码发送到服务器中，验证是否是我们的篮子，如果是，获取还篮子的位置
			getBasketLocationFromServer(String.valueOf(code));
		} catch (Exception e) {
			utils.Util.DisplayToast(mContext, e.getMessage(), R.drawable.warning);
			e.printStackTrace();
		}
	}

	// 从服务器获取篮子的位置
	public void getBasketLocationFromServer(final String uartCode) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				serialCode = uartCode;
				if (!isHaveCardCode) {
					// 刚开始我们不确定这个编码是不是会员卡的编码，默认将此编码认为是会员卡编码
					StringBuffer url = new StringBuffer("http://linliny.com/checkPhoneVipCard.json?phone=&cardSerial=").append(uartCode)
							.append("&mid=").append(mid);
					HttpUtils httpUtils = new HttpUtils();
					httpUtils.send(HttpMethod.GET, url.toString(), new RequestCallBack<String>() {

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							httpGetFail();
						}

						@Override
						public void onSuccess(ResponseInfo<String> arg0) {
							if (!TextUtils.isEmpty(arg0.result)) {
								utils.Util.sendMessage(handler, CHECK_CARD_IS_MEMBERSHIP, arg0.result);
							} else {
								httpGetFail();
							}
						}
					});
				} else {
					// TODO 3.读取的篮子的信息传给服务器进行验证
					// 如果我们已经获取到了会员卡的信息，这是可以默认用户此时是获取的是篮子的编码，获取篮子的RFID编码 然后将篮子的编码发送给服务器进行验证
					StringBuffer url = new StringBuffer("http://linliny.com/checkBasket.json?Frid=").append(uartCode);
					HttpUtils httpUtils = new HttpUtils();
					httpUtils.send(HttpMethod.GET, url.toString(), new RequestCallBack<String>() {

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

			}
		});
	}

	private long getCardCode(byte[] fullCmd) throws Exception {
		// 在这里要将编码改变一下
		if (fullCmd.length >= 14 && fullCmd[2] == 0x00) {
			int length = fullCmd[3] - 4;
			byte[] code = new byte[length];
			System.arraycopy(fullCmd, 8, code, 0, length);
			String byteToHexstring = utils.Util.byteToHexstring(code, length);
			StringBuilder cardCodeStr = new StringBuilder();
			String[] split = byteToHexstring.split(" ");
			for (int i = split.length - 1; i >= 0; i--) {
				cardCodeStr.append(split[i]);
			}
			long cardCodelong = Long.valueOf(cardCodeStr.toString(), 16);
			return cardCodelong;
		} else {
			throw new Exception("篮子编码解析错误");
		}
	}

	public int parseJson(String string, String key) {
		int value = -1;
		try {
			JSONObject object = new JSONObject(string);
			value = object.getInt(key);
		} catch (Exception e) {
			e.printStackTrace();
			value = -1;
		}
		return value;
	}

	public void showAlertDialog(final Context context, String titleName) {
		View view = View.inflate(context, R.layout.dialog, null);
		TextView tv_title = (TextView) view.findViewById(R.id.tv_title1);
		tv_title.setText(titleName);
		alertDialog = new AlertDialog.Builder(this).setView(view).create();
		alertDialog.show();
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
			}
			System.arraycopy(array, start, temp, 0, 14);
		}
		return temp;
	}

	protected void clearArray(byte[] array) {
		for (int i = 0; i < array.length; i++) {
			array[i] = 0;
		}
		sum = 0;
	}

	protected void parseMachineBasketCmd(byte[] cmd) {
		if (cmd[3] == 0x00) {
			// 此时表示正在读篮子的编码,但是还未读取到篮子的编码，继续发送命令获取篮子的编码
			sendGetMachineBasketCodeCmd();
		} else if (cmd[3] == 0xFF) {
			// 此时机器未检测到篮子的编码，也就是说机器中没有篮子
			utils.Util.sendMessage(handler, RETURN_BASKET_FAIL);
			// TODO 后续还需要做的工作 如果篮子已经放进去了 但是乜有检测到编码，这个时候爱需要开门，用户将篮子取走
			byte[] command = CommandPackage.getRequestShipment(ConstantCmd.get_request_shipment_cmd,
					Integer.valueOf("0E", 16), gid);
			mUartNative.UartWriteCmd(command, command.length);
		} else if (cmd[1] == 0x0E) {
			utils.Util.sendMessage(handler, RETURN_BASKET_SUCCESS);
		}
	}

	/**
	 * 发送获取机器篮子编码的命令
	 */
	protected void sendGetMachineBasketCodeCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				byte[] cmd = new byte[] { 0x02, 0x03, 0x71, 0x76 };
				utils.Util.delay(100);
				mUartNative.UartWriteCmd(cmd, cmd.length);
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
		default:
			break;
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
				utils.Util.DisplayToast(mContext, "网络错误，请联系客服", R.drawable.warning);
				VoiceUtils.getInstance().initmTts("网络错误，请重试");
			}
		});
	}

	// 还篮子失败的时候，我们发送出货命令，将篮子退换出来
	public void sendOutBasketCmd() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				int cycleCount = 0;
				// 查询机器状态
				while (MachineSateCode != 1) {
					// 判断机器此时的状态
					byte[] cmd = new byte[] { 0x02, 0x03, 0x10, 0x15 };
					mUartNative.UartWriteCmd(cmd, cmd.length);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (cycleCount++ > 40) {
						VoiceUtils.getInstance().initmTts("机器出错");
						return;
					}
				}
				// 延时100毫秒
				utils.Util.delay(100);
				String baseketLocation = "0E-" + gid;
				String[] rowAndColumnStr = baseketLocation.split("-");
				GoodsPosition basketPosition = new GoodsPosition(Integer.parseInt(rowAndColumnStr[0], 16),
						Integer.parseInt(rowAndColumnStr[1]));
				byte[] returnBasketCmd = CommandPackage.getRequestShipment(ConstantCmd.get_request_shipment_cmd,
						basketPosition.getRowNum(), basketPosition.getColumnNum());
				mUartNative.UartWriteCmd(returnBasketCmd, returnBasketCmd.length);
			}
		});
	}
}
