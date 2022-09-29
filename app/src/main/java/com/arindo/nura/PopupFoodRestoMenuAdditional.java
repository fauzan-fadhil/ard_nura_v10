package com.arindo.nura;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bmaxard on 22/05/2017.
 */

public class PopupFoodRestoMenuAdditional extends Dialog {
    Activity activity;
    String SERVERADDR, MyJSON, tmDevice, versi, csrid;
    SqlHelper dbHelper;
    int timeoutdata = 20000;
    public static TextView ttotal, tshipping, txtshipping;
    public static Dialog loaderActivity;
    public static final String TAG_CARTID = "cartid";
    public static final String TAG_CDID = "cdid";
    public static final String TAG_ITEMID = "itemid";
    public static final String TAG_TITLE = "item";
    public static final String TAG_QTY = "qty";
    public static final String TAG_PRICE = "price";
    public static final String TAG_PRICEVALUE = "pricevalue";
    public static final String TAG_IMAGE = "file";
    public static final String TAG_COLOR = "color";

    public PopupFoodRestoMenuAdditional(Activity act) {
        super(act);
        activity = act;

        //TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(activity);
        //tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(this.getContext().getContentResolver() , Settings.Secure.ANDROID_ID);

        versi = String.valueOf(BuildConfig.VERSION_CODE);
        dbHelper = new SqlHelper(activity);

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(activity);
        csrid = csrAccount.setid();

        loaderActivity = new Dialog(activity, android.R.style.Theme_Translucent);
        loaderActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loaderActivity.setContentView(R.layout.custom_progress_dialog);
        loaderActivity.setCancelable(true);
    }

    public void ShowAdditionalMenu() {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_foodrestomenu_additional);
        dialog.setCancelable(true);

        TextView ttitle = (TextView) dialog.findViewById(R.id.ttitle);
        txtshipping = (TextView) dialog.findViewById(R.id.txtshipping);
        tshipping = (TextView) dialog.findViewById(R.id.tshipping);
        ttotal = (TextView) dialog.findViewById(R.id.ttotal);
        TextView btn1 = (TextView) dialog.findViewById(R.id.btn1);
        //TextView btn2 = (TextView) dialog.findViewById(R.id.btn2);
        ListView list = (ListView) dialog.findViewById(R.id.list);
        //AdapterCartItem adapter;
        //ArrayList<HashMap<String, String>> ListCartItem = new ArrayList<HashMap<String, String>>();

        ttitle.setText("Add On");
        btn1.setText("Submit");
        //btn2.setText("Checkout");

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });

        /*tn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                /*Intent i = new Intent(con, Checkout.class);
                i.putExtra("title","Checkout");
                i.putExtra("myjson",MyJSON);
                con.startActivity(i);
                Toast.makeText(activity,"This is add on...",Toast.LENGTH_LONG).show();
            }
        });*/

        /*JSONArray peoples = null;
        try {
            if(list.getChildCount() > 0){ListCartItem.clear();}
            Log.e("JSON",MyJSON);
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("cartorder");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String cartid = c.getString(TAG_CARTID);
                String cdid = c.getString(TAG_CDID);
                String itemid = c.getString(TAG_ITEMID);
                String title = c.getString(TAG_TITLE);
                String qty = c.getString(TAG_QTY);
                String price = c.getString(TAG_PRICE);
                String pricevalue = c.getString(TAG_PRICEVALUE);
                String imageurl = c.getString(TAG_IMAGE);
                String color = c.getString(TAG_COLOR);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_CARTID,cartid);
                map.put(TAG_CDID,cdid);
                map.put(TAG_ITEMID,itemid);
                map.put(TAG_TITLE,title);
                map.put(TAG_QTY,qty);
                map.put(TAG_PRICE,price);
                map.put(TAG_PRICEVALUE,pricevalue);
                map.put(TAG_IMAGE,imageurl);
                map.put(TAG_COLOR,color);
                ListCartItem.add(map);
                jml++;
            }
            //adapter = new AdapterCartItem((Activity) con, ListCartItem);
            //list.setAdapter(adapter);
            totalorder();
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        dialog.show();
    }

    private void totalorder(){
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            String shippingvalue = jsonObj.getString("shippingvalue");
            String amountvalue = jsonObj.getString("amountvalue");
            txtshipping.setVisibility(View.GONE);
            tshipping.setVisibility(View.GONE);
            tshipping.setText(shippingvalue);
            ttotal.setText(amountvalue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void AdditionalMenu(){
        /*MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host + "additionalmenu";
        if (isNetworkAvailable() == true) {
            TaskShowAdditional MyTask = new TaskShowAdditional();
            MyTask.execute();
        }*/
        ShowAdditionalMenu();
    }

    public class TaskShowAdditional extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loaderActivity.show();
        }

        protected String[] doInBackground(String... value) {
            try {
                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("versi",versi));
                post.add(new BasicNameValuePair("macaddr",tmDevice));

                DefaultHttpClient httpclient = (DefaultHttpClient) com.org.apache.WebClientDevWrapper.getNewHttpClient(timeoutdata);
                HttpPost httppost = new HttpPost(SERVERADDR);

                httppost.setEntity(new UrlEncodedFormEntity(post, "UTF-8"));
                HttpResponse response = httpclient.execute(httppost);

                MyJSON = request(response);
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
        }

        protected void onCancelled() {
            super.onCancelled();
        }

        protected void onPostExecute(String[] result) {
            String rc = result[0];
            if(rc.equals("1")){
                loaderActivity.dismiss();
                ShowAdditionalMenu();
            }else if(rc.equals("88") || rc.equals("101")){
                loaderActivity.dismiss();
            }else if(rc.equals("99")){
                loaderActivity.dismiss();
            }else{
                String msg = result[1];
                Toast.makeText(activity,msg+" ("+rc+")",Toast.LENGTH_LONG).show();
                loaderActivity.dismiss();
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
}
