package com.example.git;

import java.io.File;
import java.io.UnsupportedEncodingException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
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
	        	repository.open(repoPath);
	        Button buttonPull = (Button) findViewById(R.id.button_pull);
	        Button buttonAddFiles = (Button) findViewById(R.id.button_add_files);
	        Button buttonCommit = (Button) findViewById(R.id.button_commit);
	        Button buttonPush = (Button) findViewById(R.id.button_push);
	        Button buttonAddRemote = (Button) findViewById(R.id.button_add_remote);
	        Button buttonLog = (Button) findViewById(R.id.button_log);
	        Button buttonStatus = (Button) findViewById(R.id.button_status);
					
	        buttonPull.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
							alert.setTitle("Enter pw");  
							alert.setMessage("pw");                

							final EditText input = new EditText(SingleRepositoryActivity.this); 
							input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(input);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
			      			try {
	                  repository.pull(input.getText().toString().getBytes("UTF-8"), Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa",
	                  		Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa.pub");
                  } catch (UnsupportedEncodingException e) {
	                  Log.e(TAG, "Encoding UTF-8 not supported");
	                  e.printStackTrace();
                  }
								}
							});
      			
							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									return;   
								}
							});
							alert.show();

	      		}
	      	});
	        
	        buttonAddFiles.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			if (filePathToAdd == "") {
	      				Intent intent = new Intent(SingleRepositoryActivity.this, BrowserActivity.class);
	      				intent.putExtra("startPath", repoPath);
	      				startActivityForResult(intent, 1);
	      			}
	      			else {
	      				Log.d(TAG, filePathToAdd);
	      				String filename = new File(filePathToAdd).getName();
	      				repository.add(filename);
	      			}
	  				}
	        });
	        
	        buttonCommit.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			repository.commit("ABC");
	  				}
	        });
	        
	        buttonPush.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			//repository.push();
	  				}
	        });
	        
	        buttonAddRemote.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			Toaster.makeToast(repository.getRemote(), Toast.LENGTH_LONG, SingleRepositoryActivity.this);
	      			repository.setRemote("");
	      			Toaster.makeToast(repository.getRemote(), Toast.LENGTH_LONG, SingleRepositoryActivity.this);
	  				}
	        });
	        
	        buttonLog.setOnClickListener(new View.OnClickListener() {
	      		public void onClick(View v) {
	      			String log = repository.log();
	      			Toaster.makeToast(log, Toast.LENGTH_LONG, SingleRepositoryActivity.this);
	      			
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

