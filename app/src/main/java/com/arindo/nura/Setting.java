package com.arindo.nura;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by bmaxard on 18/10/2016.
 */

public class Setting extends AppCompatActivity {
    private TableRow tRow1, tRow2, tRow3;
    private String imgprofile;
    public static ImageView imgUser;
    public static TextView title;
    private Toolbar toolbar;
    private Snackbar snackBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_setting);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.setTitle("Setting");

        tRow1 = (TableRow)findViewById(R.id.tRow1);
        tRow2 = (TableRow)findViewById(R.id.tRow2);
        tRow3 = (TableRow)findViewById(R.id.tRow3);
        imgUser = (ImageView)findViewById(R.id.imgUser);
        title = (TextView)findViewById(R.id.title);

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        imgprofile = csrAccount.setimgprofile();
        imgUser.setImageURI(Uri.parse(MyConfig.path()+"/Android/data/com.arindo.bruconnect/file/"+imgprofile));
        title.setText(csrAccount.setnama());

        tRow1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bd = new Bundle();
                bd.putInt("act", 1);
                Intent i = new Intent(Setting.this, Account.class);
                i.putExtras(bd);
                startActivity(i);
            }
        });

        tRow2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bd = new Bundle();
                bd.putInt("act", 2);
                Intent i = new Intent(Setting.this, Account.class);
                i.putExtras(bd);
                startActivity(i);
            }
        });

        tRow3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable() == true) {
                    //DownloadApp();
                    CheckNewAppVersion cekapp = new CheckNewAppVersion();
                    cekapp.CheckVersion(Setting.this, 1);
                }else{
                    SnackBarMsg("Silahkan nyalakan koneksi internet pada perangkat anda !");
                }
            }
        });

        imgUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetAccount oprAccount = new SetAccount();
                oprAccount.loadAccount(Setting.this);
                imgprofile = oprAccount.setimgprofile();

                Bundle bd = new Bundle();
                bd.putString("filePath", MyConfig.path()+"/Android/data/com.arindo.bruconnect/file/"+imgprofile);
                Intent i = new Intent(Setting.this, ZoomImage.class);
                i.putExtras(bd);
                startActivity(i);
            }
        });
    }

    public static void refreshImage(String path){
        imgUser.setImageURI(null);
        imgUser.setImageURI(Uri.parse(path));
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void SnackBarMsg(String msg){
        //final Snackbar snackBar = Snackbar.make(toolbar, msg, Snackbar.LENGTH_INDEFINITE);
        snackBar = Snackbar.make(toolbar, msg, Snackbar.LENGTH_INDEFINITE);
        snackBar.setActionTextColor(getResources().getColor(R.color.colorPrimary));

        View snackbarView = snackBar.getView();
        //snackbarView.setBackgroundColor(Color.WHITE);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        snackBar.setAction("Keluar", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
    }
}
