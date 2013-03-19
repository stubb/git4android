package com.example.git;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class handles the storage of the different repositories links.
 */
public final class GitRepositoryDatabase extends SQLiteOpenHelper {

	/**
	 * This is the single instance that is created of this class.
	 */
	private static GitRepositoryDatabase mInstance;
	
	/** 
	 * The Context to access Android related resources.
	 */
	private static Context myContext;
	
	/**
	 * The name of the database that will be created.
	 */
	private static final String DATABASE_NAME = "Git4Android";
	
	/**
	 * The version of the database.
	 */
	private static final int DATABASE_VERSION = 1;
	
	/**
	 * The name of the table that will be created.
	 */
	private static final String TABLE_NAME = "Repositories";
	
	/**
	 * The table that will hold the repositories paths and other attributes.
	 */
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (repoPath VARCHAR UNIQUE NOT NULL);";

	/**
	 * 
	 * @param context
	 */
	private GitRepositoryDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		myContext = context;
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public synchronized static GitRepositoryDatabase getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new GitRepositoryDatabase(context.getApplicationContext());
		}
		return mInstance;
	}

	@Override
	/**
	 * 
	 */
	public void onCreate(SQLiteDatabase db) {
		if(db.isOpen()){
			Log.d("database", "on create");
			db.execSQL(TABLE_CREATE);
		} else {
			Log.d("database", "on create else");
		}
	}

	@Override
	/**
	 * 
	 */
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Inserts a given path to a Git repository into the database.
	 * @param path The path to a Git repository that will be inserted.
	 */
	public void addRepository(String path) {
		SQLiteDatabase database = getWritableDatabase();
		if (database.isOpen()) {
			database.execSQL("INSERT INTO " + TABLE_NAME + " ('repoPath') VALUES ('" +  path + "');");
			database.close();
		} else {
			Log.e("database", "Can't open database!");
		}
	}

	/**
	 * Removes a given path of a Git repository from the database. 
	 * @param path The path to a Git repository that will be removed.
	 */
	public void removeRepository(String path) {
		SQLiteDatabase database = getWritableDatabase();
		if (database.isOpen()) {
			database.execSQL("DELETE FROM " + TABLE_NAME + " WHERE repoPath = '" + path + "';");
			database.close();
		} else {
			Log.e("database", "Can't open database!");
		}
	}

	/**
	 * Loads all 
	 * @return
	 */
	public List<String> loadRepositories() {
		List<String> resultList = new ArrayList<String>();
		SQLiteDatabase database = getWritableDatabase();
		if (database.isOpen()) {
			final String[] columns = new String[]{"repoPath"};
			resultList = loadStringsFromDataBaseTable(database, TABLE_NAME, columns, " ");
			database.close();
		} else {
			Log.d("database", "Can't open database!");
		}
		return resultList;
	}

	/**
	 * Queries the data from specific columns of an SQLite database table and
	 * writes them as string in the resultList.
	 * @param database The database where the data be queried
	 * @param table The table where the data will be queried
	 * @param resultList The List where the queried data will be applied
	 * @param columns The columns of the table where the data will be queried
	 */
	private List<String> loadStringsFromDataBaseTable(SQLiteDatabase database, String table, String[] columns, String spacer) {
		List<String> resultList = new ArrayList<String>();
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
		return resultList;
	}
}