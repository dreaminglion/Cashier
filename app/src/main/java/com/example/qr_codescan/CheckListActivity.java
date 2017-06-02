package com.example.qr_codescan;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.util.CircularArray;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.qr_codescan.adapter.BaseLoadingAdapter;
import com.example.qr_codescan.adapter.CheckListAdapter;
import com.example.qr_codescan.bean.CheckBean;
import com.example.qr_codescan.bean.CheckData;
import com.example.qr_codescan.utils.Constant;
import com.example.qr_codescan.utils.DensityUtils;
import com.example.qr_codescan.utils.PreferenceUtils;
import com.google.gson.Gson;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class CheckListActivity extends Activity {

    private ProgressBar mProgressBar;

    private final Gson gson = new Gson();

    public String mStoreCode;

    private CheckData mCheckData;

    private PtrClassicFrameLayout mPtrFrame;

    private RecyclerView mRecyclerView;

    private TextView mTvNoCheck;

    private TextView mTvStoreName;

    private int mCurrCount = 0;//当前条目，从0开始

    private boolean mLoadingFlag = false;

    private static final int mItemCount = 20;

    private CircularArray<CheckBean> mCheckList = new CircularArray<CheckBean>();
    private CheckListAdapter mCheckListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        initView();

        refreshCheck();

        String storeName = PreferenceUtils.getStoreName(getApplicationContext());
        if (storeName != null || storeName != "" ){
            mTvStoreName.setText(storeName +"账单");
        }

        mStoreCode = getIntent().getStringExtra(PreferenceUtils.STORE_CODE);

        mCheckListAdapter = new CheckListAdapter(mRecyclerView,mCheckList);
        mCheckListAdapter.setOnLoadingListener(new BaseLoadingAdapter.OnLoadingListener() {
            @Override
            public void loading() {
                //给当最后条目赋值
                mLoadingFlag = true;
                mCurrCount = mCheckList.size();
                getData();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                CheckListActivity.this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setPadding(DensityUtils.dp2px(CheckListActivity.this, 8), 0,
                DensityUtils.dp2px(CheckListActivity.this, 8), 0);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mCheckListAdapter);

        //初次进入刷新数据
        mProgressBar.setVisibility(View.VISIBLE);
        getData();

    }

    private void initView() {

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        LinearLayout back = (LinearLayout) findViewById(R.id.lly_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mPtrFrame = (PtrClassicFrameLayout) findViewById(R.id.ptr_frameLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);

        mTvNoCheck = (TextView) findViewById(R.id.tv_no_check);

        mTvStoreName = (TextView) findViewById(R.id.tv_storeName);

    }

    private void getData(){
        FinalHttp http = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("channelCode",mStoreCode);
        params.put("offset",mCurrCount+"");
        params.put("limit",mItemCount+"");

        http.get(Constant.url + "pegasus-pay-service/tradeflow/channelcode",
                params,
                new AjaxCallBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        super.onSuccess(s);
                        mProgressBar.setVisibility(View.GONE);
                        mTvNoCheck.setVisibility(View.GONE);
                        mCheckData = gson.fromJson(s, CheckData.class);
                        if (mCheckData == null || mCheckData.data.items == null || mCheckData.data.items.size() < 1) {
//                            mTvNoCheck.setVisibility(View.VISIBLE);
//                            mPtrFrame.setVisibility(View.GONE);
                        } else {

                            //不是加载清空数据的数据
                            if (!mLoadingFlag){
                                mCheckList.clear();
                            }

                            //加载获得到了，数据加载完成。
                            //先删除，数组中加入最有一项的null加载项，然后在添加加载获得的数据。
                            if (mLoadingFlag && mCheckData.data.items.size() != 0 ){
                                mCheckListAdapter.setLoadingComplete();
                                mLoadingFlag = false;
                            }

                            for (int i = 0; i < mCheckData.data.items.size(); i++) {
                                mCheckList.addLast(mCheckData.data.items.get(i));
                            }
                        }

                        //没有数据，显示TextView提醒。
                        if (mCheckList.size() < 1){
                            mTvNoCheck.setVisibility(View.VISIBLE);
                            mPtrFrame.setVisibility(View.GONE);
                        }

                        //没有数据加载
                        if (mLoadingFlag && mCheckData.data.items.size() == 0){
                            mCheckListAdapter.setLoadingNoMore();
                            mLoadingFlag = false;
                        }

                        mCheckListAdapter.notifyDataSetChanged();

                        mPtrFrame.refreshComplete();
//                        ToastUtil.showToast(CheckListActivity.this, "success");
                    }

                    @Override
                    public void onFailure(Throwable t, int errorNo, String strMsg) {
                        super.onFailure(t, errorNo, strMsg);
                        mProgressBar.setVisibility(View.GONE);
                        mTvNoCheck.setVisibility(View.GONE);
                        if (mCheckList.size() < 1) {
                            mTvNoCheck.setVisibility(View.VISIBLE);
                            mPtrFrame.setVisibility(View.GONE);
                        }

                        if (mLoadingFlag){
                            mCheckListAdapter.setLoadingError();
                            mLoadingFlag = false;
                        }

                        mPtrFrame.refreshComplete();
//                        ToastUtil.showToast(CheckListActivity.this, "fail");
                    }
                });
    }

    private void refreshCheck() {
        mPtrFrame.setLastUpdateTimeRelateObject(this);
        mPtrFrame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
//                Runnable runnable = new Runnable(){
//                    @Override
//                    public void run() {
//                        // TODO Auto-generated method stub
//                        getData();
//                    }
//
//                };
//                mPtrFrame.postDelayed(runnable,500);
                //刷新从0开始请求数据
                mCurrCount = 0;
                getData();
            }
        });

    }



}
