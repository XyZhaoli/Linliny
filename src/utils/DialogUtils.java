package utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android_serialport_api.sample.R;

public class DialogUtils {
	
	
	
	/**
	 * 这是整个APP中的AlertDialog的一个模板，后面显示的自定义的弹出对话框，都是以此为模板
	 * 
	 * @param message栏中所要显示的内容
	 */
//	public void showAlertDialog(Context context, String titleName, String str) {
//		final AlertDialog alertDialog;
//		View alertDialogView = View.inflate(context, R.layout.get_device_addr, null);
//		TextView tv_title = (TextView) alertDialogView.findViewById(R.id.tv_title1);
//		TextView tv_info = (TextView) alertDialogView.findViewById(R.id.tv_info);
//		Button bt_ok = (Button) alertDialogView.findViewById(R.id.bt_ok);
//		tv_title.setText(titleName);
//		tv_info.setText(str);
//		bt_ok.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				alertDialog.dismiss();
//			}
//		});
//		alertDialog = new AlertDialog.Builder(context).setView(alertDialogView).create();
//		alertDialog.show();
//	}
}
