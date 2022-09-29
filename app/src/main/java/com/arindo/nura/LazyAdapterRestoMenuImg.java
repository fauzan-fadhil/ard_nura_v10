package com.arindo.nura;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;


/**
 * Created by bmaxard on 27/01/2017.
 */

public class LazyAdapterRestoMenuImg extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    private String[] arrTempQty, arrTempBuy, arrTempNote, arrTempNoteHint;
    private int param;
    private int cursor;
    //private ProgressBar pbar;
    private WebView webview;

    public LazyAdapterRestoMenuImg(Activity a, ArrayList<HashMap<String, String>> d, int p){
        activity = a;
        data = d;
        param = p;
        inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        arrTempQty = new String[getCount()];
        arrTempBuy = new String[getCount()];
        arrTempNote = new String[getCount()];
        arrTempNoteHint = new String[getCount()];

        notifyDataSetChanged();
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return data.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder{
        TextView menuid;
        TextView menu;
        TextView price;
        TextView plus;
        TextView minus;
        TextView qty;
        TextView buy;
        TextView pricecurrency;
        TextView buycurrency;
        ImageView imagetitleurl;

        EditText tnote;
        Button btnClearNote;

        NumberPicker numpicker;

        int ref;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //ViewHolder holder = null;
        final ViewHolder holder;
        final GetTmpOrder insertTemp = new GetTmpOrder();
        final View view = convertView;
        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_foodrestomenuimg, null);

            holder.menuid = (TextView) convertView.findViewById(R.id.tmenuid);
            holder.menu = (TextView) convertView.findViewById(R.id.tmenu);
            holder.price = (TextView) convertView.findViewById(R.id.tprice);
            holder.pricecurrency = (TextView) convertView.findViewById(R.id.tpricecurrency);

            holder.imagetitleurl = (ImageView) convertView.findViewById(R.id.image_produk);

            holder.plus = (TextView) convertView.findViewById(R.id.tnumpickerpositive);
            holder.minus = (TextView) convertView.findViewById(R.id.tnumpickernegative);
            holder.qty = (TextView) convertView.findViewById(R.id.tnumpickercount);
            holder.buy = (TextView) convertView.findViewById(R.id.tbuy);
            holder.buycurrency = (TextView) convertView.findViewById(R.id.tbuycurrency);

            holder.numpicker = (NumberPicker) convertView.findViewById(R.id.number_picker);

            holder.tnote = (EditText) convertView.findViewById(R.id.tnote);
            holder.btnClearNote = (Button)convertView.findViewById(R.id.btnClearNote);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        holder.ref = position;

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        holder.menuid.setText(listsendoption.get(FoodRestoMenu.TAG_MENUID));
        holder.menu.setText(listsendoption.get(FoodRestoMenu.TAG_MENU));
        final String dprice = listsendoption.get(FoodRestoMenu.TAG_PRICE);
        holder.price.setText(currencyformat(Double.parseDouble(dprice)));
        holder.pricecurrency.setText("Rp "+currencyformat(Double.parseDouble(dprice)));

        Picasso.with(activity)
                .load(listsendoption.get(FoodRestoMenu.TAG_IMAGE))
                .error(R.color.grey_500)
                //.resize(80,80)
                .into((holder.imagetitleurl));

//        Picasso.get()
 //               .load(listsendoption.get(FoodRestoMenu.TAG_IMAGE))
  //              .error(R.color.grey_500)
   //             //.resize(500,350)
    //            .into(holder.imagetitleurl);

        final String restoid = FoodRestoMenu.restoid;
        final String groupid = FoodRestoMenu.groupid;
        final String menuid = listsendoption.get(FoodRestoMenu.TAG_MENUID);
        final String menu = listsendoption.get(FoodRestoMenu.TAG_MENU);
        final String menudesc = listsendoption.get(FoodRestoMenu.TAG_DESC);
        final String countadd = listsendoption.get(FoodRestoMenu.TAG_ADD);
        final String imageUrl = listsendoption.get(FoodRestoMenu.TAG_IMAGE);

        GetTmpOrder selectQty = new GetTmpOrder();
        selectQty.SelectTempQty(activity,menuid);

        arrTempQty[position] = String.valueOf(selectQty.setQty());
        arrTempNote[position] = selectQty.setNotes();
        if(selectQty.setQty() > 0){
            arrTempBuy[position] = currencyformat(Double.parseDouble(dprice) * selectQty.setQty());
        }

        holder.qty.setText(arrTempQty[position]);
        holder.buy.setText(arrTempBuy[position]);
        if(!holder.buy.getText().toString().equals("")) {
            holder.buycurrency.setText("Rp " + holder.buy.getText().toString());
        }else{
            holder.buycurrency.setText("");
        }

        holder.imagetitleurl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.layout_detailproduk);
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                dialog.setTitle("Detail Produk");

                TextView text = (TextView) dialog.findViewById(R.id.tv_desc);
                ImageView image = (ImageView) dialog.findViewById(R.id.iv_icon);
                text.setText(menudesc);
                Bitmap mIconVal = null;


                try {
                    URL newurl = new URL(imageUrl);
                    mIconVal = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                image.setImageBitmap(mIconVal);

                /*
                WebView webview = (WebView) dialog.findViewById(R.id.wv_detail);
                pbar = (ProgressBar) dialog.findViewById(R.id.progressbar);

                //WebView view = new WebView(this);
                //view.setVerticalScrollBarEnabled(false);

                //webview.addView(view);
                //view.loadData("","text/html; charset=utf-8", "utf-8");
                String url = "http://202.138.233.235:8081/apipaycon/3111/views/detail_product.php?id="+menuid; //getWidget(phoneNumber); //"https://"+kdhost+"/wv_bpjs/";

                webview.loadUrl(url);
                */
                /*
                if(Build.VERSION.SDK_INT >= 11){
                    webview.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                //WebSettings webSettings = webview.getSettings();
                //webSettings.setJavaScriptEnabled(true);
                //webview.setWebViewClient(new WebViewClient());
                webview.setWebViewClient(new MyWebViewClient());

                webview.clearCache(true);
                webview.clearHistory();
                webview.getSettings().setJavaScriptEnabled(true);
                webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                //String phoneNumber = login.userDb;
                String url = "http://202.138.233.235:8081/apipaycon/3111/views/detail_product.php?id="+menuid; //getWidget(phoneNumber); //"https://"+kdhost+"/wv_bpjs/";

                webview.loadUrl(url);
                */

                Button dialogButton = (Button) dialog.findViewById(R.id.bt_ok);
                /**
                 * Jika tombol diklik, tutup dialog
                 */
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
            /*
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext(), AlertDialog.THEME_HOLO_LIGHT);
                builder
                        .setTitle("Detail Produk")
                        .setMessage(menudesc)
                        //.setMessage("Yakin TOP UP "+nominal+" Akun "+phoneNumber+" ?")
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setNegativeButton("Close", null)                        //Do nothing on no
                        .show();
            }*/
        });

        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String x = holder.qty.getText().toString();
                int xqty = 0;
                Double xbuy = 0.00;
                if(Integer.parseInt(x)>=1) {
                    xqty = Integer.parseInt(holder.qty.getText().toString()) - 1;
                    xbuy = Double.parseDouble(holder.price.getText().toString().replace(",","")) * xqty;
                }
                if(xqty >= 1) {
                    arrTempQty[holder.ref] = String.valueOf(xqty);
                    holder.qty.setText(arrTempQty[position]);

                    //arrTempBuy[holder.ref] = nfout.format(xbuy);
                    arrTempBuy[holder.ref] = currencyformat(xbuy);
                    holder.buy.setText(arrTempBuy[position]);
                    if(!holder.buy.getText().toString().equals("")) {
                        holder.buycurrency.setText("Rp " + holder.buy.getText().toString());
                    }else{
                        holder.buycurrency.setText("");
                    }
                }else{
                    arrTempQty[holder.ref] = "0";
                    holder.qty.setText(arrTempQty[position]);

                    arrTempBuy[holder.ref] = "";
                    holder.buy.setText(arrTempBuy[position]);
                    if(!holder.buy.getText().toString().equals("")) {
                        holder.buycurrency.setText("Rp " + holder.buy.getText().toString());
                    }else{
                        holder.buycurrency.setText("");
                    }
                }

                if(xqty > 0) {
                    insertTemp.InsertTempOrder(activity, restoid, groupid, menuid, menu, String.valueOf(xqty), dprice, String.valueOf(xbuy));
                }else {
                    GetTmpOrder deleteItem = new GetTmpOrder();
                    deleteItem.DeleteTempItem(activity, menuid);
                }
                FoodRestoMenu sumTotal = new FoodRestoMenu();
                sumTotal.TempOrder(activity);

                FoodRestoDetail sumTotal2 = new FoodRestoDetail();
                sumTotal2.TempOrder(activity);

                FoodRestoDetail refresh = new FoodRestoDetail();
                refresh.onRefesh(activity);

                FoodRestoList refresh2 = new FoodRestoList();
                refresh2.onRefesh(activity);

                if(param==1){
                    FoodOrder onReAmmount = new FoodOrder();
                    onReAmmount.onRefreshAmmount(activity);
                    try {
                        FoodRestoMenu onRefesh3 = new FoodRestoMenu();
                        onRefesh3.onRefesh(activity);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String x = holder.qty.getText().toString();
                int xqty = 0;
                Double xbuy = 0.00;
                if(x.equals("")) {
                    xqty = 1;
                    xbuy = Double.parseDouble(holder.price.getText().toString().replace(",","")) * xqty;
                }else{
                    xqty = Integer.parseInt(holder.qty.getText().toString()) + 1;
                    xbuy = Double.parseDouble(holder.price.getText().toString().replace(",","")) * xqty;
                }

                if(xqty >= 1) {
                    arrTempQty[holder.ref] = String.valueOf(xqty);
                    holder.qty.setText(arrTempQty[position]);

                    //arrTempBuy[holder.ref] = nfout.format(xbuy);
                    arrTempBuy[holder.ref] = currencyformat(xbuy);
                    holder.buy.setText(arrTempBuy[position]);
                    if(!holder.buy.getText().toString().equals("")) {
                        holder.buycurrency.setText("Rp " + holder.buy.getText().toString());
                    }else{
                        holder.buycurrency.setText("");
                    }
                }else{
                    arrTempQty[holder.ref] = "0";
                    holder.qty.setText(arrTempQty[position]);

                    arrTempBuy[holder.ref] = "";
                    holder.buy.setText(arrTempBuy[position]);
                    if(!holder.buy.getText().toString().equals("")) {
                        holder.buycurrency.setText("Rp " + holder.buy.getText().toString());
                    }else{
                        holder.buycurrency.setText("");
                    }
                }

                if(Integer.parseInt(countadd) > 0){
                    ShowAdditionalMenu(holder.qty, xqty, holder.buy, xbuy, holder.buycurrency, restoid, groupid, menuid, menu, dprice);
                    //PopupFoodRestoMenuAdditional addon = new PopupFoodRestoMenuAdditional(activity);
                    //addon.AdditionalMenu();
                }else{

                    if(xqty > 0) {
                        insertTemp.InsertTempOrder(activity, restoid, groupid, menuid, menu, String.valueOf(xqty), dprice, String.valueOf(xbuy));
                    }else {
                        GetTmpOrder deleteItem = new GetTmpOrder();
                        deleteItem.DeleteTempItem(activity, menuid);
                    }
                    FoodRestoMenu sumTotal = new FoodRestoMenu();
                    sumTotal.TempOrder(activity);

                    FoodRestoDetail sumTotal2 = new FoodRestoDetail();
                    sumTotal2.TempOrder(activity);

                    FoodRestoDetail refresh = new FoodRestoDetail();
                    refresh.onRefesh(activity);

                    FoodRestoList refresh2 = new FoodRestoList();
                    refresh2.onRefesh(activity);

                    /*
                    holder.qty.setText(arrTempQty[position]);
                    holder.buy.setText(arrTempBuy[position]);
                    if(!holder.buy.getText().toString().equals("")) {
                        holder.buycurrency.setText("Rp " + holder.buy.getText().toString());
                    }else{
                        holder.buycurrency.setText("");
                    }

                    insertTemp.InsertTempOrder(activity, restoid, groupid, menuid, menu, String.valueOf(xqty), dprice, String.valueOf(xbuy));
                    */

                    /*
                    FoodRestoMenu sumTotal = new FoodRestoMenu();
                    sumTotal.TempOrder(activity);

                    FoodRestoDetail sumTotal2 = new FoodRestoDetail();
                    sumTotal2.TempOrder(activity);

                    FoodRestoDetail refresh = new FoodRestoDetail();
                    refresh.onRefesh(activity);

                    FoodRestoList refresh2 = new FoodRestoList();
                    refresh2.onRefesh(activity);
                    */

                    if(param==1){
                        FoodOrder onReAmmount = new FoodOrder();
                        onReAmmount.onRefreshAmmount(activity);
                        try {
                            FoodRestoMenu onRefesh3 = new FoodRestoMenu();
                            onRefesh3.onRefesh(activity);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    arrTempQty[holder.ref] = String.valueOf(xqty);
                    //arrTempBuy[holder.ref] = nfout.format(xbuy);
                    arrTempBuy[holder.ref] = currencyformat(xbuy);
                }
            }
        });

        GetTmpOrder selectNote = new GetTmpOrder();
        selectNote.SelectTempNote(activity, menuid);
        if(selectNote.setNotes()!=null){
            arrTempNote[position] = selectNote.setNotes();
        }

        //Log.e("EDIT POSISI",position+" - "+menu);

        holder.tnote.setId(position);
        //if(position==cursor){
        //    holder.tnote.requestFocus();
        //}else{
            holder.tnote.clearFocus();
        //}

        holder.tnote.setHint("Add notes..");

        if(holder.tnote.isFocused()) {
            arrTempNoteHint[position] = "";
            holder.tnote.setHint(arrTempNoteHint[position]);
        }else{
            arrTempNoteHint[position] = "Add notes..";
            holder.tnote.setHint(arrTempNoteHint[position]);
        }

        holder.tnote.setText(arrTempNote[position]);
        holder.tnote.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
                if((arg0.toString().length() > 0) && holder.tnote.getId()==position) {
                    arrTempNote[holder.ref] = arg0.toString();
                    insertTemp.InsertTempOrderNotes(activity, menuid, arg0.toString());
                }
            }
        });

        holder.tnote.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //holder.tnote.setHint("");
                    arrTempNoteHint[holder.ref]="";
                }else{
                    //holder.tnote.setHint("Add notes..");
                    arrTempNoteHint[holder.ref]="Add notes..";
                }
                holder.tnote.setHint(arrTempNoteHint[holder.ref]);
                holder.tnote.setId(holder.ref);

                //cursor = holder.tnote.getId();
                //Log.e("Cursor",cursor+"");
                //Log.e("Edit ID",holder.tnote.getId()+"");
            }
        });

        holder.btnClearNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.tnote.setText("");
                insertTemp.InsertTempOrderNotes(activity, menuid, null);
            }
        });

        return convertView;
    }

    private boolean ReturnQty(){
        return true;
    }

    private String currencyformat(double value){
        DecimalFormat df = new DecimalFormat("#,###,##0.00");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(otherSymbols);

        return String.valueOf(df.format(value));
    }

    public void ShowAdditionalMenu(final TextView tqty, final int qty, final TextView tbuy, final Double buy, final TextView tbuycurrency,
                                   final String restoid, final String groupid, final String menuid, final String menu, final String dprice) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_foodrestomenu_additional);
        dialog.setCancelable(true);

        TextView ttitle = (TextView) dialog.findViewById(R.id.ttitle);
        //txtshipping = (TextView) dialog.findViewById(R.id.txtshipping);
        //tshipping = (TextView) dialog.findViewById(R.id.tshipping);
        //ttotal = (TextView) dialog.findViewById(R.id.ttotal);
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
                tqty.setText(String.valueOf(qty));
                tbuy.setText(String.valueOf(buy));
                if(!tqty.getText().toString().equals("")) {
                    tbuycurrency.setText("Rp " + tbuy.getText().toString());
                }else{
                    tbuycurrency.setText("");
                }

                GetTmpOrder insertTemp = new GetTmpOrder();
                insertTemp.InsertTempOrder(activity, restoid, groupid, menuid, menu, String.valueOf(qty), dprice, String.valueOf(buy));

                FoodRestoMenu sumTotal = new FoodRestoMenu();
                sumTotal.TempOrder(activity);

                FoodRestoDetail sumTotal2 = new FoodRestoDetail();
                sumTotal2.TempOrder(activity);

                FoodRestoDetail refresh = new FoodRestoDetail();
                refresh.onRefesh(activity);

                FoodRestoList refresh2 = new FoodRestoList();
                refresh2.onRefesh(activity);

                if(param==1){
                    FoodOrder onReAmmount = new FoodOrder();
                    onReAmmount.onRefreshAmmount(activity);
                    try {
                        FoodRestoMenu onRefesh3 = new FoodRestoMenu();
                        onRefesh3.onRefesh(activity);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }

                dialog.dismiss();
            }
        });

        /*btn2.setOnClickListener(new View.OnClickListener() {
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

    /*
    private class MyWebViewClient extends WebViewClient {
        public MyWebViewClient() {
            pbar.setVisibility(View.VISIBLE);
            webview.setVisibility(View.GONE);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String url) {
            Log.e("Error load URL "," Error occured while loading the web page at Url"+ url+"." +description);
            view.loadUrl("about:blank");
            //Toast.makeText(this, "Error occured, please check network connectivity", Toast.LENGTH_SHORT).show();
            super.onReceivedError(view, errorCode, description, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            super.onPageFinished(view, url);
            pbar.setVisibility(View.GONE);
            webview.setVisibility(View.VISIBLE);
        }
    }*/
}
