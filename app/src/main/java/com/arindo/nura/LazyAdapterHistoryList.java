package com.arindo.nura;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
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
 * Created by bmaxard on 20/02/2017.
 */

public class LazyAdapterHistoryList extends BaseAdapter {
    private Context context;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    //  public ImageLoader imageLoader;
    public LazyAdapterHistoryList(Context a, ArrayList<HashMap<String, String>> d)
    {
        context = a;
        data = d;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public class ViewHolder{
        TextView pid;
        TextView tiket;
        TextView orderdate;
        TextView desc;
        TextView status;
        ImageView img;
        int ref;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_history, null);

            holder.pid = (TextView) convertView.findViewById(R.id.tpid);
            holder.tiket = (TextView) convertView.findViewById(R.id.ttiket);
            holder.orderdate = (TextView) convertView.findViewById(R.id.tdate);
            holder.desc = (TextView) convertView.findViewById(R.id.tdes);
            holder.img = (ImageView) convertView.findViewById(R.id.imgproduct);
            holder.status = (TextView) convertView.findViewById(R.id.tstatus);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        holder.pid.setText(listsendoption.get(HistoryBooking.TAG_PID));
        holder.tiket.setText(listsendoption.get(HistoryBooking.TAG_TIKET));
        holder.orderdate.setText(listsendoption.get(HistoryBooking.TAG_DATE));
        holder.desc.setText(listsendoption.get(HistoryBooking.TAG_DESC));

        if(listsendoption.get(HistoryBooking.TAG_STATUS).equals("9")){ // status code cancel
            holder.desc.setPaintFlags(holder.desc.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.status.setVisibility(View.VISIBLE);
        }else{
            holder.desc.setPaintFlags(0);
            holder.status.setVisibility(View.GONE);
        }

        if(listsendoption.get(HistoryBooking.TAG_PID).equals("0")){holder.img.setImageResource(R.drawable.ic_kurier);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("1")){holder.img.setImageResource(R.drawable.ic_grey_car);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("2")){holder.img.setImageResource(R.drawable.ic_grey_boat);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("3")){holder.img.setImageResource(R.drawable.ic_grey_send);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("4")){holder.img.setImageResource(R.drawable.ic_grey_clean);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("5")){holder.img.setImageResource(R.drawable.ic_sembako);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("6")){holder.img.setImageResource(R.drawable.ic_grey_tick);}
        if(listsendoption.get(HistoryBooking.TAG_PID).equals("7")){holder.img.setImageResource(R.drawable.ic_grey_towing);}

        return convertView;
    }
}
