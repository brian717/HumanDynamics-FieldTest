package edu.mit.media.realityanalysis.fieldtest;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.storage.HttpUploadService;
import edu.mit.media.funf.storage.RemoteFileArchive;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import android.content.SharedPreferences;
import android.util.Log;

public class HttpsUploadService extends HttpUploadService {
	private SharedPreferences prefs;


	@Override
	protected RemoteFileArchive getRemoteArchive(String name) {
		//prefs = MainPipeline.getSystemPrefs(this);
		prefs = FunfManagerService.getSystemPrefs(this);
		String access_token = ""; 
		// Getting Access Token
		try {
			access_token = prefs.getString(getString(R.string.access_token_prefs_key), "");
			Log.d("UPLOADDATA", "access_token"+access_token);
		} catch(Exception ex) {
		}
		return new HttpsArchive(name, access_token);
	}

}
