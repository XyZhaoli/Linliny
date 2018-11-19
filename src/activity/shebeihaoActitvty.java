package activity;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.sample.R;
import utils.ActivityManager;
import utils.VoiceUtils;

public class shebeihaoActitvty extends Activity implements OnClickListener {

	private static final int GET_DDEVICES_NUM = 1;
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
	private Context mContext = shebeihaoActitvty.this;
	public MyHandler handler = new MyHandler();
	private String Mid;

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_DDEVICES_NUM:
				parseDevicesNum((String) msg.obj);
				break;

			default:
				break;
			}
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sebeihao);
		initView();
		initData();
	}

	private void initData() {
		ActivityManager.getInstance().addActivity(shebeihaoActitvty.this);
		// 点击EditText禁止出现数字键盘
		EditText shebeiId = (EditText) findViewById(R.id.editText1);
		shebeiId.setInputType(InputType.TYPE_NULL);
	}

	private void initView() {
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

		number_clear_last = (ImageView) findViewById(R.id.number_clear_last);// 删除
		myCourse_roomId_input = (EditText) findViewById(R.id.editText1);// 文本

		findViewById(R.id.btn_enter_devices_num_activity).setOnClickListener(this);
		findViewById(R.id.btn_cancel_devices_num_activity).setOnClickListener(this);

		// 长按删除键
		number_clear_last.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				myCourse_roomId_input.setText("");
				return false;
			}
		});
	}

	@SuppressLint("NewApi")
	public void parseDevicesNum(String devicesNum) {
		// 设备号存在
		if (!TextUtils.isEmpty(devicesNum)) {
			SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
			Editor editor = sharedPreferences.edit();
			editor.putBoolean("shebeihao1", true);
			editor.putString("Mid", Mid);
			editor.commit();
			startActivity(new Intent(shebeihaoActitvty.this, IuMainActivity.class));
			shebeihaoActitvty.this.finish();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(shebeihaoActitvty.this);
			builder.setTitle("提示").setMessage("您输入的设备号不存在").create().show();
		}
	}

	@Override
	public void onClick(View v) {
		String midStr = myCourse_roomId_input.getText().toString();
		switch (v.getId()) {
		case R.id.number_1:
			myCourse_roomId_input.setText(midStr + number_1.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_2:
			myCourse_roomId_input.setText(midStr + number_2.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_3:
			myCourse_roomId_input.setText(midStr + number_3.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_4:
			myCourse_roomId_input.setText(midStr + number_4.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_5:
			myCourse_roomId_input.setText(midStr + number_5.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_6:
			myCourse_roomId_input.setText(midStr + number_6.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_7:
			myCourse_roomId_input.setText(midStr + number_7.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_8:
			myCourse_roomId_input.setText(midStr + number_8.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_9:
			myCourse_roomId_input.setText(midStr + number_9.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_0:
			myCourse_roomId_input.setText(midStr + number_0.getText().toString());
			myCourse_roomId_input.setSelection(midStr.length() + 1);
			break;
		case R.id.number_enter:
			myCourse_roomId_input.setText("");
			break;
		case R.id.number_clear_last:
			if (midStr.length() > 0) {
				myCourse_roomId_input.setText(midStr.substring(0, midStr.length() - 1));
				myCourse_roomId_input.setSelection(midStr.length() - 1);
			}
			break;
		case R.id.btn_enter_devices_num_activity:
			if (!TextUtils.isEmpty(midStr)) {
				checkMidNum(midStr);
			} else {
				Toast.makeText(getApplication(), "您输入的设备为空，请重新输入", Toast.LENGTH_LONG).show();
			}
			break;
		default:

			break;
		}
	}

	private void checkMidNum(String midStr) {
		String url = "http://linliny.com/dingyifeng_web/ByMidQueryMachine1.json?Mid=" + midStr;
		Log.e("url", url);
		Mid = midStr;
		HttpUtils httpUtils = new HttpUtils();
		httpUtils.send(HttpMethod.GET, url, new RequestCallBack<String>() {
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
						VoiceUtils.getInstance().initmTts( "网络错误，请重试");
					}
				});
			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				if (!TextUtils.isEmpty(arg0.result)) {
					utils.Util.sendMessage(handler, GET_DDEVICES_NUM, arg0.result);
				}
			}
		});
	}
}
