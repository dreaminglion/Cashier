package com.example.qr_codescan.module.statement;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cleverdou.qrscan.zxing.ToastUtil;
import com.example.qr_codescan.R;
import com.example.qr_codescan.bean.StoreBean;
import com.example.qr_codescan.bean.StoreData;
import com.example.qr_codescan.dialog.SVProgressHUD;
import com.example.qr_codescan.module.search.CheckListFragment2Activity;
import com.example.qr_codescan.utils.Constant;
import com.example.qr_codescan.utils.NetworkUtils;
import com.example.qr_codescan.utils.PreferenceUtils;
import com.google.gson.Gson;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Author: lishi
 * Time: 16/4/12 下午6:19
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.module.statement
 * Description:
 */
public class StatementSettingActivity extends AppCompatActivity {

    private TextView mTvDate;
    private ListView mLvStore;
    private TextView mTvDate1;
    private TextView mTvDate2;
    private ProgressBar mProgressBar;


    private final Gson gson = new Gson();
    private List<StoreBean> mListStore = new ArrayList<>();
    private StoreCheckAdapter mStoreCheckAdapter;
    private Dialog mDialog;
    private String mStoreCode;
    private boolean isAllStore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statement_setting);

        initView();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis() -  7 * 24 * 60 * 60 * 1000);
        mTvDate1.setText(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH));
        cal.setTimeInMillis(System.currentTimeMillis());
        mTvDate2.setText(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DAY_OF_MONTH));
        mTvDate.setText(mTvDate1.getText() + " ~ " + mTvDate2.getText());

        getStoreData();
    }

    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        LinearLayout llyBack = (LinearLayout) findViewById(R.id.lly_back);
        llyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView tvSetting = (TextView) findViewById(R.id.tv_setting);
        tvSetting.setVisibility(View.GONE);
        mTvDate = (TextView) findViewById(R.id.tv_storeName);
        mLvStore = (ListView) findViewById(R.id.lv_store);
        // 绑定listView的监听器
        mLvStore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
                StoreCheckAdapter.ViewHolder holder = (StoreCheckAdapter.ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                // 将CheckBox的选中状况记录下来
                StoreCheckAdapter.getIsSelected().put(arg2, holder.cb.isChecked());
            }
        });
        mTvDate1 = (TextView) findViewById(R.id.tv_date1);
        mTvDate1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(mTvDate1);
            }
        });

        mTvDate2 = (TextView) findViewById(R.id.tv_date2);
        mTvDate2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(mTvDate2);
            }
        });
        Button button = (Button) findViewById(R.id.btn_statement_check);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleDateFormat sdf = new SimpleDateFormat(Constant.DAILYDTAEFORMAT);
                try {
                    if (sdf.parse(mTvDate1.getText()+"").getTime() > sdf.parse(mTvDate2.getText()+"").getTime()){
                        ToastUtil.showToast(StatementSettingActivity.this,"起始时间大于结束时间！");
                        return;
                    }
                    mStoreCode = "";
                    isAllStore = false;
                    // 遍历list的长度，将已选的店铺进行code拼接
                    for (int i = 0; i < mListStore.size(); i++) {
                        if (StoreCheckAdapter.getIsSelected().get(i)) {
                            // i＝＝0 全国被选择
                            if (i == 0){
                                isAllStore = true;
                            } else {
                                mStoreCode += mListStore.get(i).code + ",";
                            }
                        }
                    }
                    // 选择全国，code串为空
                    if (isAllStore){
                        mStoreCode = "";
                    }
                    // 去掉code最后一个“，”
                    if (mStoreCode != ""){
                        mStoreCode = mStoreCode.substring(0,mStoreCode.length() - 1);
                    }

                    Intent intent = new Intent(StatementSettingActivity.this,StatementListFragmentActivity.class);
                    intent.putExtra("date1",mTvDate1.getText());
                    intent.putExtra("date2",mTvDate2.getText());
                    intent.putExtra("code",mStoreCode);
                    startActivity(intent);
                    finish();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void createDialog(final TextView textView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StatementSettingActivity.this);
        View view = View.inflate(StatementSettingActivity.this, R.layout.date_dialog, null);
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
                String date = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                textView.setText(date);
                mTvDate.setText(mTvDate1.getText() + " ~ " + mTvDate2.getText());
                mDialog.dismiss();
            }
        });
        builder.setView(view);
        mDialog = builder.create();
        mDialog.show();
    }

    private void getStoreData() {

        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            SVProgressHUD.showInfoWithStatus(StatementSettingActivity.this, "请链接网络！");
        }

        mProgressBar.setVisibility(View.VISIBLE);
        FinalHttp http = new FinalHttp();

        http.get(Constant.url + "pegasus-pay-service/channel/all", new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                mProgressBar.setVisibility(View.GONE);
                StoreData storeData = gson.fromJson(s, StoreData.class);
                mListStore.clear();
                if (storeData != null && storeData.data.size() > 0){
                    StoreBean bean = new StoreBean();
                    bean.name = "全国";
                    bean.code = "9999";
                    mListStore.add(bean);
                    mListStore.addAll(storeData.data);
                    mStoreCheckAdapter = new StoreCheckAdapter(mListStore,StatementSettingActivity.this);
                    mLvStore.setAdapter(mStoreCheckAdapter);
                }
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }
}
