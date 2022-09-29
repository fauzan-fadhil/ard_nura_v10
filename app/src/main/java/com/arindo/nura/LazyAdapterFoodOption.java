package com.arindo.nura;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bmaxard on 20/02/2017.
 */

public class LazyAdapterFoodOption extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    //  public ImageLoader imageLoader;
    public LazyAdapterFoodOption(Activity a, ArrayList<HashMap<String, String>> d)
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
            vi = inflater.inflate(R.layout.row_foodoption, null);
        TextView pid = (TextView) vi.findViewById(R.id.toptionid);
        TextView title = (TextView) vi.findViewById(R.id.toptionname);

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        pid.setText(listsendoption.get(FoodOption.TAG_ID));
        title.setText(listsendoption.get(FoodOption.TAG_TITLE));

        return vi;
    }
}
