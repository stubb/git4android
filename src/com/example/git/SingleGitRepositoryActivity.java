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

		if (!"".equals(filesystemPathToGitRepository) && gitRepository.open(filesystemPathToGitRepository)) {
			final int remoteOriginProtocolUrlType = gitRepository.checkUrlforProtokoll(gitRepository.getRemoteOriginUrl(), currentContext);

			SharedPreferences settings = getSharedPreferences(currentContext.getResources().getString(R.string.APPSETTINGS), 0);
			final String sshPrivateKeyPath = settings.getString(currentContext.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
			final String sshPublicKeyPath = settings.getString(currentContext.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

			Button gitPullButton = (Button) findViewById(R.id.button_pull);
			gitPullButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitPullButton button has been clicked.
				 * It launches the action to make a git pull.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitPullAction(remoteOriginProtocolUrlType, sshPrivateKeyPath, sshPublicKeyPath);
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
					Intent intent = new Intent(currentContext, FileBrowserActivity.class);
					intent.putExtra(FileBrowserActivity.STARTPATH, filesystemPathToGitRepository);
					intent.putExtra(FileBrowserActivity.SELECTIONTYPE, Integer.toString(FileBrowserActivity.SELECTIONTYPE_FILE));
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
					gitPushAction(remoteOriginProtocolUrlType, sshPrivateKeyPath, sshPublicKeyPath);
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

			Button gitStatusButton = (Button) findViewById(R.id.button_status);
			gitStatusButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the sgitStatusButton button has been clicked.
				 * Starts the TextActivity to display the git status.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String status = gitRepository.status();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, status);
					startActivity(intent);
				}
			});

			Button gitCheckoutByCommitButton = (Button) findViewById(R.id.button_checkout_commit);
			gitCheckoutByCommitButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitCheckoutByCommitButton button has been clicked.
				 * It launches the action to checkout a commit to a new branch.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitCheckoutByCommitToNewBranchAction();
				}
			});

			Button gitBranchButton = (Button) findViewById(R.id.button_all_branches);
			gitBranchButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitBranchButton button has been clicked.
				 * Starts the TextActivity to display the all known branches.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String branchnames = gitRepository.getAllBranchNames();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, branchnames);
					startActivity(intent);
				}
			});

			Button gitShowCurrentBranchButton = (Button) findViewById(R.id.button_current_branch);
			gitShowCurrentBranchButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitShowCurrentBranchButton button has been clicked.
				 * Starts the TextActivity to display the current used branch.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					String branchname = gitRepository.getCurrentBranch();
					Intent intent = new Intent(currentContext, TextActivity.class);
					intent.putExtra(TextActivity.TEXTTODISPLAY, branchname);
					startActivity(intent);
				}
			});

			Button gitCheckoutBranchButton = (Button) findViewById(R.id.button_checkout_branch);
			gitCheckoutBranchButton.setOnClickListener(new View.OnClickListener() {
				/**
				 * Called when the gitCheckoutBranchButton button has been clicked.
				 * It launches the action to checkout to checkout a branch.
				 * @param view The view that was clicked.
				 */
				public void onClick(View view) {
					gitCheckoutBranchAction();
				}
			});
		}
		else {
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_repository_with_given_path_doesnt_exists) + filesystemPathToGitRepository, Toast.LENGTH_LONG, currentContext);
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
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.add_success) + ":"  + filePathToAdd, Toast.LENGTH_LONG, currentContext);
				} else {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.add_fail) + ":" + filePathToAdd, Toast.LENGTH_LONG, currentContext);
				}
			}
			if (resultCode == RESULT_CANCELED) {
			}
		}
	}

	/**
	 * This method handles the user interaction to get the required data to perform a pull from a remote Git repository.
	 * @param remoteOriginProtocolUrlType The type of the URL.
	 * @param sshPrivateKeyPath	The path to the private SSH key.
	 * @param sshPublicKeyPath The path to the public SSH key.
	 */
	protected void gitPullAction(final Integer remoteOriginProtocolUrl, final String sshPrivateKeyPath, final String sshPublicKeyPath) {
		if(gitRepository.getRemoteOriginUrl().equals("")) {
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_remote_origin_url_configured), Toast.LENGTH_LONG, currentContext);
		}
		else {
			if (remoteOriginProtocolUrl == currentContext.getResources().getInteger(R.integer.GITPROTOCOL)) {
				if(gitRepository.pull()) {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_succesful), Toast.LENGTH_LONG, currentContext);
				} else{
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_failed), Toast.LENGTH_LONG, currentContext);
				}
			}
			else if (remoteOriginProtocolUrl == currentContext.getResources().getInteger(R.integer.SSHPROTOCOL)) {
				AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
				alert.setTitle(currentContext.getResources().getString(R.string.enter_password));           

				final EditText inputPassword = new EditText(currentContext); 
				inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				alert.setView(inputPassword);

				alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the PositiveButton button in the dialog is clicked.
					 * It executes the Git pull action.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int whichButton) {
						if(gitRepository.pull(inputPassword.getText().toString(), sshPrivateKeyPath, sshPublicKeyPath))  {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_succesful), Toast.LENGTH_LONG, currentContext);
						} else{
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_failed), Toast.LENGTH_LONG, currentContext);
						}
					}
				});

				alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the NegativeButton button in the dialog is clicked.
					 * It does nothing except a return to cancel the dialog.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int which) {
						return;   
					}
				});
				alert.show();
			}
			else if (remoteOriginProtocolUrl == currentContext.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
					remoteOriginProtocolUrl == currentContext.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
				AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);
				alert.setTitle(currentContext.getResources().getString(R.string.enter_credentials));

				LinearLayout linearLayout = new LinearLayout(currentContext);
				linearLayout.setOrientation(1);

				final EditText inputUsername = new EditText(currentContext); 
				inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
				inputUsername.setHint(currentContext.getResources().getString(R.string.username));
				linearLayout.addView(inputUsername);

				final EditText inputPassword = new EditText(currentContext); 
				inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				inputPassword.setHint(currentContext.getResources().getString(R.string.password));
				linearLayout.addView(inputPassword);

				alert.setView(linearLayout);

				alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the PositiveButton button in the dialog is clicked.
					 * It executes the Git pull action.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int whichButton) {
						if(gitRepository.pull(inputUsername.getText().toString(), inputPassword.getText().toString())) {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_succesful), Toast.LENGTH_LONG, currentContext);
						} else{
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_pull_failed), Toast.LENGTH_LONG, currentContext);
						}
					}
				});

				alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the NegativeButton button in the dialog is clicked.
					 * It does nothing except a return to cancel the dialog.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int which) {
						return;   
					}
				});
				alert.show();
			} 
		}
	}

	/**
	 * This method handles the user interaction to get a commit message to perform a git commit to the currently checked out branch.
	 */
	private void gitCommitAction(){
		AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
		alert.setTitle(currentContext.getResources().getString(R.string.enter_commit_message));                

		final EditText inputMessage = new EditText(currentContext); 
		inputMessage.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		alert.setView(inputMessage);

		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the PositiveButton button in the dialog is clicked.
			 * It executes the Git commit action.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int whichButton) {
				if (gitRepository.commit(inputMessage.getText().toString())) {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_commit_succesful), Toast.LENGTH_LONG, currentContext);
				} else{
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_commit_failed), Toast.LENGTH_LONG, currentContext);
				}
			}
		});

		alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the NegativeButton button in the dialog is clicked.
			 * It does nothing except a return to cancel the dialog.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int which) {
				return;   
			}
		});
		alert.show();
	}

	/**
	 * This method handles the user interaction to get the required data to perform a push to a remote Git repository.
	 * @param remoteOriginProtocolUrlType The type of the URL.
	 * @param sshPrivateKeyPath	The path to the private SSH key.
	 * @param sshPublicKeyPath The path to the public SSH key.
	 */
	private void gitPushAction(final Integer remoteOriginProtocolUrlType, final String sshPrivateKeyPath, final String sshPublicKeyPath){
		if("".equals(gitRepository.getRemoteOriginUrl())) {
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_remote_origin_url_set), Toast.LENGTH_LONG, currentContext);
		}
		else {
			if (remoteOriginProtocolUrlType == currentContext.getResources().getInteger(R.integer.GITPROTOCOL)) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_protocol_read_only_no_push), Toast.LENGTH_LONG, currentContext);
			}
			else if (remoteOriginProtocolUrlType == currentContext.getResources().getInteger(R.integer.SSHPROTOCOL)) {
				AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
				alert.setTitle(currentContext.getResources().getString(R.string.enter_password));             

				final EditText inputPassword = new EditText(currentContext); 
				inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				alert.setView(inputPassword);

				alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the PositiveButton button in the dialog is clicked.
					 * It executes the Git push action.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int whichButton) {
						if (!"".equals(sshPublicKeyPath) && !"".equals(sshPrivateKeyPath)) {
							if (gitRepository.push(inputPassword.getText().toString(), sshPrivateKeyPath, sshPublicKeyPath)) {
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_push_success), Toast.LENGTH_LONG, currentContext);
							} else {
								ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_push_fail), Toast.LENGTH_LONG, currentContext);
							}
						}
					}
				});

				alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the NegativeButton button in the dialog is clicked.
					 * It does nothing except a return to cancel the dialog.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int which) {
						return;   
					}
				});
				alert.show();
			}
			else if (remoteOriginProtocolUrlType == currentContext.getResources().getInteger(R.integer.HTTPPROTOCOL) || 
					remoteOriginProtocolUrlType == currentContext.getResources().getInteger(R.integer.HTTPSPROTOCOL)) {
				AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);
				alert.setTitle(currentContext.getResources().getString(R.string.enter_credentials));

				LinearLayout linearLayout = new LinearLayout(currentContext);
				linearLayout.setOrientation(1);

				final EditText inputUsername = new EditText(currentContext); 
				inputUsername.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
				inputUsername.setHint(currentContext.getResources().getString(R.string.username));
				linearLayout.addView(inputUsername);

				final EditText inputPassword = new EditText(currentContext); 
				inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				inputPassword.setHint(currentContext.getResources().getString(R.string.password));
				linearLayout.addView(inputPassword);

				alert.setView(linearLayout);

				alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the PositiveButton button in the dialog is clicked.
					 * It executes the Git push action.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int whichButton) {
						if (gitRepository.push(inputUsername.getText().toString(), inputPassword.getText().toString())) {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_push_success), Toast.LENGTH_LONG, currentContext);
						} else {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_push_fail), Toast.LENGTH_LONG, currentContext);
						}
					}
				});

				alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
					/**
					 * This method will be invoked when the NegativeButton button in the dialog is clicked.
					 * It does nothing except a return to cancel the dialog.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked. 
					 */
					public void onClick(DialogInterface dialog, int which) {
						return;   
					}
				});
				alert.show();
			}
		}
	}

	/**
	 * This method handles the user interaction to get an commit ID and a name for a new branch, afterwards it does a check out from the given
	 * commit into a new branch with the given name.
	 */
	private void gitCheckoutByCommitToNewBranchAction() {
		AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
		alert.setTitle(currentContext.getResources().getString(R.string.git_checkout_to_new_branch_from_commitid));

		LinearLayout linearLayout = new LinearLayout(currentContext);
		linearLayout.setOrientation(1);

		final EditText branchNameView = new EditText(currentContext); 
		branchNameView.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		branchNameView.setHint(currentContext.getResources().getString(R.string.branch_name));
		linearLayout.addView(branchNameView);

		final EditText commitIdView = new EditText(currentContext);
		commitIdView.setHint(currentContext.getResources().getString(R.string.commit_id));
		commitIdView.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		linearLayout.addView(commitIdView);

		alert.setView(linearLayout);
		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the PositiveButton button in the dialog is clicked.
			 * It executes the Git checkout by commit to a new branch action.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int whichButton) {
				String branchName = "";
				branchName = branchNameView.getText().toString();
				String commitId = "";
				commitId = commitIdView.getText().toString();
				if(gitRepository.checkoutCommitToNewBranch(commitId, branchName)) {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_checkout_success), Toast.LENGTH_LONG, currentContext);
				} else {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_checkout_fail), Toast.LENGTH_LONG, currentContext);
				}
			}
		});
		alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the NegativeButton button in the dialog is clicked.
			 * It does nothing except a return to cancel the dialog.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int which) {
				return;   
			}
		});
		alert.show();
	}

	/**
	 * This method handles the user interaction to get a branch name that will be checked out afterwards.
	 */
	private void gitCheckoutBranchAction() {
		AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
		alert.setTitle(currentContext.getResources().getString(R.string.enter_branch_name));                 

		final EditText input = new EditText(currentContext); 
		input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
		alert.setView(input);

		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the PositiveButton button in the dialog is clicked.
			 * It executes the Git checkout branch action.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int whichButton) {
				String branchName = input.getText().toString();
				if(branchName != null && !"".equals(branchName)) {
					if(gitRepository.checkoutBranch(branchName)) {
						ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_checkout_success), Toast.LENGTH_LONG, currentContext);
					}
				} else {
					ToastNotification.makeToast(currentContext.getResources().getString(R.string.git_checkout_fail), Toast.LENGTH_LONG, currentContext);
				}
			}
		});
		alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the NegativeButton button in the dialog is clicked.
			 * It does nothing except a return to cancel the dialog.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int which) {
				return;   
			}
		});
		alert.show();
	}

	/**
	 * This method handles the user interaction to get the required data to get an URL which will be afterwards set as the URL for the remote origin Git repository.
	 */
	private void gitConfigAddRemoteAsOriginAction() {
		AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
		alert.setTitle(currentContext.getResources().getString(R.string.enter_url));                 

		EditText input = new EditText(SingleGitRepositoryActivity.this); 
		input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
		alert.setView(input);
		final String url = input.getText().toString();

		alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the PositiveButton button in the dialog is clicked.
			 * It executes the action to set the URL of the remote origin repository.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int whichButton) {
				gitRepository.setRemoteOriginUrl(url);
			}
		});

		alert.setNegativeButton(currentContext.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			/**
			 * This method will be invoked when the NegativeButton button in the dialog is clicked.
			 * It does nothing except a return to cancel the dialog.
			 * @param dialog 	The dialog that received the click.
			 * @param which 	The button that was clicked. 
			 */
			public void onClick(DialogInterface dialog, int which) {
				return;   
			}
		});
		alert.show();
	}
}
