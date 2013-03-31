package com.example.git;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This activity handles the settings of the application.
 * These settings are:
 * - a private SSH key
 * - a public SSH key
 * It is possible to generate a new SSH key pair or use some different keys, by selecting there location.
 */
public class SettingsActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String TAG = getClass().getName();

	/**
	 * The path to the private key.
	 */
	private String sshPrivateKeyPath = "";

	/**
	 * The path to the public key.
	 */
	private String sshPublicKeyPath = "";
	
	/**
	 * The current context within the application.
	 */
	private final Context currentContext = SettingsActivity.this;

	/**
	 * The absolute path to the default folder where the SSH keys are stored.
	 */
	private final static String defaultAbsoluteKeyPath = Environment.getExternalStorageDirectory() + "/.ssh/";

	/**
	 * The name of the default private key.
	 */
	private final static String defaultPrivateKeyName = "id_rsa";

	/**
	 * The name of the default public key.
	 */
	private final static String defaultPublicKeyName = "id_rsa.pub";

	/**
	 * Constant to identify the origin of the request.
	 */
	private static final int PICKPRIVATEKEYPATHREQUEST = 0;

	/**
	 * Constant to identify the origin of the request.
	 */
	private static final int PICKPUBLICKEYPATHREQUEST = 1;


	@Override
	/**
	 * Called when the activity is starting. Applies actions to each element of the layout.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		SharedPreferences settings = getSharedPreferences(currentContext.getResources().getString(R.string.APPSETTINGS), 0);
		sshPrivateKeyPath = settings.getString(currentContext.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
		sshPublicKeyPath = settings.getString(currentContext.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

		EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
		if (!"".equals(sshPrivateKeyPath)) {
			sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
		}
		sshPrivateKeyPathEditText.setEnabled(false);

		Button sshPrivateKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_private_key_path);
		sshPrivateKeyPathButton.setOnClickListener(new View.OnClickListener() {
			/**
			 * Called when the sshPrivateKeyPathButton button has been clicked.
			 * Starts the FileBrowserActivity, so the user can select the path to the SSH private key.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {
				Intent intent = new Intent(currentContext, FileBrowserActivity.class);
				intent.putExtra(FileBrowserActivity.SELECTIONTYPE, Integer.toString(FileBrowserActivity.SELECTIONTYPE_FILE));
				startActivityForResult(intent, PICKPRIVATEKEYPATHREQUEST);
			}
		});

		EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
		if (!"".equals(sshPublicKeyPath)) {
			sshPublicKeyPathEditText.setText(sshPublicKeyPath);
		}
		sshPublicKeyPathEditText.setEnabled(false);

		Button sshPublicKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_public_key_path);
		sshPublicKeyPathButton.setOnClickListener(new View.OnClickListener() {
			/**
			 * Called when the sshPublicKeyPathButton button has been clicked.
			 * Starts the FileBrowserActivity, so the user can select the path to the SSH public key.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {
				Intent intent = new Intent(currentContext, FileBrowserActivity.class);
				intent.putExtra(FileBrowserActivity.SELECTIONTYPE, Integer.toString(FileBrowserActivity.SELECTIONTYPE_FILE));
				startActivityForResult(intent, PICKPUBLICKEYPATHREQUEST);
			}
		});

		Button generateKeyPairButton = (Button) findViewById(R.id.button_genKeys);
		generateKeyPairButton.setOnClickListener(new View.OnClickListener() {
			/**
			 * Called when the generateKeyPairButton button has been clicked.
			 * Generates a SSH key pair and saves them to the filesystem.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {
				buttonKeyPairGenerationAction();
			}
		});

		Button saveSettingsButton = (Button) findViewById(R.id.button_save);
		saveSettingsButton.setOnClickListener(new View.OnClickListener() {
			/**
			 * Called when the saveSettingsButton button has been clicked.
			 * Saves the settings for the SSH key pair.
			 * @param view The view that was clicked.
			 */
			public void onClick(View view) {
				saveSetting(currentContext.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), sshPrivateKeyPath);
				saveSetting(currentContext.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), sshPublicKeyPath);
				finish();
			}
		});
	}

	/**
	 * Executes the actions to generate the SSH key pair and requests the required user input.
	 */
	private void buttonKeyPairGenerationAction() {
		if ("".equals(sshPrivateKeyPath) && "".equals(sshPrivateKeyPath)) {
			AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);                 
			alert.setTitle(currentContext.getResources().getString(R.string.enter_password) + currentContext.getResources().getString(R.string.optional));  
			alert.setMessage(currentContext.getResources().getString(R.string.password));                

			final EditText input = new EditText(currentContext); 
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			alert.setView(input);

			alert.setPositiveButton(currentContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				/**
				 * This method will be invoked when the PositiveButton button in the dialog is clicked.
				 * It launches the action to generate a new SSH key pair.
				 * @param dialog 	The dialog that received the click.
				 * @param which 	The button that was clicked. 
				 */
				public void onClick(DialogInterface dialog, int whichButton) {			
					if (generateKeyPair(KeyPair.RSA, defaultAbsoluteKeyPath, defaultPrivateKeyName, defaultPublicKeyName, "", input.getText().toString())) {
						ToastNotification.makeToast(currentContext.getResources().getString(R.string.keypair_location) + defaultAbsoluteKeyPath, Toast.LENGTH_LONG, currentContext);
					} else {
						ToastNotification.makeToast(currentContext.getResources().getString(R.string.keypair_generation_failed), Toast.LENGTH_LONG, currentContext);
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
		else {
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.keypair_exists), Toast.LENGTH_LONG, currentContext);
		}
	}

	/**
	 * Save a setting for the application.
	 * @param key	The key of the setting.
	 * @param value The value of the setting.
	 */
	private void saveSetting(String key, String value) {
		SharedPreferences settings = getSharedPreferences(currentContext.getResources().getString(R.string.APPSETTINGS), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Called when the FileBrowserActivity which was launched in onCreate() via the pathButtons exits, gives the requestCode you started it with,
	 * the resultCode it returned, and any additional data from it.
	 * @param	requestCode 	The integer request code originally supplied to startActivityForResult(), allows to identify who this result came from.
	 * @param	resultCode 	The integer result code returned by the child activity through its setResult().
	 * @param	data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKPUBLICKEYPATHREQUEST) {
			if (resultCode == RESULT_OK) {
				sshPublicKeyPath = data.getStringExtra(FileBrowserActivity.SELECTION);
				EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
				sshPublicKeyPathEditText.setText(sshPublicKeyPath);
				sshPublicKeyPathEditText.setEnabled(false);					      
			}	
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.filebrowser_nothing_selected), Toast.LENGTH_LONG, currentContext);
			}
		}
		if (requestCode == PICKPRIVATEKEYPATHREQUEST) {
			if (resultCode == RESULT_OK) {
				sshPrivateKeyPath = data.getStringExtra(FileBrowserActivity.SELECTION);
				EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
				sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
				sshPrivateKeyPathEditText.setEnabled(false);
			}		
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.filebrowser_nothing_selected), Toast.LENGTH_LONG, currentContext);
			}
		}
	}

	/**
	 * Generates a public and a private key, which can be used for SSH.
	 * @param keyType	The type of the keys DSA or RSA, 1 for DSA, 2 for RSA
	 * @param absoluteKeyPath	The path where the keys should be stored.
	 * @param privateKeyFilename The filename of the private key.
	 * @param publicKeyFilename The filename of the public key.
	 * @param comment The comment thats included in the public key file.
	 * @param password The password that will be used for the private key, can be an empty String.
	 */
	private boolean generateKeyPair(Integer keyType, String absoluteKeyPath, String privateKeyFilename, String publicKeyFilename, String comment, String password) {
		boolean success = false;
		boolean folderExists = false;
		File folder = new File(absoluteKeyPath);
		if (!folder.exists()) {
			folderExists = folder.mkdir();
		}
		if (folderExists) {
			if (keyType == KeyPair.DSA || keyType == KeyPair.RSA) {
				JSch jsch = new JSch();
				KeyPair kpair;
				try {
					kpair = KeyPair.genKeyPair(jsch, keyType);
					kpair.setPassphrase(password);
					kpair.writePrivateKey(absoluteKeyPath + privateKeyFilename);
					kpair.writePublicKey(absoluteKeyPath + publicKeyFilename, comment);
					kpair.dispose();
					success = true;
				} catch (JSchException exception) {
					Log.e(TAG, currentContext.getResources().getString(R.string.keypair_creation_failed));
					exception.printStackTrace();
				} catch (FileNotFoundException exception) {
					Log.e(TAG, currentContext.getResources().getString(R.string.keypair_creation_failed));
					exception.printStackTrace();
				} catch (IOException exception) {
					Log.e(TAG, currentContext.getResources().getString(R.string.keypair_creation_failed));
					exception.printStackTrace();
				}
			} else {
				Log.e(TAG, currentContext.getResources().getString(R.string.invalid_ssh_keytype));
			}
		} else {
			Log.e(TAG, currentContext.getResources().getString(R.string.keypair_folder_creation_failed));
		}
		return success;
	}
}
