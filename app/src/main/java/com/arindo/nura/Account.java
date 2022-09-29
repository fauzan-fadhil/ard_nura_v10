package com.arindo.nura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.regex.Pattern;

/**
 * Created by bmaxard on 18/10/2016.
 */

public class Account extends AppCompatActivity {
    private EditText tnama, talamat, tkdtelp, ttelp, temail, tpassnew, tpassexp, tpassconf;
    private String csrid, nama, alamat, kdtelp, telp, email;
    private String SERVERADDR, MyJSON;
    private ImageView editnama, editalamat, edittelp, editemail;
    private String eCeklistNama = "edit", eCeklistAlamat = "edit", eCeklistTelp = "edit", eCeklistEmail = "edit";
    private String tmDevice, versi;
    private SqlHelper dbHelper;
    private int timeoutdata = 20000, act;
    private LinearLayout layoutAccount, layoutChangePassword;
    private Button btnChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.setTitle("Akun");

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        ///tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        tnama = (EditText) findViewById(R.id.tnama);
        talamat = (EditText) findViewById(R.id.talamat);
        tkdtelp = (EditText) findViewById(R.id.tkdtelp);
        ttelp = (EditText) findViewById(R.id.ttelp);
        temail = (EditText) findViewById(R.id.temail);

        editnama = (ImageView) findViewById(R.id.imgnama);
        editalamat = (ImageView) findViewById(R.id.imgalamat);
        edittelp = (ImageView) findViewById(R.id.imgtelp);
        editemail = (ImageView) findViewById(R.id.imgemail);

        layoutAccount = (LinearLayout) findViewById(R.id.layoutAccount);
        layoutChangePassword = (LinearLayout) findViewById(R.id.layoutChangePassword);
        btnChange = (Button) findViewById(R.id.btnChange);
        tpassexp = (EditText) findViewById(R.id.tpassexp);
        tpassnew = (EditText) findViewById(R.id.tpassnew);
        tpassconf = (EditText) findViewById(R.id.tpassconf);

        Intent main = getIntent();
        act = main.getExtras().getInt("act");

        if(act==1) {
            this.setTitle("Akun");
            layoutAccount.setVisibility(View.VISIBLE);
            layoutChangePassword.setVisibility(View.GONE);
            StartAccount();
        }else if(act==2){
            this.setTitle("Ubah Sandi");
            layoutAccount.setVisibility(View.GONE);
            layoutChangePassword.setVisibility(View.VISIBLE);
            StartPassword();
        }

        editnama.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eCeklistNama=="edit") {
                    enableEditText(tnama);
                    editnama.setImageResource(R.drawable.ic_checklist);
                    eCeklistNama="update";
                }else{
                    if(tnama.getText().toString().equals("")){
                        tnama.setError("Masukan nama lengkap anda");
                        return;
                    }

                    if(tnama.getText().toString().length() < 3){
                        tnama.setError("Nama minimal 3 (tiga) karakter");
                        return;
                    }


                    SendUpdateAccount(csrid, email, "1", tnama.getText().toString().trim());


                    eCeklistNama="edit";
                    disableEditText(tnama);
                    editnama.setImageResource(R.drawable.ic_edit);
                }
            }
        });

        editalamat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eCeklistAlamat=="edit") {
                    enableEditText(talamat);
                    editalamat.setImageResource(R.drawable.ic_checklist);
                    eCeklistAlamat="update";
                }else{
                    if(talamat.getText().toString().equals("")){
                        talamat.setError("Masukan alamat anda");
                        return;
                    }

                    SendUpdateAccount(csrid, email, "2", talamat.getText().toString().trim());

                    eCeklistAlamat="edit";
                    disableEditText(talamat);
                    editalamat.setImageResource(R.drawable.ic_edit);
                }
            }
        });

        edittelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eCeklistTelp=="edit") {
                    enableEditText(tkdtelp);
                    enableEditText(ttelp);
                    edittelp.setImageResource(R.drawable.ic_checklist);
                    eCeklistTelp="update";
                }else{
                    if(tkdtelp.getText().toString().equals("")){
                        tkdtelp.setError("Masukan kode area anda");
                        return;
                    }
                    if(ttelp.getText().toString().equals("")){
                        ttelp.setError("Masukan nomor telepon anda");
                        return;
                    }
                    String telp = tkdtelp.getText().toString().trim() + "-" + ttelp.getText().toString().trim();
                    SendUpdateAccount(csrid, email, "3", telp);

                    eCeklistTelp="edit";
                    disableEditText(tkdtelp);
                    disableEditText(ttelp);
                    edittelp.setImageResource(R.drawable.ic_edit);
                }
            }
        });

        editemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(eCeklistEmail=="edit") {
                    enableEditText(temail);
                    editemail.setImageResource(R.drawable.ic_checklist);
                    eCeklistEmail="update";
                }else{
                    if(!isValidEmaillId(temail.getText().toString().trim())){
                        temail.setError("Masukan email anda dengan benar");
                        return;
                    }

                    if(!temail.getText().toString().trim().equals(email.trim())) {
                        SendUpdateAccount(csrid, email, "4", temail.getText().toString().trim());
                    }

                    eCeklistEmail="edit";
                    disableEditText(temail);
                    editemail.setImageResource(R.drawable.ic_edit);
                }
            }
        });

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tpassexp.getText().toString().equals("")){
                    tpassexp.setError("Masukan sandi lama anda");
                    return;
                }

                if(tpassnew.getText().toString().equals("")){
                    tpassnew.setError("Masukan sandi baru anda");
                    return;
                }

                if(tpassnew.getText().toString().length() < 4){
                    tpassnew.setError("Sandi minimal 4 (empat) karakter");
                    return;
                }

                if(!tpassconf.getText().toString().equals(tpassnew.getText().toString())){
                    tpassconf.setError("Konfirmasi sandi tidak sesuai");
                    return;
                }

                SendUpdatePassword(csrid, email, tpassexp.getText().toString(), tpassnew.getText().toString());
            }
        });
    }

    private boolean isValidEmaillId(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    private void StartAccount(){
        eCeklistNama = "edit";
        eCeklistAlamat = "edit";
        eCeklistTelp = "edit";
        eCeklistEmail = "edit";
        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();
        nama = csrAccount.setnama();
        alamat = csrAccount.setalamat();
        kdtelp = csrAccount.setkdtelp();
        telp = csrAccount.settelp();
        email = csrAccount.setemail();

        tnama.setText(nama);
        talamat.setText(alamat);
        tkdtelp.setText(kdtelp);
        ttelp.setText(telp);
        temail.setText(email);

        disableEditText(tnama);
        disableEditText(talamat);
        disableEditText(tkdtelp);
        disableEditText(ttelp);
        disableEditText(temail);
    }

    private void StartPassword(){
        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();
        email = csrAccount.setemail();
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        //editText.setCursorVisible(false);
        //editText.setEnabled(false);
        //editText.setKeyListener(null);
        //editText.setBackgroundColor(Color.TRANSPARENT);
    }

    private void enableEditText(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        Editable etext = editText.getText();
        Selection.setSelection(etext, etext.length());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void SendUpdateAccount(String csrid, String email, String field, String value){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"editaccountcsr";
        if (isNetworkAvailable() == true) {
            TaskUpdateAccount MyTask = new TaskUpdateAccount();
            MyTask.execute(csrid, email, field, value);
        }else {
            Toast(2, "Tidak ada koneksi internet !");
        }
    }

    public class TaskUpdateAccount extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(Account.this, android.R.style.Theme_Translucent);
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
                final String field = value[2];
                final String val = value[3];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("email",email));
                post.add(new BasicNameValuePair("field",field));
                post.add(new BasicNameValuePair("value",val));
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
                    result = new String[] {"1", field, val};
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
                UpdateAccount(result[1], result[2]);
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"Request failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                Toast(1,"Request Time Out ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                Toast(1,msg+"("+rc+")");
                dialog.dismiss();
            }
        }
    }

    private void UpdateAccount(String field, String value){
        String sql = null;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            if(field.equals("1")){
                sql = "UPDATE tbl_account SET nama='"+value+"' WHERE idnom=1";
                Setting.title.setText(value);
                MainActivity.tnamemain.setText(value);
            }
            if(field.equals("2")){sql = "UPDATE tbl_account SET alamat='"+value+"' WHERE idnom=1";}
            if(field.equals("3")){
                String val[] = value.split("-");
                sql = "UPDATE tbl_account SET phonecode='"+val[0]+"', phone='"+val[1]+"' WHERE idnom=1";
            }
            if(field.equals("4")){sql = "UPDATE tbl_account SET email='"+value+"' WHERE idnom=1";}
            db.execSQL(sql);

            StartAccount();
        }catch (Exception e){
            e.printStackTrace();
        }
        db.close();
    }

    private void SendUpdatePassword(String csrid, String email, String passexp, String passnew){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"editpasscsr";
        if (isNetworkAvailable() == true) {
            TaskUpdatePassword MyTask = new TaskUpdatePassword();
            MyTask.execute(csrid, email, passexp, passnew);
        }else {
            Toast(2, "Tidak ada koneksi internet !");
        }
    }

    public class TaskUpdatePassword extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(Account.this, android.R.style.Theme_Translucent);
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
                final String passexp = value[2];
                final String passnew = value[3];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("email",email));
                post.add(new BasicNameValuePair("passexp",passexp));
                post.add(new BasicNameValuePair("passnew",passnew));
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
                    result = new String[] {"1"};
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
                ShowAlert(0);
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2, "Request failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                Toast(1, "Request Time Out ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                Toast(1,msg+"("+rc+")");
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

    private void ShowAlert(final int param) {
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
            text.setText("Sandi berhasil diubah. Silahkan keluar aplikasi dan login ulang.");
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
