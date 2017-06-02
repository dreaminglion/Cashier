package com.example.qr_codescan.bean;

import android.support.v4.util.CircularArray;
import android.text.TextUtils;

import com.example.qr_codescan.utils.AmountUtils;
import com.example.qr_codescan.utils.Constant;
import com.example.qr_codescan.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: lishi
 * Time: 16/4/8 上午9:59
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.bean
 * Description:
 */
public class CheckAndTitleBean {
    public boolean isTitle;
    public String titleDate;
    public String titleAmount;

    public String payType;
    public String payDate;
    public String payAmount;
    public String cashierName;
    public String sourceNo;

    public static CircularArray<CheckAndTitleBean> translate2Bean(CheckData checkData){
        CircularArray<CheckAndTitleBean> list = null;
        if (checkData.data.dailyAmountItems == null || checkData.data.items == null) return null;
        for (DailyAmountItemBean dailytitleBean : checkData.data.dailyAmountItems) {
            if (list == null) {
                list = new CircularArray<>();
            }
            CheckAndTitleBean titleBean = new CheckAndTitleBean();
            titleBean.isTitle = true;
            titleBean.titleDate = dailytitleBean.date.substring(0,10);
            titleBean.titleAmount = AmountUtils.feng2Yuan(dailytitleBean.amount);
            list.addLast(titleBean);
            for (CheckBean dailyCheckBean : checkData.data.items) {
                String date = dailyCheckBean.strDate;
                if (date.contains(titleBean.titleDate)){
                    CheckAndTitleBean checkBean = new CheckAndTitleBean();
                    checkBean.isTitle = false;
                    checkBean.payType = dailyCheckBean.sourceType == 1 ? "支付宝" : "微信" ;
                    checkBean.payDate = date;
                    checkBean.payAmount = AmountUtils.feng2Yuan(dailyCheckBean.amount);
                    checkBean.cashierName = dailyCheckBean.operaterName;
                    checkBean.sourceNo = dailyCheckBean.sourceNo;
                    list.addLast(checkBean);
                }
            }

        }
        return list;
    }
}
