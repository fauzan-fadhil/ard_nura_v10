package com.arindo.nura;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static android.graphics.drawable.Drawable.*;


/**
 * Created by bmaxard on 15/10/2016.
 */

public class GridviewFoodMenuAdapter extends ArrayAdapter<GridFoodItem> {

    //private final ColorMatrixColorFilter grayscaleFilter;
    private Context mContext;
    private int layoutResourceId;
    private ArrayList<GridFoodItem> mGridData = new ArrayList<GridFoodItem>();

    public GridviewFoodMenuAdapter(Context mContext, int layoutResourceId, ArrayList<GridFoodItem> mGridData) {
        super(mContext, layoutResourceId, mGridData);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.mGridData = mGridData;
    }


    /**
     * Updates grid data and refresh grid items.
     *
     * @param mGridData
     */
    public void setGridData(ArrayList<GridFoodItem> mGridData) {
        this.mGridData = mGridData;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.idTextView = (TextView) row.findViewById(R.id.textId);
            holder.titleTextView = (TextView) row.findViewById(R.id.textView1);
            holder.imageView = (ImageView) row.findViewById(R.id.imageView1);
            //holder.layoutimage = (LinearLayout) row.findViewById(R.id.layoutimage);
            holder.pbarpicasso = (ProgressBar) row.findViewById(R.id.pbarpicasso);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        GridFoodItem item = mGridData.get(position);
        holder.idTextView.setText(Html.fromHtml(item.getId()));
        holder.titleTextView.setText(Html.fromHtml(item.getTitle()));
        Picasso.with(mContext).load(item.getImage()).resize(200, 200).centerCrop()
                .into(holder.imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        holder.pbarpicasso.setVisibility(View.GONE);
                        holder.titleTextView.setBackgroundResource(R.drawable.gradient);
                    }

                    @Override
                    public void onError() {
                        //error
                    }
                });
        /*Picasso.with(mContext)
                .load(item.getImage())
                .placeholder(R.drawable.custom_progress_background)
                .error(R.color.grey_500)
                .resize(200,200)
                .into(holder.imageView);
        /*Picasso.with(mContext)
                .load(item.getImage())
                .resize(200,200)
                .into(new Target(){
                    @Override
                    public void onPrepareLoad(final Drawable placeHolderDrawable) {
                        Log.d("TAG", "Prepare Load");
                        holder.pbarpicasso.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            holder.layoutimage.setBackground(new BitmapDrawable(mContext.getResources(), bitmap));
                        }
                        holder.pbarpicasso.setVisibility(View.GONE);
                    }

                    @Override
                    public void onBitmapFailed(final Drawable errorDrawable) {
                        Log.d("TAG", "FAILED");
                    }
                });*/

        return row;
    }

    static class ViewHolder {
        TextView idTextView;
        TextView titleTextView;
        ImageView imageView;
        LinearLayout layoutimage;
        ProgressBar pbarpicasso;
    }
}
