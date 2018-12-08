package dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android_serialport_api.sample.R;

public class MembershipDialog extends Dialog {

	public MembershipDialog(Context context) {
		super(context);
	}

	public MembershipDialog(Context context, int theme) {
		super(context, theme);
	}

	public MembershipDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menbership_card_pay_dialog);
		initView();
		initData();
	}

	private void initView() {
		Button btnConfirmPayfor = (Button) findViewById(R.id.bt_ok_membership_pay_dialog);
		Button btnCancel = (Button) findViewById(R.id.bt_cancel_membership_pay_dialog);
		TextView tvPrice = (TextView) findViewById(R.id.tv_price_membership_pay_dialog);
		final EditText etPassword = (EditText) findViewById(R.id.et_password_membership_pay_dialog);
	}

	private void initData() {
		
	}

}
