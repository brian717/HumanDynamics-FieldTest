package edu.mit.media.realityanalysis.fieldtest;

import java.util.Map;

import com.google.gson.JsonElement;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.Schedule;
import edu.mit.media.funf.config.Configurable;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.storage.DatabaseService;
import edu.mit.media.funf.storage.HttpUploadService;
import edu.mit.media.funf.storage.NameValueDatabaseService;
import edu.mit.media.funf.storage.UploadService;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
//import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
//import android.content.IntentSender;
import android.util.Log;

public class MainPipelineV4 extends BasicPipeline {

	public static final String LAST_DATA_UPLOAD = "LAST_DATA_UPLOAD";	
	
	@Configurable
	private String dataUploadUrl;
	
	private FunfManager mManager;
	
	@Override
	public void onCreate(FunfManager manager) {
		mManager = manager;
		super.onCreate(manager);		
		
		if (schedules.containsKey("upload")) {
			Schedule uploadSchedule = schedules.get("upload");
			manager.registerPipelineAction(this, "upload", uploadSchedule);
		}
		
		if (schedules.containsKey("archive")) {
			Schedule archiveSchedule = schedules.get("archive");
			manager.registerPipelineAction(this, "archive", archiveSchedule);
		}
	}
	
	@Override
	public void onRun(String action, JsonElement config) {
		// TODO Auto-generated method stub
		super.onRun(action, config);
		if (action.equalsIgnoreCase("archive")) {
			archiveData();
		}
		if (action.equalsIgnoreCase("upload")){
			uploadData();
		}
		if (action.equalsIgnoreCase("notify")) {
			checkForNotifications();
		}
	}
	
	
	@Override
	public void onDataReceived(IJsonObject probeConfig, IJsonObject data) {
		super.onDataReceived(probeConfig, data);
		String probeName = probeConfig.getAsJsonPrimitive("@type").getAsString();//probeConfig.get("@type").toString();
		long timestamp = data.get("timestamp").getAsLong();

		storeData(probeName, timestamp, data);
	}
	
	private void checkForNotifications() {
		Intent i = new Intent(mManager, NotificationService.class);
		mManager.startService(i);
	}
	
	private void storeData(String name, long timestamp, IJsonObject data) {
		Bundle b = new Bundle();
		b.putString(NameValueDatabaseService.DATABASE_NAME_KEY,  mManager.getPipelineName(this));
		b.putLong(NameValueDatabaseService.TIMESTAMP_KEY, timestamp);
		b.putString(NameValueDatabaseService.NAME_KEY, name);
		b.putString(NameValueDatabaseService.VALUE_KEY, data.toString());
		Intent i = new Intent(mManager, getDatabaseServiceClass());
		i.setAction(DatabaseService.ACTION_RECORD);
		i.putExtras(b);
		mManager.startService(i);
	}
	
	public void archiveData() {
		Intent i = new Intent(mManager, getDatabaseServiceClass());
		i.setAction(DatabaseService.ACTION_ARCHIVE);
		i.putExtra(DatabaseService.DATABASE_NAME_KEY, mManager.getPipelineName(this));
		mManager.startService(i);
	}
	
	public void uploadData() {		
		archiveData();		
		if (dataUploadUrl != null && dataUploadUrl != "") {
			String archiveName = mManager.getPipelineName(this);
			Intent i = new Intent(mManager, getUploadServiceClass());
			i.putExtra(UploadService.ARCHIVE_ID, archiveName);
			i.putExtra(UploadService.REMOTE_ARCHIVE_ID, dataUploadUrl);
			mManager.startService(i);
		}
		
		FunfManagerService.getSystemPrefs(mManager).edit().putLong(LAST_DATA_UPLOAD, System.currentTimeMillis()).commit();
	}
		
	public Class<? extends DatabaseService> getDatabaseServiceClass() {
		return NameValueDatabaseService.class;
	}
	
	public Class<? extends UploadService> getUploadServiceClass() {
		return HttpsUploadService.class;
	}
}
