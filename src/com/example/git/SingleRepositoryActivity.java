package com.example.git;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
			
			SharedPreferences settings = getSharedPreferences(SingleRepositoryActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
			final String sshPrivateKeyPath = settings.getString(SingleRepositoryActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
			final String sshPublicKeyPath = settings.getString(SingleRepositoryActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

			buttonPull.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(repository.getRemoteOriginUrl() == "") {
						ToastNotification.makeToast("There is no Remote Origin Url configured", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
					}
					else {
						if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
							if(repository.pull()) {
								ToastNotification.makeToast("Pull succesful!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
							} else{
								ToastNotification.makeToast("Pull failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
							}
						}
						else if (protocol == SingleRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleRepositoryActivity.this);                 
							alert.setTitle("Enter password");           

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if(repository.pull(inputPassword.getText().toString(), sshPrivateKeyPath, sshPublicKeyPath))  {
										ToastNotification.makeToast("Pull succesful!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
									} else{
										ToastNotification.makeToast("Pull failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
									}
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
							alert.setTitle("Enter credentials");

							LinearLayout linearLayout = new LinearLayout(SingleRepositoryActivity.this);
							linearLayout.setOrientation(1);

							final EditText inputUsername = new EditText(SingleRepositoryActivity.this); 
							inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
							inputUsername.setHint("username");
							linearLayout.addView(inputUsername);

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							inputPassword.setHint("password");
							linearLayout.addView(inputPassword);

							alert.setView(linearLayout);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if(repository.pull(inputUsername.getText().toString(), inputPassword.getText().toString())) {
										ToastNotification.makeToast("Pull succesful!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
									} else{
										ToastNotification.makeToast("Pull failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
									}
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
					Intent intent = new Intent(SingleRepositoryActivity.this, BrowserActivity.class);
					intent.putExtra("startPath", repoPath);
					intent.putExtra("originOfRequestforResult", "buttonAddFiles");
					intent.putExtra("selectionTyp", "file");
					startActivityForResult(intent, 1);
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
							if (repository.commit(inputMessage.getText().toString())) {
								ToastNotification.makeToast("Commit succesful!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
							} else{
								ToastNotification.makeToast("Commit failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
							}
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

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if (sshPublicKeyPath != "" && sshPrivateKeyPath != "") {
										if (repository.push(inputPassword.getText().toString(), sshPrivateKeyPath, sshPublicKeyPath)) {
											ToastNotification.makeToast("Push succesfull!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
										} else {
											ToastNotification.makeToast("Push failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
										}
									}
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
							alert.setTitle("Enter credentials");

							LinearLayout linearLayout = new LinearLayout(SingleRepositoryActivity.this);
							linearLayout.setOrientation(1);

							final EditText inputUsername = new EditText(SingleRepositoryActivity.this); 
							inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
							inputUsername.setHint("username");
							linearLayout.addView(inputUsername);

							final EditText inputPassword = new EditText(SingleRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							inputPassword.setHint("password");
							linearLayout.addView(inputPassword);

							alert.setView(linearLayout);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if (repository.push(inputUsername.getText().toString(), inputPassword.getText().toString())) {
										ToastNotification.makeToast("Push succesfull!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
									} else {
										ToastNotification.makeToast("Push failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
									}
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
				String filePathToAdd = data.getStringExtra("currentPath");
				String originOfRequestforResult = data.getStringExtra("originOfRequestforResult");
				if (originOfRequestforResult.equalsIgnoreCase("buttonAddFiles")) {
					Log.d(TAG, filePathToAdd);
					String filename = new File(filePathToAdd).getName();
					if (repository.add(filename)) {
						ToastNotification.makeToast("Added " + filePathToAdd, Toast.LENGTH_LONG, SingleRepositoryActivity.this);
					} else {
						ToastNotification.makeToast("Adding " + filePathToAdd + "failed!", Toast.LENGTH_LONG, SingleRepositoryActivity.this);
					}
				}
			}
			if (resultCode == RESULT_CANCELED) {
			}
		}
	}
}

