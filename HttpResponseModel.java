package com.ecol.http;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by YI on 2016/2/23.
 * 请求返回的序列化结果对象
 */
public final class HttpResponseModel implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 请求的地址
     */
    private String requestUrl;
    /**
     * 返回的二进制数据
     */
    private byte[] response;
    /**
     * 第几次请求
     */
    private int which;
    /**
     * 请求附带的参数（供回调函数使用）
     */
    private Map<String, Object> attachParams;

    public HttpResponseModel(String requestUrl, byte[] response) {
        super();
        this.requestUrl = requestUrl;
        this.response = response;
    }

    public HttpResponseModel(String requestUrl, byte[] response, int which) {
        super();
        this.requestUrl = requestUrl;
        this.response = response;
        this.which = which;
    }

    public HttpResponseModel(String requestUrl, byte[] response, int which, Map<String, Object> attachParams) {
        super();
        this.requestUrl = requestUrl;
        this.response = response;
        this.which = which;
        this.attachParams = attachParams;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public byte[] getResponse() {
        return response;
    }

    public int getWhich() {
        return which;
    }

    public Map<String, Object> getAttachParams() {
        return attachParams;
    }

    public void setResponse(byte[] response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "HttpResponseModel{" +
                "requestUrl='" + requestUrl + '\'' +
                ", response=" + Arrays.toString(response) +
                ", which=" + which +
                ", attachParams=" + attachParams +
                '}';
    }
}
