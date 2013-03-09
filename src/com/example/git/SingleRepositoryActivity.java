package com.example.git;

import java.io.File;

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
 * This activity 
 */
public class SingleRepositoryActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging
	 */
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
		setContentView(R.layout.activity_single_repository_overview);

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

			final int protocol = repository.checkUrlforProtokoll(repository.getRemoteOriginUrl(), SingleRepositoryActivity.this);

			buttonPull.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(repository.getRemoteOriginUrl() == "") {
						ToastNotification.makeToast("There is no Remote Origin Url configured", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
					}
					else {
						if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
							repository.pull();
						}
						else if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
							alert.setTitle("Enter password");           

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									repository.pull(inputPassword.getText().toString(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa",
											Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa.pub");
								}
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;   
								}
							});
							alert.show();
						}
						else if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
								protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);
							alert.setTitle("Enter password");  
							
							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							final EditText inputUsername = new EditText(SingleRepositoryActivity.this); 
							inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
							alert.setView(inputUsername);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									repository.pull(inputUsername.getText().toString(), inputPassword.getText().toString());
								}
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;   
								}
							});
							alert.show();
						}
					}
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
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
					alert.setTitle("Enter commit message");                

					final EditText inputMessage = new EditText(SingleRepositoryActivity.this); 
					inputMessage.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
					alert.setView(inputMessage);

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							repository.commit(inputMessage.getText().toString());
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

			buttonPush.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(repository.getRemoteOriginUrl() == "") {
						ToastNotification.makeToast("There is no Remote Origin Url configured", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
					}
					else {
						if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
							ToastNotification.makeToast("the git:// protocol is ready only, can't used to push", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
						}
						else if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
							alert.setTitle("Enter password");  
							alert.setMessage("pw");                

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									repository.push(inputPassword.getText().toString(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa",
											Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ssh/id_rsa.pub");
								}
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;   
								}
							});
							alert.show();
						}
						else if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
								protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							final EditText inputUsername = new EditText(SingleRepositoryActivity.this); 
							inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
							alert.setView(inputUsername);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									repository.push(inputUsername.getText().toString(), inputPassword.getText().toString());
								}
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;   
								}
							});
							alert.show();
						}
					}
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
			if(resultCode == RESULT_OK) {
				filePathToAdd = data.getStringExtra("currentPath");
				ToastNotification.makeToast(filePathToAdd, Toast.LENGTH_LONG, SingleRepositoryActivity.this);
			}
			if (resultCode == RESULT_CANCELED) {
			}
		}
	}
}

