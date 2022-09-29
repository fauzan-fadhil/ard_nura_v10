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
import android.support.v7.widget.CardView;
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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryDetailFood extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "HALOOOOOOOOOOOOOOO" ;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    EditText txtdest, tdestdetail, tdestcontact;
    int PLACE_PICKER_REQUEST = 0;
    private TextView txttitle, tdriver, tcontact, ttiket, tstatus, trestonama, trestoalamat;
    private ImageView ictitle, imgContact, imgresto;
    private String tiket, SERVERADDR, csrid, email, tmDevice, versi;
    private int timeoutdata = 20000, listindex;
    private String MyJSON;
    private SqlHelper dbHelper;
    private CardView layoutrating, layoutprogress;
    private RatingBar ratingBar;
    private ProgressBar pbar;
    private TextView tpricepayment;
    private TextView texppayment;
    private TextView tpayment;
    private TextView tdistance;
    private Button btnCancel;
    Toolbar toolbar;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest, oprLoc;
    Double myLocLat, myLocLng;
    String distance, token;
    Snackbar snackBar;
    Activity activity;
    int pid;
    int paramdistancelocation = 0 ;

    GPSTracker gps;
    Timer myTimer;
    Marker markerPointsLastOprLoc;

    TextView slideHandleText;
    SlidingDrawer slidingDrawer;

    public static final String TAG_MENU = "menu";
    public static final String TAG_PRICE = "price";
    public static final String TAG_QTY = "qty";
    public static final String TAG_UNIT = "unit";
    public static final String TAG_TOTAL = "total";
    public static final String TAG_NOTES = "notes";
    LazyAdapterRestoMenu2 adapter;
    ArrayList<HashMap<String, String>> ListMenu = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_hisdetail_foodorder);

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

        //TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        //tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        btnCancel = (Button)findViewById(R.id.btnCancel);
        txttitle = (TextView)findViewById(R.id.txttitle);
        layoutrating = (CardView) findViewById(R.id.layoutrating);
        layoutprogress = (CardView)findViewById(R.id.layoutprogress);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        pbar = (ProgressBar) findViewById(R.id.pbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        tdestdetail = (EditText) findViewById(R.id.tdestdetail);
        tdestcontact = (EditText) findViewById(R.id.tdestcontact);
        txtdest = (EditText) findViewById(R.id.txtdest);
        disableEditText(txtdest);
        disableEditText(tdestcontact);
        disableEditText(tdestdetail);
        slideHandleText = (TextView) findViewById(R.id.slideHandleText);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);

        tpricepayment = (TextView) findViewById(R.id.tpricepayment);
        texppayment = (TextView) findViewById(R.id.texppayment);
        tpayment = (TextView) findViewById(R.id.tpayment);
        tdistance = (TextView) findViewById(R.id.tdistance);

        tdriver = (TextView)findViewById(R.id.tdriver);
        tcontact = (TextView)findViewById(R.id.tcontact);
        ttiket = (TextView)findViewById(R.id.ttiket);
        tstatus = (TextView)findViewById(R.id.tstatus);

        imgContact = (ImageView)findViewById(R.id.imgContact);
        imgresto = (ImageView)findViewById(R.id.imgresto);
        trestonama = (TextView)findViewById(R.id.trestonama);
        trestoalamat = (TextView)findViewById(R.id.trestoalamat);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");
        tiket = main.getExtras().getString("tiket");
        listindex = main.getExtras().getInt("listindex");

        txttitle.setText(" SEMBAKO");

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);

        gps = new GPSTracker(HistoryDetailFood.this);

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

    public class TaskCancelOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailFood.this, android.R.style.Theme_Translucent);
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
                SnackBarMsg("Order telah dibatalkan.");
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Order gagal dibatalkan ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Order gagal dibatalkan ! ("+rc+")");
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

    private void LoadLastOprLocation(){
        try {
            myTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Log.e("SCHEDULAR", "Load position " + tiket);

                    MyConfig config = new MyConfig();
                    String host = config.hostname(dbHelper);
                    SERVERADDR = host + "csrloadlastoprlocation";
                    if (isNetworkAvailable() == true) {
                        TaskLoadLastOprLoc MyTask = new TaskLoadLastOprLoc();
                        MyTask.execute(token, tiket);
                    }
                }
            }, 0, 10000);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void LoadLastOprLocationStop(){
        Log.e("SCHEDULAR STOP","Stoping load position ");
        myTimer.cancel();
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
                SnackBarMsg("Periksa lokasi yang didukung ! ("+rc+")");
            }else if(rc.equals("99")){
                SnackBarMsg("Periksa lokasi yang didukung ! ("+rc+")");
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
                .icon(BitmapDescriptorFactory.fromBitmap(icons(1, 50)))
                .title("Posisi driver saat ini")
                .snippet(driver));
    }

    private void RequestHistory(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"orderhistorydetailfoodcsr";
        if (isNetworkAvailable() == true) {
            TaskHistoryDetail MyTask = new TaskHistoryDetail();
            MyTask.execute(token,tiket);
        }
    }

    public class TaskHistoryDetail extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailFood.this, android.R.style.Theme_Translucent);
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
                    String depnote = jobject.getString("depnote");
                    String orilat = jobject.getString("orilat");
                    String orilng = jobject.getString("orilng");

                    String dest = jobject.getString("dest");
                    String destnote = jobject.getString("destnote");
                    String destaddcontact = jobject.getString("destaddcontact");
                    String destlat = jobject.getString("destlat");
                    String destlng = jobject.getString("destlng");

                    String distance = jobject.getString("distance");
                    String distanceprice = jobject.getString("distanceprice");
                    String totalorder = jobject.getString("totalorder");
                    String totalpayment = jobject.getString("totalpayment");

                    String driver = jobject.getString("driver");
                    String drivercontact = jobject.getString("drivercontact");
                    String storder = jobject.getString("storder");
                    String ratings = jobject.getString("ratings");
                    String driverlat = jobject.getString("driverlat");
                    String driverlng = jobject.getString("driverlng");
                    String takelat = jobject.getString("takelat");
                    String takelng = jobject.getString("takelng");
                    String imglogo = jobject.getString("imglogo");

                    result = new String[]{"1", dep, depnote, orilat, orilng, dest, destnote, destaddcontact, destlat, destlng,
                            distance, distanceprice, totalorder, totalpayment, driver, drivercontact, storder, ratings, driverlat, driverlng, imglogo,
                            takelat, takelng};
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
                String depnote = result[2];
                String orilat = result[3];
                String orilng = result[4];
                String dest = result[5];
                String destnote = result[6];
                String destaddcontact = result[7];
                String destlat = result[8];
                String destlng = result[9];
                String distance = result[10];
                String distanceprice = result[11];
                String totalorder = result[12];
                String totalpayment = result[13];
                String driver = result[14];
                String drivercontact = result[15];
                String storder = result[16];
                String ratings = result[17];
                String driverlat = result[18];
                String driverlng = result[19];
                String imglogo = result[20];
                String takelat = result[21];
                String takelng = result[22];

                ShowDetail(dep, depnote, orilat, orilng, dest, destnote, destaddcontact, destlat, destlng,
                        distance, distanceprice, totalorder, totalpayment, driver, drivercontact, storder, ratings, driverlat, driverlng, imglogo,
                        takelat, takelng);

                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                //Toast(2,"RC:"+rc+" >> Calculation failed !");
                SnackBarMsg("Gagal kalkulasi ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                //Toast(1,"RC:"+rc+" >> Calculation failed !");
                SnackBarMsg("Gagal kalkulasi! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowDetail(String dep, String depnote, String orilat, String orilng, String xdest, String destnote, String destaddcontact,
                            String destlat, String destlng, String distance, String distanceprice, String totalorder, String totalpayment,
                            String driver, final String contact, String storder, String ratings, String driverlat, String driverlng, String imglogo,
                            String takelat, String takelng){

        if(!imglogo.equals("")){
            Picasso.with(this)
                    .load(imglogo)
                    .error(R.color.grey_500)
                    //.resize(80,80)
                    .into((imgresto));
        }

        showList();

        trestonama.setText(depnote);
        trestoalamat.setText(dep);
        txtdest.setText(xdest);
        tdestdetail.setText(destnote);
        tdestcontact.setText(destaddcontact);

        tpricepayment.setText(totalorder);
        texppayment.setText(distanceprice);
        tpayment.setText(totalpayment);
        tdistance.setText("Biaya Pengiriman ("+distance+")");

        tdriver.setText(driver);
        ttiket.setText("TIKET ID. "+tiket);
        tcontact.setText(contact);

        origin = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
        dest = new LatLng(Double.parseDouble(destlat), Double.parseDouble(destlng));

        if(storder.equals("0") || storder.equals("9")){
            if(storder.equals("0")){
                tstatus.setText("Pesan");
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowAlert(0, tiket);
                    }
                });
            }else{
                SnackBarMsg("Order telah dibatalkan, silahkan order kembali.");
            }
            layoutprogress.setVisibility(View.GONE);
            paramdistancelocation=1;
            oprLoc = null;
            LatLng takelatlng = null;
            placeMarkerOnMap(oprLoc, origin, dest, takelatlng, depnote, dep, driver);
        }
        if(storder.equals("1")){
            tstatus.setText("In Progress");
            oprLoc = new LatLng(Double.parseDouble(driverlat), Double.parseDouble(driverlng));
            LatLng takelatlng = new LatLng(Double.parseDouble(takelat), Double.parseDouble(takelng));
            placeMarkerOnMap(oprLoc, origin, dest, takelatlng, depnote, dep, driver);
        }
        if(storder.equals("2")){
            tstatus.setText("Selesai");
            paramdistancelocation=1;
            oprLoc = null;
            LatLng takelatlng = null;
            placeMarkerOnMap(oprLoc, origin, dest, takelatlng, depnote, dep, driver);
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

    public class TaskGetRatings extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailFood.this, android.R.style.Theme_Translucent);
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
                SnackBarMsg("Rating gagal ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Rating gagal ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void showList(){
        NonScrollListView list = (NonScrollListView) findViewById(R.id.lv_nonscroll_list);
        LazyAdapterDetailHistoriFood adapter;
        ArrayList<HashMap<String, String>> ListSendOption = new ArrayList<HashMap<String, String>>();
        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("detail");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String menu = c.getString(TAG_MENU);
                String price = c.getString(TAG_PRICE);
                String qty = c.getString(TAG_QTY);
                String unit = c.getString(TAG_UNIT);
                String total = c.getString(TAG_TOTAL);
                String notes = c.getString(TAG_NOTES);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_MENU,menu);
                map.put(TAG_PRICE,price);
                map.put(TAG_QTY,qty);
                map.put(TAG_UNIT,unit);
                map.put(TAG_TOTAL,total);
                map.put(TAG_NOTES,notes);
                ListSendOption.add(map);
            }
            adapter = new LazyAdapterDetailHistoriFood(this, ListSendOption);
            list.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private Bitmap icons(int param, int pid){
        int height = 40;
        int width = 40;
        BitmapDrawable bitmapdraw=null;
        if(param==1) {
            if(pid == 50){bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker_ridesend);}
        }else{
            if(pid == 50){bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.marker_ridesend_grey);}
        }
        if(pid == 5){bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_dekstop_food);}
        if(pid==101){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_origin);}
        if(pid==102){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_destination);}
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

    protected void placeMarkerOnMap(LatLng oprLoc, LatLng origin, LatLng dest, LatLng oprtakelatlng, String resto, String restoalamat, String driver) {
        if (mMap != null) {
            mMap.clear();
            markerPoints.clear();
        }

        if(myLoc!=null && origin!=null && dest!=null){
            if(markerPoints.size()==0) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));

                if(oprLoc==null){
                    LatLng[] point_new = new LatLng[2];
                    point_new[0] = origin;
                    point_new[1] = dest;

                    for (int i = 0; i < point_new.length; i++) {
                        markerPoints.add(point_new[i]);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point_new[i]);

                        if (i == 0) {
                            options.icon(BitmapDescriptorFactory.fromBitmap(icons(0,5)));
                            options.title(resto);
                            options.snippet(restoalamat);
                        }else if (i == 1) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            options.title("Tujuan Konsumen");
                            options.snippet(txtdest.getText().toString());
                        }
                        mMap.addMarker(options);

                        if(markerPoints.size() >= 2){
                            String url = getDirectionsUrl(origin, dest);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                        }
                    }
                }else{
                    LatLng[] point_new = new LatLng[3];
                    //point_new[0] = oprLoc; //=> location driver terupdate
                    point_new[0] = oprtakelatlng;
                    point_new[1] = origin;
                    point_new[2] = dest;

                    for (int i = 0; i < point_new.length; i++) {
                        markerPoints.add(point_new[i]);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point_new[i]);

                        if (i == 0) {
                            options.icon(BitmapDescriptorFactory.fromBitmap(icons(0,50)));
                            options.title("Lokasi order diterima");
                            options.snippet(driver);
                        }else if (i == 1) {
                            options.icon(BitmapDescriptorFactory.fromBitmap(icons(0,5)));
                            options.title(resto);
                            options.snippet(restoalamat);
                        }else if (i == 2) {
                            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            options.title("Tujuan Konsumen");
                            options.snippet(txtdest.getText().toString());
                        }
                        mMap.addMarker(options);

                        if(markerPoints.size() >= 2){
                            String url = getDirectionsUrl(oprtakelatlng, origin);
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                        }

                        LoadLastOprLocation(); // Checking last position support
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

                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
                if(paramdistancelocation==0){
                    paramdistancelocation=1;
                    String url = getDirectionsUrl(origin,dest);
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.execute(url);
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
            title.setText("Konfirmasi");
            text.setText("Apakah anda akan membatalkan pesanan ?");
            btnClose.setText("Tidak");
            btnDone.setText("Ya");

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