package activity;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.bumptech.glide.Glide;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android_serialport_api.ZXingUtils;
import android_serialport_api.sample.R;
import uartJni.UartJniCard;
import utils.ActivityManager;
import utils.ThreadManager;
import utils.VoiceUtils;

@SuppressLint("NewApi")
public class IuPayListActivity extends BaseAcitivity implements OnClickListener {

	private static final String TAG = "IuPayListActivity";
	private static final String BASE_URLS = "http://linliny.com/dingyifeng_web/";
	private static final int CHECK_MEMBERSHIP_PAYFOR = 2;
	Handler handlerNum = new Handler();
	private UartJniCard mUartNativeCard = null;
	private byte[] tempCardCmd = new byte[32];
	private int sum = 0;
	private Context mContext = IuPayListActivity.this;
	private boolean isMembershipPay;
	private boolean isRunning = false;
	private boolean isDestory = false;
	private static Bitmap wechatPayBarcodeBitmap;
	private static Bitmap alipayPayBarcodeBitmap;
	private String membershipPayforCode = "";
	private String alipayUrl;
	private String wechatUrl;
	private String outTradeNo;
	private String Ono;
	private String totalPrice;
	private String goodsInfo;
	private String response;
	private static String wechatPayUrls;
	private static String zfb;
	private ImageView ivAlipay;
	private ImageView ivWechat;
	private ImageView huiyuanka;
	private ImageView iVCancel;
	// 支付二维码
	private ImageView payforBarcode;
	private TextView tvPayforMode;
	private TextView timerText;
	private AlertDialog alertDialog;

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
			IuPayListActivity.this.finish();
			break;
		case R.id.zhifubao:
			if (alipayPayBarcodeBitmap == null) {
				alipayPayBarcodeBitmap = ZXingUtils.createQRImage(alipayUrl, 200, 200);
			}
			payforBarcode.setImageBitmap(alipayPayBarcodeBitmap);
			setIvAlpha(0.5f, 1.0f, 0.5f);
			setTvText("支付宝");
			// 请求网络 查询是否支付成功
			VoiceUtils.getInstance().initmTts(mContext, "请使用支付宝支付");
			break;
		case R.id.weixin:
			if (wechatPayBarcodeBitmap == null) {
				wechatPayBarcodeBitmap = ZXingUtils.createQRImage(wechatUrl, 200, 200);
			}
			VoiceUtils.getInstance().initmTts(mContext, "请使用微信支付");
			payforBarcode.setImageBitmap(wechatPayBarcodeBitmap);
			setIvAlpha(1f, 0.5f, 0.5f);
			setTvText("微信");
			break;
		case R.id.iv_vipcard_iupaylist:
			setIvAlpha(0.5f, 0.5f, 1f);
			tvPayforMode.setText(Html.fromHtml("请将您的" + "<font color='#1c86ee'>" + "会员卡" + "</font>" + "放置在感应区"));
			isMembershipPay = true;
			Glide.with(getApplicationContext()).load(R.drawable.membership_pay_icon).into(payforBarcode);
			VoiceUtils.getInstance().initmTts(mContext, "请将您的会员卡放置在感应区");
			break;
		case R.id.iv_cancel_iupaylist:
			IuPayListActivity.this.finish();
			break;

		default:
			break;
		}
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
				VoiceUtils.getInstance().initmTts(mContext, "您的会员卡还没有绑定，请您前往商城绑定会员卡");
				utils.Util.DisplayToast(mContext, "请你前往商城绑定会员卡", R.drawable.warning);
			} else if (membershipPayforCode.equals("-1")) {
				VoiceUtils.getInstance().initmTts(mContext, "该会员卡不存在");
				utils.Util.DisplayToast(mContext, "该会员卡不存在", R.drawable.warning);
			} else if (membershipPayforCode.equals("3")) {
				VoiceUtils.getInstance().initmTts(mContext, "您输入的密码有误");
			} else if (membershipPayforCode.equals("2")) {
				VoiceUtils.getInstance().initmTts(mContext, "会员卡余额不足，请您前往商城充值");
			} else if (membershipPayforCode.length() > 1) {
				payforSuccess("success");
			}
		} else {
			VoiceUtils.getInstance().initmTts(mContext, "支付失败，请您重试");
			utils.Util.DisplayToast(mContext, "支付失败，请您重试", R.drawable.warning);
		}
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setFinishOnTouchOutside(false);
		setContentView(R.layout.ui_paylist);
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

		handler.post(new Runnable() {
			@Override
			public void run() {
				Glide.with(mContext).load(R.drawable.wechat_pay_icon).into(ivWechat);
				Glide.with(mContext).load(R.drawable.alipay_icon).into(ivAlipay);
				Glide.with(mContext).load(R.drawable.membership_pay_icon).into(huiyuanka);
			}
		});

		iVCancel.setOnClickListener(this);
		ivAlipay.setOnClickListener(this);
		ivWechat.setOnClickListener(this);
		huiyuanka.setOnClickListener(this);

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
		VoiceUtils.getInstance().initmTts(mContext, "请使用微信支付");
		ActivityManager.getInstance().addActivity(IuPayListActivity.this);
		getIntentStr();
		getQRBitMap();
	}

	private void getQRBitMap() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
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
				@Override
				public void run() {
					try {
						isRunning = true;
						int i = 0;
						HttpUtils httpUtils = new HttpUtils();
						httpUtils.configCurrentHttpCacheExpiry(1000);
						while (i++ < 120) {
							if (tvPayforMode.getText().toString().equals("请使用微信扫描上方二维码")) {
								String wecatRes = httpUtils.sendSync(HttpMethod.GET, wechatPayUrls).readString();
								if (!TextUtils.isEmpty(wecatRes)) {
									Log.e(TAG, wecatRes);
									if (wecatRes.equals("SUCCESS") || wecatRes.equals("success")
											|| wecatRes.equals("5")) {
										payforSuccess(wecatRes);
										isRunning = false;
										return;
									}
								}
							} else if (tvPayforMode.getText().toString().equals("请使用支付宝扫描上方二维码")) {
								String alipayRes = httpUtils.sendSync(HttpMethod.GET, zfb).readString();
								if (!TextUtils.isEmpty(alipayRes)) {
									Log.e(TAG, alipayRes);
									if (alipayRes.equals("SUCCESS") || alipayRes.equals("success")
											|| alipayRes.equals("5")) {
										payforSuccess(alipayRes);
										isRunning = false;
										return;
									}
								}
							}
							if (isDestory) {
								isRunning = false;
								return;
							}
							utils.Util.delay(1000);
						}
						if (!IuPayListActivity.this.isDestroyed()) {
							payforFail();
						}
						isRunning = false;
						utils.Util.delay(5000);
						IuPayListActivity.this.finish();
					} catch (Exception e) {
						payforFail();
					}
				}
			});
		}
	}

	protected void parseResponse(String res) {
		AlertDialog.Builder builder = new AlertDialog.Builder(IuPayListActivity.this);
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
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 广播机制
		if (mUartNativeCard != null) {
			mUartNativeCard.NativeCardThreadStop();
			mUartNativeCard = null;
		}
		if (alertDialog != null) {
			alertDialog.dismiss();
		}
		if (isRunning) {
			isDestory = true;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isRunning) {
			isDestory = true;
		}
		if (mUartNativeCard != null) {
			mUartNativeCard.NativeCardThreadStop();
			mUartNativeCard = null;
		}
	}

	protected void payforSuccess(String res) {
		VoiceUtils.getInstance().initmTts(getApplicationContext(), "支付成功");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Intent intent = new Intent(IuPayListActivity.this, LoaddingActivity.class);
				intent.putExtra("Ono", Ono);
				intent.putExtra("outTradeNo", outTradeNo);
				intent.putExtra("membershipPayforCode", membershipPayforCode);
				intent.putExtra("goodsInfo", goodsInfo);
				if (tvPayforMode.getText().toString().equals("请使用微信扫描上方二维码")) {
					intent.putExtra("payfor", "alipay");
				} else if (tvPayforMode.getText().toString().equals("请使用支付宝扫描上方二维码")) {
					intent.putExtra("payfor", "wechat");
				} else if (tvPayforMode.getText().toString().equals("请将您的会员卡放置在感应区")) {
					intent.putExtra("payfor", "membership");
				}
				startActivity(intent);
				IuPayListActivity.this.finish();
			}
		});
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
	 * 获取命令中的篮子的编号，并且将篮子的编号发送到服务器
	 * 
	 * @param fullCmd
	 */
	protected void getCardCodeWithToServer(byte[] fullCmd) {
		try {
			// 获取篮子的编码
			long code = getCardCode(fullCmd);
			// TODO 如果此时是用户进行会员卡支付，则将获得篮子的编码，准备将篮子的编码发送给服务器
			if (isMembershipPay && alertDialog == null) {
				showMembershipPayforDialog(code);
			}
		} catch (Exception e) {
			utils.Util.DisplayToast(mContext, "支付错误", R.drawable.warning);
			e.printStackTrace();
		}
	}

	private void showMembershipPayforDialog(final long code) {
		VoiceUtils.getInstance().initmTts(mContext, "请输入密码");
		View view = View.inflate(mContext, R.layout.menbership_card_pay_dialog, null);
		Button btnConfirmPayfor = (Button) view.findViewById(R.id.bt_ok_membership_pay_dialog);
		Button btnCancel = (Button) view.findViewById(R.id.bt_cancel_membership_pay_dialog);
		TextView tvPrice = (TextView) view.findViewById(R.id.tv_price_membership_pay_dialog);
		final EditText etPassword = (EditText) view.findViewById(R.id.et_password_membership_pay_dialog);
		setPrice(tvPrice, "￥" + priceFormat(totalPrice));
		btnConfirmPayfor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 开始请求网络
				if (!TextUtils.isEmpty(etPassword.getText().toString())) {
					String url = BASE_URLS + "UpdateUserCard.json?CcardId=" + code + "&Goods=" + goodsInfo + "&Mid="
							+ utils.Util.getMid() + "&Pwd=" + etPassword.getText().toString() + "&Total=" + totalPrice;
					HttpUtils httpUtils = new HttpUtils();
					httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
									VoiceUtils.getInstance().initmTts(mContext, "网络错误，请重试");
								}
							});
						}

						@Override
						public void onSuccess(ResponseInfo<String> arg0) {
							if (!TextUtils.isEmpty(arg0.result)) {
								utils.Util.sendMessage(handler, CHECK_MEMBERSHIP_PAYFOR, arg0.result);
							}
						}
					});
				} else {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							utils.Util.DisplayToast(mContext, "请输入您的密码", R.drawable.warning);
							VoiceUtils.getInstance().initmTts(mContext, "请您重新输入密码");
						}
					});
				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				alertDialog.cancel();
			}
		});
		alertDialog = new AlertDialog.Builder(this).setView(view).create();
		alertDialog.show();
	}

	private void setPrice(TextView tvPrice, String stringExtra) {
		if (stringExtra != null && tvPrice != null) {
			int start = stringExtra.indexOf(".");
			int end = stringExtra.length();
			SpannableString textSpan = new SpannableString(stringExtra);
			textSpan.setSpan(new AbsoluteSizeSpan(24), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			textSpan.setSpan(new AbsoluteSizeSpan(50), 1, start, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			textSpan.setSpan(new AbsoluteSizeSpan(24), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			tvPrice.setText(textSpan);
		}
	}

	public void getIntentStr() {
		outTradeNo = getIntent().getStringExtra("outTradeNo").toString().trim();
		zfb = "http://linliny.com/dingyifeng_web/QueryOrderAli.json?outTradeNo="
				+ outTradeNo;
		wechatUrl = getIntent().getStringExtra("rs").toString().trim();
		alipayUrl = getIntent().getStringExtra("ZFurl").toString().trim();
		Ono = getIntent().getStringExtra("Ono").toString().trim();
		wechatPayUrls = "http://linliny.com/dingyifeng_web/QueryOrderState1.json?Ono="
				+ Ono;
		totalPrice = getIntent().getStringExtra("totalPrice").toString().trim();
		goodsInfo = getIntent().getStringExtra("goodsInfo").toString().trim();
	}

	public String priceFormat(String num) {
		DecimalFormat format = new DecimalFormat("0.00");
		String a = format.format(new BigDecimal(num));
		return a;
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

	@Override
	public void changeTvTime(int time) {
		timerText.setText(time + "s");
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
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				utils.Util.DisplayToast(mContext, "支付失败", R.drawable.warning);
				VoiceUtils.getInstance().initmTts(mContext, "支付失败");
			}
		});
	}

}