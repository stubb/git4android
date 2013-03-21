package com.example.git;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This class is used to provide the necessary settings for the SSH session
 * via the JavaSecure channel library.
 * These settings are the private/public Key and the password for the private key. 
 */
public class CustomJschConfigSessionFactory extends JschConfigSessionFactory {

	/**
	 * This TAG identifies the class and is used for logging purposes.
	 */
	private final String TAG = getClass().getName();

	/**
	 * The context from where this class is used.
	 */
	private Context context;
	
	/**
	 * The path to the private key.
	 */
	private String privateKeyPath = "";

	/**
	 * The path to the public key.
	 */
	private String publicKeyPath = "";

	/**
	 * The password for the private key.
	 */
	private String privateKeyPassword = "";
	
	/**
	 * The identity name, used for the session.
	 */
	private String name = "";

	/**
	 * Create a new CustomJschConfigSessionFactory object.
	 * @param password The password for the private key.
	 * @param privateKeyPath The path to the private key.
	 * @param publicKeyPath The path to the public key.
	 */
	CustomJschConfigSessionFactory(final Context newContext, final String newName, final String password, final String newPrivateKeyPath, final String newPublicKeyPath) {
		context = newContext;
		name = newName;
		privateKeyPassword = password;
		privateKeyPath = newPrivateKeyPath;
		publicKeyPath = newPublicKeyPath;
	}

	@Override
	/**
	 * Provide additional configuration for the session based on the host information. This method could be used to supply UserInfo.
	 * @param hc The host configuration
	 * @param	session The session to configure
	 */
	protected void configure(Host hc, Session session) {
		try {
			JschUserInfoProvider userinfo = new JschUserInfoProvider(privateKeyPassword);
			session.setUserInfo(userinfo);

			final Properties jschConfig = new Properties();
			jschConfig.put("StrictHostKeyChecking", "no");
			JSch.setConfig(jschConfig);
			JSch.setLogger(new JschAndroidLogger());

			RandomAccessFile publicKeyFile = new RandomAccessFile(publicKeyPath, "rw");
			byte[] publicKey = new byte[(int)publicKeyFile.length()];
			publicKeyFile.read(publicKey);
			publicKeyFile.close();

			RandomAccessFile privateKeyFile = new RandomAccessFile(privateKeyPath, "rw");
			byte[] privateKey = new byte[(int)privateKeyFile.length()];
			privateKeyFile.read(privateKey);
			privateKeyFile.close();

			final JSch jsch = getJSch(hc, FS.DETECTED);
			jsch.addIdentity(name, privateKey, publicKey, privateKeyPassword.getBytes());
		} catch (JSchException e) {
			Log.e(TAG, context.getResources().getString(R.string.jsch_authetifiaction_failed));
			e.printStackTrace();     
		} catch (FileNotFoundException e) {
			Log.e(TAG, context.getResources().getString(R.string.keys_not_found));
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(TAG, context.getResources().getString(R.string.keys_not_readable));
			e.printStackTrace(); 												
		} catch (JGitInternalException e) {
			Log.e(TAG, context.getResources().getString(R.string.jgit_session_configuration_failed));
			e.printStackTrace();
		}
	}
}

