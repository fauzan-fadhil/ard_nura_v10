package com.arindo.nura;

/**
 * Created by bmaxard on 10/01/2017.
 */
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FoodOrderNonKurir extends AppCompatActivity implements OnMapReadyCallback {
    private static String restoid;
    private GoogleMap mMap;
    //EditText txtdest, tdestdetail, tdestcontact;
    int PLACE_PICKER_REQUEST = 0;
    private TextView txttitle;
    private Button btnOrder, addDest;
    private String SERVERADDR, csrid, email, tmDevice, versi;
    private int timeoutdata = 20000;
    private String MyJSON, MyJSONObjectDetail = null, JSONObjectOpr;
    private SqlHelper dbHelper;
    private LinearLayout layoutsubmit;
    private ProgressBar pbar;
    public static TextView tpricepayment;
    public static TextView texppayment;
    public static TextView tpayment;
    public static TextView tdistance;
    public static Double tammount, expprice;
    private Button addmenu;
    private String restonama, restoalamat;
    Toolbar toolbar;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest;
    Double myLocLat, myLocLng;
    String distance, token, RCFROMSERVER=null, MSGFROMSERVER=null;
    String distanceprice[] = null;
    Snackbar snackBar;
    Activity activity;

    GPSTracker gps;

    TextView slideHandleText;
    //SlidingDrawer slidingDrawer;

    public static final String TAG_MENUID = "menuid";
    public static final String TAG_MENU = "menu";
    public static final String TAG_PRICE = "price";
    LazyAdapterRestoMenuNonKurir adapter;
    ArrayList<HashMap<String, String>> ListMenu = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_foodordernonkurir);

        activity = this;

        //if(MainActivity.loaderActivity.isShowing()) {
        //    MainActivity.loaderActivity.dismiss();
        //}

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        this.setTitle(null);

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        btnOrder = (Button)findViewById(R.id.btnOrder);
        addDest = (Button)findViewById(R.id.addDest);
        txttitle = (TextView)findViewById(R.id.txttitle);
        layoutsubmit = (LinearLayout)findViewById(R.id.layoutsubmit);
        pbar = (ProgressBar) findViewById(R.id.pbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        /*
        tdestdetail = (EditText) findViewById(R.id.tdestdetail);
        tdestcontact = (EditText) findViewById(R.id.tdestcontact);
        txtdest = (EditText) findViewById(R.id.txtdest);
        disableEditText(txtdest);
        */
        slideHandleText = (TextView) findViewById(R.id.slideHandleText);
        //slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
        addmenu = (Button) findViewById(R.id.addmenu);

        tammount = 0.0;
        expprice = 0.0;
        tpricepayment = (TextView) findViewById(R.id.tpricepayment);
        texppayment = (TextView) findViewById(R.id.texppayment);
        tpayment = (TextView) findViewById(R.id.tpayment);
        tdistance = (TextView) findViewById(R.id.tdistance);
        //tambahan versi 10 os//
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        restoid = main.getExtras().getString("restoid");
        txttitle.setText(" WILAYAH");

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        markerPointsOpr = new ArrayList<LatLng>();
        //getMaps();
        getCekResto();

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();

        /*
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                slideHandleText.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_down_float, 0);
            }
        });

        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                slideHandleText.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.arrow_up_float, 0);
            }
        });
        */

        /*
        txtdest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbar.setVisibility(View.VISIBLE);
                if(RCFROMSERVER != null || markerPointsOpr.size()==0) {
                    if(MSGFROMSERVER!=null){SnackBarMsg(MSGFROMSERVER);}
                    getCekResto();
                    return;
                }
                if(snackBar.isShown()){snackBar.dismiss();}
                PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(FoodOrderNonKurir.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        */

        showList();

        btnOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.e("JSON",LoadDataOrder());
                MyJSONObjectDetail = LoadDataOrder();
                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host+"ordercsr";
                if (isNetworkAvailable() == true) {
                    if(MyJSONObjectDetail == null){
                        SnackBarMsg("Failed detail orders !");
                    }else {
                        //Log.e("Detail orders", MyJSONObjectDetail);
                        String restoLat = origin.latitude + "";
                        String restoLng = origin.longitude + "";
                        //String destination = txtdest.getText().toString();
                        //String destnote = tdestdetail.getText().toString();
                        //String destaddcontact = tdestcontact.getText().toString();
                        String destlat = origin.latitude + ""; //dest.latitude+"";
                        String destlng = origin.longitude + ""; //dest.longitude+"";

                        TaskOrder MyTask = new TaskOrder();
                        //MyTask.execute(token, restoLat, restoLng, destination, destnote, destaddcontact, destlat, destlng);
                        MyTask.execute(token, restoLat, restoLng, "", "", "", destlat, destlng);
                    }
                }
            }
        });

        addmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(FoodRestoMenu.instance != null) {
                    try {
                        FoodRestoMenu.instance.finish();
                    } catch (Exception e) {}
                }
                onBackPressed();
            }
        });
    }

    private String LoadDataOrder(){
        String result = null;
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Start the transaction.
        db.beginTransaction();
        try{
            JSONArray destarr = new JSONArray();
            JSONObject obj;
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_tmporder WHERE placesid='"+restoid+"' ORDER BY idnom",null);
            if(cursor.getCount() >0){
                int jml=0;
                tammount = 0.0;
                while (cursor.moveToNext()) {
                    // Read columns data
                    final String groupid= cursor.getString(cursor.getColumnIndex("groupid"));
                    final String menuid= cursor.getString(cursor.getColumnIndex("itemid"));
                    final Double price= cursor.getDouble(cursor.getColumnIndex("price"));
                    final int qty = cursor.getInt(cursor.getColumnIndex("qty"));
                    final String notes= cursor.getString(cursor.getColumnIndex("notes"));

                    obj = new JSONObject();
                    obj.put("groupid", groupid);
                    obj.put("menuid", menuid);
                    obj.put("price", String.valueOf(price));
                    obj.put("qty", String.valueOf(qty));
                    obj.put("notes", notes);
                    destarr.put(obj);

                }
                JSONObject object = new JSONObject();
                JSONObject JSONDestLoc = object.put("detail",destarr);
                result = JSONDestLoc+"";
            }
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e){
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally{
            db.endTransaction();
            // End the transaction.
            db.close();
            // Close database
        }
        return result;
    }

    private static String currencyformat(double value){
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(otherSymbols);

        return String.valueOf(df.format(value));
    }

    public void SetListViewAdapter(ArrayList<HashMap<String, String>> restolist) {
        adapter = new LazyAdapterRestoMenuNonKurir(this, restolist, 1);
        NonScrollListView list = (NonScrollListView) findViewById(R.id.lv_nonscroll_list);
        list.setAdapter(adapter);
    }

    protected void showList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Start the transaction.
        db.beginTransaction();
        try{
            Cursor cursor = db.rawQuery("SELECT * FROM tbl_tmporder WHERE placesid='"+restoid+"' ORDER BY idnom",null);
            if(cursor.getCount() >0){
                int jml=0;
                tammount = 0.0;
                while (cursor.moveToNext()) {
                    // Read columns data
                    final String menuid= cursor.getString(cursor.getColumnIndex("itemid"));
                    final String menu= cursor.getString(cursor.getColumnIndex("item"));
                    final Double price= cursor.getDouble(cursor.getColumnIndex("price"));

                    final Double ammount= cursor.getDouble(cursor.getColumnIndex("ammount"));
                    tammount += ammount;
                    HashMap<String, String> map = new HashMap<String,String>();
                    map.put(TAG_MENUID,menuid);
                    map.put(TAG_MENU,menu);
                    map.put(TAG_PRICE,price+"");
                    ListMenu.add(map);

                }
                SetListViewAdapter(ListMenu);
                //NumberFormat nfout = NumberFormat.getNumberInstance(Locale.ENGLISH);
                //nfout.setMaximumFractionDigits(2);
                //tpricepayment.setText("BND. "+String.valueOf(nfout.format(tammount)));
                //tpayment.setText("BND. "+String.valueOf(nfout.format(tammount + expprice)));
                tpricepayment.setText("Rp "+ currencyformat(tammount));
                tpayment.setText("Rp "+ currencyformat(tammount + expprice));
            }
            db.setTransactionSuccessful();
        }
        catch (SQLiteException e){
            e.printStackTrace();
        }
        finally{
            db.endTransaction();
            // End the transaction.
            db.close();
            // Close database
        }
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        //editText.setEnabled(false);
        editText.setCursorVisible(false);
        //editText.setKeyListener(null);
        //editText.setBackgroundColor(Color.TRANSPARENT);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);

        gps = new GPSTracker(FoodOrderNonKurir.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            myLocLat = gps.getLatitude();
            myLocLng = gps.getLongitude();
            myLoc = new LatLng(myLocLat,myLocLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
            //        + myLocLat + "\nLong: " + myLocLng, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void getMaps() {
        //make screen stays active
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get mark potiton
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync( this);

        if (mapFragment != null && mapFragment.getView().findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 180, 180, 0);
        }
    }

    private Bitmap icons(int param){
        int height = 40;
        int width = 40;
        BitmapDrawable bitmapdraw=null;
        if(param==5){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_food);}
        //if(param==50){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_ridesend);}
        if(param==50){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.motornura);}

        if(param==101){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_origin);}
        if(param==102){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_destination);}
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

    private void getCekResto() {
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"csrcekresto";
        if (isNetworkAvailable() == true) {
            TaskCekResto MyTask = new TaskCekResto();
            MyTask.execute();
        }else{
            SnackBarMsg("Tidak ada koneksi internet !");
        }
    }

    public class TaskCekResto extends AsyncTask<String, Integer, String[]> {
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
                post.add(new BasicNameValuePair("restoid",restoid));
                post.add(new BasicNameValuePair("csrlat",myLocLat+""));
                post.add(new BasicNameValuePair("csrlng",myLocLng+""));
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
            pbar.setVisibility(View.GONE);
            String rc = result[0];
            if(rc.equals("1")){
                JSONObject jsonObj = null;
                JSONArray peoples = null;
                JSONArray datarr = new JSONArray();
                try {
                    jsonObj = new JSONObject(MyJSON);
                    restonama = jsonObj.getString("resto");
                    restoalamat = jsonObj.getString("alamat");
                    String lat = jsonObj.getString("lat");
                    String lng = jsonObj.getString("lng");
                    LatLng point_newresto = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                    origin = point_newresto;
                    txttitle.setText(" "+restonama);

                    /*
                    MarkerOptions options = new MarkerOptions();
                    options.position(point_newresto);

                    options.icon(BitmapDescriptorFactory.fromBitmap(icons(5)));
                    options.title(restonama);
                    options.snippet(restoalamat);
                    mMap.addMarker(options);
                    */

                    peoples = jsonObj.getJSONArray("operator");
                    for (int i = 0; i < peoples.length(); i++) {
                        JSONObject dt = peoples.getJSONObject(i);
                        String oprid = dt.getString("oprid");
                        String oprname = dt.getString("oprname");
                        String oprlat = dt.getString("oprlat");
                        String oprlng = dt.getString("oprlng");

                        LatLng point_new = new LatLng(Double.parseDouble(oprlat),Double.parseDouble(oprlng));

                        /*
                        markerPointsOpr.add(point_new);
                        //MarkerOptions options = new MarkerOptions();
                        options.position(point_new);

                        options.icon(BitmapDescriptorFactory.fromBitmap(icons(50)));
                        options.title(oprname);
                        mMap.addMarker(options);
                        */

                        JSONObject persons = new JSONObject();
                        persons.put("oprid",oprid);
                        datarr.put(persons);
                    }
                    JSONObject object = new JSONObject();
                    JSONObject JSONDetail = object.put("arroprid",datarr);
                    JSONObjectOpr = JSONDetail.toString();

                    RCFROMSERVER = null;
                    MSGFROMSERVER = null;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(rc.equals("88") || rc.equals("101")){
                RCFROMSERVER = rc;
                MSGFROMSERVER = "Gagal pilih lokasi ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else if(rc.equals("99")){
                RCFROMSERVER = rc;
                MSGFROMSERVER = "Gagal pilih lokasi ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else{
                RCFROMSERVER = rc;
                MSGFROMSERVER = result[1];
                SnackBarMsg(MSGFROMSERVER);

                //if(rc.equals("01")){getOprLocation();}
            }
        }
    }

    /*
    protected void placeMarkerOnMap(LatLng location) {
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
        }

        dest = location;

        if(origin!=null && dest!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 10));

                LatLng[] point_new = new LatLng[2];
                point_new[0] = origin;
                point_new[1] = dest;
                for (int i = 0; i < point_new.length; i++) {
                    markerPoints.add(point_new[i]);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point_new[i]);

                    if (i == 0) {
                        options.icon(BitmapDescriptorFactory.fromBitmap(icons(5)));
                        options.title("Nama Tempat");
                        options.snippet(txtdest.getText().toString());
                    } else if (i == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Pengiriman");
                        options.snippet(txtdest.getText().toString());
                    }
                    mMap.addMarker(options);

                    if(markerPoints.size() >= 2){
                        pbar.setVisibility(View.VISIBLE);
                        LatLng originLatLng = markerPoints.get(0);
                        LatLng destLatLng = markerPoints.get(1);
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(originLatLng, destLatLng);
                        //String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }
            }
        }else if(origin!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));

                LatLng[] point_new = new LatLng[1];
                point_new[0] = origin;
                for (int i = 0; i < point_new.length; i++) {
                    markerPoints.add(point_new[i]);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point_new[i]);

                    if (i == 0) {
                        options.icon(BitmapDescriptorFactory.fromBitmap(icons(5)));
                        options.title("Nama Tempat");
                        options.snippet(txtdest.getText().toString());
                    }
                    mMap.addMarker(options);
                }
            }
        }else if(dest!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 13));

                LatLng[] point_new = new LatLng[1];
                point_new[0] = dest;
                for (int i = 0; i < point_new.length; i++) {
                    markerPoints.add(point_new[i]);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point_new[i]);

                    if (i == 0) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Tujuan");
                        options.snippet(txtdest.getText().toString());
                    }
                    mMap.addMarker(options);
                }
            }
        }
    }
    */

    private String getDirectionsUrl(LatLng origin, LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        String key = "key=AIzaSyAS2JfClIY-X5ZaILitjzAXVc1qi-MDLRA";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+key;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
        //https://maps.googleapis.com/maps/api/directions/json?origin=-6.958899,107.570207&destination=-6.953157,107.564038&sensor=false
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.e("Exception url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                //Log.e("Download","Progress 1");
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.e("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
            pbar.setVisibility(View.GONE);
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            //String distance = "";
            String duration = "";

            try {
                if (result.size() < 1) {
                    Toast.makeText(getBaseContext(), "Tidak ada pin lokasi", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Traversing through all the routes
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<LatLng>();
                    lineOptions = new PolylineOptions();
                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);
                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);
                        if (j == 0) {    // Get distance from the list
                            //distance = (String) point.get("distance_text");
                            distance = (String) point.get("distance_value"); // yang diambil value (meter) bukan text nya
                            continue;
                        } else if (j == 1) { // Get duration from the list
                            duration = (String) point.get("duration_text");
                            continue;
                        }
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(5);
                    lineOptions.color(Color.BLUE);
                }

                CalculatServer(distance);
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }catch (Exception e){
                Log.e("error parsing array","result not found");
                Toast(2,"Result Google Maps not found");
            }
        }
    }

    private void CalculatServer(String distance){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"excalculate";
        if (isNetworkAvailable() == true) {
            TaskCalcutate MyTask = new TaskCalcutate();
            MyTask.execute(token,distance,"5");
        }
    }


    public class TaskOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(FoodOrderNonKurir.this, android.R.style.Theme_Translucent);
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
                String token = value[0];
                String restoLat = value[1];
                String restoLng = value[2];
                String destination = value[3];
                String destnote = value[4];
                String destaddcontact = value[5];
                String destlat = value[6];
                String destlng = value[7];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("pid","5"));

                post.add(new BasicNameValuePair("originlat",restoLat));
                post.add(new BasicNameValuePair("originlng",restoLng));
                post.add(new BasicNameValuePair("departure",restoalamat));
                post.add(new BasicNameValuePair("departnote",restonama));

                post.add(new BasicNameValuePair("destlat",destlat));
                post.add(new BasicNameValuePair("destlng",destlng));
                post.add(new BasicNameValuePair("destination",restoalamat)); //destination
                post.add(new BasicNameValuePair("destnote",destnote));
                post.add(new BasicNameValuePair("destaddcontact",destaddcontact));

                post.add(new BasicNameValuePair("restoid",restoid));
                post.add(new BasicNameValuePair("distance", "0")); //distanceprice[0]
                post.add(new BasicNameValuePair("distanceprice", "0")); //distanceprice[1]

                post.add(new BasicNameValuePair("arroprid",JSONObjectOpr));
                post.add(new BasicNameValuePair("detail",MyJSONObjectDetail));
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
                //SnackBarMsg("Orders Success, waiting progress..");
                ShowHistory();
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Order gagal ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Order gagal ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowHistory(){
        GetTmpOrder delete = new GetTmpOrder();
        delete.DeleteTempOrder(activity,restoid);

        FoodRestoList refreshlist = new FoodRestoList();
        refreshlist.onRefesh(activity);

        FoodRestoDetail refreshdetail = new FoodRestoDetail();
        refreshdetail.onRefesh(activity);

        FoodRestoDetail sumTotal = new FoodRestoDetail();
        sumTotal.TempOrder(activity);

        if(FoodRestoMenu.instance != null) {
            try {
                FoodRestoMenu.instance.finish();
            } catch (Exception e) {}
        }

        Bundle bd = new Bundle();
        bd.putInt("act", 0);
        Intent i = new Intent(this, History.class);
        i.putExtras(bd);
        this.finish();
        startActivity(i);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        pbar.setVisibility(View.GONE);
        /*if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                try{
                    Place place = PlacePicker.getPlace(this, data);
                    if(PLACE_PICKER_REQUEST==1) {
                        txtdest.setText(place.getAddress().toString());
                        txtdest.setTextColor(Color.parseColor("#000000"));
                        dest = place.getLatLng();
                        placeMarkerOnMap(place.getLatLng());
                    }
                }catch (Exception e){
                    SnackBarMsg("Gagal pilih lokasi, silahkan coba kembali..");
                }
            }
        }*/
    }

    public class TaskCalcutate extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(FoodOrderNonKurir.this, android.R.style.Theme_Translucent);
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
                String token = value[0];
                String distance = value[1];
                String pid = value[2];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("distance",distance));
                post.add(new BasicNameValuePair("pid",pid));
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
                    String distancetext = jobject.getString("distancetext");
                    String distancevalue = jobject.getString("distancevalue");
                    String textprice = jobject.getString("tprice");
                    String priceperkm = jobject.getString("price");
                    String totalprice = jobject.getString("totalprice");
                    result = new String[]{"1", distancetext, distancevalue, textprice, priceperkm, totalprice};
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
                String distancetext = result[1];
                String distancevalue = result[2];
                String textprice = result[3];
                String priceperkm = result[4];
                String totalprice = result[5];
                distanceprice = new String[]{distancevalue,priceperkm};
                expprice = Double.parseDouble(totalprice);
                texppayment.setText(textprice);
                tdistance.setText("Biaya Pengiriman ("+distancetext+")");
                //NumberFormat nfout = NumberFormat.getNumberInstance(Locale.ENGLISH);
                //nfout.setMaximumFractionDigits(2);
                //tpayment.setText("BND. "+String.valueOf(nfout.format(tammount + expprice)));
                tpayment.setText("Rp "+ currencyformat(tammount + expprice));
                btnOrder.setEnabled(true);
                dialog.dismiss();

            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Gagal kalkulasi ! ("+rc+")");
                dialog.dismiss();
                errorCalculation();
            }else if(rc.equals("99")){
                SnackBarMsg("Gagal kalkulasi ! ("+rc+")");
                dialog.dismiss();
                errorCalculation();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
                errorCalculation();
            }
        }
    }

    private void errorCalculation(){
        tpricepayment.getText().toString();
        tdistance.setText("Biaya Pengiriman");
        texppayment.setText("0");
        tpayment.setText(tpricepayment.getText().toString());
        btnOrder.setEnabled(false);
    }

    public static void onRefreshAmmount(Context con){
        GetTmpOrder sumQty = new GetTmpOrder();
        sumQty.SelectTempTotal(con, restoid, 1);
        //NumberFormat nfout = NumberFormat.getNumberInstance(Locale.ENGLISH);
        //nfout.setMaximumFractionDigits(2);
        tammount = sumQty.setTotalAmmount();
        //tpricepayment.setText("BND. "+String.valueOf(nfout.format(tammount)));
        //tpayment.setText("BND. "+String.valueOf(nfout.format(tammount + expprice)));
        tpricepayment.setText("Rp "+ currencyformat(tammount));
        tpayment.setText("Rp "+ currencyformat(tammount + expprice));
    }
}