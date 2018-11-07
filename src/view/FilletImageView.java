package view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android_serialport_api.sample.R;

@SuppressLint("DrawAllocation")
public class FilletImageView extends ImageView {
	private static final float DEFAULT_RADIUS = 5;
	private float radius;
	// 圆角弧度
	private float[] rids = { radius, radius, radius, radius, 0.0f, 0.0f, 0.0f, 0.0f, };

	public FilletImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public FilletImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs, defStyleAttr);
	}

	public FilletImageView(Context context) {
		super(context);
		init(null, 0);
	}

	private void init(AttributeSet attrs, int defStyleAttr) {
		TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.fill_imageView, defStyleAttr, 0);
		radius = array.getDimension(R.styleable.fill_imageView_cornerRadius,
				utils.Util.dp2px(getContext(), DEFAULT_RADIUS));
		rids = new float[] { radius, radius, radius, radius, 0.0f, 0.0f, 0.0f, 0.0f, };
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Path path = new Path();
		int w = this.getWidth();
		int h = this.getHeight();
		// 绘制圆角imageview
		path.addRoundRect(new RectF(0, 0, w, h), rids, Path.Direction.CW);
		canvas.clipPath(path);
	}

}
