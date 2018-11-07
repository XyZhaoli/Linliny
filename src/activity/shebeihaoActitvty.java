package activity;

import org.apache.http.conn.ConnectTimeoutException;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android_serialport_api.MyAdapter;
import android_serialport_api.bb;
import android_serialport_api.sample.R;
import utils.ActivityManager;
import utils.VoiceUtils;

public class shebeihaoActitvty extends Activity {

	private static final int GET_DDEVICES_NUM = 1;
	private String Mid = new String();
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

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj == null) {
				Log.e("TAG", "data is null");
			} else {
				parseDevicesNum((String) msg.obj);
			}
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sebeihao);
		showNumberKeyboard();
		ActivityManager.getInstance().addActivity(shebeihaoActitvty.this);
		// 点击EditText禁止出现数字键盘
		EditText shebeiId = (EditText) findViewById(R.id.editText1);
		shebeiId.setInputType(InputType.TYPE_NULL);
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
		number_enter = (TextView) findViewById(R.id.number_enter);// 重输
		number_clear_last = (ImageView) findViewById(R.id.number_clear_last);// 删除
		myCourse_roomId_input = (EditText) findViewById(R.id.editText1);// 文本

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
		Button sure = (Button) findViewById(R.id.btn_enter_devices_num_activity);
		sure.setOnClickListener(new View.OnClickListener() {
			// @Override
			public void onClick(View v) {
				Mid = myCourse_roomId_input.getText().toString();
				if (!TextUtils.isEmpty(Mid)) {
					new Thread() {
						public void run() {
							String shebei;
							try {
								shebei = bb.getHttpResult(
										"http://linliny.com/dingyifeng_web/ByMidQueryMachine1.json?Mid=" + Mid);
								Message message = Message.obtain();
								message.obj = shebei;
								handler.sendMessage(message);
							} catch (ConnectTimeoutException e) {
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										utils.Util.DisplayToast(mContext, "网络错误，请重试", R.drawable.warning);
										VoiceUtils.getInstance().initmTts(mContext, "网络错误，请重试");
									}
								});
								e.printStackTrace();
							}
						};
					}.start();
				} else {
					Toast.makeText(getApplication(), "您输入的设备为空，请重新输入", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
