package com.example.git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class MainActivity extends Activity {

	static {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.insertProviderAt(provider, 1);
	}

	final String TAG = getClass().getName();
	String selectedPath = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button_list_repos = (Button) findViewById(R.id.button_list_repos);
		Button button_init_repo = (Button) findViewById(R.id.button_init_repo);
		Button button_clone = (Button) findViewById(R.id.button_clone);

		button_list_repos.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, RepositoryListActivity.class);
				startActivity(intent);
			}
		});

		button_clone.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CloneRepositoryActivity.class);
				startActivity(intent);
			}
		});

		button_init_repo.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, InitRepositoryActivity.class);
				startActivity(intent);
			}
		});
	}   

	/**
	 * 
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if(resultCode == RESULT_OK) {
				selectedPath = data.getStringExtra("currentPath");
				ToastNotification.makeToast("Directory " + selectedPath.toString() + " selected!", Toast.LENGTH_LONG, MainActivity.this);	     					      
			}

			if (resultCode == RESULT_CANCELED) {
				//Write your code on no result return 
			}
		}
	}

	@Override
	/**
	 * 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}  

	@Override
	/**
	 * 
	 */
	public boolean onOptionsItemSelected (MenuItem item) {
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		startActivity(intent);
		return true;
	}
}
