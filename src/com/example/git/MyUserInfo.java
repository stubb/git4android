package com.example.git;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class MyUserInfo implements UserInfo, UIKeyboardInteractive {

  @Override
  public String getPassphrase() {
      return null;
  }
  @Override
  public String getPassword() {
      return "BKJubiP!";
  }
  @Override
  public boolean promptPassphrase(String arg0) {
      return false;
  }
  @Override
  public boolean promptPassword(String arg0) {
      return false;
  }
  @Override
  public boolean promptYesNo(String arg0) {
      return false;
  }
  @Override
  public void showMessage(String arg0) {
  }
  
  @Override
  public String[] promptKeyboardInteractive(String arg0, String arg1,
          String arg2, String[] arg3, boolean[] arg4) {
      return null;
  }
}