package com.project.instaplan;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Bundle;

public class Chatroom extends Activity implements View.OnClickListener {

	// Instantiate ALl Public Variables Here.
	LinearLayout chatroom_layout;
	Button chatroom_post_button;
	TextView chatroom_entered_post_textView, chatroom_new_post, chatroom_title;
	Intent incomming_intent=new Intent();
	String tag = "MJ------>";
	IntentFilter intentFilter;
	TextView chatroom_new_sms;
	ClassEvent event = new ClassEvent("Untitled Event", "", "", "", "");

	NotificationManager nm;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO
		Log.i(tag, "Starting Chatroom");
		setContentView(R.layout.layout_chatroom);
		initializeAllVariables();
		populateChatroom();
		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");

		// Give the rest of the functions.
		chatroom_post_button.setOnClickListener(this);
	}

	public void populateChatroom() {
		// TODO Auto-generated method stub
		Log.i(tag, "Populating ChatRoom");
		incomming_intent=new Intent();
		incomming_intent = getIntent();		
		if(incomming_intent.hasExtra("withNotification")){
			Log.i(tag, "Cancelling the notification");
			nm = (NotificationManager)  getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(1234567855);
		}
		if (incomming_intent.hasExtra("Title")) {
			String title = incomming_intent.getStringExtra("Title").toString();
			Log.i(tag, "Key Title was found!! as " + title);
			if (ClassUniverse.universeEventLookUp.containsKey(title)) {
				Log.i(tag, "Matching Event Found, Now populating Chat room.");
				event = ClassUniverse.universeEventLookUp.get(title);
				chatroom_title.setText(event.title);
				event.externalUpdateCount = 0;
				for (String post : event.chat_log) {
					chatroom_new_post = new TextView(this);
					chatroom_new_post
							.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
					chatroom_new_post.setText(post);
					chatroom_layout.addView(chatroom_new_post, 0);
				}
			}
		} else {
			Log.i(tag, "Key Title Not Found");
		}
	}

	public void initializeAllVariables() {
		chatroom_layout = (LinearLayout) findViewById(R.id.chatroom_layout);
		chatroom_post_button = (Button) findViewById(R.id.chatroom_post_button);
		chatroom_entered_post_textView = (TextView) findViewById(R.id.chatroom_entered_post_textView);
		chatroom_title = (TextView) findViewById(R.id.chatroom_textView_title);
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.chatroom_post_button:
			// Enter code for this button.
			Log.i(tag, " ChatRoom Post button pressed! ");
			if (chatroom_entered_post_textView.getText().toString().length() != 0) {
				String title = chatroom_title.getText().toString();
				Log.i(tag, "Event title: " + title);
				if (ClassUniverse.universeEventLookUp.containsKey(title)) {
					Log.i(tag, "Event is a registered Event");
					event = (ClassEvent) ClassUniverse.universeEventLookUp
							.get(title);
					if (event.hasUpdate()) {
						updateChatroom(event);
						Log.i(tag, "Event Had Off-line updates!");
					}
					String post_content = (String) "Me: "
							+ chatroom_entered_post_textView.getText()
									.toString();
					Log.i(tag, "Posted Content: " + post_content);

					event.updateChatLog("internal", post_content);
					chatroom_new_post = new TextView(this);
					chatroom_new_post
							.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
					chatroom_new_post.setText(post_content);
					chatroom_layout.addView(chatroom_new_post, 0);
					spreadPost(event, chatroom_entered_post_textView.getText()
							.toString());
					chatroom_entered_post_textView.setText("");
				} else {
					Log.i(tag, "Event is NOT a registered Event");
					String post_content = (String) "Me: "
							+ chatroom_entered_post_textView.getText()
									.toString();
					chatroom_new_post = new TextView(this);
					chatroom_new_post
							.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
					chatroom_new_post.setText(post_content);
					chatroom_layout.addView(chatroom_new_post, 0);
					chatroom_entered_post_textView.setText("");
				}
			}

			// setContentView(chatroom_layout)
			break;

		// ADD MORE button CASESE:
		}
	}

	private void spreadPost(ClassEvent event, String post_content) {
		// TODO Auto-generated method stub
		Log.i(tag, "Now in Spread Post");
		post_content.replaceAll("%", "percent");
		for (ClassPeople invitee : event.invited) {
			Log.i(tag, "Invitee "+ invitee.name+" hasApp: "+ invitee.hasApp);
			SmsManager sms = SmsManager.getDefault();
			if (invitee.hasApp){
			sms.sendTextMessage(invitee.phoneNumber, null, post_content+" /%"+event.title+"%/", null,
					null);
			Log.i(tag, "Sent Sms to: " + invitee.name+ " WITH TAG");
			}
			else{
				sms.sendTextMessage(invitee.phoneNumber, null, post_content, null,
						null);
				Log.i(tag, "Sent Sms to: " + invitee.name + " WITHOUT TAG");
			}
		}
	}

	private void updateChatroom(ClassEvent event) {
		// TODO Auto-generated method stub
		if (event.hasUpdate()) {
			for (int i = event.chatroom_size - event.externalUpdateCount; i < event.chatroom_size; i++) {
				chatroom_new_post = new TextView(this);
				chatroom_new_post.setLayoutParams(new ViewGroup.LayoutParams(
						-1, -2));
				chatroom_new_post.setText(event.chat_log.get(i));
				chatroom_layout.addView(chatroom_new_post, 0);
			}
			event.externalUpdateCount = 0;
		}
	}

	public BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().containsKey(("smsFor" + event.title))) {
				initializeAllVariables();
				chatroom_new_post = new TextView(chatroom_layout.getContext());
				chatroom_new_post.setLayoutParams(new ViewGroup.LayoutParams(
						-1, -2));
				chatroom_new_post.setText(intent.getExtras().getString(
						"smsFor" + event.title));
				chatroom_layout.addView(chatroom_new_post, 0);
				event.externalUpdateCount--;
			}
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		registerReceiver(intentReceiver, intentFilter);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO
		unregisterReceiver(intentReceiver);
		super.onPause();
		finish();
	}

}
