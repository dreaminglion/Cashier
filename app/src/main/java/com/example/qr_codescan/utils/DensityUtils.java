package com.example.qr_codescan.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by sw on 2015/8/27.
 * Email: lx_sunwei@163.com
 * Description: dp 与 px之间的转换
 */
public class DensityUtils {

    public static int dp2px(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }
}
