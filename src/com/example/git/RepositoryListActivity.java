package com.example.git;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

		final SqlLiteDatabaseHelper databaseHelper = SqlLiteDatabaseHelper.getInstance(RepositoryListActivity.this);
		final SQLiteDatabase database = databaseHelper.getWritableDatabase();

		final ListView repositoryPathsListView = (ListView)findViewById(R.id.repo_list_view);

		final String[] columns = new String[]{"repoPath"};
		loadStringsFromDataBaseTable(database, "Repositories", repositoryPathList, columns, " ");

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
						databaseHelper.removeRepositoryPathfromTableRepositories(repositoryPathList.get(position));
						repositoryPathList = new ArrayList<String>();
						loadStringsFromDataBaseTable(database, "Repositories", repositoryPathList, columns, " ");
						tableRowAdapter = new ArrayAdapter<String>(RepositoryListActivity.this, android.R.layout.simple_list_item_1 , repositoryPathList);
						repositoryPathsListView.setAdapter(tableRowAdapter);
						tableRowAdapter.notifyDataSetChanged();
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show(); 			
			}
		});
	}

	/**
	 * Queries the data from specific columns of an SQLite database table and
	 * writes them as string in the resultList.
	 * @param database The database where the data be queried
	 * @param table The table where the data will be queried
	 * @param resultList The List where the queried data will be applied
	 * @param columns The columns of the table where the data will be queried
	 */
	private void loadStringsFromDataBaseTable(SQLiteDatabase database, String table, List<String> resultList, String[] columns, String spacer) {
		Cursor cursor = database.query(table, columns, null, null, null, null, null);
		if (cursor != null) {
			while(cursor.moveToNext()) {
				String rowResult = "";
				for (int i = 0; i < columns.length; i++) {
					rowResult += new String(cursor.getString(0));
					if(i != (columns.length - 1)) {
						rowResult += spacer;
					}
				}
				resultList.add(rowResult);
			}
			cursor.close();
		}
	}
}
