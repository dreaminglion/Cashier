package com.example.qr_codescan;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import com.example.qr_codescan.customview.LoadingView;

public class OrderInfoActivity extends Activity {

    private Button mCheck;

    private LoadingView mLoadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_info);

        initView();


    }

    private void initView() {
        mCheck = (Button) findViewById(R.id.btn_check);
        mLoadingView = (LoadingView) findViewById(R.id.loadView);
    }

}
