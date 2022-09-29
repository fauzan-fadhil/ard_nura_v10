package com.arindo.nura;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by bmaxard on 06/10/2016.
 */

public class GetTmpOrder {
    public int qty = 0;
    public int tqty = 0;
    public String notes = null;
    public boolean orderaktif = true;
    public Double tammount= 0.0;
    public void InsertTempOrder(Context con, String plicesid, String groupid, String itemid, String item, String qty, String price, String ammount){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            if(db.rawQuery("SELECT * FROM tbl_tmporder WHERE itemid='"+itemid+"'",null).getCount() == 0) {
                ContentValues values;
                values = new ContentValues();
                values.put("placesid", plicesid);
                values.put("groupid", groupid);
                values.put("itemid", itemid);
                values.put("item", item);
                values.put("qty", qty);
                values.put("price", price);
                values.put("ammount", ammount);
                db.insert("tbl_tmporder", null, values);
            }else{
                ContentValues values;
                values = new ContentValues();
                values.put("placesid", plicesid);
                values.put("groupid", groupid);
                values.put("item", item);
                values.put("qty", qty);
                values.put("price", price);
                values.put("ammount", ammount);
                db.update("tbl_tmporder", values, "itemid='"+itemid+"'", null);
            }
            db.close();
        }catch(Exception e){
            Log.e("Error Save Temp Order",e.toString());
            db.close();
        }
    }

    public void InsertTempOrderNotes(Context con, String itemid, String notes){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            if(db.rawQuery("SELECT * FROM tbl_tmporder WHERE itemid='"+itemid+"'",null).getCount() == 0) {
                ContentValues values;
                values = new ContentValues();
                values.put("itemid", itemid);
                values.put("notes", notes);
                db.insert("tbl_tmporder", null, values);
            }else{
                ContentValues values;
                values = new ContentValues();
                values.put("notes", notes);
                db.update("tbl_tmporder", values, "itemid='"+itemid+"'", null);
            }
            db.close();
        }catch(Exception e){
            Log.e("Error Temp Order Notes",e.toString());
            db.close();
        }
    }

    public void DeleteTempItem(Context con, String itemid){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try{
            db.execSQL("DELETE FROM tbl_tmporder WHERE itemid='"+itemid+"'");
        }catch(Exception e){
            Log.e("Error Set Tmp Order",e.toString());
        }
        db.close();
    }

    public void SelectTempQty(Context con, String itemid){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            Cursor data = db.rawQuery("SELECT COALESCE(qty,0) qty FROM tbl_tmporder WHERE itemid='"+itemid+"' ",null);
            if(data.getCount() > 0) {
                data.moveToNext();
                qty = data.getInt(data.getColumnIndex("qty"));
            }
        }catch(Exception e){
            Log.e("Error Set Tmp Order",e.toString());
        }
        db.close();
    }

    public int setQty(){ return qty; }

    public void SelectTempTotal(Context con, String id, int param){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String sql = "";
            if(param==0) {
                sql = "SELECT placesid, COALESCE(sum(qty),0) qty, COALESCE(sum(ammount),0) ammount FROM tbl_tmporder WHERE placesid IS NOT NULL";
            }else if(param==1) {
                sql = "SELECT COALESCE(sum(qty),0) qty, COALESCE(sum(ammount),0) ammount FROM tbl_tmporder WHERE placesid='"+id+"'";
            }else{
                sql = "SELECT COALESCE(sum(qty),0) qty, COALESCE(sum(ammount),0) ammount FROM tbl_tmporder WHERE groupid='"+id+"'";
            }
            Cursor data = db.rawQuery(sql,null);
            if(data.getCount() > 0) {
                data.moveToNext();
                if(param==0){
                    tqty = data.getInt(data.getColumnIndex("qty"));
                    String placesid = data.getString(data.getColumnIndex("placesid"));
                    if((tqty > 0) && (!placesid.equals(id))){
                        orderaktif = false;
                    }
                }else {
                    tqty = data.getInt(data.getColumnIndex("qty"));
                    tammount = data.getDouble(data.getColumnIndex("ammount"));
                }
            }
        }catch(Exception e){
            Log.e("Error Set Tmp Order",e.toString());
        }
        db.close();
    }

    public Integer setTotalQty(){ return tqty; }
    public Double setTotalAmmount(){ return tammount; }
    public boolean setCekOrder(){ return orderaktif; }

    public void DeleteTempOrder(Context con, String id){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try{
            //db.execSQL("DELETE FROM tbl_tmporder WHERE placesid='"+id+"'");
            db.execSQL("DELETE FROM tbl_tmporder");
        }catch(Exception e){
            Log.e("Error Delete Tmp Order",e.toString());
        }
        db.close();
    }

    public void SelectTempNote(Context con, String itemid){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            Cursor data = db.rawQuery("SELECT notes FROM tbl_tmporder WHERE itemid='"+itemid+"' ",null);
            if(data.getCount() > 0) {
                data.moveToNext();
                notes = data.getString(data.getColumnIndex("notes"));
            }
        }catch(Exception e){
            Log.e("Error Set Tmp Notes",e.toString());
        }
        db.close();
    }

    public String setNotes(){ return notes; }
}
