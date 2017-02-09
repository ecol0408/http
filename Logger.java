package com.ecol.http;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by YI on 2016/2/23.
 * 日志打印
 */
public final class Logger {
    private static final String TAG = "com.ecol.http";
    static File f = new File(Environment.getExternalStorageDirectory(), "zhenler_log.txt");

    public static final void log(Object o) {
        if (!BaseHttpConstant.LOG) return;
        if (o != null) {
            Log.v(TAG, o.toString());
        }
    }
    public static final void log(String tag,Object o){
        try {
            FileOutputStream fos = new FileOutputStream(f, true);
            StringBuffer sb = new StringBuffer();
            sb.append(o.toString());
            sb.append("\n");
            fos.write(sb.toString().getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
