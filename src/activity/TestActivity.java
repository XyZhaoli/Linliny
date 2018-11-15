package activity;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android_serialport_api.sample.R;
import utils.VoiceUtils;

public class TestActivity extends Activity {

	private AlertDialog alertDialog;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = TestActivity.this;
		setContentView(R.layout.dialog_seemore);
		findViewById(R.id.iv_arrow_dialog_seemore).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTestDialog();
			}
		});
	}

	private void showTestDialog() {
		View view = View.inflate(mContext, R.layout.menbership_card_pay_dialog, null);
		Button btnConfirmPayfor = (Button) view.findViewById(R.id.bt_ok_membership_pay_dialog);
		Button btnCancel = (Button) view.findViewById(R.id.bt_cancel_membership_pay_dialog);
		TextView tvPrice = (TextView) view.findViewById(R.id.tv_price_membership_pay_dialog);
		final EditText etPassword = (EditText) view.findViewById(R.id.et_password_membership_pay_dialog);
		btnConfirmPayfor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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

}
