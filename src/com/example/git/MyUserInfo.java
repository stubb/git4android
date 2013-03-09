package com.example.git;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * This class provides user information for a Java Secure channel session.
 */
public class MyUserInfo implements UserInfo, UIKeyboardInteractive {

	/**
	 * The password of the user.
	 */
	private String password = "";

	/**
	 * Creates a new MyUserInfo object
	 */
	MyUserInfo() {
	}

	/**
	 * Creates a new MyUserInfo object
	 * @param password The password of the user
	 */
	MyUserInfo(String newPassword) {
		password = newPassword; 
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UserInfo#getPassphrase()
	 */
	public String getPassphrase() {
		return null;
	}

	/**
	 * Set a new password for the user.
	 * @param	newPassword	The new password
	 */
	public void setPassword(String newPassword) {
		password = newPassword;
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UserInfo#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UserInfo#promptPassphrase(java.lang.String)
	 */
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
	 */
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
	 */
	public boolean promptYesNo(String message) {
		return false;
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
	 */
	public void showMessage(String message) {
	}

	@Override
	/**
	 * @see com.jcraft.jsch.UIKeyboardInteractive#promptKeyboardInteractive(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], boolean[])
	 */
	public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {
		return null;
	}
}