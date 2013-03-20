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
import android.widget.ArrayAdapter;
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
	private ArrayAdapter<String> tableRowAdapter;

	/**
	 * The list that holds all repository paths from the database.
	 */
	private List<String> gitRepositoryPathList = new ArrayList<String>();
	
	/**
	 * The database 
	 */
	private final GitRepositoryDatabase gitRepositoryDatabase = GitRepositoryDatabase.getInstance(GitRepositoryListActivity.this);
	
	/**
	 * The Listview that
	 */
	private final ListView gitRepositoryPathsListView = (ListView)findViewById(R.id.repo_list_view);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repository_list);
		if (loadGitRepositoryList(gitRepositoryDatabase, gitRepositoryPathsListView, gitRepositoryPathList, tableRowAdapter)){
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

					AlertDialog.Builder builder = new AlertDialog.Builder(GitRepositoryListActivity.this);                 
					builder.setTitle("Repository");
					builder.setMessage(gitRepositoryPathList.get(position));               
					builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							File folder = new File(gitRepositoryPathList.get(position));
							if (folder.exists()) {
								Intent intent = new Intent(GitRepositoryListActivity.this, SingleGitRepositoryActivity.class);
								intent.putExtra("repo", gitRepositoryPathList.get(position));
								startActivity(intent);
							} else {
								ToastNotification.makeToast("The repository doesn't exist!", Toast.LENGTH_LONG, GitRepositoryListActivity.this);
							}
						}
					});  
					builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, gitRepositoryPathList.get(position));
							gitRepositoryDatabase.removeRepository(gitRepositoryPathList.get(position));
							loadGitRepositoryList(gitRepositoryDatabase, gitRepositoryPathsListView,
									gitRepositoryPathList, tableRowAdapter);
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show(); 			
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
	private boolean loadGitRepositoryList(GitRepositoryDatabase gitRepositoryDatabase, ListView gitRepositoryPathsListView,
			List<String> gitRepositoryPathList, ArrayAdapter<String> tableRowAdapter) {
		boolean loaded = false;
		gitRepositoryPathList = gitRepositoryDatabase.loadRepositories();
		if(!gitRepositoryPathList.isEmpty()) {
			tableRowAdapter = new ArrayAdapter<String>(GitRepositoryListActivity.this, android.R.layout.simple_list_item_1 , gitRepositoryPathList);
			gitRepositoryPathsListView.setAdapter(tableRowAdapter);
			tableRowAdapter.notifyDataSetChanged();
			loaded = true;
		} 
		return loaded;
	}
}
