package com.arindo.nura;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;

public class HistoryBooking extends Fragment {
    SqlHelper dbHelper;
    String SERVERADDR, MyJSON, versi, csrid;
    int timeoutdata = 30000, listindex;
    TableLayout tableHistory;
    ProgressBar pbarhistory;
    public static ListView mylist;
    TextView tmsg;
    String token = null;
    Context context;

    public static final String TAG_PID = "pid";
    public static final String TAG_TIKET = "tiket";
    public static final String TAG_DATE = "orderdate";
    public static final String TAG_DESC = "destination";
    public static final String TAG_STATUS = "status";
    public static final String TAG_IMAGE = "image";

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.history_progress, container, false);

        v = inflater.inflate(R.layout.history_booking, container, false);
        context = v.getContext();

        dbHelper = new SqlHelper(getContext());
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);

        pbarhistory = (ProgressBar)v.findViewById(R.id.progressbar);
        mylist = (ListView) v.findViewById(R.id.list);
        tmsg = (TextView)v.findViewById(R.id.tmsg);
        FirebaseApp.initializeApp(getContext());
        token = FirebaseInstanceId.getInstance().getToken();
        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(getActivity());
        csrid = csrAccount.setid();

        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"orderhistorycsr";
        if (isNetworkAvailable() == true) {
            TaskHistory MyTask = new TaskHistory();
            MyTask.execute(token);
        }else {
            Toast.makeText(getActivity(),"Internet not connected",Toast.LENGTH_LONG).show();
        }
        return v;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public class TaskHistory extends AsyncTask<String, Integer, String[]> {
        String[] result = null;
        String statuskomplit = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String[] doInBackground(String... value) {
            try {
                final String token = value[0];

                ArrayList<BasicNameValuePair> post = new ArrayList<BasicNameValuePair>(1);
                post.add(new BasicNameValuePair("token",token));
                post.add(new BasicNameValuePair("csrid",csrid));
                post.add(new BasicNameValuePair("status","0"));
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
                showList(context);
                pbarhistory.setVisibility(View.GONE);
            }else if(rc.equals("88") || rc.equals("101")){
                //Toast(2,"RC:"+rc+" >> Request gagal !");
                pbarhistory.setVisibility(View.GONE);
            }else if(rc.equals("99")){
                //Toast(1,"RC:"+rc+" >> Request Time Out !");
                pbarhistory.setVisibility(View.GONE);
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                pbarhistory.setVisibility(View.GONE);
                mylist.setVisibility(View.GONE);
                tmsg.setVisibility(View.VISIBLE);
                tmsg.setText(msg);
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

    public void showList(Context con){
        LazyAdapterHistoryList adapter;
        ArrayList<HashMap<String, String>> ListHistory = new ArrayList<HashMap<String, String>>();
        JSONArray peoples = null;
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("orderhistory");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String pid = c.getString(TAG_PID);
                String tiket = c.getString(TAG_TIKET);
                String orderdate = c.getString(TAG_DATE);
                String desc = c.getString(TAG_DESC);
                String status = c.getString(TAG_STATUS);

                HashMap<String, String> map = new HashMap<String,String>();
                map.put(TAG_PID,pid);
                map.put(TAG_TIKET,tiket);
                map.put(TAG_DATE,orderdate);
                map.put(TAG_DESC,desc);
                map.put(TAG_STATUS,status);
                ListHistory.add(map);
                jml++;
            }
            try {
                adapter = new LazyAdapterHistoryList(con, ListHistory);
                mylist.setAdapter(adapter);
                mylist.setSelection(listindex);

                mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // selected item
                        //String lst_txt = parent.getItemAtPosition(position).toString().trim();

                        listindex = position;

                        TextView ttiket = (TextView) view.findViewById(R.id.ttiket);
                        TextView tpid = (TextView) view.findViewById(R.id.tpid);
                        String lst_txt1 = String.valueOf(ttiket.getText());
                        String lst_txt2 = String.valueOf(tpid.getText());

                        // Launching new Activity on selecting single List Item
                        Intent i = null;
                        if(lst_txt2.equals("3")){i = new Intent(getActivity(), HistoryDetailSend.class);}
                        else if(lst_txt2.equals("4")){i = new Intent(getActivity(), HistoryDetailClean.class);}
                        else if(lst_txt2.equals("5")){i = new Intent(getActivity(), HistoryDetailFood.class);}
                        else{i = new Intent(getActivity(), HistoryDetail.class);}
                        // sending data to new activity
                        i.putExtra("token",token);
                        i.putExtra("tiket",lst_txt1 );
                        i.putExtra("order",Integer.parseInt(lst_txt2));
                        i.putExtra("listindex",listindex);
                        startActivity(i);
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

    protected void __showList(){
        JSONArray peoples = null;
        ArrayList<HashMap<String, String>> personList;
        personList = new ArrayList<HashMap<String, String>>();
        //MyJSON = "{'status':'1','result':[{'pid':'JSR00000001','orderdate':'RAFA NAIZAR','destination':'BANDUNG'},{'pid':'JSR00000002','orderdate':'WILLY YANTI','destination':'LAHAT'}]}";
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("orderhistory");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String tiket = c.getString("tiket");
                String pid = c.getString("pid");
                String orderdate = c.getString("orderdate");
                String destination = c.getString("destination");

                HashMap<String,String> persons = new HashMap<String,String>();

                persons.put("tiket",tiket);
                persons.put("pid",pid);
                persons.put("orderdate",orderdate);
                persons.put("destination", String.valueOf(Html.fromHtml(destination)));
                if(pid.equals("0")){persons.put("image",String.valueOf(R.drawable.ic_grey_ride));}
                if(pid.equals("1")){persons.put("image",String.valueOf(R.drawable.ic_grey_car));}
                if(pid.equals("2")){persons.put("image",String.valueOf(R.drawable.ic_grey_boat));}
                if(pid.equals("3")){persons.put("image",String.valueOf(R.drawable.ic_grey_send));}
                if(pid.equals("4")){persons.put("image",String.valueOf(R.drawable.ic_grey_clean));}
                if(pid.equals("5")){persons.put("image",String.valueOf(R.drawable.ic_grey_food));}
                if(pid.equals("6")){persons.put("image",String.valueOf(R.drawable.ic_grey_tick));}
                if(pid.equals("7")){persons.put("image",String.valueOf(R.drawable.ic_grey_towing));}
                personList.add(persons);

                jml++;
            }
            try {
                ListAdapter adapter = new SimpleAdapter(getActivity(), personList, R.layout.row_history,
                        new String[]{"tiket", "pid", "image", "orderdate", "destination"},
                        new int[]{R.id.ttiket, R.id.tpid, R.id.imgproduct, R.id.tdate, R.id.tdes}
                );

                mylist.setAdapter(adapter);

                mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        // selected item
                        //String lst_txt = parent.getItemAtPosition(position).toString().trim();

                        TextView ttiket = (TextView) view.findViewById(R.id.ttiket);
                        TextView tpid = (TextView) view.findViewById(R.id.tpid);
                        String lst_txt1 = String.valueOf(ttiket.getText());
                        String lst_txt2 = String.valueOf(tpid.getText());

                        // Launching new Activity on selecting single List Item
                        Intent i = null;
                        if(lst_txt2.equals("3")){i = new Intent(getActivity(), HistoryDetailSend.class);}
                        else if(lst_txt2.equals("4")){i = new Intent(getActivity(), HistoryDetailClean.class);}
                        else if(lst_txt2.equals("5")){i = new Intent(getActivity(), HistoryDetailFood.class);}
                        else{i = new Intent(getActivity(), HistoryDetail.class);}
                        // sending data to new activity
                        i.putExtra("token",token);
                        i.putExtra("tiket",lst_txt1 );
                        i.putExtra("order",Integer.parseInt(lst_txt2));
                        startActivity(i);
                    }
                });
            }catch (Exception e){
                Log.e("CUT","Cut process");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addRowHistory(String tiket, String pid, String departure, String destination, String orderdate){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        TableRow tr = (TableRow) inflater.inflate(R.layout.row_history, tableHistory, false);
        ImageView imgtv1 = (ImageView) tr.findViewById(R.id.imgproduct);
        if(pid.equals("0")){imgtv1.setImageResource(R.drawable.motornura);}
        if(pid.equals("1")){imgtv1.setImageResource(R.drawable.ic_dekstop_car);}
        if(pid.equals("2")){imgtv1.setImageResource(R.drawable.ic_dekstop_boat);}
        if(pid.equals("3")){imgtv1.setImageResource(R.drawable.ic_dekstop_send);}
        if(pid.equals("4")){imgtv1.setImageResource(R.drawable.ic_dekstop_clean);}
        if(pid.equals("5")){imgtv1.setImageResource(R.drawable.ic_sembako);}
        if(pid.equals("6")){imgtv1.setImageResource(R.drawable.ic_dekstop_tick);}
        TextView tv1 = (TextView) tr.findViewById(R.id.tdate);
        tv1.setText(orderdate);
        TextView tv2 = (TextView) tr.findViewById(R.id.tdes);
        tv2.setText(destination);
        tr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Bundle bd = new Bundle();
                bd.putString("csrid", csrid);
                bd.putString("tiket", tiket);
                Intent i = new Intent(HistoryProgress.this, RequestDetail.class);
                i.putExtras(bd);
                startActivity(i);*/
            }
        });
        tableHistory.addView(tr);
    }
}
