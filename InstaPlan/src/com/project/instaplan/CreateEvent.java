package com.project.instaplan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import com.google.android.gcm.GCMRegistrar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class CreateEvent extends Activity implements View.OnClickListener {

	// Instantiate ALl Public Variables Here.
	String logTag = "MJ(Create Event)------>";
	String eventCode = null;

	TextView createEvent_eventTitle_textView;
	TextView createEvent_eventTitle_editText;
	TextView createEvent_eventTime_textView;

	TextView createEvent_eventDate_textView;

	TextView createEvent_eventLocation_textView;
	TextView createEvent_eventLocation_editText;
	TextView createEvent_eventDescription_textView;
	TextView createEvent_eventDescription_editText;
	CheckBox createEvent_facebookStatus_checkBox;
	CheckBox createEvent_enable_gcm_checkBox;

	Button createEvent_invite_local_contact_button;
	Button createEvent_eventDate_datePicker;
	Button createEvent_eventTime_timePicker;
	// Button createEvent_invite_facebook_friend_button;
	// Button createEvent_invite_non_local_contact_button;
	Button createEvent_done_button, createEvent_toggle_gcm_button;

	final static int GetContactsResultCode = 100;
	boolean sessionHasInternet = false;

	ClassEvent createdEvent;

	ArrayList<String> names = new ArrayList<String>();
	ArrayList<String> phoneNumbers = new ArrayList<String>();
	ArrayList<String> allInputss = new ArrayList<String>(5);
	HashMap<String, String> allInputs = new HashMap<String, String>();
	String[] allInputsOrder = { "Title", "Time", "Date", "Location",
			"Description" };
	ArrayList<TextView> allTextViews = new ArrayList<TextView>(5);
	Intent launchGetContacts;
	final static int contactData = 0;
	final String[] DAYS = { "Monday", "Tuesday", "Wednesday", "Thursday",
			"Friday", "Saturday", "Sunday" };
	static String MONTHS[] = { "January", "February", "March", "April", "May",
			"June", "July", "August", "September", "October", "November",
			"December" };
	
	String inviteeString="";
	int ERROR_RESULT_CODE2 = 999;
	int ERROR_RESULT_CODE1 = 666;
	final int DATE_DIALOG = 123;
	final int TIME_DIALOG = 321;
	final int GUEST_LIST = 111;
	int year, month, day, hour, minute, seconds;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(logTag, "Starting CreateEvent.java");
		setContentView(R.layout.layout_create_event);
		initializeAllVariables();
		getSessionInfo();
		setClickListeners();
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.createEvent_invite_local_contact_button:
			// Enter code for this button.
			Log.i(logTag, "Invite_local_contact_button was pressed");
			if (dataOk(false)) {
				doLocalContactCode();
			}
			break;

		// case R.id.createEvent_invite_facebook_friend_button:
		// // Enter code for this button.
		// Log.i(logTag, "Invite_facebook_friend_button was pressed");
		// if (dataOk(false)) {
		// doFacebookFriendCode();
		// }
		// break;
		// case R.id.createEvent_invite_non_local_contact_button:
		// // Enter code for this button.
		// Log.i(logTag, "Invite_non_local_contact_button was pressed");
		// if (dataOk(false)) {
		// doNonLocalContactCode();
		// }
		// break;
		case R.id.createEvent_done_button:
			// Enter code for this button.
			Log.i(logTag, "Done_button was pressed");
			if (dataOk(true)) {
				doDoneCode();
			}
			break;

		case R.id.createEvent_toggle_gcm_button:
			startActivity(new Intent("com.project.instaplan2.PhoneRegistration"));
			break;

		case R.id.createEvent_eventDate_datePicker:
			showDialog(DATE_DIALOG);
			break;
		case R.id.createEvent_eventTime_timePicker:
			showDialog(TIME_DIALOG);
			break;
			
		
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG:
			final Time c = new Time();
			c.setToNow();
			year = c.year;
			month = c.month;
			day = c.monthDay;
			return new DatePickerDialog(this, datePickerListener, year, month,
					day);

		case TIME_DIALOG:
			final Time now = new Time();
			now.setToNow();
			hour = now.hour;
			minute = now.minute;
			return new TimePickerDialog(this, timePickerListener, hour, minute,
					true);
		}
		return super.onCreateDialog(id);
	}

	private void setClickListeners() {
		createEvent_invite_local_contact_button.setOnClickListener(this);
		createEvent_eventDate_datePicker.setOnClickListener(this);
		createEvent_eventTime_timePicker.setOnClickListener(this);
		// createEvent_invite_facebook_friend_button.setOnClickListener(this);
		// createEvent_invite_non_local_contact_button.setOnClickListener(this);
		createEvent_done_button.setOnClickListener(this);
		createEvent_toggle_gcm_button.setOnClickListener(this);
	}

	private void getSessionInfo() {
		// ---------Getting session Info

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
				showMessage("Turn on Wifi for cheaper (and better?) performance\n");
				sessionHasInternet = true;
			}
		}
		// ----Phone Number&ID----
		if (ClassUniverse.mPhoneNumber.equals("")) {
			ClassUniverse.device_id = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID);
		}
		Log.i(logTag, "REFERENCES: DEVICEID: " + ClassUniverse.device_id
				+ " PhoneNumber: " + ClassUniverse.mPhoneNumber);
		return;
	}

	private void initializeAllVariables() {
		Log.i(logTag, "CreateEvent.java Initializing All Variables");
		createEvent_eventTitle_textView = (TextView) findViewById(R.id.createEvent_eventTitle_textView);
		createEvent_eventTitle_editText = (TextView) findViewById(R.id.createEvent_eventTitle_editText);
		createEvent_eventDate_datePicker = (Button) findViewById(R.id.createEvent_eventDate_datePicker);
		createEvent_eventTime_timePicker = (Button) findViewById(R.id.createEvent_eventTime_timePicker);
		createEvent_eventTime_textView = (TextView) findViewById(R.id.createEvent_eventTime_textView);

		createEvent_eventDate_textView = (TextView) findViewById(R.id.createEvent_eventDate_textView);

		createEvent_eventLocation_textView = (TextView) findViewById(R.id.createEvent_eventLocation_textView);
		createEvent_eventLocation_editText = (TextView) findViewById(R.id.createEvent_eventLocation_editText);
		createEvent_eventDescription_textView = (TextView) findViewById(R.id.createEvent_eventDescription_textView);
		createEvent_eventDescription_editText = (TextView) findViewById(R.id.createEvent_eventDescription_editText);
		createEvent_facebookStatus_checkBox = (CheckBox) findViewById(R.id.createEvent_facebookStatus_checkBox);
		allTextViews.add(createEvent_eventTitle_textView);
		allTextViews.add(createEvent_eventTime_textView);
		allTextViews.add(createEvent_eventDate_textView);
		allTextViews.add(createEvent_eventLocation_textView);
		allTextViews.add(createEvent_eventDescription_textView);

		createEvent_toggle_gcm_button = (Button) findViewById(R.id.createEvent_toggle_gcm_button);
		createEvent_invite_local_contact_button = (Button) findViewById(R.id.createEvent_invite_local_contact_button);
		// createEvent_invite_facebook_friend_button = (Button)
		// findViewById(R.id.createEvent_invite_facebook_friend_button);
		// createEvent_invite_non_local_contact_button = (Button)
		// findViewById(R.id.createEvent_invite_non_local_contact_button);
		createEvent_done_button = (Button) findViewById(R.id.createEvent_done_button);
		createEvent_facebookStatus_checkBox.bringToFront();
	}

	private Boolean dataOk(boolean done) {
		//
		boolean ok = true;
		allInputs.clear();
		allInputs.put("Title", createEvent_eventTitle_editText.getText()
				.toString());
		allInputs.put("Time", createEvent_eventTime_timePicker.getText()
				.toString());
		allInputs.put("Date", createEvent_eventDate_datePicker.getText()
				.toString());
		allInputs.put("Location", createEvent_eventLocation_editText.getText()
				.toString());
		allInputs.put("Description", createEvent_eventDescription_editText
				.getText().toString());

		for (int i = 0; i < 5; i++) {
			Log.i(logTag,
					"currently at i = " + i + " Looking at: "
							+ allInputs.get(allInputsOrder[i]));
			if ((allInputs.get(allInputsOrder[i]).length()) == 0) {
				allTextViews.get(i).setTextColor(Color.RED);
				ok = false;
			} else {
				allTextViews.get(i).setTextColor(Color.WHITE);
			}
		}
		if (createEvent_eventTime_timePicker.getText().toString()
				.equals("Set Time")) {
			createEvent_eventTime_textView.setTextColor(Color.RED);
			ok = false;
		}
		if (createEvent_eventDate_datePicker.getText().toString()
				.equals("Set Date")) {
			createEvent_eventDate_textView.setTextColor(Color.RED);
			ok = false;
		}
		if (!ok) {
			Toast.makeText(getApplicationContext(),
					"Please Fill All Fields In Red", Toast.LENGTH_SHORT).show();
			return false;
		} else {
			if (done) {
				// Toast.makeText(getApplicationContext(),
				// allInputs.get("Title") + " Created", Toast.LENGTH_SHORT)
				// .show();
				return true;
			} else {
				// Toast.makeText(getApplicationContext(), "Select Contacts",
				// Toast.LENGTH_SHORT).show();
				return true;
			}
		}

	}

	private void doDoneCode() {
		if (createTheEvent()) {
			Toast.makeText(getApplicationContext(),
					allInputs.get("Title") + " Created", Toast.LENGTH_SHORT)
					.show();
			Log.i(logTag, "User entered a display name: "
					+ ClassUniverse.mUserName);
			if (phoneNumbers.size() > 0) {
				for (String phoneNumber : phoneNumbers) {
					createdEvent.invite(ClassUniverse.universePhoneNumberLookUp
							.get(phoneNumber));
				}
			}
			createdEvent.host = (new ClassPeople("Me", "phoneNumber",
					ClassUniverse.mPhoneNumber));
			String post = sendInitialSms();
			new SpreadPosts().execute(post);
			finish();
		}
	}

	private String sendInitialSms() {
		String initialPost = "NEW EVENT!";
		initialPost += " Title: " + createdEvent.title;
		initialPost += ", Desc: " + createdEvent.description;
		initialPost += ", Time: " + createdEvent.time;
		initialPost += ", Date: " + createdEvent.date;
		initialPost += ", Loc: " + createdEvent.location;
		initialPost += ", PS: No InstaPlan? Add %E"
				+ Integer.toString(createdEvent.creationNumber)
				+ "% in replies";
		return initialPost;
	}

	public class SpreadPosts extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String initialPost = params[0];
			if (gcmEnabled() && sessionHasInternet) {
				Log.i(logTag, "Good. GCM is enabled on device");
				for (ClassPeople invitee: createdEvent.invited){
					inviteeString+=invitee.name+"%--%"+invitee.phoneNumber+"<br>";
				}
				createdEvent.serverIdCode = generateEventId(initialPost);
				for (ClassPeople invitee : createdEvent.invited) {
					if ((invitee.hasGCM)
							&& (sendGcmCommand(initialPost, invitee.phoneNumber) == HttpURLConnection.HTTP_OK)) {
						Log.i(logTag, "GCM sent succefully from: "
								+ invitee.name);
					} else {
						SmsManager sms = SmsManager.getDefault();
						sms.sendTextMessage(invitee.phoneNumber, null,
								initialPost, null, null);
						Log.i(logTag, "Intro Sms Sent to: " + invitee.name);
					}
				}
			} else {
				Log.i(logTag, "BAD. GCM is NOT enabled on device");
				for (ClassPeople invitee : createdEvent.invited) {

					SmsManager sms = SmsManager.getDefault();
					sms.sendTextMessage(invitee.phoneNumber, null, initialPost,
							null, null);
					Log.i(logTag, "Intro Sms Sent to: " + invitee.name);
				}
			}

			return null;

		}

		private boolean gcmEnabled() {
			try {
				Context context = getApplicationContext();
				Log.i(logTag,
						"Checking if gcm Enabled: " + ClassUniverse.GCMEnabled
								+ " &&: "
								+ GCMRegistrar.getRegistrationId(context)
								+ " equalls empty?");
				return !(GCMRegistrar.getRegistrationId(context).equals(""));
			} catch (Exception e) {
				// e.printStackTrace();
				Log.i(logTag, "Device didn't support gcm");
				return false;
			}
		}

	}

	private String generateEventId(String initialPost) {
		URL url;
		Log.i(logTag, "generating EventIdCode");
		String strUrl = "http://mj-server.mit.edu/instaplan/registerEvent/?parameters="
				+ URLEncoder.encode(initialPost)
				+ "&hostDeviceId="
				+ ClassUniverse.device_id
				+"&invitees="
				+ URLEncoder.encode(inviteeString);
		try {
			// String myUrl = URLEncoder.encode(strUrl, "UTF-8");
			// url = new URL(myUrl);
			url = new URL(strUrl);

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setConnectTimeout(3000);
			urlConnection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			String generatedEventCode = reader.readLine();
			Log.i(logTag, "Generated key: " + generatedEventCode);
			reader.close();
			urlConnection.disconnect();
			return generatedEventCode;
		} catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING GCM... MalFormedUrl");
			return "Error";
		} catch (IOException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING GCM... IOException..");
			return "Error";
		}
	}

	public int sendGcmCommand(String content, String TophoneNumber) {
		URL url;
		String strUrl = "http://mj-server.mit.edu/instaplan/command/"
				+ createdEvent.serverIdCode + "/?command=sendSmsTo"
				+ TophoneNumber + "&content=" + content + "&hostDeviceId="
				+ ClassUniverse.device_id + "&sender_phoneNumber="
				+ ClassUniverse.mPhoneNumber;
		Log.i(logTag, "Executing this URL: " + strUrl);
		try {
			// String myUrl = URLEncoder.encode(strUrl, "UTF-8");
			// url = new URL(myUrl);
			url = new URL(strUrl);

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			Log.i(logTag, "connection created!");
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			int out = urlConnection.getResponseCode();
			String server_reply = urlConnection.getResponseMessage();
			urlConnection.disconnect();
			if (out == 200) {
				Log.i(logTag, "Successfully Sent sms: code " + out);
			} else {
				Log.i(logTag, "Sms Delivery failed: code " + out + " reply: "
						+ server_reply);
			}
			return out;
		} catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING GCM... MalFormedUrl");
			return ERROR_RESULT_CODE1;
		} catch (IOException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING GCM... IOEX..");
			return ERROR_RESULT_CODE2;
		}
	}

	private boolean createTheEvent() {
		//
		createdEvent = new ClassEvent(allInputs.get("Title"),
				allInputs.get("Location"), allInputs.get("Description"),
				allInputs.get("Time"), allInputs.get("Date"));
		createdEvent.isFacebookEnabled=createEvent_facebookStatus_checkBox.isChecked();
		createdEvent.makeHost(new ClassPeople(ClassUniverse.mUserName,
				"phoneNumber", ClassUniverse.mPhoneNumber));
		createdEvent.isMine = true;
		if (ClassUniverse.registerEvent(createdEvent)) {
			return true;
		} else {
			showMessage("ERROR: Event with title: " + allInputs.get("Title")
					+ " already exists.");
			return false;
		}

	}

	// private void doNonLocalContactCode() {
	// //
	// }
	//
	// private void doFacebookFriendCode() {
	// //
	//
	// }

	private void doLocalContactCode() {
		launchGetContacts = new Intent("com.project.instaplan2.GetContacts");
		Log.i(logTag, "Going into GetContacts");
		startActivityForResult(launchGetContacts, GetContactsResultCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(logTag, "Currently in On Activity Result");
		Log.i(logTag, "REQ Code: " + requestCode + " RES Code: " + resultCode
				+ " Data: " + data);
		if (resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			names = (ArrayList<String>) extras.getStringArrayList("names");
			phoneNumbers = (ArrayList<String>) extras
					.getStringArrayList("phoneNumbers");
			Log.i(logTag, "Now Printing names gotten!");
			for (String test : names) {
				Log.i(logTag, "Obtained Resolved: " + test);
			}
		} else {
			Log.i(logTag, "RESULT NOOOOTT OK!!!");
		}
	}

	@Override
	protected void onPause() {
		//
		super.onPause();
	}

	public class RegisterPhone extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			registerPhone();
			return null;
		}

	}

	public void registerPhone() {
		URL url;
		String userName = ClassUniverse.mUserName;
		if (ClassUniverse.mUserName.equals("NotFixed")) {
			userName = ClassUniverse.mPhoneNumber;
		}
		String strUrl = "http://mj-server.mit.edu/instaplan/registerPhone/"
				+ "?phoneNumber=" + ClassUniverse.mPhoneNumber + "&username="
				+ userName + "&dev_id=" + ClassUniverse.device_id + "&reg_id="
				+ ClassUniverse.regId;
		try {
			// String myUrl = URLEncoder.encode(strUrl, "UTF-8");
			// url = new URL(myUrl);
			url = new URL(strUrl);

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			int out = urlConnection.getResponseCode();
			String server_reply = urlConnection.getResponseMessage();
			urlConnection.disconnect();
			if (out == 200) {
				Log.i(logTag, "Successfully Updated device: code " + out);
			} else {
				Log.i(logTag, "Device Update Failed: code " + out + " reply: "
						+ server_reply);
			}
			return;
		} catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING GCM... MalFormedUrl");
			return;
		} catch (IOException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING GCM... IOEX..");
			return;
		}
	}

	private void showMessage(String message) {
		Toast myToast;
		myToast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_LONG);
		myToast.show();
	}

	private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {

		// when dialog box is closed, below method will be called.

		@SuppressLint("SimpleDateFormat")
		@Override
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {

			year = selectedYear;
			month = selectedMonth;
			day = selectedDay;
			String input_date = day-2+"/"+month+1+"/"+year;
			SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy",Locale.US);
			Date dt1;
			try {
				dt1 = format1.parse(input_date);
				DateFormat format2 = new SimpleDateFormat("EEEE");
				String literal_day = format2.format(dt1);
				String surfix = "th";
				if(day%10==1){
					surfix = "st";
				}else if(day%10==2){
					surfix="nd";
				}else if(day%10==3){
					surfix="rd";
				}
				String out = literal_day+", "+day+surfix+" "+MONTHS[month]+" "+year;
				createEvent_eventDate_datePicker.setText(out);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				createEvent_eventDate_datePicker.setText(month+1+"-"+day+"-"+year);
			}			

		}
	};

	private OnTimeSetListener timePickerListener = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker arg0, int hourSelected,
				int minuteSelected) {
			// TODO Auto-generated method stub
			hour = hourSelected;
			minute = minuteSelected;
			createEvent_eventTime_timePicker.setText(new StringBuilder()
					.append(hour).append(": ").append(minute));
		}
	};

}
