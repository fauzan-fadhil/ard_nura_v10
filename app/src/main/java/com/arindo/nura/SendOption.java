package com.arindo.nura;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by bmaxard on 26/01/2017.
 */

public class SendOption extends AppCompatActivity{
    private String SERVERADDR, csrid, email, tmDevice, versi, MyJSON, token;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    Toolbar toolbar;
    Snackbar snackBar;
    ListView mylist;
    int timeoutdata = 20000;
    int pid;
    LazyAdapter adapter;
    ArrayList<HashMap<String, String>> ListSendOption = new ArrayList<HashMap<String, String>>();
    public static final String TAG_PID = "pid";
    public static final String TAG_TITLE = "title";
    public static final String TAG_DESC = "desc";
    public static final String TAG_IMAGE = "imageurl";
    SwipeRefreshLayout swLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_sendoption);

        if(MainActivity.loaderActivity.isShowing()) {
            MainActivity.loaderActivity.dismiss();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.setTitle(null);

      //  TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        pbar = (ProgressBar) findViewById(R.id.progressbar);
        mylist = (ListView) findViewById(R.id.list);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");

        RequestSendOption();

        swLayout = (SwipeRefreshLayout) findViewById(R.id.swlayout);
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RequestSendOption();
                swLayout.setRefreshing(false);
            }
        });
    }

    private void RequestSendOption(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"sendoption";
        if (isNetworkAvailable() == true) {
            TaskSendOption MyTask = new TaskSendOption();
            MyTask.execute();
        }else {
            SnackBarMsg("Internet not connected");
        }
    }

    public void SetListViewAdapter(ArrayList<HashMap<String, String>> berita) {
        adapter = new LazyAdapter(this, berita);
        mylist.setAdapter(adapter);
    }

    public class TaskSendOption extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String[] doInBackground(String... value) {
            try {
                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("pid","3"));
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
                showList();
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("88") || rc.equals("101")){
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("99")){
                pbar.setVisibility(View.GONE);
            }else{
                String msg = result[1];
                pbar.setVisibility(View.GONE);
                mylist.setVisibility(View.GONE);
                SnackBarMsg(msg);
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

    protected void showList(){
        JSONArray peoples = null;
        try {
            if(mylist.getChildCount() > 0){ListSendOption.clear();}
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("sendoption");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String pid = c.getString(TAG_PID);
                String title = c.getString(TAG_TITLE);
                String desc = c.getString(TAG_DESC);
                String imageurl = c.getString(TAG_IMAGE);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_PID,pid);
                map.put(TAG_TITLE,title);
                map.put(TAG_DESC,desc);
                map.put(TAG_IMAGE,imageurl);
                ListSendOption.add(map);
                jml++;
            }
            try {
                runOnUiThread(new Runnable() {
                    public void run() {
                        SetListViewAdapter(ListSendOption);
                    }
                });

                mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // selected item
                        //String lst_txt = parent.getItemAtPosition(position).toString().trim();

                        TextView tpid = (TextView) view.findViewById(R.id.tpid);
                        String lst_txt = String.valueOf(tpid.getText());

                        // Launching new Activity on selecting single List Item
                        Intent i = new Intent(SendOption.this, SendOrder.class);
                        // sending data to new activity
                        i.putExtra("optionid",Integer.parseInt(lst_txt));
                        i.putExtra("order",Integer.parseInt(pid+"") );
                        startActivity(i);
                        //SnackBarMsg(lst_txt);
                    }
                });
            }catch (Exception e){
                Log.e("CUT","Cut process");
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

        snackBar.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
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
