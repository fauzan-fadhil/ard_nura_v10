package com.arindo.nura;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class FoodRestoMenu extends AppCompatActivity{
    private String SERVERADDR, csrid, email, tmDevice, versi, token;
    public static String MyJSON;
    public static String restoid, groupid, groupname;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private TextView txttitle;
    static Toolbar toolbar;
    Snackbar snackBar;
    static Snackbar snackBarAmmount;
    int timeoutdata = 20000;
    LazyAdapterRestoMenu2 adapter;
    ArrayList<HashMap<String, String>> ListMenu = new ArrayList<HashMap<String, String>>();
    public static final String TAG_MENUID = "menuid";
    public static final String TAG_MENU = "menu";
    public static final String TAG_ADD = "countadd";
    public static final String TAG_PRICE = "price";
    public static final String TAG_IMAGE = "imageurl";
    public static final String TAG_DESC = "menudesc";
    //public static TextView TXT_TOTALQTY;
    public static com.arindo.nura.BadgeView TXT_TOTALQTY;
    public static TextView TXT_TOTALPRICE;
    public static LinearLayout layoutinclude;
    public static Activity activity;
    public static ListView mylist;
    public static FoodRestoMenu instance = null;

    RecyclerView recyclerView;
    LazyAdapterRestoMenu islandAdapter;
    public List<LazyAdapterRestoMenuModel> islandList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_foodrestomenu);

        //if(MainActivity.loaderActivity.isShowing()) {
        //    MainActivity.loaderActivity.dismiss();
        //}

        instance = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        this.setTitle(null);

      //  TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
       // tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        pbar = (ProgressBar) findViewById(R.id.progressbar);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);
        txttitle = (TextView) findViewById(R.id.txttitle);

        activity = this;
        mylist = (ListView) findViewById(R.id.list);
        TXT_TOTALQTY = (com.arindo.nura.BadgeView) findViewById(R.id.tcount);
        TXT_TOTALPRICE = (TextView) findViewById(R.id.ttotal);
        layoutinclude = (LinearLayout) findViewById(R.id.layoutinclude);
        //tambahan versi 10 os//
        FirebaseApp.initializeApp(getBaseContext());
        token = FirebaseInstanceId.getInstance().getToken();

        Intent main = getIntent();
        restoid = main.getExtras().getString("restoid");
        groupid = main.getExtras().getString("groupid");
        groupname = main.getExtras().getString("groupname");
        txttitle.setText(" "+groupname);

        TempOrder(this);

        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"foodrestomenu";
        if (isNetworkAvailable() == true) {
            TaskRestoMenu MyTask = new TaskRestoMenu();
            MyTask.execute();
        }else {
            SnackBarMsg("Tidak ada koneksi internet");
        }

        layoutinclude.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
                /*Intent i = null;
                i = new Intent(FoodRestoMenu.this, FoodOrder.class);
                i.putExtra("restoid",restoid);
                startActivity(i);*/
            }
        });
    }

    private void showDialog(){
        final Dialog dialog = new Dialog(FoodRestoMenu.this);
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
                i = new Intent(FoodRestoMenu.this, FoodOrder.class);
                i.putExtra("restoid",restoid);
                startActivity(i);
                dialog.dismiss();
            }
        });

        dialogButtonAmbil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = null;
                i = new Intent(FoodRestoMenu.this, FoodOrderNonKurir.class);
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

    @Override
    public void finish() {
        super.finish();
        instance = null;
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
        try {
            //NumberFormat nfout = NumberFormat.getNumberInstance(Locale.ENGLISH);
            //nfout.setMaximumFractionDigits(2);
            GetTmpOrder sumQty = new GetTmpOrder();
            sumQty.SelectTempTotal(con, restoid, 1);
            TXT_TOTALQTY.setText(String.valueOf(sumQty.setTotalQty()));
            //TXT_TOTALPRICE.setText("BND. " + nfout.format(sumQty.setTotalAmmount()));
            TXT_TOTALPRICE.setText("Rp " + currencyformat(sumQty.setTotalAmmount()));
            if (sumQty.setTotalQty() > 0) {
                layoutinclude.setVisibility(View.VISIBLE);
            } else {
                layoutinclude.setVisibility(View.GONE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public class TaskRestoMenu extends AsyncTask<String, Integer, String[]> {
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
                post.add(new BasicNameValuePair("groupid",groupid));
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
            String rc = result[0];
            if(rc.equals("1")){
                showList(activity);
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("88") || rc.equals("101")){
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("99")){
                pbar.setVisibility(View.GONE);
            }else{
                String msg = result[1];
                pbar.setVisibility(View.GONE);
                mylist.setVisibility(View.GONE);
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

    public void onRefesh(Activity activity){
        showList(activity);
    }

    public void SetListViewAdapter(ArrayList<HashMap<String, String>> restolist) {
        adapter = new LazyAdapterRestoMenu2(this, restolist, 0);
        mylist.setAdapter(adapter);
    }

    public static void showList(Activity activity){
        LazyAdapterRestoMenuImg adapter;
        ArrayList<HashMap<String, String>> ListSendOption = new ArrayList<HashMap<String, String>>();
        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("listfoodrestomenu");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String menuid = c.getString(TAG_MENUID);
                String menu = c.getString(TAG_MENU);
                String add = c.getString(TAG_ADD);
                String price = c.getString(TAG_PRICE);
                String menudesc = c.getString(TAG_DESC);
                String imagetitleurl = c.getString(TAG_IMAGE);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_MENUID,menuid);
                map.put(TAG_MENU,menu);
                map.put(TAG_ADD,add);
                map.put(TAG_PRICE,price);
                map.put(TAG_DESC,menudesc);
                map.put(TAG_IMAGE,imagetitleurl);
                ListSendOption.add(map);
            }
            //SetListViewAdapter(ListMenu);
            adapter = new LazyAdapterRestoMenuImg(activity, ListSendOption, 0);
            mylist.setItemsCanFocus(true);
            mylist.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void __showList(){
        recyclerView = (RecyclerView) findViewById(R.id.FoodMenuRecyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        islandList = new ArrayList<>();

        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("listfoodrestomenu");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String menuid = c.getString(TAG_MENUID);
                String menu = c.getString(TAG_MENU);
                String price = c.getString(TAG_PRICE);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_MENUID,menuid);
                map.put(TAG_MENU,menu);
                map.put(TAG_PRICE,price);
                ListMenu.add(map);

                islandList.add(new LazyAdapterRestoMenuModel(menuid, menu, price));
            }
            islandAdapter = new LazyAdapterRestoMenu(islandList, this);
            recyclerView.setAdapter(islandAdapter);
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

    public void SnackBarAmmount(){
        //final Snackbar snackBar = Snackbar.make(toolbar, msg, Snackbar.LENGTH_INDEFINITE);
        snackBarAmmount = Snackbar.make(toolbar, "TESSSSSS", Snackbar.LENGTH_INDEFINITE);
        snackBarAmmount.setActionTextColor(Color.parseColor("#FFFFFF"));

        View snackbarView = snackBarAmmount.getView();
        //snackbarView.setBackgroundColor(Color.WHITE);
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);

        snackBarAmmount.setAction("Close", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBarAmmount.dismiss();
            }
        });
        snackBarAmmount.show();
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
