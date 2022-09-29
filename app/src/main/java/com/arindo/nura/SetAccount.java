package com.arindo.nura;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by bmaxard on 06/10/2016.
 */

public class SetAccount {
    public String id="", email="", nama="", country="", alamat="", phonecode="", phone="", device="", tokens="", imgprofile="";

    public void loadAccount(Context con){
        SqlHelper dbHelper = new SqlHelper(con);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            Cursor data = db.rawQuery("SELECT * FROM tbl_account WHERE idnom=1",null);
            if(data.getCount() > 0) {
                data.moveToNext();
                id = data.getString(data.getColumnIndex("csrid"));
                email = data.getString(data.getColumnIndex("email"));
                nama = data.getString(data.getColumnIndex("nama"));
                country = data.getString(data.getColumnIndex("country"));
                alamat = data.getString(data.getColumnIndex("alamat"));
                phonecode = data.getString(data.getColumnIndex("phonecode"));
                phone = data.getString(data.getColumnIndex("phone"));
                device = data.getString(data.getColumnIndex("device"));
                tokens = data.getString(data.getColumnIndex("tokens"));
                imgprofile = data.getString(data.getColumnIndex("imgprofile"));
            }
        }catch(Exception e){
            Log.e("Error Set Account",e.toString());
        }
        db.close();
    }

    public String setid(){ return id; }
    public String setemail(){ return email; }
    public String setnama(){ return nama; }
    public String setcountry(){ return country; }
    public String setalamat(){ return alamat; }
    public String setkdtelp(){ return phonecode; }
    public String settelp(){ return phone; }
    public String setdevice(){ return device; }
    public String settokens(){ return tokens; }
    public String setimgprofile(){ return imgprofile; }
}
