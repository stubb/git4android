package com.example.git;

import java.io.File;

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
 * This activity is used to handle the cloning process of a Git repository.
 */
public class CloneGitRepositoryActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String LOGTAG = getClass().getName();

	/**
	 * The current used android context within this class.
	 */
	private final Context currentContext = CloneGitRepositoryActivity.this;
	
	@Override
	/**
	 * Called when the activity is starting. Attach actions to the layout.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clone_repository);

		EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
		pathEditText.setEnabled(false);

		Button selectFoldertoStoreButton = (Button) findViewById(R.id.button_select_folder);
		selectFoldertoStoreButton.setOnClickListener(new View.OnClickListener(){
			/**
			 * Called when the selectFoldertoStoreButton button has been clicked.
			 * Starts the FileBrowserActivity for the user to select a folder.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {     			
				Intent intent = new Intent(currentContext, FileBrowserActivity.class);
				intent.putExtra(FileBrowserActivity.SELECTIONTYPE, Integer.toString(FileBrowserActivity.SELECTIONTYPE_FOLDER));
				startActivityForResult(intent, 1);
			}
		});

		Button gitCloneButton = (Button) findViewById(R.id.button_submit_clone_repository);
		gitCloneButton.setOnClickListener(new View.OnClickListener(){
			/**
			 * Called when the gitCloneButton button has been clicked.
			 * Shows a toast notification, because user input is required.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.url_and_path_missing), Toast.LENGTH_LONG, currentContext);
			}
		});
	}

	/**
	 * Called when the FileBrowserActivity which was launched in onCreate() via the selectFolderButton exits, gives the requestCode you started it with, the resultCode it returned, and any additional data from it.
	 * @param	requestCode 	The integer request code originally supplied to startActivityForResult(), allows to identify who this result came from.
	 * @param	resultCode 	The integer result code returned by the child activity through its setResult().
	 * @param	data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				final String selectedPath = data.getStringExtra(FileBrowserActivity.SELECTION);

				final EditText urlEditText = (EditText) findViewById(R.id.clone_repository_url);

				EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
				pathEditText.setText(selectedPath);
				pathEditText.setEnabled(false);

				final EditText gitRepositoryName = (EditText) findViewById(R.id.git_repository_name);
				
				Button gitCloneButton = (Button) findViewById(R.id.button_submit_clone_repository);
				gitCloneButton.setOnClickListener(new View.OnClickListener(){
					/**
					 * Called when the gitCloneButton button has been clicked.
					 * Launches the action to clone the Git Repository.
					 * @param view The view that was clicked.
					 */
					public void onClick(View view) {
						GitRepository git = new GitRepository(currentContext);
						final String repositoryUrl = urlEditText.getText().toString();
						int protocol = git.checkUrlforProtokoll(repositoryUrl, currentContext);
						if (!"".equals(repositoryUrl) && !"".equals(selectedPath) && protocol != 0) {
							File path = new File(selectedPath);
							boolean cloneResult = false;
							if (path.isDirectory()) {
								if (protocol == currentContext.getResources().getInteger(R.integer.SSHPROTOCOL)) {
									EditText passwordEditText = (EditText) findViewById(R.id.input_password);
									String password = passwordEditText.getText().toString();
									SharedPreferences settings = getSharedPreferences(currentContext.getResources().getString(R.string.APPSETTINGS), 0);
									String privateKeyFilenameWithPath = settings.getString(currentContext.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
									String publicKeyFilenameWithPath = settings.getString(currentContext.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");
									if (!"".equals(privateKeyFilenameWithPath) && !"".equals(publicKeyFilenameWithPath)) {
										cloneResult = git.clone(selectedPath, repositoryUrl, password, privateKeyFilenameWithPath, publicKeyFilenameWithPath);
									}
									else {
										ToastNotification.makeToast(currentContext.getResources().getString(R.string.settings_no_key_pair_available), Toast.LENGTH_LONG, currentContext);
									}
								}
								if (protocol == currentContext.getResources().getInteger(R.integer.GITPROTOCOL)) {
									// no authentification is required
									cloneResult = git.clone(selectedPath, repositoryUrl);
								}
								if (protocol == currentContext.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
										protocol == currentContext.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
									// username and password can be empty.
									EditText passwordEditText = (EditText) findViewById(R.id.input_password);
									String password = passwordEditText.getText().toString();
									EditText usernameEditText = (EditText) findViewById(R.id.input_username);
									String username = usernameEditText.getText().toString();
									cloneResult = git.clone(selectedPath, repositoryUrl, username, password);
								}
								if (cloneResult) {
									GitRepositoryDatabase repositoryDatabase = GitRepositoryDatabase.getInstance(currentContext);
									repositoryDatabase.addGitRepositoryLink(selectedPath, gitRepositoryName.getText().toString());
									ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_clone_succes), Toast.LENGTH_LONG, currentContext);
									Log.d(LOGTAG, currentContext.getResources().getString(R.string.git_clone_succes));
									finish();
								}	else {
									ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_clone_fail), Toast.LENGTH_LONG, currentContext);
								}
							}
						} else {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_path_and_url), Toast.LENGTH_LONG, currentContext);
						}
					}
				});				      
			}

			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.selection_canceled), Toast.LENGTH_LONG, currentContext);
			}
		}
	}
}