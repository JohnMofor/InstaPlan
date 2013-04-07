package com.project.instaplan;

import org.apache.http.message.BasicNameValuePair;

import com.google.android.gcm.GCMBaseIntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GCMIntentService extends GCMBaseIntentService {
	final String LOG_TAG = "MJ(GCMIntentService)----->";
	public static String GCM_SENDER = "981817883739";
	static String logTag = "MJ(GCMIntentService)----->";
	Bundle incoming_bundle;

	int ERROR_RESULT_CODE2 = 999;
	int ERROR_RESULT_CODE1 = 666;
	boolean sessionHasInternet = true;
	boolean isnew = false;
	ClassEvent event;

	public static String API_URL = "http://mj-server.mit.edu/gcm/v1/device/";

	public GCMIntentService() {
		super(GCM_SENDER);
	}

	@Override
	public void onError(Context context, String errorId) {
		ClassUniverse.regId = "";
		Log.i(LOG_TAG, "Messaging registration error: " + errorId);
		if (errorId.equals("ACCOUNT_MISSING")) {
			errorId = "NO GOOGLE ACCOUNT FOUND!";
		}
		ClassUniverse.GcmRegError = "ERROR: " + errorId;
	}

	public void show(Context context, String msg) {
		// Auto-generated method stub
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		ClassUniverse.regId = "";
		Log.i(LOG_TAG, "Received recoverable error: " + errorId);
		ClassUniverse.GcmRegError = "ERROR: " + errorId;
		return super.onRecoverableError(context, errorId);
	}

	@Override
	protected void onMessage(Context context, Intent incoming_intent) {
		String msg = incoming_intent.getExtras().getString("msg");
		Log.i(LOG_TAG, "Just received a GCM message!: " + msg);
		Log.i(logTag, "Splitting message to PhoneNumber, content");
		String[] receivedMessage = msg.split("%xd%");

		if (receivedMessage.length > 2) {
			Log.i(logTag, "Spilt text: " + "sender phoneNumber: "
					+ receivedMessage[0] + " sender preferred_name:"
					+ receivedMessage[1] + " 2:" + receivedMessage[2]);

			// Resolving contact name from phone number------------
			String phoneNumber = receivedMessage[0];
			String preferred_name = receivedMessage[1];
			String smsBody = receivedMessage[2];
			switch (getSmsType(smsBody)) {
			case 0:
				// Just Inbox
				Log.i(logTag, "This was a regular GCM SMS. Sent To inbox");
				updateGCMInbox(msg, context);
				break;
			case 1:
				// Event update request
				Log.i(logTag, "This is an Event update");
				ClassPeople sender = giveMePerson(phoneNumber, context,
						preferred_name);
				String processedSMS = decodingComnandFromSms(sender, smsBody);
				if (processedSMS == null) {
					// Either a failed request, or just a troller, send to
					// inbox.
					updateGCMInbox(msg, context);
					break;
				}

				if (executeSmsCommand(processedSMS, sender, context)) {
					Log.i(logTag, "Event title we will be broadcasting: "
							+ event.title);
					notifyUser(event.title, sender.name, context);
				}
				break;
			case 2: // Event Creation
				Log.i(logTag, "This is an Event Creation");
				ClassPeople creator = giveMePerson(phoneNumber, context,
						preferred_name);
				createEventFromSms(smsBody, creator, context);
				notifyUser(creator.name, context);
			}
		} else {
			updateGCMInbox(msg, context);
		}
	}

	private void updateGCMInbox(String msg, Context context) {
		// Auto-generated method stub
		if (ClassUniverse.universeAllEventHashLookUp.get(("Your GCM Inbox"
				+ "00000000000").hashCode()) == null) {
			ClassEvent event = ClassUniverse.universeAllEventHashLookUp
					.get(("Your GCM Inbox" + "00000000000").hashCode());
			event.updateChatLog("external", msg, "GCM-Bot ");
		} else {
			ClassEvent newEvent = new ClassEvent("Your GCM Inbox", "N/A",
					"Messages sent to your GCM Address", "N/A", "N/A");
			newEvent.serverIdCode = "000";
			ClassUniverse.registerEvent(newEvent);
			ClassPeople GCM = new ClassPeople("GCM Bot", "phoneNumber",
					"00000000000");
			newEvent.makeHost(GCM);
			newEvent.updateChatLog("external", msg, "GCM-Bot ");
		}
		// Sending out Intent if Chatroom was active...
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction("SMS_RECEIVED_ACTION");
		broadcastIntent.putExtra("smsFor" + "Your GCM Inbox", msg);
		context.sendBroadcast(broadcastIntent);
		notifyUser("GCM Inbox", "GCM Bot", context);
	}

	@Override
	public void onRegistered(Context context, String registrationId) {
		Log.i(LOG_TAG, "Device was registered: onRegistered()");
		// String deviceID = Secure.getString(context.getContentResolver(),
		// Secure.ANDROID_ID);
		ClassUniverse.regId = registrationId;
		ClassUniverse.GcmRegError = "";
		ClassUniverse.GCMEnabled = true;
		registerDevice(ClassUniverse.device_id, registrationId);

	}

	@Override
	protected void onUnregistered(Context context, String s) {
		Log.i(LOG_TAG, "onUnregistered()");
		// String deviceID = Secure.getString(context.getContentResolver(),
		// Secure.ANDROID_ID);
		ClassUniverse.regId = "";
		ClassUniverse.GCMEnabled = false;
		unregisterDevice(ClassUniverse.device_id);
	}

	public void registerDevice(String deviceID, String registrationId) {
		Log.i(LOG_TAG,
				"Generating device ID parameters to send to server: registerDevice");

		String url = API_URL + "register/";
		Log.i(LOG_TAG, "Destination url: " + url);

		RestClient client = new RestClient(url);
		client.addPostParam(new BasicNameValuePair("dev_id", deviceID));
		client.addPostParam(new BasicNameValuePair("reg_id", registrationId));
		Log.i(LOG_TAG, "Starting to execute request...");
		if (client.execute()) {
			registerPhone();
			;
		}
	}

	public void unregisterDevice(String deviceID) {
		String url = API_URL + "unregister/";
		Log.i(LOG_TAG, "Destination url: " + url);

		RestClient client = new RestClient(url);

		client.addPostParam(new BasicNameValuePair("dev_id", deviceID));
		client.execute();

	}

	public class RestClient {
		private final String LOG_TAG = "MJ---> RestClient";

		private ArrayList<NameValuePair> headers;
		private ArrayList<NameValuePair> postParams;
		private String url;

		public RestClient(String url) {
			this.url = url;
			headers = new ArrayList<NameValuePair>();
			postParams = new ArrayList<NameValuePair>();
		}

		private void setHeadersParameters(HttpUriRequest request) {
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}
			Log.i(LOG_TAG,
					"Header(if any) added to request. This was content of header: "
							+ headers.toString() + ". django overrides them :)");
		}

		public void addPostParam(NameValuePair param) {
			postParams.add(param);
		}

		public JSONObject getJsonObject() {
			JSONObject jsonObj = new JSONObject();

			for (NameValuePair p : postParams) {
				try {
					jsonObj.put(p.getName(), p.getValue());
				} catch (JSONException e) {
					Log.e(LOG_TAG, "JSONException: " + e);
				}
			}
			Log.i(LOG_TAG,
					"Just created json pair Post Params: " + jsonObj.toString());
			return jsonObj;
		}

		public boolean execute() {
			Log.i(LOG_TAG,
					"Execute Rest Client: will add Parameters, then launch");
			HttpPost request = new HttpPost(url);
			setHeadersParameters(request);
			Log.i(LOG_TAG, "Execute(): setting entity");
			try {
				StringEntity entity = new StringEntity(getJsonObject()
						.toString(), HTTP.UTF_8);
				entity.setContentType("application/json");
				request.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
				Log.i(LOG_TAG, "UnsupportedEncodingException: " + e);
				return false;
			}

			Log.i(LOG_TAG, "Execute: Firing Request, will print code");
			HttpClient client = new DefaultHttpClient();
			try {

				HttpResponse httpResponse = client.execute(request);
				Integer responseCode = httpResponse.getStatusLine()
						.getStatusCode();
				String responseMessage = httpResponse.getStatusLine()
						.getReasonPhrase();

				Log.i(LOG_TAG, "Response code: " + responseCode);
				Log.i(LOG_TAG, "Response message: " + responseMessage);
				return responseCode == HttpStatus.SC_OK;

			} catch (ClientProtocolException e) {
				client.getConnectionManager().shutdown();
				Log.e(LOG_TAG, "ClientProtocolException: " + e);
			} catch (IOException e) {
				client.getConnectionManager().shutdown();
				Log.e(LOG_TAG, "IOException: " + e);
			} catch (Exception e) {
				Log.e(LOG_TAG, "Exception: " + e);
			}
			return false;
		}
	}

	public void createEventFromSms(String smsBody, ClassPeople creator,
			Context context) {
		Log.i(logTag, "This was a valid Event Creating GCM Command");
		ArrayList<String> out = new ArrayList<String>();
		Pattern pattern = Pattern.compile("%(.*?)%");
		String processedBody = smsToCreateCommand(smsBody);
		Matcher matcher = pattern.matcher(processedBody);
		while (matcher.find()) {
			Log.i(logTag, matcher.group());
			out.add(matcher.group(1));
		}
		event = new ClassEvent(out.get(0), out.get(4), out.get(1), out.get(2),
				out.get(3));
		ClassUniverse.registerEvent(event);
		event.makeHost(creator);
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

	private String smsToCreateCommand(String smsBody) {
		String out = "";
		out = smsBody.replaceFirst("NEW EVENT! Title: ", "%");
		out = out.replaceFirst(", Desc: ", "% %");
		out = out.replaceFirst(", Time: ", "% %");
		out = out.replaceFirst(", Date: ", "% %");
		out = out.replaceFirst(", Loc: ", "% %");
		out = out.replaceFirst(", PS:", "%");
		return out;
	}

	public int getSmsType(String smsBody) {
		Log.i(logTag, "Checking what type of message we are dealing with");
		if (smsBody.contains("NEW EVENT! Title: ")
				&& smsBody.contains("PS: No InstaPlan? Add %E")) {
			Log.i(logTag, "This was a create event command, returning 2");
			return 2;
		}
		if (smsBody.contains("%E")) {
			Log.i(logTag,
					"This could be an sms Event update, we will check that, returning 1");
			return 1;
		}
		if (smsBody.contains("/-%")) {
			Log.i(logTag,
					"This could be an sms Event update, we will check that, returning 1");
			return 1;
		}
		return 0;
	}

	private boolean executeSmsCommand(String processedSMS, ClassPeople sender,
			Context context) {
		boolean out = false;

		Log.i(logTag, "About to executeSmsCommand");
		if (event != null) {
			if (sender.isParticipatingIn(event) || isnew) {
				Log.i(logTag, sender.name + " is associated with this event: "
						+ processedSMS + "Updating Event's chatlog");
				String postUpdate = "";
				Log.i(logTag, "This is Processed get 1: " + processedSMS);
				if (processedSMS.contains("%N%")) {
					Log.i(logTag, "%N% was found inside...");
					// This means, the post is NOT mine, so do not from the
					// sender, but on behalf of someone else... :D
					postUpdate = processedSMS.replace("%N%", "");
				} else {
					Log.i(logTag, "%N% was NOT found inside...");
					postUpdate += sender.name;
					postUpdate += ": ";
					postUpdate += processedSMS;
					Log.i(logTag, " Post Created = " + postUpdate);
				}

				if (isnew) {
					// event.invite(person); //Plan 2 right now... sending back
					// to host, and host will spread the post.
				}
				event.updateChatLog("external", postUpdate, sender.name);

				if (event.isMine) {
					spreadPost(sender, processedSMS);
				}

				// Sending out Intent if Chatroom was active...
				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction("SMS_RECEIVED_ACTION");
				broadcastIntent.putExtra("smsFor" + event.title, postUpdate);
				context.sendBroadcast(broadcastIntent);
				out = true;
			} else {
				Log.i(logTag,
						"Person not participating in event AND is not new");
			}

		} else {
			Log.i(logTag, "Couldn't Resolve Event... Index out of range?");
		}
		return out;

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

	public void registerPhone() {
		URL url;
		String userName = ClassUniverse.mUserName;
		String userEmail = ClassUniverse.mEmail;
		if (ClassUniverse.mUserName.equals("")) {
			userName = "Anonymous";
		}
		if (userEmail.equals("")) {
			userEmail = "anonymous@noreply.com";
		}
		String strUrl = "http://mj-server.mit.edu/instaplan/registerPhone/"
				+ "?phoneNumber="
				+ URLEncoder.encode(ClassUniverse.mPhoneNumber) + "&username="
				+ URLEncoder.encode(userName) + "&dev_id="
				+ URLEncoder.encode(ClassUniverse.device_id) + "&useremail="
				+ URLEncoder.encode(userEmail);
		try {
			// String myUrl = URLEncoder.encode(strUrl, "UTF-8");
			// url = new URL(myUrl);
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
				Log.i(TAG, "Successfully registered device: code " + out);
			} else {
				Log.i(TAG, "Device Registration Failed: code " + out
						+ " reply: " + server_reply);
			}
			return;
		} catch (MalformedURLException e) {
			Log.i(TAG, "ERROR SENDING GCM... MalFormedUrl");
			Log.i(logTag, "ERROR SENDING GCM... MalFormedUrl");
			return;
		} catch (IOException e) {
			Log.i(TAG, "ERROR SENDING GCM... IOEX..");
			Log.i(logTag, "ERROR SENDING GCM... IOEX..");
			return;
		}
	}

	private Void spreadPost(ClassPeople sender, String raw_sms_without_tags) {

		Log.i(logTag, "Spread Post GCM!!!");

		Log.i(logTag,
				"This was my event, I'm now spreading it... POST UPDATE: "
						+ raw_sms_without_tags);
		// 1. Person is appless
		String post_content_appless = sender.name + ": " + raw_sms_without_tags;

		// 2. Person has app.
		String post_content_sms = "%N%" + post_content_appless + " /-%"
				+ event.eventHash + "%-/";

		// 3. Person has gcm.
		String post_content_gcm = raw_sms_without_tags + " /-%"
				+ event.eventHash + "%-/";
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
								sender.phoneNumber, post_content_gcm,
								post_content_sms);
						continue;
					} else {
						sms.sendTextMessage(invitee.phoneNumber, null,
								post_content_sms, null, null);
						Log.i(logTag, "Sent Sms to: " + invitee.name
								+ " with tags, Content: " + post_content_sms);
					}

				} else {
					sms.sendTextMessage(invitee.phoneNumber, null,
							post_content_appless, null, null);
					Log.i(logTag, "Sent Sms to: " + invitee.name
							+ " with tags, Content: " + post_content_appless);
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
							+ " without tags. Content: " + post_content_appless);
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
			content_sms = params[3];

			String strUrl = "http://mj-server.mit.edu/instaplan/command/"
					+ event.serverIdCode + "/?command=sendSmsTo"
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
				Log.i(logTag,
						"Successfully sent GCM Message to: "
								+ ClassUniverse.universePhoneNumberLookUp
										.get(senderPhoneNumber).name);
			} else {
				Log.i(logTag,
						"Failed to send GCM Message to: "
								+ ClassUniverse.universePhoneNumberLookUp
										.get(senderPhoneNumber).name);
				ClassUniverse.universePhoneNumberLookUp.get(TophoneNumber).hasGCM = false;
				SmsManager sms = SmsManager.getDefault();
				sms.sendTextMessage(TophoneNumber, null, content_sms, null,
						null);
			}
		}

	}

	private ClassPeople giveMePerson(String phoneNumber, Context context,
			String preferred_name) {

		// //////////////////////////
		String senderName = getContactDisplayNameByNumber(phoneNumber, context);
		// //////////////////////////

		// Try to get the person iff name is found.
		if (senderName != null) {
			// If person was in contacts & associated with our app
			// (update his/her contact name)
			if (ClassUniverse.universePhoneNumberLookUp
					.containsKey(phoneNumber)) {
				ClassUniverse.universePhoneNumberLookUp.get(phoneNumber).name = senderName;

				// Person in contacts, but NOT associated with our app
				// yet. Register person
			} else {
				isnew = true;
				new ClassPeople(senderName, "phoneNumber", phoneNumber);
			}
		} else {
			// If person not in contacts and NOT yet associated with our
			// app: Register the person with preferred name
			if (!ClassUniverse.universePhoneNumberLookUp
					.containsKey(phoneNumber)) {

				new ClassPeople(preferred_name + " -gcm", "phoneNumber",
						phoneNumber);
				isnew = true;
			} else {
				ClassUniverse.universePhoneNumberLookUp.get(phoneNumber).name = preferred_name
						+ " -gcm";
			}
		}
		ClassPeople sender = ClassUniverse.universePhoneNumberLookUp
				.get(phoneNumber);
		Log.i(logTag, "Give me Person outputs: " + sender.name);
		return sender;

	}

	public String decodingComnandFromSms(ClassPeople person, String smsBody) {
		/**
		 * Takes a sender (ClassPeople), modifies sender.hasApp and .hasGCM
		 * accordingly, Returns null if this sms was NOT associated with our
		 * app, smsbody without tags otherwise.
		 * 
		 * @param smsBody
		 *            raw sms body
		 * @param person
		 * @return null if this sms was NOT associated with our app, smsbody
		 *         without tags otherwise.
		 */
		Pattern pattern = Pattern.compile("%E(\\d*)%");
		Matcher matcher = pattern.matcher(smsBody);
		if (matcher.find()) {
			int index = Integer.parseInt(matcher.group(1));

			Log.i(logTag, "A valid SMS Tag was found: " + matcher.group(1));
			Log.i(logTag, "Setting person to APPLESS mode");
			person.hasApp = false;
			person.hasGCM = false; // not really... lol

			if (ClassUniverse.universeAllMyEventCreationNumberLookUp.get(index) != null) {
				// Event was found!
				event = ClassUniverse.universeAllMyEventCreationNumberLookUp
						.get(index);
				Log.i(logTag, "Event found, from APPLESS PERSON : "
						+ event.title);
				return smsBody.replaceAll("%E" + matcher.group(1) + "%", "");
			} else {
				// Event was missed.
				Log.i(logTag, "No event with creation Number: " + (index));
				return null;
			}
		} else {
			pattern = Pattern.compile("/-%(.*?)%-/");
			matcher = pattern.matcher(smsBody);
			if (matcher.find()) {
				int hashReceived = Integer.parseInt(matcher.group(1));

				person.hasApp = true;
				person.hasGCM = true; // not really... lol

				if (ClassUniverse.universeAllEventHashLookUp.get(hashReceived) != null) {
					// Event Found!
					event = ClassUniverse.universeAllEventHashLookUp
							.get(hashReceived);
					Log.i(logTag, "Valid event: " + event.title);
					return smsBody.replaceAll("/-%" + matcher.group(1) + "%-/",
							"");
				} else {
					// Event Missed!
					Log.i(logTag, "INVALID EVENT: " + matcher.group(1));
					return null;
				}

			} else {
				Log.i(logTag, "This is a REGULAR SMS!!");
				return null;
			}

		}
	}

}