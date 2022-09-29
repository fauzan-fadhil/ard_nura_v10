package com.arindo.nura;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by bmaxard on 27/01/2017.
 */

public class LazyAdapterDetailHistoriFood extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;

    public LazyAdapterDetailHistoriFood(Activity a, ArrayList<HashMap<String, String>> d){
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
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
        TextView menu;
        TextView price;
        TextView qty;
        TextView total;
        TextView notes;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {

            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.row_foodhistoridetail, null);
            holder.menu = (TextView) convertView.findViewById(R.id.tmenu);
            holder.notes = (TextView) convertView.findViewById(R.id.tnotes);
            holder.total = (TextView) convertView.findViewById(R.id.ttotal);

            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        String menu = listsendoption.get(HistoryDetailFood.TAG_MENU);
        String qty = listsendoption.get(HistoryDetailFood.TAG_QTY);
        String unit = listsendoption.get(HistoryDetailFood.TAG_UNIT);
        String total = listsendoption.get(HistoryDetailFood.TAG_TOTAL);
        String notes = listsendoption.get(HistoryDetailFood.TAG_NOTES);

        if(!notes.equals("")){holder.notes.setVisibility(View.VISIBLE);}
        else{holder.notes.setVisibility(View.GONE);}

        holder.notes.setText(notes);
        holder.menu.setText(qty+" "+unit+" "+menu);
        holder.total.setText(total);

        return convertView;
    }
}
