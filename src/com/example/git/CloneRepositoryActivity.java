package com.example.git;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * 
 *
 */
public class CloneRepositoryActivity extends Activity {

	private final String TAG = getClass().getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clone_repository);

		EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
		pathEditText.setEnabled(false);

		Button button_select_folder = (Button) findViewById(R.id.button_select_folder);
		button_select_folder.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {     			
				Intent intent = new Intent(CloneRepositoryActivity.this, BrowserActivity.class);
				startActivityForResult(intent, 1);
			}
		});

		Button button_submit_clone_repository = (Button) findViewById(R.id.button_submit_clone_repository);
		button_submit_clone_repository.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				ToastNotification.makeToast("Enter URL and select a path", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			}
		});
	}

	/**
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult");

		if (requestCode == 1) {

			if(resultCode == RESULT_OK){
				final String selectedPath = data.getStringExtra("currentPath");

				EditText urlEditText = (EditText) findViewById(R.id.clone_repository_url);
				final String repositoryUrl = urlEditText.getText().toString();

				EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
				pathEditText.setText(selectedPath);
				pathEditText.setEnabled(false);

				Button button_submit_clone_repository = (Button) findViewById(R.id.button_submit_clone_repository);
				button_submit_clone_repository.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v) {
						int protocol = checkUrlforProtokoll(repositoryUrl, CloneRepositoryActivity.this);
						Log.d(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
						Log.d(TAG, String.valueOf(protocol));
						if (repositoryUrl != "" && selectedPath != "" && protocol != 0) {
							File path = new File(selectedPath);
							boolean cloneResult = false;
							if (path.isDirectory()) {
								GitRepository git = new GitRepository();
								if (protocol == CloneRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
									EditText passwordEditText = (EditText) findViewById(R.id.input_password);
									String password = passwordEditText.getText().toString();
									SharedPreferences settings = getSharedPreferences(CloneRepositoryActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
									String privateKeyFilenameWithPath = settings.getString(CloneRepositoryActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
									String publicKeyFilenameWithPath = settings.getString(CloneRepositoryActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");
									cloneResult = git.clone(selectedPath, repositoryUrl, password, privateKeyFilenameWithPath, publicKeyFilenameWithPath);
								}
								if (protocol == CloneRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
									// no authentification is required
									cloneResult = git.clone(selectedPath, repositoryUrl);
								}
								if (protocol == CloneRepositoryActivity.this.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
										protocol == CloneRepositoryActivity.this.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
									EditText passwordEditText = (EditText) findViewById(R.id.input_password);
									String password = passwordEditText.getText().toString();
									EditText usernameEditText = (EditText) findViewById(R.id.input_username);
									String username = usernameEditText.getText().toString();
									cloneResult = git.clone(selectedPath, repositoryUrl, username, password);
								}
								if (cloneResult) {
									SqlLiteDatabaseHelper databaseHelper = SqlLiteDatabaseHelper.getInstance(CloneRepositoryActivity.this);
									databaseHelper.insertRepositoryPathintoTableRepositories(selectedPath);
									ToastNotification.makeToast("Repo cloned!", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
									finish();
								}	else {
									ToastNotification.makeToast("Something went wrong during the clone process", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
								}
							}
						} else {
							ToastNotification.makeToast("Cant clone", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
						}
					}
				});				      
			}

			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast("Something went wrong during the selection, please do it again!", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			}
		}
	}

	/**
	 * Checks the given URL for the used protocol and returns
	 * @param url
	 * @return
	 * TODO remove context?
	 */
	private int checkUrlforProtokoll(String url, Context context){
		int result = 0;
		if (url.toLowerCase(Locale.US).startsWith("ssh://")) {
			result = context.getResources().getInteger(R.integer.SSHPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith("git://")) {
			result = context.getResources().getInteger(R.integer.GITPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith("http://")) {
			result = context.getResources().getInteger(R.integer.HTTPPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith("https://")) {
			result = context.getResources().getInteger(R.integer.HTTPSPROTOCOL);
		}
		else {
			Log.e(TAG, "The URL " + url + " is not supported!");
		}
		return result;
	}
}