package com.example.git;

import android.util.Log;

import com.jcraft.jsch.Logger;

public class JschLogger implements Logger {

	public boolean isEnabled(int arg0) {
		return true;
	}

	public void log(int arg0, String arg1) {
		Log.d("JschLogger", arg1);
	}
}
