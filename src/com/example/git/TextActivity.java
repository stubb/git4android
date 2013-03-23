package com.example.git;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * This Activity is used to display text a provided text.
 */
public class TextActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String TAG = getClass().getName();
	
	/**
	 * The text that will be displayed.
	 */
	private String text = "";
	
	public static final String INTENTNAME= "text";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_activity);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String tempText = extras.getString(INTENTNAME);
			if (tempText != null) {
				text = tempText;
			}
		}
		EditText textView = (EditText) findViewById(R.id.text);
		textView.setKeyListener(null);
		textView.setText(text);
	}
}