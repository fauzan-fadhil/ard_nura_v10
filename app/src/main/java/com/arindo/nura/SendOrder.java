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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.zip.Inflater;

public class SendOrder extends AppCompatActivity implements OnMapReadyCallback {
    private int pid, optionid;
    private GoogleMap mMap;
    EditText txtori, toridetail, toricontact, tdetailitem;
    int PLACE_PICKER_REQUEST = 0;
    private TextView txttitle, tdistance, tprice;
    private ImageView ictitle, srcStart;
    private Button btnOrder, addDest;
    private String SERVERADDR, csrid, email, tmDevice, versi;
    private int timeoutdata = 20000;
    private String MyJSON, MyJSONObjectDetail, JSONObjectOpr;
    private SqlHelper dbHelper;
    private LinearLayout layoutsubmit;
    private TableLayout tblPrice;
    private ProgressBar pbar;
    private LayoutInflater inflater;
    private EditText txtdestresult;
    private TextView txtdestlatresult;
    private TextView txtdestlngresult;
    private TextView tnumpickernegative, tnumpickerpositive;
    private TextView tshipperprice;
    private int tshippernum = 1;
    private String shipperprice;
    private int distanceid, priceid;
    private CheckBox cbLoadService;
    Toolbar toolbar;
    ArrayList<LatLng> markerPoints, markerPointsOpr;
    LatLng myLoc, origin, dest;
    Double myLocLat, myLocLng;
    String distance, price, token, RCFROMSERVER=null, MSGFROMSERVER=null;
    String distancetotal = "0";
    Snackbar snackBar;

    GPSTracker gps;

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
        setContentView(R.layout.content_sendorder);

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

        btnOrder = (Button)findViewById(R.id.btnOrder);
        addDest = (Button)findViewById(R.id.addDest);
        txttitle = (TextView)findViewById(R.id.txttitle);
        tdistance = (TextView)findViewById(R.id.tdistance);
        tprice = (TextView)findViewById(R.id.tprice);
        ictitle = (ImageView)findViewById(R.id.ictitle);
        srcStart = (ImageView)findViewById(R.id.srcStart);
        layoutsubmit = (LinearLayout)findViewById(R.id.layoutsubmit);
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
        slideHandleText = (TextView) findViewById(R.id.slideHandleText);
        slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);

        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        pid = main.getExtras().getInt("order");
        optionid = main.getExtras().getInt("optionid");
        ictitle.setImageResource(R.drawable.ic_dekstop_send);
        if (optionid == 1) {
            txttitle.setText("BC2 SEND - MOTORCYCLE");
        }else if (optionid == 2) {
            txttitle.setText("BC2 SEND - SMALL PICKUP");
        }else if (optionid == 3) {
            txttitle.setText("BC2 SEND - LORRY PICKUP");
        }else if (optionid == 4) {
            txttitle.setText("BC2 SEND - HIAB CRANE");
        }

        origin = null;
        dest = null;
        markerPoints = new ArrayList<LatLng>();
        markerPointsOpr = new ArrayList<LatLng>();
        getMaps();

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
                    startActivityForResult(builder.build(SendOrder.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        cbLoadService.setText(tshippernum+" Additional Shipper");
        tnumpickernegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((tshippernum > 1) && cbLoadService.isChecked()){
                    tshippernum = tshippernum - 1;
                    cbLoadService.setText(tshippernum+" Additional Shipper");
                }
            }
        });

        tnumpickerpositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((tshippernum <= 1) && cbLoadService.isChecked()){
                    tshippernum = tshippernum + 1;
                    cbLoadService.setText(tshippernum+" Additional Shippers");
                }
            }
        });

        addRowDestination();

        /*txtdest.setOnClickListener(new View.OnClickListener() {
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
                    startActivityForResult(builder.build(SendOrder.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });*/

        btnOrder.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MyConfig config = new MyConfig();
                String host = config.hostname(dbHelper);
                SERVERADDR = host+"ordercsr";
                if (isNetworkAvailable() == true) {
                    MyJSONObjectDetail = DetailDataOrder();
                    if(MyJSONObjectDetail.equals("0")){
                        SnackBarMsg("Failed detail orders !");
                    }else {
                        Log.e("Detail orders", MyJSONObjectDetail);

                        String originLat = origin.latitude + "";
                        String originLng = origin.longitude + "";
                        String departure = txtori.getText().toString();
                        String departnote = toridetail.getText().toString();
                        String departaddcontact = toricontact.getText().toString();
                        String dest = txtdestresult.getText().toString();
                        String destlat = txtdestlatresult.getText().toString();
                        String destlng = txtdestlngresult.getText().toString();
                        String xpid = pid + "";
                        String itemdetail = tdetailitem.getText().toString();
                        if (!cbLoadService.isChecked()) {
                            tshippernum = 0;
                            shipperprice = "0";
                        }
                        //String itemdes = txtdestlngresult.getText().toString();

                        TaskOrder MyTask = new TaskOrder();
                        MyTask.execute(token, originLat, originLng, departure, departnote, departaddcontact,
                                dest, destlat, destlng, itemdetail,
                                xpid, tshippernum + "", shipperprice, MyJSONObjectDetail);
                    }
                }
            }
        });

        addDest.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                addRowDestination();
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

        gps = new GPSTracker(SendOrder.this);

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
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
                MSGFROMSERVER = "Get Location Failed ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else if(rc.equals("99")){
                RCFROMSERVER = rc;
                MSGFROMSERVER = "Get Location Failed ("+rc+")";
                SnackBarMsg(MSGFROMSERVER);
            }else{
                RCFROMSERVER = rc;
                MSGFROMSERVER = result[1];
                SnackBarMsg(MSGFROMSERVER);

                //if(rc.equals("01")){getOprLocation();}
            }
        }
    }

    protected void __placeMarkerOnMap(LatLng location) {
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
                        options.title("Departure");
                        options.snippet(txtori.getText().toString());
                    } else if (i == 1) {
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        options.title("Destination");
                        options.snippet(txtdestresult.getText().toString());
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
                        options.title("Destination");
                        //options.snippet(txtdest.getText().toString());
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

                CalculatServer(distancetotal, distance, distanceid, priceid);
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);
            }catch (Exception e){
                Log.e("error parsing array","result not found");
                Toast(2,"Result Google Maps not found");
            }
        }
    }

    public class TaskOrder extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(SendOrder.this, android.R.style.Theme_Translucent);
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
                String departure = value[3];
                String departnote = value[4];
                String departaddcontact = value[5];
                String dest = value[6];
                String destlat = value[7];
                String destlng = value[8];
                String itemdetail = value[9];
                String pid = value[10];
                String addshipper = value[11];
                String shipperprice = value[12];
                String detail = value[13];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("originlat",originlat));
                post.add(new BasicNameValuePair("originlng",originlng));
                post.add(new BasicNameValuePair("departure",departure));
                post.add(new BasicNameValuePair("departnote",departnote));
                post.add(new BasicNameValuePair("departaddcontact",departaddcontact));
                post.add(new BasicNameValuePair("destination",dest));
                post.add(new BasicNameValuePair("destlat",destlat));
                post.add(new BasicNameValuePair("destlng",destlng));
                post.add(new BasicNameValuePair("itemdetail",itemdetail));
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
                try {
                Place place = PlacePicker.getPlace(this, data);
                    if(PLACE_PICKER_REQUEST==1) {
                        txtori.setText(place.getAddress().toString());
                        txtori.setTextColor(Color.parseColor("#000000"));
                        origin = place.getLatLng();
                        //placeMarkerOnMap(place.getLatLng());
                        try {
                            getArrayLocationToMarkers();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else if(PLACE_PICKER_REQUEST==2) {
                        txtdestresult.setText(place.getAddress().toString());
                        txtdestresult.setTextColor(Color.parseColor("#000000"));
                        txtdestlatresult.setText(place.getLatLng().latitude+"");
                        txtdestlngresult.setText(place.getLatLng().longitude+"");
                        //placeMarkerOnMap(place.getLatLng());
                        try {
                            getArrayLocationToMarkers();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){
                    SnackBarMsg("Failed to selecting place, please try again..");
                }
            }
        }
    }

    private void CalculatServer(String distancetotal, String distance, int distanceid, int priceid){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"excalculate";
        if (isNetworkAvailable() == true) {
            TaskCalcutate MyTask = new TaskCalcutate();
            MyTask.execute(token,distancetotal,pid+"",distance, distanceid+"", priceid+"");
        }
    }

    public class TaskCalcutate extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(SendOrder.this, android.R.style.Theme_Translucent);
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
                String distancetotal = value[1];
                String pid = value[2];
                String distance = value[3];
                String distanceid = value[4];
                String priceid = value[5];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
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

                    layoutsubmit.setVisibility(View.VISIBLE);
                    tblPrice.setVisibility(View.VISIBLE);
                    tdistance.setText("Distance : " + distancetexttotal);
                    tprice.setText("Price : " +tpricetotal);
                    tshipperprice.setText("+ BND. "+shipperprice+" per shipper");
                    btnOrder.setEnabled(true);
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

    private void getArrayLocationToMarkers()throws JSONException{
        distancetotal = "0";
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
                EditText txtdest = (EditText) child.findViewById(100 + i);
                TextView txtdestlat = (TextView) child.findViewById(200 + i);
                TextView txtdestlng = (TextView) child.findViewById(300 + i);

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
                        obj.put("distanceid", 400 + i);
                        obj.put("priceid", 500 + i);
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

        placeMarkerOnMap(JSONOriLoc.toString(), JSONDestLoc.toString());
    }

    protected void placeMarkerOnMap(String OBJOri, String OBJDest) {
        if(mMap!=null){
            mMap.clear();
            markerPoints.clear();
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
                addDest.setVisibility(View.VISIBLE);
                btnOrder.setEnabled(false);
                for (int i = 0; i < destarr.length(); i++) {
                    JSONObject dtdest = destarr.getJSONObject(i);
                    String destitle = dtdest.getString("title");
                    String xdest = dtdest.getString("dest");
                    String deslat = dtdest.getString("deslat");
                    String deslng = dtdest.getString("deslng");
                    distanceid = Integer.parseInt(dtdest.getString("distanceid"));
                    priceid = Integer.parseInt(dtdest.getString("priceid"));

                    Log.e("DEST ARRAY : ", destitle+", "+distanceid+", "+priceid);

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
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addRowDestination() {
        addDest.setVisibility(View.GONE);
        final TableLayout tableDestination = (TableLayout) findViewById(R.id.tableDestination);
        LayoutInflater inflater = getLayoutInflater();
        final TableRow rowDestination = (TableRow)inflater.inflate(R.layout.row_destsendorder, tableDestination, false);
        final EditText txtdest = (EditText)rowDestination.findViewById(R.id.txtdest);
        final TextView txtdestlat = (TextView)rowDestination.findViewById(R.id.txtdestlat);
        final TextView txtdestlng = (TextView)rowDestination.findViewById(R.id.txtdestlng);
        final TextView txtdistance = (TextView)rowDestination.findViewById(R.id.txtdistance);
        final TextView txtprice = (TextView)rowDestination.findViewById(R.id.txtprice);

        txtdest.setId(100+tableDestination.indexOfChild(rowDestination));
        txtdestlat.setId(200+tableDestination.indexOfChild(rowDestination));
        txtdestlng.setId(300+tableDestination.indexOfChild(rowDestination));
        txtdistance.setId(400+tableDestination.indexOfChild(rowDestination));
        txtprice.setId(500+tableDestination.indexOfChild(rowDestination));
        disableEditText(txtdest);

        txtdest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtdest.setId(100+tableDestination.indexOfChild(rowDestination));
                txtdestresult = (EditText)rowDestination.findViewById(txtdest.getId());

                txtdestlat.setId(200+tableDestination.indexOfChild(rowDestination));
                txtdestlatresult = (TextView)rowDestination.findViewById(txtdestlat.getId());

                txtdestlng.setId(300+tableDestination.indexOfChild(rowDestination));
                txtdestlngresult = (TextView)rowDestination.findViewById(txtdestlng.getId());

                txtdistance.setId(400+tableDestination.indexOfChild(rowDestination));
                txtprice.setId(500+tableDestination.indexOfChild(rowDestination));

                //Toast(0,"Baris ke : "+txtdestlatlng.getId());

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
                    startActivityForResult(builder.build(SendOrder.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        tableDestination.addView(rowDestination);

        Button remove = (Button)rowDestination.findViewById(R.id.removeDest);
        int x=0;
        for(int i=0; i < tableDestination.getChildCount(); i++) {
            View child = tableDestination.getChildAt(i);
            TextView element1 = (TextView)child.findViewById(R.id.desttitle);
            Button element2 = (Button)child.findViewById(R.id.removeDest);
            if (element1 != null) {
                x = x+1;
                element1.setText("DESTINATION ("+x+")");
            }
        }

        if(x > 1){
            remove.setVisibility(View.VISIBLE);
        }else{
            remove.setVisibility(View.GONE);
        }

        remove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rowDestination.removeAllViews();
                int y = 0;
                for(int i=0; i < tableDestination.getChildCount(); i++) {
                    View child = tableDestination.getChildAt(i);
                    TextView element1 = (TextView)child.findViewById(R.id.desttitle);
                    if (element1 != null) {
                        y = y+1;
                        element1.setText("DESTINATION ("+y+")");
                        //Log.e("Destionation ke ",y+"");
                    }
                }

                try {
                    getArrayLocationToMarkers();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}