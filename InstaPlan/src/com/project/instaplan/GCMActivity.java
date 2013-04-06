package com.project.instaplan;

import android.app.Activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class GCMActivity extends Activity {

	// http://marakana.com/forums/android/examples/41.html
	protected String regId;
	private Context context = null;
	public static String GCM_SENDER = "981817883739";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_dialog_main);
		
		if (ClassUniverse.mPhoneNumber == null) {
			ClassUniverse.device_id = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID);
			Log.i("In GCMAcitivity ", "DEVICEID: "+ClassUniverse.device_id+" phoneNumber: "+ClassUniverse.mPhoneNumber);
		}

		context = getApplicationContext();

		try {
			GCMRegistrar.checkDevice(this);
		} catch (UnsupportedOperationException e) {
			Log.i("MJ--->GCMActivity",
					"Phone doesn't support GCM: " + e.toString());
			showMessage("Your Device Does Not Support GCM");
			finish();
		}

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (!(mWifi.isConnected())) {
			NetworkInfo mMobile = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobile.isConnected()) {
				showMessage("Turn on Wifi for cheaper (and better?) performance");
			} else {
				showMessage("Niether Wifi nor 3G connectivity detected. Cannot launch GCM");
				finish();
			}
		}
		
		
		ClassUniverse.regId = GCMRegistrar.getRegistrationId(context);
		regId=ClassUniverse.regId;

		Button serviceButton = (Button) findViewById(R.id.bService);
		serviceButton.setOnClickListener(buttonListener);

		if (regId.equals("")) {
			serviceButton.setText("Register device");
		} else {
			serviceButton.setText("Unregister device");
		}
	}

	private final OnClickListener buttonListener = new OnClickListener() {
		public void onClick(View v) {
			ClassUniverse.GCMEnabled = true;
			if (regId.equals("")) {
				GCMRegistrar.register(context, GCM_SENDER);
				ClassUniverse.GCMEnabled = true;
				showMessage("Device should be successfully registered");
			} else {
				GCMRegistrar.unregister(context);
				showMessage("Device should be successfully unregistered");
			}
			finish();
		}
	};

	private void showMessage(String message) {
		Toast myToast;
		myToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		myToast.show();
	}
	
}
