package edu.mit.media.realityanalysis.fieldtest;


import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebStorage.QuotaUpdater;

public class WebViewFragment extends Fragment {

	private String mUrl;
	private View mView;
	private WebView mWebView;
	private View mLoadingStatusView;
	private String mTitle;
	private Context mContext;
	
	private static HashMap<String, WebViewFragment> cachedWebviewFragments = new HashMap<String, WebViewFragment>();
	
	public static WebViewFragment Create(String url, String title, Context context) {

		return new WebViewFragment(url, title, context);
	}
	
	public WebViewFragment() {
		super();
		mUrl = mTitle = "";
	}
	
	protected WebViewFragment(String url, String title, Context context) {
		mUrl = url;
		mTitle = title;
		mContext = context;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.webview_fragment_layout, container, false); 
		mLoadingStatusView = mView.findViewById(R.id.loading_status);
		
		mWebView = (WebView) mView.findViewById(R.id.fragment_webview);
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
				// NOTE: change this to use a javascript interface to android: 
				// having it done onload should be faster, or allow us to stop the navbar from loading at all
				view.loadUrl("javascript:$('#navbar').hide()");
				mWebView.setVisibility(View.VISIBLE);
				mLoadingStatusView.setVisibility(View.GONE);
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				view.loadData(getString(R.string.problem_contacting_server), "text/html", "UTF-8");
			}		
		});
			
		if (savedInstanceState != null) {
			mWebView.restoreState(savedInstanceState);
		} else {
			mWebView.loadUrl(mUrl);
		}		
		
		return mView;
	}	
	
	public String getTitle() {
		return mTitle;
	}
	
	public WebView getWebView() {
		return mWebView;
	}
}
