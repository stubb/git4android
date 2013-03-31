package com.example.git;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

/**
 * This activity provides the user the functionality to initialize a Git repository in a selected directory. 
 */
public class InitGitRepositoryActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String LOGTAG = getClass().getName();
	
	/**
	 * The current used android context within this class.
	 */
	private final Context currentContext = InitGitRepositoryActivity.this;

	@Override
	/**
	 * Called when the activity is starting. Attach actions to the layout.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down
	 *  then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
	 *  Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_repository);

		EditText pathEditText = (EditText) findViewById(R.id.path_to_init_repository);
		pathEditText.setEnabled(false);

		Button selectFoldertoStoreButton = (Button) findViewById(R.id.button_init_select_folder);
		selectFoldertoStoreButton.setOnClickListener(new View.OnClickListener(){
			/**
			 * Called when the selectFoldertoStoreButton button has been clicked.
			 * Starts the FileBrowserActivity.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {     			
				Intent intent = new Intent(currentContext, FileBrowserActivity.class);
				intent.putExtra(FileBrowserActivity.SELECTIONTYPE, Integer.toString(FileBrowserActivity.SELECTIONTYPE_FOLDER));
				startActivityForResult(intent, 1);
			}
		});

		Button gitInitButton = (Button) findViewById(R.id.button_submit_init_repository);
		gitInitButton.setOnClickListener(new View.OnClickListener(){
			/**
			 * Called when the gitInitButton button has been clicked.
			 * Shows a toast notification, because user input is required.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.select_a_path), Toast.LENGTH_LONG, currentContext);
			}
		});
	}

	/**
	 * Called when the FileBrowserActivity which was launched in onCreate() via the selectFoldertoStoreButton exits, gives the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 * @param	requestCode 	The integer request code originally supplied to startActivityForResult(), allows to identify who this result came from.
	 * @param	resultCode 	The integer result code returned by the child activity through its setResult().
	 * @param	data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if(resultCode == RESULT_OK){
				final String selectedPath = data.getStringExtra(FileBrowserActivity.SELECTION);
				Log.d(LOGTAG, selectedPath);

				EditText pathEditText = (EditText) findViewById(R.id.path_to_init_repository);
				pathEditText.setText(selectedPath);
				pathEditText.setEnabled(false);
				
				final EditText gitRepositoryName = (EditText) findViewById(R.id.git_repository_name);
				
				Button gitInitButton = (Button) findViewById(R.id.button_submit_init_repository);
				gitInitButton.setOnClickListener(new View.OnClickListener(){
					/**
					 * Called when the gitInitButton button has been clicked.
					 * Starts the GitRepositoryListActivity.
					 * @param view The view that was clicked.
					 */
					public void onClick(View view) {
						if (!"".equals(selectedPath)) {
							File path = new File(selectedPath);
							if (path.isDirectory()) {
								GitRepository git = new GitRepository(currentContext);
								if (git.init(selectedPath)) {
									GitRepositoryDatabase repositoryDatabase = GitRepositoryDatabase.getInstance(currentContext);
									if(repositoryDatabase.addGitRepositoryLink(selectedPath, gitRepositoryName.getText().toString())) {
									ToastNotification.makeToast(currentContext.getResources().getString(R.string.repository_created), Toast.LENGTH_LONG, currentContext);
									finish();
									} else {
										ToastNotification.makeToast(currentContext.getResources().getString(R.string.add_git_repository_link_to_database_fail), Toast.LENGTH_LONG, currentContext);
										git.resetRepository(path);
									}
								} else {
									ToastNotification.makeToast(currentContext.getResources().getString(R.string.init_git_repository_failed), Toast.LENGTH_LONG, currentContext);
								}
							} else {
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_vaild_folder_selected), Toast.LENGTH_LONG, currentContext);
							}
						}
					}
				});		     					      
			}
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.file_browser_selection_failed), Toast.LENGTH_LONG, currentContext);
			}
		}
	}
}