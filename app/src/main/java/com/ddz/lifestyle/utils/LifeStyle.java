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
        LifeStyleAppContext.init(this);
    }




}
