package com.arindo.nura;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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
import java.util.Locale;
import java.util.Map;
import java.util.logging.Handler;

/**
 * Created by bmaxard on 17/02/2017.
 */

public class FoodOption extends AppCompatActivity {
    private String SERVERADDR, csrid, email, tmDevice, versi, MyJSON, token;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private TextView tsearchfood, txtLocation;
    Toolbar toolbar;
    Snackbar snackBar;
    ListView mylist;
    int timeoutdata = 20000;
    int pid;
    public static final String TAG_ID = "id";
    public static final String TAG_TITLE = "title";
    public static final String TAG_DESC = "desc";
    public static final String TAG_IMAGE = "imageurl";

    private GridView mGridView;
    private GridviewFoodMenuAdapter mGridAdapter;
    private ArrayList<GridFoodItem> mGridData;
    SwipeRefreshLayout swLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_foodoption);

        if(MainActivity.loaderActivity.isShowing()) {
            MainActivity.loaderActivity.dismiss();
        }

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
        mylist = (ListView) findViewById(R.id.list);
        mGridView = (GridView) findViewById(R.id.gridView1);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");

        RequestFoodOption();

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridviewFoodMenuAdapter(this, R.layout.gridview_foodmenu, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridFoodItem item = (GridFoodItem) parent.getItemAtPosition(position);
                Intent i = new Intent(FoodOption.this, FoodRestoList.class);
                i.putExtra("groupid",item.getId());
                i.putExtra("foodtitle",item.getTitle());
                startActivity(i);
            }
        });

        swLayout = (SwipeRefreshLayout) findViewById(R.id.swlayout);
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RequestFoodOption();
                swLayout.setRefreshing(false);
            }
        });

        tsearchfood = (TextView)findViewById(R.id.tsearchfood);
        tsearchfood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FoodOption.this, FoodSearch.class);
                startActivity(i);
            }
        });

        tsearchfood.setVisibility(View.GONE);
        setLocation();
    }

    private void setLocation(){
        GPSTracker gps = new GPSTracker(FoodOption.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            Double myLocLat = gps.getLatitude();
            Double myLocLng = gps.getLongitude();
            LatLng myLoc = new LatLng(myLocLat, myLocLng);
            getAlamat(myLoc);
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void RequestFoodOption(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"foodoption";
        if (isNetworkAvailable() == true) {
            TaskFoodOption MyTask = new TaskFoodOption();
            MyTask.execute();
        }else {
            SnackBarMsg("Tidak ada koneksi internet");
        }
    }

    public class TaskFoodOption extends AsyncTask<String, Integer, String[]> {
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
                post.add(new BasicNameValuePair("pid","5"));
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
                //mylist.setVisibility(View.GONE);
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

    protected void __showList(){
        JSONObject response = null;
        try {
            if(mGridAdapter!=null){mGridAdapter.clear();}
            response = new JSONObject(MyJSON);
            JSONArray posts = response.optJSONArray("listfood");
            GridFoodItem item;
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.optJSONObject(i);
                String id = post.optString(TAG_ID);
                String title = post.optString(TAG_TITLE);
                String image = post.optString(TAG_IMAGE);
                item = new GridFoodItem();
                item.setId(id);
                item.setTitle(title);
                item.setImage(image);
                /*JSONArray attachments = post.getJSONArray(TAG_IMAGE);
                if (null != attachments && attachments.length() > 0) {
                    JSONObject attachment = attachments.getJSONObject(0);
                    if (attachment != null)
                        item.setImage(attachment.getString("url"));
                }*/
                mGridData.add(item);
            }
            mGridAdapter.setGridData(mGridData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showList(){
        LazyAdapterFoodOption adapter;
        ArrayList<HashMap<String, String>> ListSendOption = new ArrayList<HashMap<String, String>>();
        JSONArray peoples = null;
        try {
            JSONObject response = new JSONObject(MyJSON);
            peoples = response.getJSONArray("listfood");
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                final String groupid = c.getString(TAG_ID);
                String groupname = c.getString(TAG_TITLE);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_ID,groupid);
                map.put(TAG_TITLE,groupname);
                ListSendOption.add(map);
            }

            try {
                adapter = new LazyAdapterFoodOption(this, ListSendOption);
                mylist.setAdapter(adapter);

                mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        TextView toptionid = (TextView) view.findViewById(R.id.toptionid);
                        String optionid = String.valueOf(toptionid.getText());
                        TextView toptionname = (TextView) view.findViewById(R.id.toptionname);
                        String optionname = String.valueOf(toptionname.getText());

                        Intent i = new Intent(FoodOption.this, FoodRestoList.class);
                        i.putExtra("groupid",optionid);
                        i.putExtra("foodtitle",optionname);
                        startActivity(i);
                    }
                });

                ColorDrawable myColor = new ColorDrawable(this.getResources().getColor(R.color.colorPrimaryDark));
                mylist.setDivider(myColor);
                mylist.setDividerHeight(1);
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

        snackBar.setAction("Keluar", new View.OnClickListener() {
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

    public void getAlamat(LatLng point){
        try{
            Geocoder geo = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(point.latitude, point.longitude, 1);
            if(addresses.isEmpty()){
                txtLocation.setText("Pencarian Lokasi");
            }else{
                String[] addr = addresses.get(0).getAddressLine(0).split(",");
                txtLocation.setText(addr[0] + " " +addr[1]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
