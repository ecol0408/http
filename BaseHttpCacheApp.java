package com.ecol.http;

import android.app.Application;
import android.net.http.HttpResponseCache;

import java.io.File;
import java.io.IOException;

/**
 * Created by YI on 2016/2/23.
 */
public class BaseHttpCacheApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        File file = new File(getCacheDir(), "http");
        long size = 1024 * 1024 * 50;
        try {
            HttpResponseCache.install(file, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
