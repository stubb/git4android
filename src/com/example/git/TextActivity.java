package com.example.git;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

/**
 * This Activity is used to display text a provided text.
 */
public class TextActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String LOGTAG = getClass().getName();
	
	/**
	 * The text that will be displayed.
	 */
	private String text = "";
	
	/**
	 * The name of the intent thats used to provide the text via the intent extras.
	 */
	public static final String TEXTTODISPLAY = "texttodisplay";
	
	@Override
	/**
	 * Called when the activity is starting. Set the layout, fetches the text from the initial intent and displays the text within an EditText view.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_activity);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String tempText = extras.getString(TEXTTODISPLAY);
			if (tempText != null) {
				text = tempText;
				Log.d(LOGTAG, TextActivity.this.getResources().getString(R.string.text_activity_got_texttodisplay_intent));
			}
		}
		// Instead of an TextView an EditText with disabled KeyLIstener is used to provide the select & copy functionality for all API levels
		EditText textView = (EditText) findViewById(R.id.text);
		textView.setKeyListener(null);
		textView.setText(text);
	}
}