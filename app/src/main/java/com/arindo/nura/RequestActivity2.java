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
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.Locale;

public class RequestActivity2 extends AppCompatActivity implements OnMapReadyCallback {
    private int pid;
    private GoogleMap mMap;
    EditText txtori, txtdest;
    LinearLayout layoutrute;
    int PLACE_PICKER_REQUEST = 0;
    private String boatrutedeparture, depnote, boatrutedestination, destnote;
    private TextView txttitle, tdistance, tprice, rutetitle;
    private ImageView ictitle, srcStart;
    private Button btnOrder;
    private String SERVERADDR, csrid, email, tmDevice, versi, ruteid="";
    private int timeoutdata = 20000;
    private String MyJSON, MyJSONObjectDetail, JSONObjectOpr;
    private SqlHelper dbHelper;
    private LinearLayout layoutsubmit;
    private ProgressBar pbar;
    public static final String TAG_RUTEID="ruteid";
    public static final String TAG_RUTE="rute";
    public static final String TAG_RUTEADDR="ruteaddress";
    public static final String TAG_RUTELAT="rutelat";
    public static final String TAG_RUTELNG="rutelng";
    Toolbar toolbar;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest;
    Double myLocLat, myLocLng;
    String distance, price, token, RCFROMSERVER=null, MSGFROMSERVER=null;
    Snackbar snackBar;

    GPSTracker gps;

    CustomAutoCompleteTextView mact;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_request2);

        if(MainActivity.loaderActivity.isShowing()) {
            MainActivity.loaderActivity.dismiss();
        }

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

        btnOrder = (Button)findViewById(R.id.btnOrder);
        txttitle = (TextView)findViewById(R.id.txttitle);
        tdistance = (TextView)findViewById(R.id.tdistance);
        tprice = (TextView)findViewById(R.id.tprice);
        ictitle = (ImageView)findViewById(R.id.ictitle);
        srcStart = (ImageView)findViewById(R.id.srcStart);
        layoutsubmit = (LinearLayout)findViewById(R.id.layoutsubmit);
        pbar = (ProgressBar) findViewById(R.id.pbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        rutetitle = (TextView)findViewById(R.id.rutetitle);
        layoutrute = (LinearLayout)findViewById(R.id.layoutrute);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");
        if (pid == 0) {
            txttitle.setText(" OJEK");
            ictitle.setImageResource(R.drawable.motornura);
        }else if (pid == 1) {
            //txttitle.setText("BC2 CAR");
            //ictitle.setImageResource(R.drawable.ic_dekstop_car);
            txttitle.setText(" FOOD");
            ictitle.setImageResource(R.drawable.ic_sembako);
        }else if (pid == 2) {
            txttitle.setText("BC2 BOAT");
            ictitle.setImageResource(R.drawable.ic_dekstop_boat);
        }else if (pid == 3) {
            txttitle.setText("BC2 SEND");
            ictitle.setImageResource(R.drawable.ic_dekstop_send);
        }else if (pid == 4) {
            txttitle.setText("BC2 CLEAN");
            ictitle.setImageResource(R.drawable.ic_dekstop_clean);
        }else if (pid == 5) {
            txttitle.setText(" FOOD");
            ictitle.setImageResource(R.drawable.ic_dekstop_food);
        }else if (pid == 6) {
            txttitle.setText("BC2 TICK");
            ictitle.setImageResource(R.drawable.ic_dekstop_tick);
        }else if (pid == 7) {
            txttitle.setText("BC2 TOWING");
            ictitle.setImageResource(R.drawable.ic_dekstop_towing);
        }else if (pid == 8) {
            txttitle.setText("BC2 DES");
            ictitle.setImageResource(R.drawable.ic_dekstop_des);
        }else if (pid == 9) {
            txttitle.setText("BC2 DST");
            ictitle.setImageResource(R.drawable.ic_dekstop_dst);
        }else if (pid == 10) {
            txttitle.setText("BC2 INDOSAT");
            ictitle.setImageResource(R.drawable.ic_dekstop_indosat);
        }else if (pid == 11) {
            txttitle.setText("BC2 TELKOMSEL");
            ictitle.setImageResource(R.drawable.ic_dekstop_telkomsel);
        }

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        markerPointsOpr = new ArrayList<LatLng>();
        getMaps();

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        csrid = csrAccount.setid();

        txtori = (EditText) findViewById(R.id.txtori);
        txtdest = (EditText) findViewById(R.id.txtdest);
        disableEditText(txtori);
        disableEditText(txtdest);
        txtori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbar.setVisibility(View.VISIBLE);
                if(RCFROMSERVER != null || markerPointsOpr.size()==0) {
                    if(MSGFROMSERVER!=null){SnackBarMsg(MSGFROMSERVER);}
                    getOprLocation();
                    return;
                }
                if(snackBar.isShown()){snackBar.dismiss();}
                PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    if(pid==2){
                        ShowBoatRute("0");
                    }else {
                        startActivityForResult(builder.build(RequestActivity2.this), PLACE_PICKER_REQUEST);
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        txtdest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pbar.setVisibility(View.VISIBLE);
                if(RCFROMSERVER != null || markerPointsOpr.size()==0) {
                    if(MSGFROMSERVER!=null){SnackBarMsg(MSGFROMSERVER);}
                    getOprLocation();
                    return;
                }
                if(snackBar.isShown()){snackBar.dismiss();}
                PLACE_PICKER_REQUEST = 2;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    if(pid==2){
                        ShowBoatRute("1");
                    }else {
                        startActivityForResult(builder.build(RequestActivity2.this), PLACE_PICKER_REQUEST);
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        //mact = (MultiAutoCompleteTextView) findViewById(R.id.mact1);
        //membuat adapter untuk menampilkan list item
        //adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,item);
        //menerapkan adapter pada objek mact
        //mact.setAdapter(adapter);
        //membuat karakter pembatas antar kota
        //mact.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        // Adding textchange listener
        mact = (CustomAutoCompleteTextView) findViewById(R.id.mact1);
        mact.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //loadPlace(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });

        srcStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //loadPlace(mact.getText().toString());
            }
        });

        btnOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host+"ordercsr";
                if (isNetworkAvailable() == true) {
                    String originLat = origin.latitude+"";
                    String originLng = origin.longitude+"";
                    String destLat = dest.latitude+"";
                    String destLng = dest.longitude+"";
                    String departure = txtori.getText().toString();
                    String destination = txtdest.getText().toString();
                    String departnote = depnote;
                    String desnote = destnote;
                    String xpid = pid+"";
                    TaskOrder MyTask = new TaskOrder();
                    MyTask.execute(token,originLat,originLng,destLat,destLng,departure,destination,departnote,desnote,xpid);
                }
            }
        });

        /*PlaceAutocompleteFragment autocompleteFragmentStart = null;
        PlaceAutocompleteFragment autocompleteFragmentEnd = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            autocompleteFragmentStart = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_start);
            autocompleteFragmentEnd = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_end);
        }

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragmentStart.setFilter(typeFilter);
        autocompleteFragmentStart.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.e(TAG, "Place: " + place.getName());//get place details here
                Log.e(TAG, "Place: " + place.getLatLng());//get place details here
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.e(TAG, "An error occurred: " + status);
            }
        });

        autocompleteFragmentEnd.setFilter(typeFilter);
        autocompleteFragmentEnd.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //Log.e(TAG, "Place: " + place.getName());//get place details here
                Log.e(TAG, "Place: " + place.getLatLng());//get place details here
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.e(TAG, "An error occurred: " + status);
            }
        });*/

        /*imageViewSlider = (ImageView) findViewById(R.id.imageViewSlider);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingDrawer);
        slidingDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                imageViewSlider.setBackgroundResource(R.drawable.back);
            }
        });

        slidingDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {
            @Override
            public void onDrawerClosed() {
                imageViewSlider.setBackgroundResource(R.drawable.back);
            }
        });*/
    }

    private void ShowBoatRute(String param){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"reqboatrute";
        if (isNetworkAvailable() == true) {
            TaskBoatRute MyTask = new TaskBoatRute();
            MyTask.execute(token, param, ruteid);
        }
    }

    private void disableEditText(EditText editText) {
        editText.setFocusable(false);
        //editText.setEnabled(false);
        editText.setCursorVisible(false);
        //editText.setKeyListener(null);
        //editText.setBackgroundColor(Color.TRANSPARENT);
    }

    /*public void loadPlace(CharSequence place){
        PLACE = "0";
        String key = "key=AIzaSyCoy7DtH2XYYO8VG249bQtHHbweXudm8XU";
        String input = "input="+place;
        String types = "types=geocode";
        String sensor = "sensor=false";
        String parameters = input+"&"+types+"&"+sensor+"&"+key;
        String output = "json";
        googleURL = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        TaskPlace MyTask = new TaskPlace();
        MyTask = null;
        MyTask.execute("0");
        //mact.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    public class TaskPlace extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        //final Dialog dialog = new Dialog(RequestActivity2.this, android.R.style.Theme_Translucent);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //here we set layout of progress dialog
            //dialog.setContentView(R.layout.custom_progress_dialog);
            //dialog.setCancelable(true);
            //dialog.show();
        }

        protected String[] doInBackground(String... value) {
            try {
                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(1000);
                HttpGet httpget = new HttpGet(googleURL);
                HttpResponse response = httpclient.execute(httpget);

                MyJSON = request(response);
                Log.e("JSON",MyJSON);
                JSONObject jobject = new JSONObject(MyJSON);

                String rc = jobject.getString("status");
                if (rc.equals("OK")) {
                    result = new String[]{"1", MyJSON};
                } else {
                    result = new String[]{"99", "Gagal"};
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
                //Toast(0,"Request permohonan telah dikirim");
                String mJson = result[1];
                //RefreshInput();
                //dialog.dismiss();

                // Creating ParserTask for parsing Google Places
                //ParserTask placesParserTask = new ParserTask();
                // Start parsing google places json data
                // This causes to execute doInBackground() of ParserTask class
                //placesParserTask.execute(jObject);

                JSONObject jObject;
                List<HashMap<String, String>> list = null;
                try{
                    jObject = new JSONObject(mJson);
                    PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                    // Getting the parsed data as a List construct
                    list = placeJsonParser.parse(jObject);

                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text1 };

                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), list, android.R.layout.simple_list_item_1, from, to);
                    //menerapkan adapter pada objek mact
                    mact.setAdapter(adapter);
                    //membuat karakter pembatas antar kota
                    //mact.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                }catch(Exception e){
                    Log.e("Error List View",e.toString());
                }

            }else if(rc.equals("88") || rc.equals("101")){
                //Toast(2,"RC:"+rc+" >> Request gagal !");
                //dialog.dismiss();
                Log.e("Gagal ", "RC 88");
            }else if(rc.equals("99")){
                //Toast(1,"RC:"+rc+" >> Request Time Out !");
                //dialog.dismiss();
                Log.e("Gagal ", "RC 99");
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                //dialog.dismiss();
            }
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<HashMap<String, String>> list = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                // Getting the parsed data as a List construct
                list = placeJsonParser.parse(jObject);
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);
            //menerapkan adapter pada objek mact
            mact.setAdapter(adapter);
            //membuat karakter pembatas antar kota
            //mact.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        }
    }*/

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

    /*private void callPlaceAutocompleteActivityIntent() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            //PLACE_AUTOCOMPLETE_REQUEST_CODE is integer for request code
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place:" + place.toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (requestCode == RESULT_CANCELED) {

            }
        }
    }*/

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

    private Bitmap icons(int param){
        int height = 40;
        int width = 40;
        BitmapDrawable bitmapdraw=null;
        if(param==0){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.motornura);}
        //if(param==1){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_car);}
        if(param==1){bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_dekstop_food);}
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mMap.setMyLocationEnabled(true);


        gps = new GPSTracker(RequestActivity2.this);

        // check if GPS enabled
        if(gps.canGetLocation()){
            myLocLat = gps.getLatitude();
            myLocLng = gps.getLongitude();
            myLoc = new LatLng(myLocLat,myLocLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 13));
            getOprLocation();

            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: "
            //        + myLocLat + "\nLong: " + myLocLng, Toast.LENGTH_LONG).show();


            //20170714 map onclick => untuk pengembangan, add point marker dengan cara klik map layer
            /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng arg0) {
                    // TODO Auto-generated method stub

                    Geocoder geocoder = null;
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    List<Address> addresses = new ArrayList<>();
                    try {
                        addresses = geocoder.getFromLocation(arg0.latitude, arg0.longitude,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    android.location.Address address = addresses.get(0);

                    if (address != null) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < address.getMaxAddressLineIndex(); i++){
                            sb.append(address.getAddressLine(i) + "\n");
                        }
                        Toast.makeText(getApplicationContext(), sb.toString(), Toast.LENGTH_LONG).show();

                        LatLng point_new = new LatLng(arg0.latitude, arg0.longitude);
                        markerPointsOpr.add(point_new);
                        MarkerOptions options = new MarkerOptions();
                        options.position(point_new);

                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                        options.title("New Point");
                        mMap.addMarker(options);
                    }
                }
            });*/
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
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
                post.add(new BasicNameValuePair("pid",pid+""));
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

                        options.icon(BitmapDescriptorFactory.fromBitmap(icons(pid)));
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
                MSGFROMSERVER = "Lokasi gagal dipilih ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else if(rc.equals("99")){
                RCFROMSERVER = rc;
                MSGFROMSERVER = "Lokasi gagal dipilih ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else{
                RCFROMSERVER = rc;
                MSGFROMSERVER = result[1];
                SnackBarMsg(MSGFROMSERVER);

                //if(rc.equals("01")){getOprLocation();}
            }
        }
    }

    protected void placeMarkerOnMap(LatLng location) {
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
        }

        if(PLACE_PICKER_REQUEST==1) {
            origin = location;
        }else if(PLACE_PICKER_REQUEST==2) {
            dest = location;
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
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title("Dari");
                        options.snippet(txtori.getText().toString());
                    } else if (i == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Tujuan");
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
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        options.title("Dari");
                        options.snippet(txtori.getText().toString());
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
                    if(pid!=2){ //if pid = 2 then request price boat rute from server
                        lineOptions.addAll(points);
                        lineOptions.width(5);
                        lineOptions.color(Color.BLUE);
                    }
                }


                CalculatServer(distance);
                if(pid!=2) {
                    // Drawing polyline in the Google Map for the i-th route
                    mMap.addPolyline(lineOptions);
                }
            }catch (Exception e){
                Log.e("error parsing array","result not found");
                Toast(2,"Result Google Maps not found");
            }
        }
    }

    private void CalculatServer(String distance){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        if(pid==2){
            SERVERADDR = host+"reqboatruteprice";
            if (isNetworkAvailable() == true) {
                TaskBoatRutePrice MyTask = new TaskBoatRutePrice();
                MyTask.execute(token, distance, pid + "");
            }
        }else {
            SERVERADDR = host + "excalculate";
            if (isNetworkAvailable() == true) {
                TaskCalcutate MyTask = new TaskCalcutate();
                MyTask.execute(token, distance, pid + "");
            }
        }
    }

    public class TaskBoatRutePrice extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(RequestActivity2.this, android.R.style.Theme_Translucent);
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
                post.add(new BasicNameValuePair("depid",boatrutedeparture));
                post.add(new BasicNameValuePair("destid",boatrutedestination));
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
                    String tprice = jobject.getString("tprice");
                    String price = jobject.getString("price");
                    result = new String[]{"1", distancetext, distancevalue, tprice, price};
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
                String price = result[4];
                layoutsubmit.setVisibility(View.VISIBLE);
                tdistance.setText("Jarak : " + distancetext);
                //tprice.setText("Price : " +textprice);
                tprice.setText(textprice);
                dialog.dismiss();
                try {
                    MyJSONObjectDetail = DetailDataOrder(price,distancevalue);
                    Log.e("JSON",MyJSONObjectDetail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Kalkulasi gagal ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Kalkulasi gagal ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    public class TaskCalcutate extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(RequestActivity2.this, android.R.style.Theme_Translucent);
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
                    String tprice = jobject.getString("tprice");
                    String price = jobject.getString("price");
                    result = new String[]{"1", distancetext, distancevalue, tprice, price};
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
                //Toast(0,"Calculate Success");
                String distancetext = result[1];
                String distancevalue = result[2];
                String textprice = result[3];
                String price = result[4];
                layoutsubmit.setVisibility(View.VISIBLE);
                tdistance.setText("Jarak : " + distancetext);
                tprice.setText("Tarif : " +textprice);
                dialog.dismiss();
                try {
                    MyJSONObjectDetail = DetailDataOrder(price,distancevalue);
                    Log.e("JSON",MyJSONObjectDetail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Kalkulasi gagal ! ("+rc+")");
                dialog.dismiss();
            }else if(rc.equals("99")){
                SnackBarMsg("Kalkulasi gagal ! ("+rc+")");
                dialog.dismiss();
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                SnackBarMsg(msg);
                dialog.dismiss();
            }
        }
    }

    private String DetailDataOrder(String price, String qty)throws JSONException{
        JSONArray detail = new JSONArray();
        for(int i=0; i < 1; i++){
            JSONObject obj = new JSONObject();

            obj.put("price", price);
            obj.put("qty", qty);
            detail.put(obj);
        }
        JSONObject object = new JSONObject();
        JSONObject JSONDetail = object.put("detail",detail);
        Log.e("Objek Detail : ", JSONDetail.toString());
        return JSONDetail.toString();
    }

    public class TaskOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(RequestActivity2.this, android.R.style.Theme_Translucent);
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
                String originlat = value[1];
                String originlng = value[2];
                String destlat = value[3];
                String destlng = value[4];
                String departure = value[5];
                String destination = value[6];
                String departnote = value[7];
                String destnote = value[8];
                String pid = value[9];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("originlat",originlat));
                post.add(new BasicNameValuePair("originlng",originlng));
                post.add(new BasicNameValuePair("destlat",destlat));
                post.add(new BasicNameValuePair("destlng",destlng));
                post.add(new BasicNameValuePair("departure",departure));
                post.add(new BasicNameValuePair("destination",destination));
                post.add(new BasicNameValuePair("departnote",departnote));
                post.add(new BasicNameValuePair("destnote",destnote));
                post.add(new BasicNameValuePair("pid",pid));
                post.add(new BasicNameValuePair("detail",MyJSONObjectDetail));
                post.add(new BasicNameValuePair("arroprid",JSONObjectOpr));
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
        Bundle bd = new Bundle();
        bd.putInt("act", 0);
        Intent i = new Intent(this, History.class);
        i.putExtras(bd);
        this.finish();
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        pbar.setVisibility(View.GONE);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    Place place = PlacePicker.getPlace(this, data);
                    //String addressText = place.getName().toString();
                    //addressText += "\n" + place.getAddress().toString();
                    if (PLACE_PICKER_REQUEST == 1) {
                        txtori.setText(place.getAddress().toString());
                        txtori.setTextColor(Color.parseColor("#000000"));
                        placeMarkerOnMap(place.getLatLng());
                    } else if (PLACE_PICKER_REQUEST == 2) {
                        txtdest.setText(place.getAddress().toString());
                        txtdest.setTextColor(Color.parseColor("#000000"));
                        placeMarkerOnMap(place.getLatLng());
                    }
                }catch (Exception e){
                    SnackBarMsg("Gagal memilih tempat, silahkan ulangi..");
                }
            }
        }
    }

    public class TaskBoatRute extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(RequestActivity2.this, android.R.style.Theme_Translucent);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String[] doInBackground(String... value) {
            try {
                String token = value[0];
                String param = value[1];
                String ruteid = value[2];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("ruteid",ruteid));
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
                    //String dep = jobject.getString("dep");
                    result = new String[]{"1",param};
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
                String param = result[1];
                listRuteBoat(param);
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("88") || rc.equals("101")){
                SnackBarMsg("Request failed ! ("+rc+")");
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("99")){
                SnackBarMsg("Request failed ! ("+rc+")");
                pbar.setVisibility(View.GONE);
            }else{
                String msg = result[1];
                SnackBarMsg(msg);
                pbar.setVisibility(View.GONE);
            }
        }
    }

    private void listRuteBoat(final String param){
        if(param.equals("0")) {
            rutetitle.setText("Lokasi Penjemputan");
        }else{
            rutetitle.setText("Pilih Tujuan");
        }
        layoutrute.setVisibility(View.VISIBLE);

        ListView mylist = (ListView) findViewById(R.id.rutelist);
        LazyAdapterRute adapter;
        ArrayList<HashMap<String, String>> ListSendOption = new ArrayList<HashMap<String, String>>();
        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("rutelist");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String ruteid = c.getString(TAG_RUTEID);
                String rute = c.getString(TAG_RUTE);
                String ruteaddress = c.getString(TAG_RUTEADDR);
                String rutelat = c.getString(TAG_RUTELAT);
                String rutelng = c.getString(TAG_RUTELNG);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_RUTEID,ruteid);
                map.put(TAG_RUTE,rute);
                map.put(TAG_RUTEADDR,ruteaddress);
                map.put(TAG_RUTELAT,rutelat);
                map.put(TAG_RUTELNG,rutelng);
                ListSendOption.add(map);
                jml++;
            }
            try {
                adapter = new LazyAdapterRute(this, ListSendOption);
                mylist.setAdapter(adapter);

                mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        TextView truteid = (TextView) view.findViewById(R.id.truteid);
                        TextView trute = (TextView) view.findViewById(R.id.trute);
                        TextView truteaddress = (TextView) view.findViewById(R.id.truteaddress);
                        TextView trutelat = (TextView) view.findViewById(R.id.trutelat);
                        TextView trutelng = (TextView) view.findViewById(R.id.trutelng);
                        ruteid = String.valueOf(truteid.getText());
                        String rute = String.valueOf(trute.getText());
                        String ruteaddress = String.valueOf(truteaddress.getText());
                        String rutelat = String.valueOf(trutelat.getText());
                        String rutelng = String.valueOf(trutelng.getText());

                        if(param.equals("0")) {
                            txtori.setText(ruteaddress);
                            depnote = rute;
                            boatrutedeparture = String.valueOf(truteid.getText());
                            origin = new LatLng(Double.parseDouble(rutelat), Double.parseDouble(rutelng));
                            placeMarkerOnMap(origin);
                        }else{
                            txtdest.setText(ruteaddress);
                            destnote = rute;
                            boatrutedestination = String.valueOf(truteid.getText());
                            dest = new LatLng(Double.parseDouble(rutelat), Double.parseDouble(rutelng));
                            placeMarkerOnMap(dest);
                        }
                        layoutrute.setVisibility(View.GONE);
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
}