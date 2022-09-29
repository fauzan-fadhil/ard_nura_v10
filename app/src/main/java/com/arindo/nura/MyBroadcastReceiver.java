package com.arindo.nura;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by bmaxard on 19/03/2017.
 */

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent)
    {
        File file = new File(Environment.getExternalStorageDirectory(), "/Download/app-release.apk");
        file.setReadable(true, false);
        Intent promptInstall = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        promptInstall.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        promptInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(promptInstall);
        //CheckNewAppVersion.requestapk = null;
    }
}
