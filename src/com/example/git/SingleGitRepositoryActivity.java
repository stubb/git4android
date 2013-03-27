package com.example.git;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
 * This activity provides a bunch of different actions which can be performed on a Git repository by a given path.
 * These actions are:
 *  - git pull
 *  - git add
 *  - git commit
 *  - git push
 *  - git log
 *  - git status
 *  - show git remote
 *  - change git remote
 *  - git checkout branch
 *  - show current branch
 *  - show all available branches
 *  - git checkout by commit to a new branch
 */
public class SingleGitRepositoryActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String LOGTAG = getClass().getName();

	/**
	 * The path to the Git repository on the filesystem.
	 */
	private String filesystemPathToGitRepository = "";

	/**
	 * The Git repository.
	 */
	private GitRepository gitRepository = new GitRepository(SingleGitRepositoryActivity.this);

	/**
	 * The current context within the application.
	 */
	private final Context currentContext = SingleGitRepositoryActivity.this;

	/**
	 * The name of the intent thats used to provide the path of the Git repository via the intent extras.
	 */
	public final static String GITREPOSITORYPATH = "gitrepositorypath";

	/**
	 * Constant to identify the origin of the request.
	 */
	private static final int GITADDFILEREQUEST = 0;

	@Override
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_single_repository_overview);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			filesystemPathToGitRepository = extras.getString(GITREPOSITORYPATH);
			Log.d(LOGTAG, filesystemPathToGitRepository.toString());
		}

		if (filesystemPathToGitRepository != "" && gitRepository.open(filesystemPathToGitRepository)) {
			Button buttonPull = (Button) findViewById(R.id.button_pull);
			Button buttonAddFile = (Button) findViewById(R.id.button_add_files);
			Button buttonCommit = (Button) findViewById(R.id.button_commit);
			Button buttonPush = (Button) findViewById(R.id.button_push);
			Button buttonAddRemote = (Button) findViewById(R.id.button_add_remote);
			Button buttonLog = (Button) findViewById(R.id.button_log);
			Button buttonStatus = (Button) findViewById(R.id.button_status);
			Button buttonShowRemote = (Button) findViewById(R.id.button_show_remote);
			Button buttonCheckoutByCommit = (Button) findViewById(R.id.button_checkout_commit);
			Button buttonCurrentBranch = (Button) findViewById(R.id.button_current_branch);
			Button buttonShowAllBranches = (Button) findViewById(R.id.button_all_branches);
			Button buttonCheckoutBranch = (Button) findViewById(R.id.button_checkout_branch);

			final int remoteOriginProtocolUrl = gitRepository.checkUrlforProtokoll(gitRepository.getRemoteOriginUrl(), SingleGitRepositoryActivity.this);

			SharedPreferences settings = getSharedPreferences(SingleGitRepositoryActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
			final String sshPrivateKeyPath = settings.getString(SingleGitRepositoryActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
			final String sshPublicKeyPath = settings.getString(SingleGitRepositoryActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

			buttonPull.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(gitRepository.getRemoteOriginUrl().equals("")) {
						ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_remote_origin_url_configured), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
					}
					else {
						if (remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
							if(gitRepository.pull()) {
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_succesful), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
							} else{
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_failed), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
							}
						}
						else if (remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
							alert.setTitle(currentContext.getResources().getString(R.string.enter_password));           

							final EditText inputPassword = new EditText(SingleGitRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if(gitRepository.pull(inputPassword.getText().toString(), sshPrivateKeyPath, sshPublicKeyPath))  {
										ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_succesful), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
									} else{
										ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_failed), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
									}
								}
							});

							alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;   
								}
							});
							alert.show();
						}
						else if (remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
								remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);
							alert.setTitle(currentContext.getResources().getString(R.string.enter_credentials));

							LinearLayout linearLayout = new LinearLayout(SingleGitRepositoryActivity.this);
							linearLayout.setOrientation(1);

							final EditText inputUsername = new EditText(SingleGitRepositoryActivity.this); 
							inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
							inputUsername.setHint(currentContext.getResources().getString(R.string.username));
							linearLayout.addView(inputUsername);

							final EditText inputPassword = new EditText(SingleGitRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							inputPassword.setHint(currentContext.getResources().getString(R.string.password));
							linearLayout.addView(inputPassword);

							alert.setView(linearLayout);

							alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if(gitRepository.pull(inputUsername.getText().toString(), inputPassword.getText().toString())) {
										ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_succesful), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
									} else{
										ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_failed), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
									}
								}
							});

							alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									return;   
								}
							});
							alert.show();
						} 
					}
				}
			});

			buttonAddFile.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(SingleGitRepositoryActivity.this, FileBrowserActivity.class);
					intent.putExtra("startPath", filesystemPathToGitRepository);
					intent.putExtra("selectionTyp", Integer.toString(FileBrowserActivity.SELECTIONTYP_FILE));
					startActivityForResult(intent, GITADDFILEREQUEST);
				}
			});

			buttonCommit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
					alert.setTitle("Enter commit message");                

					final EditText inputMessage = new EditText(SingleGitRepositoryActivity.this); 
					inputMessage.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
					alert.setView(inputMessage);

					alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							if (gitRepository.commit(inputMessage.getText().toString())) {
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_commit_succesful), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
							} else{
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_commit_failed), Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
							}
						}
					});

					alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							return;   
						}
					});
					alert.show();
				}
			});

			buttonPush.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(gitRepository.getRemoteOriginUrl().equals("")) {
						ToastNotification.makeToast("There is no Remote Origin Url configured", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
					}
					else {
						if (remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.GITPROTOCOL)) {
							ToastNotification.makeToast("the git:// protocol is ready only, can't used to push", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
						}
						else if (remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.SSHPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
							alert.setTitle("Enter password");             

							final EditText inputPassword = new EditText(SingleGitRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							alert.setView(inputPassword);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if (sshPublicKeyPath != "" && sshPrivateKeyPath != "") {
										if (gitRepository.push(inputPassword.getText().toString(), sshPrivateKeyPath, sshPublicKeyPath)) {
											ToastNotification.makeToast("Push succesfull!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
										} else {
											ToastNotification.makeToast("Push failed!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
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
						else if (remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
								remoteOriginProtocolUrl == SingleGitRepositoryActivity.this.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
							AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);
							alert.setTitle("Enter credentials");

							LinearLayout linearLayout = new LinearLayout(SingleGitRepositoryActivity.this);
							linearLayout.setOrientation(1);

							final EditText inputUsername = new EditText(SingleGitRepositoryActivity.this); 
							inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
							inputUsername.setHint("username");
							linearLayout.addView(inputUsername);

							final EditText inputPassword = new EditText(SingleGitRepositoryActivity.this); 
							inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
							inputPassword.setHint("password");
							linearLayout.addView(inputPassword);

							alert.setView(linearLayout);

							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {
									if (gitRepository.push(inputUsername.getText().toString(), inputPassword.getText().toString())) {
										ToastNotification.makeToast("Push succesfull!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
									} else {
										ToastNotification.makeToast("Push failed!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
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
					String urlOfRemoteRepository = gitRepository.getRemoteOriginUrl();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, urlOfRemoteRepository);
					startActivity(intent);
				}
			});

			buttonAddRemote.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
					alert.setTitle("Enter URL");                 

					EditText input = new EditText(SingleGitRepositoryActivity.this); 
					input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
					alert.setView(input);
					final String url = input.getText().toString();

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							gitRepository.setRemoteOriginUrl(url);
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
					String log = gitRepository.log();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, log);
					startActivity(intent);
				}
			});

			buttonStatus.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String status = gitRepository.status();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, status);
					startActivity(intent);
				}
			});

			buttonCheckoutByCommit.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
					alert.setTitle("Checkout new branch starting from commitid");

					LinearLayout linearLayout = new LinearLayout(SingleGitRepositoryActivity.this);
					linearLayout.setOrientation(1);

					final EditText branchNameView = new EditText(SingleGitRepositoryActivity.this); 
					branchNameView.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
					branchNameView.setHint("branch name");
					linearLayout.addView(branchNameView);

					final EditText commitIdView = new EditText(SingleGitRepositoryActivity.this);
					commitIdView.setHint("commit id");
					commitIdView.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
					linearLayout.addView(commitIdView);

					alert.setView(linearLayout);
					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							String branchName = "";
							branchName = branchNameView.getText().toString();
							String commitId = "";
							commitId = commitIdView.getText().toString();
							if(gitRepository.checkoutCommitToNewBranch(commitId, branchName)) {
								ToastNotification.makeToast("Check out succesfull", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
							} else {
								ToastNotification.makeToast("Checked out failed", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
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
			buttonShowAllBranches.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String branchnames = gitRepository.getAllBranchNames();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, branchnames);
					startActivity(intent);
				}
			});
			buttonCurrentBranch.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					String branchname = gitRepository.getCurrentBranch();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, branchname);
					startActivity(intent);
				}
			});
			buttonCheckoutBranch.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
					alert.setTitle("Enter branch name e.g. master");                 

					final EditText input = new EditText(SingleGitRepositoryActivity.this); 
					input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
					alert.setView(input);

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							//todo string empty
							String branchName = input.getText().toString();
							if(gitRepository.checkoutBranch(branchName)) {
								ToastNotification.makeToast("Check out succesfull", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
							} else {
								ToastNotification.makeToast("Checked out failed", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
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
		}
		else {
			ToastNotification.makeToast("Wasn't able to find this repo : (", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
		}
	}

	/**
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GITADDFILEREQUEST) {
			if(resultCode == RESULT_OK) {
				String filePathToAdd = data.getStringExtra("currentPath");
				String filename = new File(filePathToAdd).getName();
				if (gitRepository.add(filename)) {
					ToastNotification.makeToast("Added " + filePathToAdd, Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
				} else {
					ToastNotification.makeToast("Adding " + filePathToAdd + "failed!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
				}
			}
			if (resultCode == RESULT_CANCELED) {
			}
		}
	}
}

