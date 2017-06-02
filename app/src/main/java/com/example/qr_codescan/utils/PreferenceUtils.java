package com.example.qr_codescan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by lishi on 2015/12/25.
 */
public class PreferenceUtils {

    private static SharedPreferences sSharedPreferences;

    public static final String STORE_NAME = "store_name";
    public static final String STORE_CODE = "store_code";
    public static final String SEARCH_DATE = "search_date";
    public static final String CASHIER_NAME = "cashier_name";

    /**
     * 获取SharedPreferences
     *
     * @param context context
     * @return SharedPreferences
     */
    private static SharedPreferences getPreference(Context context) {
        if (sSharedPreferences == null) {
            sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sSharedPreferences;
    }

    public static String getStoreName(Context context) {
        return getPreference(context).getString(STORE_NAME, "");
    }

    public static void setStoreName(Context context, String name) {
        getPreference(context).edit().putString(STORE_NAME, name).apply();
    }

    public static String getStoreCode(Context context) {
        return getPreference(context).getString(STORE_CODE, "");
    }

    public static void setStoreCode(Context context, String name) {
        getPreference(context).edit().putString(STORE_CODE, name).apply();
    }

    public static String getSearchDate(Context context) {
        return getPreference(context).getString(SEARCH_DATE, "");
    }

    public static void setSearchDate(Context context, String name) {
        getPreference(context).edit().putString(SEARCH_DATE, name).apply();
    }

    public static String getCashierName(Context context) {
        return getPreference(context).getString(CASHIER_NAME, "");
    }

    public static void setCashierName(Context context, String name) {
        getPreference(context).edit().putString(CASHIER_NAME, name).apply();
    }

}
