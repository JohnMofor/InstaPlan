package com.project.instaplan;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class CreateEvent extends Activity implements View.OnClickListener {

	// Instantiate ALl Public Variables Here.
	String logTag = "MJ------>";
	TextView createEvent_eventTitle_textView;
	TextView createEvent_eventTitle_editText;
	TextView createEvent_eventTime_textView;
	TextView createEvent_eventTime_editText;
	TextView createEvent_eventDate_textView;
	TextView createEvent_eventDate_editText;
	TextView createEvent_eventLocation_textView;
	TextView createEvent_eventLocation_editText;
	TextView createEvent_eventDescription_textView;
	TextView createEvent_eventDescription_editText;
	CheckBox createEvent_facebookStatus_checkBox;
	Button createEvent_invite_local_contact_button;
	Button createEvent_invite_facebook_friend_button;
	Button createEvent_invite_non_local_contact_button;
	Button createEvent_done_button;
	final static int GetContactsResultCode = 100;

	ClassEvent createdEvent;

	ArrayList<String> names = new ArrayList<String>();
	ArrayList<String> allInputss = new ArrayList<String>(5);
	HashMap<String, String> allInputs = new HashMap<String, String>();
	String[] allInputsOrder = { "Title", "Time", "Date", "Location",
			"Description" };
	ArrayList<TextView> allTextViews = new ArrayList<TextView>(5);
	Intent launchGetContacts;

	final static int contactData = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(logTag, "Starting CreateEvent.java");
		setContentView(R.layout.layout_create_event);
		initializeAllVariables();

		// Give the rest of the functions.
		// TODO
		createEvent_invite_local_contact_button.setOnClickListener(this);
		createEvent_invite_facebook_friend_button.setOnClickListener(this);
		createEvent_invite_non_local_contact_button.setOnClickListener(this);
		createEvent_done_button.setOnClickListener(this);
	}

	private void initializeAllVariables() {
		Log.i(logTag, "CreateEvent.java Initializing All Variables");
		createEvent_eventTitle_textView = (TextView) findViewById(R.id.createEvent_eventTitle_textView);
		createEvent_eventTitle_editText = (TextView) findViewById(R.id.createEvent_eventTitle_editText);
		createEvent_eventTime_textView = (TextView) findViewById(R.id.createEvent_eventTime_textView);
		createEvent_eventTime_editText = (TextView) findViewById(R.id.createEvent_eventTime_editText);
		createEvent_eventDate_textView = (TextView) findViewById(R.id.createEvent_eventDate_textView);
		createEvent_eventDate_editText = (TextView) findViewById(R.id.createEvent_eventDate_editText);
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

		createEvent_invite_local_contact_button = (Button) findViewById(R.id.createEvent_invite_local_contact_button);
		createEvent_invite_facebook_friend_button = (Button) findViewById(R.id.createEvent_invite_facebook_friend_button);
		createEvent_invite_non_local_contact_button = (Button) findViewById(R.id.createEvent_invite_non_local_contact_button);
		createEvent_done_button = (Button) findViewById(R.id.createEvent_done_button);
		createEvent_facebookStatus_checkBox.bringToFront();
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

		case R.id.createEvent_invite_facebook_friend_button:
			// Enter code for this button.
			Log.i(logTag, "Invite_facebook_friend_button was pressed");
			if (dataOk(false)) {
				doFacebookFriendCode();
			}
			break;
		case R.id.createEvent_invite_non_local_contact_button:
			// Enter code for this button.
			Log.i(logTag, "Invite_non_local_contact_button was pressed");
			if (dataOk(false)) {
				doNonLocalContactCode();
			}
			break;
		case R.id.createEvent_done_button:
			// Enter code for this button.
			Log.i(logTag, "Done_button was pressed");
			if (dataOk(true)) {
				doDoneCode();
			}
			break;

		}
	}

	private Boolean dataOk(boolean done) {
		// TODO Auto-generated method stub
		boolean ok = true;
		allInputs.clear();
		allInputs.put("Title", createEvent_eventTitle_editText.getText()
				.toString());
		allInputs.put("Time", createEvent_eventTime_editText.getText()
				.toString());
		allInputs.put("Date", createEvent_eventDate_editText.getText()
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
		if (!ok) {
			Toast.makeText(getApplicationContext(),
					"Please Fill All Fields In Red", Toast.LENGTH_SHORT).show();
			return false;
		} else {
			if (done) {
				Toast.makeText(getApplicationContext(),
						allInputs.get("Title") + " Created", Toast.LENGTH_SHORT)
						.show();
				return true;
			} else {
				Toast.makeText(getApplicationContext(), "Select Contacts",
						Toast.LENGTH_SHORT).show();
				return true;
			}
		}

	}

	private void doDoneCode() {
		// TODO Auto-generated method stub
		createTheEvent();
		if (names.size() > 0) {
			for (String name : names) {
				ClassPeople person = ClassUniverse.universeNameLookUp.get(name);
				createdEvent.invite(person);
			}
		}
		sendInitialSms();
		finish();
	}

	private void sendInitialSms() {
		String initialPost = "New event. ";
		initialPost += "Title: /%" + createdEvent.title + "%/ ";
		initialPost += "Desc: /%" + createdEvent.description + "%/ ";
		initialPost += "Time: /%" + createdEvent.time + "%/ ";
		initialPost += "Date: /%" + createdEvent.date + "%/ ";
		initialPost += "Loc: /%" + createdEvent.location + "%/ ";
		initialPost += "PS: No InstaPlan? Add /%E" + createdEvent.eventCode
				+ "%/ in replies";
		for (ClassPeople invitee : createdEvent.invited) {
			SmsManager sms = SmsManager.getDefault();
			sms.sendTextMessage(invitee.phoneNumber, null, initialPost, null,
					null);
			Log.i(logTag, "Intro Sms Sent to: " + invitee.name);
		}
		// TODO Auto-generated method stub

	}

	private void createTheEvent() {
		// TODO Auto-generated method stub
		createdEvent = new ClassEvent(allInputs.get("Title"),
				allInputs.get("Location"), allInputs.get("Description"),
				allInputs.get("Time"), allInputs.get("Date"));
		ClassUniverse.createEvent(createdEvent);
	}

	private void doNonLocalContactCode() {
		// TODO Auto-generated method stub

	}

	private void doFacebookFriendCode() {
		// TODO Auto-generated method stub

	}

	private void doLocalContactCode() {
		launchGetContacts = new Intent("com.project.instaplan.GetContacts");
		Log.i(logTag, "Going into GetContacts");
		startActivityForResult(launchGetContacts, GetContactsResultCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(logTag, "Currently in On Activity Result");
		Log.i(logTag, "REQ Code: " + requestCode + " RES Code: " + resultCode
				+ " Data: " + data);
		if (resultCode == RESULT_OK) {
			Bundle extras = data.getExtras();
			names = (ArrayList<String>) extras.getStringArrayList("names");
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
		// TODO
		super.onPause();
	}
}
