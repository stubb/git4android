package com.example.git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

/**
 * 
 *
 */
public class InitRepositoryActivity extends Activity {

	private final String TAG = getClass().getName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_init_repository);

		EditText pathEditText = (EditText) findViewById(R.id.path_to_init_repository);
		pathEditText.setEnabled(false);

		Button button_select_folder = (Button) findViewById(R.id.button_init_select_folder);
		button_select_folder.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {     			
				Intent intent = new Intent(InitRepositoryActivity.this, BrowserActivity.class);
				startActivityForResult(intent, 1);
			}
		});

		Button button_submit_init_repository = (Button) findViewById(R.id.button_submit_init_repository);
		button_submit_init_repository.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				ToastNotification.makeToast("Select a path", Toast.LENGTH_LONG, InitRepositoryActivity.this);
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
								GitRepository git = new GitRepository();
								if (git.init(selectedPath)) {
									SqlLiteDatabaseHelper databaseHelper = SqlLiteDatabaseHelper.getInstance(InitRepositoryActivity.this);
									databaseHelper.insertRepositoryPathintoTableRepositories(selectedPath);
									ToastNotification.makeToast("Repository created!", Toast.LENGTH_LONG, InitRepositoryActivity.this);
									finish();
								} else {
									ToastNotification.makeToast("Something went wrong during the init process", Toast.LENGTH_LONG, InitRepositoryActivity.this);
								}
							} else {
								ToastNotification.makeToast("You havn't selected a valid folder", Toast.LENGTH_LONG, InitRepositoryActivity.this);
							}
						}
					}
				});		     					      
			}
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast("Something went wrong during the selection, please do it again!", Toast.LENGTH_LONG, InitRepositoryActivity.this);
			}
		}
	}
}