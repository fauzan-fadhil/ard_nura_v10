package com.arindo.nura;

import android.app.Activity;
import android.app.Dialog;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

/**
 * Created by bmaxard on 26/01/2017.
 */

public class FoodRestoList extends AppCompatActivity{
    private String SERVERADDR, csrid, email, tmDevice, versi, token, groupid, foodtitle;
    public static String MyJSON;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private TextView txttitle;
    Toolbar toolbar;
    Snackbar snackBar;
    SwipeRefreshLayout swLayout;
    int timeoutdata = 20000;
    public static ListView mylist;
    public static final String TAG_PID = "id";
    public static final String TAG_TITLE = "title";
    public static final String TAG_DESC = "desc";
    public static final String TAG_IMAGE = "imageurl";
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_foodrestolist);

//        if(MainActivity.loaderActivity.isShowing()) {
//            MainActivity.loaderActivity.dismiss();
//        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        this.setTitle(null);

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        //tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        pbar = (ProgressBar) findViewById(R.id.progressbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        txttitle = (TextView) findViewById(R.id.txttitle);

        activity = this;
        mylist = (ListView) findViewById(R.id.list);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        groupid = main.getExtras().getString("groupid");
        foodtitle = main.getExtras().getString("foodtitle");
        txttitle.setText(" "+foodtitle);

        RequestFoodList();

        swLayout = (SwipeRefreshLayout) findViewById(R.id.swlayout);
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RequestFoodList();
                swLayout.setRefreshing(false);
            }
        });
    }

    /*public static void SetListViewAdapter(ArrayList<HashMap<String, String>> restolist, Context context) {
        adapter = new LazyAdapterRestoList(this, restolist);
        mylist.setAdapter(adapter);
    }*/

    private void RequestFoodList(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"foodrestolist";
        if (isNetworkAvailable() == true) {
            TaskRestoList MyTask = new TaskRestoList();
            MyTask.execute();
        }else {
            SnackBarMsg("Internet not connected");
        }
    }

    public class TaskRestoList extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String[] doInBackground(String... value) {
            try {
                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                //post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("token",tmDevice)); // jangan lupa hapus lagi ganti dengan Token sebenarnya
                post.add(new BasicNameValuePair("groupid",groupid));
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
                showList(activity);
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

    public void onRefesh(Activity activity){
        showList(activity);
    }

    public static void showList(final Activity activity){
        LazyAdapterRestoList adapter;
        ArrayList<HashMap<String, String>> ListSendOption = new ArrayList<HashMap<String, String>>();
        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("listfoodresto");
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
                adapter = new LazyAdapterRestoList(activity, ListSendOption);
                mylist.setAdapter(adapter);

                mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // selected item
                        //String lst_txt = parent.getItemAtPosition(position).toString().trim();

                        TextView tpid = (TextView) view.findViewById(R.id.tpid);
                        //TextView ttitle = (TextView) view.findViewById(R.id.ttitle);
                        //TextView tdes = (TextView) view.findViewById(R.id.tdes);
                        String rid = String.valueOf(tpid.getText());

                        GetTmpOrder cekorder = new GetTmpOrder();
                        cekorder.SelectTempTotal(activity,rid,0);
                        if(cekorder.setCekOrder()==false){
                            ShowAlert(0, rid);
                            return;
                        }

                        Intent i = new Intent(activity.getApplicationContext(), FoodRestoDetail.class);
                        i.putExtra("restoid",rid);
                        activity.startActivity(i);
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

    private static void ShowAlert(final int param, final String id) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_alert);
        //dialog.setTitle("Custom Dialog");

        TextView title = (TextView) dialog.findViewById(R.id.titledata);
        TextView text = (TextView) dialog.findViewById(R.id.textMsg);
        Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);

        if (param == 0) {
            title.setText("Switch another place?");
            text.setText("Switch to another place, would cancel the orders now.");
            btnClose.setText("No");
            btnDone.setText("Yes");

            // if decline button is clicked, close the custom dialog
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                }
            });

            // if decline button is clicked, close the custom dialog
            btnDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    GetTmpOrder deleteorder = new GetTmpOrder();
                    deleteorder.DeleteTempOrder(activity,"");

                    Intent i = new Intent(activity.getApplicationContext(), FoodRestoDetail.class);
                    i.putExtra("restoid",id);
                    activity.startActivity(i);
                    dialog.dismiss();
                }
            });
        }
        dialog.show();
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
