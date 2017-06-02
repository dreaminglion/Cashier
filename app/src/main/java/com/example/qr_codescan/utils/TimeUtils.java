package com.example.qr_codescan.utils;


import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * Created by lishi on 16/4/6.
 */
public class TimeUtils {

    public static String formatData(String dataFormat, long timeStamp) {
        if (timeStamp == 0) {
            return "";
        }
//        timeStamp = timeStamp * 1000;
        String result = "";
        SimpleDateFormat format = new SimpleDateFormat(dataFormat);
        result = format.format(new Date(timeStamp));
        return result;
    }

}
