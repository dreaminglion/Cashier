package com.example.qr_codescan.bean;

import android.support.v4.util.CircularArray;

import com.example.qr_codescan.utils.AmountUtils;

/**
 * Author: lishi
 * Time: 16/4/8 上午9:59
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.bean
 * Description:
 */
public class StatementAndTitleBean {
    public boolean isTitle;
    public String titleDate;
    public String titleAmount;
    public String titleAliAmount;
    public String titleWeixinAmount;

    public String storeName;
    public String storeCode;
    public String amount;
    public String aliAmount;
    public String weixinAmount;

    public static CircularArray<StatementAndTitleBean> translate2Bean(StatementData statementData,String mDate){
        CircularArray<StatementAndTitleBean> list = null;
        long allAmount = 0;
        long wAmount = 0;
        long aAmount = 0;
        if (statementData.data == null || statementData.data.size() == 0) return null;
        for (StatementBean bean : statementData.data) {
            if (list == null) {
                list = new CircularArray<>();
            }
            allAmount += bean.amount;
            aAmount += bean.alipayAmount;
            wAmount += bean.weixinAmount;

            StatementAndTitleBean normalBean = new StatementAndTitleBean();
            normalBean.isTitle = false;
            normalBean.storeName = bean.channelName;
            normalBean.storeCode = bean.ext_id;
            normalBean.amount = bean.amount/100f + "";
            normalBean.aliAmount = bean.alipayAmount/100f + "";
            normalBean.weixinAmount = bean.weixinAmount/100f + "";

            list.addLast(normalBean);
        }
        StatementAndTitleBean titleBean = new StatementAndTitleBean();
        titleBean.isTitle = true;
        titleBean.titleDate = mDate;
        titleBean.titleAmount = allAmount/100f + "";
        titleBean.titleAliAmount = aAmount/100f + "";
        titleBean.titleWeixinAmount = wAmount/100f + "";
        list.addFirst(titleBean);
        return list;
    }
}
