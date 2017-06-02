package com.example.qr_codescan;

import android.app.Application;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogTool;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;


/**
 * Created by lishi on 16/3/30.
 */
public class MainApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init("TheBeastWish")            // default TAG or use just init()
                .methodCount(1)                 // default 2 , 控制打印log的 method 行数
//                .hideThreadInfo()               // default shown,隐藏线程名称
                .logLevel(LogLevel.FULL)        // default LogLevel.FULL,打印日志等级, full 全打 none 不打印
                .methodOffset(0)                // default 0 ,打印偏移对应数值的method位置
                .logTool(new AndroidLogTool()); // custom log tool, optional
//        Logger.t(TAG).d("hello");//二级 tag 在 第一个 tag 后面拼接一个 也可进行 二级 tag 过滤


        MobclickAgent.openActivityDurationTrack(false);//友盟 禁止默认的页面统计方式，这样将不会再自动统计Activity
//        MobclickAgent.setDebugMode( true );//集成测试
    }

}
