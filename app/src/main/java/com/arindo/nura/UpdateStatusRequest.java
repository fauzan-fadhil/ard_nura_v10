package com.arindo.nura;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by bmaxard on 27/09/2016.
 */
public class UpdateStatusRequest {
    public static boolean updateStatus(Context con, String tiket, String status){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            db.execSQL("UPDATE tbl_request SET status='"+status+"' WHERE tiket='"+tiket+"'");
            db.close();
            return true;
        }catch(Exception e){
            Log.e("Error Update Status",e.toString());
            db.close();
            return false;
        }

    }
}
