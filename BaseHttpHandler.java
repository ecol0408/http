package com.ecol.http;

import android.os.Handler;
import android.os.Message;

/**
 * Created by YI on 2016/2/23.
 * 网络请求回调
 */
public final class BaseHttpHandler extends Handler {
    HttpHandler httpHandler;

    public BaseHttpHandler(HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public final void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg == null)
            return;
        if (httpHandler == null)
            return;
        int what = msg.what;
        switch (what) {
            case HttpResponseMsgType.RESPONSE_ERR:
                try {
                    httpHandler.httpErr((HttpResponseModel) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.log(e.toString());
                }
                break;
            case HttpResponseMsgType.RESPONSE_SUCCESS:
                try {
                    httpHandler.httpSuccess((HttpResponseModel) msg.obj);
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.log(e.toString());
                }
                break;
        }
    }
}
