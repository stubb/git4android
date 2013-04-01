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
 * This activity lists all Git repositoriy links that are stored in the database.
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
	 * Called when the activity is starting. Attach actions to the layout.
	 * @param savedInstanceState 	If the activity is being re-initialized after previously being shut down
	 *  then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
	 *  Note: Otherwise it is null.
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
				 * Launches the action to open a Git repository or delete its link from the database.
				 * @param parent	The AdapterView where the click happened.
				 * @param view	The view within the AdapterView that was clicked (this will be a view provided by the adapter)
				 * @param position	The position of the view in the adapter.
				 * @param id	The row id of the item that was clicked. 
				 */
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
					openOrDeleteRepositoryAction(gitRepositoryPathList, position, gitRepositoryPathsListView, gitRepositoryDatabase);
				}
			});
		} else {
			ToastNotification.makeToast(currentContext.getResources().getString(R.string.no_repository_known_so_far), Toast.LENGTH_LONG, currentContext);	
		}
	}

	/**
	 * Creates the dialog to open a Git Repository or delete the link to it from the database.
	 * @param gitRepositoryLinks The list of Git repository links and their data.
	 * @param itemPosition The position of th item in the list of the Git repositories.
	 * @param gitRepositoryPaths The view where the dialog should be created on top.
	 * @param database The database where removement will be done.
	 */
	private void openOrDeleteRepositoryAction(final ArrayList<List<String>> gitRepositoryLinks, final Integer itemPosition, final ListView gitRepositoryPaths, final GitRepositoryDatabase database) {
		if (gitRepositoryLinks.size() > 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(currentContext);                 
			builder.setTitle(currentContext.getResources().getString(R.string.repository));
			final String itemPath = getPathOfGitRepositoryLink(itemPosition);
			if (!"".equals(itemPath)) {
				builder.setMessage(itemPath);               
				builder.setPositiveButton(currentContext.getResources().getString(R.string.open), new DialogInterface.OnClickListener() {				
					/**
					 * This method will be invoked when the PositiveButton button in the dialog is clicked.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked (e.g. BUTTON1) or the position of the item clicked. 
					 */
					public void onClick(DialogInterface dialog, int whichButton) {
						File folder = new File(itemPath);
						if (folder.exists()) {
							Intent intent = new Intent(currentContext, SingleGitRepositoryActivity.class);
							intent.putExtra(SingleGitRepositoryActivity.GITREPOSITORYPATH, itemPath);
							startActivity(intent);
						} else {
							ToastNotification.makeToast(currentContext.getResources().getString(R.string.repository_doesnt_exist), Toast.LENGTH_LONG, currentContext);
						}
					}
				});
				builder.setNegativeButton(currentContext.getResources().getString(R.string.delete), new DialogInterface.OnClickListener() {			
					/**
					 * This method will be invoked when the NegativeButton button in the dialog is clicked.
					 * @param dialog 	The dialog that received the click.
					 * @param which 	The button that was clicked (e.g. BUTTON1) or the position of the item clicked. 
					 */
					public void onClick(DialogInterface dialog, int which) {
						database.removeGitRepositoryLink(itemPath);
						loadGitRepositoryList(database, gitRepositoryPaths);
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
			} else {
				Log.e(LOGTAG, currentContext.getResources().getString(R.string.item_not_found));
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.repository_can_not_be_opened), Toast.LENGTH_LONG, currentContext);
			}
		}
	}

	/**
	 * Loads a list of Git repository links and their data from the given database and attach them to the given view via the adapter.
	 * @param gitRepositoryDatabase	The database.
	 * @param gitRepositoryPathsListView	The view.
	 * @return True if the action went successfully, otherwise false. 
	 */
	private boolean loadGitRepositoryList(GitRepositoryDatabase gitRepositoryDatabase, ListView gitRepositoryPathsListView) {
		boolean loaded = false;
		try {
			gitRepositoryPathList = gitRepositoryDatabase.loadGitRepositoriyLinks();
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

	/**
	 * Returns the path of a Git repository Link.
	 * @param position The position of the Git repository in the list of repositories.
	 * @return The path, can be empty if it was not possible to get the path.
	 */
	private String getPathOfGitRepositoryLink(Integer position) {
		String path = "";
		try {
			path = gitRepositoryPathList.get(position).get(0);
		} catch(IndexOutOfBoundsException exception) {
			exception.printStackTrace();
		}
		return path;
	}
}
