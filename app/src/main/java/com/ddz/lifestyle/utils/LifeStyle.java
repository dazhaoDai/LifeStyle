package com.ddz.lifestyle.utils;

import android.app.Application;
import android.content.Context;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import cn.bmob.v3.Bmob;
import io.rong.imkit.RongBaseActivity;
import io.rong.imkit.RongIM;

import static com.ddz.lifestyle.utils.Constants.APPID;


/**
 * Author : ddz
 * Creation time   : 2016/12/2 10:08
 * Fix time   :  2016/12/2 10:08
 */

public class LifeStyle extends Application {

    public static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            context = getApplicationContext();
        }
        //Bmob初始化
        Bmob.initialize(this, Constants.APPID);
        //融云初始化
        RongIM.init(this);
        LifeStyleAppContext.init(this);
    }


    /**
     * 获取当前运行的进程名
     * @return
     */
    public static String getCurProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
