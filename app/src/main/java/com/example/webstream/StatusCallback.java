package com.example.webstream;

import android.util.Log;

import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

class StatusCallback implements WOWZStatusCallback {
    private static final String TAG = "StatusCallback";
    @Override
    public void onWZStatus(WOWZStatus wzStatus) {
        //setLog("허허허");
    }
    @Override
    public void onWZError(WOWZStatus wzStatus) {
        //setLog("에러에러");
    }
    public void setLog(String content){
        Log.e(TAG,content);
    }
}