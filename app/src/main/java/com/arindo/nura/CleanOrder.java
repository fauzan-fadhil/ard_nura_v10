package com.arindo.nura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by bmaxard on 26/01/2017.
 */

public class CleanOrder extends AppCompatActivity implements OnMapReadyCallback {
    private String SERVERADDR, csrid, email, tmDevice, versi;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private CardView cview1, cview2, cvprice;
    private RadioButton rbtn1, rbtn2;
    private CheckBox cbtn1, cbtn2;
    Toolbar toolbar;
    Snackbar snackBar;
    int timeoutdata = 20000;
    int PLACE_PICKER_REQUEST = 0;
    private String MyJSON, MyJSONObjectDetail, JSONObjectOpr;
    EditText txtdest, tdestdetail, tdestcontact;
    private CollapsingToolbarLayout collapsingToolbarLayout = null;

    GPSTracker gps;
    private GoogleMap mMap;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest;
    Double myLocLat, myLocLng;
    String distance, token, RCFROMSERVER = null, MSGFROMSERVER = null;
    private double hourlyratecustomers = 0.0;
    private double hourlyratecompany = 0.0;
    private double discount = 0.0; // satuan discount prosentase (%)
    private double sumofhour = 0.0;
    private int sumofcleaner = 1;
    TextView tprice, tdiscountpersen, tdiscount, tpayment, thour, tcleaner;
    Button btnOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_cleanorder);

        if (MainActivity.loaderActivity.isShowing()) {
            MainActivity.loaderActivity.dismiss();
        }

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTextAppernce();
        collapsingToolbarLayout.setTitle("BC2 CLEAN");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        this.setTitle(null);

       // TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        //tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        pbar = (ProgressBar) findViewById(R.id.progressbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        cview1 = (CardView) findViewById(R.id.cview1);
        cvprice = (CardView) findViewById(R.id.cvprice);
        rbtn1 = (RadioButton) findViewById(R.id.rbtn1);
        rbtn2 = (RadioButton) findViewById(R.id.rbtn2);
        cbtn1 = (CheckBox) findViewById(R.id.cbtn1);
        cbtn2 = (CheckBox) findViewById(R.id.cbtn2);
        tdestdetail = (EditText) findViewById(R.id.tdestdetail);
        tdestcontact = (EditText) findViewById(R.id.tdestcontact);
        txtdest = (EditText) findViewById(R.id.txtdest);
        disableEditText(txtdest);

        tprice = (TextView) findViewById(R.id.tprice);
        tdiscountpersen = (TextView) findViewById(R.id.tdiscountpersen);
        tdiscount = (TextView) findViewById(R.id.tdiscount);
        tpayment = (TextView) findViewById(R.id.tpayment);
        thour = (TextView) findViewById(R.id.thour);
        tcleaner = (TextView) findViewById(R.id.tcleaner);
        btnOrder = (Button) findViewById(R.id.btnOrder);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        markerPointsOpr = new ArrayList<LatLng>();
        getMaps();

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();

        txtdest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbar.setVisibility(View.VISIBLE);
                if (RCFROMSERVER != null || markerPointsOpr.size() == 0) {
                    if (MSGFROMSERVER != null) {
                        SnackBarMsg(MSGFROMSERVER);
                    }
                    getOprLocation();
                    return;
                }
                if (snackBar.isShown()) {
                    snackBar.dismiss();
                }
                PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(CleanOrder.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        rbtn1.setText("Customer provides cleaning tools\n(BND. " + currencyformat(hourlyratecustomers) + " Hourly rate)");
        rbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double rate = (hourlyratecustomers * sumofhour);
                tprice.setText("BND. " + currencyformat(rate));
                tdiscount.setText("BND. " + currencyformat(rate * discount / 100));
                tpayment.setText("BND. " + currencyformat(rate - (rate * discount / 100)));
                cvprice.setVisibility(View.VISIBLE);
            }
        });

        rbtn2.setText("BC2 CLEAN provides cleaning tools\n(BND. " + currencyformat(hourlyratecompany) + " Hourly rate)");
        rbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double rate = (hourlyratecompany * sumofhour);
                tprice.setText("BND. " + currencyformat(rate));
                tdiscount.setText("BND. " + currencyformat(rate * discount / 100));
                tpayment.setText("BND. " + currencyformat(rate - (rate * discount / 100)));
                cvprice.setVisibility(View.VISIBLE);
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tdestdetail.getText().toString().equals("")) {
                    SnackBarMsg("Please input your detail location");
                    return;
                }

                if (!cbtn1.isChecked()) {
                    SnackBarMsg("Please checked cleaner detail");
                    return;
                }

                if (!cbtn2.isChecked()) {
                    SnackBarMsg("Please checked cleaner detail");
                    return;
                }

                try {
                    MyJSONObjectDetail = DetailDataOrder();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host + "ordercsr";
                if (isNetworkAvailable() == true) {
                    if (MyJSONObjectDetail == null) {
                        SnackBarMsg("Failed detail orders !");
                    } else {
                        String destination = txtdest.getText().toString();
                        String destnote = tdestdetail.getText().toString();
                        String destaddcontact = tdestcontact.getText().toString();
                        String destlat = dest.latitude + "";
                        String destlng = dest.longitude + "";

                        /*Log.e("destination", destination);
                        Log.e("destnote", destnote);
                        Log.e("destaddcontact", destaddcontact);
                        Log.e("destlatlng", destlat+", "+destlng);
                        Log.e("Detail orders", MyJSONObjectDetail);*/

                        TaskOrder MyTask = new TaskOrder();
                        MyTask.execute(token, destination, destnote, destaddcontact, destlat, destlng);
                    }
                }
            }
        });
    }

    private String DetailDataOrder() throws JSONException {
        JSONArray detail = new JSONArray();
        for (int i = 0; i < 1; i++) {
            JSONObject obj = new JSONObject();
            String tools = null;
            String price = null;
            String cleanergender = null;
            if (rbtn1.isChecked()) {
                tools = "0";
                price = String.valueOf(hourlyratecustomers);
            }
            if (rbtn2.isChecked()) {
                tools = "1";
                price = String.valueOf(hourlyratecompany);
            }
            if (cbtn1.isChecked()) {
                cleanergender = "0";
            }

            obj.put("tools", tools);
            obj.put("price", price);
            obj.put("qty", sumofhour);
            obj.put("discount", discount);
            obj.put("cleanercount", sumofcleaner);
            obj.put("cleanergender", cleanergender);
            detail.put(obj);
        }
        JSONObject object = new JSONObject();
        JSONObject JSONDetail = object.put("detail", detail);
        Log.e("Object Detail : ", JSONDetail.toString());
        return JSONDetail.toString();
    }

    public class TaskOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(CleanOrder.this, android.R.style.Theme_Translucent);

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
                String destination = value[1];
                String destnote = value[2];
                String destaddcontact = value[3];
                String destlat = value[4];
                String destlng = value[5];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token", token));
                post.add(new BasicNameValuePair("csrid", csrid));
                post.add(new BasicNameValuePair("pid", "4"));

                post.add(new BasicNameValuePair("destination", destination));
                post.add(new BasicNameValuePair("destnote", destnote));
                post.add(new BasicNameValuePair("destaddcontact", destaddcontact));
                post.add(new BasicNameValuePair("destlat", destlat));
                post.add(new BasicNameValuePair("destlng", destlng));

                post.add(new BasicNameValuePair("arroprid", JSONObjectOpr));
                post.add(new BasicNameValuePair("detail", MyJSONObjectDetail));
                post.add(new BasicNameValuePair("versi", versi));

                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(timeoutdata);
                HttpPost httppost = new HttpPost(SERVERADDR);

                httppost.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                MyJSON = request(response);
                Log.e("JSON", MyJSON);
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
            if (rc.equals("1")) {
                ShowHistory();
                dialog.dismiss();
            } else if (rc.equals("88") || rc.equals("101")) {
                SnackBarMsg("Order failed ! (" + rc + ")");
                dialog.dismiss();
            } else if (rc.equals("99")) {
                SnackBarMsg("Order failed ! (" + rc + ")");
                dialog.dismiss();
            } else {
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private void ShowHistory() {
        Bundle bd = new Bundle();
        bd.putInt("act", 0);
        Intent i = new Intent(this, History.class);
        i.putExtras(bd);
        this.finish();
        startActivity(i);
    }

    private String currencyformat(double value) {
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(otherSymbols);

        return String.valueOf(df.format(value));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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

        gps = new GPSTracker(CleanOrder.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            myLocLat = gps.getLatitude();
            myLocLng = gps.getLongitude();
            myLoc = new LatLng(myLocLat,myLocLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
            getOprLocation();
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

    private Bitmap icons(int param){
        int height = 40;
        int width = 40;
        BitmapDrawable bitmapdraw=null;
        if(param==0){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_ride);}
        if(param==1){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_car);}
        if(param==2){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_boat);}
        if(param==3){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_send);}
        if(param==4){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_clean);}
        if(param==5){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_food);}
        if(param==6){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_tick);}
        if(param==7){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_towing);}

        if(param==101){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_origin);}
        if(param==102){width = 30; height = 42; bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.marker_destination);}
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return smallMarker;
    }

    private void getOprLocation() {
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"getoprlocation";
        if (isNetworkAvailable() == true) {
            TaskOprLocation MyTask = new TaskOprLocation();
            MyTask.execute();
        }else{
            SnackBarMsg("Internet not connected !");
        }
    }

    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        editText.setCursorVisible(false);
    }

    public class TaskOprLocation extends AsyncTask<String, Integer, String[]> {
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
                post.add(new BasicNameValuePair("pid","4"));
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
                String hourlyratecustomers = jobject.getString("hourlyratecustomers");
                String hourlyratecompany = jobject.getString("hourlyratecompany");
                String minimalrate = jobject.getString("minimalrate");
                String discount = jobject.getString("discount");

                if (rc.equals("1")) {
                    result = new String[] {"1", hourlyratecustomers, hourlyratecompany, minimalrate, discount};
                } else {
                    String msg = jobject.getString("msg");
                    statuskomplit = rc;
                    result = new String[]{rc, msg, hourlyratecustomers, hourlyratecompany, minimalrate, discount};
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
                hourlyratecustomers = Double.parseDouble(result[1]);
                hourlyratecompany = Double.parseDouble(result[2]);
                sumofhour = Double.parseDouble(result[3]);
                discount = Double.parseDouble(result[4]);
                rbtn1.setText("Customer provides cleaning tools\n(BND. "+currencyformat(hourlyratecustomers)+" Hourly rate)");
                rbtn2.setText("BC2 Clean provides cleaning tools\n(BND. "+currencyformat(hourlyratecompany)+" Hourly rate)");
                thour.setText(sumofhour+" Hour");
                tcleaner.setText(sumofcleaner+" Cleaner");
                tdiscountpersen.setText("Discount ("+ discount +"%)");

                JSONObject jsonObj = null;
                JSONArray peoples = null;
                JSONArray datarr = new JSONArray();
                try {
                    jsonObj = new JSONObject(MyJSON);
                    peoples = jsonObj.getJSONArray("operator");
                    for (int i = 0; i < peoples.length(); i++) {
                        JSONObject dt = peoples.getJSONObject(i);
                        String oprid = dt.getString("oprid");
                        String oprname = dt.getString("oprname");
                        String oprlat = dt.getString("oprlat");
                        String oprlng = dt.getString("oprlng");

                        LatLng point_new = new LatLng(Double.parseDouble(oprlat),Double.parseDouble(oprlng));

                        markerPointsOpr.add(point_new);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point_new);

                        options.icon(BitmapDescriptorFactory.fromBitmap(icons(4)));
                        options.title(oprname);
                        mMap.addMarker(options);

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
                MSGFROMSERVER = "Get Location Failed ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else if(rc.equals("99")){
                RCFROMSERVER = rc;
                MSGFROMSERVER = "Get Location Failed ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else{
                hourlyratecustomers = Double.parseDouble(result[2]);
                hourlyratecompany = Double.parseDouble(result[3]);
                sumofhour = Double.parseDouble(result[4]);
                discount = Double.parseDouble(result[5]);
                rbtn1.setText("Customer provides cleaning tools\n(BND. "+currencyformat(hourlyratecustomers)+" Hourly rate)");
                rbtn2.setText("Customer provides cleaning tools\n(BND. "+currencyformat(hourlyratecompany)+" Hourly rate)");
                thour.setText(sumofhour+" Hour");
                tcleaner.setText(sumofcleaner+" Cleaner");
                tdiscountpersen.setText("Discount ("+ discount +"%)");

                RCFROMSERVER = rc;
                MSGFROMSERVER = result[1];
                SnackBarMsg(MSGFROMSERVER);
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

    protected void showList(String id, String title, String desc, String imagetitleurl){
        collapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#333333"));
        collapsingToolbarLayout.setTitle(title);

    }

    protected void placeMarkerOnMap(LatLng location) {
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
        }

        if(PLACE_PICKER_REQUEST==1) {
            dest = location; // location cleaning
        }else if(PLACE_PICKER_REQUEST==2) {
            origin = location; // cleaner position
        }

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
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Your location");
                        options.snippet(txtdest.getText().toString());
                    } else if (i == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Cleaner position");
                        options.snippet("Cleaner");
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
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title("Departure");
                        options.snippet("snippet");
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
                        options.title("Your cleaning location");
                        options.snippet(txtdest.getText().toString());
                    }
                    mMap.addMarker(options);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        pbar.setVisibility(View.GONE);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    Place place = PlacePicker.getPlace(this, data);
                    if (PLACE_PICKER_REQUEST == 1) {
                        txtdest.setText(place.getAddress().toString());
                        txtdest.setTextColor(Color.parseColor("#000000"));
                        placeMarkerOnMap(place.getLatLng());
                        btnOrder.setEnabled(true);
                    }
                }catch (Exception e){
                    btnOrder.setEnabled(false);
                    SnackBarMsg("Failed to selecting cleaner, please try again..");
                }
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
