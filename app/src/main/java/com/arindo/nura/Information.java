package com.arindo.nura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by bmaxard on 19/10/2016.
 */

public class Information extends AppCompatActivity {
    private LinearLayout layoutAbout, layoutHelp, layoutContact;
    private TextView ttelp, tfax, tpesan, tversi;
    private Button btnSending, btnAbout;
    private ScrollView scInfo;
    private String tmDevice, versi, SERVERADDR, MyJSON, csrid, email;
    private SqlHelper dbHelper;
    private int act, timeoutdata=20000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_information);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();
        email = csrAccount.setemail();

        Intent main = getIntent();
        act = main.getExtras().getInt("act");

        layoutAbout = (LinearLayout)findViewById(R.id.layoutAbout);
        layoutHelp = (LinearLayout)findViewById(R.id.layoutHelp);
        layoutContact = (LinearLayout)findViewById(R.id.layoutContact);
        scInfo = (ScrollView) findViewById(R.id.scInfo);

        tversi = (TextView) findViewById(R.id.tversi);
        ttelp = (TextView) findViewById(R.id.ttelp);
        tfax = (TextView) findViewById(R.id.tfax);
        btnSending = (Button) findViewById(R.id.btnSending);
        tpesan = (TextView) findViewById(R.id.tpesan);
        btnAbout = (Button) findViewById(R.id.btnAbout);

        if(act==1) {
            this.setTitle("Tentang");
            tversi.setText("Versi "+BuildConfig.VERSION_NAME);
            layoutAbout.setVisibility(View.VISIBLE);
            scInfo.setVisibility(View.GONE);
            layoutHelp.setVisibility(View.GONE);
            layoutContact.setVisibility(View.GONE);

            //RequestInformation("1", csrid, email);
        }else if(act==2){
            this.setTitle("Bantuan");
            layoutAbout.setVisibility(View.GONE);
            scInfo.setVisibility(View.VISIBLE);
            layoutHelp.setVisibility(View.VISIBLE);
            layoutContact.setVisibility(View.GONE);

            //RequestInformation("2", csrid, email);
        }else if(act==3){
            this.setTitle("Kontak");
            layoutAbout.setVisibility(View.GONE);
            scInfo.setVisibility(View.VISIBLE);
            layoutHelp.setVisibility(View.GONE);
            layoutContact.setVisibility(View.VISIBLE);

            RequestInformation("3", csrid, email);
        }

        btnSending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tpesan.getText().toString().trim().length() == 0){
                    //tpesan.setError("Silahkan masukkan pesan");
                    return;
                }
                SentMSG(csrid, email, tpesan.getText().toString().trim());
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isNetworkAvailable()){
                    Toast(2, "Tidak ada koneksi internet !");
                }else{
                    MyConfig config = new MyConfig();
                    String host = config.hostname(dbHelper);
                    WebLink(host+"about");
                }
            }
        });

        btnAbout.setVisibility(View.GONE);
    }

    private void WebLink(String url){
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void RequestInformation(String jinfo, String csrid, String email){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"csrinfo";
        if (isNetworkAvailable() == true) {
            TaskInfo MyTask = new TaskInfo();
            MyTask.execute(jinfo, csrid, email);
        }else {
            Toast(2, "Tidak ada koneksi internet !");
        }
    }

    public class TaskInfo extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(Information.this, android.R.style.Theme_Translucent);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //here we set layout of progress dialog
            dialog.setContentView(R.layout.custom_progress_dialog);
            dialog.setCancelable(true);
            dialog.show();
        }

        protected String[] doInBackground(String... value) {
            try {
                final String jinfo = value[0];
                final String csrid = value[1];
                final String email = value[2];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("jinfo",jinfo));
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("email",email));
                post.add(new BasicNameValuePair("versi",versi));

                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(timeoutdata);
                HttpPost httppost = new HttpPost(SERVERADDR);

                httppost.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                MyJSON = request(response);
                Log.e("JSON",MyJSON);
                JSONObject jobject = new JSONObject(MyJSON);

                String rc = jobject.getString("rc");
                if (rc.equals("1")) {
                    String telp = jobject.getString("telp");
                    String fax = jobject.getString("fax");
                    result = new String[] {"1",telp,fax};
                } else {
                    String msg = jobject.getString("msg");
                    statuskomplit = rc;
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
            if(rc.equals("1")){
                ttelp.setText(result[1]);
                tfax.setText(result[2]);
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"RC:"+rc+" >> Request gagal !");
                dialog.dismiss();
            }else if(rc.equals("99")){
                Toast(1,"RC:"+rc+" >> Request Time Out !");
                dialog.dismiss();
            }else{
                String msg = result[1];
                Toast(1,"RC:"+rc+" >> "+msg);
                dialog.dismiss();
            }
        }
    }

    private void SentMSG(String csrid, String email, String pesan){
        FirebaseApp.initializeApp(getBaseContext());
        String token = FirebaseInstanceId.getInstance().getToken();
        if(token==null){
            Toast(2,"Token Salah");
            return;
        }

        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"csrmsg";
        if (isNetworkAvailable() == true) {
            TaskSentMSG MyTask = new TaskSentMSG();
            MyTask.execute(csrid, email, token, pesan);
        }else {
            Toast(2, "Tidak ada koneksi internet !");
        }
    }

    public class TaskSentMSG extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(Information.this, android.R.style.Theme_Translucent);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //here we set layout of progress dialog
            dialog.setContentView(R.layout.custom_progress_dialog);
            dialog.setCancelable(true);
            dialog.show();
        }

        protected String[] doInBackground(String... value) {
            try {
                final String csrid = value[0];
                final String email = value[1];
                final String token = value[2];
                final String pesan = value[3];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("email",email));
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("pesan",pesan));
                post.add(new BasicNameValuePair("versi",versi));

                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(timeoutdata);
                HttpPost httppost = new HttpPost(SERVERADDR);

                httppost.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                MyJSON = request(response);
                Log.e("JSON",MyJSON);
                JSONObject jobject = new JSONObject(MyJSON);

                String rc = jobject.getString("rc");
                if (rc.equals("1")) {
                    String msg = jobject.getString("msg");
                    result = new String[] {"1",msg};
                } else {
                    String msg = jobject.getString("msg");
                    statuskomplit = rc;
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
            if(rc.equals("1")){
                Toast(0,result[1]);
                tpesan.setText("");
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"RC:"+rc+" >> Request gagal !");
                dialog.dismiss();
                onBackPressed();
            }else if(rc.equals("99")){
                Toast(1,"RC:"+rc+" >> Request Time Out !");
                dialog.dismiss();
                onBackPressed();
            }else{
                String msg = result[1];
                Toast(1,"RC:"+rc+" >> "+msg);
                dialog.dismiss();
                onBackPressed();
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

    private void Toast(int token, String msg) {
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);

        LayoutInflater li = getLayoutInflater();
        View toastAppear = li.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.CustomToastLayout));

        ImageView imageView = (ImageView) toastAppear.findViewById(R.id.imageView);

        switch (token) {
            case 0:
                imageView.setImageResource(R.drawable.notification_success);
                break;
            case 1:
                imageView.setImageResource(R.drawable.notification_warning);
                break;
            case 2:
                imageView.setImageResource(R.drawable.notification_error);
                break;
            default:
                break;
        }

        TextView text = (TextView) toastAppear.findViewById(R.id.textView);
        text.setText(msg);

        toast.setView(toastAppear);
        toast.show();
    }

    private void ShowAlert(final int param, String msg) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_alert);
        //dialog.setTitle("Custom Dialog");

        TextView title = (TextView) dialog.findViewById(R.id.titledata);
        TextView text = (TextView) dialog.findViewById(R.id.textMsg);
        Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);

        if (param == 0) {
            title.setText("INFORMASI");
            text.setText(msg);
            btnClose.setVisibility(View.GONE);
            btnDone.setText("OK");

            // if decline button is clicked, close the custom dialog
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                    onBackPressed();
                }
            });
        }
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
