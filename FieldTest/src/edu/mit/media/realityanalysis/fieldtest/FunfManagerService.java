package edu.mit.media.realityanalysis.fieldtest;

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import edu.mit.media.funf.FunfManager;
import static edu.mit.media.funf.util.AsyncSharedPrefs.async;

public class FunfManagerService extends FunfManager {
	
	public SharedPreferences getSystemPrefs() {
		return getSystemPrefs(this);
	}
	
	public static SharedPreferences getSystemPrefs(Context context) {
		return async(context.getSharedPreferences(context.getString(R.string.prefs_file), MODE_PRIVATE));
	}

}
