package com.example.qr_codescan.module.check;

import android.content.Context;
import android.support.v4.util.CircularArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cleverdou.qrscan.zxing.ToastUtil;
import com.example.qr_codescan.R;
import com.example.qr_codescan.adapter.BaseLoadingAdapter;
import com.example.qr_codescan.bean.CheckAndTitleBean;

import java.util.ArrayList;

/**
 * Created by sunwei on 16/1/8.
 * Email: lx_sunwei@163.com.
 * Description:
 */
public class CheckFragmentListAdapter extends BaseLoadingAdapter<CheckAndTitleBean> {

    private CircularArray<CheckAndTitleBean> mRepositories;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public CheckFragmentListAdapter(RecyclerView recyclerView, CircularArray<CheckAndTitleBean> repositories) {
        super(recyclerView, repositories);
        mRepositories = repositories;
    }

    private static class CheckViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout llyCheck;
        public TextView tvAmount;
        public TextView tvPayType;
        public TextView tvCheckTime;

        public CheckViewHolder(View v) {
            super(v);

            llyCheck = (LinearLayout) v.findViewById(R.id.lly_check);
            tvPayType = (TextView) v.findViewById(R.id.tv_payType);
            tvAmount = (TextView) v.findViewById(R.id.tv_pay_amount);
            tvCheckTime = (TextView) v.findViewById(R.id.tv_time_check);

        }
    }

    private static class CheckTitleViewHolder extends RecyclerView.ViewHolder {

        public TextView tvTitleTime;
        public TextView tvTitleAmount;

        public CheckTitleViewHolder(View v) {
            super(v);

            tvTitleTime = (TextView) v.findViewById(R.id.tv_title_date);
            tvTitleAmount = (TextView) v.findViewById(R.id.tv_title_amount);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent, int position) {
        View view = null;
        ToastUtil.showToast(mContext,position + "");
        mContext = parent.getContext();
        if (mRepositories.get(position) != null && mRepositories.get(position).isTitle) {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_fragment_check_title, parent, false);
            return new CheckTitleViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.list_item_fragment_check, parent, false);
            return new CheckViewHolder(view);
        }
    }

    @Override
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (mRepositories.get(position).isTitle) {
            loadTitleData((CheckTitleViewHolder) holder, position);
        } else {
            loadCheckData((CheckViewHolder) holder, position);
        }
    }

    private void loadCheckData(CheckViewHolder viewHolder, int position) {
        viewHolder.tvPayType.setText(mRepositories.get(position).payType);
        viewHolder.tvAmount.setText("¥ " + mRepositories.get(position).payAmount);
        viewHolder.tvCheckTime.setText(mRepositories.get(position).payDate);
    }

    private void loadTitleData(CheckTitleViewHolder viewHolder, int position) {
        viewHolder.tvTitleTime.setText(mRepositories.get(position).titleDate);
        viewHolder.tvTitleAmount.setText("合计: " + mRepositories.get(position).titleAmount + " 元");
    }

    public interface OnItemClickListener {
        void onItemClick(int position, ArrayList<String> urls);
    }

}
