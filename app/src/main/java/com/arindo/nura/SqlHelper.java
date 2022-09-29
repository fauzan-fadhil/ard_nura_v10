package com.arindo.nura;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SqlHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "store";
    public static final String path = MyConfig.path() + "/Android/data/com.arindo.nura/journal/";
    public Context myContext;

    @Override
    public void onCreate(SQLiteDatabase db) {
		try {
    		copyDataBase();
		} catch (IOException e) {
			throw new Error("Error copying database");
    	}
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }

    public SqlHelper(Context context) {
        super(context, path + DATABASE_NAME, null, DATABASE_VERSION);
        myContext=context;

        File dbfile = new File(path+DATABASE_NAME);
        if(dbfile.exists()){
            if(DataBaseisExist()){
                //tidk melakukan apapun, db telah ada
            }
            else{
                this.getReadableDatabase();

                try {
                    copyDataBase();
                } catch (IOException e) {
                    throw new Error("Error copying database");
                }
            }
        }else{
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }


    public void createDataBase() throws IOException{
        if(DataBaseisExist()){
            //tidk melakukan apapun, db telah ada
        }
        else{
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean DataBaseisExist(){
        SQLiteDatabase checkDB = null;
        try{
            String myPath = path + DATABASE_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){
            //database tidak ada
        }
        if(checkDB != null){
            checkDB.close();
        }
        if(checkDB != null )return true ;else return false;
    }

    private void copyDataBase() throws IOException{
        InputStream myInput = myContext.getAssets().open(DATABASE_NAME);

        String outFileName = path + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }
}
