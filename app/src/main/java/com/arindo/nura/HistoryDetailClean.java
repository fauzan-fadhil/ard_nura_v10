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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.Marker;
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
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by bmaxard on 26/01/2017.
 */

public class HistoryDetailClean extends AppCompatActivity implements OnMapReadyCallback {
    private String tiket, SERVERADDR, csrid, email, tmDevice, versi;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private CardView cview1, cview2, cvprice;
    private RadioButton rbtn1;
    private CheckBox cbtn1, cbtn2;
    private CardView layoutrating, layoutprogress;
    private RatingBar ratingBar;
    private Button btnCancel;
    Toolbar toolbar;
    Snackbar snackBar;
    int timeoutdata = 20000, listindex;
    int PLACE_PICKER_REQUEST = 0;
    private String MyJSON;
    EditText txtdest, tdestdetail, tdestcontact;
    private CollapsingToolbarLayout collapsingToolbarLayout = null;

    GPSTracker gps;
    Timer myTimer;
    Marker markerPointsLastOprLoc;
    private GoogleMap mMap;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest, cleanerLoc;
    Double myLocLat, myLocLng;
    String distance, token;
    TextView tprice, tdiscountpersen, tdiscount, tpayment, thour, tcleaner, tcleanername, tcontact, ttiket, tstatus;
    ImageView imgContact;
    int pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_hisdetail_cleanorder);

        //if(MainActivity.loaderActivity.isShowing()) {
        //    MainActivity.loaderActivity.dismiss();
        //}

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTextAppernce();
        collapsingToolbarLayout.setTitle("BC2 CLEAN - DETAIL");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.setTitle(null);

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        pbar = (ProgressBar) findViewById(R.id.progressbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        cview1 = (CardView) findViewById(R.id.cview1);
        cvprice = (CardView) findViewById(R.id.cvprice);
        rbtn1 = (RadioButton) findViewById(R.id.rbtn1);
        cbtn1 = (CheckBox) findViewById(R.id.cbtn1);
        cbtn2 = (CheckBox) findViewById(R.id.cbtn2);
        tdestdetail = (EditText) findViewById(R.id.tdestdetail);
        tdestcontact = (EditText) findViewById(R.id.tdestcontact);
        txtdest = (EditText) findViewById(R.id.txtdest);
        disableEditText(txtdest);
        disableEditText(tdestcontact);
        disableEditText(tdestdetail);

        tprice = (TextView) findViewById(R.id.tprice);
        tdiscountpersen = (TextView) findViewById(R.id.tdiscountpersen);
        tdiscount = (TextView) findViewById(R.id.tdiscount);
        tpayment = (TextView) findViewById(R.id.tpayment);
        thour = (TextView) findViewById(R.id.thour);
        tcleaner = (TextView) findViewById(R.id.tcleaner);

        layoutrating = (CardView) findViewById(R.id.layoutrating);
        layoutprogress = (CardView)findViewById(R.id.layoutprogress);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        tcleanername = (TextView)findViewById(R.id.tcleanername);
        tcontact = (TextView)findViewById(R.id.tcontact);
        ttiket = (TextView)findViewById(R.id.ttiket);
        tstatus = (TextView)findViewById(R.id.tstatus);
        imgContact = (ImageView)findViewById(R.id.imgContact);

        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();
        Intent main = getIntent();
        pid = main.getExtras().getInt("order");
        tiket = main.getExtras().getString("tiket");
        listindex = main.getExtras().getInt("listindex");

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        markerPointsOpr = new ArrayList<LatLng>();
        getMaps();

        RequestHistory();

        myTimer = new Timer();

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();
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
                .title("Current cleaner position")
                .snippet(driver));
    }

    private void RequestHistory(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"orderhistorydetailcleancsr";
        if (isNetworkAvailable() == true) {
            TaskHistoryDetail MyTask = new TaskHistoryDetail();
            MyTask.execute(token,tiket);
        }
    }

    public class TaskHistoryDetail extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailClean.this, android.R.style.Theme_Translucent);
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
                    String dest = jobject.getString("dest");
                    String destnote = jobject.getString("destnote");
                    String destaddcontact = jobject.getString("destaddcontact");
                    String destlat = jobject.getString("destlat");
                    String destlng = jobject.getString("destlng");

                    String tools = jobject.getString("tools");
                    String sumofhour = jobject.getString("sumofhour");
                    String sumofcleaner = jobject.getString("sumofcleaner");
                    String payment = jobject.getString("payment");
                    String discpersen = jobject.getString("discpersen");
                    String discount = jobject.getString("discount");
                    String totalpayment = jobject.getString("totalpayment");

                    String cleanername = jobject.getString("cleanername");
                    String contact = jobject.getString("contact");
                    String storder = jobject.getString("storder");
                    String ratings = jobject.getString("ratings");
                    String cleanerlat = jobject.getString("cleanerlat");
                    String cleanerlng = jobject.getString("cleanerlng");
                    String takelat = jobject.getString("takelat");
                    String takelng = jobject.getString("takelng");

                    result = new String[]{"1", dest, destnote, destaddcontact, destlat, destlng,
                            tools, sumofhour, sumofcleaner, payment, discpersen, discount, totalpayment,
                            cleanername, contact, storder, ratings, cleanerlat, cleanerlng, takelat, takelng};
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
                String dest = result[1];
                String destnote = result[2];
                String destaddcontact = result[3];
                String destlat = result[4];
                String destlng = result[5];
                String tools = result[6];
                String sumofhour = result[7];
                String sumofcleaner = result[8];
                String payment = result[9];
                String discpersen = result[10];
                String discount = result[11];
                String totalpayment = result[12];
                String cleanername = result[13];
                String contact = result[14];
                String storder = result[15];
                String ratings = result[16];
                String cleanerlat = result[17];
                String cleanerlng = result[18];
                String takelat = result[19];
                String takelng = result[20];

                ShowDetail(dest, destnote, destaddcontact, destlat, destlng,
                        tools, sumofhour, sumofcleaner, payment, discpersen, discount, totalpayment,
                        cleanername, contact, storder, ratings, cleanerlat, cleanerlng, takelat, takelng);

                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Request failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Request failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowDetail(String xdest, String destnote, String destaddcontact, String destlat, String destlng,
                            String tools, String sumofhour, String sumofcleaner, String payment, String discpersen, String discount, String totalpayment,
                            String cleanername, final String contact, String storder, String ratings, String cleanerlat, String cleanerlng, String takelat, String takelng){

        txtdest.setText(xdest);
        tdestdetail.setText(destnote);
        tdestcontact.setText(destaddcontact);

        rbtn1.setText(tools);

        tprice.setText(payment);
        tdiscountpersen.setText(discpersen);
        tdiscount.setText(discount);
        tpayment.setText(totalpayment);
        thour.setText(sumofhour);
        tcleaner.setText(sumofcleaner);

        tcleanername.setText(cleanername);
        ttiket.setText("TIKET ID. "+tiket);
        tcontact.setText(contact);

        dest = new LatLng(Double.parseDouble(destlat), Double.parseDouble(destlng));

        if(storder.equals("0") || storder.equals("9") ){
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
                SnackBarMsg("Order has been canceled, please re-order.");
            }

            //paramdistancelocation=1;
            cleanerLoc = null;
            placeMarkerOnMap(cleanerLoc, dest, null, cleanername);
        }
        if(storder.equals("1")){
            tstatus.setText("In Progress");
            cleanerLoc = new LatLng(Double.parseDouble(cleanerlat), Double.parseDouble(cleanerlng));
            LatLng takelatlng = new LatLng(Double.parseDouble(takelat), Double.parseDouble(takelng));
            placeMarkerOnMap(cleanerLoc, dest, takelatlng, cleanername);
            layoutprogress.setVisibility(View.VISIBLE);

            LoadLastOprLocation(); // Checking last position support
        }
        if(storder.equals("2")){
            tstatus.setText("Completed");
            //paramdistancelocation=1;
            cleanerLoc = null;
            LatLng takelatlng = new LatLng(Double.parseDouble(takelat), Double.parseDouble(takelng));
            placeMarkerOnMap(cleanerLoc, dest, takelatlng, cleanername);
            layoutprogress.setVisibility(View.VISIBLE);
            layoutrating.setVisibility(View.VISIBLE);
            if(Integer.parseInt(ratings) > 0){
                ratingBar.setIsIndicator(true);
            }
            ratingBar.setRating(Float.parseFloat(ratings));
        }

        imgContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String PhoneNum = contact;
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:"+ Uri.encode(PhoneNum.trim())));
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
        final Dialog dialog = new Dialog(HistoryDetailClean.this, android.R.style.Theme_Translucent);
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
        final Dialog dialog = new Dialog(HistoryDetailClean.this, android.R.style.Theme_Translucent);
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

    private String currencyformat(double value){
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(otherSymbols);

        return String.valueOf(df.format(value));
    }

    private void getMaps() {
        //make screen stays active
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get mark potiton
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);

        if (mapFragment != null && mapFragment.getView().findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            rlp.setMargins(0, 180, 180, 0);
        }

        gps = new GPSTracker(HistoryDetailClean.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            myLocLat = gps.getLatitude();
            myLocLng = gps.getLongitude();
            myLoc = new LatLng(myLocLat,myLocLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
        }else{
            gps.showSettingsAlert();
        }

        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);
    }

    private Bitmap icons(int param, int pid){
        int height = 40;
        int width = 40;
        BitmapDrawable bitmapdraw=null;

        if(param==1) {
            if (pid == 0) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_ride);}
            if (pid == 1) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_car);}
            if (pid == 2) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_boat);}
            if (pid == 3) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_send);}
            if (pid == 4) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_clean);}
            if (pid == 5) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_food);}
            if (pid == 6) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_tick);}
            if (pid == 7) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_towing);}
        }else {
            if (pid == 0) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_ride);}
            if (pid == 1) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_car);}
            if (pid == 2) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_boat);}
            if (pid == 3) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_send);}
            if (pid == 4) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_clean);}
            if (pid == 5) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_food);}
            if (pid == 6) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_tick);}
            if (pid == 7) {bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_grey_towing);}
        }

        if(pid==101){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_origin);}
        if(pid==102){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_destination);}
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
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

    protected void showList(String id, String title, String desc, String imagetitleurl){
        collapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#333333"));
        collapsingToolbarLayout.setTitle(title);

    }

    protected void placeMarkerOnMap(LatLng cleanerLoc, LatLng dest, LatLng cleantakelatlng, String cleanername) {
        if (mMap != null) {
            mMap.clear();
            markerPoints.clear();
        }

        if(myLoc!=null && dest!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 13));

                //if(cleanerLoc==null){
                if(cleantakelatlng==null){
                    LatLng[] point_new = new LatLng[1];
                    point_new[0] = dest;

                    for (int i = 0; i < point_new.length; i++) {
                        markerPoints.add(point_new[i]);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point_new[i]);

                        if (i == 0) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            options.title("Your location");
                            options.snippet(txtdest.getText().toString());
                        }
                        mMap.addMarker(options);
                    }
                }else{
                    LatLng[] point_new = new LatLng[2];
                    //point_new[0] = cleanerLoc;
                    point_new[0] = cleantakelatlng;
                    point_new[1] = dest;

                    for (int i = 0; i < point_new.length; i++) {
                        markerPoints.add(point_new[i]);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point_new[i]);

                        if (i == 0) {
                            options.icon(BitmapDescriptorFactory.fromBitmap(icons(0,4)));
                            options.title("Cleaner position when taking orders");
                            options.snippet(cleanername);
                        }else if (i == 1) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            options.title("Your location");
                            options.snippet(txtdest.getText().toString());
                        }
                        mMap.addMarker(options);

                        if(markerPoints.size() >= 2){
                            String url = getDirectionsUrl(cleantakelatlng, dest);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                        }
                    }
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
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
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

    private class DownloadTask extends AsyncTask<String, Void, String>{
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

                //CalculatServer(distance);
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }catch (Exception e){
                Log.e("error parsing array","result not found");
                SnackBarMsg("Result Google Maps not found");
            }
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
