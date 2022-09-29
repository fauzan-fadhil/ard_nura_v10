package com.arindo.nura;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        BaseSliderView.OnSliderClickListener, ViewPagerEx.OnPageChangeListener{

    private int pushMsg = 0;
    public static Dialog loaderActivity;
    private String tmDevice, versi, SERVERADDR, MyJSON;
    private Md5Hex MD5;
    private GridviewAdapter mAdapter;
    private ArrayList<String> listLayanan;
    private ArrayList<Integer> listIconLayanan;
    private ArrayList<Integer> listID;
    private GridView gridView;
    private File profile, imgbanner;
    public static ImageView imgprofile;
    public static TextView tnamemain;
    private SqlHelper dbHelper;
    private ProgressBar pbar;
    private int timeoutdata = 20000;
    private Toolbar toolbar;
    private Snackbar snackBar;

    SliderLayout sliderLayout;
    HashMap<String,File> Hash_file_maps ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);
        //tmDevice = telephonyInfo.getImsiSIM1();
        tmDevice  = Settings.Secure.getString(getApplication().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        MD5 = new Md5Hex();
        //versi = BuildConfig.VERSION_NAME;
        versi = String.valueOf(BuildConfig.VERSION_CODE);
        dbHelper = new SqlHelper(this);
        snackBar = Snackbar.make(toolbar, "", Snackbar.LENGTH_INDEFINITE);

        pbar = (ProgressBar)findViewById(R.id.pbar);
        //pbar.setVisibility(View.GONE);

        //Delete file apk bruconnect
        String apkpath = MyConfig.path()+"/Android/data/com.arindo.nura/file";
        File f = new File(apkpath);
        if(f.exists()) {
            File file[] = f.listFiles();
            for (File f1 : file) {
                if (f1.isFile() && f1.getPath().endsWith(".apk")) {
                    f1.delete();
                }
            }
        }

        //End Delete file apk bruconnect

        if(isNetworkAvailable()== true) {
            //CheckNewAppVersion cek = new CheckNewAppVersion();
            //cek.CheckVersion(this);
        }

        this.setTitle(null);

        /*try {
            Intent main = getIntent();
            pushMsg = main.getExtras().getInt("push");

            if(ceklogin()==false) {
                Intent i = new Intent(this, LoginActivity.class);
                startActivity(i);
                finish();
                return;
            }

            if(pushMsg==1){
                Intent i = new Intent(this, RequestList.class);
                startActivity(i);
            }
        }catch (Exception e){
            //
        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View v = navigationView.getHeaderView(0);
        tnamemain = (TextView ) v.findViewById(R.id.tusername);
        imgprofile = (ImageView) v.findViewById(R.id.imgUser);

        SetAccount csrAccount = new SetAccount();
        csrAccount.loadAccount(this);
        tnamemain.setText(csrAccount.setnama());

        try {
            if (!csrAccount.setimgprofile().toString().equals("") || csrAccount.setimgprofile() != null) {
                profile = new File(MyConfig.path() + "/Android/data/com.arindo.nura/file/" + csrAccount.setimgprofile().toString());
                if (profile.exists()) {
                    imgprofile.setImageURI(Uri.parse(profile.toString()));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        imgprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetAccount csrAccount = new SetAccount();
                csrAccount.loadAccount(MainActivity.this);

                try {
                    if (!csrAccount.setimgprofile().toString().equals("") || csrAccount.setimgprofile() != null) {
                        profile = new File(MyConfig.path() + "/Android/data/com.arindo.nura/file/" + csrAccount.setimgprofile().toString());
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

                Bundle bd = new Bundle();
                bd.putString("filePath", profile+"");
                Intent i = new Intent(MainActivity.this, ZoomImage.class);
                i.putExtras(bd);
                startActivity(i);
            }
        });

        prepareList();
        // prepared arraylist and passed it to the Adapter class
        mAdapter = new GridviewAdapter(this, listID, listLayanan, listIconLayanan);

        // Set custom adapter to gridview
        gridView = (GridView) findViewById(R.id.gridView1);
        gridView.setAdapter(mAdapter);

        loaderActivity = new Dialog(MainActivity.this, android.R.style.Theme_Translucent);
        loaderActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loaderActivity.setContentView(R.layout.custom_progress_dialog);
        loaderActivity.setCancelable(true);
        // Implement On Item click listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                String a = mAdapter.getItemId(position)+"";
                IntentRequest(Integer.parseInt(a));

                //Toast.makeText(MainActivity.this, mAdapter.getItemId(position) + " - "+
                //        mAdapter.getItem(position), Toast.LENGTH_SHORT).show();
            }
        });

        //mSlider();
        RequestBanner();
    }

    private boolean ceklogin(){
        boolean status = false;
        try{
            SetAccount csrAccount = new SetAccount();
            csrAccount.loadAccount(this);
            if(csrAccount.setdevice().toString().equals(MD5.Md5Hex(tmDevice))){
                status = true;
            }else{
                status = false;
            }
        }catch(Exception e){
            Log.e("Error Status Login",e.toString());
        }
        return status;
    }

    private void RequestBanner(){
        MyConfig config = new MyConfig();
        String host = config.hostname(dbHelper);
        SERVERADDR = host+"reqbanner";
        if (isNetworkAvailable() == true) {
            TaskBanner MyTask = new TaskBanner();
            MyTask.execute("123");
        }
    }

    public class TaskBanner extends AsyncTask<String, Integer, String[]> {
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
                pbar.setVisibility(View.GONE);
                mmSlider();
            }else if(rc.equals("88") || rc.equals("101")){
                //Toast(2,"RC:"+rc+" >> Request gagal !");
                pbar.setVisibility(View.GONE);
            }else if(rc.equals("99")){
                //Toast(1,"RC:"+rc+" >> Request Time Out !");
                pbar.setVisibility(View.GONE);
            }else{
                String msg = result[1];
                //Toast(1,"RC:"+rc+" >> "+msg);
                pbar.setVisibility(View.GONE);
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

    public class CustomSliderView extends BaseSliderView {
        public CustomSliderView(Context context) {
            super(context);
        }
        public View getView() {
            View v = LayoutInflater.from(this.getContext()).inflate(R.layout.render_type_text, null);
            ImageView target = (ImageView) v.findViewById(R.id.daimajia_slider_image);
            LinearLayout frame = (LinearLayout) v.findViewById(R.id.description_layout);
            frame.setBackgroundColor(Color.TRANSPARENT);

          this.bindEventAndShow(v, target);
            return v;
        }
    }


    public void mmSlider(){
        sliderLayout = (SliderLayout)findViewById(R.id.slider);
        JSONArray peoples = null;
        //ArrayList<HashMap<String, String>> personList;
        //personList = new ArrayList<HashMap<String, String>>();
        //MyJSON = "{'status':'1','result':[{'pid':'JSR00000001','orderdate':'RAFA NAIZAR','destination':'BANDUNG'},{'pid':'JSR00000002','orderdate':'WILLY YANTI','destination':'LAHAT'}]}";
        try {
            JSONObject jsonObj = new JSONObject(MyJSON);
            peoples = jsonObj.getJSONArray("banners");
            int jml=0;
            for(int i=0;i<peoples.length();i++){
                JSONObject c = peoples.getJSONObject(i);
                String imgid = c.getString("imgid");
                String typeid = c.getString("typeid");
                String file = c.getString("file");

                CustomSliderView textSliderView = new CustomSliderView(MainActivity.this);
                textSliderView
                        //.description(name)
                        .image(file)
                        .setScaleType(BaseSliderView.ScaleType.CenterCrop)
                        .setOnSliderClickListener(this);
                textSliderView.bundle(new Bundle());
                textSliderView.getBundle().putString("imgid",imgid);
                textSliderView.getBundle().putString("typeid",typeid);

                sliderLayout.addSlider(textSliderView);
            }
            sliderLayout.setVisibility(View.VISIBLE);
            sliderLayout.setPresetTransformer(SliderLayout.Transformer.Accordion);
            sliderLayout.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
            sliderLayout.setCustomAnimation(new DescriptionAnimation());
            sliderLayout.setDuration(3000);
            sliderLayout.addOnPageChangeListener(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        //sliderLayout.stopAutoCycle();
        //sliderLayout.refreshDrawableState();
        //sliderLayout.removeAllSliders();
        super.onStop();
    }

    @Override
    public void onSliderClick(BaseSliderView slider) {
        //Toast.makeText(this,slider.getBundle().get("extra") + "",Toast.LENGTH_SHORT).show();
        String typeid = slider.getBundle().get("typeid").toString();
        if(typeid.equals("0")) {
            loaderActivity.show();
            String a = slider.getBundle().get("imgid").toString();
            IntentRequest(Integer.parseInt(a));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        //Log.e("Slider Demo", "Page Changed: " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    public void prepareList(){
        listID = new ArrayList<Integer>();
        listID.add(0);
        listID.add(1);
        listID.add(2);
        listID.add(3);
        listID.add(4);
        listID.add(5);
        //listID.add(6);
        listID.add(7);
        listID.add(6);
        listID.add(8);
        /*listID.add(8);
        listID.add(9);
        listID.add(10);
        listID.add(11);*/

        listLayanan = new ArrayList<String>();
        listLayanan.add("");
        /*
        listLayanan.add("BC2 CAR");
        listLayanan.add("BC2 BOAT");
        listLayanan.add("BC2 SEND");
        listLayanan.add("BC2 CLEAN");
        */
        listLayanan.add("");
        //listLayanan.add("BC2 EVENT");
        //listLayanan.add("BC2 TOWING");
        //listLayanan.add("");
        listLayanan.add("");
        listLayanan.add("");
        /*listLayanan.add("BC2 DES");
        listLayanan.add("BC2 DST");
        listLayanan.add("BC2 INDOSAT");
        listLayanan.add("BC2 TELKOMSEL");*/

        listIconLayanan = new ArrayList<Integer>();
        listIconLayanan.add(R.drawable.motornura);
        /*
        listIconLayanan.add(R.drawable.ic_dekstop_car);
        listIconLayanan.add(R.drawable.ic_dekstop_boat);
        listIconLayanan.add(R.drawable.ic_dekstop_send);
        listIconLayanan.add(R.drawable.ic_dekstop_clean);
        */
        listIconLayanan.add(R.drawable.ic_sembako);
        //listIconLayanan.add(R.drawable.ic_dekstop_tick);
        //listIconLayanan.add(R.drawable.ic_dekstop_towing);
        //listIconLayanan.add(R.color.colorTransparent);
        //listIconLayanan.add(R.color.colorTransparent);
        listIconLayanan.add(R.drawable.logo_arindo);
        listIconLayanan.add(R.drawable.history);
        /*listIconLayanan.add(R.drawable.ic_dekstop_des);
        listIconLayanan.add(R.drawable.ic_dekstop_dst);
        listIconLayanan.add(R.drawable.ic_dekstop_indosat);
        listIconLayanan.add(R.drawable.ic_dekstop_telkomsel);*/
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            ClearCache Clear = new ClearCache();
            Clear.CacheClear();
            super.onBackPressed();
            /*Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_account) {
            Intent i = new Intent(this, Account.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, Setting.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_logout) {
            ShowAlert(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view list_item clicks here.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        int id = item.getItemId();
        if (id == R.id.nav_history) {
            Intent i = new Intent(this, History.class);
            startActivity(i);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_about) {
            IntentInfo(1);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_help) {
            IntentInfo(2);
            drawer.closeDrawer(GravityCompat.START);
        }

        if (id == R.id.nav_contact) {
            IntentInfo(3);
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void WebLink(String url){
        Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public static void refreshImage(String path){
        imgprofile.setImageURI(null);
        imgprofile.setImageURI(Uri.parse(path));
    }

    private void IntentRequest(int pid){
        if(pid==6 || pid==8){
            //if(pid==6){SnackBarMsg("BC2 EVENT on progress..");}
            //if(pid==8){SnackBarMsg("BC2 EVENT on progress..");}
            return;
        }
        Bundle bd = new Bundle();
        bd.putInt("order", pid);
        Intent i = null;
        loaderActivity.show();
        if(pid==3){
            //i = new Intent(this, SendOption.class);
            i = new Intent(this, History.class);
            loaderActivity.dismiss();
            i.putExtras(bd);
            startActivity(i);
        }else if(pid==4){
            i = new Intent(this, CleanOrder.class);
            i.putExtras(bd);
            startActivity(i);
       // }else if(pid==5){
        }else if(pid==1){
            i = new Intent(this, FoodOption.class);
            i.putExtras(bd);
            startActivity(i);
        }else if(pid==2){
            loaderActivity.dismiss();
            openApp(MainActivity.this, "com.onlinepayment");
            /*
            i = new Intent(this, FoodRestoList.class);
            String optionname = "SEMBAKO";
            String optionid = "0";
            i.putExtra("groupid",optionid);
            i.putExtra("foodtitle",optionname);
            */
            /*
            String rid = "PF00016";
            i = new Intent(this, FoodRestoDetail.class);
            i.putExtra("restoid",rid);
            startActivity(i);
            */
        }else{
            i = new Intent(this, RequestActivity2.class);
            i.putExtras(bd);
            startActivity(i);
        }
    }

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                MyConfig config = new MyConfig();
                String host = "";//config.hostname(dbHelper);
                DownloadApk download = new DownloadApk();
                //download.Download(context,0, "2", host);
                download.Download(context,0, "1", host);
                return false;
                //throw new ActivityNotFoundException();
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            context.startActivity(i);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    private void IntentInfo(int act){
        if(!isNetworkAvailable() && (act==2 || act==3)){
            Toast(2, "Mohon aktifkan koneksi internet pada perangkat Anda !");
            return;
        }

        if(act==2){
            SqlHelper dbHelper = new SqlHelper(this);
            MyConfig config = new MyConfig();
            String host = config.hostname(dbHelper);
            WebLink(host+"help");
        }else {
            Bundle bd = new Bundle();
            bd.putInt("act", act);
            Intent i = new Intent(this, Information.class);
            i.putExtras(bd);
            startActivity(i);
        }
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

    private void ShowAlert(final int param) {
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
            text.setText("Apakah anda akan keluar dari aplikasi ?");
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
                    doLogout();
                }
            });
        }
        dialog.show();
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

    private void doLogout(){
        try {
            SqlHelper dbHelper = new SqlHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("UPDATE tbl_account SET device = NULL WHERE idnom=1");
            db.close();
            System.exit(0);
            onBackPressed();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
