package com.arindo.nura;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by bmaxard on 18/02/2017.
 */

public class Splash extends AppCompatActivity {
    public  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    private static int SPLASH_TIME_OUT = 2000;
    String[] PERMISSIONS;
    Activity contect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        contect = this;
        PERMISSIONS = new String[]{
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.CALL_PHONE
        };

        CekPermission();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void CekPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            // write your logic here
            MyConfig config = new MyConfig();
            if(config.create_folder()!="1"){
                Toast.makeText(this,"Please restart youre device. ("+config.create_folder()+")",Toast.LENGTH_LONG).show();
                return;
            }else{
                ShowActivity();
            }
        }
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) +
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.READ_EXTERNAL_STORAGE) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.ACCESS_NETWORK_STATE) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.ACCESS_FINE_LOCATION) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.READ_PHONE_STATE) +
            ActivityCompat.checkSelfPermission (this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.CAMERA) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.INTERNET) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.ACCESS_NETWORK_STATE) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.READ_PHONE_STATE) ||
                ActivityCompat.shouldShowRequestPermissionRationale (this, android.Manifest.permission.CALL_PHONE) ){

                SnackBarMsg("Arindo Kurir require some permissions, please enabled.");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSIONS_MULTIPLE_REQUEST);
                }
            }
        }else{
            ShowActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean permission0 = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean permission1 = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean permission2 = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean permission3 = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean permission4 = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean permission5 = grantResults[5] == PackageManager.PERMISSION_GRANTED;
                    boolean permission6 = grantResults[6] == PackageManager.PERMISSION_GRANTED;
                    boolean permission7 = grantResults[7] == PackageManager.PERMISSION_GRANTED;
                    boolean permission8 = grantResults[8] == PackageManager.PERMISSION_GRANTED;

                    if(permission0 && permission1 && permission2 && permission3 && permission4 && permission5 && permission6 && permission7 && permission8)
                    {
                        ShowActivity();
                    }else{
                        SnackBarMsg("Arindo Kurir require some permissions, please enabled.");
                    }
                }
                else {
                    SnackBarMsg("Arindo Kurir require some permissions, please enabled.");
                }
                break;
        }
    }

    private void ShowActivity(){
        new Handler().postDelayed(new Runnable() {
			/*
             * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                if(isNetworkAvailable()== true) {
                    CheckNewAppVersion cekapp = new CheckNewAppVersion();
                    cekapp.CheckVersion(contect,0);
                }else{
                    Toast.makeText(contect,"Please turn on your internet connection device !",Toast.LENGTH_LONG).show();
                    Intent i = new Intent(Splash.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void SnackBarMsg(String msg){
        final Snackbar snackBar = Snackbar.make(this.findViewById(android.R.id.content), msg, Snackbar.LENGTH_INDEFINITE);
        View snackbarView = snackBar.getView();
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        snackBar.setAction("Enable", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(PERMISSIONS, PERMISSIONS_MULTIPLE_REQUEST);
                }
            }
        });
        snackBar.show();
    }
}
