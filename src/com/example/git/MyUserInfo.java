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
	 * 
	 */
	MyUserInfo() {
	}
	
	/**
	 * Creates a new MyUserInfo 
	 * @param password
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
   * 
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
  public boolean promptPassphrase(String arg0) {
      return false;
  }
  
  @Override
  /**
   * @see com.jcraft.jsch.UserInfo#promptPassword(java.lang.String)
   */
  public boolean promptPassword(String arg0) {
      return false;
  }
  
  @Override
  /**
   * @see com.jcraft.jsch.UserInfo#promptYesNo(java.lang.String)
   */
  public boolean promptYesNo(String arg0) {
      return false;
  }
  
  @Override
  /**
   * @see com.jcraft.jsch.UserInfo#showMessage(java.lang.String)
   */
  public void showMessage(String arg0) {
  }
  
  @Override
  /**
   * @see com.jcraft.jsch.UIKeyboardInteractive#promptKeyboardInteractive(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], boolean[])
   */
  public String[] promptKeyboardInteractive(String arg0, String arg1,
          String arg2, String[] arg3, boolean[] arg4) {
      return null;
  }
}