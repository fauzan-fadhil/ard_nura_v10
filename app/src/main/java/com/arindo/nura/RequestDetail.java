package com.arindo.nura;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by bmaxard on 16/10/2016.
 */

public class RequestDetail extends AppCompatActivity {
    private LinearLayout layoutRequest, layoutOrder, layoutRincian;
    private TextView ttiket, tnama, talamat, ttelp, tket, treqtime, tstatus, ttimetakeorder, ttimeorder, tpaymenttime, tgrandtotal, tinfo, tinfoadmin, texprice, trincian;
    private String csrid, tiket, nama, alamat, telp, ket, reqtime, status, jenis, statusid, taketime, ordertime, paymenttime, deskripsi, exprice;
    private SqlHelper dbHelper;
    private String SERVERADDR, MyJSON, tmDevice, versi;
    private int timeoutdata = 30000;
    private TableLayout tableRincian, tableTotalRincian, tableRincianBiaya;
    private TableRow tRow1, tRow2, tRow3, rowInfoAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_request_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        this.setTitle("Detail Permohonan");

     //   TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
      //  tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper = new SqlHelper(this);
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        Intent main = getIntent();
        csrid = main.getExtras().getString("csrid");
        tiket = main.getExtras().getString("tiket");
        nama = main.getExtras().getString("nama");
        alamat = main.getExtras().getString("alamat");
        telp = main.getExtras().getString("telp");
        jenis = main.getExtras().getString("jenis");
        ket = main.getExtras().getString("ket");
        reqtime = main.getExtras().getString("reqtime");
        statusid = main.getExtras().getString("status");
        status = main.getExtras().getString("tstatus");
        taketime = main.getExtras().getString("taketime");
        ordertime = main.getExtras().getString("ordertime");
        paymenttime = main.getExtras().getString("paymenttime");
        deskripsi = main.getExtras().getString("deskripsi");
        exprice = main.getExtras().getString("exprice");

        layoutRequest = (LinearLayout) findViewById(R.id.layoutRequest);
        layoutOrder = (LinearLayout)findViewById(R.id.layoutOrder);
        layoutRincian = (LinearLayout)findViewById(R.id.layoutRincian);
        tableRincian = (TableLayout)findViewById(R.id.tableRincian);

        tableTotalRincian = (TableLayout)findViewById(R.id.tableTotalRincian);
        tableRincianBiaya = (TableLayout)findViewById(R.id.tableRincianBiaya);
        trincian = (TextView) findViewById(R.id.trincian);

        tgrandtotal = (TextView) findViewById(R.id.tgrandtotal);
        tinfo = (TextView) findViewById(R.id.tinfo);
        tinfoadmin = (TextView) findViewById(R.id.tinfoadmin);
        texprice = (TextView) findViewById(R.id.texprice);

        tRow1 = (TableRow)findViewById(R.id.tRow1);
        tRow2 = (TableRow)findViewById(R.id.tRow2);
        tRow3 = (TableRow)findViewById(R.id.tRow3);
        rowInfoAdmin = (TableRow)findViewById(R.id.rowInfoAdmin);
        ttiket = (TextView)findViewById(R.id.ttiket);
        tnama = (TextView)findViewById(R.id.tnama);
        talamat = (TextView)findViewById(R.id.talamat);
        ttelp = (TextView)findViewById(R.id.ttelp);
        tket = (TextView)findViewById(R.id.tket);
        treqtime = (TextView)findViewById(R.id.ttime);
        tstatus = (TextView)findViewById(R.id.tstatus);
        ttimetakeorder = (TextView)findViewById(R.id.ttimetakeorder);
        ttimeorder = (TextView)findViewById(R.id.ttimeorder);
        tpaymenttime = (TextView) findViewById(R.id.tpaymenttime);

        if(statusid.equals("1")){
            layoutOrder.setVisibility(View.VISIBLE);
            tRow2.setVisibility(View.VISIBLE);
            texprice.setText("Rp. "+ moneyFormat(Double.parseDouble(exprice)));
        }
        if(Integer.parseInt(statusid) > 1){
            layoutOrder.setVisibility(View.VISIBLE);
            layoutRincian.setVisibility(View.VISIBLE);
            tRow1.setVisibility(View.VISIBLE);
            if(statusid.equals("2")) {
                tinfo.setVisibility(View.VISIBLE);
            }
            if(Integer.parseInt(statusid) > 2) {
                tRow3.setVisibility(View.VISIBLE);
            }

            if(!jenis.equals("1")){
                tableRincian.setVisibility(View.GONE);
                tableTotalRincian.setVisibility(View.GONE);
            }

            RequestBilling(csrid, tiket);
        }


        ttiket.setText(tiket);
        tnama.setText(nama);
        talamat.setText(alamat);
        ttelp.setText(telp);
        tket.setText(ket+" "+deskripsi);
        treqtime.setText(reqtime);
        tstatus.setText(status);
        ttimetakeorder.setText(taketime);
        ttimeorder.setText(ordertime);
        tpaymenttime.setText(paymenttime);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void RequestBilling(String csrid, String tiket){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"billreqcsr";
        if (isNetworkAvailable() == true) {
            TaskBilling MyTask = new TaskBilling();
            MyTask.execute(csrid, tiket);
        }else {
            Toast(2, "Internet not connected !");
        }
    }

    public class TaskBilling extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        final Dialog dialog = new Dialog(RequestDetail.this, android.R.style.Theme_Translucent);
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
                final String csrid = value[0];
                final String tiket = value[1];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("csrid",csrid));
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
                    String trincian  = jobject.getString("trincian");
                    String tagihan  = jobject.getString("tagihan");
                    String info  = jobject.getString("info");
                    String infoadm  = jobject.getString("infoadm");

                    JSONArray listrequest = null;
                    listrequest = jobject.getJSONArray("rincian");
                    int progress=0;
                    for(int i=0;i<listrequest.length();i++) {

                        JSONObject dt = listrequest.getJSONObject(i);
                        final String item = dt.getString("item");
                        final String harga = dt.getString("harga");
                        final String qty = dt.getString("qty");
                        final String satuan = dt.getString("satuan");
                        final String total = dt.getString("total");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AddRincianBilling(item, harga, qty, satuan, total);
                            }
                        });

                        progress++;
                        publishProgress(progress);
                    }

                    if(progress==0){
                        JSONArray listbiaya = null;
                        listbiaya = jobject.getJSONArray("biaya");
                        for(int i=0;i<listbiaya.length();i++) {

                            JSONObject dt = listbiaya.getJSONObject(i);
                            final String ket = dt.getString("ket");
                            final String nominal = dt.getString("nominal");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AddRincianBiaya(ket, nominal);
                                }
                            });
                        }

                        result = new String[] {"1",trincian, tagihan, info, infoadm};
                    }else if(progress==listrequest.length()){
                        JSONArray listbiaya = null;
                        listbiaya = jobject.getJSONArray("biaya");
                        for(int i=0;i<listbiaya.length();i++) {

                            JSONObject dt = listbiaya.getJSONObject(i);
                            final String ket = dt.getString("ket");
                            final String nominal = dt.getString("nominal");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AddRincianBiaya(ket, nominal);
                                }
                            });
                        }

                        result = new String[] {"1",trincian, tagihan, info, infoadm};
                    }
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
                trincian.setText(moneyFormat(Double.parseDouble(result[1])));
                tgrandtotal.setText(moneyFormat(Double.parseDouble(result[2])));
                tinfo.setText(result[3]);
                if(!result[4].equals("")){
                    rowInfoAdmin.setVisibility(View.VISIBLE);
                    tinfoadmin.setText(result[4]);
                }
                dialog.dismiss();
            }else if(rc.equals("88") || rc.equals("101")){
                Toast(2,"RC:"+rc+" >> Request gagal !");
                dialog.dismiss();
            }else if(rc.equals("99")){
                Toast(1,"RC:"+rc+" >> Request Time Out !");
                dialog.dismiss();
            }else{
                String msg = result[1];
                Toast(1,"RC:"+rc+" >> "+msg);
                dialog.dismiss();
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

    private void AddRincianBilling(final String item, final String harga, final String jml,
                                   final String satuan, final String total){
        LayoutInflater inflater = getLayoutInflater();
        TableRow tr = (TableRow)inflater.inflate(R.layout.row_rincian, tableRincian, false);

        TextView tv1 = (TextView) tr.findViewById(R.id.titem);
        tv1.setText(item);
        TextView tv3 = (TextView) tr.findViewById(R.id.tharga);
        tv3.setText(harga);
        TextView tv4 = (TextView) tr.findViewById(R.id.tjml);
        tv4.setText(jml);
        TextView tv5 = (TextView) tr.findViewById(R.id.tsatuan);
        tv5.setText(satuan);
        TextView tv6 = (TextView) tr.findViewById(R.id.ttotal);
        tv6.setText(moneyFormat(Double.parseDouble(total))+"");

        tableRincian.addView(tr);
    }

    private void AddRincianBiaya(final String ket, final String nominal){
        LayoutInflater inflater = getLayoutInflater();
        TableRow tr = (TableRow)inflater.inflate(R.layout.row_rincian_biaya, tableRincianBiaya, false);

        TextView tv1 = (TextView) tr.findViewById(R.id.tket);
        tv1.setText(ket);
        TextView tv2 = (TextView) tr.findViewById(R.id.tnominal);
        tv2.setText(moneyFormat(Double.parseDouble(nominal)));

        tableRincianBiaya.addView(tr);
    }

    private String moneyFormat(double val) {
        /*DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("");
        formatRp.setMonetaryDecimalSeparator('.');
        formatRp.setGroupingSeparator(',');

        kursIndonesia.setDecimalFormatSymbols(formatRp);

        return kursIndonesia.format(val);*/
        DecimalFormat formatter = new DecimalFormat("#,###,###");
        return formatter.format(val);
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

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }
}
