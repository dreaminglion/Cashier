package com.example.qr_codescan.utils;

/**
 * Author: lishi
 * Time: 16/4/8 上午10:25
 * Email: 763258230@qq.com
 * Package: com.example.qr_codescan.utils
 * Description:
 */
public class AmountUtils {

    public static String feng2Yuan(long amount){
        return (amount / 100f) + "";

    }

}
