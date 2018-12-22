package dialog;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.bumptech.glide.Glide;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.orhanobut.logger.Logger;

import activity.LoaddingActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.ZXingUtils;
import android_serialport_api.aa;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.PayforResponse;
import uartJni.UartJniCard;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;
import utils.WXPayUtil;

/**
 * 支付页面
 * 
 * @author Administrator
 *
 */
@SuppressLint("HandlerLeak")
public class CheckOutDialog extends Dialog implements View.OnClickListener {

	private static final String TAG = "CheckOutDialog";
	private static final int CHECK_MEMBERSHIP_PAYFOR = 2;
	protected static final int CREATE_CODE = 3;
	Handler handlerNum = new Handler();
	private UartJniCard mUartNativeCard = null;
	private byte[] tempCardCmd = new byte[32];
	private int sum = 0;
	private Context mContext;
	private boolean isMembershipPay;
	private boolean isRunning = false;
	private boolean isDestory = false;
	private Bitmap wechatPayBarcodeBitmap;
	private Bitmap alipayPayBarcodeBitmap;
	private String membershipPayforCode = "";
	private String alipayUrl;
	private String wechatUrl;
	private String outTradeNo;
	private String Ono;
	private String totalPrice;
	private String goodsInfo;
	private String linliPayOno;
	private StringBuffer wechatPayUrls = new StringBuffer();
	private StringBuffer zfb = new StringBuffer();
	private StringBuffer linliNyUrl;
	private ImageView ivAlipay;
	private ImageView ivWechat;
	private ImageView huiyuanka;
	private ImageView iVCancel;
	// 支付二维码
	private ImageView payforBarcode;
	private TextView tvPayforMode;
	private TextView timerText;
	private TextView tvRemind;
	private AlertDialog alertDialog;
	private AlertDialog linliDialog;
	private Bitmap linlinyQRImage;
	
	private int destroryCount;

	public CheckOutDialog(Context context) {
		super(context);
	}

	public CheckOutDialog(Context context, int theme, PayforResponse response, String Ccard, String totalPrice,
			String goodsInfo) {
		super(context, theme);
		this.mContext = context;
		initCheckOutStr(response, Ccard, totalPrice, goodsInfo);
	}

	public CheckOutDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				if (msg.obj != null) {
					String string = msg.obj.toString();
					if (string.equals("success")) {
						payforSuccess(msg.obj.toString());
					}
				}
				break;
			// 检查会员卡支付结果
			case CHECK_MEMBERSHIP_PAYFOR:
				parseMembershipRes(msg.obj.toString());
				break;
			case 3:
				if (wechatPayBarcodeBitmap == null) {
					wechatPayBarcodeBitmap = ZXingUtils.createQRImage(wechatUrl, 200, 200);
				}
				payforBarcode.setImageBitmap(wechatPayBarcodeBitmap);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_cancel_dialogactivity:
			Util.disMissDialog(CheckOutDialog.this, (Activity) mContext);
			break;
		case R.id.zhifubao:
			if (alipayPayBarcodeBitmap == null) {
				alipayPayBarcodeBitmap = ZXingUtils.createQRImage(alipayUrl, 200, 200);
			}
			payforBarcode.setImageBitmap(alipayPayBarcodeBitmap);
			setIvAlpha(0.5f, 1.0f, 0.5f);
			setTvText("支付宝");
			tvRemind.setVisibility(View.INVISIBLE);
			// 请求网络 查询是否支付成功
			str2Voice("请使用支付宝支付");
			break;
		case R.id.weixin:
			if (wechatPayBarcodeBitmap == null) {
				wechatPayBarcodeBitmap = ZXingUtils.createQRImage(wechatUrl, 200, 200);
			}
			tvRemind.setVisibility(View.INVISIBLE);
			str2Voice("请使用微信支付");
			payforBarcode.setImageBitmap(wechatPayBarcodeBitmap);
			setIvAlpha(1f, 0.5f, 0.5f);
			setTvText("微信");
			break;
		case R.id.iv_vipcard_iupaylist:
			setIvAlpha(0.5f, 0.5f, 1f);
			tvPayforMode.setText(Html.fromHtml("请将您的" + "<font color='#1c86ee'>" + "会员卡" + "</font>" + "放置在感应区"));
			isMembershipPay = true;
			Glide.with(mContext).load(R.drawable.membership_pay_icon).into(payforBarcode);
			str2Voice("请将您的会员卡放置在感应区");
			// tvRemind.setVisibility(View.VISIBLE);
			break;
		case R.id.iv_cancel_iupaylist:
			Util.disMissDialog(CheckOutDialog.this, (Activity) mContext);
			break;
		case R.id.tv_remind:
			showRemindDialog();
			break;
		default:
			break;
		}
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

	
	//忘带会员卡扫码支付提示框
	private void showRemindDialog() {
		if (isRunning) {
			isDestory = true;
		}
		View view = View.inflate(mContext, R.layout.remind_scan_code_dialog,
				(ViewGroup) CheckOutDialog.this.getWindow().getDecorView());
		final ImageView ivCode = (ImageView) view.findViewById(R.id.iv_code);
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (linlinyQRImage != null) {
							ivCode.setImageBitmap(linlinyQRImage);
						}
					}
				});
				HttpUtils httpUtils = new HttpUtils();
				int i = 0;
				while (i++ < 60) {
					httpUtils.send(HttpMethod.GET, ConstantCmd.BASE_URLS + "QueryOrderState1.json?Ono=" + linliPayOno,
							new RequestCallBack<String>() {

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							Logger.e(arg1);
						}

						@Override
						public void onSuccess(ResponseInfo<String> arg0) {
							if (arg0.result.equals("5")) {
								payforSuccess(arg0.result);
							}
						}
					});
					Util.delay(1000);
				}
				if (!CheckOutDialog.this.isDestory) {
					payforFail();
					Util.disMissDialog(linliDialog, (Activity) mContext);
				}
			}
		});
		linliDialog = new AlertDialog.Builder(mContext).setView(view).create();
		linliDialog.show();
	}

	/**
	 * 检查会员卡的支付结果
	 * 
	 * @param string
	 */
	protected void parseMembershipRes(String string) {
		if (!TextUtils.isEmpty(string)) {
			membershipPayforCode = utils.Util.parseJsonStr(string, "res");
			if (membershipPayforCode.equals("0")) {
				// 会员卡没有绑定
				str2Voice("您的会员卡还没有绑定，请您前往商城绑定会员卡");
				utils.Util.DisplayToast(mContext, "请你前往商城绑定会员卡", R.drawable.warning);
			} else if (membershipPayforCode.equals("-1")) {
				str2Voice("该会员卡不存在");
				utils.Util.DisplayToast(mContext, "该会员卡不存在", R.drawable.warning);
			} else if (membershipPayforCode.equals("3")) {
				str2Voice("您输入的密码有误");
			} else if (membershipPayforCode.equals("2")) {
				str2Voice("会员卡余额不足，请您前往商城充值");
			} else if (membershipPayforCode.length() > 1) {
				payforSuccess("success");
			}
		} else {
			str2Voice("支付失败，请您重试");
			utils.Util.DisplayToast(mContext, "支付失败，请您重试", R.drawable.warning);
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_paylist);
		setCanceledOnTouchOutside(false);
		initData();
		initView();
		initSerial();
		startCheckResonse();
	}

	private void initView() {
		iVCancel = (ImageView) findViewById(R.id.iv_cancel_iupaylist);
		ivAlipay = (ImageView) findViewById(R.id.zhifubao);
		ivWechat = (ImageView) findViewById(R.id.weixin);
		huiyuanka = (ImageView) findViewById(R.id.iv_vipcard_iupaylist);
		payforBarcode = (ImageView) findViewById(R.id.iv_QR_code_iupaylist);
		timerText = (TextView) findViewById(R.id.timer);
		tvPayforMode = (TextView) findViewById(R.id.tv_payfor_mode);
		tvRemind = (TextView) findViewById(R.id.tv_remind);
		tvRemind.setVisibility(View.INVISIBLE);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (mContext != null) {
					Glide.with(mContext).load(R.drawable.wechat_pay_icon).into(ivWechat);
					Glide.with(mContext).load(R.drawable.alipay_icon).into(ivAlipay);
					Glide.with(mContext).load(R.drawable.membership_pay_icon).into(huiyuanka);
				}
			}
		});

		iVCancel.setOnClickListener(this);
		ivAlipay.setOnClickListener(this);
		ivWechat.setOnClickListener(this);
		huiyuanka.setOnClickListener(this);
		tvRemind.setOnClickListener(this);
		setIvAlpha(1f, 0.5f, 0.5f);
		setTvText("微信");
	}

	private void initSerial() {
		// 初始化读取会员卡的串口
		mUartNativeCard = new UartJniCard() {
			@Override
			public void onCardNativeCallback(final byte[] cmd) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
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
	}

	private void initData() {
		linliPayOno = WXPayUtil.getorderid();
		str2Voice("请使用微信支付");
	}

	public void initCheckOutStr(final PayforResponse response, String ccard, final String totalPrice,
			final String goodsInfo) {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				outTradeNo = aa.AlipayOrder(response.getAliPayRes());
				zfb = zfb.append(ConstantCmd.BASE_URLS + "QueryOrderAli.json?outTradeNo=").append(outTradeNo);
				wechatUrl = aa.weixinStr(response.getWechatRes());
				alipayUrl = aa.AlipayStr(response.getAliPayRes());
				Ono = aa.AlipayWXOrder(response.getWechatRes());
				wechatPayUrls = wechatPayUrls.append(ConstantCmd.BASE_URLS + "QueryOrderState1.json?Ono=").append(Ono);
				CheckOutDialog.this.totalPrice = totalPrice;
				CheckOutDialog.this.goodsInfo = goodsInfo;
				Logger.e(goodsInfo);
				linliNyUrl = new StringBuffer();
				linliNyUrl
						.append("https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx2783185b0a0d7d44&redirect_uri=http%3a%2f%2flinliny.com.cn%2f%2ftestBalPay.json%3fTotal=")
						.append(totalPrice).append("%26Goods=").append(goodsInfo).append("%26mid=")
						.append(Util.getMid()).append("%26ono").append(linliPayOno)
						.append("&response_type=code&scope=snsapi_userinfo&state=STATE&connect_redirect=1#wechat_redirect");

				linlinyQRImage = ZXingUtils.createQRImage(linliNyUrl.toString(), 200, 200);
				wechatPayBarcodeBitmap = ZXingUtils.createQRImage(wechatUrl, 200, 200);
				alipayPayBarcodeBitmap = ZXingUtils.createQRImage(alipayUrl, 200, 200);
				handler.sendEmptyMessage(3);
			}
		});
	}

	// 检查支付结果
	protected void startCheckResonse() {
		if (!isRunning) {
			ThreadManager.getThreadPool().execute(new Runnable() {
				private int count = 120;
				@Override
				public void run() {
					try {
						isRunning = true;
						HttpUtils httpUtils = new HttpUtils();
						httpUtils.configCurrentHttpCacheExpiry(500);
						while (count-- > 0) {
							if (tvPayforMode.getText().toString().equals("请使用微信扫描上方二维码")) {
								String wecatRes = httpUtils.sendSync(HttpMethod.GET, wechatPayUrls.toString())
										.readString();
								if (!TextUtils.isEmpty(wecatRes)) {
									if (wecatRes.equals("SUCCESS") || wecatRes.equals("success")
											|| wecatRes.equals("5")) {
										payforSuccess(wecatRes);
										isRunning = false;
										return;
									}
								}
							} else if (tvPayforMode.getText().toString().equals("请使用支付宝扫描上方二维码")) {
								String alipayRes = httpUtils.sendSync(HttpMethod.GET, zfb.toString()).readString();
								if (!TextUtils.isEmpty(alipayRes)) {
									if (alipayRes.equals("SUCCESS") || alipayRes.equals("success")
											|| alipayRes.equals("5")) {
										payforSuccess(alipayRes);
										isRunning = false;
										return;
									}
								}
							}
							if(isDestory) {
								destroryCount++;
							}
							if (isDestory && destroryCount > 5) {
								isRunning = false;
								return;
							}
							utils.Util.delay(1000);
							((Activity) mContext).runOnUiThread(new Runnable() {

								@Override
								public void run() {
									timerText.setText(count + "s");
								}
							});
							if(count == 30) {
								str2Voice("您好，请您尽快付款");
							}
						}
						isRunning = false;
						Util.delay(5000);
						Util.disMissDialog(CheckOutDialog.this, (Activity) mContext);
					} catch (Exception e) {
						e.printStackTrace();
						payforFail();
						Util.disMissDialog(CheckOutDialog.this, (Activity) mContext);
					}
				}
			});
		}
	}

	protected void parseResponse(String res) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		if (res.equals("1")) {
			builder.setTitle("温馨提示").setMessage("支付成功，等待出货").create().show();
		} else if (res.equals("2")) {
			builder.setTitle("温馨提示").setMessage("会员卡余额不足").create().show();
		} else if (res.equals("0")) {
			builder.setTitle("温馨提示").setMessage("此会员卡未绑定").create().show();
		} else if (res.equals("-1")) {
			builder.setTitle("温馨提示").setMessage("会员卡不存在").create().show();
		} else {
			builder.setTitle("温馨提示").setMessage("请重新刷卡").create().show();
		}
	}

	/**
	 * 关闭页面时销毁定时器
	 */
	@SuppressLint("NewApi")
	@Override
	protected void onStop() {
		super.onStop();
		JniThreadStop();
		Util.disMissDialog(alertDialog, (Activity) mContext);
		if (isRunning) {
			isDestory = true;
		}
	}
	
	private void JniThreadStop() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				if (mUartNativeCard != null) {
					mUartNativeCard.NativeCardThreadStop();
					mUartNativeCard = null;
				}
			}
		});
	}

	protected void payforSuccess(String res) {
		if(mContext != null) {
			((Activity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(mContext, LoaddingActivity.class);
					intent.putExtra("Ono", Ono);
					intent.putExtra("outTradeNo", outTradeNo);
					intent.putExtra("membershipPayforCode", membershipPayforCode);
					intent.putExtra("goodsInfo", goodsInfo);
					if (tvPayforMode.getText().toString().equals("请使用微信扫描上方二维码")) {
						intent.putExtra("payfor", "wechat");
					} else if (tvPayforMode.getText().toString().equals("请使用支付宝扫描上方二维码")) {
						intent.putExtra("payfor", "alipay");
					} else if (tvPayforMode.getText().toString().equals("请将您的会员卡放置在感应区")) {
						intent.putExtra("payfor", "membership");
					}
					mContext.startActivity(intent);
					Util.disMissDialog(CheckOutDialog.this, (Activity) mContext);
				}
			});
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

	/**
	 * 读取会员卡编号
	 * @param fullCmd
	 */
	protected void getCardCodeWithToServer(byte[] fullCmd) {
		try {
			// 获取会员卡的编码
			long code = getCardCode(fullCmd);
			// TODO 如果此时是用户进行会员卡支付
			Log.e("code", code + "");
			if (isMembershipPay && alertDialog == null && code > 0) {
				showMembershipPayforDialog(code);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showMembershipPayforDialog(final long code) {
		str2Voice("请输入会员卡支付密码");
		final View view = View.inflate(mContext, R.layout.menbership_card_pay_dialog,
				(ViewGroup) CheckOutDialog.this.getWindow().getDecorView());
		alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).setView(view).create();
		Button btnConfirmPayfor = (Button) view.findViewById(R.id.bt_ok_membership_pay_dialog);
		Button btnCancel = (Button) view.findViewById(R.id.bt_cancel_membership_pay_dialog);
		TextView tvPrice = (TextView) view.findViewById(R.id.tv_price_membership_pay_dialog);
		final EditText etPassword = (EditText) view.findViewById(R.id.et_password_membership_pay_dialog);
		setPrice(tvPrice, "￥" + priceFormat(totalPrice));
		btnConfirmPayfor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 开始请求网络
				if (!TextUtils.isEmpty(etPassword.getText().toString())) {
					String url = ConstantCmd.BASE_URLS + "UpdateUserCard.json?CcardId=" + code + "&Goods=" + goodsInfo
							+ "&Mid=" + utils.Util.getMid() + "&Pwd=" + etPassword.getText().toString() + "&Total="
							+ totalPrice;
					Log.e("CardUrl", url);
					HttpUtils httpUtils = new HttpUtils();
					httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							payforFail();
						}

						@Override
						public void onSuccess(ResponseInfo<String> arg0) {
							if (!TextUtils.isEmpty(arg0.result)) {
								utils.Util.sendMessage(handler, CHECK_MEMBERSHIP_PAYFOR, arg0.result);
							}
						}
					});
				} else {
					((Activity) mContext).runOnUiThread(new Runnable() {
						@Override
						public void run() {
							utils.Util.DisplayToast(mContext, "请输入您的密码", R.drawable.warning);
							str2Voice("请您重新输入密码");
						}
					});
				}
			}
		});

		btnCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (view != null) {
					Util.disMissDialog(CheckOutDialog.this, (Activity) mContext);
				}
			}
		});
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				showKeyboard(etPassword);
			}
		}, 200);
		Util.showCustomDialog(alertDialog, (Activity) mContext);
	}

	private void setPrice(TextView tvPrice, String stringExtra) {
		if (!TextUtils.isEmpty(stringExtra) && tvPrice != null) {
			int start = stringExtra.indexOf(".");
			int end = stringExtra.length();
			SpannableString textSpan = new SpannableString(stringExtra);
			textSpan.setSpan(new AbsoluteSizeSpan(24), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			textSpan.setSpan(new AbsoluteSizeSpan(50), 1, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			textSpan.setSpan(new AbsoluteSizeSpan(24), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			tvPrice.setText(textSpan);
		}
	}

	public String priceFormat(String num) {
		DecimalFormat format = new DecimalFormat("0.00");
		String a = format.format(new BigDecimal(num));
		return a;
	}

	private long getCardCode(byte[] fullCmd) throws Exception {
		if(fullCmd == null) {
			return -1;
		}
		// 在这里要将编码改变一下
		try {
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
		} catch (Exception e) {
			return -1;
		}
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

	@SuppressLint("NewApi")
	private void setIvAlpha(float f, float g, float h) {
		ivWechat.setAlpha(f);
		ivAlipay.setAlpha(g);
		huiyuanka.setAlpha(h);
	}

	private void setTvText(String str) {
		if (!TextUtils.isEmpty(str)) {
			tvPayforMode.setText(Html.fromHtml("请使用" + "<font color='#1c86ee'>" + str + "</font>" + "扫描上方二维码"));
		}
	}

	private void payforFail() {
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				utils.Util.DisplayToast(mContext, "支付失败", R.drawable.warning);
				str2Voice("支付失败");
			}
		});
	}

	public void showKeyboard(EditText editText) {
		if (editText != null) {
			// 设置可获得焦点
			editText.setFocusable(true);
			editText.setFocusableInTouchMode(true);
			// 请求获得焦点
			editText.requestFocus();
			// 调用系统输入法
			InputMethodManager inputManager = (InputMethodManager) editText.getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(editText, 0);
		}
	}

	private void showSoftInputFromWindow(Activity activity, EditText editText) {
		editText.setFocusable(true);
		editText.setFocusableInTouchMode(true);
		editText.requestFocus();
		activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

}
