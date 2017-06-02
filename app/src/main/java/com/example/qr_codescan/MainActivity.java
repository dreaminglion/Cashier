package com.example.qr_codescan;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cleverdou.qrscan.zxing.CaptureActivity;
import com.example.cleverdou.qrscan.zxing.ToastUtil;
import com.example.qr_codescan.bean.StoreData;
import com.example.qr_codescan.customview.PasswordDialog;
import com.example.qr_codescan.dialog.SVProgressHUD;
import com.example.qr_codescan.dialog.selector.ActionSheetDialog;
import com.example.qr_codescan.module.check.CheckListFragmentActivity;
import com.example.qr_codescan.utils.CharsetUtils;
import com.example.qr_codescan.utils.Constant;
import com.example.qr_codescan.utils.NetworkUtils;
import com.example.qr_codescan.utils.PreferenceUtils;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;


import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private final Gson gson = new Gson();

    /**
     * 显示扫描结果
     */
    private TextView mTextView;
    private TextView mTvChecklist;
    //收款金额
    private EditText mETPrice;
    //扫码付款
    private Button mBtnPay;
    private ProgressBar mProgressBar;
    //公司logo
    private ImageView mIvLogo;
    //店名
    private TextView mTvStoreName;
    private TextView mTvRmb;
    //店员名字
    private EditText mEdtName;
    private boolean isHideSoftInput = false;
    /**
     * 输入框小数的位数
     */
    private static final int DECIMAL_DIGITS = 2;
    private StoreData mStoreDatas;

    private ActionSheetDialog mSheetDialog;
    private String mStoreName;
    private String mStoreCode;
    private Dialog mDialog;
    private String cashierName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        managerPayBtn();

        haveSetStore(); //判断是否设置过店铺,没有进行设置.

        cashierName = PreferenceUtils.getCashierName(MainActivity.this);
        mEdtName.setText(cashierName);
    }

    private void closeSoftInput() {
        if (!isHideSoftInput) {
            //隐藏软键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(
                    InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
            isHideSoftInput = true;
            Logger.d("close soft input");
        }
    }

    private void haveSetStore() {
        mStoreName = PreferenceUtils.getStoreName(getApplicationContext());
        mStoreCode = PreferenceUtils.getStoreCode(getApplicationContext());
        if (mStoreName == null || mStoreName == "" || mStoreCode == null || mStoreCode == "") {
//            ToastUtil.showToast(getApplicationContext(), "请设置店铺名称 : " + storeName);
            getStoreData();
            Logger.d("请设置店铺名称 : " + mStoreName);
        } else {
            MobclickAgent.onProfileSignIn(mStoreName);//友盟组册店铺
            mTvStoreName.setText(mStoreName);
//            ToastUtil.showToast(getApplicationContext(), "storeName : " + storeName);
            Logger.d("storeName : " + mStoreName);
        }
    }

    private void managerPayBtn() {
        if (mETPrice.getText().toString().trim().length() <= 0 || mETPrice.getText() == null) {
            mBtnPay.setBackgroundResource(R.drawable.btn_not_selector);
            mBtnPay.setTextColor(getResources().getColor(R.color.btn_not_selected_text));
            mBtnPay.setClickable(false);
//			Log.d(TAG, "not price");
            Logger.d("not price");
        } else {
            mBtnPay.setBackgroundResource(R.drawable.btn_selector);
            mBtnPay.setTextColor(Color.WHITE);
            mBtnPay.setClickable(true);
//			Log.d(TAG, "have price");
            Logger.d("have price");
        }
    }

    private void initView() {

        mTextView = (TextView) findViewById(R.id.result);
        mETPrice = (EditText) findViewById(R.id.et_price);
        mTvRmb = (TextView) findViewById(R.id.tv_rmb);
        mEdtName = (EditText) findViewById(R.id.edt_name);
        mEdtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus == false) {
                    if (v instanceof EditText){
                        EditText edt = (EditText) v;
                        Logger.d(" edt char :" + CharsetUtils.getEncoding(edt.getText().toString()) + " : " + edt.getText().toString());
                    }
                    Logger.d(" get char :" + CharsetUtils.getEncoding(mEdtName.getText().toString()));
                    PreferenceUtils.setCashierName(MainActivity.this, mEdtName.getText().toString());
                    cashierName = PreferenceUtils.getCashierName(MainActivity.this);
                }
            }
        });

        mETPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
                    Logger.d("scroll to bottom");
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //打点，前面自动加0
                if (charSequence.toString().trim().substring(0).equals(".")) {
                    charSequence = "0" + charSequence;
                    mETPrice.setText(charSequence);
                    mETPrice.setSelection(2);
                }

                //有小数点“.”的，取小数点后两位的数，设置在edt上
                if (charSequence.toString().contains(".")) {
                    if (charSequence.length() - 1 - charSequence.toString().indexOf(".") > 2) {
                        charSequence = charSequence.toString().subSequence(0,
                                charSequence.toString().indexOf(".") + 3);
                        mETPrice.setText(charSequence);
                        mETPrice.setSelection(charSequence.length());
                    }
                }

                //没有"."的最大数为999999 ，有"."最大为999999.99
                if (charSequence.length() > 6) {
                    if (!charSequence.toString().contains(".")) {
                        mETPrice.setText(charSequence.subSequence(0, 6));
                        mETPrice.setSelection(6);
                        return;
                    }
                }

                //以0开头，第二位不是“.”的，只显示0，移动光标到末尾
                if (charSequence.toString().startsWith("0")
                        && charSequence.toString().trim().length() > 1) {
                    if (!charSequence.toString().substring(1, 2).equals(".")) {
                        mETPrice.setText(charSequence.subSequence(0, 1));
                        mETPrice.setSelection(1);//把光标，移动到指定索引字符位置。
                        return;
                    }
                }

                managerPayBtn();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        mETPrice.setFilters(new InputFilter[]{lengthfilter, new InputFilter.LengthFilter(9)});

        //点击按钮跳转到二维码扫描界面，这里用的是startActivityForResult跳转
        //扫描完了之后调到该界面
        mBtnPay = (Button) findViewById(R.id.button1);
        mBtnPay.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mStoreCode == null || mStoreCode == "") {
                    SVProgressHUD.showInfoWithStatus(MainActivity.this, "请确认店铺信息");
                    return;
                }

                if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                    SVProgressHUD.showInfoWithStatus(MainActivity.this, "请链接网络！");
                    return;
                }

                //支付金额小于一分
                if (Float.valueOf(mETPrice.getText().toString().trim()) * 100 < 1) {
                    ToastUtil.showToast(getApplicationContext(), "请输入正确的金额");
                    return;
                }

//                ToastUtil.showToast(MainActivity.this, Float.valueOf(mETPrice.getText().toString().trim()) * 100);

                Intent intent = new Intent(MainActivity.this,
                        com.example.cleverdou.qrscan.zxing.CaptureActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivityForResult(intent, SCANNIN_GREQUEST_CODE);

            }
        });

        LinearLayout back = (LinearLayout) findViewById(R.id.lly_back);
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                PasswordDialog dialog = new PasswordDialog(MainActivity.this);
                dialog.show();

            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mIvLogo = (ImageView) findViewById(R.id.iv_logo);
        mIvLogo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
//                Intent intent = new Intent(MainActivity.this,CheckListActivity.class);
//                intent.putExtra(PreferenceUtils.STORE_CODE,mStoreCode);
//                startActivity(intent);
                return false;
            }
        });

        mTvStoreName = (TextView) findViewById(R.id.tv_storeName);
        mTvStoreName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getStoreData();
            }
        });
        mTvChecklist = (TextView) findViewById(R.id.tv_store_checklist);
        mTvChecklist.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mStoreCode == null || mStoreCode == "") {
                    SVProgressHUD.showInfoWithStatus(MainActivity.this, "请确认店铺信息");
                    return;
                }

//                Intent intent = new Intent(MainActivity.this,CheckListActivity.class);
                Intent intent = new Intent(MainActivity.this, CheckListFragmentActivity.class);
                intent.putExtra(PreferenceUtils.STORE_CODE, mStoreCode);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String msg = data.getStringExtra(CaptureActivity.QRSCAN_RESULT);
            mTextView.setText(msg);
            if (!TextUtils.isEmpty(msg)) try {
                getData(msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Logger.d("扫码失败");
        }
    }

    private void getStoreData() {
        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            SVProgressHUD.showInfoWithStatus(MainActivity.this, "请链接网络！");
        }

        mProgressBar.setVisibility(View.VISIBLE);

        FinalHttp http = new FinalHttp();
        http.get(Constant.url + "pegasus-pay-service/channel/all", new AjaxCallBack<String>() {
            @Override
            public void onSuccess(String s) {
                super.onSuccess(s);
                mProgressBar.setVisibility(View.GONE);
                mStoreDatas = gson.fromJson(s, StoreData.class);
                storeSelector();
            }

            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                super.onFailure(t, errorNo, strMsg);
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void getData(String code) throws JSONException {
        if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
            SVProgressHUD.showInfoWithStatus(MainActivity.this, "请链接网络！");
        }

        mProgressBar.setVisibility(View.VISIBLE);

        String charset = CharsetUtils.getEncoding(cashierName);

        Logger.d("one charset : " + charset + "||  cashierName : " + cashierName);


        JSONObject obj = new JSONObject();
        obj.put("payType", "ALIPAY");
        obj.put("channelCode", mStoreCode);
        obj.put("operaterName", cashierName);
        obj.put("amount", Float.valueOf(mETPrice.getText().toString().trim()) * 100);
        obj.put("authCode", code);
        FinalHttp http = new FinalHttp();
        try {
            http.configTimeout(60000);//一分钟等待付款时间
            http.post(Constant.url +
                            "pegasus-pay-service/payment",
                    new StringEntity(obj.toString(), HTTP.UTF_8),
                    "application/json",
                    new AjaxCallBack<String>() {
                        @Override
                        public void onSuccess(String s) {
                            super.onSuccess(s);
                            mProgressBar.setVisibility(View.GONE);
                            if (s.contains("\"code\":\"200\"")) {
//                        Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_SHORT).show();
                                mETPrice.setText("");
                                SVProgressHUD.showSuccessWithStatus(MainActivity.this, "恭喜，支付成功");
                            } else {
//                        Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                                SVProgressHUD.showErrorWithStatus(MainActivity.this, "抱歉，支付失败",
                                        SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                            }
                        }

                        @Override
                        public void onFailure(Throwable t, int errorNo, String strMsg) {
                            super.onFailure(t, errorNo, strMsg);
                            mProgressBar.setVisibility(View.GONE);
//                    Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                            SVProgressHUD.showErrorWithStatus(MainActivity.this, "抱歉，支付失败",
                                    SVProgressHUD.SVProgressHUDMaskType.GradientCancel);
                        }
                    });

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置小数位数控制
     */
    InputFilter lengthfilter = new InputFilter() {
        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {
            // 删除等特殊字符，直接返回
            if ("".equals(source.toString())) {
                return null;
            }
            String dValue = dest.toString();
            String[] splitArray = dValue.split("\\.");
            if (splitArray.length > 1) {
                String dotValue = splitArray[1];
                int diff = dotValue.length() + 1 - DECIMAL_DIGITS;
                if (diff > 0) {
                    return source.subSequence(start, end - diff);
                }
            }
            return null;
        }
    };

    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(this);
    }

    private void storeSelector() {

        mSheetDialog = null;
        if (mSheetDialog == null) {
            mSheetDialog = new ActionSheetDialog(MainActivity.this);
            mSheetDialog.builder()
                    .setTitle("请选择店铺")
                    .setCancelable(false)
                    .setCanceledOnTouchOutside(false);
            if (mStoreDatas.data != null && mStoreDatas.data.size() > 0) {
                for (int i = 0; i < mStoreDatas.data.size(); i++) {
                    mSheetDialog.addSheetItem(
                            mStoreDatas.data.get(i).name,
                            ActionSheetDialog.SheetItemColor.Blue,
                            new ActionSheetDialog.OnSheetItemClickListener() {
                                @Override
                                public void onClick(int which) {
                                    PreferenceUtils.setStoreName(getApplicationContext(), mStoreDatas.data.get(which - 1).name);
                                    PreferenceUtils.setStoreCode(getApplicationContext(), mStoreDatas.data.get(which - 1).code);
                                    mTvStoreName.setText(mStoreDatas.data.get(which - 1).name);
//                                    mTvRmb.setVisibility(View.VISIBLE);
                                    mStoreCode = mStoreDatas.data.get(which - 1).code;
                                    Logger.d("mStoreCode : " + mStoreCode);
                                }
                            });
                }
            }
        }

        if (mSheetDialog != null) {
            mSheetDialog.show();
//            mTvRmb.setVisibility(View.INVISIBLE);
        }

    }

}
