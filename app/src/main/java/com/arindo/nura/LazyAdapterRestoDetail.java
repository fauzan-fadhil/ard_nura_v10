package com.arindo.nura;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by bmaxard on 20/02/2017.
 */

public class LazyAdapterRestoDetail extends RecyclerView.Adapter<LazyAdapterRestoDetail.ViewHolder>{

    private List<LazyAdapterRestoDetailModel> groupmenuList;
    private Context context;

    public LazyAdapterRestoDetail(List<LazyAdapterRestoDetailModel> islandList, Context context){
        this.groupmenuList = islandList;
        this.context = context;

    }

    @Override
    public LazyAdapterRestoDetail.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_foodgroupmenu, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LazyAdapterRestoDetail.ViewHolder holder, final int position) {
        holder.groupid.setText(groupmenuList.get(position).getId());
        holder.groupname.setText(groupmenuList.get(position).getName());

        GetTmpOrder sumQty = new GetTmpOrder();
        sumQty.SelectTempTotal(context,groupmenuList.get(position).getId(),2);
        holder.groupqty.setText(String.valueOf(sumQty.setTotalQty()));
        if(sumQty.setTotalQty() <=0 ){
            holder.groupqty.setVisibility(View.GONE);
            holder.groupicon.setVisibility(View.GONE);
        }

        holder.groupmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, islandList.get(position).getId(), Toast.LENGTH_SHORT).show();
                String groupid = groupmenuList.get(position).getId();
                String groupname = groupmenuList.get(position).getName();
                Intent i = null;
                i = new Intent(context, FoodRestoMenu.class);
                i.putExtra("restoid",FoodRestoDetail.TAG_RESTOID.toString());
                i.putExtra("groupid",groupid);
                i.putExtra("groupname",groupname);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != groupmenuList ? groupmenuList.size() : 0);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        private TextView groupname;
        private TextView groupid;
        private CardView groupmenu;
        private com.arindo.nura.BadgeView groupqty;
        private ImageView groupicon;

        public ViewHolder(View view) {
            super(view);
            groupmenu = (CardView) view.findViewById(R.id.tgroupmenu);
            groupname = (TextView) view.findViewById(R.id.tgroupname);
            groupid = (TextView) view.findViewById(R.id.tgroupid);
            groupqty = (com.arindo.nura.BadgeView) view.findViewById(R.id.tgroupqty);
            groupicon = (ImageView) view.findViewById(R.id.tgroupicon);
        }
    }
}
