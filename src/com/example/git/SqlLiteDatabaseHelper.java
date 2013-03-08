package com.example.git;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SqlLiteDatabaseHelper extends SQLiteOpenHelper {
	
		private static SqlLiteDatabaseHelper mInstance;
		private static final String DATABASE_NAME = "woopwoop";
		private static final int DATABASE_VERSION = 2;
		private static final String TABLE_NAME = "woop";
		private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (repoPath VARCHAR UNIQUE NOT NULL);";
		
		private SqlLiteDatabaseHelper(Context context) {
			 super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
}