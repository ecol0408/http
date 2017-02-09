package com.ecol.http;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by YI on 2016/2/23.
 * 网络请求链接池
 */
public final class BaseHttpConnectPool {
    /**
     * 获取对象
     */
    public static BaseHttpConnectPool getInstance() {
        return Hcp.httpConnectionPool;
    }

    private BaseHttpConnectPool() {
    }

    private static class Hcp {
        static BaseHttpConnectPool httpConnectionPool = new BaseHttpConnectPool();
    }

    Object object = new Object();

    /**
     * 当前的请求连接
     */
    private static Map<String, BaseHttpRequest> httpRequests = new ConcurrentHashMap<String, BaseHttpRequest>();


    /**
     * 移除所有请求
     */
    public final void removeAllRequest() {
        try {
            Set<Map.Entry<String, BaseHttpRequest>> set = httpRequests.entrySet();
            for (Map.Entry<String, BaseHttpRequest> entry : set) {
                entry.getValue().setRequesting(false);
            }
            httpRequests.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除完结的请求
     */
    private final void removeDieRequest() {
        try {
            Set<Map.Entry<String, BaseHttpRequest>> set = httpRequests.entrySet();
            for (Map.Entry<String, BaseHttpRequest> entry : set) {
                if (!entry.getValue().isAlive()) {
                    httpRequests.remove(entry.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除一个请求
     */
    public final void removeRequest(String requestTag) {
        if (requestTag == null) return;
        try {
            if (httpRequests.containsKey(requestTag)) {
                httpRequests.remove(requestTag).setRequesting(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final String initRequestTag(String requestUrl, int which) {
        Logger.log("requestTime:" + System.currentTimeMillis());
        removeDieRequest();
        if (httpRequests.size() > BaseHttpConstant.HTTP_MAX_REQUESTS) {
            Logger.log("requestNotAllow  Limit Max Request!");
            return null;
        }
        String requestTag = requestUrl;
        if (which != -1) requestTag += which;
        if (httpRequests.containsKey(requestTag)) {
            Logger.log("requestNotAllow  The Same Limit!");
            return null;
        }
        return requestTag;
    }


    /**
     * 加入一个请求-----对于本地系统作BASE64加密处理
     *
     * @param requestUrl   请求的地址
     * @param params       请求参数
     * @param callBack     回调函数
     * @param which        第几次请求
     * @param attachParams 附加参数（供回调时使用）
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which, Map<String, Object> attachParams) {
        synchronized (object) {
            String requestTag = initRequestTag(requestUrl, which);
            if (requestTag == null) return;
            BaseHttpRequest baseHttpRequest = new BaseHttpRequest(requestUrl, params, callBack, which, attachParams, null, true);
            httpRequests.put(requestTag, baseHttpRequest);
            baseHttpRequest.start();
        }
    }

    /**
     * 加入一个请求--对于第三方网络的请求使用原始请求
     *
     * @param requestUrl   请求的地址
     * @param params       请求参数
     * @param callBack     回调函数
     * @param which        第几次请求
     * @param attachParams 附加参数（供回调时使用）
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which, Map<String, Object> attachParams, String requestType) {
        synchronized (object) {
            String requestTag = initRequestTag(requestUrl, which);
            if (requestTag == null) return;
            BaseHttpRequest baseHttpRequest = new BaseHttpRequest(requestUrl, params, callBack, which, attachParams, requestType, false);
            httpRequests.put(requestTag, baseHttpRequest);
            baseHttpRequest.start();
        }
    }

    /**
     * 加入一个请求--对于第三方网络的请求使用原始请求
     *
     * @param requestUrl   请求的地址
     * @param params       请求参数
     * @param callBack     回调函数
     * @param which        第几次请求
     * @param attachParams 附加参数（供回调时使用）
     * @param encrypt      是否采用base64
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which, Map<String, Object> attachParams, boolean encrypt) {
        addRequest(requestUrl, params, callBack, which, attachParams, encrypt, BaseHttpConstant.TIME_OUT);
    }

    /**
     * 加入一个请求--对于第三方网络的请求使用原始请求
     *
     * @param requestUrl   请求的地址
     * @param params       请求参数
     * @param callBack     回调函数
     * @param which        第几次请求
     * @param attachParams 附加参数（供回调时使用）
     * @param encrypt      是否采用base64
     * @param timeOut      超时时间
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which, Map<String, Object> attachParams, boolean encrypt, int timeOut) {
        synchronized (object) {
            String requestTag = initRequestTag(requestUrl, which);
            if (requestTag == null) return;
            BaseHttpRequest baseHttpRequest = new BaseHttpRequest(requestUrl, params, callBack, which, attachParams, null, encrypt, timeOut);
            httpRequests.put(requestTag, baseHttpRequest);
            baseHttpRequest.start();
        }
    }


    /**
     * 加入一个请求
     *
     * @param requestUrl 请求的地址
     * @param params     请求参数
     * @param callBack   回调函数
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which) {
        addRequest(requestUrl, params, callBack, which, null);
    }

    /**
     * 加入一个请求
     *
     * @param requestUrl 请求的地址
     * @param params     请求参数
     * @param callBack   回调函数
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack) {
        addRequest(requestUrl, params, callBack, -1, null);
    }

    /**
     * 加入一个请求
     *
     * @param requestUrl 请求的地址
     * @param params     请求参数
     * @param callBack   回调函数
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, Map<String, Object> attachParams) {
        addRequest(requestUrl, params, callBack, -1, attachParams);
    }

    /**
     * 加入一个请求有请求类型
     *
     * @param requestUrl  请求的地址
     * @param params      请求参数
     * @param callBack    回调函数
     * @param requestType 请求类型
     */
    public synchronized void addRequest(String requestUrl, Object params, BaseHttpHandler callBack, String requestType) {
        addRequest(requestUrl, params, callBack, -1, null, requestType);
    }
}
