package activity;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android_serialport_api.sample.R;
import domain.ConstantCmd;
import utils.Util;

public class ExitActivity extends Activity {
	private EditText etName;
	private EditText etPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit_app_dialog);
		initView();
	}

	private void initView() {
		etName = (EditText) findViewById(R.id.et_name_exit_dialog);
		etPassword = (EditText) findViewById(R.id.et_password_exit_dialog);
		Button btnOk = (Button) findViewById(R.id.bt_ok_membership_exit_dialog);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = etPassword.getText().toString().trim();
				String name = etName.getText().toString().trim();
				if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(name)) {
					HttpUtils httpUtils = new HttpUtils();
					StringBuilder url = new StringBuilder(ConstantCmd.BASE_URLS).append("loginReic.json?Ruser=")
							.append(name).append("&Rpass=").append(password);
					httpUtils.send(HttpMethod.GET, url.toString(), new RequestCallBack<String>() {

						@Override
						public void onFailure(HttpException arg0, String arg1) {
							Toast.makeText(ExitActivity.this, "请重试", Toast.LENGTH_LONG).show();
						}

						@Override
						public void onSuccess(ResponseInfo<String> arg0) {
							Log.e("arg0", arg0.result.toString());
							final boolean contains = arg0.result.toString().contains("result");
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (contains) {
										Toast.makeText(ExitActivity.this, "密码或者账号错误", Toast.LENGTH_LONG).show();
									} else {
										Intent intent = getPackageManager()
												.getLaunchIntentForPackage("com.ycf.uartmaster");
										startActivity(intent);
										android.os.Process.killProcess(android.os.Process.myPid());
									}
								}
							});
						}
					});
				} else {
					Toast.makeText(ExitActivity.this, "账号或者密码不能为空", Toast.LENGTH_SHORT).show();
				}
			}
		});
		Button btnCancel = (Button) findViewById(R.id.bt_cancel_exit_dialog);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ExitActivity.this.finish();
				startActivity(new Intent(ExitActivity.this, IuMainActivity.class));
			}
		});
	}

}
