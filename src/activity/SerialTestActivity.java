package activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android_serialport_api.sample.R;
import utils.SerialManager;
import utils.SerialManager.onCallback;

public class SerialTestActivity extends Activity {

	private Button btnSend;
	private TextView tvInfo;
	private SerialManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnSend = (Button) findViewById(R.id.btn_send);
		tvInfo = (TextView) findViewById(R.id.tv_info);

		manager = SerialManager.getInstance();
		manager.setOnCallback(new onCallback() {

			@Override
			public void onResponse(byte[] response) {
				String str_cmd = "STM32 返回数据:";
				for (int i = 0; i < response.length; i++) {
					Log.e("信息", "Callback cmd[" + i + "] = " + String.format("0x%02x", (byte) response[i]));
					str_cmd = str_cmd + " " + String.format("0x%02x", (byte) response[i]);
				}
			}
		});

		btnSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				byte[] cmd_search = { 0x02, 0x05, 0x20, 0x0A, 0x02, 0x33 };
				manager.writeCmd(cmd_search, cmd_search.length);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		manager.closeSerial();
		finish();
	}
}
