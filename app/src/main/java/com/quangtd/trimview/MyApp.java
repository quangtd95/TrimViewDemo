package com.quangtd.trimview;

import android.app.Application;

/**
 * QuangTD on 10/9/2017.
 */

public class MyApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        VideoUtils.loadLibrary(this);
    }
}
