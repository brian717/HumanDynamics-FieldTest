/**
 * Funf: Open Sensing Framework
 * Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland. 
 * Acknowledgments: Alan Gardner
 * Contact: nadav@media.mit.edu
 * 
 * This file is part of Funf.
 * 
 * Funf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of 
 * the License, or (at your option) any later version. 
 * 
 * Funf is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with Funf. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.mit.media.realityanalysis.fieldtest;

import static edu.mit.media.funf.AsyncSharedPrefs.async;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import edu.mit.media.funf.IOUtils;
import edu.mit.media.funf.Utils;
import edu.mit.media.funf.configured.ConfiguredPipeline;
import edu.mit.media.funf.configured.FunfConfig;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.storage.BundleSerializer;
import edu.mit.media.funf.storage.UploadService;

public class MainPipeline extends ConfiguredPipeline {
	
	public static final String TAG = "MainPipeline";	
	
	public static final String MAIN_CONFIG = "main_config";
	public static final String START_DATE_KEY = "START_DATE";

	public static final String ACTION_RUN_ONCE = "RUN_ONCE";
	public static final String RUN_ONCE_PROBE_NAME = "PROBE_NAME";	
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		String password = getDataPassword(this);
		
		// If the user hasn't logged in yet, we need to stop the service 
		// NOTE: this currently doesn't work properly because we're just using the default password (changeme)
		if (password == null || password =="") {
			Intent loginIntent = new Intent(this, LoginActivity.class);
			startActivity(loginIntent);
			this.stopSelf();
		} else {		
			setEncryptionPassword(getDataPassword(this).toCharArray());
		}
		
		//Setup the notification service to run periodically
		Intent notificationIntent = new Intent(this, NotificationService.class);

		PendingIntent notificationPendingIntent = PendingIntent.getService(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
	
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);		
		
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_HALF_HOUR, notificationPendingIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ACTION_RUN_ONCE.equals(intent.getAction())) {
			String probeName = intent.getStringExtra(RUN_ONCE_PROBE_NAME);
			runProbeOnceNow(probeName);
		} else if (ACTION_UPLOAD_DATA.equals(intent.getAction())) {
				uploadData();
			}
		else {
			super.onHandleIntent(intent);
		}
	}
	
	public Class<? extends UploadService> getUploadServiceClass() {
		return HttpsUploadService.class;
	}
	
	@Override
	public BundleSerializer getBundleSerializer() {
		return new BundleToJson();
	}
	
	public static class BundleToJson implements BundleSerializer {
		public String serialize(Bundle bundle) {
			return JsonUtils.getGson().toJson(Utils.getValues(bundle));
		}
		
	}
	
	public void uploadData() {
		archiveData();
		String archiveName = getPipelineName();
		String uploadUrl = getConfig().getDataUploadUrl();
		Intent i = new Intent(this, getUploadServiceClass());
		i.putExtra(UploadService.ARCHIVE_ID, archiveName);
		i.putExtra(UploadService.REMOTE_ARCHIVE_ID, uploadUrl);
		startService(i);
		getSystemPrefs().edit().putLong(LAST_DATA_UPLOAD, System.currentTimeMillis()).commit();
	}

	@Override
	public void onDataReceived(Bundle data) {
		super.onDataReceived(data);
		incrementCount();
	}

	public static final String SCAN_COUNT_KEY = "SCAN_COUNT";
	
	public static long getScanCount(Context context) {
		return getSystemPrefs(context).getLong(SCAN_COUNT_KEY, 0L);
	}
	
	private void incrementCount() {
		boolean success = false;
		while(!success) { 
			SharedPreferences.Editor editor = getSystemPrefs().edit();
			editor.putLong(SCAN_COUNT_KEY, getScanCount(this) + 1L);
			success = editor.commit();
		}
	}

	public static boolean isEnabled(Context context) {
		return getSystemPrefs(context).getBoolean(ENABLED_KEY, true);
	}
	
	@Override
	public SharedPreferences getSystemPrefs() {
		return getSystemPrefs(this);
	}
	
	public static SharedPreferences getSystemPrefs(Context context) {
		return async(context.getSharedPreferences(context.getString(R.string.prefs_file), MODE_PRIVATE));
	}
	
	@Override
	public FunfConfig getConfig() {
		return getMainConfig(this);
	}
	
	public class LocalBinder extends Binder {
		public MainPipeline getPipeline() {
			return MainPipeline.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}
	
	/**
	 * Easy access to Funf config.  
	 * As long as this service is running, changes will be automatically picked up.
	 * @param context
	 * @return
	 */
	public static FunfConfig getMainConfig(Context context) {
		FunfConfig config = getConfig(context, MAIN_CONFIG);
		if (config.getName() == null) {			
			String jsonString = getStringFromAsset(context, "default_config.json");
			if (jsonString == null) {
				Log.e(TAG, "Error loading default config.  Using blank config.");
				jsonString = "{}";
			}
			try {
				config.edit().setAll(jsonString).commit();
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing default config", e);
			}
		}
		return config;
	}
	
	public static String getStringFromAsset(Context context, String filename) {
		InputStream is = null;
		try {
			is = context.getAssets().open(filename);
			return IOUtils.inputStreamToString(is, Charset.defaultCharset().name());
		} catch (IOException e) {
			Log.e(TAG, "Unable to read asset to string", e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, "Unable to close asset input stream", e);
				}
			}
		}
	}
	
	public void runProbeOnceNow(final String probeName) {
		FunfConfig config = getMainConfig(this);
		ArrayList<Bundle> updatedRequests = new ArrayList<Bundle>();
		Bundle[] existingRequests = config.getDataRequests(probeName);
		if (existingRequests != null) {
			for (Bundle existingRequest : existingRequests) {
				updatedRequests.add(existingRequest);
			}
		}
		
		Bundle oneTimeRequest = new Bundle();
		oneTimeRequest.putLong(Probe.Parameter.Builtin.PERIOD.name, 0L);
		updatedRequests.add(oneTimeRequest);
		
		Intent request = new Intent(Probe.ACTION_REQUEST);
		request.setClassName(this, probeName);
		request.putExtra(Probe.CALLBACK_KEY, getCallback());
		request.putExtra(Probe.REQUESTS_KEY, updatedRequests);
		startService(request);
	}
  
	public final String getDataPassword(Context context) {
		//SharedPreferences prefs = getSystemPrefs(context);
		//String password = prefs.getString(context.getString(R.string.password_prefs_key), null); 
		return this.getString(R.string.default_funf_password);
	}
	
}
