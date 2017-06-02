package com.example.qr_codescan.module.statement;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.cleverdou.qrscan.zxing.CaptureActivity;
import com.example.cleverdou.qrscan.zxing.ToastUtil;
import com.example.qr_codescan.R;
import com.example.qr_codescan.customview.tab.TabLayout;
import com.example.qr_codescan.module.check.CheckAlipayFragment;
import com.example.qr_codescan.module.check.CheckAllFragment;
import com.example.qr_codescan.module.check.CheckWeiXinFragment;
import com.example.qr_codescan.utils.PreferenceUtils;
import com.orhanobut.logger.Logger;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: lishi
 * Time: 16/4/7 下午3:11
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.module.check
 * Description:
 */
public class StatementListFragmentActivity extends AppCompatActivity {

    private static final String TAG = "CheckListFragmentActivity";
    private final static int SETTING_CODE = 1;
    
    private TextView mTvStoreName;
    private LinearLayout mLlyBack;
    private TabLayout mTabCheck;
    private ViewPager mViewPager;

    private List<Fragment> mFragments = new ArrayList<>();
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_statement_list);

        initView();

        String storeName = PreferenceUtils.getStoreName(getApplicationContext());
        if (storeName != null || storeName != "") {
            mTvStoreName.setText(storeName + "收款纪录");
        }

        if (mFragments != null && mFragments.size() < 1) {
            mFragments.add(new CheckAllFragment());
            mFragments.add(new CheckAlipayFragment());
            mFragments.add(new CheckWeiXinFragment());
        }

        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mTabCheck.setViewPager(mViewPager);

    }

    private void initView() {
        mTvStoreName = (TextView) findViewById(R.id.tv_storeName);
        TextView tvSearch = (TextView) findViewById(R.id.tv_search);
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StatementListFragmentActivity.this, StatementSettingActivity.class);
                startActivityForResult(intent, SETTING_CODE);
                finish();
            }
        });
        mLlyBack = (LinearLayout) findViewById(R.id.lly_back);
        mLlyBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTabCheck = (TabLayout) findViewById(R.id.tab_check);
        mTabCheck.setTabViewBackgroundColors(R.color.tab_my_page_back_color,
                R.color.tab_my_page_back_color);
        mTabCheck.setTabTextColors(R.color.tab_my_page, R.color.tab_my_page);
        mTabCheck.setTabTextSize(14);
        mTabCheck.setUnderlineIndicator(true);
        mTabCheck.setSelectedIndicatorColors(
                getResources().getColor(R.color.repository_tab_text_selected));
        mTabCheck.setBottomBorderColor(R.color.title_line);
        mTabCheck.addTab(mTabCheck.newTab().setTabText("全部"));
        mTabCheck.addTab(mTabCheck.newTab().setTabText("支付宝"));
        mTabCheck.addTab(mTabCheck.newTab().setTabText("微信"));

        mViewPager = (ViewPager) findViewById(R.id.viewPage_my_page);
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String date1 = data.getStringExtra("date1");
            String date2 = data.getStringExtra("date2");
            String storeCode = data.getStringExtra("code");
            Logger.d("code : " + storeCode);

        } else {
            Logger.d("扫码失败");
        }
    }
}
