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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Log.d(TAG, SettingsActivity.this.getResources().getString(R.string.APPSETTINGS));
		SharedPreferences settings = getSharedPreferences(SettingsActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
		sshPrivateKeyPath = settings.getString(SettingsActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
		sshPublicKeyPath = settings.getString(SettingsActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), "");

		EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
		if (sshPrivateKeyPath != "") {
			sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
		}
		sshPrivateKeyPathEditText.setEnabled(false);

		Button sshPrivateKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_private_key_path);
		sshPrivateKeyPathButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, FileBrowserActivity.class);
				intent.putExtra("originOfRequestforResult", "sshPrivateKeyPathButton");
				startActivityForResult(intent, 1);
			}
		});

		EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
		if (sshPublicKeyPath != "") {
			sshPublicKeyPathEditText.setText(sshPublicKeyPath);
		}
		sshPublicKeyPathEditText.setEnabled(false);

		Button sshPublicKeyPathButton = (Button) findViewById(R.id.button_select_folder_ssh_public_key_path);
		sshPublicKeyPathButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, FileBrowserActivity.class);
				intent.putExtra("originOfRequestforResult", "sshPublicKeyPathButton");
				startActivityForResult(intent, 1);
			}
		});

		Button button_genKeys = (Button) findViewById(R.id.button_genKeys);
		button_genKeys.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (sshPrivateKeyPath == "" && sshPrivateKeyPath == "") {
					AlertDialog.Builder alert = new AlertDialog.Builder(SettingsActivity.this);                 
					alert.setTitle("Enter password (optional)");  
					alert.setMessage("pw");                

					final EditText input = new EditText(SettingsActivity.this); 
					input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
					alert.setView(input);

					alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
						public void onClick(DialogInterface dialog, int whichButton) {
							Log.d(TAG,"generate Keys");				
							if (generateKeyPair("RSA", defaultAbsoluteKeyPath, defaultPrivateKeyName, defaultPublicKeyName, "", input.getText().toString())) {
								ToastNotification.makeToast("Generated the keys in the folder " + defaultAbsoluteKeyPath + "!", Toast.LENGTH_LONG, SettingsActivity.this);
							} else {
								ToastNotification.makeToast("Wasn't able to gen keys : (", Toast.LENGTH_LONG, SettingsActivity.this);
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
				else {
					ToastNotification.makeToast("There are keys already. Nothing to do here.", Toast.LENGTH_LONG, SettingsActivity.this);
				}
			}
		});

		Button saveButton = (Button) findViewById(R.id.button_save);
		saveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(SettingsActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(SettingsActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), sshPrivateKeyPath);
				editor.putString(SettingsActivity.this.getResources().getString(R.string.SSHPUBLICKEYPATHSETTING), sshPublicKeyPath);
				editor.commit();
				finish();
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				if (data.getStringExtra("originOfRequestforResult").equals("sshPrivateKeyPathButton")) {
					sshPrivateKeyPath = data.getStringExtra("currentPath");
				}
				if (data.getStringExtra("originOfRequestforResult").equals("sshPublicKeyPathButton")) {
					sshPublicKeyPath = data.getStringExtra("currentPath");
				}
				EditText sshPrivateKeyPathEditText = (EditText) findViewById(R.id.ssh_private_key_path);
				sshPrivateKeyPathEditText.setText(sshPrivateKeyPath);
				sshPrivateKeyPathEditText.setEnabled(false);

				EditText sshPublicKeyPathEditText = (EditText) findViewById(R.id.ssh_public_key_path);
				sshPublicKeyPathEditText.setText(sshPublicKeyPath);
				sshPublicKeyPathEditText.setEnabled(false);					      
			}
			if (resultCode == RESULT_CANCELED) {
				ToastNotification.makeToast("No valid choice!", Toast.LENGTH_LONG, SettingsActivity.this);
			}
		}
	}

	/**
	 * Generates a public and a private key, which can be used for SSH.
	 * @param keyType	The type of the keys DSA or RSA
	 * @param absoluteKeyPath	The path where the keys should be stored.
	 * @param privateKeyFilename The filename of the private key.
	 * @param publicKeyFilename The filename of the public key.
	 * @param comment The comment thats included in the public key file.
	 * @param password The password that will be used for the private key, can be an empty String.
	 */
	private boolean generateKeyPair(String keyType, String absoluteKeyPath, String privateKeyFilename, String publicKeyFilename, String comment, String password) {
		boolean success = false;
		boolean folderExists = false;
		File folder = new File(absoluteKeyPath);
		if (!folder.exists()) {
			folderExists = folder.mkdir();
		}
		if (folderExists) {
			int type = 0;
			if (keyType.equals("DSA")) {
				type = KeyPair.DSA;
			}
			else {
				type = KeyPair.RSA;
			}
			JSch jsch = new JSch();
			KeyPair kpair;
			try {
				kpair = KeyPair.genKeyPair(jsch, type);
				kpair.setPassphrase(password);
				kpair.writePrivateKey(absoluteKeyPath + privateKeyFilename);
				kpair.writePublicKey(absoluteKeyPath + publicKeyFilename, comment);
				Log.d(TAG,"Finger print: " + kpair.getFingerPrint());
				kpair.dispose();
				success = true;
			} catch (JSchException e) {
				Log.e(TAG, "JSchException gen keys");
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "FileNotFoundException gen keys");
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, "IOException gen key");
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "Wasn't able to create folder for ssh keys");
		}
		return success;
	}
}
