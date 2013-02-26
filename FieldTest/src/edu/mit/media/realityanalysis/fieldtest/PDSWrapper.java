package edu.mit.media.realityanalysis.fieldtest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

public class PDSWrapper {

	private Context mContext; 
	private PreferencesWrapper mPrefs; 
	
	public PDSWrapper(Context context) {
		mContext = context;
		mPrefs = new PreferencesWrapper(context);
		assert(mPrefs.getAccessToken() != null && mPrefs.getPDSLocation() != null && mPrefs.getUUID() != null);
	}
	
	private String buildAbsoluteUrl(String relativeUrl) {
		return String.format("%s%s?bearer_token=%s&datastore_owner=%s", mPrefs.getPDSLocation(), relativeUrl, mPrefs.getAccessToken(), mPrefs.getUUID());
	}
	
	private String buildAbsoluteApiUrl(String relativeUrl) {
		return String.format("%s%s?bearer_token=%s&datastore_owner__uuid=%s", mPrefs.getPDSLocation(), relativeUrl, mPrefs.getAccessToken(), mPrefs.getUUID());
	}
	
	public String getNotificationApiUrl() {
		return buildAbsoluteApiUrl(mContext.getString(R.string.notification_api_relative_url));
	}
	
	public Map<Integer, Notification> getNotifications() {
		HttpGet getNotificationsRequest = new HttpGet(getNotificationApiUrl());
		getNotificationsRequest.addHeader("Content-Type", "application/json");
		
		HttpClient client = new DefaultHttpClient();
		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		
		try {
			responseBody = client.execute(getNotificationsRequest, responseHandler);
		} catch (ClientProtocolException e) {
	        client.getConnectionManager().shutdown();  
			return null;
		} catch (IOException e) {
	        client.getConnectionManager().shutdown();  
			return null;
		}
		
		Map<Integer, Notification> notifications = new HashMap<Integer, Notification>();
		
		try {
			JSONObject notificationsBody = new  JSONObject(responseBody);
			JSONArray notificationsArray = notificationsBody.getJSONArray("objects");
			
			for (int i = 0; i < notificationsArray.length(); i++) {
				JSONObject notification = notificationsArray.optJSONObject(i);
				
				if (notification != null) {
					notifications.put(notification.getInt("type"), new NotificationCompat.Builder(mContext).setContentTitle(notification.getString("title")).setContentText(notification.getString("content")).setSmallIcon(R.drawable.ic_launcher).build());
				}
			}			
		} catch (JSONException e) {
			return null;
		}
		
		// if we've gotten this far, we've successfully parsed all of the notifications, so clear the list on the server
		HttpDelete deleteNotificationsRequest = new HttpDelete(getNotificationApiUrl());
		
		try {
			// We don't care about the response here - if it succeeds, then no exception is thrown and the response has no content
			client.execute(deleteNotificationsRequest, responseHandler);
		} catch (ClientProtocolException e) {
			// Log something here
		} catch (IOException e) {
			// Log something here
		} finally {
	        client.getConnectionManager().shutdown();  
		}
		
		return notifications;
	}
}
