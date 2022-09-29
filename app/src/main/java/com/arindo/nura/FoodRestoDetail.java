package com.arindo.nura;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by bmaxard on 26/01/2017.
 */

public class FoodRestoDetail extends AppCompatActivity {
    private String SERVERADDR, csrid, email, tmDevice, versi, token;
    private TextView trestoaddress;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private ImageView imgtitle;
    private CardView cview1, cview2;
    Toolbar toolbar;
    Snackbar snackBar;
    int timeoutdata = 20000;
    public static String TAG_RESTOID = "restoid";
    public static final String TAG_GROUPID = "groupid";
    public static final String TAG_GROUPNAME = "groupname";
    public static String MyJSON;
    public static com.arindo.nura.BadgeView TXT_TOTALQTY;
    public static TextView TXT_TOTALPRICE;
    public static LinearLayout layoutinclude;
    public static String restoid;

    private CollapsingToolbarLayout collapsingToolbarLayout = null;

    public static RecyclerView recyclerView;
    public static LazyAdapterRestoDetail islandAdapter;
    public static List<LazyAdapterRestoDetailModel> islandList;
    public static JSONObject jsonObjGroup;

    SwipeRefreshLayout swLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_foodrestodetail);

        if(MainActivity.loaderActivity.isShowing()) {
            MainActivity.loaderActivity.dismiss();
        }

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarTextAppernce();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        //this.setTitle(null);

      //  TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        pbar = (ProgressBar) findViewById(R.id.progressbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        imgtitle = (ImageView) findViewById(R.id.imgtitle);
        cview1 = (CardView) findViewById(R.id.cview1);
        cview2 = (CardView) findViewById(R.id.cview2);
        trestoaddress = (TextView) findViewById(R.id.trestoaddress);

        TXT_TOTALQTY = (com.arindo.nura.BadgeView) findViewById(R.id.tcount);
        TXT_TOTALPRICE = (TextView) findViewById(R.id.ttotal);
        layoutinclude = (LinearLayout) findViewById(R.id.layoutinclude);
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        restoid = main.getExtras().getString("restoid");

        recyclerView = (RecyclerView) findViewById(R.id.FoodDetailRecyclerView);
        RequestFoodDetail();

        layoutinclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                /*
                Intent i = null;
                i = new Intent(FoodRestoDetail.this, FoodOrder.class);
                i.putExtra("restoid",restoid);
                startActivity(i);
                */
            }
        });

        swLayout = (SwipeRefreshLayout) findViewById(R.id.swlayout);
        swLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RequestFoodDetail();
                swLayout.setRefreshing(false);
            }
        });
    }


    private void showDialog(){
        final Dialog dialog = new Dialog(FoodRestoDetail.this);
        dialog.setContentView(R.layout.layout_ordertype);
        //dialog.setTitle("Pengambilan Pesanan");

        TextView text = (TextView) dialog.findViewById(R.id.tv_desc);
        Button dialogButtonKirim = (Button) dialog.findViewById(R.id.bt_kirim);
        Button dialogButtonAmbil = (Button) dialog.findViewById(R.id.bt_ambil);
        /**
         * Jika tombol diklik, tutup dialog
         */

        text.setText("PENGIRIMAN PESANAN :");
        dialogButtonKirim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = null;
                i = new Intent(FoodRestoDetail.this, FoodOrder.class);
                i.putExtra("restoid",restoid);
                startActivity(i);
                dialog.dismiss();
            }
        });

        dialogButtonAmbil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = null;
                i = new Intent(FoodRestoDetail.this, FoodOrderNonKurir.class);
                i.putExtra("restoid",restoid);
                startActivity(i);
                dialog.dismiss();
            }
        });

        dialog.show();
        /*
        CharSequence[] pesan = {" - Kirim Pesanan"," - Ambil Ditempat"};

        //public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(FoodRestoMenu.this,R.style.AlertDialogStyle));

        builder.setTitle(R.string.action_order);
        builder.setItems(pesan, new DialogInterface.OnClickListener() {
                    public void onClick (DialogInterface dialog, int which){
                        if(which == 0){
                            Intent i = null;
                            i = new Intent(FoodRestoMenu.this, FoodOrder.class);
                            i.putExtra("restoid",restoid);
                            startActivity(i);
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        //}
        alertDialog.show();*/
    }

    private void RequestFoodDetail(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"foodrestodetail";
        if (isNetworkAvailable() == true) {
            TaskRestoList MyTask = new TaskRestoList();
            MyTask.execute();
            TempOrder(this);
        }else {
            SnackBarMsg("Tidak ada koneksi internet");
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

    public void TempOrder(Context con){
        //NumberFormat nfout = NumberFormat.getNumberInstance(Locale.ENGLISH);
        //nfout.setMaximumFractionDigits(2);
        GetTmpOrder sumQty = new GetTmpOrder();
        sumQty.SelectTempTotal(con,restoid,1);
        TXT_TOTALQTY.setText(String.valueOf(sumQty.setTotalQty()));
        //TXT_TOTALPRICE.setText("BND. "+nfout.format(sumQty.setTotalAmmount()));
        TXT_TOTALPRICE.setText("Rp. "+currencyformat(sumQty.setTotalAmmount()));
        if(sumQty.setTotalQty() > 0){
            layoutinclude.setVisibility(View.VISIBLE);
        }else{
            layoutinclude.setVisibility(View.GONE);
        }
    }

    private void toolbarTextAppernce() {
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
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
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("restoid",restoid));
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
                    String id = jobject.getString("id");
                    String title = jobject.getString("title");
                    String desc = jobject.getString("desc");
                    String imagetitleurl = jobject.getString("imagetitleurl");
                    result = new String[] {"1",id,title,desc,imagetitleurl};
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
                String id = result[1];
                String title = result[2];
                String desc = result[3];
                String imagetitleurl = result[4];
                showList(id,title,desc,imagetitleurl);
                pbar.setVisibility(View.GONE);
                //cview1.setVisibility(View.VISIBLE);
                cview2.setVisibility(View.VISIBLE);
            }else if(rc.equals("88") || rc.equals("101")){
                pbar.setVisibility(View.GONE);
                cview1.setVisibility(View.GONE);
                cview2.setVisibility(View.GONE);
            }else if(rc.equals("99")){
                pbar.setVisibility(View.GONE);
                cview1.setVisibility(View.GONE);
                cview2.setVisibility(View.GONE);
            }else{
                String msg = result[1];
                pbar.setVisibility(View.GONE);
                cview1.setVisibility(View.GONE);
                cview2.setVisibility(View.GONE);
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

    protected void showList(String id, String title, String desc, String imagetitleurl){
        TAG_RESTOID = id;
        collapsingToolbarLayout.setTitle(title);
        //collapsingToolbarLayout.setTitle("");
        trestoaddress.setText(desc);
        //trestoaddress.setText("");

        Picasso.with(this.getApplicationContext())
                .load(imagetitleurl)
                .error(R.color.grey_500)
                //.resize(500,350)
                .into(imgtitle);

        mmSliderFavorite();
        rowlistgroup(this);
    }

    public void onRefesh(Context con){
        rowlistgroup(con);
    }

    public static void rowlistgroup(Context con){
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(null);
        recyclerView.setLayoutManager(linearLayoutManager);
        ArrayList islandList = new ArrayList<>();
        JSONArray peoples = null;
        try {
            jsonObjGroup = new JSONObject(MyJSON);
            peoples = jsonObjGroup.getJSONArray("listfoodrestodetail");
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                final String groupid = c.getString(TAG_GROUPID);
                String groupname = c.getString(TAG_GROUPNAME);

                islandList.add(new LazyAdapterRestoDetailModel(groupid, groupname));
            }
            LazyAdapterRestoDetail islandAdapter = new LazyAdapterRestoDetail(islandList, con);
            recyclerView.setAdapter(islandAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void mmSliderFavorite(){
        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("listfoodfavorite");
            int jml=0;
            final LinearLayout mGallery = (LinearLayout) findViewById(R.id.layoutmenufavorite);
            final LayoutInflater mInflater = LayoutInflater.from(this);
            if(mGallery.getChildCount() > 0) mGallery.removeAllViews();
            if(peoples.length()>0){
                cview1.setVisibility(View.VISIBLE);
            }else{
                cview1.setVisibility(View.GONE);
            }
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                final String groupid = c.getString("groupid");
                final String groupname = c.getString("groupname");
                final String menuid = c.getString("menuid");
                final String menu = c.getString("menu");
                final String imageurl = c.getString("imageurl");

                runOnUiThread(new Runnable() {
                    public void run() {
                        View view = mInflater.inflate(R.layout.row_foodfavorite, mGallery, false);
                        ImageView img = (ImageView) view.findViewById(R.id.imageurl);

                         Picasso.with(FoodRestoDetail.this)
                                .load(imageurl)
                                .error(R.color.grey_500)
                                .resize(200,200)
                                .into(img);

                        final TextView id = (TextView) view.findViewById(R.id.menuid);
                        id.setText(menuid);
                        TextView txt = (TextView) view.findViewById(R.id.menu);
                        txt.setText(menu);
                        mGallery.addView(view);
                        img.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Toast.makeText(FoodRestoDetail.this, menuid, Toast.LENGTH_SHORT).show();
                                Intent i = null;
                                i = new Intent(FoodRestoDetail.this, FoodRestoMenu.class);
                                i.putExtra("restoid",restoid);
                                i.putExtra("groupid",groupid);
                                i.putExtra("groupname",groupname);
                                startActivity(i);
                            }
                        });
                    }
                });
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
}
