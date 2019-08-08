package com.example.webstream;

public class Logging {
    String tag ;
    public Logging(String tag){
        this.tag = tag;
    }

    public static void e(String s) {
    }

    public void setLog(String content){
        android.util.Log.e(tag,content);
    }
}
