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
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RepositoryListActivity extends Activity{

			private final String TAG = getClass().getName();
			private ListView fileListView;
			private ArrayAdapter<String> wurst;
			
			private List<String> repoList = new ArrayList<String>();
			
			SqlLiteDatabaseHelper dbHelper = SqlLiteDatabaseHelper.getInstance(this);
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			
			public void insertNewRepo(String path) {
				db.execSQL("INSERT INTO " + "woop" + " (repoPath) VALUES (" +  path + ");");
			}
			
			public void removeRepo(String path) {
				db.execSQL("DELETE FROM " + "woop" + " WHERE repoPath=" + path + ";");
			}
			
			@Override
			public void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);
				setContentView(R.layout.repository_list);

			  fileListView = (ListView)findViewById(R.id.repo_list_view);
	      // By using setAdpater method in listview we an add string array in list.
			  String[] columns = new String[]{"repoPath"};
			  Cursor c = db.query("woop", columns, null, null, null, null, null);
			  if (c != null) {
			      while(c.moveToNext()) {
			      	repoList.add(new String(c.getString(0)));
			      }
			      c.close();
			  }
				  
			  wurst = new ArrayAdapter<String>(RepositoryListActivity.this, android.R.layout.simple_list_item_1 , repoList);
			  fileListView.setAdapter(wurst);
			  fileListView.setOnItemClickListener( new OnItemClickListener() {
						@Override
	          public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    					Intent intent = new Intent(RepositoryListActivity.this, SingleRepositoryActivity.class);
    					intent.putExtra("repo", repoList.get(arg2));
    					startActivity(intent);
	          }
				});	   
		}
}
