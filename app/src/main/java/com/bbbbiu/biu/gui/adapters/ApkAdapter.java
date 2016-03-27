package com.bbbbiu.biu.gui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bbbbiu.biu.R;
import com.bbbbiu.biu.util.Apk;

import java.util.ArrayList;

/**
 * Created by fangdongliang on 16/3/24.
 */
public class ApkAdapter extends RecyclerView.Adapter<ApkAdapter.MyViewHolder> {
    Context myContext;
    ArrayList<Apk> apkArrayList;
    Apk apk;
    public ApkAdapter(Context context,ArrayList<Apk> apklist)
    {
        myContext = context;
        apkArrayList = apklist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder =
                new MyViewHolder(
                        LayoutInflater.from(myContext).inflate(R.layout.apk_item,null));

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        apk = apkArrayList.get(position);
        holder.tv.setText(apk.getName());
        holder.iv.setImageBitmap(apk.getBitmap());

    }

    @Override
    public int getItemCount() {
        return apkArrayList.size();
    }

     class MyViewHolder extends RecyclerView.ViewHolder{
         ImageView iv;

         TextView tv;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv = (TextView) itemView.findViewById(R.id.apk_name);
            iv = (ImageView) itemView.findViewById(R.id.apk_image);
        }
    }
}

