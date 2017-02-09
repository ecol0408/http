package com.ecol.http;

import android.os.Message;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by YI on 2016/2/23.
 * 网络请求
 */
final class BaseHttpRequest extends Thread {
    /**
     * 请求地址
     */
    String requestUrl;
    /**
     * 请求参数
     */
    Object params;
    /**
     * 请求类型   默认POST
     */
    String requestType = "POST";
    /**
     * 是否加密处理
     */
    boolean encrypt = true;

    /**
     * 回调处理
     */
    BaseHttpHandler callBack;
    /**
     * 第几次请求
     */
    int which;
    /**
     * 附加参数（供回调时使用）
     */
    Map<String, Object> attachParams;
    /**
     * 请求标识（请求地址+第几次请求）
     */
    private String requestTag;
    /**
     * 请求状态
     */
    private boolean requesting;
    private int timeOut = BaseHttpConstant.TIME_OUT;

    public void setRequesting(boolean requesting) {
        this.requesting = requesting;
    }


    BaseHttpRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which, Map<String, Object> attachParams, String requestType, boolean encrypt) {
        this.requestUrl = requestUrl;
        this.params = params;
        this.requestType = requestType;
        this.encrypt = encrypt;
        this.callBack = callBack;
        this.which = which;
        this.attachParams = attachParams;
        this.requestTag = requestUrl + which;
    }

    BaseHttpRequest(String requestUrl, Object params, BaseHttpHandler callBack, int which, Map<String, Object> attachParams, String requestType, boolean encrypt, int timeOut) {
        this(requestUrl, params, callBack, which, attachParams, requestType, encrypt);
        if (timeOut < 1000) {
            timeOut = 1000;
        }
        this.timeOut = timeOut;
    }

    @Override
    public void run() {
        super.run();
        requesting = true;
        if (requestType == null || requestType.isEmpty()) {
            requestType = "POST";
        }
        Logger.log("requesturl:" + requestUrl);
        Logger.log("requestParams:" + params);
        try {
            post();
        } catch (Exception e) {
            e.printStackTrace();
        }
        requesting = false;
        BaseHttpConnectPool.getInstance().removeRequest(requestTag);
    }

    private final void post() {
        Message message = Message.obtain();
        HttpResponseModel model = new HttpResponseModel(requestUrl, null, which, attachParams);
        message.obj = model;
        try {
            byte[] reqParams = objToByteArray(params);
            if (reqParams != null && encrypt) {
                reqParams = Base64.encodeBase64(reqParams);
            }
            URL url = new URL(requestUrl);
            URLConnection connection = url.openConnection();
            sslInit(connection);
            ((HttpURLConnection) connection).setRequestMethod(requestType);
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
            connection.setDoInput(true);
            connection.connect();
            if (reqParams != null) {
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(reqParams);
                outputStream.flush();
                outputStream.close();
            }
            if (connection != null) {
                Logger.log("responseCode:" + ((HttpURLConnection) connection).getResponseCode());
                if (((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    byte[] data;
                    if (encrypt) {
                        data = Base64.decodeBase64(IOUtils.toByteArray(inputStream));//BASE64解密
                    } else {
                        data = IOUtils.toByteArray(inputStream);
                    }
                    if (requesting && callBack != null) {
                        message.what = HttpResponseMsgType.RESPONSE_SUCCESS;
                        model.setResponse(data);
                        callBack.sendMessage(message);
                    }
                } else {
                    //错误的返回
                    if (requesting && callBack != null) {
                        message.what = HttpResponseMsgType.RESPONSE_ERR;
                        model.setResponse("Service Inner Err".getBytes());
                        callBack.sendMessage(message);
                    }
                }
                ((HttpURLConnection) connection).disconnect();
            }
        } catch (Exception e) {
            if (callBack != null) {
                message.what = HttpResponseMsgType.RESPONSE_ERR;
                model.setResponse("网络连接失败，请检查网络或主收银机是否开启".getBytes());
                callBack.sendMessage(message);
            }
        }
    }

    /**
     * Object转化为byte[]
     */
    @SuppressWarnings("unchecked")
    private static final byte[] objToByteArray(Object params) {
        try {
            if (params == null) {
                return null;
            } else if (params instanceof JSONObject) {
                return params.toString().getBytes();
            } else if (params instanceof JSONArray) {
                return params.toString().getBytes();
            } else if (params instanceof String) {
                return params.toString().getBytes();
            } else if (params instanceof Map) {
                JSONObject object = new JSONObject();
                Set<Map.Entry<Object, Object>> set = ((Map<Object, Object>) params).entrySet();
                for (Map.Entry<Object, Object> entry : set) {
                    Object key = entry.getKey(), value = entry.getValue();
                    if (key != null) {
                        if (value == null) {
                            object.put(key.toString(), "");
                        } else {
                            object.put(key.toString(), value);
                        }
                    }
                }
                return object.toString().getBytes();
            } else if (params instanceof byte[]) {
                return (byte[]) params;
            } else {
                return params.toString().getBytes();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 私有证书SSL初始化
     */
    private final void sslInit(URLConnection connection) {
        if (connection == null || requestUrl == null
                || !requestUrl.startsWith("https")
                || !BaseHttpConstant.IS_PRIVATE_CA
                || BaseHttpConstant.PRIVATE_CA_RES == null)
            return;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = BaseHttpConstant.APPLICATION.getAssets().open(BaseHttpConstant.PRIVATE_CA_RES);
            Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
            } finally {
                caInput.close();
            }
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
