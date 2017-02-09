package com.ecol.http;

import android.app.Application;

import com.ecol.App;

/**
 * Created by YI on 2016/2/26.
 */
public final class BaseHttpConstant {
    /**
     * 是否打印日志
     */
    public static boolean LOG = true;
    /**
     * 最大HTTP请求数
     */
    public static int HTTP_MAX_REQUESTS = 500;
    /**
     * HTTP超时时间
     */
    public static final int TIME_OUT = 2 * 60 * 1000;
    /**
     * 是否是私有证书，仅当请求的地址为https时使用
     */
    public static final boolean IS_PRIVATE_CA = false;
    /**
     * 私有证书文件名称
     */
    public static final String PRIVATE_CA_RES = "ca.crt";
    /**
     * 程序application配置
     * */
    public static final Application APPLICATION= App.app;

}
