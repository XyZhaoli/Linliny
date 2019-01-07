package dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android_serialport_api.ZXingUtils;
import android_serialport_api.sample.R;
import utils.ThreadManager;
import utils.Util;

public class LinliUrlQrDialog extends Dialog {

	private ImageView ima;
	private MyHandler handler;

	@SuppressLint("HandlerLeak")
	class MyHandler extends Handler {
		public void dispatchMessage(android.os.Message msg) {
			ima.setImageBitmap((Bitmap) msg.obj);
		};
	}

	public LinliUrlQrDialog(Context context) {
		super(context);
	}

	public LinliUrlQrDialog(Context context, int theme) {
		super(context, theme);
	}

	public LinliUrlQrDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basket_huiyuan);
		handler = new MyHandler();
		ima = (ImageView) findViewById(R.id.ima);
		findViewById(R.id.iv_cancel_dialog).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LinliUrlQrDialog.this.dismiss();
			}
		});
	}

}
