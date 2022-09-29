package com.arindo.nura;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by bmaxard on 20/02/2017.
 */

public class LazyAdapterRestoMenu extends RecyclerView.Adapter<LazyAdapterRestoMenu.ViewHolder>{

    private List<LazyAdapterRestoMenuModel> islandList;
    private Context context;

    TextView islandNumPositive;
    TextView islandNumNegative;
    String result;
    String formattedDate;
    int id;
    private View.OnClickListener onAddNum;
    private View.OnClickListener onSubNum;


    public LazyAdapterRestoMenu(List<LazyAdapterRestoMenuModel> islandList, Context context){
        this.islandList = islandList;
        this.context = context;

    }

    public void setOnAddNum(View.OnClickListener onAddNum){
        this.onAddNum = onAddNum;
    }
    public void setOnSubNum(View.OnClickListener onSubNum){
        this.onSubNum = onSubNum;
    }

    @Override
    public LazyAdapterRestoMenu.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_foodrestomenu, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final LazyAdapterRestoMenu.ViewHolder holder, final int position) {
        final LazyAdapterRestoMenuModel model = islandList.get(position);
        holder.islandId.setText(model.getId());
        holder.islandName.setText(model.getName());
        holder.islandPrice.setText(model.getPrice());

        // set tag to know which button you clicked'

        holder.islandNumPositive.setTag(position);
        holder.islandNumPositive.setFocusable(true);
        holder.islandNumPositive.setClickable(true);

        //  set callback interface  ，

        holder.islandNumPositive.setOnClickListener(onAddNum);
        holder.islandNumNegative.setTag(position);
        holder.islandNumNegative.setOnClickListener(onSubNum);

    }

    @Override
    public int getItemCount() {
        return (null != islandList ? islandList.size() : 0);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        private CardView islands;
        private TextView islandId;
        private TextView islandName;
        private TextView islandPrice;
        TextView islandNumPositive;
        TextView islandNumNegative;
        TextView islandNumCount;

        public ViewHolder(View view) {
            super(view);
            islands = (CardView) view.findViewById(R.id.islands);
            islandId = (TextView) view.findViewById(R.id.tmenuid);
            islandName = (TextView) view.findViewById(R.id.tmenu);
            islandPrice = (TextView) view.findViewById(R.id.tprice);
            islandNumPositive = (TextView) view.findViewById(R.id.tnumpickerpositive);
            islandNumNegative = (TextView) view.findViewById(R.id.tnumpickernegative);
            islandNumCount = (TextView) view.findViewById(R.id.tnumpickercount);
        }
    }
}
