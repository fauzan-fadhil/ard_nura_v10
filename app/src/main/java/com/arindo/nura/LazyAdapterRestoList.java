package com.arindo.nura;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bmaxard on 27/01/2017.
 */

public class LazyAdapterRestoList extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    //  public ImageLoader imageLoader;
    public LazyAdapterRestoList(Activity a, ArrayList<HashMap<String, String>> d)
    {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        imageLoader = new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }
    public Object getItem(int position) {
        return position;
    }
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.row_foodrestolist, null);
        TextView pid = (TextView) vi.findViewById(R.id.tpid);
        TextView title = (TextView) vi.findViewById(R.id.ttitle);
        TextView desc = (TextView) vi.findViewById(R.id.tdes);
        TextView tqty = (TextView) vi.findViewById(R.id.tqty);
        LinearLayout layoutorder = (LinearLayout) vi.findViewById(R.id.layoutorder);
        //ImageView thumb_image = (ImageView) vi.findViewById(R.id.imgproduct);
        final RelativeLayout thumb_image = (RelativeLayout) vi.findViewById(R.id.imgproductresto);

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        pid.setText(listsendoption.get(FoodRestoList.TAG_PID));
        title.setText(listsendoption.get(FoodRestoList.TAG_TITLE));
        desc.setText(listsendoption.get(FoodRestoList.TAG_DESC));

        GetTmpOrder sumQty = new GetTmpOrder();
        sumQty.SelectTempTotal(activity,listsendoption.get(FoodRestoList.TAG_PID),1);
        if(sumQty.setTotalQty() > 0 ){
            tqty.setText(String.valueOf(sumQty.setTotalQty()));
            layoutorder.setVisibility(View.VISIBLE);
        }

        //imageLoader.DisplayImage(daftar_berita.get(BeritaUtama.TAG_GAMBAR),thumb_image);
        /*Picasso.with(activity.getApplicationContext())
                .load(listsendoption.get(SendOption.TAG_IMAGE))
                .error(R.color.grey_500)
                .resize(500,350)
                .into((Target));*/

        Picasso.with(activity)
                .load(listsendoption.get(SendOption.TAG_IMAGE))
                .resize(200,350)
                .into(new Target(){
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            thumb_image.setBackground(new BitmapDrawable(activity.getResources(), bitmap));
                        }
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        Log.d("TAG", "FAILED");
                    }

                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        Log.d("TAG", "Prepare Load");
                    }
                });


        return vi;
    }
}
