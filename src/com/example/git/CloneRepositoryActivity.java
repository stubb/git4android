package com.example.git;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
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
		private String selectedPath = "";
		GitRepository git = new GitRepository();
	//	private String password = "";
	
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
			    	 selectedPath = data.getStringExtra("currentPath");

			    	 EditText urlEditText = (EditText) findViewById(R.id.clone_repository_url);
			    	 final String repositoryUrl = urlEditText.getText().toString();
						      		
			    	 EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
			    	 pathEditText.setText(selectedPath);
			    	 pathEditText.setEnabled(false);
			    	 
			    	 Button button_submit_clone_repository = (Button) findViewById(R.id.button_submit_clone_repository);
			 			 button_submit_clone_repository.setOnClickListener(new View.OnClickListener(){
			      		public void onClick(View v) {     			
			      				if (repositoryUrl != "" && selectedPath != "") {
			      					File path = new File(selectedPath);
			      					if (path.isDirectory()) {
			      			   	 	EditText passwordEditText = (EditText) findViewById(R.id.input_password);
			      			   	 	String password = passwordEditText.getText().toString();
			            			SharedPreferences settings = getSharedPreferences(CloneRepositoryActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
			            			String privateKeyFilenameWithPath = settings.getString(CloneRepositoryActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
			            			String publicKeyFilenameWithPath = settings.getString(CloneRepositoryActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");
			      						if (git.clone(selectedPath, repositoryUrl, password, privateKeyFilenameWithPath, publicKeyFilenameWithPath)) {
			      							SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(CloneRepositoryActivity.this);
			      							SQLiteDatabase db = dbHelper.getWritableDatabase();
			      							db.execSQL("INSERT INTO " + "Repositories" + " ('repoPath') VALUES ('" +  selectedPath + "');");
			      							selectedPath = "";
			      							ToastNotification.makeToast("Repo cloned!", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			      							finish();
			      						}	else {
			      							ToastNotification.makeToast("Something went wrong during the clone process", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			      						}
			      					} else {
			      						ToastNotification.makeToast("Cant clone", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			      					}
			      				}
			      			}
			        });				      
			     }

			     if (resultCode == RESULT_CANCELED) {
			    	 ToastNotification.makeToast("Something went wrong during the selection, please do it again!", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			     }
			}
		}
}