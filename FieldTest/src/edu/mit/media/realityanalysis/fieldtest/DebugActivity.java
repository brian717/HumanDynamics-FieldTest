package edu.mit.media.realityanalysis.fieldtest;

import java.util.ArrayList;

import edu.mit.media.funf.configured.FunfConfig;

import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DebugActivity extends Activity {

	private MainPipeline mPipeline;
	private TextView mUploadPeriodTextView;
	
	
	private ServiceConnection mPipelineConnection = new ServiceConnection() {
		public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
			mPipeline = ((MainPipeline.LocalBinder) service).getPipeline();
			FunfConfig config = mPipeline.getConfig();
			
			mUploadPeriodTextView.setText(String.format("%s: %d", "Upload Interval", config.getDataUploadPeriod()));
		};
		
		public void onServiceDisconnected(android.content.ComponentName name) {
			mPipeline = null;
		};		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		mUploadPeriodTextView = (TextView) DebugActivity.this.findViewById(R.id.data_upload_period_textview);
		
		bindService(new Intent(this, MainPipeline.class), mPipelineConnection, 0);
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_debug, menu);
		return true;
	}

	@Override
	protected void onPause() {
		if (mPipelineConnection != null) {
			unbindService(mPipelineConnection);
		}
		super.onPause();
	}
}
