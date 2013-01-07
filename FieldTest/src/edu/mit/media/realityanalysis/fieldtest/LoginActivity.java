package edu.mit.media.realityanalysis.fieldtest;

import org.json.JSONObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
	private static final String LOG_TAG = "LoginActivity";
	
	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mUsername;
	private String mPassword;

	// UI references.
	private EditText mUsernameView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mUsernameView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mUsername = mUsernameView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid username
		if (TextUtils.isEmpty(mUsername)) {
			mUsernameView.setError(getString(R.string.error_field_required));
			focusView = mUsernameView;
			cancel = true;
		} 

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask(this);
			mAuthTask.execute(mUsername, mPassword);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	public class UserInfoTask extends AsyncTask<String, Void, Boolean>  {

		private static final String LOG_TAG = "UserInfoTask";
		
		private Activity mActivity;
		
		public UserInfoTask(Activity activity) {
			mActivity = activity;
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			if (params.length != 1) {
				throw new IllegalArgumentException("UserInfoTask requires token as a parameter.");
			}
			String token = params[0];
			RegistryServer registry = new RegistryServer(mActivity);
			
			try {
				JSONObject responseJson = registry.getUserInfo(token);
				
				if (responseJson == null) {
					showToast("Registry server user info is broken. Please contact brian717@media.mit.edu");
					Log.e(LOG_TAG, "Unable to parse response from getUserInfo.");
				}
				
				if (responseJson.has("error")) {
					showToast("Error while getting user info.");
					Log.e(LOG_TAG, String.format("Error while getting user info: %s - %s", responseJson.getString("error"), responseJson.getString("error_description")));
				} else if (responseJson.has("id") && responseJson.has("pds_location")) {
					Editor prefsEditor = mActivity.getSharedPreferences(mActivity.getString(R.string.prefs_file), MODE_PRIVATE).edit();
					
					prefsEditor.putString("uuid", responseJson.getString("id"));
					prefsEditor.putString("pds_location", responseJson.getString("pds_location"));
					prefsEditor.commit();
					
					return true;
				}
				
			} catch (Exception e) {
				showToast("Failed contacting the server. Please try again later.");
				Log.e(LOG_TAG, "Error during login - " + e.getMessage());
			}
			
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			
			if (result) { 
				mActivity.finish();
			}
		}		
		
		private void showToast(final String message) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
				}
			});
		}
	}
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, String> {
		
		private static final String LOG_TAG = "UserLoginTask";
		
		private Activity mActivity;
		
		public UserLoginTask(Activity activity) {
			mActivity = activity;
		}
		
		private void showToast(final String message) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
				}
			});
		}
		
		@Override
		protected String doInBackground(String... params)  {
			if (params.length != 2) {
				Log.e(LOG_TAG, "UserLoginTask requires username and password as parameters.");
				throw new IllegalArgumentException("UserLoginTask requires username and password as parameters.");
			}
			
			String username = params[0];
			String password = params[1];


			JSONObject responseJson = null;
			RegistryServer registryServer = new RegistryServer(mActivity);
			
			try {
				responseJson = registryServer.authorize(username, password);	
				
				if (responseJson == null) { 
					showToast("Registry server is broken. Please contact brian717@media.mit.edu");
					return null;
				}
				
				if (responseJson.has("error")) {
					Log.e(LOG_TAG, String.format("Error response to login: %s - %s", responseJson.getString("error"), responseJson.getString("error_description")));
					showToast("Login failed - please check your username and password.");
					return null;
				} else if (responseJson.has("access_token") && responseJson.has("refresh_token") && responseJson.has("expires_in")) {
					Editor prefsEditor = getSharedPreferences(getString(R.string.prefs_file), MODE_PRIVATE).edit();
					prefsEditor.putString("accessToken", responseJson.getString("access_token"));
					prefsEditor.putString("refreshToken", responseJson.getString("refresh_token"));
					prefsEditor.putLong("tokenExpirationTime", System.currentTimeMillis() + (responseJson.getLong("expires_in") * 1000));
					prefsEditor.commit();
					
					return responseJson.getString("access_token");
				}			
			} catch (Exception e) {				
				showToast("Failed contacting the server. Please try again later.");
				Log.e(LOG_TAG, String.format("Error during login: %s", e.getMessage()));
			}

			return null;
		}

		@Override
		protected void onPostExecute(final String token) {
			mAuthTask = null;
			showProgress(false);

			if (token != null) {
				showToast("Login Successful");
				
				UserInfoTask userInfoTask = new UserInfoTask(mActivity);
				
				userInfoTask.execute(token);
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}
	}
}
