package com.example.git;

import android.content.Context;
import android.widget.Toast;

public class ToastNotification {

	private ToastNotification() {
	}
	
	/**
	 * 	Erzeugt einen Toast auf dem Bildschirm.
	 * 
	 * 	@param message	Nachricht die angezeigt werden soll.
	 * 	@param duration	Dauer der Nachrichtenanzeige.
	 */
	public static void makeToast(String message, int duration, Context mcon) {
		Context context = mcon.getApplicationContext();
		CharSequence text = message;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();		
	}
}