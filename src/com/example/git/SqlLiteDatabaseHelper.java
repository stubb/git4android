package com.example.git;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlLiteDatabaseHelper extends SQLiteOpenHelper {
	
		private static SqlLiteDatabaseHelper mInstance;
		private final Context myContext;
		private static final String DATABASE_NAME = "woopwoop";
		private static final int DATABASE_VERSION = 2;
		private static final String TABLE_NAME = "woop";
		private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (repoPath VARCHAR UNIQUE NOT NULL);";
		
		private SqlLiteDatabaseHelper(Context context) {
			 super(context, DATABASE_NAME, null, DATABASE_VERSION);
			 this.myContext = context;
		}
		
		public static SqlLiteDatabaseHelper getInstance(Context context) {
		 if (mInstance == null) {
			 mInstance = new SqlLiteDatabaseHelper(context.getApplicationContext());
		 }
		 return mInstance;
		}
				
		@Override
		public void onCreate(SQLiteDatabase db) {
		    db.execSQL(TABLE_CREATE);
		}

		@Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	    // TODO Auto-generated method stub
	    
    }
		
		/*
		 @Override
		public void close() {
			super.close();
			if (myWritableDb != null) {
				myWritableDb.close();
				myWritableDb = null;
			}
		}
		
		public void insertNewRepo(String path) {
			myWritableDb.execSQL("INSERT INTO " + TABLE_NAME + " (path) VALUES (" +  path + ");");
		}
		
		public void removeRepo(String path) {
			myWritableDb.execSQL("DELETE FROM " + TABLE_NAME + " WHERE path=" + path + ";");
		}
		
		public Cursor getAllRepos(){
			String[] columns = new String[]{"path"};
			Cursor cursor = myWritableDb.query(TABLE_NAME, columns, null, null, null, null, null);
			return cursor;
		}
		public void initDatabase() {
			if ((myWritableDb == null) || (!myWritableDb.isOpen())) {
				myWritableDb = this.getWritableDatabase();
			}
		}
		 */
}