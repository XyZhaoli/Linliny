package activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;
import com.orhanobut.logger.Logger;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import domain.MachineState;
import domain.SoftwareInfo;
import utils.MachineStateManager;
import utils.ThreadManager;
import utils.Util;
import utils.VoiceUtils;
import view.BannerLayout;
import view.LEDView;

@SuppressLint("HandlerLeak")
public class IuMainActivity extends BaseActivity implements View.OnClickListener {

	private SoftwareInfo versionInfo;
	private LEDView tv_teperm;
	private LEDView tv_hum;

	private TextView mTime;
	private TextView shebei;
	private TextView tv_sofeware_version;
	private TextView clockView;
	private boolean isRunning = false;
	private static final String DATE_FORMAT = "%02d:%02d:%02d";
	private static final String tag = "IuMainActivity";
	protected static final int GET_TEM = 0;
	private BannerLayout bannerLayout;
	private List<Integer> res;
	private List<String> titles;
	private Handler handler;
	private MachineState machineState;
	private IuMainActivity mContext;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_mains);
		initViews();
		initData();
		getMachineState();
		checkSoftVersion();
	}

	private void checkSoftVersion() {
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.GET, ConstantCmd.GET_APP_VERSION, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {

			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				Gson gson = new Gson();
				versionInfo = gson.fromJson(arg0.result, SoftwareInfo.class);
				int appVersionCode = Util.getAppVersionCode();
				if(versionInfo == null) {
					return;
				}
				if (Integer.parseInt(versionInfo.getVersionCode()) > appVersionCode) {
					downloadApk(versionInfo.getUrl());
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showRemindDialog(false);
						}
					});
				}
			}
		});
	}

	private void initViews() {
		tv_sofeware_version = (TextView) findViewById(R.id.tv_sofeware_version);
		bannerLayout = (BannerLayout) findViewById(R.id.banner1);
		clockView = (TextView) findViewById(R.id.clock);
		mTime = (TextView) findViewById(R.id.current_time);
		shebei = (TextView) findViewById(R.id.devices_num);
		tv_teperm = (LEDView) findViewById(R.id.tv_teperm);
		tv_hum = (LEDView) findViewById(R.id.tv_hum);
		shebei.setText("设备号:" + Util.getMid());

		findViewById(R.id.iv_goodsdetils_shopping).setOnClickListener(this);
		findViewById(R.id.iv_goodsdetils_take_goods).setOnClickListener(this);
		findViewById(R.id.iv_goodsdetils_return).setOnClickListener(this);

		tv_sofeware_version.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				startActivity(new Intent(IuMainActivity.this, ExitActivity.class));
				return false;
			}
		});
	}

	private void initData() {
		mContext = IuMainActivity.this;
		if (getIntent().getBooleanExtra("isToVoice", false)) {
			str2Voice("欢迎光临邻里农园鲜果智能售卖机");
		}
		if (!utils.NetworkUtils.isNetworkAvailable(IuMainActivity.this)) {
			Toast.makeText(IuMainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
		}
		tv_sofeware_version.setText("软件版本:" + Util.getVersionName());
		Logger.e(Util.getVersionName());
		handler = new Handler();
		if (bannerLayout != null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					res = new ArrayList<Integer>();
					res.add(R.drawable.viewpage_1);
					res.add(R.drawable.viewpage_2);
					res.add(R.drawable.viewpage_3);
					titles = new ArrayList<String>();
					titles.add(" ");
					titles.add(" ");
					titles.add(" ");
					bannerLayout.setViewRes(res, titles);
				}
			});
		}
	}

	private void getMachineState() {
		ThreadManager.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				machineState = MachineStateManager.getInstance().getMachineState();
				if (machineState == null) {
					// 如果为空说明机器没有反应，直接报故障就好
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showRemindDialog(true);
						}
					});
					MachineStateManager.getInstance().closeSerial();
					return;
				}
				if (machineState.getMachineStateCode() == 0x09) {
					MachineStateManager.getInstance().reportMachineState();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							showRemindDialog(true);
						}
					});
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO
						tv_teperm.setText(machineState.getTemper() + "℃   ");
						tv_hum.setText(machineState.getHumidity() + "%Rh  ");
					}
				});
				MachineStateManager.getInstance().closeSerial();
			}
		});
	}

	// 获得当前年月日时分秒星期
	public String getDateTime() {
		final Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
		String mYear = String.valueOf(c.get(Calendar.YEAR)); // 获取当前年份
		String mMonth = String.valueOf(c.get(Calendar.MONTH) + 1);// 获取当前月份
		String mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));// 获取当前月份的日期号码
		String mWay = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
		if ("1".equals(mWay)) {
			mWay = "日";
		} else if ("2".equals(mWay)) {
			mWay = "一";
		} else if ("3".equals(mWay)) {
			mWay = "二";
		} else if ("4".equals(mWay)) {
			mWay = "三";
		} else if ("5".equals(mWay)) {
			mWay = "四";
		} else if ("6".equals(mWay)) {
			mWay = "五";
		} else if ("7".equals(mWay)) {
			mWay = "六";
		}
		return mYear + "-" + String.format("%02d", Integer.parseInt(mMonth)) + "-"
				+ String.format("%02d", Integer.parseInt(mDay)) + "" + "  " + "星期" + mWay + "  ";
	}

	protected void downloadApk(String url) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
					+ "linlinySellApp.apk";
			HttpUtils httpUtils = new HttpUtils();
			httpUtils.download(url, path, new RequestCallBack<File>() {
				@Override
				public void onSuccess(ResponseInfo<File> responseInfo) {
					File file = responseInfo.result;
					Util.installBySlient(getApplicationContext(), file);
				}

				@Override
				public void onFailure(HttpException arg0, String arg1) {
					// 下载失败
					Logger.e("onFailure");
				}

				@Override
				public void onStart() {
					super.onStart();
				}

				@Override
				public void onLoading(long total, long current, boolean isUploading) {
					super.onLoading(total, current, isUploading);
				}
			});
		}
	}

	/**
	 * 安装对应apk
	 * 
	 * @param file
	 *            安装文件
	 */
	protected void installApk(File file) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
		startActivityForResult(intent, 0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void changeTvTime(int time) {
		mTime.setText(getDateTime());
		Date d = new Date();
		clockView.setText(String.format(DATE_FORMAT, d.getHours(), d.getMinutes(), d.getSeconds()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_goodsdetils_shopping:
			Intent intent = new Intent(IuMainActivity.this, GoodsDetailsActivity.class);
			intent.putExtra("title", "购      物");
			startActivity(intent);
			IuMainActivity.this.finish();
			break;
		case R.id.iv_goodsdetils_take_goods:
			Intent intent1 = new Intent(IuMainActivity.this, quhuoActitvty.class);
			intent1.putExtra("title", "取      货");
			startActivity(intent1);
			break;
		case R.id.iv_goodsdetils_return:
			Intent intent11 = new Intent(IuMainActivity.this, BasketMainActitvty.class);
			intent11.putExtra("title", "退      篮");
			startActivity(intent11);
			IuMainActivity.this.finish();
			break;
		default:
			break;
		}
	}

	public void showRemindDialog(boolean isSkip) {
		View view = View.inflate(mContext, R.layout.remind_scan_code_dialog, null);
		TextView tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvTitle.setText("机器维护，暂停服务");
		ImageView ivIcon = (ImageView) view.findViewById(R.id.iv_code);
		view.findViewById(R.id.bt_ok_membership_pay_dialog).setVisibility(View.INVISIBLE);
		view.findViewById(R.id.bt_cancel_membership_pay_dialog).setVisibility(View.INVISIBLE);
		Glide.with(mContext).load(R.drawable.warning).into(ivIcon);
		Builder builder = new AlertDialog.Builder(mContext, R.style.MyDialogStyle);
		final AlertDialog alertDialog = builder.setView(view).create();
		alertDialog.setCanceledOnTouchOutside(false);
		Util.showCustomDialog(alertDialog, IuMainActivity.this);
		alertDialog.show();
		if(isSkip) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					Util.disMissDialog(alertDialog, IuMainActivity.this);
					startActivity(new Intent(IuMainActivity.this, SplashActivity.class));
				}
			}, 5000);
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onActivityDectory() {
		if (!IuMainActivity.this.isDestroyed()) {
			IuMainActivity.this.finish();
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
}
