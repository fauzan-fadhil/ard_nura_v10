package com.arindo.nura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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

/**
 * Created by bmaxard on 05/10/2016.
 */

public class RequestList extends AppCompatActivity {
    private String tmDevice, versi, textsql;
    private SqlHelper dbHelper;
    private Cursor cursor;
    private int listindex;
    private String[] arr;
    private String SERVERADDR, MyJSON;
    private TableLayout tableRequest;
    private int timeoutdata = 30000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_request_list);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.setTitle("List Permohonan");

      //  TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        tableRequest = (TableLayout) findViewById(R.id.tableRequest);

        //loadDataRequest(0);
        loadStatusRequest();
    }

    private void loadStatusRequest(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"statusreqcsr";
        if (isNetworkAvailable() == true) {
            SetAccount csrAccount = new SetAccount();
            csrAccount.loadAccount(this);
            TaskListRequest MyTask = new TaskListRequest();
            MyTask.execute(csrAccount.setid());
        }else {
            Toast(2, "Internet not connected !");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public class TaskListRequest extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(RequestList.this, android.R.style.Theme_Translucent);
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

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("csrid",csrid));
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
                    JSONArray listrequest = null;
                    listrequest = jobject.getJSONArray("listrequest");
                    int progress=0;
                    for(int i=0;i<listrequest.length();i++) {

                        JSONObject dt = listrequest.getJSONObject(i);
                        final String tiket = dt.getString("tiket");
                        final String nama = dt.getString("nama");
                        final String alamat = dt.getString("alamat");
                        final String telp = dt.getString("telp");
                        final String jenis = dt.getString("jenis");
                        final String ket = dt.getString("ket");
                        final String reqtime = dt.getString("reqtime");
                        final String status = dt.getString("status");
                        final String taketime = dt.getString("taketime");
                        final String ordertime = dt.getString("ordertime");
                        final String paymenttime = dt.getString("paymenttime");
                        final String deskripsi = dt.getString("deskripsi");
                        final String exprice = dt.getString("exprice");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addRowRequest(csrid, tiket, nama, alamat, telp, jenis, ket, reqtime, status, taketime, ordertime, paymenttime, deskripsi, exprice);
                            }
                        });

                        progress++;
                        publishProgress(progress);
                    }

                    if(progress==0){
                        result = new String[] {"1"};
                    }else if(progress==listrequest.length()){
                        result = new String[] {"1"};
                    }
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

    private void addRowRequest(final String csrid, final String tiket, final String nama,
                               final String alamat, final String telp, final String jenis,
                               final String ket, final String reqtime, final String status,
                               final String taketime, final String ordertime, final String paymenttime,
                               final String deskripsi, final String exprice) {
        String tstatus = null;

        LayoutInflater inflater = getLayoutInflater();
        TableRow tr = (TableRow)inflater.inflate(R.layout.row_request, tableRequest, false);

        TextView tv1 = (TextView)tr.findViewById(R.id.ttiket);
        tv1.setText(tiket);

        TextView tv2 = (TextView)tr.findViewById(R.id.tket);
        tv2.setText(ket);

        TextView tv3 = (TextView)tr.findViewById(R.id.tstatus);
        if(status.equals("0")){
            tstatus="Waiting list";
            tv3.setTextColor(getResources().getColor(R.color.red_A700));
        }
        if(status.equals("1")){
            tstatus="In order";
            tv3.setTextColor(getResources().getColor(R.color.blue_A700));
        }
        if(status.equals("2")){
            tstatus="Billing payment";
            tv3.setTextColor(getResources().getColor(R.color.orange_A700));
        }
        if(status.equals("3")){
            tstatus="Setup proccess";
            tv3.setTextColor(getResources().getColor(R.color.brown_700));
        }
        if(status.equals("4")){
            tstatus="Finish";
            tv3.setTextColor(getResources().getColor(R.color.green_A700));
        }
        tv3.setText(tstatus);

        final String finalTstatus = tstatus;
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bd = new Bundle();
                bd.putString("csrid", csrid);
                bd.putString("tiket", tiket);
                bd.putString("nama", nama);
                bd.putString("alamat", alamat);
                bd.putString("telp", telp);
                bd.putString("jenis", jenis);
                bd.putString("ket", ket);
                bd.putString("reqtime", reqtime);
                bd.putString("status", status);
                bd.putString("tstatus", finalTstatus);
                bd.putString("taketime", taketime);
                bd.putString("ordertime", ordertime);
                bd.putString("paymenttime", paymenttime);
                bd.putString("deskripsi", deskripsi);
                bd.putString("exprice", exprice);
                Intent i = new Intent(RequestList.this, RequestDetail.class);
                i.putExtras(bd);
                startActivity(i);
            }
        });

        tableRequest.addView(tr);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
