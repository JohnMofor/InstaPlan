package com.project.instaplan;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;

public class PhoneRegistration extends Activity implements View.OnClickListener {

	// Instantiate ALl Public Variables Here.
	String tag = "MJ(PhoneRegistration)------>";
	TextView phoneRegistration_phoneNumber_editText;
	TextView phoneRegistration_userName_editText;
	TextView phoneRegistration_userEmail_editText;
	Button phoneRegistration_register_button;
	Button phoneRegistration_unregister_button;

	ProgressDialog mProgressDialog;

	boolean sessionHasInternet;

	int ERROR_RESULT_CODE2 = 999;
	int ERROR_RESULT_CODE1 = 666;
	public static String GCM_SENDER = "981817883739";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(tag, "Starting PhoneRegistration.java");
		setContentView(R.layout.layout_phone_registration);
		initializeAllVariables();
		getSessionInfo();
		reloadPreviousValues();
		setupProgressSpinner();

		// Give the rest of the functions.
		phoneRegistration_register_button.setOnClickListener(this);
	}

	private void reloadPreviousValues() {
		if (ClassUniverse.regId.equals("")) {
			phoneRegistration_register_button.setText("Register device");
		} else {
			phoneRegistration_register_button.setText("Unregister device");
		}
		if(!ClassUniverse.mPhoneNumber.equals("")){
			phoneRegistration_phoneNumber_editText.setText(ClassUniverse.mPhoneNumber);
		}
		if(!ClassUniverse.mEmail.equals("")){
			phoneRegistration_userEmail_editText.setText(ClassUniverse.mEmail);
		}
		if(!ClassUniverse.mUserName.equals("")){
			phoneRegistration_userName_editText.setText(ClassUniverse.mUserName);
		}
	}

	private void initializeAllVariables() {
		Log.i(tag, "PhoneRegistration Initializing All Variables");
		phoneRegistration_phoneNumber_editText = (TextView) findViewById(R.id.phoneRegistration_phoneNumber_editText);
		phoneRegistration_userName_editText = (TextView) findViewById(R.id.phoneRegistration_userName_editText);
		phoneRegistration_userEmail_editText = (TextView) findViewById(R.id.phoneRegistration_userEmail_editText);
		phoneRegistration_register_button = (Button) findViewById(R.id.phoneRegistration_register_button);
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.phoneRegistration_register_button:
			// Enter code for this button.
			Log.i(tag, "Registration button was pressed");
			if (ClassUniverse.regId.equals("")) {
				Log.i(tag, "User requested to unregister");
				if (dataOk()) {
					ClassUniverse.mEmail=phoneRegistration_userEmail_editText.getText().toString();
					ClassUniverse.mPhoneNumber=phoneRegistration_phoneNumber_editText.getText().toString();
					ClassUniverse.mUserName=phoneRegistration_userName_editText.getText().toString();
					GCMRegistrar.register(getApplicationContext(), GCM_SENDER);
					new DisplayProgress().execute();
				} else {
					showMessage("Invalid Phone Number");
				}
				break;
			}else{
				Log.i(tag, "User requested to unregister");
				GCMRegistrar.unregister(getApplicationContext());
				phoneRegistration_register_button.setText("Register");
				showMessage("Unregistration request sent.");
			}
		}
	}

	private boolean dataOk() {
		int length = phoneRegistration_phoneNumber_editText.getText()
				.toString().length();
		return ((length > 9) && (length < 12));
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	private void getSessionInfo() {
		String statusReport = "";
		// ---------Getting session Info ---------

		// ----Internet State----
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
			sessionHasInternet = true;
		} else {
			NetworkInfo mMobile = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobile.isConnected()) {
				sessionHasInternet = true;
			}
		}
		if (!sessionHasInternet) {
			showMessage("Must have access to the internet");
			finish();
		}

		// ----Phone Number&ID----
		if (ClassUniverse.device_id.equals("")) {
			ClassUniverse.device_id = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID);
		}
		Log.i(tag, "REFERENCES: DEVICEID: " + ClassUniverse.device_id);

		// ----GCM-State----
		Context context = getApplicationContext();
		try {
			GCMRegistrar.checkDevice(this);
			ClassUniverse.GCMPossible = true;
			ClassUniverse.regId = GCMRegistrar.getRegistrationId(context);
		} catch (UnsupportedOperationException e) {
			ClassUniverse.GCMPossible = false;
			ClassUniverse.GCMEnabled = false;
			showMessage("Device does not support GCM");
			Log.i(tag, "Device Not GCM enabled!");
			finish();
			return;
		}

		if (!statusReport.equals("")) {
			showMessage(statusReport);
		}
		return;
	}

	private void showMessage(String message) {
		Toast myToast;
		myToast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_LONG);
		myToast.show();
	}

	public class DisplayProgress extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			Long start = System.currentTimeMillis();
			while(((System.currentTimeMillis()-start)<3000)){
				
			}
			Log.i(tag, "Registration time: " + Long.toString(System.currentTimeMillis()-start));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (!ClassUniverse.GcmRegError.equals("")) {
				mProgressDialog.dismiss();
				showMessage(ClassUniverse.GcmRegError);
			} else {
				phoneRegistration_register_button.setText("Unregister Device");
				showMessage("Registration Success!");				
				finish();
			}
		}

	}

	private void setupProgressSpinner() {
		mProgressDialog = new ProgressDialog(PhoneRegistration.this);
		mProgressDialog.setTitle("GCM Registration");
		mProgressDialog.setMessage("Registering your device for use with GCM.");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
}
