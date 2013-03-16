package com.example.git;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class handles the storage of the different 
 *
 */
public class SqlLiteDatabaseHelper extends SQLiteOpenHelper {

	private static SqlLiteDatabaseHelper mInstance;
	private static final String DATABASE_NAME = "woopwoop";
	private static final int DATABASE_VERSION = 2;
	private static final String TABLE_NAME = "Repositories";
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (repoPath VARCHAR UNIQUE NOT NULL);";

	/**
	 * 
	 * @param context
	 */
	private SqlLiteDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static SqlLiteDatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SqlLiteDatabaseHelper(context.getApplicationContext());
		}
		return mInstance;
	}

	@Override
	/**
	 * 
	 */
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_CREATE);
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
	public void insertRepositoryPathintoTableRepositories(String path) {
		SQLiteDatabase database = getWritableDatabase();
		database.execSQL("INSERT INTO " + TABLE_NAME + " ('repoPath') VALUES ('" +  path + "');");
	}

	/**
	 * Removes a given path of a Git repository from the database. 
	 * @param path The path to a Git repository that will be removed.
	 */
	public void removeRepositoryPathfromTableRepositories(String path) {
		SQLiteDatabase database = getWritableDatabase();
		database.execSQL("DELETE FROM " + TABLE_NAME + " WHERE repoPath = '" + path + "';");
	}
}