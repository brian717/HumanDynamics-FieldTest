package edu.mit.media.realityanalysis.fieldtest;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.app.Activity;
import android.content.SharedPreferences;

public class SettingsActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE);
		String token = prefs.getString("accessToken", null);
		String uuid = prefs.getString("uuid", null);
		String pdsLocation = prefs.getString("pds_location", null);
		
		String sharingUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.sharing_relative_url), token, uuid);
		
		Fragment settingsFragment = WebViewFragment.Create(sharingUrl, "Settings", this, null);
		
		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		
		fragmentTransaction.add(R.id.settings_content_layout, settingsFragment);
		fragmentTransaction.commit();
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
