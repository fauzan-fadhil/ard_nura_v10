package com.arindo.nura;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bmaxard on 20/02/2017.
 */

public class LazyAdapterFoodSearch extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    private int param;
    //  public ImageLoader imageLoader;
    public LazyAdapterFoodSearch(Activity a, ArrayList<HashMap<String, String>> d, int p)
    {
        activity = a;
        data = d;
        param = p;
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
            vi = inflater.inflate(R.layout.row_foodsearch, null);
        TextView pid = (TextView) vi.findViewById(R.id.tid);
        TextView title = (TextView) vi.findViewById(R.id.tname);
        TextView desc = (TextView) vi.findViewById(R.id.tdesc);

        HashMap<String, String> listsearch = new HashMap<String, String>();
        listsearch = data.get(position);

        if(param==2){desc.setVisibility(View.VISIBLE);}

        pid.setText(listsearch.get(FoodSearch.TAG_ID));
        title.setText(listsearch.get(FoodSearch.TAG_TITLE));
        desc.setText(listsearch.get(FoodSearch.TAG_DESC));

        return vi;
    }
}
