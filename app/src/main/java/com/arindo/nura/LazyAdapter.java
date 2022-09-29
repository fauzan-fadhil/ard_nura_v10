package com.arindo.nura;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.security.NetworkSecurityPolicy;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by bmaxard on 27/01/2017.
 */

public class LazyAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater = null;
    //  public ImageLoader imageLoader;
    public LazyAdapter(Activity a, ArrayList<HashMap<String, String>> d)
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
            vi = inflater.inflate(R.layout.row_sendoption, null);
        TextView pid = (TextView) vi.findViewById(R.id.tpid);
        TextView title = (TextView) vi.findViewById(R.id.ttitle);
        TextView desc = (TextView) vi.findViewById(R.id.tdes);
        ImageView thumb_image = (ImageView) vi.findViewById(R.id.imgproduct);

        HashMap<String, String> listsendoption = new HashMap<String, String>();
        listsendoption = data.get(position);

        pid.setText(listsendoption.get(SendOption.TAG_PID));
        title.setText(listsendoption.get(SendOption.TAG_TITLE));
        desc.setText(listsendoption.get(SendOption.TAG_DESC));

        /*int a=0; int b=0;
        if(listsendoption.get(SendOption.TAG_PID).equals("1")){a=30; b=20;}
        if(listsendoption.get(SendOption.TAG_PID).equals("2") || listsendoption.get(SendOption.TAG_PID).equals("3")){a=40; b=30;}
        if(listsendoption.get(SendOption.TAG_PID).equals("4") || listsendoption.get(SendOption.TAG_PID).equals("5")){a=50; b=40;}*/

//        imageLoader.DisplayImage(daftar_berita.get(BeritaUtama.TAG_GAMBAR),thumb_image);
        Picasso.with(activity.getApplicationContext())
                .load(listsendoption.get(SendOption.TAG_IMAGE))
                .error(R.color.grey_500)
                //.resize(a,b)
                .into(thumb_image);


        return vi;
    }
}
