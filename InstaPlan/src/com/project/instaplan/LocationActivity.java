package com.project.instaplan;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.text.format.Time;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@SuppressLint({ "ValidFragment", "HandlerLeak" })
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class LocationActivity extends FragmentActivity implements
		View.OnClickListener {
	String tag = "MJ------>";
	Time now = new Time();
	TextView mLatLng;
	TextView mAddress;
	Button mFineProviderButton, location_refresh_button;
	Button mBothProviderButton;
	LocationManager mLocationManager;
	Handler mHandler;
	boolean mGeocoderAvailable;
	boolean mUseFine;
	boolean mUseBoth;
	FileOutputStream FOS;
	File sdCard = Environment.getExternalStorageDirectory();
	File instaPlanDirHomeWork = new File(sdCard.getAbsolutePath()
			+ "/InstaPlan/HomeWork/");

	// Keys for maintaining UI states after rotation.
	private static final String KEY_FINE = "use_fine";
	private static final String KEY_BOTH = "use_both";
	// UI handler codes.
	private static final int UPDATE_ADDRESS = 1;
	private static final int UPDATE_LATLNG = 2;

	// private static final int TEN_SECONDS = 10000;
	// private static final int TEN_METERS = 10;
	// private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int TWO_MINUTES = 20;

	/**
	 * This sample demonstrates how to incorporate location based services in
	 * your app and process location updates. The app also shows how to convert
	 * lat/long coordinates to human-readable addresses.
	 */

	@SuppressLint("HandlerLeak")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instaPlanDirHomeWork.mkdirs();
		setContentView(R.layout.layout_location);
		Log.i(tag, "Initializing layout variables");
		initializingLayoutVariables();
		Log.i(tag, "Done initializing all layout variables");

		location_refresh_button.setOnClickListener(this);

		Log.i(tag, "Checking for saved Instances..");
		// Restore apps state (if exists) after rotation.
		if (savedInstanceState != null) {
			mUseFine = savedInstanceState.getBoolean(KEY_FINE);
			mUseBoth = savedInstanceState.getBoolean(KEY_BOTH);
		} else {
			mUseFine = false;
			mUseBoth = false;
		}
		Log.i(tag, "Done checking for saved Instances..");

	}

	public void initializingLayoutVariables() {
		// TODO Auto-generated method stub
		location_refresh_button = (Button) findViewById(R.id.location_refresh_button);
		mLatLng = (TextView) findViewById(R.id.latlng);
		mAddress = (TextView) findViewById(R.id.address);
		mFineProviderButton = (Button) findViewById(R.id.provider_fine);
		mBothProviderButton = (Button) findViewById(R.id.provider_both);
		mGeocoderAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD
				&& Geocoder.isPresent();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Handler for updating text fields on the UI
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_ADDRESS:
					mAddress.setText((String) msg.obj);
					Log.i(tag, "Updated Address " + (String) msg.obj);
					break;
				case UPDATE_LATLNG:
					mLatLng.setText((String) msg.obj);
					Log.i(tag, "Updated Latitude/Longitude " + (String) msg.obj);
					break;
				}
			}
		};
	}

	// Restores UI states after rotation.
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_FINE, mUseFine);
		outState.putBoolean(KEY_BOTH, mUseBoth);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setup();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Check if the GPS setting is currently enabled on the device.
		// This verification should be done during onStart() because the system
		// calls this method
		// when the user returns to the activity, which ensures the desired
		// location provider is
		// enabled each time the activity resumes from the stopped state.

		Log.i(tag, "Currently in onStart()");

		Log.i(tag, "Checking if the provider is enabled");
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		final boolean gpsEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!gpsEnabled) {
			Log.i(tag, "GPS was not enabled, now launching dialog!");
			// Build an alert dialog here that requests that the user enable
			// the location services, then when the user clicks the "OK" button,
			// call enableLocationSettings()

			new EnableGpsDialogFragment().show(getSupportFragmentManager(),
					"enableGpsDialog");
		}
	}

	// Method to launch Settings
	private void enableLocationSettings() {
		Log.i(tag,
				"Currently in enableLocationSettings() will launch settings!");
		Intent settingsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		startActivity(settingsIntent);
	}

	// Stop receiving location updates whenever the Activity becomes invisible.
	@Override
	protected void onStop() {
		super.onStop();
		Log.i(tag, "Removing updates... stoping to listen...");
		mLocationManager.removeUpdates(listener);
	}

	// Set up fine and/or coarse location providers depending on whether the
	// fine provider or
	// both providers button is pressed.
	private void setup() {
		Log.i(tag, "Currently in Setup!");
		Location gpsLocation = null;
		Location networkLocation = null;
		mLocationManager.removeUpdates(listener);
		mLatLng.setText(R.string.unknown);
		mAddress.setText(R.string.unknown);

		// Get fine location updates only.
		if (mUseFine) {

			Log.i(tag, "Use Fine was turned on!");
			mFineProviderButton.setBackgroundResource(R.drawable.button_active);
			mBothProviderButton
					.setBackgroundResource(R.drawable.button_inactive);

			// Request updates from just the fine (gps) provider.
			Log.i(tag, "Requesting update from gps location provider");
			gpsLocation = requestUpdatesFromProvider(
					LocationManager.GPS_PROVIDER, R.string.not_support_gps);

			// Update the UI immediately if a location is obtained.

			if (gpsLocation != null) {
				Log.i(tag,
						"A valid gps Location was returned... running updateUI...");
				updateUILocation(gpsLocation);
				Log.i(tag, "Done updating UI");
			} else {
				Log.i(tag, "Obtained gps location was not valid!");
			}

		} else if (mUseBoth) {
			Log.i(tag, "Use both was checked");
			// Get coarse and fine location updates.
			mFineProviderButton
					.setBackgroundResource(R.drawable.button_inactive);
			mBothProviderButton.setBackgroundResource(R.drawable.button_active);
			// Request updates from both fine (gps) and coarse (network)
			// providers.
			Log.i(tag, "Requesting update from both location providers");
			gpsLocation = requestUpdatesFromProvider(
					LocationManager.GPS_PROVIDER, R.string.not_support_gps);
			networkLocation = requestUpdatesFromProvider(
					LocationManager.NETWORK_PROVIDER,
					R.string.not_support_network);

			// If both providers return last known locations, compare the two
			// and use the better
			// one to update the UI. If only one provider returns a location,
			// use it.
			if (gpsLocation != null && networkLocation != null) {
				Log.i(tag, "We got both GPS and wifi location");
				Log.i(tag,
						"These were the locations obtained: "
								+ gpsLocation.toString() + " and network: "
								+ networkLocation.toString());
				updateUILocation(getBetterLocation(gpsLocation, networkLocation));
			} else if (gpsLocation != null) {
				Log.i(tag,
						"These were the locations obtained: "
								+ gpsLocation.toString());
				Log.i(tag, "We got only gps location");
				updateUILocation(gpsLocation);
			} else if (networkLocation != null) {
				Log.i(tag, "These were the locations obtained: and network: "
						+ networkLocation.toString());
				Log.i(tag, "We got only wifi location");
				updateUILocation(networkLocation);
			}
		}
	}

	/**
	 * Method to register location updates with a desired location provider. If
	 * the requested provider is not available on the device, the app displays a
	 * Toast with a message referenced by a resource id.
	 * 
	 * @param provider
	 *            Name of the requested provider.
	 * @param errorResId
	 *            Resource id for the string message to be displayed if the
	 *            provider does not exist on the device.
	 * @return A previously returned {@link android.location.Location} from the
	 *         requested provider, if exists.
	 */
	private Location requestUpdatesFromProvider(final String provider,
			final int errorResId) {
		Log.i(tag, "Requesting update from (provider): " + provider);
		Location location = null;
		if (mLocationManager.isProviderEnabled(provider)) {
			// mLocationManager.requestLocationUpdates(provider, TEN_SECONDS,
			// TEN_METERS, listener);
			Log.i(tag, "Provider is Enabled!");
			mLocationManager.requestLocationUpdates(provider, 0, 0, listener);
			location = mLocationManager.getLastKnownLocation(provider);
			if (location != null) {
				Log.i(tag, "Location we got: " + location.toString());
			}
		} else {
			Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
		}
		return location;
	}

	// Callback method for the "fine provider" button.
	public void useFineProvider(View v) {
		mUseFine = true;
		mUseBoth = false;
		setup();
	}

	// Callback method for the "both providers" button.
	public void useCoarseFineProviders(View v) {
		mUseFine = false;
		mUseBoth = true;
		setup();
	}

	private void doReverseGeocoding(Location location) {
		// Since the geocoding API is synchronous and may take a while. You
		// don't want to lock
		// up the UI thread. Invoking reverse geocoding in an AsyncTask.
		(new ReverseGeocodingTask(this)).execute(new Location[] { location });
	}

	private void updateUILocation(Location location) {
		// We're sending the update to a handler which then updates the UI with
		// the new
		// location.
		// Log.i(tag, "Just received update: " + location.getLatitude() + ", "
		// + location.getLongitude());
		if (location != null) {
			Log.i(tag, "UpdataUI with location: " + location.toString());
			Message.obtain(
					mHandler,
					UPDATE_LATLNG,
					location.getProvider() + ", " + location.getLatitude()
							+ ", " + location.getLongitude() + ", "
							+ location.getAccuracy()).sendToTarget();

			// Bypass reverse-geocoding only if the Geocoder service is
			// available on
			// the device.
			if (mGeocoderAvailable)
				Log.i(tag, "Geocoder was available...");
			doReverseGeocoding(location);
		}
	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// A new location update is received. Do something useful with it.
			// Update the UI with
			// the location update.
			Log.i(tag, "Listener just recieved a new location... ;D "
					+ location.toString());
			now.setToNow();
			String logged_report = "\n(automated) Time " + Integer.toString(now.hour)
					+ ":" + Integer.toString(now.minute) + ":"
					+ Integer.toString(now.second) + " Update from " + location.getProvider()
					+ ": Latitude " + location.getLatitude() + ", Longitude " + location.getLongitude()
					+ ". Accuracy: " + location.getAccuracy();
			Log.i(tag, "Now writing to log!!");
			File file = new File(instaPlanDirHomeWork, "Log_LocationFile.txt");
			try {
				FOS = new FileOutputStream(file, true);
				FOS.write(logged_report.getBytes());
				FOS.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(tag, "Done writing to log!! Now updating UI");
			updateUILocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix. Code taken from
	 * http://developer.android.com/guide/topics/location
	 * /obtaining-user-location.html
	 * 
	 * @param newLocation
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 * @return The better Location object based on "recency" and accuracy.
	 */
	protected Location getBetterLocation(Location newLocation,
			Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return newLocation;
		}
		if (newLocation != null) {
			Log.i(tag,
					"Now comparing 2 locations... :" + newLocation.toString()
							+ " with old: " + currentBestLocation.toString());

			// Check whether the new location fix is newer or older
			long timeDelta = newLocation.getTime()
					- currentBestLocation.getTime();
			boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
			boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
			boolean isNewer = timeDelta > 0;

			// If it's been more than two minutes since the current location,
			// use
			// the new location
			// because the user has likely moved.
			if (isSignificantlyNewer) {
				return newLocation;
				// If the new location is more than two minutes older, it must
				// be
				// worse
			} else if (isSignificantlyOlder) {
				return currentBestLocation;
			}

			// Check whether the new location fix is more or less accurate
			int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation
					.getAccuracy());
			Log.i(tag,
					"Accuracies: new = "
							+ Float.toString(newLocation.getAccuracy())
							+ " old = "
							+ Float.toString(currentBestLocation.getAccuracy()));
			boolean isLessAccurate = accuracyDelta > 0;
			boolean isMoreAccurate = accuracyDelta < 0;
			boolean isSignificantlyLessAccurate = accuracyDelta > 200;

			// Check if the old and new location are from the same provider
			boolean isFromSameProvider = isSameProvider(
					newLocation.getProvider(),
					currentBestLocation.getProvider());

			// Determine location quality using a combination of timeliness and
			// accuracy
			if (isMoreAccurate) {
				Log.i(tag, "Done: New location is more accurate.");
				return newLocation;
			} else if (isNewer && !isLessAccurate) {
				Log.i(tag, "Done: New location is newer and of same accuracy.");
				return newLocation;
			} else if (isNewer && !isSignificantlyLessAccurate
					&& isFromSameProvider) {
				Log.i(tag,
						"Done: New location is newer and from same provider.");
				return newLocation;
			}
			return currentBestLocation;
		}
		Log.i(tag, "Both locations were Nulls!");
		return currentBestLocation;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	// AsyncTask encapsulating the reverse-geocoding API. Since the geocoder API
	// is blocked,
	// we do not want to invoke it from the UI thread.
	private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
		Context mContext;

		public ReverseGeocodingTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			Log.i(tag, "DoInBackground... ");
			Location loc = params[0];
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
				// Update address field with the exception.
				Message.obtain(mHandler, UPDATE_ADDRESS, e.toString())
						.sendToTarget();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				String addressText = String.format(
						"%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality(),
						address.getCountryName());
				// Update address field on UI.
				Message.obtain(mHandler, UPDATE_ADDRESS, addressText)
						.sendToTarget();
			}
			return null;
		}
	}

	/**
	 * Dialog to prompt users to enable GPS on the device.
	 */

	private class EnableGpsDialogFragment extends DialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setTitle(R.string.enable_gps)
					.setMessage(R.string.enable_gps_dialog)
					.setPositiveButton(R.string.enable_gps,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									enableLocationSettings();
								}
							}).create();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		String[] raw = mLatLng.getText().toString().split(", ");
		now.setToNow();
		String logged_report = "\n(requested) Time " + Integer.toString(now.hour)
				+ ":" + Integer.toString(now.minute) + ":"
				+ Integer.toString(now.second) + " Update from " + raw[0]
				+ ": Latitude " + raw[1] + ", Longitude " + raw[2]
				+ ". Accuracy: " + raw[3];
		Log.i(tag, "Now writing to log!!");
		File file = new File(instaPlanDirHomeWork, "Log_LocationFile.txt");
		try {
			FOS = new FileOutputStream(file, true);
			FOS.write(logged_report.getBytes());
			FOS.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i(tag, "Done writing to log!! Now updating UI");
	}
}
