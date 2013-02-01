package edu.mit.media.realityanalysis.fieldtest;


import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class WebViewFragment extends Fragment {

	private String mUrl;
	private FrameLayout mView;
	private WebView mWebView;
	private View mLoadingStatusView;
	private String mTitle;
	private Activity mActivity;
	private ViewPager mViewPager;
	
	public static WebViewFragment Create(String url, String title, Activity activity, ViewPager viewPager) {

		return new WebViewFragment(url, title, activity, viewPager);
	}
	
	public WebViewFragment() {
		super();
		mUrl = mTitle = "";
	}
	
	protected WebViewFragment(String url, String title, Activity activity, ViewPager viewPager) {
		mUrl = url;
		mTitle = title;
		mActivity = activity;
		mViewPager = viewPager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = (FrameLayout) inflater.inflate(R.layout.webview_fragment_layout, container, false); 
		mLoadingStatusView = mView.findViewById(R.id.loading_status);
	
		// NOTE: code below for programmatically adding the webview may be necessary to avoid a 
		// memory leak related to using the main activity as the context when constructing a webview
		// This is what occurs when the webview is specified declaratively in the layout xml
		
		mWebView = (WebView) mView.findViewById(R.id.fragment_webview);
		//mWebView.setVisibility(View.VISIBLE);
		//mWebView.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		mWebView.setWebViewClient(new WebViewClient() {			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mWebView.setVisibility(View.GONE);
				mLoadingStatusView.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {		
				mWebView.setVisibility(View.VISIBLE);
				mLoadingStatusView.setVisibility(View.GONE);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				view.loadData(getString(R.string.problem_contacting_server), "text/html", "UTF-8");
			}		
		});
					
		mWebView.addJavascriptInterface(new WebViewFragmentJavascriptInterface(mViewPager), "android");
		
		if (savedInstanceState != null) {
			mWebView.restoreState(savedInstanceState);
		} else {
			mWebView.loadUrl(mUrl);
		}		
		
		//mView.addView(mWebView);
		return mView;
	}	
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		
		// For some reason, the webview is never destroyed by default, even though the view is destroyed
		// As a result, we need to manually do it
		//mWebView.destroy();
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	
	private class WebViewFragmentJavascriptInterface {
		
		private ViewPager mViewPager;
		
		public WebViewFragmentJavascriptInterface(ViewPager viewPager) {
			mViewPager = viewPager;
		}
		
		public Boolean hideWebNavBar() {
			return true;
		}
		
		public Boolean handleTabChange(final String dimension, final int tabNumber) {
			mActivity.runOnUiThread(new Runnable() { 
				public void run() {
					mViewPager.setCurrentItem(tabNumber + 1);
				}
			});
			
			return true;
		}
	}
	
}
