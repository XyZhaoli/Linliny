package activity;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest.HttpMethod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android_serialport_api.sample.R;

public class TestActivity extends Activity {

	private AlertDialog alertDialog;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		HttpUtils httpUtils = new HttpUtils();
		RequestParams params = new RequestParams();
		params.addBodyParameter("Fcode", "0x09");
		params.addBodyParameter("Fname", "0x08");
		params.addBodyParameter("Fcontent", "参考故障名称");
		params.addBodyParameter("Fresolve", "暂无");
		params.addBodyParameter("Funwound", "否");
		params.addBodyParameter("Mid", utils.Util.getMid());
		String Url = "http://linliny.com.cn/AddFailure.json?Fcode=0x09&Fname=0x08&Fcontent=参考故障名称&Fresolve=暂无&Funwound=0&Mid=64";
		httpUtils.send(HttpMethod.POST, Url, new RequestCallBack<String>() {

			@Override
			public void onFailure(HttpException arg0, String arg1) {
				// TODO Auto-generated method stub
				Log.e("onFailure", arg1);

			}

			@Override
			public void onSuccess(ResponseInfo<String> arg0) {
				// TODO Auto-generated method stub
				Log.e("onSuccess", arg0.result.toString());

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
