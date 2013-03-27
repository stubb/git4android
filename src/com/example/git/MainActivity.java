package com.example.git;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/**
 * This application can be used to manage Git (http://git-scm.com/) repositories.
 * It is possible to create new repositories or clone existing ones, via the Git, SSH or HTTP/HTTPS protocol.
 * Every created or cloned repository can be accessed from a list. For each of these repositories
 * different actions can be performed:
 * PULL - Fetch new data from a remote instance of a Git repository.
 * ADD - Adds a modified or new file to the Git repository.
 * COMMIT - Make the changes done via ADD persistent.
 * PUSH - Sends the changes done via COMMIT to a remote Git repository via the SSH or HTTP/HTTPS protocol.
 * SET REMOTE - Overwrite the current used remote Git repository.
 * SHOW REMOTE - Show the current used remote Git repository.
 * LOG - Shows a log of all commits done on this repository
 * STATUS - 
 * 
 * This is the first activity that is started on the application start.
 * The activity provides a menu to create, clone or open a Git repository.
 */
public class MainActivity extends Activity {

	// This provider is used to provide necessary functions for the Java Secure Channel library.
	static {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.insertProviderAt(provider, 1);
	}

	@Override
	/**
	 * Called when the activity is starting. Attach actions to the layout.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Button button_list_repos = (Button) findViewById(R.id.button_list_repos);
		Button button_init_repo = (Button) findViewById(R.id.button_init_repo);
		Button button_clone = (Button) findViewById(R.id.button_clone);

		button_list_repos.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, GitRepositoryListActivity.class);
				startActivity(intent);
			}
		});

		button_clone.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CloneGitRepositoryActivity.class);
				startActivity(intent);
			}
		});

		button_init_repo.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, InitGitRepositoryActivity.class);
				startActivity(intent);
			}
		});
	}   

	/**
	 * Performs any final cleanup before an activity is destroyed.
	 * The GitRepositoryDatabase will be closed here.
	 */
	protected void onDestroy(){
		super.onDestroy();
		GitRepositoryDatabase repositoryDatabase = GitRepositoryDatabase.getInstance(MainActivity.this);
		repositoryDatabase.close();
	}

	@Override
	/** Initialize the contents of the Activity's standard options menu from the used layout.
	 * @param menu	The options menu in which you place your items.
	 * @return You must return true for the menu to be displayed; if you return false it will not be shown.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}  

	@Override
	/**
	 * This hook is called whenever an item in the options menu is selected. This calls the SettingsActivity.
	 * @param item 	The menu item that was selected.
	 * @return Return false to allow normal menu processing to proceed, true to consume it here.
	 */
	public boolean onOptionsItemSelected (MenuItem item) {
		boolean returnValue = false;
		if(item.getItemId() == R.id.menu_settings) {
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
			returnValue = true;
		}
		return returnValue;
	}
}
