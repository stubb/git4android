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
 * This activity lists all repositories that are known by the application.
 */
public class RepositoryListActivity extends Activity {

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
	private List<String> repositoryPathList = new ArrayList<String>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_repository_list);

		final GitRepositoryDatabase repositoryDatabase = GitRepositoryDatabase.getInstance(RepositoryListActivity.this);

		final ListView repositoryPathsListView = (ListView)findViewById(R.id.repo_list_view);


		repositoryPathList = repositoryDatabase.loadRepositories();

		if(!repositoryPathList.isEmpty()) {

			tableRowAdapter = new ArrayAdapter<String>(RepositoryListActivity.this, android.R.layout.simple_list_item_1 , repositoryPathList);
			repositoryPathsListView.setAdapter(tableRowAdapter);
			repositoryPathsListView.setOnItemClickListener( new OnItemClickListener() {

				@Override
				/**
				 * Callback method to be invoked when an item in this AdapterView has been clicked.
				 * @param parent	The AdapterView where the click happened.
				 * @param view	The view within the AdapterView that was clicked (this will be a view provided by the adapter)
				 * @param position	The position of the view in the adapter.
				 * @param id	The row id of the item that was clicked. 
				 */
				public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

					AlertDialog.Builder builder = new AlertDialog.Builder(RepositoryListActivity.this);                 
					builder.setTitle("Repository");
					builder.setMessage(repositoryPathList.get(position));               

					builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {
						//TODO
						public void onClick(DialogInterface dialog, int whichButton) {
							File folder = new File(repositoryPathList.get(position));
							if (folder.exists()) {
								Intent intent = new Intent(RepositoryListActivity.this, SingleRepositoryActivity.class);
								intent.putExtra("repo", repositoryPathList.get(position));
								startActivity(intent);
							} else {
								ToastNotification.makeToast("The repository doesn't exist!", Toast.LENGTH_LONG, RepositoryListActivity.this);
							}
						}
					});  

					builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, repositoryPathList.get(position));
							repositoryDatabase.removeRepository(repositoryPathList.get(position));
							repositoryPathList = repositoryDatabase.loadRepositories();
							tableRowAdapter = new ArrayAdapter<String>(RepositoryListActivity.this, android.R.layout.simple_list_item_1 , repositoryPathList);
							repositoryPathsListView.setAdapter(tableRowAdapter);
							tableRowAdapter.notifyDataSetChanged();
						}
					});
					AlertDialog dialog = builder.create();
					dialog.show(); 			
				}
			});
		} else {
			ToastNotification.makeToast("No repositories known so far", Toast.LENGTH_LONG, RepositoryListActivity.this);	
		}
	}
}
