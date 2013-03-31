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
	 * Called when the activity is starting. Attach actions to the layout.
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
			final int remoteOriginProtocolUrl = gitRepository.checkUrlforProtokoll(gitRepository.getRemoteOriginUrl(), SingleGitRepositoryActivity.this);

			SharedPreferences settings = getSharedPreferences(SingleGitRepositoryActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
			final String sshPrivateKeyPath = settings.getString(SingleGitRepositoryActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
			final String sshPublicKeyPath = settings.getString(SingleGitRepositoryActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

			Button gitPullButton = (Button) findViewById(R.id.button_pull);
			gitPullButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitPullButton button has been clicked.
				 * It launches the action to make a git pull.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitPullAction(remoteOriginProtocolUrl, sshPrivateKeyPath, sshPublicKeyPath);
				}
			});

			Button gitAddFileButton = (Button) findViewById(R.id.button_add_file);
			gitAddFileButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitAddFileButton button has been clicked.
				 * Starts the FileBrowserActivity, so the user can select a file that will be processed by git add.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					Intent intent = new Intent(SingleGitRepositoryActivity.this, FileBrowserActivity.class);
					intent.putExtra(FileBrowserActivity.STARTPATH, filesystemPathToGitRepository);
					intent.putExtra(FileBrowserActivity.SELECTIONTYP, Integer.toString(FileBrowserActivity.SELECTIONTYP_FILE));
					startActivityForResult(intent, GITADDFILEREQUEST);
				}
			});

			Button gitCommitButton = (Button) findViewById(R.id.button_commit);
			gitCommitButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitCommitButton button has been clicked.
				 * It launches the action to make a git commit.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitCommitAction();
				}
			});

			Button gitPushButton = (Button) findViewById(R.id.button_push);
			gitPushButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitPushButton button has been clicked.
				 * It launches the action to make a git push.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitPushAction(remoteOriginProtocolUrl, sshPrivateKeyPath, sshPublicKeyPath);
				}
			});

			Button showRemoteOriginUrlButton = (Button) findViewById(R.id.button_show_remote);
			showRemoteOriginUrlButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the showRemoteOriginUrlButton button has been clicked.
				 * Starts the TextActivity to display the currently used URL of the remote origin git repository.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String urlOfRemoteOriginRepository = gitRepository.getRemoteOriginUrl();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, urlOfRemoteOriginRepository);
					startActivity(intent);
				}
			});

			Button addUrlAsRemoteOriginUrlButton = (Button) findViewById(R.id.button_add_remote);
			addUrlAsRemoteOriginUrlButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the addUrlAsRemoteOriginUrlButton button has been clicked.
				 * It launches the action to add a user provided URL as remote origin URL.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitConfigAddRemoteAsOriginAction();
				}
			});

			Button gitLogButton = (Button) findViewById(R.id.button_log);
			gitLogButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitLogButton button has been clicked.
				 * Starts the TextActivity to display the git log.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String log = gitRepository.log();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, log);
					startActivity(intent);
				}
			});

			Button buttonStatus = (Button) findViewById(R.id.button_status);
			buttonStatus.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the sshPrivateKeyPathButton button has been clicked.
				 * Starts the FileBrowserActivity, so the user can select the path to the SSH private key.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String status = gitRepository.status();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, status);
					startActivity(intent);
				}
			});

			Button buttonCheckoutByCommit = (Button) findViewById(R.id.button_checkout_commit);
			buttonCheckoutByCommit.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the sshPrivateKeyPathButton button has been clicked.
				 * Starts the FileBrowserActivity, so the user can select the path to the SSH private key.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitCheckoutByCommitToNewBranchAction();
				}
			});

			Button buttonShowAllBranches = (Button) findViewById(R.id.button_all_branches);
			buttonShowAllBranches.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the sshPrivateKeyPathButton button has been clicked.
				 * Starts the FileBrowserActivity, so the user can select the path to the SSH private key.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String branchnames = gitRepository.getAllBranchNames();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, branchnames);
					startActivity(intent);
				}
			});

			Button buttonCurrentBranch = (Button) findViewById(R.id.button_current_branch);
			buttonCurrentBranch.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the sshPrivateKeyPathButton button has been clicked.
				 * Starts the FileBrowserActivity, so the user can select the path to the SSH private key.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String branchname = gitRepository.getCurrentBranch();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, branchname);
					startActivity(intent);
				}
			});

			Button buttonCheckoutBranch = (Button) findViewById(R.id.button_checkout_branch);
			buttonCheckoutBranch.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the sshPrivateKeyPathButton button has been clicked.
				 * Starts the FileBrowserActivity, so the user can select the path to the SSH private key.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitCheckoutBranchAction();
				}
			});
		}
		else {
			ToastNotification.makeToast("Wasn't able to find this repository", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
		}
	}

	/**
	 * Called when the FileBrowserActivity which was launched in onCreate() via the gitAddFileButton exits, gives the requestCode you started it with, the resultCode it returned, and any additional data from it.
	 * @param	requestCode 	The integer request code originally supplied to startActivityForResult(), allows to identify who this result came from.
	 * @param	resultCode 	The integer result code returned by the child activity through its setResult().
	 * @param	data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GITADDFILEREQUEST) {
			if(resultCode == RESULT_OK) {
				String filePathToAdd = data.getStringExtra(FileBrowserActivity.SELECTION);
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

	protected void gitPullAction(final Integer remoteOriginProtocolUrl, final String sshPrivateKeyPath, final String sshPublicKeyPath) {
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

	private void gitCommitAction(){
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

	private void gitPushAction(final Integer remoteOriginProtocolUrl, final String sshPrivateKeyPath, final String sshPublicKeyPath){
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

				alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
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
				alert.setTitle("Enter credentials");

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
						if (gitRepository.push(inputUsername.getText().toString(), inputPassword.getText().toString())) {
							ToastNotification.makeToast("Push succesfull!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
						} else {
							ToastNotification.makeToast("Push failed!", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
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

	private void gitCheckoutByCommitToNewBranchAction() {
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
		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
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
		alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;   
			}
		});
		alert.show();
	}

	private void gitCheckoutBranchAction() {
		AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
		alert.setTitle("Enter branch name e.g. master");                 

		final EditText input = new EditText(SingleGitRepositoryActivity.this); 
		input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		alert.setView(input);

		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				String branchName = input.getText().toString();
				if(branchName != null && !branchName.equals("")) {
					if(gitRepository.checkoutBranch(branchName)) {
						ToastNotification.makeToast("Check out succesfull", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
					}
				} else {
					ToastNotification.makeToast("Check out failed", Toast.LENGTH_LONG, SingleGitRepositoryActivity.this);
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

	private void gitConfigAddRemoteAsOriginAction(){
		AlertDialog.Builder alert = new AlertDialog.Builder(SingleGitRepositoryActivity.this);                 
		alert.setTitle("Enter URL");                 

		EditText input = new EditText(SingleGitRepositoryActivity.this); 
		input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		alert.setView(input);
		final String url = input.getText().toString();

		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int whichButton) {
				gitRepository.setRemoteOriginUrl(url);
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
