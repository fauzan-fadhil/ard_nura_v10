package com.arindo.nura;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by bmaxard on 27/01/2017.
 */

public class LazyAdapterRute extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    //  public ImageLoader imageLoader;
    public LazyAdapterRute(Activity a, ArrayList<HashMap<String, String>> d)
    {
        activity = a;

        data = d;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup
            parent) {
        View vi = convertView;
        if (convertView == null)
            vi = inflater.inflate(R.layout.row_rutelist, null);
        TextView ruteid = (TextView) vi.findViewById(R.id.truteid);
        TextView rute = (TextView) vi.findViewById(R.id.trute);
        TextView ruteaddress = (TextView) vi.findViewById(R.id.truteaddress);
        TextView rutelat = (TextView) vi.findViewById(R.id.trutelat);
        TextView rutelng = (TextView) vi.findViewById(R.id.trutelng);

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        ruteid.setText(listsendoption.get(RequestActivity2.TAG_RUTEID));
        rute.setText(listsendoption.get(RequestActivity2.TAG_RUTE));
        ruteaddress.setText(listsendoption.get(RequestActivity2.TAG_RUTEADDR));
        rutelat.setText(listsendoption.get(RequestActivity2.TAG_RUTELAT));
        rutelng.setText(listsendoption.get(RequestActivity2.TAG_RUTELNG));

        return vi;
    }
}
