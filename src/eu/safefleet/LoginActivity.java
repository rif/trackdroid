package eu.trackdroid;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	// private static final String TAG = "LoginActivity";
	public static final String PREFS_NAME = "TrackDroidPrefs";
	private static final String PREF_USERNAME = "username";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_REMEMBER = "remember";
	private EditText userEdit;
	private EditText passwordEdit;
	private Button loginButton;
	private CheckBox rememberCheck;
	private TextView resultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		userEdit = (EditText) findViewById(R.id.userentry);
		passwordEdit = (EditText) findViewById(R.id.passwordentry);
		loginButton = (Button) findViewById(R.id.loginbutton);
		rememberCheck = (CheckBox) findViewById(R.id.rememberBox);
		resultText = (TextView) findViewById(R.id.resultlabel);

		SharedPreferences pref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String username = pref.getString(PREF_USERNAME, null);
		String password = pref.getString(PREF_PASSWORD, null);
		boolean checked = pref.getBoolean(PREF_REMEMBER, false);

		if (username != null) {
			userEdit.setText(username);
		}

		if (password != null) {
			passwordEdit.setText(password);
		}
		rememberCheck.setChecked(checked);

		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				boolean success;
				try {
					success = WebService.getInstance().login(
							userEdit.getText().toString(),
							passwordEdit.getText().toString());
					resultText.setText(success ? R.string.loginsuccessful
							: R.string.loginfailed);
					if (success) {
						finish();
					}
				} catch (ClientProtocolException e) {
					resultText.setText(R.string.servererror);
				} catch (IOException e) {
					resultText.setText(R.string.nointernet);
				}
				saveCredentials();
			}
		});
	}

	private void saveCredentials() {
		if (rememberCheck.isChecked()) {
			// Log.d(TAG, "Saving credential");
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
					.edit()
					.putString(PREF_USERNAME, userEdit.getText().toString())
					.putString(PREF_PASSWORD, passwordEdit.getText().toString())
					.putBoolean(PREF_REMEMBER, true).commit();
		} else {
			// Log.d(TAG, "Clearing credential");
			getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
					.putString(PREF_USERNAME, "").putString(PREF_PASSWORD, "")
					.putBoolean(PREF_REMEMBER, false).commit();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			moveTaskToBack(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
