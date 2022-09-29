package com.arindo.nura;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by bmaxard on 09/09/2016.
 */
public class MyConfig {
    public static String result = null;

    public CharSequence create_folder(){
        try {
            Boolean success = true;
            File folder = new File(path() + "/Android/data/com.arindo.nura");
            if (folder.exists()) {
                File folderData = new File(path() + "/Android/data/com.arindo.nura/journal");
                if (!folderData.exists()) {folderData.mkdir();}
                File folderFile = new File(path()+"/Android/data/com.arindo.nura/file");
                if (!folderFile.exists()) {folderFile.mkdir();}

                result = "1";
            }else{
                success = folder.mkdirs();
                Log.e("create","folder");

                if(success) {
                    File folderData = new File(path() + "/Android/data/com.arindo.nura/journal");
                    if (!folderData.exists()) {
                        folderData.mkdir();
                    }
                    File folderFile = new File(path() + "/Android/data/com.arindo.nura/file");
                    if (!folderFile.exists()) {
                        folderFile.mkdir();
                    }

                    result = "1";
                }else{
                    result = "77"; // gagal create folder
                }

            }
        } catch (Exception e) {
            result = "66"; // code error
        }

        return result;
    }

    public static String path(){
        File sdCardRoot = Environment.getExternalStorageDirectory();
        String inpath = sdCardRoot.toString();
        return inpath;
    }

    public static String hostname(SqlHelper dbHelper){
        String ip = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try{
            Cursor selectIP = db.rawQuery("SELECT * FROM tbl_setting WHERE idnom=1",null);
            if(selectIP.getCount() > 0) {
                selectIP.moveToFirst();
                String host = selectIP.getString(selectIP.getColumnIndex("ipaddress"));
                String port = selectIP.getString(selectIP.getColumnIndex("port"));
                //ip = "http://"+host+":"+port+"/index.php?";
                //ip = "http://192.168.189.1/apiardbruconnect/index.php?"; //this is local ip
                //ip = "http://118.97.191.109:3111/index.php?";
                //ip = "http://bruconnect.com/apipaycon/3111/index.php?";
                //ip = "http://202.138.233.232/apipaycon/3111/index.php?";
                //ip = "http://202.138.233.235:8081/apipaycon/3111/index.php?";
                //ip = "http://backend.nura-sr.com/apipaycon/3111/index.php?";
                ip = "http://backend.waserbanura.com/apipaycon/3111/index.php?";
              //  ip ="http://bruconnect.com/apibrucon/3111/index.php?";
            }
        }catch(Exception e){
            Log.e("Error Load IP",e.toString());
        }
        db.close();
        return ip;
    }
}