package com.project.instaplan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class Intro extends Activity {
	String tag = "MJ(Intro)------>";
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.i(tag, "In Intro");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_intro);
		
		final ImageView image = (ImageView) findViewById(R.id.introImage);
		Animation fadeIn = new AlphaAnimation(0, 1);
		fadeIn.setInterpolator(new DecelerateInterpolator()); //add this
		fadeIn.setDuration(2000);

		Animation fadeOut = new AlphaAnimation(1, 0);
		fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
		fadeOut.setStartOffset(2000);
		fadeOut.setDuration(2000);

		AnimationSet animation = new AnimationSet(false); //change to false
		animation.addAnimation(fadeIn);
		animation.addAnimation(fadeOut);
		image.startAnimation(animation);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				if (ClassUniverse.phoneUnlocked) {
					startActivity(new Intent("com.project.instaplan.AllEvents"));
				} else {
					startActivity(new Intent("com.project.instaplan.PhoneLock"));
				}
			}
		}, 3750);

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}
