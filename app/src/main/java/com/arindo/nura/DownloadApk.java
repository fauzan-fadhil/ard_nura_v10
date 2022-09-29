package com.arindo.nura;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
 * Created by bmaxard on 23/10/2016.
 */

public class DownloadApk {
    private String versi, SERVERADDR, MyJSON, tmDevice;
    private int timeoutdata = 20000;
    private Context context;
    private String filaApk;
    public static String TAG_VERSI = null;
    ProgressDialog pDialog;
    int param = 0;
    String host;

    protected void Download(Context con, int p, String downloadfrom, String url) {
        param = p;
        context = con;
        host = url;
        filaApk = "arindo.apk";
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);
        TAG_VERSI = versi;

        Log.e("host",host);

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.e("TM device",tmDevice);

        try{
            MyConfig config = new MyConfig();
            config.create_folder();
        }catch (Exception e){
            //
        }

        ShowAlertDownload(downloadfrom);
    }

    private void ShowAlertDownload(final String downloadfrom) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_alert);

        TextView title = (TextView) dialog.findViewById(R.id.titledata);
        TextView text = (TextView) dialog.findViewById(R.id.textMsg);
        Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);

        title.setText("DOWNLOAD");
        text.setText("Click \"OK\" to download Arindo PPOB app");
        btnClose.setText("Cancel");
        //btnClose.setVisibility(View.GONE);
        btnDone.setText("OK");

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                /*Intent i = null;
                if(param==1){
                    i = new Intent(context, Setting.class);
                }else {
                    i = new Intent(context, MainActivity.class);
                }

                context.startActivity(i);
                if(context instanceof Activity){((Activity)context).finish(); }*/
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(downloadfrom.equals("2")) { //from server
                    DownloadApp();
                }else if(downloadfrom.equals("1")) { //from place market
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.onlinepayment"));
                    context.startActivity(intent);
                }
            }
        });

        dialog.show();
    }

    private void DownloadApp(){
        SERVERADDR = host;

        TaskDownload MyTask = new TaskDownload();
        MyTask.execute();
    }

    public class TaskDownload extends AsyncTask<String, String, String[]> {
        String[] result = null;
        //Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(context, android.R.style.Theme_Holo_Light_Dialog);
            pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            pDialog.setMessage("Downlod process, please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);

            pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File aplikasi = new File(MyConfig.path()+"/Android/data/com.arindo.nura/file/"+filaApk);
                    if(aplikasi.exists()){
                        aplikasi.delete();
                    }
                    cancel(true);
                    pDialog.dismiss();
                    /*if(param==0) {
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }*/
                }
            });
            pDialog.show();
        }

        protected String[] doInBackground(String... value) {
            try{
                Log.e("download","Start Download");

                try {
                    File file = new File(filaApk);
                    String[] exp = SERVERADDR.split("index.php");
                    String urlapk = exp[0];
                    URL server = new URL(urlapk+"/file/"+ file.getName());

                    HttpURLConnection connection = (HttpURLConnection) server.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setUseCaches(false);
                    connection.addRequestProperty("Accept","image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-shockwave-flash, */*");
                    connection.addRequestProperty("Accept-Language", "en-us,zh-cn;q=0.5");
                    connection.addRequestProperty("Accept-Encoding", "gzip, deflate");

                    connection.connect();
                    //InputStream is = new BufferedInputStream(server.openStream(), 8192);
                    InputStream is = connection.getInputStream();
                    OutputStream os = new FileOutputStream(MyConfig.path()+"/Android/data/com.arindo.nura/file/"+file);

                    int lenghtOfFile = connection.getContentLength();
                    long total = 0;

                    byte[] buffer = new byte[1024];
                    int byteReaded = is.read(buffer);
                    while(byteReaded != -1){
                        total += byteReaded;
                        publishProgress(""+(int)((total*100)/lenghtOfFile));

                        os.write(buffer,0,byteReaded);
                        byteReaded = is.read(buffer);
                    }

                    os.close();
                    result = new String[] {"1"};
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    result = new String[] {"2",""};
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    result = new String[] {"3",""};
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    result = new String[] {"4",""};
                }
            }catch (Exception e) {
                Log.e("Error Exception 1 : ", e.toString());
                result = new String[] {"88",""};
                return result;
            }
            return result;
        }

        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onPostExecute(String[] result) {
            String status = result[0];
            if(status.equals("1")){
                pDialog.dismiss();
                //Toast.makeText(context, "Silahkan install Aplikasi Bruconnect versi terbaru !", Toast.LENGTH_LONG).show();
                install();
            }else if(status.equals("88")){
                pDialog.dismiss();
                Toast.makeText(context, "Failed download! ("+status+")", Toast.LENGTH_LONG).show();
            }else{
                String msg = result[1];
                pDialog.dismiss();
                Toast.makeText(context, msg+" ("+status+")", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void install(){
        //if(context instanceof Activity){((Activity)context).finish(); }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(MyConfig.path()+"/Android/data/com.arindo.nura/file/" + filaApk)), "application/vnd.android.package-archive");
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
