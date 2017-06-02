package com.example.qr_codescan.bean;

/**
 * Created by lishi on 16/4/5.
 */
public class CheckBean {
    public long updateTime;  // 更新时间
    public String note;               // 备注
    public int status;// 记录状态 -1删除 1正常
    public int sourceType; // 来源类型 1支付宝 2微信
    public long createTime; // 创建时间
    public String strDate; //字符串的格式话时间
    public int tradeFlowId; // 记录唯一标识
    public String tradeCode; // 订单号
    public int extType;   // 扩展类型 1渠道
    public String extId; // 扩展id
    public String sourceNo; // 来源订单号
    public long   amount; // 金额（单位为分）
    public int displayOrder;
    public int createBy;
    public int updateBy;
    public long lastUpdate;
    public String sourceDesc;
    public String operaterName;

}
