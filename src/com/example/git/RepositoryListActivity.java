package com.example.git;

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
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * @author kili
 *
 */
public class RepositoryListActivity extends Activity {

			private final String TAG = getClass().getName();
			private ListView repositoryPathsListView;
			private ArrayAdapter<String> tableRowAdapter;			
			private List<String> repositoryPathList = new ArrayList<String>();
		
			@Override
			public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.repository_list);
				
				SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(RepositoryListActivity.this);
				final SQLiteDatabase db = dbHelper.getWritableDatabase();
				
				repositoryPathsListView = (ListView)findViewById(R.id.repo_list_view);
	      // By using setAdpater method in listview we an add string array in list.
			  
			  final String[] columns = new String[]{"repoPath"};
			  loadStringsFromDataBaseTable(db, "woop", repositoryPathList, columns, " ");
				  
			  tableRowAdapter = new ArrayAdapter<String>(RepositoryListActivity.this, android.R.layout.simple_list_item_1 , repositoryPathList);
			  repositoryPathsListView.setAdapter(tableRowAdapter);
			  repositoryPathsListView.setOnItemClickListener( new OnItemClickListener() {
						@Override
	          public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
							
							AlertDialog.Builder builder = new AlertDialog.Builder(RepositoryListActivity.this);                 
							builder.setTitle("Repository");
							builder.setMessage(repositoryPathList.get(arg2));               

							builder.setPositiveButton("OPEN", new DialogInterface.OnClickListener() {  
								public void onClick(DialogInterface dialog, int whichButton) {  
		    					Intent intent = new Intent(RepositoryListActivity.this, SingleRepositoryActivity.class);
		    					intent.putExtra("repo", repositoryPathList.get(arg2));
		    					startActivity(intent);									
									}
							});  

							builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									Log.d(TAG, repositoryPathList.get(arg2));
									db.execSQL("DELETE FROM " + "woop" + " WHERE repoPath = '" + repositoryPathList.get(arg2) + "';");
									repositoryPathList = new ArrayList<String>();
							  	loadStringsFromDataBaseTable(db, "woop", repositoryPathList, columns, " ");
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
			 * Queries the data from specific columns of an sqlite database table and
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
