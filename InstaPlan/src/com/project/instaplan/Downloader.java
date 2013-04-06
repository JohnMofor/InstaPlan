package com.project.instaplan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Downloader extends Activity implements View.OnClickListener {

	TextView editText_url;
	Button downloader_fast_download_button;
	Button downloader_slow_download_button;

	String tag = "MJ------->";
	ProgressDialog mProgressDialog;
	FileOutputStream FOS;
	File sdCard = Environment.getExternalStorageDirectory();
	File instaPlanDirHomeWork = new File(sdCard.getAbsolutePath()
			+ "/InstaPlan/HomeWork/");
	File instaPlanDirData = new File(sdCard.getAbsolutePath()
			+ "/InstaPlan/Data/");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_downloader);
		editText_url = (TextView) findViewById(R.id.downloader_editText);
		downloader_fast_download_button = (Button) findViewById(R.id.downloader_fast_download_button);
		downloader_slow_download_button = (Button) findViewById(R.id.downloader_slow_download_button);
		mProgressDialog = new ProgressDialog(Downloader.this);
		mProgressDialog.setMessage("A certain message");
		mProgressDialog.setIndeterminate(false);
		mProgressDialog.setMax(100);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		downloader_fast_download_button.setOnClickListener(this);
		downloader_slow_download_button.setOnClickListener(this);
	}

	public static boolean isDownloadManagerAvailable(Context context) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
				return false;
			}
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setClassName("com.android.providers.downloads.ui",
					"com.android.providers.downloads.ui.DownloadList");
			List<ResolveInfo> list = context.getPackageManager()
					.queryIntentActivities(intent,
							PackageManager.MATCH_DEFAULT_ONLY);
			return list.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	public void doDownload(Context context, String url) {
		DownloadManager.Request request = new DownloadManager.Request(
				Uri.parse(url));
		request.setDescription("Some descrition");
		request.setTitle("Some title");
		// in order for this if to run, you must use the android 3.2 to compile
		// your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			request.allowScanningByMediaScanner();
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS, getName(url));

		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) context
				.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}

	private String getName(String url) {
		// TODO Auto-generated method stub
		if (url.contains("/")) {
			String name = url.substring(url.lastIndexOf("/") + 1);
			return name;
		} else {
			Log.i(tag, "Invalid url");
			return "Invalid";
		}
	}

	@Override
	public void onClick(View view_clicked) {
		// TODO Auto-generated method stub
		Log.i(tag, "Button pressed!");
		String title = editText_url.getText().toString();
		String url_in = "http://web.mit.edu/21W.789/www/papers/griswold2004.pdf";
		DownloadFile downloadFile = new DownloadFile();
		Log.i(tag, "Download Instance created!");
		switch (view_clicked.getId()) {
		case R.id.downloader_fast_download_button:
			Log.i(tag, "Just passed Fast:)");
			downloadFile.execute(url_in, "Fast", title);
			break;
		case R.id.downloader_slow_download_button:
			Log.i(tag, "Just passed SLOW :(");
			downloadFile.execute(url_in, "Slow", title);
			break;
		}
		Log.i(tag, "Done downloading!");

		// if (isDownloadManagerAvailable(this)) {
		// Log.i(tag,
		// "Download manager was available, getting into Do download...");
		// String url = editText_url.getText().toString();
		// doDownload(this, url);
		// } else {
		// Log.i(tag, "Download manager was not available!!!");
		// }
	}

	private class DownloadFile extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... sUrl) {
			Log.i(tag, "Currently in doInBackground");

			try {
				Log.i(tag, "Creating/Getting Dirs!");
				instaPlanDirData.mkdirs();
				instaPlanDirHomeWork.mkdirs();
				Log.i(tag, "Done Creating/Getting Dirs!");

				Log.i(tag, "Opening Connection");
				long startCon = System.currentTimeMillis();
				URL url = new URL(sUrl[0]);
				URLConnection connection = url.openConnection();
				connection.setUseCaches(false);
				long connectionLatency = System.currentTimeMillis() - startCon;
				connection.connect();
				Log.i(tag, "Connection was openned");

				Log.i(tag, "Getting download pointer...");
				int fileLength = connection.getContentLength();
				// Download key: pointer from where we shall download...
				InputStream input = new BufferedInputStream(url.openStream());
				File data_file = new File(instaPlanDirData, getName(sUrl[0]));
				OutputStream output = new FileOutputStream(data_file, false);
				Log.i(tag, "Done Getting download pointer...");

				Log.i(tag, "Setting sampling pace");
				byte data_bucket[] = new byte[512];
				if (sUrl[1].contains("Slow")) {
					data_bucket = new byte[10];
				}
				Log.i(tag, "Done Setting sampling pace");

				Log.i(tag, "Start of Download & log generation");
				String logged_report = sUrl[1]
						+ " measurement of "
						+ sUrl[2]
						+ " --------------------------------------------\nLatency: "
						+ Long.toString(connectionLatency) + "ms"
						+ " File size: " + Integer.toString(fileLength)
						+ " bytes\n";
				long total = 0;
				int count = 0;

				long reference = System.currentTimeMillis();
				long total_ref = 0;
				long start = reference;
				while ((count = input.read(data_bucket)) != -1) {
					int interval = (int) (System.currentTimeMillis() - reference);
					total += count;
					// publishing the progress....
					int progress = (int) (total * 100 / fileLength);
					publishProgress(progress);
					if (interval > 4999) {
						reference = System.currentTimeMillis();
						double throughput = (total - total_ref) / 5;
						total_ref = total;
						logged_report += "Update=" + " Time now: " + reference
								+ " Currently at: "
								+ Integer.toString(progress)
								+ "% download. Calculated throughput: "
								+ Double.toString(throughput)
								+ " bytes/seconds\n";
					}
					output.write(data_bucket, 0, count);
				}
				float interval = (float) ((System.currentTimeMillis() - start) / 1000.0);
				logged_report += "Total download time: " + interval
						+ " seconds";
				logged_report += "Overall Average throughput: "
						+ Float.toString(fileLength / interval);
				logged_report += "\nEND------------------------------------------\n";
				Log.i(tag, "End of Download & log generation");

				Log.i(tag, "Now writing to log!!");
				File file = new File(instaPlanDirHomeWork,
						"Log_NetworkFile.txt");
				FOS = new FileOutputStream(file, true);
				FOS.write(logged_report.getBytes());
				Log.i(tag, "Done writing to log!!");

				Log.i(tag, "Done downloading, now closing connection..");
				output.flush();
				FOS.close();
				output.close();
				input.close();
			} catch (Exception e) {
				Log.i(tag,
						"Something screwed-up up there... lol \n"
								+ e.toString());
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mProgressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			super.onProgressUpdate(progress);
			mProgressDialog.setProgress(progress[0]);
		}

	}

}
