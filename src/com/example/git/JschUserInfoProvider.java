package com.example.git;

import com.jcraft.jsch.UserInfo;

/**
 * This class is used to provide the password for the SSH session
 * via the JavaSecure channel library. Most of the methods from the
 * Interface UserInfo are dummies, without any functionality.
 */
public class JschUserInfoProvider implements UserInfo {

	/**
	 * The password of the user.
	 */
	private String password = "";

	/**
	 * Creates a new JschUserInfoProvider object
	 */
	JschUserInfoProvider() {
	}

	/**
	 * Creates a new JschUserInfoProvider object
	 * @param password The password of the user
	 */
	JschUserInfoProvider(String newPassword) {
		password = newPassword; 
	}

	@Override
	/**
	 * This is a dummy method.
	 * Returns the passphrase entered by the user. 
	 * @return Returns null.
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
	 * Returns the password entered by the user.
	 * @return The password.
	 */
	public String getPassword() {
		return password;
	}

	@Override
	/**
	 * This is a dummy method.
	 * Prompts the user for a passphrase for a public key.
	 * @param	message - the prompt message to be shown to the user. 
	 * @return	Always false.
	 */
	public boolean promptPassphrase(String message) {
		return false;
	}

	@Override
	/**
	 * This is a dummy method.
	 * Prompts the user for a password used for authentication for the remote server.
	 * @param	message - the prompt string to be shown to the user. 
	 * @return	Always false.
	 */
	public boolean promptPassword(String message) {
		return false;
	}

	@Override
	/**
	 *  This is a dummy method.
	 * Prompts the user to answer a yes-no-question. 
	 * @param	message - the prompt message to be shown to the user. 
	 * @return	Always false.
	 */
	public boolean promptYesNo(String message) {
		return false;
	}

	@Override
	/**
	 * This is a dummy method.
	 * Shows an informational message to the user.
	 * @param	message - the message to show to the user.
	 */
	public void showMessage(String message) {
	}
}