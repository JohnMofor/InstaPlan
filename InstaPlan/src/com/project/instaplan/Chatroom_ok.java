package com.project.instaplan;
//package com.project.instaplan2;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLEncoder;
//
//import com.google.android.gcm.GCMRegistrar;
//import android.view.View;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TabHost;
//import android.widget.TextView;
//import android.widget.Toast;
//import android.widget.TabHost.TabSpec;
//import android.app.ListActivity;
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.provider.Settings.Secure;
//import android.telephony.SmsManager;
//import android.util.Log;
//
//public class Chatroom_ok extends ListActivity implements View.OnClickListener {
//
//	// Instantiate ALl Public Variables Here.
//	String logTag = "MJ------>";
//	TabHost tabHost;
//
//	// From Chatroom
//	// Instantiate ALl Public Variables Here.
//	LinearLayout chatroom_layout;
//	ListView chatroom_listView;
//
//	Button chatroom_post_button;
//
//	TextView chatroom_entered_post_textView;
//	TextView chatroom_new_post;
//	TextView chatroom_new_sms;
//
//	AwesomeAdapter adapter;
//
//	Intent incomming_intent = new Intent();
//	IntentFilter intentFilter;
//
//	String tag = "MJ(Chatroom)------>";
//
//	ClassEvent event = new ClassEvent("Untitled Event", "", "", "", "");
//
//	NotificationManager nm;
//	int ERROR_RESULT_CODE2 = 999;
//	int ERROR_RESULT_CODE1 = 666;
//	boolean sessionHasInternet = false;
//	boolean valid;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		Log.i(logTag, "Starting Chatroom_ok");
//		setContentView(R.layout.layout_chatroom_ok);
//		initializeAllVariables();
//		valid = populateChatroom();
//		Log.i(tag, "Generating mini broadcast receiver");
//		intentFilter = new IntentFilter();
//		intentFilter.addAction("SMS_RECEIVED_ACTION");
//		Log.i(tag, "Done setting up filter");
//		getSessionInfo();
//	}
//
//	// Give the rest of the functions.
//	// buttons.setOnclickListener(this);
//
//	private void setting_up_tab() {
//		Log.i(logTag, "In setting up tab");
//
//		// Tab 1.
//		Log.i(logTag, "Tab 1 start!");
//		TabSpec specs = tabHost.newTabSpec("chatroomTab");
//		specs.setContent(R.id.chatroom_tab);
//		specs.setIndicator("Chatroom");
//		tabHost.addTab(specs);
//
//		// Tab 2.
//		Log.i(logTag, "Tab 2 start!");
//		specs = tabHost.newTabSpec("doodle");
//		specs.setContent(R.id.doodle_tab);
//		specs.setIndicator("Doodle");
//		tabHost.addTab(specs);
//
//		// Tab 3.
//		Log.i(logTag, "Tab 3 start!");
//		specs = tabHost.newTabSpec("map");
//		specs.setContent(R.id.map_tab);
//		specs.setIndicator("Map");
//		tabHost.addTab(specs);
//
//		// Tab 4.
//		Log.i(logTag, "Tab 4 start!");
//		specs = tabHost.newTabSpec("event_guest_list");
//		specs.setContent(R.id.event_guestList_tab);
//		specs.setIndicator("Event Guest List");
//		tabHost.addTab(specs);
//
//		// Tab 5.
//		Log.i(logTag, "Tab 5 start!");
//		specs = tabHost.newTabSpec("settings");
//		specs.setContent(R.id.settings_tab);
//		specs.setIndicator("Settings");
//		tabHost.addTab(specs);
//
//	}
//
//	private void initializeAllVariables() {
//		Log.i(logTag, "Chatroom_ok Initializing All Variables");
//		tabHost = (TabHost) findViewById(R.id.tabhost);
//		tabHost.setup();
//		setting_up_tab();
//		chatroom_listView = getListView();
//		chatroom_post_button = (Button) findViewById(R.id.chatroom_post_button);
//		chatroom_entered_post_textView = (TextView) findViewById(R.id.chatroom_entered_post_textView);
//		
//		Log.i(tag, "Done with Initialization of all variables!");
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//	
//	private void getSessionInfo() {
//		Log.i(tag, "Getting Session Info");
//		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//		NetworkInfo mWifi = connManager
//				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//		if (mWifi.isConnected()) {
//			sessionHasInternet = true;
//			Log.i(tag, "Session has Wifi... Good!");
//		} else {
//			NetworkInfo mMobile = connManager
//					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//			if (mMobile.isConnected()) {
//				Log.i(tag, "Session has No wifi, but 3G only!");
//				showMessage("Turn on Wifi for cheaper (and better?) performance");
//				sessionHasInternet = true;
//			} else {
//				Log.i(tag, "Session has No connectivity!");
//			}
//		}
//		Log.i(tag, "Done getting session Info");
//
//		Log.i(tag, "Now informing user about phone status");
//		if (!sessionHasInternet) {
//			showMessage("Not Internet texting");
//		} else {
//			Log.i(tag, "Session has internet, now fetching eventIdCode if new");
//			if (event.eventIdCode == null) {
//				Log.i(tag, "INDEED was new!");
//				if (valid) {
//					new GetEventId().execute(event.host.phoneNumber,
//							event.title);
//				} else {
//					Log.i(tag, "Invalid Event... No getting code.");
//				}
//
//			} else {
//				Log.i(tag, "We already knew this eventId");
//			}
//		}
//		if (ClassUniverse.mPhoneNumber.equals("")) {
//			ClassUniverse.device_id = Secure.getString(getApplicationContext()
//					.getContentResolver(), Secure.ANDROID_ID);
//		}
//		Log.i(tag, "REFERENCES: DEVICEID: " + ClassUniverse.device_id
//				+ " PhoneNumber: " + ClassUniverse.mPhoneNumber);
//
//		// Give the rest of the functions.
//		if ((valid) && !(event.title.equals("Your GCM Inbox"))) {
//			chatroom_post_button.setOnClickListener(this);
//		} else {
//			chatroom_entered_post_textView.setEnabled(false);
//			chatroom_entered_post_textView.setText("No Posts Allowed");
//		}
//		if (ClassUniverse.GCMPossible) {
//			ClassUniverse.regId = GCMRegistrar
//					.getRegistrationId(getApplicationContext());
//		}
//	}
//
//	public boolean populateChatroom() {
//		Log.i(tag, "Starting Populating Chatroom!");
//		boolean out = false;
//		incomming_intent = new Intent();
//		incomming_intent = getIntent();
//
//		if (incomming_intent.hasExtra("withNotification")) {
//			Log.i(tag, "Cancelling the notification");
//			nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//			nm.cancel(1234567855);
//			Log.i(tag, "Done cancelling the notification");
//		}
//
//		if (incomming_intent.hasExtra("Title")) {
//			String title = incomming_intent.getStringExtra("Title").toString();
//			Log.i(tag, "Incomming intent had title: " + title);
//			if (ClassUniverse.universeEventLookUp.containsKey(title)) {
//				Log.i(tag, "Event found, now populating Chatroom.");
//				out = true;
//				event = ClassUniverse.universeEventLookUp.get(title);
//				event.externalUpdateCount = 0;
//				adapter = new AwesomeAdapter(this, event.messages);
//				
//				setListAdapter(adapter);
//			}
//		} else {
//			Log.i(tag, "Event not found on this device. No adapter required..");
//		}
//
//		Log.i(tag, "Done with Populating Chatroom!");
//		return out;
//	}
//
//
//	public void onClick(View viewClicked) {
//		switch (viewClicked.getId()) {
//
//		case R.id.chatroom_post_button:
//			// Enter code for this button.
//			Log.i(tag, " Chatroom Post button pressed! ");
//			if (chatroom_entered_post_textView.getText().toString().length() != 0) {
//				String title = event.title;
//				Log.i(tag, "Event title: " + title);
//				if (ClassUniverse.universeEventLookUp.containsKey(title)) {
//					Log.i(tag, "Event found, now updating event");
//					if (event.hasUpdate()) {
//						Log.i(tag,
//								"Event had off-line updates, now clearing them");
//						updateChatroom(event);
//					}
//					String entered_text = chatroom_entered_post_textView
//							.getText().toString().trim();
//
//					Log.i(tag, "Posted Content: " + entered_text);
//
//					event.updateChatLog("internal", entered_text, "Me");
//					adapter.notifyDataSetChanged();
//
//					chatroom_entered_post_textView.setText("");
//					getListView().setSelection(event.messages.size() - 1);
//					spreadPost(entered_text);
//
//				} else {
//					Log.i(tag, "Event is NOT a registered Event");
//					chatroom_entered_post_textView.setText("");
//				}
//			}
//			break;
//
//		// ADD MORE button CASESE:
//		}
//	}
//
//	private void spreadPost(String post_content) {
//		// Auto-generated method stub
//		Log.i(tag, "Now in Spread Main Ui Post");
//		// post_content.replaceAll("%", "percent");
//		// Auto-generated method stub
//
//		String post_content_appless = post_content;
//		String post_content_app = post_content_appless + " /-%" + event.title
//				+ "%-/";
//		// post_content.replaceAll("%", "percent");
//		if (sessionHasInternet && ClassUniverse.GCMEnabled) {
//			Log.i(tag, "Session has both internet and GCMEnabled");
//			for (ClassPeople invitee : event.invited) {
//				Log.i(tag, "Invitee " + invitee.name + " hasApp: "
//						+ invitee.hasApp);
//				SmsManager sms = SmsManager.getDefault();
//				if (invitee.hasApp) {
//					if (invitee.hasGCM) {
//						Log.i(tag, "Trying to send GCM to " + invitee.name);
//						new SendGCMCommand().execute(invitee.phoneNumber,
//								invitee.name, post_content_app);
//						continue;
//					} else {
//						Log.i(tag, invitee.name + "Has No GCM");
//						sms.sendTextMessage(invitee.phoneNumber, null,
//								post_content_app, null, null);
//					}
//				} else {
//					sms.sendTextMessage(invitee.phoneNumber, null,
//							post_content_appless, null, null);
//					Log.i(tag, "Sent Sms to: " + invitee.name
//							+ " without tags.");
//				}
//			}
//			return;
//		} else {
//			Log.i(tag, "No wifi or google account... sending sms's");
//			for (ClassPeople invitee : event.invited) {
//				SmsManager sms = SmsManager.getDefault();
//				if (invitee.hasApp) {
//					Log.i(tag, "Invitee " + invitee.name + " hasApp: "
//							+ invitee.hasApp);
//					sms.sendTextMessage(invitee.phoneNumber, null,
//							post_content_app, null, null);
//					Log.i(tag, "Sent Sms to: " + invitee.name + " with tags.");
//				} else {
//					sms.sendTextMessage(invitee.phoneNumber, null,
//							post_content_appless, null, null);
//					Log.i(tag, "Sent Sms to: " + invitee.name
//							+ " without tags.");
//				}
//			}
//		}
//		return;
//	}
//
//	private void updateChatroom(ClassEvent event) {
//		// Auto-generated method stub
//		if (event.hasUpdate()) {
//			adapter.notifyDataSetChanged();
//			event.externalUpdateCount = 0;
//		}
//	}
//
//	public BroadcastReceiver intentReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent.getExtras().containsKey(("smsFor" + event.title))) {
//				initializeAllVariables();
//				adapter.notifyDataSetChanged();
//				getListView().setSelection(event.messages.size() - 1);
//				event.externalUpdateCount--;
//			}
//		}
//	};
//
//	@Override
//	protected void onResume() {
//		// Auto-generated method stub
//		registerReceiver(intentReceiver, intentFilter);
//		super.onResume();
//		getListView().setSelection(event.messages.size() - 1);
//	}
//
//	
//
//	public class SendGCMCommand extends AsyncTask<String, Void, Integer> {
//
//		String TophoneNumber = "";
//		String content = "";
//		String receiverName = "";
//
//		@Override
//		protected void onPostExecute(Integer result) {
//			super.onPostExecute(result);
//			if (result == HttpURLConnection.HTTP_OK) {
//				Log.i(tag, "Successfully sent GCM Message to: " + receiverName);
//			} else {
//				Log.i(tag, "Failed to send GCM Message to: " + receiverName);
//				SmsManager sms = SmsManager.getDefault();
//				sms.sendTextMessage(TophoneNumber, null, content, null, null);
//			}
//		}
//
//		@Override
//		protected Integer doInBackground(String... params) {
//			URL url;
//			TophoneNumber = params[0];
//			content = params[2];
//			receiverName = params[1];
//
//			String strUrl = "http://mj-server.mit.edu/instaplan/command/"
//					+ event.eventIdCode + "/?command=sendSmsTo"
//					+ URLEncoder.encode(TophoneNumber) + "&content="
//					+ URLEncoder.encode(content) + "&hostDeviceId="
//					+ URLEncoder.encode(ClassUniverse.device_id)
//					+ "&sender_phoneNumber="
//					+ URLEncoder.encode(ClassUniverse.mPhoneNumber);
//			try {
//				url = new URL(strUrl);
//
//				HttpURLConnection urlConnection = (HttpURLConnection) url
//						.openConnection();
//				urlConnection.setRequestMethod("GET");
//				urlConnection.setConnectTimeout(3000);
//				urlConnection.connect();
//				int out = urlConnection.getResponseCode();
//				String server_reply = urlConnection.getResponseMessage();
//				urlConnection.disconnect();
//				if (out == 200) {
//					Log.i(tag, "Successfully Sent GCM sms: code " + out);
//				} else {
//					Log.i(tag, "GCM Delivery failed: code " + out + ", reply: "
//							+ server_reply);
//				}
//				return out;
//			} catch (MalformedURLException e) {
//				// Auto-generated catch block
//				// e.printStackTrace();
//				Log.i(tag, "ERROR SENDING GCM... MalFormedUrl");
//				return ERROR_RESULT_CODE1;
//			} catch (IOException e) {
//				// Auto-generated catch block
//				// e.printStackTrace();
//				Log.i(tag, "ERROR SENDING GCM... IOEX..");
//				return ERROR_RESULT_CODE2;
//			}
//		}
//
//	}
//
//	private void showMessage(String message) {
//		Toast myToast;
//		myToast = Toast.makeText(getApplicationContext(), message,
//				Toast.LENGTH_SHORT);
//		myToast.show();
//	}
//
//	public class GetEventId extends AsyncTask<String, Void, String> {
//
//		@Override
//		protected String doInBackground(String... params) {
//			// Auto-generated method stub
//			return getEventId(params[0], params[1]);
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			// Auto-generated method stub
//			super.onPostExecute(result);
//			if (!result.equals("Error")) {
//				event.eventIdCode = result;
//				Log.i(tag, "EventIdCode: " + result);
//			}
//			Log.i(tag, "Just set Event's ID code :) " + result);
//		}
//
//	}
//
//	private String getEventId(String host_phoneNumber, String event_title) {
//		URL url;
//		Log.i(tag, "generating EventIdCode");
//		String strUrl = "http://mj-server.mit.edu/instaplan/getEventId/?hostPhoneNumber="
//				+ URLEncoder.encode(host_phoneNumber)
//				+ "&eventTitle="
//				+ URLEncoder.encode(event_title);
//		try {
//			url = new URL(strUrl);
//			HttpURLConnection urlConnection = (HttpURLConnection) url
//					.openConnection();
//			urlConnection.setRequestMethod("GET");
//			urlConnection.connect();
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					urlConnection.getInputStream()));
//			String generatedEventCode = reader.readLine();
//			Log.i(tag, "Generated key: " + generatedEventCode);
//			reader.close();
//			urlConnection.disconnect();
//			return generatedEventCode;
//		} catch (MalformedURLException e) {
//			// Auto-generated catch block
//			// e.printStackTrace();
//			Log.i(tag, "ERROR SENDING GCM... MalFormedUrl");
//			return "Error";
//		} catch (IOException e) {
//			// Auto-generated catch block
//			// e.printStackTrace();
//			Log.i(tag, "ERROR SENDING GCM... IOException..");
//			return "Error";
//		}
//	}
//
//}
