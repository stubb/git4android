package com.example.git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * 
 * @author kili
 *
 */
public class SingleRepositoryActivity extends Activity{

				private final String TAG = getClass().getName();
				private String repoPath = "";
				private String filePathToAdd = "";
				GitRepository repository;
				
				/**
				 * 
				 */
				@Override
				public void onCreate(Bundle savedInstanceState) {
					super.onCreate(savedInstanceState);
					setContentView(R.layout.single_repository_overview);
	        
	        Bundle extras = getIntent().getExtras();
	        if (extras != null) {
	            repoPath = extras.getString("repo");
	      			Log.d(TAG, repoPath.toString());
	        }
	        
	        //init repo
	        if (repoPath != "") {
	        	repository = new GitRepository();
	        	repository.hookIn(repoPath);
	        
	        Button buttonAddFiles = (Button) findViewById(R.id.button_add_files);
	        Button buttonCommit = (Button) findViewById(R.id.button_commit);
	        Button buttonPush = (Button) findViewById(R.id.button_push);
	        Button buttonAddRemote = (Button) findViewById(R.id.button_add_remote);
	        Button buttonLog = (Button) findViewById(R.id.button_log);
	        Button buttonStatus = (Button) findViewById(R.id.button_status);
					
	        buttonAddFiles.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			if (filePathToAdd == "") {
	      				Intent intent = new Intent(SingleRepositoryActivity.this, BrowserActivity.class);
	      				startActivityForResult(intent, 1);
	      			}
	      			else {
	      				repository.add(filePathToAdd);
	      			}
	  				}
	        });
	        
	        buttonCommit.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	  				}
	        });
	        
	        buttonPush.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	  				}
	        });
	        
	        buttonAddRemote.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	  				}
	        });
	        
	        buttonLog.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	  				}
	        });
	        
	        buttonStatus.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			String status = repository.status();
	      			Toaster.makeToast(status, Toast.LENGTH_LONG, SingleRepositoryActivity.this);
	  				}
	        });
	        }
	        else {
	        	Toaster.makeToast("Wasn't able to find this repo : (", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
	        }
				}
			
				/**
				 * 
				 */
				protected void onActivityResult(int requestCode, int resultCode, Intent data) {

					if (requestCode == 1) {

					     if(resultCode == RESULT_OK){

					    	filePathToAdd = data.getStringExtra("currentPath");
					      Toaster.makeToast(filePathToAdd, Toast.LENGTH_LONG, SingleRepositoryActivity.this);
					     					      
					     }

					     if (resultCode == RESULT_CANCELED) {

					     //Write your code on no result return 

					     }
					}
				}
					
}

