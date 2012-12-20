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
		
		SharedPreferences prefs = getSharedPreferences(getString(R.string.token_prefs_file), MODE_PRIVATE);
		
		String token = prefs.getString("accessToken", null);
		String uuid = prefs.getString("uuid", null);
		String pdsLocation = prefs.getString("pds_location", null);
		
		if (token != null && uuid != null && pdsLocation != null) {
			setContentView(R.layout.activity_main);
			mViewPager = (ViewPager) findViewById(R.id.viewpager);
			
			if (mFragmentAdapter == null) {
				mFragmentAdapter = new WebViewFragmentPagerAdapter(getSupportFragmentManager());
				
				String visualizationUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, getString(R.string.visualization_relative_url), token, uuid);
				String adminUrl = String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, "/admin/audit/", token, uuid);
	
				mFragmentAdapter.addItem(WebViewFragment.Create(visualizationUrl, "My Social Health", this));
				mFragmentAdapter.addItem(WebViewFragment.Create(adminUrl, "My PDS", this));
				
				mViewPager.setAdapter(mFragmentAdapter);
				mViewPager.setCurrentItem(0);
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		
		if (mFragmentAdapter != null) {
			mFragmentAdapter.requestLayout();
		}
	}
	
	private void startLoginActivity() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);
	}
}
