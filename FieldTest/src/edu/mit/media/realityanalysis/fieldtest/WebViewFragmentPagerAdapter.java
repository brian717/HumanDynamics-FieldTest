package edu.mit.media.realityanalysis.fieldtest;

import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

public class WebViewFragmentPagerAdapter extends FragmentPagerAdapter {

	private List<WebViewFragment> mFragments;
	
	public WebViewFragmentPagerAdapter(FragmentManager fm) {
		super(fm);
		
		mFragments = new ArrayList<WebViewFragment>();
	}

	@Override
	public Fragment getItem(int arg0) {
		return mFragments.get(arg0);
	}
	
	@Override
	public int getCount() {
		return mFragments.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return mFragments.get(position).getTitle();
	}
	
	public void addItem(WebViewFragment fragment) {
		mFragments.add(fragment);
	}
	
	
}
