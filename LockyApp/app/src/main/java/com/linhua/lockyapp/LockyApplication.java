package com.linhua.lockyapp;

import android.app.Application;
import com.linhua.locky.utils.AppMgr;

public class LockyApplication extends Application {
    private String tag = "LockyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppMgr.context = this;
    }
}
