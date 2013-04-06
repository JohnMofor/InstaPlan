package com.project.instaplan;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;

public class GetContacts extends Activity implements View.OnClickListener {
	// Instantiate ALl Public Variables Here.
	String TAG = "MJ(GetContact)------>";

	ListView getContactsListView;
	Button getContacts_done_button;
	Cursor databaseCursor;
	ListAdapter adapter2;
	String[] Contacts = {};
	int[] to = {};

	ClassPeople person = null;
	Intent sendResultsBackToCreateEvent = new Intent();
	Bundle bundleBackToCreateEvent = new Bundle();

	ArrayList<String> contactNamesString = new ArrayList<String>();
	// Remove this when done!! we just sending names.
	ArrayList<String> contactPhonesString = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_get_contacts);
		initializeAllVariables();
		if (databaseCursor.getCount() < 1) {
			Log.i(TAG, "DataBase was Empty");
		} else {
			databaseCursor.moveToFirst();
		}
		settingUpAdapter();
	}

	// --------------ALL HELPER FUNCTIONS----------------

	private void settingUpAdapter() {
		Log.i(TAG, "Presently in settingUpAdapter");
		getContactsListView.setAdapter(adapter2);
		getContactsListView.setItemsCanFocus(false);
		getContactsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		getContacts_done_button.setOnClickListener(this);
	}

	private void initializeAllVariables() {
		Log.i(TAG, "Get Contacts: Presently in initialzeAllVariables");
		getContactsListView = (ListView) findViewById(R.id.getContacts_listView);
		getContacts_done_button = (Button) findViewById(R.id.getContacts_done_button);
		databaseCursor = getContacts();

		adapter2 = new SimpleCursorAdapter(
				this,
				android.R.layout.simple_list_item_multiple_choice,
				databaseCursor,
				Contacts = new String[] { ContactsContract.Contacts.DISPLAY_NAME },
				to = new int[] { android.R.id.text1 });
	}

	private Cursor getContacts() {
		Log.i(TAG, "Presently in getContacts");
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = '"
				+ ("1") + "'";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		return managedQuery(uri, projection, selection, selectionArgs,
				sortOrder);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {
		case R.id.getContacts_done_button:
			Log.i(TAG, "Done Button pressed");
			long[] ids = getContactsListView.getCheckedItemIds();
			Log.i(TAG,
					"Number of Items checked: "
							+ Integer.toString(getContactsListView
									.getCheckedItemCount()));

			for (long id : ids) {
				String current = Long.toString(id);
				// Log.i(TAG, "Now processing retrieved ID: " + current);
				Cursor contact = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = ?", new String[] { current }, null);

				// Log.i(TAG,
				// "Contact Cursor Created, Trying to Fetch name...");
				contact.moveToFirst(); // MAGIC LINE!!!!

				String name = contact
						.getString(contact
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
				String phoneNumber = contact
						.getString(contact
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				contactNamesString.add(name);
				contactPhonesString.add(phoneNumber);

				person = new ClassPeople(name, "phoneNumber", phoneNumber);
				Log.i(TAG, "Person Created (reading class): " + person.name
						+ " & Phone: " + person.phoneNumber);
			}
			Log.i(TAG, "Now Creating Bundle");
			bundleBackToCreateEvent.putStringArrayList("names",
					contactNamesString);
			sendResultsBackToCreateEvent.putExtras(bundleBackToCreateEvent);
			setResult(RESULT_OK, sendResultsBackToCreateEvent);

			// for (String name : contactNamesString) {
			// Log.i(TAG, "Choosen: " + name);
			// }
			// for (String num : contactPhonesString) {
			// Log.i(TAG, "Choosen: " + num);
			// }

			break;

		// ADD MORE button CASESE:
		}
		quit();
	}

	private void quit() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Selected Contacts Added",
				Toast.LENGTH_SHORT).show();
		databaseCursor.close();
		finish();
	}

	@Override
	protected void onPause() {
		// TODO
		super.onPause();
		quit();
	}
}
/**
 * // Residue Code:-----------------------------------------------------
 * 
 * 
 * //} //ArrayAdapter<String> adapter1; //private Cursor getContacts1() { // //
 * Run query // Uri uri = ContactsContract.Contacts.CONTENT_URI; // String[]
 * projection = new String[] { ContactsContract.Contacts._ID, //
 * ContactsContract.Contacts.DISPLAY_NAME }; // String selection =
 * ContactsContract.Contacts.HAS_PHONE_NUMBER; // String[] selectionArgs = null;
 * // String sortOrder = null; // // return managedQuery(uri, projection,
 * selection, selectionArgs, // sortOrder); //} //if (databaseCursor.getCount()
 * < 1) { // Log.i(TAG, "DataBase was Empty"); //} else { // Log.i(TAG,
 * "There was something in the Database.. Populating"); //
 * databaseCursor.moveToFirst(); // // populateAdapter(); // } //private void
 * populateAdapter() { //// TODO Auto-generated method stub //while
 * (databaseCursor.moveToNext()) { // String name =
 * databaseCursor.getString(databaseCursor //
 * .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)); //
 * adapter1.add(name); // adapter1.notifyDataSetChanged(); //} //In IniTIALIZE:
 * // ADAPTER1 = NEW ARRAYADAPTER<STRING>(THIS, //
 * ANDROID.R.LAYOUT.simple_list_item_multiple_choice, 0);
 **/
