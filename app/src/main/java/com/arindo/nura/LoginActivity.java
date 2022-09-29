package com.arindo.nura;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by bmaxard on 03/10/2016.
 */

public class LoginActivity extends AppCompatActivity {
    private Button btnLogin, btnReg;
    private EditText temail, tpwd;
    private LinearLayout loginbox;
    private SqlHelper dbHelper;
    private String tmDevice, versi, SERVERADDR, MyJSON;
    private int timeoutdata = 20000;
    private Md5Hex MD5;
    private CheckBox showPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //try{
           // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
           // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.e("TM device",tmDevice);
        //}catch (Exception e){
        //    finish();
        //    return;
        //}

        MD5 = new Md5Hex();
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        temail = (EditText)findViewById(R.id.email);
        tpwd = (EditText)findViewById(R.id.password);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnReg = (Button)findViewById(R.id.btnReg);
        loginbox = (LinearLayout)findViewById(R.id.loginbox);
        showPassword = (CheckBox) findViewById(R.id.showPassword);

        cekConfig();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(temail.getText().toString().equals("")){
                    temail.setError("Email not empty");
                    return;
                }
                if(tpwd.getText().toString().equals("")){
                    tpwd.setError("Password not empty");
                    return;
                }
                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host+"logincsr";
                if (isNetworkAvailable() == true) {
                    String email = temail.getText().toString();
                    String pwd = tpwd.getText().toString();
                    TaskLogin MyTask = new TaskLogin();
                    MyTask.execute(email, pwd);
                } else {
                    Toast(2, "Internet not connected !");
                }
                /*Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
                finish();*/
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, Registration.class);
                startActivity(i);
                finish();

            }
        });

        tpwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!showPassword.isChecked()) {
                        tpwd.setInputType(129);
                    } else {
                        tpwd.setInputType(128);
                    }
                }
                return false;
            }
        });

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    tpwd.setInputType(129);
                } else {
                    tpwd.setInputType(128);
                }
            }
        });
    }

    private void cekConfig(){
        try{
            MyConfig config = new MyConfig();

            if(config.create_folder()=="1"){
                SetAccount csrAccount = new SetAccount();
                csrAccount.loadAccount(this);
                temail.setText(csrAccount.setemail());
                dbHelper = new SqlHelper(this);

                try{
                    SqlHelper dbHelper = new SqlHelper(this);
                    boolean isColumNotes = SqlModify.CekColumn(dbHelper);
                    if(isColumNotes==true){}else{}
                }catch(Exception e){Log.e("error column notes ", e.toString());}
            }else{
                loginbox.setVisibility(View.VISIBLE);
                btnLogin.setEnabled(false);
                btnReg.setEnabled(false);
                Toast(2, "RC:"+ config.create_folder() +" >> Please restart youre device");
                return;
            }

            //Delete file apk bruconnect
            String apkpath = MyConfig.path()+"/Android/data/com.arindo.nura/file";
            File f = new File(apkpath);
            if(f.exists()) {
                File file[] = f.listFiles();
                for (File f1 : file) {
                    if (f1.isFile() && f1.getPath().endsWith(".apk")) {
                        f1.delete();
                    }
                }
            }
            //End Delete file apk bruconnect

            if(ceklogin()==false) {
                loginbox.setVisibility(View.VISIBLE);
            }else{
                Bundle bd = new Bundle();
                bd.putInt("indexlist", 0);
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtras(bd);
                startActivity(i);
                finish();
            }
        }catch(Exception e){
            e.printStackTrace();
            Log.e("Error Config",e.toString());
        }
    }

    private boolean ceklogin(){
        boolean status = false;
        try{
            SetAccount csrAccount = new SetAccount();
            csrAccount.loadAccount(this);
            if(csrAccount.setdevice().toString().equals(MD5.Md5Hex(tmDevice))){
                status = true;
            }else{
                status = false;
            }
        }catch(Exception e){
            Log.e("Error Login Status",e.toString());
        }
        return status;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public class TaskLogin extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        String refreshToken = null;
        final Dialog dialog = new Dialog(LoginActivity.this, android.R.style.Theme_Translucent);
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
                String email = value[0];
                String pwd = value[1];
                //tambahan versi 10 os//
                FirebaseApp.initializeApp(getBaseContext());
                refreshToken = FirebaseInstanceId.getInstance().getToken();
                if(refreshToken==null){
                    result = new String[]{"2", "Token failed"};
                    return result;
                }

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",refreshToken));
                post.add(new BasicNameValuePair("email",email));
                post.add(new BasicNameValuePair("userpwd",pwd));
                post.add(new BasicNameValuePair("device",tmDevice));
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
                    String csrid = jobject.getString("csrid");
                    String nama = jobject.getString("nama");
                    String alamat = jobject.getString("alamat");
                    String kdtelp = jobject.getString("kdtelp");
                    String telp = jobject.getString("telp");
                    result = new String[]{"1", csrid, email, nama, alamat, kdtelp, telp};
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
                String csrid = result[1];
                String email = result[2];
                String nama = result[3];
                String alamat = result[4];
                String kdtelp = result[5];
                String telp = result[6];
                saveRegistration(csrid, email, nama, alamat, kdtelp, telp);
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"RC:"+rc+" >> Login failed !");
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

    private void saveRegistration(String csrid, String email, String nama, String alamat, String kdtelp, String telp){
        String device = MD5.Md5Hex(tmDevice);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try{
            if(db.rawQuery("SELECT * FROM tbl_account WHERE idnom=1",null).getCount() == 0) {
                ContentValues values;
                values = new ContentValues();
                values.put("csrid", csrid);
                values.put("email", email);
                values.put("nama", nama);
                values.put("alamat", alamat);
                values.put("phonecode", kdtelp);
                values.put("phone", telp);
                values.put("device", device);
                db.insert("tbl_account", null, values);
            }else{

                ContentValues values;
                values = new ContentValues();
                values.put("csrid", csrid);
                values.put("email", email);
                values.put("nama", nama);
                values.put("alamat", alamat);
                values.put("phonecode", kdtelp);
                values.put("phone", telp);
                values.put("device", device);
                db.update("tbl_account", values, "idnom=1", null);
            }

            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }catch(Exception e){
            Log.e("Error Save Account",e.toString());
        }
        db.close();
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
}
