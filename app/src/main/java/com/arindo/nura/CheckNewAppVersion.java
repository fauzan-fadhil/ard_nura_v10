package com.arindo.nura;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Created by bmaxard on 23/10/2016.
 */

public class CheckNewAppVersion {
    private String versi, SERVERADDR, MyJSON, tmDevice;
    private int timeoutdata = 20000;
    private Context context;
    private String filaApk;
    public static String TAG_VERSI = null;
    ProgressDialog pDialog;
    int param = 0;
    //String host = "http://118.97.191.109:3111/index.php?";
    //String host = "http://bruconnect.com/apibrucon/3111/index.php?";
    //String host = "http://bruconnect.com/apipaycon/3111/index.php?";
    //String host = "http://202.138.233.235:8081/apipaycon/3111/index.php?";
    //String host = "http://backend.nura-sr.com/apipaycon/3111/index.php?";
    String host = "http://backend.waserbanura.com/apipaycon/3111/index.php?";
    // String host ="http://bruconnect.com/apibrucon/3111/index.php?";
    protected void CheckVersion(Context con, int p) {
        param = p;
        context = con;
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);
        TAG_VERSI = versi;

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

        SERVERADDR = host+"csrcekversi";
        TaskCheck MyTask = new TaskCheck();
        MyTask.execute();
    }

    public class TaskCheck extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_progress_dialog);
            dialog.setCancelable(true);
            dialog.show();
        }

        protected String[] doInBackground(String... value) {
            try {
                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("versi", versi));
                post.add(new BasicNameValuePair("imei", tmDevice));

                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(timeoutdata);
                HttpPost httppost = new HttpPost(SERVERADDR);

                httppost.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                MyJSON = request(response);
                Log.e("JSON", MyJSON);
                JSONObject jobject = new JSONObject(MyJSON);

                String rc = jobject.getString("rc");
                if (rc.equals("1")) {
                    filaApk = jobject.getString("filename");
                    String version = jobject.getString("versionnow");
                    String msg = jobject.getString("msg");
                    result = new String[]{"1", msg, version};
                } else {
                    String msg = jobject.getString("msg");
                    result = new String[]{rc, msg};
                }
            } catch (ConnectTimeoutException ae) {
                Log.e("Request Time Out : ", ae.toString());
                result = new String[]{"99"};
                return result;
            } catch (IOException e) {
                Log.e("Error Exception 2 : ", e.toString());
                result = new String[]{"101"};                // HTTP Refused -> hostname / ip addr not found
                return result;
            } catch (Exception e) {
                Log.e("Error Exception 1 : ", e.toString());
                result = new String[]{"88"};                // error String JSON atau webservice atau lainnya
                return result;
            }
            return result;
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            //jmlexport = values[0];
            //textstatusprogress.setText("Export data ("+jmlexport+" dari "+jmlData+")");
        }

        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onPostExecute(String[] result) {
            String rc = result[0];
            if (rc.equals("1")) {
                TAG_VERSI = result[2];
                ShowAlertDownload(result[1]);
                dialog.dismiss();
            } else if (rc.equals("88") || rc.equals("101")) {
                //Toast.makeText(context, "RC:"+rc+" >> Version not found !", Toast.LENGTH_LONG).show();
                Toast.makeText(context, "RC:"+rc, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else if (rc.equals("99")) {
                //Toast.makeText(context, "RC:"+rc+" >> Request Time Out !", Toast.LENGTH_LONG).show();
                Toast.makeText(context, "RC:" + rc, Toast.LENGTH_LONG).show();
                dialog.dismiss();
            } else {
                if(rc.equals("02")){
                    //Tidak ada versi terbaru
                    String msg = result[1];
                    dialog.dismiss();
                    if(param==1){
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                    }

                    Intent i = null;
                    if(param==1){
                        i = new Intent(context, Setting.class);
                    }else {
                        i = new Intent(context, LoginActivity.class);
                    }
                    context.startActivity(i);
                    if(context instanceof Activity){((Activity)context).finish(); }
                }else {
                    String msg = result[1];
                    //Toast.makeText(context, "RC:"+rc+" >> "+msg, Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "RC:" + rc +" >> "+msg, Toast.LENGTH_LONG).show();
                }
                dialog.dismiss();
            }
        }
    }

    public static String request(HttpResponse response) {
        String result = "";
        try {
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;

            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        } catch (Exception ex) {
            result = "Error";
        }
        return result;
    }

    private void ShowAlertDownload(final String msg) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_alert);

        TextView title = (TextView) dialog.findViewById(R.id.titledata);
        TextView text = (TextView) dialog.findViewById(R.id.textMsg);
        Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);

        title.setText("INFORMATION");
        text.setText(msg);
        btnClose.setText("Cancel");
        //btnClose.setVisibility(View.GONE);
        btnDone.setText("OK");

        // if decline button is clicked, close the custom dialog
        btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                // Close dialog
                    dialog.dismiss();
                    Intent i = null;
                    if(param==1){
                        i = new Intent(context, Setting.class);
                    }else {
                        i = new Intent(context, LoginActivity.class);
                    }
                    context.startActivity(i);
                    if(context instanceof Activity){((Activity)context).finish(); }
                }
            });

        // if decline button is clicked, close the custom dialog
        btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                DownloadApp();

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
            pDialog.setMessage("The process of downloading a new version, please wait...");
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
                    if(param==0) {
                        if (context instanceof Activity) {
                            ((Activity) context).finish();
                        }
                    }
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
                    URL server = new URL(urlapk+"/file/CSR/"+ file.getName());

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
                Toast.makeText(context, "RC:"+status+" -> Failed download new version !", Toast.LENGTH_LONG).show();
            }else{
                String msg = result[1];
                pDialog.dismiss();
                Toast.makeText(context, "RC:"+status+" -> Failed download new version !\n"+msg, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void install(){
        if(context instanceof Activity){((Activity)context).finish(); }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(MyConfig.path()+"/Android/data/com.arindo.nura/file/" + filaApk)), "application/vnd.android.package-archive");
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
