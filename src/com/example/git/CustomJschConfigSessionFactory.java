package com.example.git;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;

import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;

import android.util.Log;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

/**
 * 
 * @author kili
 *
 */
public class CustomJschConfigSessionFactory extends JschConfigSessionFactory {
	
	private final String TAG = getClass().getName();
	private String privateKeyPath = "";
	private String publicKeyPath = "";
	private byte[] privateKeyPassword = null;
	
	/**
	 * 
	 * @param password
	 * @param privateKeyPath
	 * @param publicKeyPath
	 */
	CustomJschConfigSessionFactory(final byte[] password, final String newPrivateKeyPath, final String newPublicKeyPath) {
		privateKeyPassword = password;
		privateKeyPath = newPrivateKeyPath;
		publicKeyPath = newPublicKeyPath;
	}
	
	@Override
	/**
	 * 
	 */
  protected void configure(Host hc, Session session) {
		try {
			UserInfo userinfo = new MyUserInfo();
			session.setUserInfo(userinfo);
			
			final Properties jschConfig = new Properties();
			jschConfig.put("StrictHostKeyChecking", "no");
		  JSch.setConfig(jschConfig);
		  JSch.setLogger(new JschLogger());
		    
			RandomAccessFile publicKeyFile = new RandomAccessFile(publicKeyPath, "rw");
			byte [] publicKey = new byte[(int)publicKeyFile.length()];
			publicKeyFile.read(publicKey);
			publicKeyFile.close();
  	    
			RandomAccessFile privateKeyFile = new RandomAccessFile(privateKeyPath, "rw");
			byte [] privateKey = new byte[(int)privateKeyFile.length()];
			privateKeyFile.read(privateKey);
			privateKeyFile.close();
  	    
			final JSch jsch = getJSch(hc, FS.DETECTED);
        jsch.addIdentity("git", privateKey, publicKey, privateKeyPassword); 
     } catch (JSchException e) {
        throw new RuntimeException(e);       
     } catch (FileNotFoundException e) {
    	 Log.e(TAG, "Private/Public key not found!");
       e.printStackTrace();
     } catch (IOException e) {
    	 Log.e(TAG, "Private/Public key not readable!");
       e.printStackTrace(); 												
     } catch (JGitInternalException e) {
    	 e.printStackTrace();
     }
    }
  }

