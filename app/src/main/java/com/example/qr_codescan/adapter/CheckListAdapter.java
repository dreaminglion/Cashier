package com.example.qr_codescan.adapter;

import android.content.Context;
import android.support.v4.util.CircularArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.qr_codescan.R;
import com.example.qr_codescan.bean.CheckBean;
import com.example.qr_codescan.utils.DensityUtils;
import com.example.qr_codescan.utils.TimeUtils;


import java.util.ArrayList;

/**
 * Created by sunwei on 16/1/8.
 * Email: lx_sunwei@163.com.
 * Description:
 */
public class CheckListAdapter extends BaseLoadingAdapter<CheckBean>{

    private CircularArray<CheckBean> mRepositories;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public CheckListAdapter(RecyclerView recyclerView, CircularArray<CheckBean> repositories) {
        super(recyclerView, repositories);
        mRepositories = repositories;
    }

    private static class RepositoryViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout llyCheck;
        public TextView tvAmount;
        public TextView tvTime;
        public TextView tvOrderNo;

        public RepositoryViewHolder(View v) {
            super(v);

            llyCheck = (LinearLayout) v.findViewById(R.id.lly_check);
            tvAmount = (TextView) v.findViewById(R.id.tv_amount);
            tvTime = (TextView) v.findViewById(R.id.tv_time);
            tvOrderNo = (TextView) v.findViewById(R.id.tv_check_no);

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent,int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_check, parent, false);
        mContext = parent.getContext();
        return new RepositoryViewHolder(view);
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, final int position) {

        RepositoryViewHolder viewHolder = (RepositoryViewHolder)holder;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            layoutParams.setMargins(0, DensityUtils.dp2px(mContext, 4),
                    0, DensityUtils.dp2px(mContext, 4));

            viewHolder.llyCheck.setLayoutParams(layoutParams);

        loadData(viewHolder, position);

    }

    private void loadData(RepositoryViewHolder viewHolder,int position){
        viewHolder.tvAmount.setText("金额：" + (mRepositories.get(position).amount / 100) + "."
                + (mRepositories.get(position).amount / 10)
                + (mRepositories.get(position).amount % 10) + " 元" );
        viewHolder.tvTime.setText("时间：" + TimeUtils.formatData("yyyy-MM-dd HH:mm:ss",mRepositories.get(position).updateTime));
        viewHolder.tvOrderNo.setText("订单号：" + mRepositories.get(position).tradeCode);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, ArrayList<String> urls);
    }

}
