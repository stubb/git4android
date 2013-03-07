package com.example.git;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

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
public class InitRepositoryActivity extends Activity {
	
		private final String TAG = getClass().getName();
		private String selectedPath = "";
		GitRepository git = null;
	
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
			    	 selectedPath = data.getStringExtra("currentPath");
			    	 Log.d(TAG, selectedPath);
			    	 
			    	 EditText pathEditText = (EditText) findViewById(R.id.path_to_init_repository);
			    	 pathEditText.setText(selectedPath);
			    	 pathEditText.setEnabled(false);
						
						 Button button_submit_init_repository = (Button) findViewById(R.id.button_submit_init_repository);
			 			 button_submit_init_repository.setOnClickListener(new View.OnClickListener(){
			      		public void onClick(View v) {     			
			      				if (selectedPath != "") {
			      					init(selectedPath);
			      					SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(InitRepositoryActivity.this);
			      					SQLiteDatabase db = dbHelper.getWritableDatabase();
			      					db.execSQL("INSERT INTO " + "woop" + " ('repoPath') VALUES ('" +  selectedPath + "');");
			      					selectedPath = "";
			      					ToastNotification.makeToast("Repo created!", Toast.LENGTH_LONG, InitRepositoryActivity.this);
			    						finish();
			      			} else {
										ToastNotification.makeToast("Cant init", Toast.LENGTH_LONG, InitRepositoryActivity.this);
			      			}

			  				}
			        });
			     					      
			     }

			     if (resultCode == RESULT_CANCELED) {

			     //Write your code on no result return 

			     }
			}
		}
		
	   /**
	    * Inits a new GIT repo within a given folder.
	    * A .git folder is created where all the stuff is inside
	    * @param String targetDirectory 
	    */
	   private boolean init(String targetDirectory){
	  	 boolean buildRepoSuccessfully = false;
		   Repository repository;
		   File path = new File(targetDirectory + "/.git/");
		   FileRepositoryBuilder builder = new FileRepositoryBuilder();
		   try {
			   repository = builder.setGitDir(path)
					   .readEnvironment()
					   .findGitDir()
					   .build();
			   repository.create();
			   buildRepoSuccessfully = true;
		   } catch (IOException e1) {
			   Log.e(TAG, "Wasn't able to init Repo : /");
			   e1.printStackTrace();
		   } 
		   return buildRepoSuccessfully;
		}
}