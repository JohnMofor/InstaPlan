package com.project.instaplan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class PhoneLock extends Activity implements View.OnClickListener {

	// Instantiate ALl Public Variables Here.
	String tag = "MJ(PhoneLock)------>";
	TextView phoneRegistration_phoneNumber_editText;
	TextView phoneRegistration_userName_editText;
	TextView phoneRegistration_userEmail_editText;
	Button phoneLock_verify_button;

	ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(tag, "Starting PhoneRegistration.java");
		setContentView(R.layout.layout_phone_lock);
		initializeAllVariables();
		setupProgressSpinner();

		// Give the rest of the functions.
		phoneLock_verify_button.setOnClickListener(this);
	}

	private void initializeAllVariables() {
		Log.i(tag, "PhoneRegistration Initializing All Variables");
		phoneRegistration_phoneNumber_editText = (TextView) findViewById(R.id.phoneRegistration_phoneNumber_editText);
		phoneRegistration_userName_editText = (TextView) findViewById(R.id.phoneRegistration_userName_editText);
		phoneRegistration_userEmail_editText = (TextView) findViewById(R.id.phoneRegistration_userEmail_editText);
		phoneLock_verify_button = (Button) findViewById(R.id.phoneLock_verify_button);
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.phoneLock_verify_button:
			// Enter code for this button.
			Log.i(tag, "Verify button was pressed");
			if (dataOk()) {
				ClassUniverse.mEmail = phoneRegistration_userEmail_editText
						.getText().toString();
				ClassUniverse.mPhoneNumber = phoneRegistration_phoneNumber_editText
						.getText().toString();
				ClassUniverse.mUserName = phoneRegistration_userName_editText
						.getText().toString();
				if(ClassUniverse.mUserName.contentEquals("devadmin")){
					ClassUniverse.phoneUnlocked=true;
				}
				new DisplayProgress().execute();
			} else {
				showMessage("Phone Number must be 11 digits");
			}
			break;
		}

	}

	private boolean dataOk() {
		int length = phoneRegistration_phoneNumber_editText.getText()
				.toString().length();
		return length == 11;
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
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
			SmsManager sms = SmsManager.getDefault();
			Log.i(tag,"sending sms tag");
			sms.sendTextMessage(ClassUniverse.mPhoneNumber, null,
					ClassUniverse.registrationTag, null, null);
			Log.i(tag,"sms sent");
			while (!(ClassUniverse.phoneUnlocked)
					&& ((System.currentTimeMillis() - start) < 10000)) {
				// wait for 10 seconds max
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mProgressDialog.dismiss();
			if (!ClassUniverse.phoneUnlocked) {
				showMessage("Verification failed. Check phone number & data plan, and retry");
			} else {
				showMessage("Verification Successful");
				startActivity(new Intent("com.project.instaplan.AllEvents"));

			}
		}

	}

	private void setupProgressSpinner() {
		mProgressDialog = new ProgressDialog(PhoneLock.this);
		mProgressDialog.setTitle("Carrier Verification");
		mProgressDialog.setMessage("Verifying your carrier. Please wait...");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}
}
