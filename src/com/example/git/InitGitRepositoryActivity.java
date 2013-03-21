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
import java.util.jar.Attributes.Name;

/**
 * 
 *
 */
public class InitGitRepositoryActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String TAG = getClass().getName();
	
	private final Context context = InitGitRepositoryActivity.this;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_repository);

		EditText pathEditText = (EditText) findViewById(R.id.path_to_init_repository);
		pathEditText.setEnabled(false);
		
		EditText nameToRepresentGitRepositoryEditText = (EditText) findViewById(R.id.path_to_init_repository);

		Button button_select_folder = (Button) findViewById(R.id.button_init_select_folder);
		button_select_folder.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {     			
				Intent intent = new Intent(context, FileBrowserActivity.class);
				intent.putExtra("selectionTyp", FileBrowserActivity.SELECTIONTYP_FOLDER);
				startActivityForResult(intent, 1);
			}
		});

		Button button_submit_init_repository = (Button) findViewById(R.id.button_submit_init_repository);
		button_submit_init_repository.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				ToastNotification.makeToast(context.getResources().getString(R.string.select_a_path), Toast.LENGTH_LONG, context);
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
				Log.d(TAG, selectedPath);

				EditText pathEditText = (EditText) findViewById(R.id.path_to_init_repository);
				pathEditText.setText(selectedPath);
				pathEditText.setEnabled(false);

				Button button_submit_init_repository = (Button) findViewById(R.id.button_submit_init_repository);
				button_submit_init_repository.setOnClickListener(new View.OnClickListener(){
					public void onClick(View v) {
						if (selectedPath != "") {
							File path = new File(selectedPath);
							if (path.isDirectory()) {
								GitRepository git = new GitRepository(context);
								if (git.init(selectedPath)) {
									GitRepositoryDatabase repositoryDatabase = GitRepositoryDatabase.getInstance(context);
									repositoryDatabase.addRepository(selectedPath, "test");
									ToastNotification.makeToast(context.getResources().getString(R.string.repository_created), Toast.LENGTH_LONG, context);
									finish();
								} else {
									ToastNotification.makeToast(context.getResources().getString(R.string.init_git_repository_failed), Toast.LENGTH_LONG, context);
								}
							} else {
								ToastNotification.makeToast(context.getResources().getString(R.string.no_vaild_folder_selected), Toast.LENGTH_LONG, context);
							}
						}
					}
				});		     					      
			}
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(context.getResources().getString(R.string.file_browser_selection_failed), Toast.LENGTH_LONG, context);
			}
		}
	}
}