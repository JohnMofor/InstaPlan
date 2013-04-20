package com.project.instaplan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
//import java.util.List;
import com.google.android.gcm.GCMRegistrar;
//import com.google.android.gms.maps.CameraUpdate;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.Marker;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.project.instaplan.MyLocation.LocationResult;

import android.view.LayoutInflater;
//import android.view.Menu;
import android.view.View;
//import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.TabSpec;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.util.Log;

public class Chatroom_ok extends FragmentActivity implements
//		View.OnClickListener, OnItemClickListener, OnMapLongClickListener { // uncomment & delete line below. if you support google maps
	View.OnClickListener, OnItemClickListener{

	// Instantiate ALl Public Variables Here.
	String logTag = "MJ------>";
	TabHost tabHost;

	// Chatroom aka "Wall" Variables
	// -------------------------------------------------
	// Instantiate ALl Public Variables Here.
	LinearLayout chatroom_layout;
	ListView chatroom_listView;

	Button chatroom_post_button;

	TextView chatroom_entered_post_textView;
	TextView chatroom_new_post;
	TextView chatroom_title;
	TextView chatroom_new_sms;
	

	AwesomeAdapter adapter;

	Intent incomming_intent = new Intent();
	IntentFilter intentFilter;

	String tag = "MJ(Chatroom)------>";

	ClassEvent event = new ClassEvent("Empty Event 999", "", "", "", "");

	NotificationManager nm;

	int eventHash;
	int ERROR_RESULT_CODE2 = 999;
	final int SHOW_GUEST_LIST = 231;
	final int EVENT_INFO = 232;
	final int SEARCH_OPTIONS = 992;
	int ERROR_RESULT_CODE1 = 666;
	int GCM_SERVER_RESPONSE_WAIT_TIME = 3000;
	boolean sessionHasInternet = false;
	boolean valid;

	// Event Info Slider Variables
	// -------------------------------------------------
	int key = 0;
	Button slider_button;
	Button chatroom_ok_slider_popout_button;
	Button chatroom_ok_showEventInfo_button;
	Button chatroom_ok_showEventGuestList_button;
	Button chatroom_ok_showMenu_button;
	TransparentPanel popup;
	Dialog eventInfoDialog;

	// Guest List Variables
	// ------------------------------------------------
	ListView create_event_guestList;
	ArrayAdapter<String> guestList_adapter;

	// Google Maps Variables (uncomment if you have google maps.
	// ------------------------------------------------
	
	/*
	public GoogleMap mMap;
	public EditText addr;
	public Button searchMap, directions, track_button;
	public Marker info;
	*/
	
	// Doodle Variables
	// ------------------------------------------------
	
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(logTag, "Starting Chatroom_ok");
		setContentView(R.layout.layout_chatroom_ok);
		initializeAllVariables();
		valid = populateChatroom();
		Log.i(tag, "Generating mini broadcast receiver");
		intentFilter = new IntentFilter();
		intentFilter.addAction("SMS_RECEIVED_ACTION");
		Log.i(tag, "Done setting up filter");
		getSessionInfo();
	}

	private void getGuestListInBackGround(String serverIdCode) {
		Log.i(tag, "This is the server code I will be trying to fetch: "
				+ serverIdCode);
		new GetGuestList().execute(serverIdCode);
	}

	private void initializeAllVariables() {
		initializeTab();
		initializeChatroomVariables();
		initializeSliderVariables();
//		initializeAllMapVariables();

	}
	
	/* Google maps.
	private void initializeAllMapVariables() {
		setUpMapIfNeeded();
		addr = (EditText) findViewById(R.id.etAddress);
		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		searchMap = (Button) findViewById(R.id.button1);
		directions = (Button) findViewById(R.id.buttonDir);
		track_button = (Button) findViewById(R.id.track);
		searchMap.setOnClickListener(this);
		directions.setOnClickListener(this);
		track_button.setOnClickListener(this);
		setUpMap(event.location, true);

	}
	*/

	private void initializeGuestList() {
		Log.i(logTag, "Guest Room Initializing All Variables");
		create_event_guestList = (ListView) findViewById(R.id.create_event_guestList);
		guestList_adapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, 0);
		for (String name : event.guestList.get(0)) {
			guestList_adapter.add(name);
			guestList_adapter.notifyDataSetChanged();
		}
		create_event_guestList.setAdapter(guestList_adapter);
		create_event_guestList.setOnItemClickListener(this);

	}

	private void initializeTab() {
		tabHost = (TabHost) findViewById(R.id.tabhost);
		tabHost.setup();
		tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
		setting_up_tab();
	}

	private void initializeChatroomVariables() {
		Log.i(logTag, "Chatroom_ok Initializing All Variables");
		chatroom_listView = (ListView) findViewById(R.id.chatroom_ok_listview);
		chatroom_post_button = (Button) findViewById(R.id.chatroom_post_button);
		chatroom_entered_post_textView = (TextView) findViewById(R.id.chatroom_entered_post_textView);
		
		Log.i(tag, "Done with Initialization of all variables!");
	}

	private void initializeSliderVariables() {
		popup = (TransparentPanel) findViewById(R.id.popup_window);
		popup.setVisibility(View.GONE);
		chatroom_ok_slider_popout_button = (Button) findViewById(R.id.chatroom_ok_slider_popout_button);
		chatroom_ok_slider_popout_button.setOnClickListener(this);
		chatroom_ok_showEventInfo_button = (Button) findViewById(R.id.chatroom_ok_showEventInfo_button);
		chatroom_ok_showEventInfo_button.setOnClickListener(this);
		chatroom_ok_showEventGuestList_button = (Button) findViewById(R.id.chatroom_ok_showEventGuestList_button);
		chatroom_ok_showEventGuestList_button.setOnClickListener(this);
		chatroom_ok_showMenu_button = (Button) findViewById(R.id.chatroom_ok_showMenu_button);
		chatroom_ok_showMenu_button.setOnClickListener(this);

	}

	private void setting_up_tab() {
		Log.i(logTag, "In setting up tab");
		setupTab(new TextView(this), "Wall", R.id.chatroom_tab);
		setupTab(new TextView(this), "Doodle", R.id.doodle_tab);
		setupTab(new TextView(this), "Map", R.id.Map);
		setupTab(new TextView(this), "Guest List", R.id.event_guestList_tab);
	}

	private void setupTab(final View view, final String tag, final Integer RID) {
		View tabview = createTabView(tabHost.getContext(), tag);

		TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview)
				.setContent(RID);
		tabHost.addTab(setContent);

	}

	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context)
				.inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.chatroom_post_button:
			do_chatroom_post_button();
			break;
		case R.id.chatroom_ok_slider_popout_button:
			do_slide();
			break;
		case R.id.chatroom_ok_showEventInfo_button:
			do_show_eventInfo();
			break;
		case R.id.chatroom_ok_showEventGuestList_button:

			do_show_guestList();

			break;
		case R.id.chatroom_ok_showMenu_button:
			do_show_menu();
			break;
			
		/* Google maps
		case R.id.button1: // Search
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(addr.getWindowToken(), 0);
			String strAddress = addr.getText().toString();
			if (strAddress != "") {
				setUpMap(strAddress, false);

			}
			showDialog(SEARCH_OPTIONS);
			break;
		case R.id.buttonDir:
			getDirections();
			break;
		case R.id.track:
			LocationResult locationResult = new LocationResult() {
				@Override
				public void gotLocation(final Location location) {

					Handler handler = new Handler(Looper.getMainLooper());
					handler.post(new Runnable() {

						@Override
						public void run() {
							CameraUpdate update = CameraUpdateFactory
									.newLatLng(new LatLng(location
											.getLatitude(), location
											.getLongitude()));

							mMap.moveCamera(update);
							mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
							if (true)
								mMap.addMarker(new MarkerOptions().position(
										new LatLng(location.getLatitude(),
												location.getLongitude()))
										.title("You are here"));
							Geocoder coder = new Geocoder(
									getApplicationContext());

							List<Address> address;
							try {
								address = coder.getFromLocation(
										location.getLatitude(),
										location.getLongitude(), 5);
								if (address.size() != 0) {
									addr.setText(address.get(0)
											.getFeatureName());
								}
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								showMessage("Couldn't resolve address");
							}
						}

					});
					// your UI code here
				}

			};
			MyLocation myLocation = new MyLocation();
			myLocation.getLocation(this, locationResult);
			break;
			*/
		}
		

	}

	private void do_show_menu() {
		startActivity(new Intent("com.project.instaplan.Menu"));
		finish();
	}

	private void do_show_guestList() {
		if (event.isMine) {
			if (event.getGuestList() != null) {
				showDialog(SHOW_GUEST_LIST);
			} else {
				showMessage("No Guests");
			}
		} else {
			if (event.guestList == null) {
				showMessage("ERROR: Could not retrieve guest list.");
			} else {
				showDialog(SHOW_GUEST_LIST);
			}
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case SHOW_GUEST_LIST:
			Log.i(tag, "Now going to show alert");
			AlertDialog guestList_dialog = showAlertBox();

			return guestList_dialog;

		case EVENT_INFO:
			eventInfoDialog = new Dialog(this);
			eventInfoDialog.setContentView(R.layout.layout_show_event_info);
			eventInfoDialog.setTitle(event.title + " Event Info");
			populateDialog();
			return eventInfoDialog;
//		case SEARCH_OPTIONS:
//			return showAlertBox2();
		}
		return super.onCreateDialog(id);
	}

	/*
	private Dialog showAlertBox2() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final ArrayList<String> guestList1 = plotOptions();
		CharSequence[] charSeq = guestList1.toArray(new CharSequence[guestList1
				.size()]);

		builder.setTitle("Your options");
		builder.setItems(charSeq, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int index) {
				addr.setText(guestList1.get(index));
			}
		});
		Log.i(tag, "Builder settup");
		return builder.create();
	}
	*/

	public AlertDialog showAlertBox() {
		final ArrayList<ArrayList<String>> guestList1 = event.guestList;
		if (event.guestList.size() != 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			Log.i(tag, "About to set up charseq");
			Log.i(tag, "guestList: ");
			for (ArrayList<String> ar : guestList1) {
				Log.i(tag, "Array XXXXXXXXXXXX");
				for (String pr : ar) {
					Log.i(tag, pr);
				}
			}
			CharSequence[] charSeq = guestList1.get(0).toArray(
					new CharSequence[guestList1.get(0).size()]);
			for (CharSequence str : charSeq) {
				Log.i(tag, "Char found: " + str);
			}
			builder.setTitle(event.title + " Guest List");
			builder.setItems(charSeq, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int index) {
					Log.i(tag, guestList1.get(0).get(index)
							+ " was clicked, Phone Number: "
							+ guestList1.get(1).get(index));
					showMessage(guestList1.get(0).get(index)
							+ " was clicked, Phone Number: "
							+ guestList1.get(1).get(index));
				}
			});
			Log.i(tag, "Builder settup");
			return builder.create();
		} else {
			showMessage("No guests");
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(event.title + " Guest List");
			builder.setMessage("No guests.");

			return builder.create();
		}
	}

	private void do_show_eventInfo() {
		showDialog(EVENT_INFO);
	}

	private void populateDialog() {
		if (!event.title.equals("Empty Event 999")) {
			TextView title = (TextView) eventInfoDialog
					.findViewById(R.id.show_title);
			title.setText(event.title);
			TextView host = (TextView) eventInfoDialog
					.findViewById(R.id.show_host);
			host.setText(event.host.name);
			TextView description = (TextView) eventInfoDialog
					.findViewById(R.id.show_description);
			description.setText(event.description);
			TextView location = (TextView) eventInfoDialog
					.findViewById(R.id.show_location);
			location.setText(event.location);
			TextView date = (TextView) eventInfoDialog
					.findViewById(R.id.show_date);
			date.setText(event.date);
			TextView time = (TextView) eventInfoDialog
					.findViewById(R.id.show_time);
			time.setText(event.time);
		}
	}

	private void do_slide() {
		if (key == 0) {
			key = 1;
			popup.setVisibility(View.VISIBLE);
			chatroom_ok_slider_popout_button
					.setBackgroundResource(R.drawable.leftarrow);
		} else if (key == 1) {
			key = 0;
			popup.setVisibility(View.GONE);
			chatroom_ok_slider_popout_button
					.setBackgroundResource(R.drawable.rightarrow);
		}

	}

	private void do_chatroom_post_button() {
		Log.i(tag, " Chatroom Post button pressed! ");
		if (chatroom_entered_post_textView.getText().toString().length() != 0) {
			String title = event.title;
			Log.i(tag, "Event title: " + title);
			if (!event.title.equals("Empty Event 999")) {
				Log.i(tag, "Event found, now updating event");
				if (event.hasUpdate()) {
					Log.i(tag, "Event had off-line updates, now clearing them");
					updateChatroom(event);
				}
				String entered_text = chatroom_entered_post_textView.getText()
						.toString().trim();

				Log.i(tag, "Posted Content: " + entered_text);

				event.updateChatLog("internal", entered_text, "Me");
				adapter.notifyDataSetChanged();

				chatroom_entered_post_textView.setText("");
				chatroom_listView.setSelection(event.messages.size() - 1);
				spreadPost(entered_text);

			} else {
				Log.i(tag, "Event is NOT a registered Event");
				chatroom_entered_post_textView.setText("");
			}
		}

	}

	private void spreadPost(String post_content) {
		// Auto-generated method stub
		Log.i(tag, "Now in Spread Main Ui Post");
		// post_content.replaceAll("%", "percent");
		// Auto-generated method stub

		String post_content_appless = post_content;
		String post_content_app = post_content_appless + " /-%"
				+ event.eventHash + "%-/";
		// post_content.replaceAll("%", "percent");
		if (sessionHasInternet && ClassUniverse.GCMEnabled) {
			Log.i(tag, "Session has both internet and GCMEnabled");
			for (ClassPeople invitee : event.invited) {
				Log.i(tag, "Invitee " + invitee.name + " hasApp: "
						+ invitee.hasApp);
				SmsManager sms = SmsManager.getDefault();
				if (invitee.hasApp) {
					if (invitee.hasGCM) {
						Log.i(tag, "Trying to send GCM to " + invitee.name);
						new SendGCMCommand().execute(invitee.phoneNumber,
								invitee.name, post_content_app);
						continue;
					} else {
						Log.i(tag, invitee.name + "Has No GCM");
						sms.sendTextMessage(invitee.phoneNumber, null,
								post_content_app, null, null);
					}
				} else {
					sms.sendTextMessage(invitee.phoneNumber, null,
							post_content_appless, null, null);
					Log.i(tag, "Sent Sms to: " + invitee.name
							+ " without tags.");
				}
			}
			return;
		} else {
			Log.i(tag, "No wifi or google account... sending sms's");
			for (ClassPeople invitee : event.invited) {
				SmsManager sms = SmsManager.getDefault();
				if (invitee.hasApp) {
					Log.i(tag, "Invitee " + invitee.name + " hasApp: "
							+ invitee.hasApp);
					sms.sendTextMessage(invitee.phoneNumber, null,
							post_content_app, null, null);
					Log.i(tag, "Sent Sms to: " + invitee.name + " with tags.");
				} else {
					sms.sendTextMessage(invitee.phoneNumber, null,
							post_content_appless, null, null);
					Log.i(tag, "Sent Sms to: " + invitee.name
							+ " without tags.");
				}
			}
		}
		return;
	}

	private void updateChatroom(ClassEvent event) {
		// Auto-generated method stub
		if (event.hasUpdate()) {
			adapter.notifyDataSetChanged();
			event.externalUpdateCount = 0;
		}
	}

	public class SendGCMCommand extends AsyncTask<String, Void, Integer> {

		String TophoneNumber = "";
		String content = "";
		String receiverName = "";

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == HttpURLConnection.HTTP_OK) {
				Log.i(tag, "Successfully sent GCM Message to: " + receiverName);
				ClassUniverse.universePhoneNumberLookUp.get(TophoneNumber).hasGCM = true;
			} else {
				Log.i(tag, "Failed to send GCM Message to: " + receiverName);
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(TophoneNumber, null, content, null, null);
				// ClassUniverse.universePhoneNumberLookUp.get(TophoneNumber).hasGCM=false;
			}
		}

		@Override
		protected Integer doInBackground(String... params) {
			URL url;
			TophoneNumber = params[0];
			content = params[2];
			receiverName = params[1];

			String strUrl = "http://mj-server.mit.edu/instaplan/command/"
					+ event.serverIdCode + "/?command=sendSmsTo"
					+ URLEncoder.encode(TophoneNumber) + "&content="
					+ URLEncoder.encode(content) + "&hostDeviceId="
					+ URLEncoder.encode(ClassUniverse.device_id)
					+ "&sender_phoneNumber="
					+ URLEncoder.encode(ClassUniverse.mPhoneNumber);
			try {
				url = new URL(strUrl);

				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(GCM_SERVER_RESPONSE_WAIT_TIME);
				urlConnection.connect();
				int out = urlConnection.getResponseCode();
				String server_reply = urlConnection.getResponseMessage();
				urlConnection.disconnect();
				if (out == 200) {
					Log.i(tag, "Successfully Sent GCM sms: code " + out);
				} else {
					Log.i(tag, "GCM Delivery failed: code " + out + ", reply: "
							+ server_reply);
				}
				return out;
			} catch (MalformedURLException e) {
				// Auto-generated catch block
				// e.printStackTrace();
				Log.i(tag, "ERROR SENDING GCM... MalFormedUrl");
				return ERROR_RESULT_CODE1;
			} catch (IOException e) {
				// Auto-generated catch block
				// e.printStackTrace();
				Log.i(tag, "ERROR SENDING GCM... IOEX..");
				return ERROR_RESULT_CODE2;
			}
		}

	}

	private void showMessage(String message) {
		Toast myToast;
		myToast = Toast.makeText(getApplicationContext(), message,
				Toast.LENGTH_SHORT);
		myToast.show();
	}

	public class GetEventId extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// Auto-generated method stub
			return getEventId(params[0], params[1]);
		}

		@Override
		protected void onPostExecute(String result) {
			// Auto-generated method stub
			super.onPostExecute(result);
			if (!result.equals("Error")) {
				event.serverIdCode = result;
				Log.i(tag, "EventIdCode: " + result);
				if (!event.isMine) {
					getGuestListInBackGround(event.serverIdCode);
				}
			}
			Log.i(tag, "Just set Event's ID code :) " + result);
		}

	}

	private String getEventId(String host_phoneNumber, String event_title) {
		URL url;
		Log.i(tag, "generating EventIdCode");
		String strUrl = "http://mj-server.mit.edu/instaplan/getEventId/?hostPhoneNumber="
				+ URLEncoder.encode(host_phoneNumber)
				+ "&eventTitle="
				+ URLEncoder.encode(event_title);
		try {
			Log.i(tag, " about to make the url...");
			url = new URL(strUrl);
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			Log.i(tag, " Made connections...");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			Log.i(tag, " readline?");
			String generatedEventCode = reader.readLine();
			Log.i(tag, "Generated key: " + generatedEventCode);
			reader.close();
			urlConnection.disconnect();
			return generatedEventCode;
		} catch (MalformedURLException e) {
			// Auto-generated catch block
			// e.printStackTrace();
			Log.i(tag, "ERROR SENDING GCM... MalFormedUrl");
			return "Error";
		} catch (IOException e) {
			// Auto-generated catch block
			// e.printStackTrace();
			Log.i(tag, "ERROR SENDING GCM... IOException..");
			return "Error";
		}
	}

	private void getSessionInfo() {
		Log.i(tag, "Getting Session Info");
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (mWifi.isConnected()) {
			sessionHasInternet = true;
			Log.i(tag, "Session has Wifi... Good!");
		} else {
			NetworkInfo mMobile = connManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobile.isConnected()) {
				Log.i(tag, "Session has No wifi, but 3G only!");
				showMessage("Turn on Wifi for cheaper (and better?) performance");
				sessionHasInternet = true;
			} else {
				Log.i(tag, "Session has No connectivity!");
			}
		}
		Log.i(tag, "Done getting session Info");

		Log.i(tag, "Now informing user about phone status");
		if (!sessionHasInternet) {
			showMessage("Not Internet texting");
		} else {
			Log.i(tag, "Session has internet, now fetching eventIdCode if new");
			if (event.serverIdCode == null) {
				Log.i(tag, "INDEED was new!");
				if (valid) {
					new GetEventId().execute(event.host.phoneNumber,
							event.title);
				} else {
					Log.i(tag, "Invalid Event... No getting code.");
				}

			} else {
				Log.i(tag, "We already knew this eventId");
			}
		}
		if (ClassUniverse.mPhoneNumber.equals("")) {
			ClassUniverse.device_id = Secure.getString(getApplicationContext()
					.getContentResolver(), Secure.ANDROID_ID);
		}
		Log.i(tag, "REFERENCES: DEVICEID: " + ClassUniverse.device_id
				+ " PhoneNumber: " + ClassUniverse.mPhoneNumber);

		// Give the rest of the functions.
		if ((valid) && !(event.title.equals("Your GCM Inbox"))) {
			chatroom_post_button.setOnClickListener(this);
		} else {
			chatroom_entered_post_textView.setEnabled(false);
			chatroom_entered_post_textView.setText("No Posts Allowed");
		}
		if (ClassUniverse.GCMPossible) {
			ClassUniverse.regId = GCMRegistrar
					.getRegistrationId(getApplicationContext());
		}
	}

	public boolean populateChatroom() {
		Log.i(tag, "Starting Populating Chatroom!");
		boolean out = false;
		incomming_intent = new Intent();
		incomming_intent = getIntent();

		if (incomming_intent.hasExtra("withNotification")) {
			Log.i(tag, "Cancelling the notification");
			nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(1234567855);
			Log.i(tag, "Done cancelling the notification");
		}

		if (incomming_intent.hasExtra("Title")) {
			String title = incomming_intent.getStringExtra("Title").toString();
			String hostPhoneNumber = incomming_intent.getStringExtra(
					"hostPhoneNumber").toString();
			eventHash = (title + hostPhoneNumber).hashCode();
			eventHash = (eventHash < 0 ? -eventHash : eventHash);
			Log.i(tag, "Incomming intent had title: " + title);
			if (ClassUniverse.universeAllEventHashLookUp.get(eventHash) != null) {
				Log.i(tag, "Event found, now populating Chatroom.");
				out = true;
				event = ClassUniverse.universeAllEventHashLookUp.get(eventHash);
				event.externalUpdateCount = 0;
				adapter = new AwesomeAdapter(this, event.messages);
				chatroom_listView.setAdapter(adapter);
			}
		} else {
			Log.i(tag, "Event not found on this device. No adapter required..");
		}

		Log.i(tag, "Done with Populating Chatroom!");
		return out;
	}

	// Managing the Chatroom Receiver ---------------
	public BroadcastReceiver intentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getExtras().containsKey(("smsFor" + event.title))) {
				// initializeAllVariables();
				
				initializeChatroomVariables();
				adapter.notifyDataSetChanged();
				chatroom_listView.setSelection(event.messages.size() - 1);
				event.externalUpdateCount--;
			}
		}
	};

	@Override
	protected void onResume() {
		// Auto-generated method stub
		registerReceiver(intentReceiver, intentFilter);
		super.onResume();
		chatroom_listView.setSelection(event.messages.size() - 1);
//		setUpMapIfNeeded(); // uncomment if you support Google map.
	}

	@Override
	protected void onPause() {
		//
		unregisterReceiver(intentReceiver);
		super.onPause();
		finish();
	}

	private ArrayList<ArrayList<String>> getExternalGuestList(
			String serverIdCode) {
		URL url;
		Log.i(tag, "in get Guest List... server Id code: " + serverIdCode);
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> phoneNumbers = new ArrayList<String>();
		String strUrl = "http://instaplan.mit.edu/instaplan/command/"
				+ URLEncoder.encode(serverIdCode) + "/?command=getGuestList";
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
			String responseLine;
			responseLine = reader.readLine();
			Log.i(tag, "THis is the response line?: " + responseLine);
			reader.close();
			if (responseLine.contains("<br>")) {
				String[] lines = responseLine.split("<br>");
				for (String line : lines) {
					if (line.contains("%--%")) {
						String[] parts = line.split("%--%");
						phoneNumbers.add(parts[1]);
						String displayName = getContactDisplayNameByNumber(
								parts[1], getApplicationContext());
						if (displayName != null) {
							names.add(displayName);
						} else {
							names.add(parts[0] + " -host contact");
						}
					}
				}
			}

			urlConnection.disconnect();
			ArrayList<ArrayList<String>> out = new ArrayList<ArrayList<String>>();
			out.add(names);
			out.add(phoneNumbers);
			return out;
		} catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR RETRIEVING GUESTLIST");
			return null;
		} catch (IOException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR RETRIEVING GUESTLIST2");
			return null;
		}
	}

	public String getContactDisplayNameByNumber(String number, Context inContext) {
		Uri uri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(number));
		String name = null;

		ContentResolver contentResolver = inContext.getContentResolver();
		Cursor contactLookup = contentResolver.query(uri, new String[] {
				BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME },
				null, null, null);

		try {
			if (contactLookup != null && contactLookup.getCount() > 0) {
				contactLookup.moveToNext();
				name = contactLookup.getString(contactLookup
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
				// String contactId =
				// contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID));
			}
		} finally {
			if (contactLookup != null) {
				contactLookup.close();
			}
		}

		return name;
	}

	public class GetGuestList extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			Log.i(logTag, "(BAKGROUND) I'm in the guest List!!!! params: "
					+ params[0]);
			event.guestList = getExternalGuestList(params[0]);
			initializeGuestList();
			Log.i(logTag, "Now we can set up guest list");
			return null;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
		Log.i(tag,
				event.guestList.get(0).get(index)
						+ " was clicked, Phone Number: "
						+ event.guestList.get(1).get(index));
		showMessage(event.guestList.get(0).get(index)
				+ " was clicked, Phone Number: "
				+ event.guestList.get(1).get(index));

	}

	/*
	@Override
	public void onMapLongClick(LatLng arg0) {

		info = mMap.addMarker(new MarkerOptions().position(arg0).title("Me"));

	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	private void setUpMap(String strAddress, boolean mark) {
		Geocoder coder = new Geocoder(this);

		try {
			List<Address> address = coder.getFromLocationName(strAddress, 5);
			if (address.size() == 0) {
				return;
			} else {
				Address location = address.get(0);
				location.getLatitude();
				location.getLongitude();
				CameraUpdate update = CameraUpdateFactory.newLatLng(new LatLng(
						location.getLatitude(), location.getLongitude()));
				mMap.moveCamera(update);
				mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
				if (mark)
					mMap.addMarker(new MarkerOptions().position(
							new LatLng(location.getLatitude(), location
									.getLongitude())).title(event.title));

			}
		} catch (IOException e) {

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.layout_menu, menu);
		return true;
	}

	private void setUpMap() {
		mMap.setOnMapLongClickListener(this);
	}

	public void getDirections() {
		Geocoder coder = new Geocoder(this);

		try {
			List<Address> to_address = coder.getFromLocationName(
					event.location, 5);
			List<Address> from_address = coder.getFromLocationName(addr
					.getText().toString(), 5);
			if (to_address.size() == 0 || from_address.size() == 0) {
				showMessage("Could not resolve locations.");
				return;
			} else {
				Address to_location = to_address.get(0);
				Address from_location = from_address.get(0);
				// to_location.getLatitude();
				// to_location.getLongitude();
				Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
						Uri.parse("http://maps.google.com/maps?saddr="
								+ from_location.getLatitude() + ","
								+ from_location.getLongitude() + "&daddr="
								+ to_location.getLatitude() + ","
								+ to_location.getLongitude() + ""));
				startActivity(intent);
			}
		} catch (IOException e) {
			showMessage("Error launching Google Maps");
		}

	}

	public ArrayList<String> plotOptions() {
		ArrayList<String> out = new ArrayList<String>();
		Geocoder coder = new Geocoder(this);
		try {
			List<Address> from_address = coder.getFromLocationName(addr
					.getText().toString(), 5);
			if (from_address.size() == 0) {
				showMessage("Could not resolve locations.");
				return null;
			} else {
				for (Address option : from_address) {
					out.add(option.getFeatureName());
				}
				return out;
			}
		} catch (IOException e) {
			showMessage("Error launching Google Maps");
		}
		return out;

	}
	*/
	
	
	
	
	/*
	 * For Marissa. USE THIS CONVENTION!
	 * To populate your Poll class, Modify the getExternalPollList() function.
	 * To send data to the server, use the updatePollInBackGround function. eg:
	 * NB. Use a String Builder! I suspect you will have huge strings!! I will do the URLEncode... just send the string as shown below.
	 * - to create a new poll use the command: updatePollInBackGround("addPoll","Poll Title%--%OptName<887>phoneNum1,phoneNum2||OptName<887>phoneNum3,phoneNum3<br>...")
	 * - to update an option, use the command: updatePollInBackGround("updateOptionInPoll","Poll Title%--%OptName<887>NewListofphoneNumbers")
	 * - to add an option to a Poll, use the command: updatePollInBackGround("addOptionToPoll","Poll Title%--%OptName<887>ListOfContacts")
	 * - to remove a Poll use the command: updatePollInBackGround("removePoll","Poll Title")
	 * - to remove an option from Poll, use the command: updatePollInBackGround("removeOptionToPoll","Poll Title%--%OptName")
	 * 
	 * The only way to get Data from the server, is to use the getPollListInBackGround, so when you update a Poll, you may want to recall the 
	 * getPollListInBackGround() to have updated data in your Poll Class.
	 * 
	 * Enjoy!
	 */
	

	
	private void updatePollInBackGround(String updateCommand, String content) {
		/*
		 * 	elif command=="addOptionToPoll":
    		elif command=="addPoll":
    		elif command=="removeOptionFromPoll":
    		elif command=="updateOptionInPoll":
		 */
		Log.i(tag, "This is the update Command we will try to process: "
				+ event.serverIdCode);
		
		new UpdatePoll().execute(updateCommand,content);
	}
	
	
	public class UpdatePoll extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {
			String updateCommand = params[0];
			String content = params[1];
			updatePoll(updateCommand, content);
			return null;
		}
		
	}
	public void updatePoll(String updateCommand, String content) {
		URL url;
		StringBuilder strUrl = new StringBuilder();
		strUrl.append("http://instaplan.mit.edu/instaplan/command/");
		strUrl.append(URLEncoder.encode(event.serverIdCode));
		strUrl.append("/?command=");
		strUrl.append(URLEncoder.encode(updateCommand));
		strUrl.append("&content=");
		strUrl.append(URLEncoder.encode(content));
		Log.i(logTag, "Executing this URL");
		try {
			url = new URL(strUrl.toString());

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			Log.i(logTag, "connection created!");
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();
			int out = urlConnection.getResponseCode();
			String server_reply = urlConnection.getResponseMessage();
			urlConnection.disconnect();
			if (out == 200) {
				Log.i(logTag, "Successfully updated Poll: code " + out);
			} else {
				Log.i(logTag, "Update Failed due to:  " + out + " meaning: "
						+ server_reply);
			}
			return;
		} catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING Update request... MalFormedUrl");
			return;
		} catch (IOException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR SENDING Update request type 2... IOEX..");
			return;
		}
	}
	
	private void getPollListInBackGround() {
		Log.i(tag, "This is the server code I will be trying to fetch: "
				+ event.serverIdCode);
		new GetPollList().execute();
	}
	
	
	public class GetPollList extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			getExternalPollList();
			return null;
		}

	}
	
	
	// Marissa fix this ;)
	private void getExternalPollList() {
		// Use this to populate your Poll class.
		
		URL url;
		Log.i(tag, "in get Poll List... server Id code: " + event.serverIdCode);
		String strUrl = "http://instaplan.mit.edu/instaplan/command/"
				+ URLEncoder.encode(event.serverIdCode) + "/?command=getPollList";
		try {
			url = new URL(strUrl);

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setConnectTimeout(3000);
			urlConnection.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					urlConnection.getInputStream()));
			
			// This is your Stuff!!!!!
			String pollListString = reader.readLine();// This is your Stuff!!!!!
			// This is your Stuff!!!!!
			
			
			Log.i(tag, "This is the response line?: " + pollListString);
			reader.close();
			urlConnection.disconnect();
			if (pollListString.contains("<br>")) {
				String[] pollsStrings = pollListString.split("<br>");
				for (String pollString : pollsStrings) {
					if (pollString.contains("%--%")) {
						String[] parts = pollString.split("%--%");
						
						// Do something with the parts. (title,content)
//						phoneNumbers.add(parts[1]);
//						String displayName = getContactDisplayNameByNumber(
//								parts[1], getApplicationContext());
//						if (displayName != null) {
//							names.add(displayName);
//						} else {
//							names.add(parts[0] + " -host contact");
//						}
					}
				}
			}

			return;
		} catch (MalformedURLException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR RETRIEVING GUESTLIST");
			return;
		} catch (IOException e) {
			//
			// e.printStackTrace();
			Log.i(logTag, "ERROR RETRIEVING GUESTLIST2");
			return;
		}
	}
	

}
