package com.project.instaplan;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {

	// Instantiate ALl Public Variables Here.
	String logTag = "MJ------>";
	SmsMessage[] receivedMessage = null;

	Bundle incoming_bundle = new Bundle();

	@Override
	public void onReceive(Context context, Intent incoming_intent) {
		Log.i(logTag, "Sms Received!");
		incoming_bundle = incoming_intent.getExtras();
		if (incoming_bundle != null) {
			Log.i(logTag, "There was something in our bundle");
			Object[] rawSms = (Object[]) incoming_bundle.get("pdus");
			receivedMessage = new SmsMessage[rawSms.length];
			String phoneNumber = SmsMessage.createFromPdu((byte[]) rawSms[0])
					.getOriginatingAddress().toString();
			Log.i(logTag, "This is phoneNumber of sender: " + phoneNumber);
			String name = (String) getContactDisplayNameByNumber(phoneNumber,
					context);
			Log.i(logTag, "Name of Sender: " + name);
			String smsBody = "";
			for (int i = 0; i < receivedMessage.length; i++) {
				receivedMessage[i] = SmsMessage
						.createFromPdu((byte[]) rawSms[i]);
				smsBody += receivedMessage[i].getMessageBody().toString();
			}
			Log.i(logTag, "MSG: " + smsBody);
			switch (getSmsType(smsBody)) {
			case 0: // Regular Event
				Log.i(logTag, "This was a regular SMS");
				break;
			case 1: // Event update
				if (listeningToPerson(name)) {
					Log.i(logTag,
							"We are listenning to this person! Generating processedSMS");
					Log.i(logTag, "this is an SMS update!");
					ClassPeople person;
					if (ClassUniverse.universeNameLookUp.containsKey(name)) {
						person = ClassUniverse.universeNameLookUp.get(name);
					} else {
						person = ClassUniverse.universePhoneNumberLookUp
								.get(name);
					}
					Log.i(logTag, "Found person: " + person.name);
					ArrayList<String> processedSMS = updateEventFromSms(person,
							smsBody);
					if(executeSms(processedSMS, name, context)){
						Log.i(logTag, "This is event title we are sending: "+processedSMS.get(0));
					notifyUser(processedSMS.get(0), name, context);
					}
				} else {
					Log.i(logTag, "We are not listening to this person..."
							+ name);
				}
				break;
			case 2: // Event Creation
				Log.i(logTag, "This is an Event Creation");
				createEventFromSms(smsBody, name, phoneNumber, context);
				notifyUser(name, context);
			}

		}
	}

	private void notifyUser(String inEventTitle, String name, Context context) {
		if (inEventTitle != null) {
			Log.i(logTag, "In chat Notification!!");
			String body = name + " just posted to " + inEventTitle;
			String title = "InstaPlan: Chat Update!";
			Intent intent = new Intent();
			intent.setAction("com.project.instaplan.Chatroom");
			intent.putExtra("Title", inEventTitle);
			intent.putExtra("withNotification", "haha");
			PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Service.NOTIFICATION_SERVICE);
			Notification n = new Notification(R.drawable.instaplan_icon, body,
					System.currentTimeMillis());
			n.setLatestEventInfo(context, title, body, pi);
			Log.i(logTag, "Everything Set up");
			n.defaults = Notification.DEFAULT_ALL;
			nm.notify(1234567855, n);
			Log.i(logTag, "Notification Done");
		}
	}

	private boolean listeningToPerson(String name) {
		return ((ClassUniverse.universeNameLookUp.containsKey(name)) || (ClassUniverse.universePhoneNumberLookUp
				.containsKey(name)));

	}

	private void notifyUser(String name, Context context) {
		Log.i(logTag, "In Notification!!");
		String body = "Created by " + name;
		String title = "InstaPlan: New Event!";
		Intent intent = new Intent();
		intent.setAction("com.project.instaplan.AllEvents");
		intent.putExtra("withNotification", "haha");
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Service.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.instaplan_icon, body,
				System.currentTimeMillis());
		n.setLatestEventInfo(context, title, body, pi);
		Log.i(logTag, "Everything Set up");
		n.defaults = Notification.DEFAULT_ALL;
		nm.notify(1234567856, n);
		Log.i(logTag, "Notification Done");

	}

	private boolean executeSms(ArrayList<String> processedSMS, String name,
			Context context) {
		boolean out = false;
		if (processedSMS.get(0) != null) {
			Log.i(logTag, "An Event was found");
			if ((ClassUniverse.isPersonParticipatingInEvent("name", name,
					processedSMS.get(0)).contentEquals("true"))) {
				Log.i(logTag,
						"This Name is Associated with this event. Creating Post Update");
				String postUpdate = "";
				ClassPeople person;
				if (ClassUniverse.universeNameLookUp.containsKey(name)) {
					person = ClassUniverse.universeNameLookUp.get(name);
				} else {
					person = ClassUniverse.universePhoneNumberLookUp.get(name);
				}
				abortBroadcast();
				postUpdate += person.name;
				postUpdate += ": ";
				postUpdate += processedSMS.get(1);
				Log.i(logTag, " Post Created = " + postUpdate);

				ClassUniverse.universeEventLookUp.get(processedSMS.get(0))
						.updateChatLog("external", postUpdate);

				// Sending out Intent if Chatroom was active...
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction("SMS_RECEIVED_ACTION");
				broadcastIntent.putExtra("smsFor" + processedSMS.get(0),
						postUpdate);
				context.sendBroadcast(broadcastIntent);
				out=true;
			} else {
				Log.i(logTag,
						name
								+ " isParticipating gave: "
								+ ClassUniverse.isPersonParticipatingInEvent(
										"name", name, processedSMS.get(0)));
			}

		} else {
			Log.i(logTag, "Couldn't Resolve Event... Index out of range");
		}
		return out;

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
		if (name == null) {
			return number;
		}
		return name;
	}

	public int getSmsType(String smsBody) {
		String[] groups = smsBody.split("/%");
		Log.i(logTag, "Group length: " + groups.length);
		if (groups.length == 2) {
			Log.i(logTag, "returning 1");
			return 1;
		}
		if (groups.length == 7) {
			Log.i(logTag, "returning 2");
			return 2;
		}
		return 0;

	}

	public ArrayList<String> updateEventFromSms(ClassPeople person,
			String smsBody) {
		ArrayList<String> out = new ArrayList<String>();
		Pattern pattern = Pattern.compile("/%E(\\d*)%/");
		Matcher matcher = pattern.matcher(smsBody);
		if (matcher.find()) {
			Log.i(logTag, "A valid SMS Tag was found: " + matcher.group(1));
			Log.i(logTag, "THIS PERSON IS APPLESS");
			person.hasApp = false;
			int index = Integer.parseInt(matcher.group(1));
			if ((index <= ClassUniverse.universeListOfAllEvents.size())
					&& (index > 0)) {
				Log.i(logTag,
						"Event Found at index "
								+ (index - 1)
								+ " is: "
								+ ClassUniverse.universeListOfAllEvents
										.get(index - 1).title);
				out.add(ClassUniverse.universeListOfAllEvents.get(index - 1).title);
				out.add(smsBody.replaceAll("/%E" + matcher.group(1) + "%/", ""));
				return out;
			} else {
				Log.i(logTag, "No event at index " + (index - 1));
				out.add(null);
				out.add(smsBody + " ||InstaPlan: No event with code "
						+ (index - 1));
				return out;
			}
		} else {
			pattern = Pattern.compile("%(.*?)%");
			matcher = pattern.matcher(smsBody);
			if (matcher.find()) {
				person.hasApp = true;
				if (ClassUniverse.universeEventLookUp.containsKey(matcher
						.group(1))) {
					Log.i(logTag, "Valid event: " + matcher.group(1));
					out.add(matcher.group(1));
					out.add(smsBody.replaceAll("/%" + matcher.group(1) + "%/", ""));
					return out;
				} else {
					Log.i(logTag, "INVALID EVENT: " + matcher.group(1));
					out.add(null);
					out.add(smsBody);
					return out;
				}

			} else {
				Log.i(logTag, "This is a REGULAR SMS!!");
				out.add(null);
				out.add(smsBody);
				return out;
			}

		}
	}

	public void createEventFromSms(String smsBody, String name,
			String phoneNumber, Context context) {
		if (smsBody.contains("New event.")) {
			abortBroadcast();
			Log.i(logTag, "This was a valid Event Creating Command");
			ArrayList<String> out = new ArrayList<String>();
			Pattern pattern = Pattern.compile("/%(.*?)%/");
			Matcher matcher = pattern.matcher(smsBody);
			ClassPeople person;
			while (matcher.find()) {
				Log.i(logTag, matcher.group());
				out.add(matcher.group(1));
			}
			ClassEvent newEvent = new ClassEvent(out.get(0), out.get(4),
					out.get(1), out.get(2), out.get(3));
			ClassUniverse.createEvent(newEvent);
			if (ClassUniverse.universeNameLookUp.containsKey(name)) {
				person = (ClassUniverse.universeNameLookUp.get(name));
			} else {
				person = new ClassPeople(name, "phoneNumber", phoneNumber);
			}
			person.hasApp=true;
			newEvent.makeHost(person);
			newEvent.invite(person);

			// Notification instead!
			// Toast.makeText(context, "New Event Created By: " + name,
			// Toast.LENGTH_SHORT).show();
		}

	}

}
