package edu.mit.media.realityanalysis.fieldtest;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesWrapper {

	private SharedPreferences mSharedPreferences;
	private Context mContext;
	
	public PreferencesWrapper(Context context) {
		mContext = context;
		mSharedPreferences = context.getSharedPreferences(context.getString(R.string.prefs_file), context.MODE_PRIVATE);
	}
	
	public String getAccessToken() {
		return mSharedPreferences.getString(mContext.getString(R.string.access_token_prefs_key), null);
	}
	
	public String getPDSLocation() {
		return mSharedPreferences.getString("pds_location", null);
	}
	
	public String getUUID() {
		return mSharedPreferences.getString("uuid", null);
	}
}
