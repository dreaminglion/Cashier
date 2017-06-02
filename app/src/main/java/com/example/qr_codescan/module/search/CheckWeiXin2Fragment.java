package com.example.qr_codescan.module.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.CircularArray;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.qr_codescan.R;
import com.example.qr_codescan.adapter.BaseCheckAdapter;
import com.example.qr_codescan.bean.CheckAndTitleBean;
import com.example.qr_codescan.bean.CheckData;
import com.example.qr_codescan.dialog.SVProgressHUD;
import com.example.qr_codescan.utils.Constant;
import com.example.qr_codescan.utils.DensityUtils;
import com.example.qr_codescan.utils.NetworkUtils;
import com.example.qr_codescan.utils.PreferenceUtils;
import com.google.gson.Gson;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * Author: lishi
 * Time: 16/4/7 下午4:28
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.module.check
 * Description:
 */
public class CheckWeiXin2Fragment extends Fragment {

    private static final String TAG = "CheckWeiXinFragment";

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private final Gson gson = new Gson();
    private CircularArray<CheckAndTitleBean> mCheckList = new CircularArray<>();
    private BaseCheckAdapter mCheckListAdapter;
    private PtrClassicFrameLayout mPtrFrame;
    private String mStoreCode;
    private int mCurrCount = 0;//当前条目，从0开始
    private static final int mItemCount = 20;
    private CheckData mCheckData;
    private boolean mLoadingFlag = false;
    private String mSearchDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_check_all, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        refreshCheck();

        mSearchDate = PreferenceUtils.getSearchDate(getActivity());
        if (TextUtils.isEmpty(mSearchDate)) return;
        mStoreCode =  PreferenceUtils.getStoreCode(getActivity());
        if (TextUtils.isEmpty(mStoreCode)) return;

        mCheckListAdapter = new BaseCheckAdapter(mRecyclerView,mCheckList);
        mCheckListAdapter.setOnLoadingListener(new BaseCheckAdapter.OnLoadingListener() {
            @Override
            public void loading() {
                //给当最后条目赋值
                mLoadingFlag = true;
                mCurrCount = mCheckList.size();
                getData();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setPadding(DensityUtils.dp2px(getActivity(), 4), 0,
                DensityUtils.dp2px(getActivity(), 4), 0);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mCheckListAdapter);

        //初次进入刷新数据
        mProgressBar.setVisibility(View.VISIBLE);
        getData();

    }


    private void initView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_check_fragment);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar_check_fragment);
        mPtrFrame = (PtrClassicFrameLayout) view.findViewById(R.id.ptr_check_frameLayout);
    }

    private void getData(){
        if (!NetworkUtils.isNetworkAvailable(getActivity())){
            SVProgressHUD.showInfoWithStatus(getActivity(), "请链接网络！");
        }
        FinalHttp http = new FinalHttp();
        AjaxParams params = new AjaxParams();
        params.put("channelCode",mStoreCode);
        params.put("payType","WEIXIN");
        params.put("date",mSearchDate);
        params.put("offset",mCurrCount+"");
        params.put("limit",mItemCount+"");

        http.get(Constant.url + "pegasus-pay-service/tradeflow/channelcode/query",
                params,
                new AjaxCallBack<String>() {
                    @Override
                    public void onSuccess(String s) {
                        super.onSuccess(s);
                        mProgressBar.setVisibility(View.GONE);
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

                            CircularArray<CheckAndTitleBean> list =  CheckAndTitleBean.translate2Bean(mCheckData);
                            for (int i = 0; i < list.size(); i++) {
                                mCheckList.addLast(list.get(i));
                            }

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
                mCurrCount = 0;
                getData();
            }
        });
    }


}
