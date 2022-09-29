package com.arindo.nura;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SqlModify {
	//******************** This is Order Temporary ***********************//
	public static boolean CekColumn(SqlHelper dbHelper) {
		try{
			//query 1 row
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Cursor mCursor  = db.rawQuery( "SELECT * FROM tbl_tmporder LIMIT 0", null );

			//getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
			if(mCursor.getColumnIndex("notes") != -1) {
				db.close();
				return true;
			}else {
				CreateColumn(dbHelper);
				return false;
			}
		}catch (Exception Exp){
			//something went wrong. Missing the database? The table?
			Log.e("Cek Column","When checking whether a column exists in the table, an error occurred: " + Exp.getMessage());
			CreateColumn(dbHelper);
			return false;
		}
	}

	public static void CreateColumn(SqlHelper dbHelper) {
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    String sql = "ALTER TABLE tbl_tmporder ADD COLUMN notes TEXT null";

		try {
	      	db.execSQL(sql);
	    } catch (SQLException e) {
	    	Log.e("error = ", e.toString());
	    }
	    db.close(); 
	}
}
