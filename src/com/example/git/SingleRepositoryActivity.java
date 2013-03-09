package com.example.git;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
	GitRepository repository = new GitRepository();

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

		if (repoPath != "" && repository.open(repoPath)) {
			Button buttonPull = (Button) findViewById(R.id.button_pull);
			Button buttonAddFiles = (Button) findViewById(R.id.button_add_files);
			Button buttonCommit = (Button) findViewById(R.id.button_commit);
			Button buttonPush = (Button) findViewById(R.id.button_push);
			Button buttonAddRemote = (Button) findViewById(R.id.button_add_remote);
			Button buttonLog = (Button) findViewById(R.id.button_log);
			Button buttonStatus = (Button) findViewById(R.id.button_status);
			Button buttonShowRemote = (Button) findViewById(R.id.button_show_remote);

			buttonPull.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// check
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
					alert.setTitle("Enter pw");  
					alert.setMessage("pw");                

					final EditText input = new EditText(SingleRepositoryActivity.this); 
					input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					alert.setView(input);

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
				/*			try {
						//		repository.pull(input.getText().toString().getBytes("UTF-8"), Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa",
						//				Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa.pub");
							} catch (UnsupportedEncodingException e) {
								Log.e(TAG, "Encoding UTF-8 not supported");
								e.printStackTrace();
							}*/
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

			buttonShowRemote.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					ToastNotification.makeToast(repository.getRemoteOriginUrl(), Toast.LENGTH_LONG, SingleRepositoryActivity.this);
				}
			});
			
			buttonAddRemote.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
					alert.setTitle("Enter URL");                 

					EditText input = new EditText(SingleRepositoryActivity.this); 
					input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
					alert.setView(input);
					final String url = input.getText().toString();

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							repository.setRemoteOriginUrl(url);
						}
					});

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							return;   
						}
					});
					alert.show();
					
				}
			});

			buttonLog.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String log = repository.log();
					ToastNotification.makeToast(log, Toast.LENGTH_LONG, SingleRepositoryActivity.this);

				}
			});

			buttonStatus.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String status = repository.status();
					ToastNotification.makeToast(status, Toast.LENGTH_LONG, SingleRepositoryActivity.this);
				}
			});
		}
		else {
			ToastNotification.makeToast("Wasn't able to find this repo : (", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
		}
	}

	/**
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {

			if(resultCode == RESULT_OK){

				filePathToAdd = data.getStringExtra("currentPath");
				ToastNotification.makeToast(filePathToAdd, Toast.LENGTH_LONG, SingleRepositoryActivity.this);

			}

			if (resultCode == RESULT_CANCELED) {

				//Write your code on no result return 

			}
		}
	}
	
	/**
	 * Checks the given URL for a known protocol (ssh://, git://, http:// or https://)
	 * @param url	The url that should be checked.
	 * @param context The activity context, from which this function is called.
	 * @return	The result of the check 0, if no protocol was recognized, 1 for ssh://, 2 for git://, 3 for http:// and 4 for https:// .
	 */
	private int checkUrlforProtokoll(String url, Context context){
		int result = 0;
		// Locale.US is guaranteed to be available on all devices
		// http://developer.android.com/reference/java/util/Locale.html
		if (url.toLowerCase(Locale.US).startsWith("ssh://")) {
			result = context.getResources().getInteger(R.integer.SSHPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith("git://")) {
			result = context.getResources().getInteger(R.integer.GITPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith("http://")) {
			result = context.getResources().getInteger(R.integer.HTTPPROTOCOL);
		}
		else if (url.toLowerCase(Locale.US).startsWith("https://")) {
			result = context.getResources().getInteger(R.integer.HTTPSPROTOCOL);
		}
		else {
			Log.e(TAG, "The URL " + url + " is not supported!");
		}
		return result;
	}

}

