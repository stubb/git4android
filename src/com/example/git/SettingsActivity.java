package com.example.git;

import android.app.Activity;
import android.app.AlertDialog;
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
 * It is possible to generate new SSH keys and select key files
 * which should be used with this application.
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

		Log.d(TAG, SettingsActivity.this.getResources().getString(R.string.APPSETTINGS));
		SharedPreferences settings = getSharedPreferences(SettingsActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
		sshPrivateKeyPath = settings.getString(SettingsActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
		sshPublicKeyPath = settings.getString(SettingsActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

		EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
		if (!sshPrivateKeyPath.equals("")) {
			sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
		}
		sshPrivateKeyPathEditText.setEnabled(false);

		Button sshPrivateKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_private_key_path);
		sshPrivateKeyPathButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, FileBrowserActivity.class);
				intent.putExtra("selectionTyp", Integer.toString(FileBrowserActivity.SELECTIONTYP_FILE));
				startActivityForResult(intent, PICKPRIVATEKEYPATHREQUEST);
			}
		});

		EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
		if (!sshPublicKeyPath.equals("")) {
			sshPublicKeyPathEditText.setText(sshPublicKeyPath);
		}
		sshPublicKeyPathEditText.setEnabled(false);

		Button sshPublicKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_public_key_path);
		sshPublicKeyPathButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, FileBrowserActivity.class);
				intent.putExtra("selectionTyp", Integer.toString(FileBrowserActivity.SELECTIONTYP_FILE));
				startActivityForResult(intent, PICKPUBLICKEYPATHREQUEST);
			}
		});

		Button button_genKeys = (Button) findViewById(R.id.button_genKeys);
		button_genKeys.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonKeyPairGenerationAction();
			}
		});

		Button saveButton = (Button) findViewById(R.id.button_save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveSetting(SettingsActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), sshPrivateKeyPath);
				saveSetting(SettingsActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), sshPublicKeyPath);
				finish();
			}
		});
	}

	/**
	 * Executes the actions to generate the SSH key pair.
	 */
	private void buttonKeyPairGenerationAction() {
		if (sshPrivateKeyPath.equals("") && sshPrivateKeyPath.equals("")) {
			AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);                 
			alert.setTitle(SettingsActivity.this.getResources().getString(R.string.enter_password) + SettingsActivity.this.getResources().getString(R.string.optional));  
			alert.setMessage(SettingsActivity.this.getResources().getString(R.string.password));                

			final EditText input = new EditText(SettingsActivity.this); 
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
			alert.setView(input);

			alert.setPositiveButton(SettingsActivity.this.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int whichButton) {			
					if (generateKeyPair(KeyPair.RSA, defaultAbsoluteKeyPath, defaultPrivateKeyName, defaultPublicKeyName, "", input.getText().toString())) {
						ToastNotification.makeToast(SettingsActivity.this.getResources().getString(R.string.keypair_location) + defaultAbsoluteKeyPath, Toast.LENGTH_LONG, SettingsActivity.this);
					} else {
						ToastNotification.makeToast(SettingsActivity.this.getResources().getString(R.string.keypair_generation_failed), Toast.LENGTH_LONG, SettingsActivity.this);
					}									
				}
			});

			alert.setNegativeButton(SettingsActivity.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;   
				}
			});
			alert.show();
		}
		else {
			ToastNotification.makeToast(SettingsActivity.this.getResources().getString(R.string.keypair_exists), Toast.LENGTH_LONG, SettingsActivity.this);
		}
	}

	/**
	 * Save a setting for the application.
	 * @param key	The key of the setting.
	 * @param value The value of the setting.
	 */
	private void saveSetting(String key, String value) {
		SharedPreferences settings = getSharedPreferences(SettingsActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Called when the FileBrowserActivity which was launched in onCreate() via the pathButtons exits, giving you the requestCode you started it with, the resultCode it returned, and any additional data from it.
	 * The resultCode will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result, or crashed during its operation.
	 * You will receive this call immediately before onResume() when your activity is re-starting.
	 * @param	requestCode 	The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
	 * @param	resultCode 	The integer result code returned by the child activity through its setResult().
	 * @param	data 	An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PICKPUBLICKEYPATHREQUEST) {
			if (resultCode == RESULT_OK) {
				sshPublicKeyPath = data.getStringExtra("currentPath");
				EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
				sshPublicKeyPathEditText.setText(sshPublicKeyPath);
				sshPublicKeyPathEditText.setEnabled(false);					      
			}	
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(SettingsActivity.this.getResources().getString(R.string.filebrowser_nothing_selected), Toast.LENGTH_LONG, SettingsActivity.this);
			}
		}
		if (requestCode == PICKPRIVATEKEYPATHREQUEST) {
			if (resultCode == RESULT_OK) {
				sshPrivateKeyPath = data.getStringExtra("currentPath");
				EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
				sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
				sshPrivateKeyPathEditText.setEnabled(false);
			}		
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast(SettingsActivity.this.getResources().getString(R.string.filebrowser_nothing_selected), Toast.LENGTH_LONG, SettingsActivity.this);
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
					Log.e(TAG, SettingsActivity.this.getResources().getString(R.string.keypair_creation_failed));
					exception.printStackTrace();
				} catch (FileNotFoundException exception) {
					Log.e(TAG, SettingsActivity.this.getResources().getString(R.string.keypair_creation_failed));
					exception.printStackTrace();
				} catch (IOException exception) {
					Log.e(TAG, SettingsActivity.this.getResources().getString(R.string.keypair_creation_failed));
					exception.printStackTrace();
				}
			} else {
				Log.e(TAG, SettingsActivity.this.getResources().getString(R.string.invalid_ssh_keytype));
			}
		} else {
			Log.e(TAG, SettingsActivity.this.getResources().getString(R.string.keypair_folder_creation_failed));
		}
		return success;
	}
}
