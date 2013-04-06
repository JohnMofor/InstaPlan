package com.project.instaplan;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class DialogActivity extends Activity {

	TextView tvMsg;
	TextView tvTitle;
	String LOG_TAG = "DialogAcitivty";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "DialogActivity OnCreate!");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_dialog);

		getWindow().setLayout(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		tvMsg = (TextView) findViewById(R.id.tvMsg);
		tvTitle = (TextView) findViewById(R.id.tvTitle);

		tvTitle.setText("GCM");

		Bundle extras = getIntent().getExtras();
		String msg = extras.getString("msg");
		tvMsg.setText(msg);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}
	
}
