package activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android_serialport_api.sample.R;

public class ExitActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.exit_app_dialog);
		initView();
	}

	private void initView() {
		final EditText etPassword = (EditText) findViewById(R.id.et_password_exit_dialog);
		Button btnOk = (Button) findViewById(R.id.bt_ok_membership_exit_dialog);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String password = etPassword.getText().toString().trim();
				if (!TextUtils.isEmpty(password) && password.equals("123")) {
					Intent intent = getPackageManager().getLaunchIntentForPackage("com.ycf.uartmaster");
					startActivity(intent);
					android.os.Process.killProcess(android.os.Process.myPid());
				} else {
					Toast.makeText(ExitActivity.this, "密码错误", Toast.LENGTH_SHORT).show();
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
