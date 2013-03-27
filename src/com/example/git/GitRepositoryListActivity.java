package com.example.git;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This activity lists all Git repositories that are known by the application.
 */
public class GitRepositoryListActivity extends Activity {

	/**
	 * The tag is used to identify the class while logging
	 */
	private final String TAG = getClass().getName();

	/**
	 * 
	 */
	private MyArrayAdapter tableRowAdapter;

	/**
	 * The list that holds all repository paths from the database.
	 */
	private ArrayList<List<String>> gitRepositoryPathList = new ArrayList<List<String>>();


	@Override
	/**
	 * Called when the activity is starting.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repository_list);
		final GitRepositoryDatabase gitRepositoryDatabase = GitRepositoryDatabase.getInstance(GitRepositoryListActivity.this);
		final ListView gitRepositoryPathsListView = (ListView)findViewById(R.id.repo_list_view);
		if (loadGitRepositoryList(gitRepositoryDatabase, gitRepositoryPathsListView)){
			gitRepositoryPathsListView.setOnItemClickListener( new OnItemClickListener() {

				@Override
				/**
				 * Callback method to be invoked when an item in this AdapterView has been clicked.
				 * @param parent	The AdapterView where the click happened.
				 * @param view	The view within the AdapterView that was clicked (this will be a view provided by the adapter)
				 * @param position	The position of the view in the adapter.
				 * @param id	The row id of the item that was clicked. 
				 */
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
					if (gitRepositoryPathList.size() > 0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(GitRepositoryListActivity.this);                 
					builder.setTitle("Repository");
					String itemName = "";
					try {
						Log.e(TAG, "list size" + Integer.toString(gitRepositoryPathList.size()));
						Log.e(TAG, "position" + Integer.toString(position));

							itemName = gitRepositoryPathList.get(position).get(0);
							builder.setMessage(itemName);               
							builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									File folder = new File(gitRepositoryPathList.get(position).get(0));
									if (folder.exists()) {
										Intent intent = new Intent(GitRepositoryListActivity.this, SingleGitRepositoryActivity.class);
										intent.putExtra(SingleGitRepositoryActivity.GITREPOSITORYPATH, gitRepositoryPathList.get(position).get(0));
										startActivity(intent);
									} else {
										ToastNotification.makeToast("The repository doesn't exist!", Toast.LENGTH_LONG, GitRepositoryListActivity.this);
									}
								}
							});
							builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									Log.d(TAG, gitRepositoryPathList.get(position).get(0));
									gitRepositoryDatabase.removeRepository(gitRepositoryPathList.get(position).get(0));
									loadGitRepositoryList(gitRepositoryDatabase, gitRepositoryPathsListView);
								}
							});

						AlertDialog dialog = builder.create();
						dialog.show();
					} catch (IndexOutOfBoundsException exception) {
						Log.e(TAG, "Item not found");
						exception.printStackTrace();
						ToastNotification.makeToast("The repository cant be opened!", Toast.LENGTH_LONG, GitRepositoryListActivity.this);

					}
					}
				}
			});
		} else {
			ToastNotification.makeToast("No repositories known so far", Toast.LENGTH_LONG, GitRepositoryListActivity.this);	
		}
	}

	/**
	 * 
	 * @param gitRepositoryDatabase
	 * @param gitRepositoryPathsListView
	 * @param gitRepositoryPathList
	 * @param tableRowAdapter
	 * @return
	 */
	private boolean loadGitRepositoryList(GitRepositoryDatabase gitRepositoryDatabase, ListView gitRepositoryPathsListView) {
		boolean loaded = false;
		try {
		gitRepositoryPathList = gitRepositoryDatabase.loadRepositories();
			tableRowAdapter = new MyArrayAdapter(GitRepositoryListActivity.this, gitRepositoryPathsListView.getId(), gitRepositoryPathList);
			gitRepositoryPathsListView.setAdapter(tableRowAdapter);
			tableRowAdapter.notifyDataSetChanged();
			loaded = true;
		} catch (IndexOutOfBoundsException exception) {
			Log.e(TAG, "Item not found");
			exception.printStackTrace();
		}
		Log.e(TAG, "list size in method" + Integer.toString(gitRepositoryPathList.size()));
		Log.e(TAG, "list size in method global" + Integer.toString(gitRepositoryPathList.size()));
		return loaded;
	}
}
