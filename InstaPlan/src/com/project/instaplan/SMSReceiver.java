package com.project.instaplan;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.project.instaplan2.Chatroom.SpreadPost; //We should do this sometime... for all reg tasks

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

//import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver {

	// Instantiate ALl Public Variables Here.
	String logTag = "MJ(SMSReceiver)------>";
	SmsMessage[] receivedMessage = null;

	Bundle incoming_bundle = new Bundle();
	int ERROR_RESULT_CODE2 = 999;
	int ERROR_RESULT_CODE1 = 666;
	boolean sessionHasInternet;
	ClassEvent event;

	@Override
	public void onReceive(Context context, Intent incoming_intent) {
		Log.i(logTag, "Sms Received! Now unifying parts");
		incoming_bundle = incoming_intent.getExtras();
		if (incoming_bundle != null) {
			Log.i(logTag, "There was something in our bundle");
			Object[] rawSms = (Object[]) incoming_bundle.get("pdus");
			receivedMessage = new SmsMessage[rawSms.length];

			// Resolving contact name from phone number
			String phoneNumber = SmsMessage.createFromPdu((byte[]) rawSms[0])
					.getOriginatingAddress().toString();
			Log.i(logTag, "Phone number of sender: " + phoneNumber);

			Log.i(logTag, "Now getting name...");
			String name = getContactDisplayNameByNumber(phoneNumber, context);
			
			// Unifying all SMS Parts into 1.
			String smsBody = "";
			for (int i = 0; i < receivedMessage.length; i++) {
				receivedMessage[i] = SmsMessage
						.createFromPdu((byte[]) rawSms[i]);
				smsBody += receivedMessage[i].getMessageBody().toString();
			}
			Log.i(logTag, "Unified sms: " + smsBody);

			// Getting the sms type and excuting it.
			switch (getSmsType(smsBody)) {
			case 0: // Regular Event
				Log.i(logTag, "This was a regular SMS. Doing nothing with it.");
				break;
			case 1:
				// Event update
				boolean isnew = false;
				// Try to get the person iff name is found.
				ClassPeople person;
				if (name != null) {
					if(ClassUniverse.universePhoneNumberLookUp.containsKey(phoneNumber)){
						person = ClassUniverse.universePhoneNumberLookUp
								.get(phoneNumber);
						person.name=name;
					}else{
						isnew = true;
						person = new ClassPeople(name, "phoneNumber", phoneNumber);
					}
				} else {
					name=phoneNumber;
					if(ClassUniverse.universePhoneNumberLookUp.containsKey(phoneNumber)){
						person = ClassUniverse.universePhoneNumberLookUp
								.get(phoneNumber);
						person.name=name;
					}else{
						isnew = true;
						person = new ClassPeople(name, "phoneNumber", phoneNumber);
					}
				}

				Log.i(logTag, "Resolved Person: " + person.name);
				// Processing: means removing the tags, if there are any...
				ArrayList<String> processedSMS = decodingComnandFromSms(person,
						smsBody);

				// Executing the sms command as in update the event chat room...
				if (executeSmsCommand(processedSMS, name, context, isnew)) {
					Log.i(logTag, "Event title we will be broadcasting: "
							+ processedSMS.get(0));
					notifyUser(processedSMS.get(0), name, context);
				}
				break;
			case 2: // Event Creation
				ClassPeople person1;
				if (name == null) {
					person1 = new ClassPeople(phoneNumber, "phoneNumber",
							phoneNumber);
					name = phoneNumber;
					person1.hasApp = true;
					person1.hasGCM = true; 
				}
				Log.i(logTag, "This is an Event Creation");
				createEventFromSms(smsBody, name, phoneNumber, context);
				notifyUser(name, context);
			}

		}
	}

	private void notifyUser(String inEventTitle, String name, Context context) {
		if (inEventTitle != null) {
			Log.i(logTag, "About to fire a notification!");
			// Log.i(logTag, "In chat Notification!!");
			// String body = name + " just posted to " + inEventTitle;
			// String title = "InstaPlan: Chat Update!";
			// Intent intent = new Intent();
			// intent.setAction("com.project.instaplan2.Chatroom");
			// intent.putExtra("Title", inEventTitle);
			// intent.putExtra("withNotification", "haha");
			// PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
			// 0);
			// NotificationManager nm = (NotificationManager) context
			// .getSystemService(Service.NOTIFICATION_SERVICE);
			// Notification n = new Notification(R.drawable.instaplan_icon,
			// body,
			// System.currentTimeMillis());
			// n.setLatestEventInfo(context, title, body, pi);
			// Log.i(logTag, "Everything Set up");
			// n.defaults = Notification.DEFAULT_ALL;
			// nm.notify(1234567855, n);
			// Log.i(logTag, "Notification Done");
			String body = "Update from " + name + " to: " + inEventTitle;
			String title = "InstaPlan: Chatroom Update!";
			Intent intent = new Intent();
			intent.setAction("com.project.instaplan2.AllEvents");
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
	}

	private void notifyUser(String name, Context context) {
		Log.i(logTag, "In Notification!!");
		String body = "Created by " + name;
		String title = "InstaPlan: New Event!";
		Intent intent = new Intent();
		intent.setAction("com.project.instaplan2.AllEvents");
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

	private boolean executeSmsCommand(ArrayList<String> processedSMS,
			String name, Context context, boolean newbie) {
		boolean out = false;
		Log.i(logTag, "About to executeSmsCommand");
		if (processedSMS.get(0) != null) {
			if ((ClassUniverse.isPersonParticipatingInEvent("name", name,
					processedSMS.get(0)).contentEquals("true")) || newbie) {
				Log.i(logTag, name + " is associated with this event: "
						+ processedSMS.get(0) + "Updating Event's chatlog");
				String postUpdate = "";
				ClassPeople person;
				if (ClassUniverse.universeNameLookUp.containsKey(name)) {
					person = ClassUniverse.universeNameLookUp.get(name);
				} else {
					person = ClassUniverse.universePhoneNumberLookUp.get(name);
				}
				Log.i(logTag, "Matching, so we can abort the broadcast");

				Log.i(logTag, "This is Processed get 1: " + processedSMS.get(1));
				if (processedSMS.get(1).contains("%N%")) {
					Log.i(logTag, "%N% was found inside...");
					// This means, the post is NOT mine, so do not from the
					// sender, but on behalf of someone else... :D
					postUpdate = processedSMS.get(1).replace("%N%", "");
				} else {
					Log.i(logTag, "%N% was NOT found inside...");
					postUpdate += person.name;
					postUpdate += ": ";
					postUpdate += processedSMS.get(1);
					Log.i(logTag, " Post Created = " + postUpdate);
				}
				event = ClassUniverse.universeEventLookUp
						.get(processedSMS.get(0));

				if (newbie) {
//					event.invite(person); //SEND TO ONLY THE HOST
				}
				event.updateChatLog("external", postUpdate, person.name);
				// new GetSessionInfo().execute();
				if (event.isMine) {
					spreadPost(person, processedSMS.get(1));
				}

				// Sending out Intent if Chatroom was active...
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction("SMS_RECEIVED_ACTION");
				broadcastIntent.putExtra("smsFor" + processedSMS.get(0),
						postUpdate);
				context.sendBroadcast(broadcastIntent);
				out = true;
			} else {
				Log.i(logTag,
						name
								+ " isParticipating gave: "
								+ ClassUniverse.isPersonParticipatingInEvent(
										"name", name, processedSMS.get(0)));
			}

		} else {
			Log.i(logTag, "Couldn't Resolve Event... Index out of range?");
		}
		if (out) {
			abortBroadcast();
		}
		;
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

		return name;
	}

	public int getSmsType(String smsBody) {
		Log.i(logTag, "Checking what type of message we are dealing with");
		if (smsBody.contains("NEW EVENT! Title: ")
				&& smsBody.contains("PS: No InstaPlan? Add %E")) {
			Log.i(logTag, "This was a create event command, returning 2");
			return 2;
		}
		if (smsBody.contains("/-%")) {
			Log.i(logTag,
					"This could be an APP-sent sms Event update, we will check that, returning 1");
			return 1;
		}
		if (smsBody.contains("%E")) {
			Log.i(logTag,
					"This could be an Appless-sent sms Event update, we will check that, returning 1");
			return 1;
		}
		return 0;
	}

	public ArrayList<String> decodingComnandFromSms(ClassPeople person,
			String smsBody) {
		ArrayList<String> out = new ArrayList<String>();
		Pattern pattern = Pattern.compile("%E(\\d*)%");
		Matcher matcher = pattern.matcher(smsBody);
		if (matcher.find()) {
			Log.i(logTag, "A valid SMS Tag was found: " + matcher.group(1));
			Log.i(logTag, "Setting person to APPLESS mode");
			person.hasApp = false;
			person.hasGCM = false; // not really... lol
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
				out.add(smsBody.replaceAll("%E" + matcher.group(1) + "%", ""));
				return out;
			} else {
				Log.i(logTag, "No event at index " + (index - 1));
				out.add(null);
				out.add(smsBody + " ||InstaPlan: No event with code "
						+ (index - 1));
				return out;
			}
		} else {
			pattern = Pattern.compile("/-%(.*?)%-/");
			matcher = pattern.matcher(smsBody);
			if (matcher.find()) {
				person.hasApp = true;
				person.hasGCM = true; // not really... lol
				if (ClassUniverse.universeEventLookUp.containsKey(matcher
						.group(1))) {
					Log.i(logTag, "Valid event: " + matcher.group(1));
					out.add(matcher.group(1));
					String body = smsBody.replaceAll("/-%" + matcher.group(1)
							+ "%-/", "");
					out.add(body);
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

		abortBroadcast();
		Log.i(logTag, "This was a valid Event Creating Command");

		ArrayList<String> out = new ArrayList<String>();
		Pattern pattern = Pattern.compile("%(.*?)%");
		String processedBody = smsToCreateCommand(smsBody);
		Matcher matcher = pattern.matcher(processedBody);
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
		person.hasApp = true;
		newEvent.makeHost(person);
		newEvent.invite(person);

		// Notification instead!
		// Toast.makeText(context, "New Event Created By: " + name,
		// Toast.LENGTH_SHORT).show();
	}

	private String smsToCreateCommand(String smsBody) {
		// Auto-generated method stub
		String out = "";
		out = smsBody.replaceFirst("NEW EVENT! Title: ", "%");
		out = out.replaceFirst(", Desc: ", "% %");
		out = out.replaceFirst(", Time: ", "% %");
		out = out.replaceFirst(", Date: ", "% %");
		out = out.replaceFirst(", Loc: ", "% %");
		out = out.replaceFirst(", PS:", "%");
		return out;
	}

	private Void spreadPost(ClassPeople sender, String raw_sms_without_tags) {

		Log.i(logTag, "Now in Spread Main Ui Post");
		
			Log.i(logTag,
					"This was my event, I'm now spreading it... POST UPDATE: "
							+ raw_sms_without_tags);
			
			// 1. Person is appless
			String post_content_appless = sender.name+": "+raw_sms_without_tags;
			
			// 2. Person has app.			
			String post_content_sms = "%N%" + post_content_appless + " /-%"
					+ event.title + "%-/";
			
			// 3. Person has gcm.
			String post_content_gcm= raw_sms_without_tags + " /-%"
					+ event.title + "%-/";
			Log.i(logTag, "Post content app: " + post_content_sms);

			if (ClassUniverse.GCMEnabled) {
				Log.i(logTag, "Session has both wifi and GCMEnabled");
				for (ClassPeople invitee : event.invited) {
					if (invitee.phoneNumber.equals(sender.phoneNumber)) {
						continue;
					}
					Log.i(logTag, "Invitee " + invitee.name + " hasApp: "
							+ invitee.hasApp);
					SmsManager sms = SmsManager.getDefault();
					if (invitee.hasApp) {
						if (invitee.hasGCM) {
							new SendGCMCommand().execute(invitee.phoneNumber,
									sender.phoneNumber, post_content_gcm,post_content_sms);
							continue;
						} else {
							sms.sendTextMessage(invitee.phoneNumber, null,
									post_content_sms, null, null);
							Log.i(logTag, "Sent Sms to: " + invitee.name
									+ " with tags, Content: "
									+ post_content_sms);
						}

					} else {
						sms.sendTextMessage(invitee.phoneNumber, null,
								post_content_appless, null, null);
						Log.i(logTag, "Sent Sms to: " + invitee.name
								+ " with tags, Content: "
								+ post_content_appless);
					}
				}
				return null;

			} else {
				Log.i(logTag, "No GCM enabled ... sending sms's");
				for (ClassPeople invitee : event.invited) {
					if (invitee.phoneNumber.equals(sender.phoneNumber)) {
						continue;
					}
					SmsManager sms = SmsManager.getDefault();
					if (invitee.hasApp) {
						Log.i(logTag, "Invitee " + invitee.name + " hasApp: "
								+ invitee.hasApp);
						sms.sendTextMessage(invitee.phoneNumber, null,
								post_content_sms, null, null);
						Log.i(logTag, "Sent Sms to: " + invitee.name
								+ " with tags. content: " + post_content_sms);
					} else {
						sms.sendTextMessage(invitee.phoneNumber, null,
								post_content_appless, null, null);
						Log.i(logTag, "Sent Sms to: " + invitee.name
								+ " without tags. Content: "
								+ post_content_appless);
					}
				}
			}
			return null;
		}
	

	public class SendGCMCommand extends AsyncTask<String, Void, Integer> {

		String TophoneNumber = "";
		String senderPhoneNumber = "";
		String content_gcm = "";
		String content_sms = "";

		
		@Override
		protected Integer doInBackground(String... params) {
			URL url;
			TophoneNumber = params[0];
			senderPhoneNumber = params[1];
			content_gcm = params[2];
			content_sms= params[3];
			

			String strUrl = "http://mj-server.mit.edu/instaplan/command/"
					+ event.eventIdCode + "/?command=sendSmsTo"
					+ URLEncoder.encode(TophoneNumber) + "&content="
					+ URLEncoder.encode(content_gcm) + "&hostDeviceId="
					+ URLEncoder.encode(ClassUniverse.device_id)
					+ "&sender_phoneNumber="
					+ URLEncoder.encode(senderPhoneNumber);
			try {
				url = new URL(strUrl);

				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setConnectTimeout(3000);
				urlConnection.connect();
				int out = urlConnection.getResponseCode();
				String server_reply = urlConnection.getResponseMessage();
				urlConnection.disconnect();
				if (out == 200) {
					Log.i(logTag, "Successfully Sent sms: code " + out);
				} else {
					Log.i(logTag, "Sms Delivery failed: code " + out
							+ " reply: " + server_reply);
				}
				return out;
			} catch (MalformedURLException e) {
				// Auto-generated catch block
				// e.printStackTrace();
				Log.i(logTag, "ERROR SENDING GCM... MalFormedUrl");
				return ERROR_RESULT_CODE1;
			} catch (IOException e) {
				// Auto-generated catch block
				// e.printStackTrace();
				Log.i(logTag, "ERROR SENDING GCM... IOEX..");
				return ERROR_RESULT_CODE2;
			}
		}
		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == HttpURLConnection.HTTP_OK) {
				Log.i(logTag, "Successfully sent GCM Message to: "
						+ ClassUniverse.universePhoneNumberLookUp.get(senderPhoneNumber).name);
			} else {
				Log.i(logTag, "Failed to send GCM Message to: " + ClassUniverse.universePhoneNumberLookUp.get(senderPhoneNumber).name);
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(TophoneNumber, null, content_sms, null, null);
			}
		}

	}
}
