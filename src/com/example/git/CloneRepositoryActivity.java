package com.example.git;

import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * 
 * @author kili
 *
 */
public class CloneRepositoryActivity extends Activity {
	
		private final String TAG = getClass().getName();
		private String selectedPath = "";
		GitRepository git = null;
	
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
						Toaster.makeToast("Enter URL and select a path", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
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
						
			    	 EditText passwordEditText = (EditText) findViewById(R.id.password);
			    	 final String password = passwordEditText.getText().toString();
			    	 
			    	 EditText pathEditText = (EditText) findViewById(R.id.path_to_save_repository);
			    	 pathEditText.setText(selectedPath);
			    	 pathEditText.setEnabled(false);
						
						
			    	 Button button_submit_clone_repository = (Button) findViewById(R.id.button_submit_clone_repository);
			 			 button_submit_clone_repository.setOnClickListener(new View.OnClickListener(){
			      		public void onClick(View v) {     			
			      				if (repositoryUrl != "" && selectedPath != "") {	//password can be empty
			      					git = new GitRepository(selectedPath, repositoryUrl, password.getBytes(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa",
												Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa.pub");
			      					SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(CloneRepositoryActivity.this);
			      					SQLiteDatabase db = dbHelper.getWritableDatabase();
			      					db.execSQL("INSERT INTO " + "woop" + " ('repoPath') VALUES ('" +  selectedPath + "');");
			      					selectedPath = "";
			      					Toaster.makeToast("Repo cloned!", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			    						finish();
			      			} else {
										Toaster.makeToast("Cant clone", Toast.LENGTH_LONG, CloneRepositoryActivity.this);
			      			}

			  				}
			        });
			     					      
			     }

			     if (resultCode == RESULT_CANCELED) {

			     //Write your code on no result return 

			     }
			}
		}
}