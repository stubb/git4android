package com.example.git;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
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
	private final String LOGTAG = getClass().getName();

	/**
	 * The Adapter to handle the table rows.
	 */
	private GitRepositoryArrayAdapter tableRowAdapter;

	/** 
	 * The Context to access Android related resources.
	 */
	private final Context currentContext = GitRepositoryListActivity.this;

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
		final ListView gitRepositoryPathsListView = (ListView)findViewById(R.id.repo_list_view);
		final GitRepositoryDatabase gitRepositoryDatabase = GitRepositoryDatabase.getInstance(currentContext);
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
					openOrDeleteRepositoryAction(position, gitRepositoryPathsListView, gitRepositoryDatabase);
				}
			});
		} else {
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_repository_known_so_far), Toast.LENGTH_LONG, currentContext);	
		}
	}

	/**
	 * 
	 * @param itemPosition
	 * @param gitRepositoryPaths
	 * @param database
	 */
	private void openOrDeleteRepositoryAction(final Integer itemPosition, final ListView gitRepositoryPaths, final GitRepositoryDatabase database) {
		if (gitRepositoryPathList.size() > 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(currentContext);                 
			builder.setTitle(currentContext.getResources().getString(R.string.repository));
			String itemName = "";
			try {
				itemName = gitRepositoryPathList.get(itemPosition).get(0);
				builder.setMessage(itemName);               
				builder.setPositiveButton(currentContext.getResources().getString(R.string.open), new DialogInterface.OnClickListener() {
					
					/**
					 * This method will be invoked when a button in the dialog is clicked.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked (e.g. BUTTON1) or the position of the item clicked. 
					 */
					public void onClick(DialogInterface dialog, int whichButton) {
						File folder = new File(gitRepositoryPathList.get(itemPosition).get(0));
						if (folder.exists()) {
							Intent intent = new Intent(currentContext, SingleGitRepositoryActivity.class);
							intent.putExtra(SingleGitRepositoryActivity.GITREPOSITORYPATH, gitRepositoryPathList.get(itemPosition).get(0));
							startActivity(intent);
						} else {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.repository_doesnt_exist), Toast.LENGTH_LONG, currentContext);
						}
					}
				});
				builder.setNegativeButton(currentContext.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {
					
					/**
					 * This method will be invoked when a button in the dialog is clicked.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked (e.g. BUTTON1) or the position of the item clicked. 
					 */
					public void onClick(DialogInterface dialog, int which) {
						Log.d(LOGTAG, gitRepositoryPathList.get(itemPosition).get(0));
						database.removeRepository(gitRepositoryPathList.get(itemPosition).get(0));
						loadGitRepositoryList(database, gitRepositoryPaths);
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			} catch (IndexOutOfBoundsException exception) {
				Log.e(LOGTAG, currentContext.getResources().getString(R.string.item_not_found));
				exception.printStackTrace();
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.repository_can_not_be_opened), Toast.LENGTH_LONG, currentContext);
			}
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
			tableRowAdapter = new GitRepositoryArrayAdapter(currentContext, gitRepositoryPathsListView.getId(), gitRepositoryPathList);
			gitRepositoryPathsListView.setAdapter(tableRowAdapter);
			tableRowAdapter.notifyDataSetChanged();
			loaded = true;
		} catch (IndexOutOfBoundsException exception) {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.item_not_found));
			exception.printStackTrace();
		}
		return loaded;
	}
}
