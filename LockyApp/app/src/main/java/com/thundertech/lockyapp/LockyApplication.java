package com.thundertech.lockyapp;

import android.app.Application;
import com.thundertech.locky.utils.AppMgr;

public class LockyApplication extends Application {
    private String tag = "LockyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppMgr.context = this;
    }
}
