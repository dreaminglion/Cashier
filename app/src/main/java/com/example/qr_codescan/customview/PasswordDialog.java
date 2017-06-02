package com.example.qr_codescan.customview;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cleverdou.qrscan.zxing.ToastUtil;
import com.example.qr_codescan.R;
import com.example.qr_codescan.module.statement.StatementActivity;

/**
 * Author: lishi
 * Time: 16/4/13 下午8:45
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.customview
 * Description:
 */
public class PasswordDialog extends Dialog {

    private TextView tv_my_camera_take_photo;
    private TextView tv_my_camera_from_album;
    private EditText edt_password;
    private Context mContext;
    private static int mTheme = R.style.CustomDialog;

    public PasswordDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    public PasswordDialog(Context context) {
        super(context, mTheme);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_dialog);
        tv_my_camera_take_photo = (TextView) findViewById(R.id.tv_cancel);
        tv_my_camera_from_album = (TextView) findViewById(R.id.tv_sure);
        edt_password = (EditText) findViewById(R.id.edt_password);

        tv_my_camera_take_photo
                .setOnClickListener(new android.view.View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        PasswordDialog.this.dismiss();
                    }
                });

        tv_my_camera_from_album
                .setOnClickListener(new android.view.View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (edt_password.getText().toString().equals("666888")){
                            Intent intent = new Intent(mContext,
                                    StatementActivity.class);
                            mContext.startActivity(intent);
                            PasswordDialog.this.dismiss();
                        } else {
                            ToastUtil.showToast(mContext,"密码错误");
                        }
                    }
                });

    }
}
