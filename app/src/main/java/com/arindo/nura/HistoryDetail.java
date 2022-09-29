package com.arindo.nura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bmaxard on 15/01/2017.
 */

public class HistoryDetail extends AppCompatActivity implements OnMapReadyCallback {
    private int pid;
    private GoogleMap mMap;
    EditText txtori, txtdest;
    int PLACE_PICKER_REQUEST = 0, listindex;
    private TextView txttitle, tdistance, tprice, tdriver, tcontact, ttiket, tstatus;
    private ImageView ictitle, imgContact;
    private Button btnCancel;
    private LinearLayout layoutsubmit, layoutprogress, layoutrating;
    private RatingBar ratingBar;
    private String SERVERADDR, csrid, email, tmDevice, versi;
    private int timeoutdata = 20000;
    private String MyJSON;
    private SqlHelper dbHelper;
    Toolbar toolbar;
    ArrayList<LatLng> markerPoints;
    LatLng myLoc, origin, dest, oprLoc;
    Double myLocLat, myLocLng;
    String tiket, token;
    int paramdistancelocation = 0 ;

    GPSTracker gps;

    Timer myTimer;
    Marker markerPointsLastOprLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_history_detail);

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

        btnCancel = (Button)findViewById(R.id.btnCancel);
        txttitle = (TextView)findViewById(R.id.txttitle);
        tdistance = (TextView)findViewById(R.id.tdistance);
        tprice = (TextView)findViewById(R.id.tprice);
        tdriver = (TextView)findViewById(R.id.tdriver);
        tcontact = (TextView)findViewById(R.id.tcontact);
        ttiket = (TextView)findViewById(R.id.ttiket);
        tstatus = (TextView)findViewById(R.id.tstatus);
        ictitle = (ImageView)findViewById(R.id.ictitle);
        imgContact = (ImageView)findViewById(R.id.imgContact);
        layoutprogress = (LinearLayout) findViewById(R.id.layoutprogress);
        layoutsubmit = (LinearLayout) findViewById(R.id.layoutsubmit);
        layoutrating = (LinearLayout) findViewById(R.id.layoutrating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");
        token = main.getExtras().getString("token");
        tiket = main.getExtras().getString("tiket");
        listindex = main.getExtras().getInt("listindex");

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();

        if (pid == 0) {
            txttitle.setText(" RIDE - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_ride);
        }else if (pid == 1) {
            txttitle.setText(" FOOD - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_car);
        }else if (pid == 2) {
            txttitle.setText("BC2 BOAT - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_boat);
        }else if (pid == 3) {
            txttitle.setText("BC2 SEND - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_send);
        }else if (pid == 4) {
            txttitle.setText("BC2 CLEAN - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_clean);
        }else if (pid == 5) {
            txttitle.setText(" FOOD - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_food);
        }else if (pid == 6) {
            txttitle.setText("BC2 TICK - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_tick);
        }else if (pid == 7) {
            txttitle.setText("BC2 TOWING - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_towing);
        }else if (pid == 8) {
            txttitle.setText("BC2 DES - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_des);
        }else if (pid == 9) {
            txttitle.setText("BC2 DST - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_dst);
        }else if (pid == 10) {
            txttitle.setText("BC2 INDOSAT - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_indosat);
        }else if (pid == 11) {
            txttitle.setText("BC2 TELKOMSEL - DETAIL");
            ictitle.setImageResource(R.drawable.ic_dekstop_telkomsel);
        }

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        getMaps();

        txtori = (EditText) findViewById(R.id.txtori);
        txtdest = (EditText) findViewById(R.id.txtdest);
        disableEditText(txtori);
        disableEditText(txtdest);

        RequestHistory();

        myTimer = new Timer();
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setCursorVisible(false);
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

    private void getMaps() {
        //make screen stays active
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get mark potiton
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (mapFragment != null && mapFragment.getView().findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 180, 180, 0);
        }
    }

    private void LoadLastOprLocation(){
        myTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //Log.e("SCHEDULAR","Load position "+tiket);

                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host+"csrloadlastoprlocation";
                if (isNetworkAvailable() == true) {
                    TaskLoadLastOprLoc MyTask = new TaskLoadLastOprLoc();
                    MyTask.execute(token,tiket);
                }
            }
        }, 0, 10000);
    }

    private void LoadLastOprLocationStop(){
        //Log.e("SCHEDULAR STOP","Stoping load position ");
        myTimer.cancel();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);


        gps = new GPSTracker(HistoryDetail.this);

        // check if GPS enabled        gps = new GPSTracker(HistoryDetail.this);

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

    public class TaskLoadLastOprLoc extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String[] doInBackground(String... value) {
            try {
                String token = value[0];
                String tiket = value[1];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("tiket",tiket));
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
                    String driverlat = jobject.getString("driverlat");
                    String driverlng = jobject.getString("driverlng");
                    String driver = jobject.getString("driver");
                    result = new String[]{"1", driverlat, driverlng, driver};
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
                String driverlat = result[1];
                String driverlng = result[2];
                String driver = result[3];
                LastOprLocation(driver, driverlat, driverlng);
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Checking support location ! ("+rc+")");
            }else if(rc.equals("99")){
                SnackBarMsg("Checking support location ! ("+rc+")");
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
            }
        }
    }

    private void LastOprLocation(String driver, String lat, String lng){
        if (markerPointsLastOprLoc != null) {
            markerPointsLastOprLoc.remove();
        }

        markerPointsLastOprLoc = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)))
                .draggable(true).visible(true)
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .icon(BitmapDescriptorFactory.fromBitmap(icons(1, pid)))
                .title("Current driver position")
                .snippet(driver));
    }

    private void RequestHistory(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"orderhistorydetailcsr";
        if (isNetworkAvailable() == true) {
            TaskHistoryDetail MyTask = new TaskHistoryDetail();
            MyTask.execute(token,tiket);
        }
    }

    public class TaskHistoryDetail extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetail.this, android.R.style.Theme_Translucent);
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
                String tiket = value[1];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("tiket",tiket));
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
                    String dep = jobject.getString("dep");
                    String dest = jobject.getString("dest");
                    String depnote = jobject.getString("depnote");
                    String destnote = jobject.getString("destnote");
                    String orilat = jobject.getString("orilat");
                    String orilng = jobject.getString("orilng");
                    String destlat = jobject.getString("destlat");
                    String destlng = jobject.getString("destlng");
                    String dist = jobject.getString("distance");
                    String price = jobject.getString("price");
                    String driver = jobject.getString("driver");
                    String drivercontact = jobject.getString("drivercontact");
                    String storder = jobject.getString("storder");
                    String ratings = jobject.getString("ratings");
                    String driverlat = jobject.getString("driverlat");
                    String driverlng = jobject.getString("driverlng");
                    String takelat = jobject.getString("takelat");
                    String takelng = jobject.getString("takelng");
                    result = new String[]{"1", dep, dest, depnote, destnote, orilat, orilng, destlat, destlng, dist,
                            price, driver, drivercontact, storder, ratings, driverlat, driverlng, takelat, takelng};
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
                String dep = result[1];
                String dest = result[2];
                String depnote = result[3];
                String destnote = result[4];
                String orilat = result[5];
                String orilng = result[6];
                String destlat = result[7];
                String destlng = result[8];
                String dist = result[9];
                String price = result[10];
                String driver = result[11];
                String drivercontact = result[12];
                String storder = result[13];
                String ratings = result[14];
                String driverlat = result[15];
                String driverlng = result[16];
                String takelat = result[17];
                String takelng = result[18];
                ShowDetail(dep, dest, depnote, destnote, orilat, orilng, destlat, destlng, dist, price,
                        driver, drivercontact, storder, ratings, driverlat, driverlng, takelat, takelng);

                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                //Toast(2,"RC:"+rc+" >> Calculation failed !");
                SnackBarMsg("Request failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                //Toast(1,"RC:"+rc+" >> Calculation failed !");
                SnackBarMsg("Request failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowDetail(String dep, String xdest, String depnote, String destnote, String orilat, String orilng, String destlat, String destlng,
                            String dist, String price, String driver, final String drivercontact, String storder, String ratings,
                            String driverlat, String driverlng, String takelat, String takelng){
        txtori.setText(dep);
        txtdest.setText(xdest);
        tdistance.setText("Distance : " + dist);
        tprice.setText("Price : " +price);

        tdriver.setText(driver);
        ttiket.setText("TIKET ID. "+tiket);
        tcontact.setText(drivercontact);

        if(storder.equals("0") || storder.equals("9")){

            if(storder.equals("0")){
                tstatus.setText("Bookings");
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowAlert(0, tiket);
                    }
                });
            }else{
                SnackBarMsg("Order has been cancelled, please re-order.");
            }
            layoutprogress.setVisibility(View.GONE);
            paramdistancelocation=1;
            LatLng orilatlng = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
            LatLng destlatlng = new LatLng(Double.parseDouble(destlat), Double.parseDouble(destlng));
            placeMarkerOnMap(orilatlng, destlatlng);
        }
        if(storder.equals("1")){
            tstatus.setText("In Progress");
            oprLoc = new LatLng(Double.parseDouble(driverlat), Double.parseDouble(driverlng));
            origin = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
            dest = new LatLng(Double.parseDouble(destlat), Double.parseDouble(destlng));
            LatLng takelatlng = new LatLng(Double.parseDouble(takelat), Double.parseDouble(takelng));
            placeMarkerOnMapProgress(origin, dest, oprLoc, driver, takelatlng);
        }
        if(storder.equals("2")){
            tstatus.setText("Completed");
            paramdistancelocation=1;
            LatLng orilatlng = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
            LatLng destlatlng = new LatLng(Double.parseDouble(destlat), Double.parseDouble(destlng));
            placeMarkerOnMap(orilatlng, destlatlng);
            layoutrating.setVisibility(View.VISIBLE);
            if(Integer.parseInt(ratings) > 0){
                ratingBar.setIsIndicator(true);
            }
            ratingBar.setRating(Float.parseFloat(ratings));
        }

        layoutsubmit.setVisibility(View.VISIBLE);

        imgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String PhoneNum = drivercontact;
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+Uri.encode(PhoneNum.trim())));
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);
            }
        });

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                int rat =(int)(Math.round(rating));
                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host+"getratings";
                if (isNetworkAvailable() == true) {
                    TaskGetRatings MyTask = new TaskGetRatings();
                    MyTask.execute(token,tiket,rat+"");
                }
            }
        });
    }

    private void CancelOrder(String tiket){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"csrcancelorder";
        if (isNetworkAvailable() == true) {
            TaskCancelOrder MyTask = new TaskCancelOrder();
            MyTask.execute(tiket);
        }
    }

    public class TaskCancelOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetail.this, android.R.style.Theme_Translucent);
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
                String tiket = value[0];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("tiket",tiket));
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
                if(HistoryBooking.mylist != null) updateItemListView(listindex);
                btnCancel.setVisibility(View.GONE);
                SnackBarMsg("Order has been cancelled.");
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Cancel order failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Cancel order failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void updateItemListView(int index){
        View v = HistoryBooking.mylist.getChildAt(index - HistoryBooking.mylist.getFirstVisiblePosition());

        if(v == null) return;

        TextView desc = (TextView) v.findViewById(R.id.tdes);
        TextView status = (TextView) v.findViewById(R.id.tstatus);
        desc.setPaintFlags(desc.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        status.setVisibility(View.VISIBLE);
    }

    public class TaskGetRatings extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetail.this, android.R.style.Theme_Translucent);
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
                String tiket = value[1];
                String ratings = value[2];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("tiket",tiket));
                post.add(new BasicNameValuePair("ratings",ratings));
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
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Ratings failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Ratings failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private Bitmap icons(int param, int pid){
        int height = 40;
        int width = 40;
        BitmapDrawable bitmapdraw=null;

        if(param==1) {
            if (pid == 0) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.motornura);}
            if (pid == 1) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_car);}
            if (pid == 2) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_boat);}
            if (pid == 3) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_send);}
            if (pid == 4) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_clean);}
            if (pid == 5) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_sembako);}
            if (pid == 6) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_tick);}
            if (pid == 7) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_towing);}
        }else {
            if (pid == 0) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.motornura);}
            if (pid == 1) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_car);}
            if (pid == 2) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_boat);}
            if (pid == 3) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_send);}
            if (pid == 4) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_clean);}
            if (pid == 5) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_sembako);}
            if (pid == 6) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_tick);}
            if (pid == 7) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_towing);}
        }

        if(pid==101){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_origin);}
        if(pid==102){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_destination);}
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

    protected void placeMarkerOnMap(LatLng location1, LatLng location2) {
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
        }

        origin = location1;
        dest = location2;

        if(origin!=null && dest!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));

                LatLng[] point_new = new LatLng[2];
                point_new[0] = origin;
                point_new[1] = dest;
                for (int i = 0; i < point_new.length; i++) {
                    markerPoints.add(point_new[i]);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point_new[i]);

                    if (i == 0) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title("Departure");
                        options.snippet(txtori.getText().toString());
                    } else if (i == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Destination");
                        options.snippet(txtdest.getText().toString());
                    }
                    mMap.addMarker(options);

                    if(markerPoints.size() >= 2){
                        //LatLng originLatLng = markerPoints.get(0);
                        //LatLng destLatLng = markerPoints.get(1);
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);
                        //String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }
                }
            }
        }
    }

    protected void placeMarkerOnMapProgress(LatLng location1, LatLng location2, LatLng location3, String driver, LatLng oprtakelatlng) {
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
        }

        origin = location1;
        dest = location2;
        //oprLoc = location3; //=> location driver terupdate
        oprLoc = oprtakelatlng;

        if(oprLoc!=null && origin!=null && dest!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));

                LatLng[] point_new = new LatLng[3];
                point_new[0] = origin;
                point_new[1] = dest;
                point_new[2] = oprLoc;
                for (int i = 0; i < point_new.length; i++) {
                    markerPoints.add(point_new[i]);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point_new[i]);

                    if (i == 0) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title("Departure");
                        options.snippet(txtori.getText().toString());
                    } else if (i == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Destination");
                        options.snippet(txtdest.getText().toString());
                    } else if (i == 2) {
                        options.icon(BitmapDescriptorFactory.fromBitmap(icons(0, pid)));
                        options.title("Driver position when taking orders");
                        options.snippet(driver);
                    }

                    mMap.addMarker(options);

                    if(markerPoints.size() >= 2){
                        //LatLng originLatLng = markerPoints.get(0);
                        //LatLng destLatLng = markerPoints.get(1);
                        // Getting URL to the Google Directions API
                        String url = getDirectionsUrl(origin, dest);
                        //String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    }

                    LoadLastOprLocation(); // Checking last position support
                }
            }
        }
    }

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
    private String downloadUrl(String strUrl) throws IOException {
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
    private class DownloadTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                Log.e("Download","Progress 1");
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
            String distance = "";
            String duration = "";

            try {
                if (result.size() < 1) {
                    Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
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
                            distance = (String) point.get("distance");
                            continue;
                        } else if (j == 1) { // Get duration from the list
                            duration = (String) point.get("duration");
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

                // Drawing polyline in the Google Map for the i-th route
                if(pid!=2) {
                    mMap.addPolyline(lineOptions);
                    if (paramdistancelocation == 0) {
                        paramdistancelocation = 1;
                        String url = getDirectionsUrl(oprLoc, origin);
                        DownloadTask downloadTask = new DownloadTask();
                        downloadTask.execute(url);
                    }
                }
            }catch (Exception e){
                Log.e("error parsing array","result not found");
                Toast(2,"Result Google Maps not found");
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    @Override
    public void onBackPressed(){
        LoadLastOprLocationStop();
        this.finish();
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
        final Snackbar snackBar = Snackbar.make(toolbar, msg, Snackbar.LENGTH_INDEFINITE);
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

    private void ShowAlert(final int param, final String id) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_alert);
        //dialog.setTitle("Custom Dialog");

        TextView title = (TextView) dialog.findViewById(R.id.titledata);
        TextView text = (TextView) dialog.findViewById(R.id.textMsg);
        Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
        Button btnDone = (Button) dialog.findViewById(R.id.btnDone);

        if (param == 0) {
            title.setText("Confirmation");
            text.setText("Do you want to cancel the orders ?");
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
                    dialog.dismiss();
                    CancelOrder(id);
                }
            });
        }
        dialog.show();
    }
}
