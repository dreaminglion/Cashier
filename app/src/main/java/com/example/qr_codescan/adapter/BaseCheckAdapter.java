package com.example.qr_codescan.adapter;

import android.content.Context;
import android.support.v4.util.CircularArray;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.qr_codescan.R;
import com.example.qr_codescan.bean.CheckAndTitleBean;


/**
 * Created by sunwei on 2015/12/4.
 * Email: lx_sunwei@163.com.
 * Description: recycleView 滑动到底部加载更多
 */
public class BaseCheckAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BaseLoadingAdapter";

    //是否正在加载
    public boolean mIsLoading = false;
    //正常条目
    private static final int TYPE_NORMAL_ITEM = 0;
    //加载条目
    private static final int TYPE_LOADING_ITEM = 1;
    //title条目
    private static final int TYPE_TITLE_ITEM = 2;
    //加载viewHolder
    private LoadingViewHolder mLoadingViewHolder;
    //瀑布流
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    //数据集
    private CircularArray<CheckAndTitleBean> mTs;
    //首次进入
    private boolean mFirstEnter = true;
    private RecyclerView mRecyclerView;
    //view position
    private int mPosition;
    private Context mContext;

    public BaseCheckAdapter(RecyclerView recyclerView, CircularArray<CheckAndTitleBean> ts) {

        mTs = ts;

        mRecyclerView = recyclerView;

        setSpanCount(recyclerView);

        notifyLoading();

    }

    private OnLoadingListener mOnLoadingListener;

    /**
     * 加载更多接口
     */
    public interface OnLoadingListener {
        void loading();
    }

    /**
     * 设置监听接口
     *
     * @param onLoadingListener onLoadingListener
     */
    public void setOnLoadingListener(OnLoadingListener onLoadingListener) {
        mOnLoadingListener = onLoadingListener;
    }

    /**
     * 加载完成
     */
    public void setLoadingComplete() {

        if (mTs.size() > 0 && mTs.getLast() == null) {
            mIsLoading = false;
            mTs.removeFromEnd(1);
            notifyItemRemoved(mTs.size() - 1);
        }
    }

    /**
     * 没有更多数据
     */
    public void setLoadingNoMore() {
        mIsLoading = false;
        mLoadingViewHolder.progressBar.setVisibility(View.GONE);
        mLoadingViewHolder.tvLoading.setText("已加载完！");
    }

    /**
     * 加载失败
     */
    public void setLoadingError() {
        if (mLoadingViewHolder != null) {
            mIsLoading = false;
            mLoadingViewHolder.progressBar.setVisibility(View.GONE);
            mLoadingViewHolder.tvLoading.setText("加载失败，点击重新加载！");

            mLoadingViewHolder.tvLoading.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLoadingListener != null) {
                        mIsLoading = true;
                        mLoadingViewHolder.progressBar.setVisibility(View.VISIBLE);
                        mLoadingViewHolder.tvLoading.setText("正在加载...");

                        mOnLoadingListener.loading();
                    }
                }
            });
        }
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    private boolean canScrollDown(RecyclerView recyclerView) {
        return ViewCompat.canScrollVertically(recyclerView, 1);
    }

    /**
     * 设置每个条目占用的列数
     *
     * @param recyclerView recycleView
     */
    private void setSpanCount(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager == null) {
            Log.e(TAG, "LayoutManager 为空,请先设置 recycleView.setLayoutManager(...)");
        }

        //网格布局
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);//position取到adapter对应的bean为normal item 否则为 load item
                    if (type == TYPE_NORMAL_ITEM) {
                        return 1;
                    } else {
                        return gridLayoutManager.getSpanCount();
                    }
                }
            });
        }

        //瀑布流布局
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            mStaggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
        }
    }

    /**
     * 显示加载
     */
    private void notifyLoading() {
        if (mTs.size() == 0 || mTs.getLast() != null) {
            mTs.addLast(null);
            notifyItemInserted(mTs.size() - 1);
        }
    }

    /**
     * 监听滚动事件
     *
     * @param recyclerView recycleView
     */
    private void setScrollListener(RecyclerView recyclerView) {
        if (recyclerView == null) {
            Log.e(TAG, "recycleView 为空");
            return;
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //recyclerView数据到达底部,不能进行滑动了
                if (!canScrollDown(recyclerView)) {

                    if (!mIsLoading && !mFirstEnter) {

                        notifyLoading();

                        mIsLoading = true;

                        if (mLoadingViewHolder != null) {
                            mLoadingViewHolder.progressBar.setVisibility(View.VISIBLE);
                            mLoadingViewHolder.tvLoading.setText("正在加载...");
                        }

                        if (mOnLoadingListener != null) {
                            mOnLoadingListener.loading();
                        }
                    }
                }

                if (mFirstEnter) {
                    mFirstEnter = false;
                }
            }
        });
    }

    /**
     * 创建viewHolder
     *
     * @param parent viewGroup
     * @return viewHolder
     */
    public RecyclerView.ViewHolder onCreateNormalViewHolder(ViewGroup parent) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_fragment_check, parent, false);
        return new CheckViewHolder(view);
    }

    public RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_fragment_check_title, parent, false);
        return new CheckTitleViewHolder(view);
    }

    /**
     * 绑定viewHolder
     *
     * @param holder   viewHolder
     * @param position position
     */
    public void onBindNormalViewHolder(RecyclerView.ViewHolder holder, final int position) {
        loadCheckData((CheckViewHolder) holder, position);
    }

    public void onBindTitleViewHolder(RecyclerView.ViewHolder holder, final int position) {
        loadTitleData((CheckTitleViewHolder) holder, position);
    }

    private void loadCheckData(CheckViewHolder viewHolder, int position) {
        viewHolder.tvPayType.setText(mTs.get(position).payType);
        viewHolder.tvAmount.setText("¥ " + mTs.get(position).payAmount);
        viewHolder.tvCheckTime.setText(mTs.get(position).payDate);
        viewHolder.tvCashierName.setText(mTs.get(position).cashierName);
        viewHolder.tvOrderId.setText("订单号：" + mTs.get(position).sourceNo);
    }

    private void loadTitleData(CheckTitleViewHolder viewHolder, int position) {
        viewHolder.tvTitleTime.setText(mTs.get(position).titleDate);
        viewHolder.tvTitleAmount.setText("合计: " + mTs.get(position).titleAmount + " 元");
    }

    /**
     * 加载布局
     */
    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;
        public TextView tvLoading;
        public LinearLayout llyLoading;

        public LoadingViewHolder(View view) {
            super(view);

            progressBar = (ProgressBar) view.findViewById(R.id.progress_loading);
            tvLoading = (TextView) view.findViewById(R.id.tv_loading);
            llyLoading = (LinearLayout) view.findViewById(R.id.lly_loading);
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
    public int getItemViewType(int position) {
        CheckAndTitleBean t = mTs.get(position);
        if (t == null) {
            return TYPE_LOADING_ITEM;
        } else {
            if (t.isTitle) {
                return TYPE_TITLE_ITEM;
            } else {
                return TYPE_NORMAL_ITEM;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_NORMAL_ITEM) {
            return onCreateNormalViewHolder(parent);
        } else if (viewType == TYPE_TITLE_ITEM) {
            return onCreateTitleViewHolder(parent);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.loading_layout, parent, false);
            mLoadingViewHolder = new LoadingViewHolder(view);
            return mLoadingViewHolder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == TYPE_NORMAL_ITEM) {
            onBindNormalViewHolder(holder, position);
        } else if (type == TYPE_TITLE_ITEM) {
            onBindTitleViewHolder(holder, position);
        } else {

            //首次进入不加载
            if (mFirstEnter) {
                mLoadingViewHolder.tvLoading.setText("已加载完");
                mLoadingViewHolder.progressBar.setVisibility(View.GONE);

                setScrollListener(mRecyclerView);
            }

            if (mStaggeredGridLayoutManager != null) {
                StaggeredGridLayoutManager.LayoutParams layoutParams =
                        new StaggeredGridLayoutManager.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setFullSpan(true);

                mLoadingViewHolder.llyLoading.setLayoutParams(layoutParams);

            }
        }
    }

    @Override
    public int getItemCount() {
        return mTs.size();
    }

    private static class CheckViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout llyCheck;
        public TextView tvAmount;
        public TextView tvPayType;
        public TextView tvCheckTime;
        public TextView tvCashierName;
        public TextView tvOrderId;

        public CheckViewHolder(View v) {
            super(v);

            llyCheck = (LinearLayout) v.findViewById(R.id.lly_check);
            tvPayType = (TextView) v.findViewById(R.id.tv_payType);
            tvAmount = (TextView) v.findViewById(R.id.tv_pay_amount);
            tvCheckTime = (TextView) v.findViewById(R.id.tv_time_check);
            tvCashierName = (TextView) v.findViewById(R.id.tv_cashier_name);
            tvOrderId = (TextView) v.findViewById(R.id.tv_order_id);
        }
    }
}
