package com.arindo.nura;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;


/**
 * Created by bmaxard on 03/10/2016.
 */

public class Registration extends AppCompatActivity {
    private EditText temail, tnama, tphone, tphonecode, tpassword, tpasswordconf, talamat;
    private Button btnReg, btnCancel;
    private SqlHelper dbHelper;
    private String tmDevice, versi;
    private Md5Hex MD5;
    private String SERVERADDR;
    private int timeoutdata = 20000;
    private String MyJSON, countryid, phonecode;
    private CheckBox showPassword;
    private String[] spincountryid, spincountryname, spinphonecode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        //TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        //tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        MD5 = new Md5Hex();
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        temail = (EditText)findViewById(R.id.email);
        tnama = (EditText)findViewById(R.id.nama);
        tphone = (EditText)findViewById(R.id.telp);
        tphonecode = (EditText)findViewById(R.id.telpcode);
        tpassword = (EditText)findViewById(R.id.password);
        tpasswordconf = (EditText)findViewById(R.id.passwordconf);
        talamat = (EditText)findViewById(R.id.alamat);
        btnReg = (Button)findViewById(R.id.btnReg);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        showPassword = (CheckBox) findViewById(R.id.showPassword);

        RequestCountry();
        //SpinnerCountry();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(temail.getText().toString().equals("")){
                    temail.setError("Email cannot be empty");
                    return;
                }
                if(!isValidEmaillId(temail.getText().toString().trim())){
                    temail.setError("Input your email correctly");
                    return;
                }
                if(tnama.getText().toString().equals("")){
                    tnama.setError("Name cannot be empty");
                    return;
                }
                if(talamat.getText().toString().equals("")){
                    talamat.setError("Address cannot be empty");
                    return;
                }
                if(tphone.getText().toString().equals("")){
                    tphone.setError("Phone number cannot be empty");
                    return;
                }
                if(tpassword.getText().toString().equals("")){
                    tpassword.setError("Password cannot be empty");
                    return;
                }
                if(tpasswordconf.getText().toString().equals("")){
                    tpasswordconf.setError("Confirmation cannot be empty");
                    return;
                }
                if(!tpasswordconf.getText().toString().equals(tpassword.getText().toString())){
                    tpasswordconf.setError("Confirmation is not equal");
                    return;
                }

                if (isNetworkAvailable() == true) {
                    ShowAlert(0);
                } else {
                    Toast(2, "Turn on your device's internet connection !");
                }
            }
        });

        tpassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!showPassword.isChecked()) {
                        tpassword.setInputType(129);
                    } else {
                        tpassword.setInputType(128);
                    }
                }
                return false;
            }
        });

        tpasswordconf.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(!showPassword.isChecked()) {
                        tpasswordconf.setInputType(129);
                    } else {
                        tpasswordconf.setInputType(128);
                    }
                }
                return false;
            }
        });

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    tpassword.setInputType(129);
                    tpasswordconf.setInputType(129);
                } else {
                    tpassword.setInputType(128);
                    tpasswordconf.setInputType(128);
                }
            }
        });
    }

    private void RequestCountry(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"reqcountry";
        if (isNetworkAvailable() == true) {
            TaskCountry MyTask = new TaskCountry();
            MyTask.execute();
        } else {
            Toast(2, "Internet not connected !");
        }
    }

    public class TaskCountry extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        String refreshToken = null;
        final Dialog dialog = new Dialog(Registration.this, android.R.style.Theme_Translucent);
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
                FirebaseApp.initializeApp(getBaseContext());
                refreshToken = FirebaseInstanceId.getInstance().getToken();
                if(refreshToken==null){
                    result = new String[]{"2", "Token failed"};
                    return result;
                }

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",refreshToken));
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
                    result = new String[]{"1"};
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
                SpinnerCountry();
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"Request Country failed !"+" ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                Toast(1,"Request Time Out !"+" ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                Toast(1,msg+" ("+rc+")");
                dialog.dismiss();
            }
        }
    }

    private void SpinnerCountry(){
        JSONArray data = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            data = jsonObj.getJSONArray("country");
            int jml = 0;
            spincountryid = new String[data.length()];
            spincountryname = new String[data.length()];
            spinphonecode = new String[data.length()];
            for (int i = 0; i < data.length(); i++) {
                JSONObject c = data.getJSONObject(i);
                String cid = c.getString("countryid");
                String cname = c.getString("countryname");
                String pcode = c.getString("phonecode");
                spincountryid[i] = cid;
                spincountryname[i] = cname;
                spinphonecode[i] = pcode;
                //poselected = Arrays.asList(spincountryid).indexOf(spincountryid);
                jml = jml+1;
            }

            if(jml==data.length()){
                //spincountryid = new String[]{"bn","id"};
                //spincountryname = new String[]{"Brunei Darussalam","Indonesia"};

                Spinner mySpinner = (Spinner) findViewById(R.id.scountry);
                mySpinner.setAdapter(new MyAdapter(this, R.layout.row_country, spincountryname));
                //mySpinner.setSelection(poselected);

                mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View v, int position, long id) {
                        //String titlerror = ((TextView) v.findViewById(R.id.countryname)).getText().toString();
                        countryid = ((TextView) v.findViewById(R.id.countryid)).getText().toString();
                        phonecode = ((TextView) v.findViewById(R.id.phonecode)).getText().toString();
                        tphonecode.setText(phonecode);
                        //kdkelainan = parentView.getItemAtPosition(position).toString();
                        //String OutputMsg = "Selected Kelainan : "+kderror;
                        //Toast.makeText(getApplicationContext(),OutputMsg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }
                });
            }
        }catch (Exception e){
            //
        }
    }

    public class MyAdapter extends ArrayAdapter<String> {

        public MyAdapter(Context ctx, int txtViewResourceId, String[] objects) {
            super(ctx, txtViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
            return getCustomView(position, cnvtView, prnt);
        }

        @Override
        public View getView(int pos, View cnvtView, ViewGroup prnt) {
            return getCustomView(pos, cnvtView, prnt);
        }

        public View getCustomView(int position, View convertView,
                                  ViewGroup parent) {
            LayoutInflater inflater = getLayoutInflater();
            View mySpinner = inflater.inflate(R.layout.row_country, parent,
                    false);
            TextView main_text = (TextView) mySpinner
                    .findViewById(R.id.countryname);
            main_text.setText(spincountryname[position]);

            TextView subSpinner = (TextView) mySpinner
                    .findViewById(R.id.countryid);
            subSpinner.setText(spincountryid[position]);

            TextView subSpinner2 = (TextView) mySpinner
                    .findViewById(R.id.phonecode);
            subSpinner2.setText(spinphonecode[position]);

            /*ImageView left_icon = (ImageView) mySpinner
                    .findViewById(R.id.left_pic);
            left_icon.setImageResource(total_images[position]);*/

            return mySpinner;
        }
    }

    private boolean isValidEmaillId(String email){
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    private void doRegistration(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"regcsr";

        String email = temail.getText().toString();
        String nama = tnama.getText().toString();
        String telp = tphone.getText().toString();
        String pwd = tpasswordconf.getText().toString();
        String alamat = talamat.getText().toString();
        TaskRegistration MyTask = new TaskRegistration();
        MyTask.execute(email, nama, alamat, telp, pwd);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public class TaskRegistration extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        String refreshToken = null;
        final Dialog dialog = new Dialog(Registration.this, android.R.style.Theme_Translucent);
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
                String nama = value[1];
                String alamat = value[2];
                String telp = value[3];
                String pwd = value[4];
                FirebaseApp.initializeApp(getBaseContext());
                refreshToken = FirebaseInstanceId.getInstance().getToken();
                if(refreshToken==null){
                    result = new String[]{"2", "Token failed"};
                    return result;
                }

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",refreshToken));
                post.add(new BasicNameValuePair("email",email));
                post.add(new BasicNameValuePair("nama",nama));
                post.add(new BasicNameValuePair("kdtelp",phonecode));
                post.add(new BasicNameValuePair("telp",telp));
                post.add(new BasicNameValuePair("userpwd",pwd));
                post.add(new BasicNameValuePair("alamat",alamat));
                post.add(new BasicNameValuePair("country",countryid));
                post.add(new BasicNameValuePair("device",tmDevice));
                post.add(new BasicNameValuePair("versi",versi));

                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(timeoutdata);
                HttpPost httppost = new HttpPost(SERVERADDR);

                httppost.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                MyJSON = request(response);
                //Log.e("JSON",MyJSON);
                JSONObject jobject = new JSONObject(MyJSON);

                String rc = jobject.getString("rc");
                if (rc.equals("1")) {
                    String csrid = jobject.getString("csrid");
                    result = new String[]{"1", csrid, email, nama, alamat, phonecode, telp};
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
                String telpkode = result[5];
                String telp = result[6];
                saveRegistration(csrid, email, nama, alamat, telpkode, telp);
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"Registration failed !"+" ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                Toast(1,"Request Time Out !"+" ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                Toast(1,msg+" ("+rc+")");
                dialog.dismiss();
            }
        }
    }

    private void saveRegistration(String csrid, String email, String nama, String alamat, String telpkode, String telp){
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
                values.put("phonecode", telpkode);
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
                values.put("phonecode", telpkode);
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
            title.setText("CONFIRMATION");
            text.setText("Send registration?");
            btnClose.setText("NO");
            btnDone.setText("YES");

            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                    doRegistration();
                }
            });

            // if decline button is clicked, close the custom dialog
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
