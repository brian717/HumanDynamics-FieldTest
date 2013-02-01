package edu.mit.media.realityanalysis.fieldtest;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.view.Menu;
import android.widget.ArrayAdapter;

public class DebugActivity extends ListActivity {

	private ArrayList<String> debugItems = new ArrayList<String>();
	private ArrayAdapter mAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		mAdapter = new ArrayAdapter(this, R.id.listView1, debugItems);
		
		
		
		setListAdapter(mAdapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_debug, menu);
		return true;
	}

}
