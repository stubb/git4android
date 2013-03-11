package com.example.git;

import android.content.Context;
import android.widget.Toast;

/**
 * This class provides a toast notification. The notification is displayed
 *  on top of all other GUI elements. 
 */
public class ToastNotification {

	/**
	 * Creates a new toast notification.
	 */
	private ToastNotification() {
	}

	/**
	 * 	Show a notification with the given, message for a specific duration for
	 * 	the application context. The notification isn't destroyed while changing
	 * 	the activities.
	 * 
	 * 	@param message	The message that will be displayed.
	 * 	@param duration	Duration.
	 *  @param context	The activity context.
	 */
	public static void makeToast(String message, int duration, Context context) {
		Context applicationContext = context.getApplicationContext();
		CharSequence text = message;
		Toast toast = Toast.makeText(applicationContext, text, duration);
		toast.show();		
	}
}