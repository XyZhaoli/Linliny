package activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android_serialport_api.sample.R;

public class TestActivity extends Activity {

	private ImageView ivArrow;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_seemore);
		ivArrow = (ImageView) findViewById(R.id.iv_arrow_dialog_seemore);

		ObjectAnimator animatorTop = ObjectAnimator.ofFloat(ivArrow, "translationY", -100);
		ObjectAnimator animatorButtom = ObjectAnimator.ofFloat(ivArrow, "translationY", 0);
		AnimatorSet set = new AnimatorSet();
		set.play(animatorTop).before(animatorButtom);
		set.setDuration(1000);
		set.start();
	}

}
