package edu.mit.media.realityanalysis.fieldtest;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

public class MainActivity extends FragmentActivity {
	private ViewPager mViewPager;
	private WebViewFragmentPagerAdapter mFragmentAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PreferencesWrapper prefs = new PreferencesWrapper(this);
		
		//SharedPreferences prefs = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE);
		
		String token = prefs.getAccessToken();//prefs.getString("accessToken", null);
		String uuid = prefs.getUUID();//prefs.getString("uuid", null);
		String pdsLocation = prefs.getPDSLocation();//prefs.getString("pds_location", null);
		
		if (token != null && uuid != null && pdsLocation != null) {
			setContentView(R.layout.activity_main);
			mViewPager = (ViewPager) findViewById(R.id.viewpager);
			
			if (mFragmentAdapter == null) {
				mFragmentAdapter = new WebViewFragmentPagerAdapter(getSupportFragmentManager());
				
				String radialUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.radial_relative_url), token, uuid);
				String activityUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.activity_relative_url), token, uuid);
				String socialUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.social_relative_url), token, uuid);
				String focusUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.focus_relative_url), token, uuid);
				String placesUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.places_relative_url), token, uuid);
				String adminUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, "/admin/audit.html", token, uuid);
				String groupMembersUrl = String.format("%s%s", pdsLocation, "/discovery/members/");
				
				mFragmentAdapter.addItem(WebViewFragment.Create(radialUrl, "My Social Health", this, mViewPager));
				mFragmentAdapter.addItem(WebViewFragment.Create(activityUrl, "Activity", this, mViewPager));
				mFragmentAdapter.addItem(WebViewFragment.Create(socialUrl, "Social", this, mViewPager));
				mFragmentAdapter.addItem(WebViewFragment.Create(focusUrl, "Focus", this, mViewPager));
				mFragmentAdapter.addItem(WebViewFragment.Create(placesUrl, "Places", this, mViewPager));
				//mFragmentAdapter.addItem(WebViewFragment.Create(groupMembersUrl, "Group Members", this, mViewPager));
				//mFragmentAdapter.addItem(WebViewFragment.Create(sharingUrl, "Settings", getApplicationContext()));
				mFragmentAdapter.addItem(WebViewFragment.Create(adminUrl, "Audit Logs", this, mViewPager));
				
				mViewPager.setAdapter(mFragmentAdapter);
				//mViewPager.setCurrentItem(0);
			}
			
		} else {
			startLoginActivity();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}	
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ReauthMenuItem:
				startLoginActivity();
				return true;
			case R.id.menu_settings:
				startSettingsActivity();
				return true;
			case R.id.menu_debug:
				startActivity(new Intent(this, DebugActivity.class));
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private void startLoginActivity() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);	
	}
	
	private void startSettingsActivity() {
		Intent settingsIntent = new Intent(this, SettingsActivity.class);
		startActivity(settingsIntent);	
	}
	
	
}
