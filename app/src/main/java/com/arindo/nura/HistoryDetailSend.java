package com.arindo.nura;

/**
 * Created by bmaxard on 10/01/2017.
 */
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryDetailSend extends AppCompatActivity implements OnMapReadyCallback {
    private int pid, optionid;
    private static final String TAG = "HALOOOOOOOOOOOOOOO" ;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private GoogleMap mMap;
    EditText txtori, toridetail, toricontact, tdetailitem;
    int PLACE_PICKER_REQUEST = 0;
    private TextView txttitle, tdistance, tprice, tdriver, tcontact, ttiket, tstatus;
    private ImageView ictitle, imgContact;
    private Button btnOrder, btnCancel, addDest;
    private String SERVERADDR, csrid, email, tmDevice, versi;
    private int timeoutdata = 20000, listindex;
    private String MyJSON, MyJSONObjectDetail, JSONObjectOpr;
    private SqlHelper dbHelper;
    private CardView layoutrating, layoutprogress;
    private TableLayout tblPrice;
    private ProgressBar pbar;
    private LayoutInflater inflater;
    private EditText txtdestresult;
    private TextView txtdestlatresult;
    private TextView txtdestlngresult;
    private TextView tnumpickernegative, tnumpickerpositive;
    private TextView tshipperprice;
    private RatingBar ratingBar;
    private int tshippernum = 1;
    private String shipperprice;
    private int distanceid, priceid;
    private CheckBox cbLoadService;
    Toolbar toolbar;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest;
    Double myLocLat, myLocLng;
    String distance, RCFROMSERVER=null, MSGFROMSERVER=null;
    String distancetotal = "0";
    Snackbar snackBar;
    String tiket, token;

    GPSTracker gps;
    Timer myTimer;
    Marker markerPointsLastOprLoc;

    CustomAutoCompleteTextView mact;
    ArrayAdapter<String> adapter;
    //String[] item = {"Aceh","Pekanbaru","Palembang","Pekalongan"};

    String PLACE;
    String googleURL;

    TextView slideHandleText;
    SlidingDrawer slidingDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_hisdetail_sendorder);

        if(MainActivity.loaderActivity.isShowing()) {
            MainActivity.loaderActivity.dismiss();
        }

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

        btnOrder = (Button)findViewById(R.id.btnOrder);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        addDest = (Button)findViewById(R.id.addDest);
        txttitle = (TextView)findViewById(R.id.txttitle);
        tdistance = (TextView)findViewById(R.id.tdistance);
        tprice = (TextView)findViewById(R.id.tprice);
        tdriver = (TextView)findViewById(R.id.tdriver);
        tcontact = (TextView)findViewById(R.id.tcontact);
        ttiket = (TextView)findViewById(R.id.ttiket);
        tstatus = (TextView)findViewById(R.id.tstatus);
        ictitle = (ImageView)findViewById(R.id.ictitle);
        imgContact = (ImageView)findViewById(R.id.imgContact);
        layoutprogress = (CardView) findViewById(R.id.layoutprogress);
        layoutrating = (CardView) findViewById(R.id.layoutrating);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        tblPrice = (TableLayout) findViewById(R.id.tblPrice);
        pbar = (ProgressBar) findViewById(R.id.pbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        tnumpickernegative = (TextView)findViewById(R.id.tnumpickernegative);
        tnumpickerpositive = (TextView)findViewById(R.id.tnumpickerpositive);
        tshipperprice = (TextView)findViewById(R.id.tshipperprice);
        cbLoadService = (CheckBox) findViewById(R.id.cbLoadService);
        toridetail = (EditText) findViewById(R.id.toridetail);
        toricontact = (EditText) findViewById(R.id.toricontact);
        txtori = (EditText) findViewById(R.id.txtori);
        tdetailitem = (EditText) findViewById(R.id.tdetailitem);
        disableEditText(txtori);
        disableEditText(toridetail);
        disableEditText(toricontact);
        disableEditText(tdetailitem);
        cbLoadService.setClickable(false);
        slideHandleText = (TextView) findViewById(R.id.slideHandleText);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
        //tambahan versi 10 os//
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");
        token = main.getExtras().getString("token");
        tiket = main.getExtras().getString("tiket");
        listindex = main.getExtras().getInt("listindex");

        ictitle.setImageResource(R.drawable.ic_dekstop_send);
        txttitle.setText("BC2 SEND");

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        markerPointsOpr = new ArrayList<LatLng>();
        getMaps();

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

        //addRowDestination();
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

        gps = new GPSTracker(HistoryDetailSend.this);

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
                .icon(BitmapDescriptorFactory.fromBitmap(icons(1, 3)))
                .title("Current Sender position")
                .snippet(driver));
    }

    private void RequestHistory(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"orderhistorydetailsendcsr";
        if (isNetworkAvailable() == true) {
            TaskHistoryDetail MyTask = new TaskHistoryDetail();
            MyTask.execute(token,tiket);
        }
    }

    public class TaskHistoryDetail extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailSend.this, android.R.style.Theme_Translucent);
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
                    String depaddcontact = jobject.getString("depaddcontact");
                    String itemdesc = jobject.getString("itemdesc");
                    String orilat = jobject.getString("orilat");
                    String orilng = jobject.getString("orilng");
                    String dist = jobject.getString("distance");
                    String price = jobject.getString("price");
                    String driver = jobject.getString("driver");
                    String drivercontact = jobject.getString("drivercontact");
                    String storder = jobject.getString("storder");
                    String ratings = jobject.getString("ratings");
                    String driverlat = jobject.getString("driverlat");
                    String driverlng = jobject.getString("driverlng");
                    String shippernum = jobject.getString("shippernum");
                    String shipperprice = jobject.getString("shipperprice");
                    String pidoption = jobject.getString("pidoption");
                    String takelat = jobject.getString("takelat");
                    String takelng = jobject.getString("takelng");
                    result = new String[]{"1", dep, depnote, depaddcontact, itemdesc, orilat, orilng, dist,
                            price, driver, drivercontact, storder, ratings, driverlat, driverlng, shippernum, shipperprice, pidoption, takelat, takelng};
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
                String depaddcontact = result[3];
                String itemdesc = result[4];
                String orilat = result[5];
                String orilng = result[6];
                String dist = result[7];
                String price = result[8];
                String driver = result[9];
                String drivercontact = result[10];
                String storder = result[11];
                String ratings = result[12];
                String driverlat = result[13];
                String driverlng = result[14];
                String shippernum = result[15];
                String shipperprice = result[16];
                String pidoption = result[17];
                String takelat = result[18];
                String takelng = result[19];
                ShowDetail(dep, depnote, depaddcontact, itemdesc, orilat, orilng, dist, price, driver, drivercontact,
                        storder, ratings, driverlat, driverlng, shippernum, shipperprice, pidoption, takelat, takelng);

                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                //Toast(2,"RC:"+rc+" >> Calculation failed !");
                SnackBarMsg("Calculation failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                //Toast(1,"RC:"+rc+" >> Calculation failed !");
                SnackBarMsg("Calculation failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowDetail(String dep, String depnote, String depaddcontact, String itemdesc, String orilat, String orilng,
                            String dist, String price, String driver, final String drivercontact, String storder, String ratings,
                            String driverlat, String driverlng, String shippernum, String shipperprice, String pidoption, String takelat, String takelng){
        if (pidoption.equals("3.1")) {
            txttitle.setText("B2 SEND - MOTORCYCLE");
        }else if (pidoption.equals("3.2")) {
            txttitle.setText("B2 SEND - PICKUP BAK");
        }else if (pidoption.equals("3.3")) {
            txttitle.setText("B2 SEND - PICKUP BOX");
        }else if (pidoption.equals("3.4")) {
            txttitle.setText("B2 SEND - ENGKEL BAK");
        }else if (pidoption.equals("3.5")) {
            txttitle.setText("B2 SEND - ENGKEL BOX");
        }

        txtori.setText(dep);
        toridetail.setText(depnote);
        toricontact.setText(depaddcontact);

        tdistance.setText("Distance : " + dist);
        tprice.setText("Price : " +price);

        tdetailitem.setText(itemdesc);
        if(!shippernum.equals("")) {
            cbLoadService.setText(shippernum + " Additional Shipper");
            cbLoadService.setChecked(true);
        }else{
            cbLoadService.setText("No Additional Shipper");
        }
        tblPrice.setVisibility(View.VISIBLE);

        tdriver.setText(driver);
        ttiket.setText("TIKET ID. "+tiket);
        tcontact.setText(drivercontact);

        JSONArray list = null;
        ArrayList<HashMap<String, String>> detailList;
        detailList = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            list = jsonObj.getJSONArray("detail");
            int jml=0;
            for(int i=0;i<list.length();i++){
                JSONObject c = list.getJSONObject(i);
                String title = c.getString("title");
                String dest = c.getString("dest");
                String destnote = c.getString("destnote");
                String destaddcontact = c.getString("destaddcontact");
                String destlat = c.getString("destlat");
                String destlng = c.getString("destlng");
                addRowDestination(title, dest,destnote,destaddcontact,destlat,destlng);
                jml++;
            }

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

                layoutprogress.setVisibility(View.GONE);
                LatLng orilatlng = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
                getArrayLocationToMarkers(orilatlng,null,null,driver);
            }

            if(storder.equals("1")){
                tstatus.setText("In Progress");
                LatLng orilatlng = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
                LatLng driverlatlng = new LatLng(Double.parseDouble(driverlat), Double.parseDouble(driverlng));
                LatLng takelatlng = new LatLng(Double.parseDouble(takelat), Double.parseDouble(takelng));
                getArrayLocationToMarkers(orilatlng,driverlatlng,takelatlng,driver);
            }
            if(storder.equals("2")){
                tstatus.setText("Completed");
                LatLng orilatlng = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));
                getArrayLocationToMarkers(orilatlng,null,null,driver);
                layoutrating.setVisibility(View.VISIBLE);
                if(Integer.parseInt(ratings) > 0){
                    ratingBar.setIsIndicator(true);
                }
                ratingBar.setRating(Float.parseFloat(ratings));
            }
        } catch (JSONException e) {
            //Log.e("Error","show detail");
            e.printStackTrace();
        }

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
        final Dialog dialog = new Dialog(HistoryDetailSend.this, android.R.style.Theme_Translucent);
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
        final Dialog dialog = new Dialog(HistoryDetailSend.this, android.R.style.Theme_Translucent);
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
                            Log.e("Distance ",distance);
                            distancetotal = (Integer.parseInt(distance) + Integer.parseInt(distancetotal))+"";
                            Log.e("Distance Total",distancetotal);
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

                //CalculatServer(distancetotal, distance, distanceid, priceid);
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }catch (Exception e){
                Log.e("error parsing array","result not found");
                Toast(2,"Result Google Maps not found");
            }
        }
    }

    private void CalculatServer(String distancetotal, String distance, int distanceid, int priceid){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"excalculate";
        if (isNetworkAvailable() == true) {
            TaskCalcutate MyTask = new TaskCalcutate();
            MyTask.execute("1","bn",distancetotal,pid+"",distance, distanceid+"", priceid+"");
        }
    }

    private String ExPrice(String distance, Double expriceperkm, int minprice, Double minkm){
        String price = null;
        try{
            String[] x = distance.split(" ");
            String a = x[0];
            String b = x[1];
            Double jr = 0.0;
            if(b.equals("m") || b.equals("M")) {
                jr = (Double.parseDouble(a) * expriceperkm) / 1000;
            }else{
                jr = Double.parseDouble(a) * expriceperkm;
            }

            if(jr.intValue() <= minprice){
                price = minprice+"";
            }else{
                Double pr = ((Double.parseDouble(a) - minkm) * expriceperkm) + minprice;
                price = pr.intValue()+"";
            }
        }catch (Exception e){
            //
        }
        return price;
    }

    private String moneyFormat(double val) {
        DecimalFormat formatter = new DecimalFormat("#,###,###.##");
        return formatter.format(val);
    }

    public class TaskOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailSend.this, android.R.style.Theme_Translucent);
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
                String country = value[1];
                String originlat = value[2];
                String originlng = value[3];
                String departure = value[4];
                String departnote = value[5];
                String departaddcontact = value[6];
                String dest = value[7];
                String destlat = value[8];
                String destlng = value[9];
                String pid = value[10];
                String addshipper = value[11];
                String shipperprice = value[12];
                String detail = value[13];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("country",country));
                post.add(new BasicNameValuePair("originlat",originlat));
                post.add(new BasicNameValuePair("originlng",originlng));
                post.add(new BasicNameValuePair("departure",departure));
                post.add(new BasicNameValuePair("departnote",departnote));
                post.add(new BasicNameValuePair("departaddcontact",departaddcontact));
                post.add(new BasicNameValuePair("destination",dest));
                post.add(new BasicNameValuePair("destlat",destlat));
                post.add(new BasicNameValuePair("destlng",destlng));
                post.add(new BasicNameValuePair("pid",pid));
                post.add(new BasicNameValuePair("optionid",optionid+""));
                post.add(new BasicNameValuePair("arroprid",JSONObjectOpr));
                post.add(new BasicNameValuePair("addshipper",addshipper));
                post.add(new BasicNameValuePair("shipperprice",shipperprice));
                post.add(new BasicNameValuePair("detail",detail));
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
                SnackBarMsg("Order failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Order failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowHistory(){
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

        snackBar.setAction("Close", new View.OnClickListener() {
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
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String addressText = place.getName().toString();
                addressText += "\n" + place.getAddress().toString();
                if(PLACE_PICKER_REQUEST==1) {
                    txtori.setText(place.getAddress().toString());
                    txtori.setTextColor(Color.parseColor("#000000"));
                    origin = place.getLatLng();
                    //placeMarkerOnMap(place.getLatLng());

                }else if(PLACE_PICKER_REQUEST==2) {
                    txtdestresult.setText(place.getAddress().toString());
                    txtdestresult.setTextColor(Color.parseColor("#000000"));
                    txtdestlatresult.setText(place.getLatLng().latitude+"");
                    txtdestlngresult.setText(place.getLatLng().longitude+"");
                    //placeMarkerOnMap(place.getLatLng());

                }
            }
        }
    }

    public class TaskCalcutate extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(HistoryDetailSend.this, android.R.style.Theme_Translucent);
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
                String country = value[1];
                String distancetotal = value[2];
                String pid = value[3];
                String distance = value[4];
                String distanceid = value[5];
                String priceid = value[6];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("country",country));
                post.add(new BasicNameValuePair("distance",distance));
                post.add(new BasicNameValuePair("distancetotal",distancetotal));
                post.add(new BasicNameValuePair("pid",pid));
                post.add(new BasicNameValuePair("optionid",optionid+""));
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
                    String distancetexttotal = jobject.getString("distancetexttotal");
                    String distancevaluetotal = jobject.getString("distancevaluetotal");
                    String tpricetotal = jobject.getString("tpricetotal");
                    String price = jobject.getString("price");
                    String distancevalue = jobject.getString("distancevalue");
                    String priceshipper = jobject.getString("priceshipper");
                    result = new String[]{"1", distancetexttotal, distancevaluetotal, tpricetotal, price, distancevalue, priceshipper, distanceid, priceid};
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
                String distancetexttotal = result[1];
                String distancevaluetotal = result[2];
                String tpricetotal = result[3];
                String price = result[4];
                String distancevalue = result[5];
                shipperprice = result[6];
                String distanceid = result[7];
                String priceid = result[8];

                dialog.dismiss();

                try {
                    final TableLayout tableDestination = (TableLayout) findViewById(R.id.tableDestination);
                    for (int i = 0; i < tableDestination.getChildCount(); i++) {
                        View child = tableDestination.getChildAt(i);
                        TextView txtdistance = (TextView)child.findViewById(Integer.parseInt(distanceid));
                        TextView txtprice = (TextView)child.findViewById(Integer.parseInt(priceid));
                        if(txtdistance!=null && txtprice!=null) {
                            txtdistance.setText(distancevalue);
                            txtprice.setText(price);
                        }
                    }

                    tblPrice.setVisibility(View.VISIBLE);
                    tdistance.setText("Distance : " + distancetexttotal);
                    tprice.setText("Price : " +tpricetotal);
                    tshipperprice.setText("+ IDR. "+shipperprice+" per shipper");
                    //btnOrder.setEnabled(true);
                    //MyJSONObjectDetail = DetailDataOrder("Distance",price,distancevalue,"km");
                    //Log.e("JSON",MyJSONObjectDetail);
                } catch (Exception e) {
                    Log.e("Error","Array Distance");
                }

            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Calculation failed ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Calculation failed ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private String DetailDataOrder(){
        final TableLayout tableDestination = (TableLayout)findViewById(R.id.tableDestination);

        JSONArray destarr = new JSONArray();
        JSONObject obj;
        try {
            for (int i = 0; i < tableDestination.getChildCount(); i++) {
                View child = tableDestination.getChildAt(i);
                TextView desttitle = (TextView) child.findViewById(R.id.desttitle);
                EditText txtdestdetail = (EditText) child.findViewById(R.id.tdestdetail);
                EditText txtdestcontact = (EditText) child.findViewById(R.id.tdestcontact);
                EditText txtdest = (EditText) child.findViewById(100 + i);
                TextView txtdestlat = (TextView) child.findViewById(200 + i);
                TextView txtdestlng = (TextView) child.findViewById(300 + i);
                TextView txtdistance = (TextView) child.findViewById(400 + i);
                TextView txtprice = (TextView) child.findViewById(500 + i);

                if (txtdest != null) {
                    String ele1 = desttitle.getText().toString();
                    String ele2 = txtdest.getText().toString();
                    String ele3 = txtdestdetail.getText().toString();
                    String ele4 = txtdestcontact.getText().toString();
                    String ele5 = txtdestlat.getText().toString();
                    String ele6 = txtdestlng.getText().toString();
                    String ele7 = txtdistance.getText().toString();
                    String ele8 = txtprice.getText().toString();
                    if(ele3!=null && ele4!=null) {
                        obj = new JSONObject();
                        obj.put("itemid", "0");
                        obj.put("item", ele1);
                        obj.put("dest", ele2);
                        obj.put("destdetail", ele3);
                        obj.put("destcontact", ele4);
                        obj.put("deslat", ele5);
                        obj.put("deslng", ele6);
                        obj.put("qty", ele7);
                        obj.put("price", ele8);
                        obj.put("unit", "km");
                        destarr.put(obj);
                    }
                }
            }
            JSONObject object = new JSONObject();
            JSONObject JSONDestLoc = object.put("detail",destarr);
            return JSONDestLoc+"";
        }catch (Exception e){
            return "0";
        }
    }

    private void getArrayLocationToMarkers(LatLng origin, LatLng driverlatlng, LatLng drivertakelatlng, String driver)throws JSONException{
        distancetotal = "0";
        JSONArray driverarr = new JSONArray();
        JSONObject obj3;
        //if (driverlatlng != null) {
        if (drivertakelatlng != null) {
            String ele1 = "Sender position when taking orders";
            String ele2 = driver;
            //String ele3 = driverlatlng.latitude+""; //=> update lokasi driver
            //String ele4 = driverlatlng.longitude+""; //=> update lokasi driver
            String ele3 = drivertakelatlng.latitude+"";
            String ele4 = drivertakelatlng.longitude+"";

            obj3 = new JSONObject();
            obj3.put("title",ele1);
            obj3.put("sender", ele2);
            obj3.put("sendlat", ele3);
            obj3.put("sendlng", ele4);
            driverarr.put(obj3);

            LoadLastOprLocation(); // Checking last position support
        }
        JSONObject object3 = new JSONObject();
        JSONObject JSONDriverLoc = object3.put("JSONDRIVER",driverarr);

        JSONArray oriarr = new JSONArray();
        JSONObject obj2;
        if (origin != null) {
            String ele1 = "DEPARTURE";
            String ele2 = txtori.getText().toString();
            String ele3 = origin.latitude+"";
            String ele4 = origin.longitude+"";

            obj2 = new JSONObject();
            obj2.put("title",ele1);
            obj2.put("dep", ele2);
            obj2.put("orilat", ele3);
            obj2.put("orilng", ele4);
            oriarr.put(obj2);
        }
        JSONObject object2 = new JSONObject();
        JSONObject JSONOriLoc = object2.put("JSONORI",oriarr);

        final TableLayout tableDestination = (TableLayout) findViewById(R.id.tableDestination);
        JSONArray destarr = new JSONArray();
        JSONObject obj;
        try {
            for (int i = 0; i < tableDestination.getChildCount(); i++) {
                View child = tableDestination.getChildAt(i);
                TextView desttitle = (TextView) child.findViewById(R.id.desttitle);
                EditText txtdest = (EditText) child.findViewById(R.id.txtdest);
                TextView txtdestlat = (TextView) child.findViewById(R.id.txtdestlat);
                TextView txtdestlng = (TextView) child.findViewById(R.id.txtdestlng);

                if (txtdest != null) {
                    String ele1 = desttitle.getText().toString();
                    String ele2 = txtdest.getText().toString();
                    String ele3 = txtdestlat.getText().toString();
                    String ele4 = txtdestlng.getText().toString();
                    if(ele3!=null && ele4!=null) {
                        obj = new JSONObject();
                        obj.put("title", ele1);
                        obj.put("dest", ele2);
                        obj.put("deslat", ele3);
                        obj.put("deslng", ele4);
                        //obj.put("distanceid", 400 + i);
                        //obj.put("priceid", 500 + i);
                        destarr.put(obj);
                    }
                }
            }
        }catch (Exception e){

        }
        JSONObject object = new JSONObject();
        JSONObject JSONDestLoc = object.put("JSONDEST",destarr);

        //Log.e("JSON ORIGIN : ", JSONOriLoc.toString());
        //Log.e("JSON DESTINATION : ", JSONDestLoc.toString());

        placeMarkerOnMap(JSONDriverLoc.toString(), JSONOriLoc.toString(), JSONDestLoc.toString());
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

    protected void placeMarkerOnMap(String OBJDriver, String OBJOri, String OBJDest) {
        LatLng driverlatlng = null;
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
        }

        JSONObject jsonObjDriver = null;
        JSONArray driverarr = null;
        try {
            jsonObjDriver = new JSONObject(OBJDriver);
            driverarr = jsonObjDriver.getJSONArray("JSONDRIVER");
            if(driverarr.length()>0) {
                for (int i = 0; i < driverarr.length(); i++) {
                    JSONObject dtdriver = driverarr.getJSONObject(i);
                    String sendtitle = dtdriver.getString("title");
                    String sender = dtdriver.getString("sender");
                    String sendlat = dtdriver.getString("sendlat");
                    String sendlng = dtdriver.getString("sendlng");
                    //Log.e("ORI ARRAY : ", orititle+", "+dep+", "+orilat+", "+orilng);

                    driverlatlng = new LatLng(Double.parseDouble(sendlat), Double.parseDouble(sendlng));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverlatlng, 13));
                    //markerPoints.add(origin);
                    MarkerOptions options = new MarkerOptions();
                    options.position(driverlatlng);
                    options.icon(BitmapDescriptorFactory.fromBitmap(icons(0,3)));
                    options.title(sendtitle);
                    options.snippet(sender);
                    mMap.addMarker(options);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObjOri = null;
        JSONArray oriarr = null;
        try {
            jsonObjOri = new JSONObject(OBJOri);
            oriarr = jsonObjOri.getJSONArray("JSONORI");
            if(oriarr.length()>0) {
                for (int i = 0; i < oriarr.length(); i++) {
                    JSONObject dtori = oriarr.getJSONObject(i);
                    String orititle = dtori.getString("title");
                    String dep = dtori.getString("dep");
                    String orilat = dtori.getString("orilat");
                    String orilng = dtori.getString("orilng");
                    //Log.e("ORI ARRAY : ", orititle+", "+dep+", "+orilat+", "+orilng);

                    origin = new LatLng(Double.parseDouble(orilat), Double.parseDouble(orilng));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin, 13));
                    //markerPoints.add(origin);
                    MarkerOptions options = new MarkerOptions();
                    options.position(origin);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    options.title(orititle);
                    options.snippet(dep);
                    mMap.addMarker(options);

                    try {
                        //if(markerPoints.size() >= 2){
                        if (driverarr.length() > 0 && oriarr.length() > 0) {
                            if (i == 0) {
                                pbar.setVisibility(View.VISIBLE);
                                String url = getDirectionsUrl(driverlatlng, origin);
                                DownloadTask downloadTask = new DownloadTask();
                                downloadTask.execute(url);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("Error","placeMarkerOnMap 3");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObjDest = null;
        JSONArray destarr = null;
        try {
            jsonObjDest = new JSONObject(OBJDest);
            destarr = jsonObjDest.getJSONArray("JSONDEST");
            if(destarr.length()>0) {
                for (int i = 0; i < destarr.length(); i++) {
                    JSONObject dtdest = destarr.getJSONObject(i);
                    String destitle = dtdest.getString("title");
                    String xdest = dtdest.getString("dest");
                    String deslat = dtdest.getString("deslat");
                    String deslng = dtdest.getString("deslng");
                    //distanceid = Integer.parseInt(dtdest.getString("distanceid"));
                    //priceid = Integer.parseInt(dtdest.getString("priceid"));

                    //Log.e("DEST ARRAY : ", destitle+", "+distanceid+", "+priceid);

                    dest = new LatLng(Double.parseDouble(deslat), Double.parseDouble(deslng));

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(dest, 13));
                    //markerPoints.add(dest);
                    MarkerOptions options = new MarkerOptions();
                    options.position(dest);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    options.title(destitle);
                    options.snippet(xdest);
                    mMap.addMarker(options);

                    try{
                        //if(markerPoints.size() >= 2){
                        if(oriarr.length()>0 && destarr.length()>0){
                            if(i==0) {
                                pbar.setVisibility(View.VISIBLE);
                                String url = getDirectionsUrl(origin, dest);
                                DownloadTask downloadTask = new DownloadTask();
                                downloadTask.execute(url);
                            }

                            if(i>0) {
                                pbar.setVisibility(View.VISIBLE);
                                JSONObject dtdest1 = destarr.getJSONObject(i-1);
                                String a = dtdest1.getString("deslat");
                                String b = dtdest1.getString("deslng");
                                LatLng loc1 = new LatLng(Double.parseDouble(a), Double.parseDouble(b));

                                JSONObject dtdest2 = destarr.getJSONObject(i);
                                String c = dtdest2.getString("deslat");
                                String d = dtdest2.getString("deslng");
                                LatLng loc2 = new LatLng(Double.parseDouble(c), Double.parseDouble(d));

                                String url = getDirectionsUrl(loc1, loc2);
                                DownloadTask downloadTask = new DownloadTask();
                                downloadTask.execute(url);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("Error","placeMarkerOnMap 2");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Error","placeMarkerOnMap 1");
        }
    }

    private void addRowDestination(String title, String dest,String destnote,String destaddcontact,String destlat,String destlng) {
        final TableLayout tableDestination = (TableLayout) findViewById(R.id.tableDestination);
        LayoutInflater inflater = getLayoutInflater();
        final TableRow rowDestination = (TableRow)inflater.inflate(R.layout.row_destsendorder, tableDestination, false);
        final TextView desttitle = (TextView)rowDestination.findViewById(R.id.desttitle);
        final EditText txtdest = (EditText)rowDestination.findViewById(R.id.txtdest);
        final EditText txtdestdetail = (EditText)rowDestination.findViewById(R.id.tdestdetail);
        final EditText tdestcontact = (EditText)rowDestination.findViewById(R.id.tdestcontact);
        final TextView txtdestlat = (TextView)rowDestination.findViewById(R.id.txtdestlat);
        final TextView txtdestlng = (TextView)rowDestination.findViewById(R.id.txtdestlng);
        final TextView txtdistance = (TextView)rowDestination.findViewById(R.id.txtdistance);
        final TextView txtprice = (TextView)rowDestination.findViewById(R.id.txtprice);

        disableEditText(txtdest);
        disableEditText(txtdestdetail);
        disableEditText(tdestcontact);

        desttitle.setText(title);
        txtdest.setText(dest);
        txtdestdetail.setText(destnote);
        tdestcontact.setText(destaddcontact);
        txtdestlat.setText(destlat);
        txtdestlng.setText(destlng);

        tableDestination.addView(rowDestination);
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