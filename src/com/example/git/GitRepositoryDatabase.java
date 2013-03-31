package com.example.git;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * This class handles the storage of the different repositories links.
 */
public final class GitRepositoryDatabase extends SQLiteOpenHelper {

	/**
	 * The tag is used to identify the class while logging.
	 */
	private final String LOGTAG = getClass().getName();

	/**
	 * Date format used for dates that should be stored within this database.
	 */
	public final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US); 

	/**
	 * This is the single instance that is created of this class.
	 */
	private static GitRepositoryDatabase mInstance;

	/** 
	 * The Context to access Android related resources.
	 */
	private static Context currentContext;

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
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (repoPath VARCHAR UNIQUE NOT NULL, name VARCHAR, date VARCHAR);";

	/**
	 * 
	 * @param context
	 */
	private GitRepositoryDatabase(Context newContext) {
		super(newContext, DATABASE_NAME, null, DATABASE_VERSION);
		currentContext = newContext;
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
	public void onCreate(SQLiteDatabase database) {
		if(database.isOpen()){
			try {
				database.execSQL(TABLE_CREATE);
			} catch (SQLiteException exception) {
				Log.d(LOGTAG, currentContext.getResources().getString(R.string.table_create_failed));
				exception.printStackTrace();
			}
		} else {
			Log.d(LOGTAG, currentContext.getResources().getString(R.string.database_not_open));
		}
	}

	@Override
	/**
	 * Called when the database needs to be upgraded.
	 * @param db 	The database.
	 * @param oldVersion 	The old database version.
	 * @param newVersion 	The new database version. 
	 */
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		// no update necessary so far
	}

	/**
	 * 
	 * @param path
	 * @param name
	 */
	public boolean addRepository(String path, String name) {
		Date date = new Date();
		return addRepository(path, name, date);
	}

	/**
	 * Inserts a given path to a Git repository into the database.
	 * @param path The path to a Git repository that will be inserted.
	 */
	public boolean addRepository(String path, String name, Date date) {
		boolean added = false;
		try {
			SQLiteDatabase database = getWritableDatabase();
			if (database.isOpen()) {
				database.execSQL("INSERT INTO " + TABLE_NAME + " ('repoPath', 'name', 'date') VALUES ('" +  path + "', '" +  name + "', '" +  dateFormat.format(date) + "');");
				database.close();
				added = true;
			} else {
				Log.e(LOGTAG, currentContext.getResources().getString(R.string.cant_open_database));
			}
		} catch (SQLiteException exception) {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.sql_insert_git_repo_link_entry_invalid));
			exception.printStackTrace();
		}
		return added;
	}

	/**
	 * Removes a given path of a Git repository from the database. 
	 * @param path The path to a Git repository that will be removed.
	 */
	public boolean removeRepository(String path) {
		boolean removed = false;
		try {
			SQLiteDatabase database = getWritableDatabase();
			if (database.isOpen()) {
				database.execSQL("DELETE FROM " + TABLE_NAME + " WHERE repoPath = '" + path + "';");
				database.close();
				removed = true;
			} else {
				Log.e(LOGTAG, currentContext.getResources().getString(R.string.cant_open_database));
			}
		} catch (SQLiteException exception) {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.sql_remove_gitrepo_link_entryinvalid));
			exception.printStackTrace();
		}
		return removed;
	}

	/**
	 * Loads all 
	 * @return
	 */
	public ArrayList<List<String>> loadRepositories() {
		ArrayList<List<String>> resultList = new ArrayList<List<String>>();
		try {
			SQLiteDatabase database = getWritableDatabase();
			if (database.isOpen()) {
				final String[] columns = new String[]{"repoPath", "name", "date"};
				resultList = loadStringsFromDataBaseTable(database, TABLE_NAME, columns);
				database.close();
			} else {
				Log.d(LOGTAG, currentContext.getResources().getString(R.string.cant_open_database));
				ToastNotification.makeToast(currentContext.getResources().getString(R.string.cant_open_database), Toast.LENGTH_LONG, currentContext);
			}
		} catch (SQLiteException exception) {
			Log.e(LOGTAG, currentContext.getResources().getString(R.string.database_cannot_opened_for_writing));
			exception.printStackTrace();
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
	private ArrayList<List<String>> loadStringsFromDataBaseTable(SQLiteDatabase database, String table, String[] columns) {
		ArrayList<List<String>> resultList = new ArrayList<List<String>>();
		Cursor cursor = database.query(table, columns, null, null, null, null, null);
		if (cursor != null) {
			while(cursor.moveToNext()) {
				List<String> row = new ArrayList<String>();
				for (int i = 0; i < columns.length; i++) {
					row.add(new String(cursor.getString(i)));
				}
				resultList.add(row);
			}
			cursor.close();
		}
		return resultList;
	}
}