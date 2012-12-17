package edu.mit.media.realityanalysis.fieldtest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class MainActivity extends FragmentActivity {
	private WebView _mainWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		SharedPreferences prefs = getSharedPreferences(getString(R.string.token_prefs_file), MODE_PRIVATE);
		
		String token = prefs.getString("accessToken", null);
		String uuid = prefs.getString("uuid", null);
		String pdsLocation = prefs.getString("pds_location", null);
		
		if (token != null && uuid != null && pdsLocation != null) {
			setContentView(R.layout.activity_main);
			
			_mainWebView = (WebView) findViewById(R.id.webview);
			_mainWebView.getSettings().setJavaScriptEnabled(true);
			_mainWebView.setWebViewClient(new WebViewClient());
	
			//_mainWebView.addJavascriptInterface(new WebViewLoginInterface(this), "android");
			_mainWebView.loadUrl(String.format("%s%s?bearer_token=%s&datastore_owner=%s", pdsLocation, "/admin/audit/", token, uuid));
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
	
	private void startLoginActivity() {

		Intent loginIntent = new Intent(this, LoginActivity.class);
		startActivity(loginIntent);
	}
}
