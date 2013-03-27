package com.example.git;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * This Activity is used to handle the cloning process of a Git repository.
 */
public class CloneGitRepositoryActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String LOGTAG = getClass().getName();

	@Override
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clone_repository);

		EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
		pathEditText.setEnabled(false);

		Button button_select_folder = (Button) findViewById(R.id.button_select_folder);
		button_select_folder.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {     			
				Intent intent = new Intent(CloneGitRepositoryActivity.this, FileBrowserActivity.class);
				intent.putExtra("selectionTyp", Integer.toString(FileBrowserActivity.SELECTIONTYP_FOLDER));
				startActivityForResult(intent, 1);
			}
		});

		Button button_submit_clone_repository = (Button) findViewById(R.id.button_submit_clone_repository);
		button_submit_clone_repository.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				ToastNotification.makeToast("Enter URL and select a path", Toast.LENGTH_LONG, CloneGitRepositoryActivity.this);
			}
		});
	}

	/**
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				final String selectedPath = data.getStringExtra("currentPath");

				final EditText urlEditText = (EditText) findViewById(R.id.clone_repository_url);

				EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
				pathEditText.setText(selectedPath);
				pathEditText.setEnabled(false);

				final EditText gitRepositoryName = (EditText) findViewById(R.id.git_repository_name);
				
				Button button_submit_clone_repository = (Button) findViewById(R.id.button_submit_clone_repository);
				button_submit_clone_repository.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v) {
						GitRepository git = new GitRepository(CloneGitRepositoryActivity.this);
						final String repositoryUrl = urlEditText.getText().toString();
						int protocol = git.checkUrlforProtokoll(repositoryUrl, CloneGitRepositoryActivity.this);
						Log.d(LOGTAG, String.valueOf(protocol));
						if (repositoryUrl != "" && selectedPath != "" && protocol != 0) {
							File path = new File(selectedPath);
							boolean cloneResult = false;
							if (path.isDirectory()) {
								if (protocol == CloneGitRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
									EditText passwordEditText = (EditText) findViewById(R.id.input_password);
									String password = passwordEditText.getText().toString();
									SharedPreferences settings = getSharedPreferences(CloneGitRepositoryActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
									String privateKeyFilenameWithPath = settings.getString(CloneGitRepositoryActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
									String publicKeyFilenameWithPath = settings.getString(CloneGitRepositoryActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");
									if (privateKeyFilenameWithPath != "" && publicKeyFilenameWithPath != "") {
										cloneResult = git.clone(selectedPath, repositoryUrl, password, privateKeyFilenameWithPath, publicKeyFilenameWithPath);
									}
									else {
										ToastNotification.makeToast("No ssh keys available add some in the settings menu", Toast.LENGTH_LONG, CloneGitRepositoryActivity.this);
									}
								}
								if (protocol == CloneGitRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
									// no authentification is required
									cloneResult = git.clone(selectedPath, repositoryUrl);
								}
								if (protocol == CloneGitRepositoryActivity.this.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
										protocol == CloneGitRepositoryActivity.this.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
									EditText passwordEditText = (EditText) findViewById(R.id.input_password);
									String password = passwordEditText.getText().toString();
									EditText usernameEditText = (EditText) findViewById(R.id.input_username);
									String username = usernameEditText.getText().toString();
									cloneResult = git.clone(selectedPath, repositoryUrl, username, password);
								}
								if (cloneResult) {
									GitRepositoryDatabase repositoryDatabase = GitRepositoryDatabase.getInstance(CloneGitRepositoryActivity.this);
									repositoryDatabase.addRepository(selectedPath, gitRepositoryName.getText().toString());
									ToastNotification.makeToast("Repo cloned!", Toast.LENGTH_LONG, CloneGitRepositoryActivity.this);
									finish();
								}	else {
									ToastNotification.makeToast("Something went wrong during the clone process", Toast.LENGTH_LONG, CloneGitRepositoryActivity.this);
								}
							}
						} else {
							ToastNotification.makeToast("Cant clone", Toast.LENGTH_LONG, CloneGitRepositoryActivity.this);
						}
					}
				});				      
			}

			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast("Something went wrong during the selection, please do it again!", Toast.LENGTH_LONG, CloneGitRepositoryActivity.this);
			}
		}
	}
}