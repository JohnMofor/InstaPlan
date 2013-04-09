package com.project.instaplan;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.os.Bundle;
import android.util.Log;

public class AllEvents extends Activity implements View.OnClickListener,
		OnItemClickListener, OnItemLongClickListener {

	// Instantiate ALl Public Variables Here.
	String logTag = "MJ(All events)------>";
	Button allEvents_createNew_button;
	ListView allEvents_listView;
	public static final int uniqueID = 1234567856;
	NotificationManager nm;
	Intent incomingIntent;

	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(logTag, "Starting AllEvents.java");
		setContentView(R.layout.layout_all_events);
		initializeAllVariables();
		settingUpAdapter();
		incomingIntent = getIntent();		
		if(incomingIntent.hasExtra("withNotification")){
			Log.i(logTag, "Cancelling the notification");
			nm = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
			nm.cancel(uniqueID);
		}

		// Give the rest of the functions.
		// TODO
		allEvents_createNew_button.setOnClickListener(this);
		allEvents_listView.setOnItemClickListener(this);
		allEvents_listView.setOnItemLongClickListener(this);
	}

	private void settingUpAdapter() {
		// TODO Auto-generated method stub
		Log.i(logTag, "In setting Adapter");
		allEvents_listView.setAdapter(adapter);
	}

	private void initializeAllVariables() {
		Log.i(logTag, "AllEvents.java Initializing All Variables");
		allEvents_createNew_button = (Button) findViewById(R.id.allEvents_createNew_button);
		allEvents_listView = (ListView) findViewById(R.id.allEvents_listView);
		adapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, 0);
		for (ClassEvent event : ClassUniverse.universeListOfAllEvents) {
			adapter.add(event.title);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long id) {
		// TODO Auto-generated method stub
		Log.i(logTag, "List item Clicked" + position);
		ClassEvent clicked_event = ClassUniverse.universeListOfAllEvents
				.get(position);
		Intent sendToChatRoom = new Intent("com.project.instaplan2.Chatroom_ok");
		Log.i(logTag, "Putting Title Extra: " + clicked_event.title);
		sendToChatRoom.putExtra("Title", clicked_event.title);
		sendToChatRoom.putExtra("hostPhoneNumber", clicked_event.host.phoneNumber);
		startActivity(sendToChatRoom);
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.allEvents_createNew_button:
			// Enter code for this button.
			Log.i(logTag, "Create New Button was pressed");
			Intent sendToCreateEvent = new Intent(
					"com.project.instaplan2.CreateEvent");
			startActivity(sendToCreateEvent);
			break;

		// ADD MORE button CASESE:
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (ClassUniverse.universeListOfAllEvents.size() > 0) {
			adapter.clear();
			for (ClassEvent event : ClassUniverse.universeListOfAllEvents) {
				adapter.add(event.title);
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO
		super.onPause();
		finish();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		Log.i(logTag, "List item Clicked" + position);
		ClassEvent clicked_event = ClassUniverse.universeListOfAllEvents
				.get(position);
		clicked_event.delete();
		clicked_event=null;
		adapter.clear();
		if (ClassUniverse.universeListOfAllEvents.size() > 0) {			
			for (ClassEvent event : ClassUniverse.universeListOfAllEvents) {
				adapter.add(event.title);
				adapter.notifyDataSetChanged();
			}
		}
		return false;
		
		// TODO Auto-generated method stub
		
	}
}
