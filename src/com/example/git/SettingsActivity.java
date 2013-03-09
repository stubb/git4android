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

import java.io.FileNotFoundException;
import java.io.IOException;

public class SettingsActivity extends Activity {

	private final String TAG = getClass().getName();
	private String sshPrivateKeyPath = "";
	private String sshPublicKeyPath = "";

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
				Intent intent = new Intent(SettingsActivity.this, BrowserActivity.class);
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
				Intent intent = new Intent(SettingsActivity.this, BrowserActivity.class);
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
							if (generateKeyPair("RSA", Environment.getExternalStorageDirectory() + "/.ssh/", "id_rsa", "", input.getText().toString())) {
								ToastNotification.makeToast("The keys can be found at " + Environment.getExternalStorageDirectory() + "/.ssh/", Toast.LENGTH_LONG, SettingsActivity.this);
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
	 * 
	 * @param keyType
	 * @param absolutePath
	 * @param filename
	 * @param comment
	 * @param password
	 */
	private boolean generateKeyPair(String keyType, String absolutePath, String filename, String comment, String password) {
		boolean success = false;
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
			kpair.writePrivateKey(absolutePath + filename);
			kpair.writePublicKey(absolutePath + filename + ".pub", comment);
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
		return success;
	}
}
