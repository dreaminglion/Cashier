package com.example.qr_codescan.module.statement;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.CircularArray;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cleverdou.qrscan.zxing.ToastUtil;
import com.example.qr_codescan.R;
import com.example.qr_codescan.bean.StatementAndTitleBean;
import com.example.qr_codescan.bean.StatementData;
import com.example.qr_codescan.dialog.SVProgressHUD;
import com.example.qr_codescan.module.search.CheckListFragment2Activity;
import com.example.qr_codescan.utils.Constant;
import com.example.qr_codescan.utils.DensityUtils;
import com.example.qr_codescan.utils.NetworkUtils;
import com.example.qr_codescan.utils.TimeUtils;
import com.google.gson.Gson;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Author: lishi
 * Time: 16/4/13 下午3:03
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.module.statement
 * Description:
 */
public class StatementActivity extends AppCompatActivity {
    private TextView mTvDate;
    private TextView mTvDate1;
    private TextView mTvDate2;
    private ProgressBar mProgressBar;
    private Dialog mDialog;
    private RecyclerView mRecyclerView;

    private final Gson gson = new Gson();
    private CircularArray<StatementAndTitleBean> mDatalist;
    private StatementAdapter statementAdapter;
    private SimpleDateFormat sdf = new SimpleDateFormat(Constant.DAILYDTAEFORMAT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement);

        initView();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());//-  1 * 24 * 60 * 60 * 1000
        mTvDate1.setText(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH));
        cal.setTimeInMillis(System.currentTimeMillis());
        mTvDate2.setText(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH));
        mTvDate.setText("今天");

        mRecyclerView.setLayoutManager(new LinearLayoutManager(
                getApplicationContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setPadding(DensityUtils.dp2px(getApplicationContext(), 4), 0,
                DensityUtils.dp2px(getApplicationContext(), 4), 0);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        getData();

    }

    private void initView() {
        LinearLayout llyBack = (LinearLayout) findViewById(R.id.lly_back);
        llyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvSetting = (TextView) findViewById(R.id.tv_setting);
        tvSetting.setVisibility(View.GONE);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_check_fragment);
        mTvDate = (TextView) findViewById(R.id.tv_storeName);
        mTvDate1 = (TextView) findViewById(R.id.tv_date1);
        mTvDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(mTvDate1, 1);
            }
        });
        mTvDate2 = (TextView) findViewById(R.id.tv_date2);
        mTvDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(mTvDate2, 2);
            }
        });
    }

    private void createDialog(final TextView textView, final int flag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StatementActivity.this);
        View view = View.inflate(StatementActivity.this, R.layout.date_dialog, null);
        final DatePicker datePicker = (DatePicker) view.findViewById(R.id.date_picker);
        TextView tv_close_date_chooser = (TextView) view.findViewById(R.id.tv_close_date_chooser);
        tv_close_date_chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        TextView tv_sure_date_chooser = (TextView) view.findViewById(R.id.tv_sure_date_chooser);
        tv_sure_date_chooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String date = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                    if (flag == 1) {

                        if (sdf.parse(date).getTime() > sdf.parse(mTvDate2.getText().toString()).getTime()) {
                            ToastUtil.showToast(StatementActivity.this, "起始时间大于结束时间");
                            return;
                        }

                    } else {
                        if (sdf.parse(date).getTime() < sdf.parse(mTvDate1.getText().toString()).getTime()) {
                            ToastUtil.showToast(StatementActivity.this, "结束时间小于起始时间");
                            return;
                        }
                    }

                    textView.setText(date);
                    mTvDate.setText(mTvDate1.getText() + " ~ " + mTvDate2.getText());
                    mDialog.dismiss();

                    getData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setView(view);
        mDialog = builder.create();
        mDialog.show();
    }

    private void getData() {
        mProgressBar.setVisibility(View.VISIBLE);

        try {
            String date2 = TimeUtils.formatData(Constant.DAILYDTAEFORMAT,
                    sdf.parse(mTvDate2.getText().toString()).getTime() + 1 * 24 * 60 * 60 * 1000);


            if (!NetworkUtils.isNetworkAvailable(StatementActivity.this)) {
                SVProgressHUD.showInfoWithStatus(StatementActivity.this, "请链接网络！");
            }

            FinalHttp http = new FinalHttp();
            AjaxParams params = new AjaxParams();
            params.put("channelCode", "");
            params.put("dateFrom", mTvDate1.getText().toString());
            params.put("dateTo", date2);

        http.get(Constant.url + "pegasus-pay-service/tradeflow/statistics/allPayType/query",
                    params,
                    new AjaxCallBack<String>() {
                        @Override
                        public void onSuccess(String s) {
                            super.onSuccess(s);
                            mProgressBar.setVisibility(View.GONE);
                            StatementData statementData = gson.fromJson(s, StatementData.class);
                            if (statementData != null) {
                                mDatalist = StatementAndTitleBean.translate2Bean(statementData,
                                        mTvDate1.getText() + " ~ " + mTvDate2.getText());
                                if (mDatalist == null){
                                    mDatalist = new CircularArray<>();
                                }
                                statementAdapter = new StatementAdapter(mRecyclerView, mDatalist);
                                statementAdapter.setLoadingComplete();
                                mRecyclerView.setAdapter(statementAdapter);
                                try {
                                    final String date2 = TimeUtils.formatData(Constant.DAILYDTAEFORMAT,
                                            sdf.parse(mTvDate2.getText().toString()).getTime() + 1 * 24 * 60 * 60 * 1000);
                                statementAdapter.setOnItemClickListener(new StatementAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(int position) {
                                        Intent intent = new Intent(StatementActivity.this,
                                                StatementDetailFragmentActivity.class);
                                        intent.putExtra("code",mDatalist.get(position).storeCode);
                                        intent.putExtra("name",mDatalist.get(position).storeName);
                                        intent.putExtra("date1",mTvDate1.getText());
                                        intent.putExtra("date2",date2);
                                        startActivity(intent);
                                    }
                                });
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            super.onFailure(t, errorNo, strMsg);
                            mProgressBar.setVisibility(View.GONE);

                        }
                    });

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
