package com.example.webstream;

import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpConnection {

    private OkHttpClient client;
    private static HttpConnection instance = new HttpConnection();
    public static HttpConnection getInstance() {
        return instance;
    }

    private HttpConnection(){ this.client = new OkHttpClient(); }


    /** 웹 서버로 요청을 한다. */
    public void requestWebServer(String parameter,  Callback callback,String url) {
        RequestBody body = new FormBody.Builder()
                .add("parameter", parameter)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
    public void requestCheckId(String parameter,Callback callback,String url) {
        RequestBody body = new FormBody.Builder()
                .add("InputId", parameter)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestCheckNickname(String parameter,Callback callback,String url) {
        RequestBody body = new FormBody.Builder()
                .add("InputNickname", parameter)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestCheckPassword(String parameter,Callback callback,String url) {
        RequestBody body = new FormBody.Builder()
                .add("InputPassword", parameter)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestSignup(String parameter,String parameter2,String parameter3,String parameter4,Callback callback,String url){
        RequestBody body = new FormBody.Builder()
                .add("InputId", parameter)
                .add("InputPassword", parameter2)
                .add("InputNickname", parameter3)
                .add("InputEmail", parameter4)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestGetUserData(String parameter,Callback callback,String url){
        RequestBody body = new FormBody.Builder()
                .add("InputId", parameter)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestLogin(String parameter, String parameter2, Callback callback,String url){
        RequestBody body = new FormBody.Builder()
                .add("InputId", parameter)
                .add("InputPassword", parameter2)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestChaneNickname(String parameter,String parameter2, Callback callback,String url) {
        RequestBody body = new FormBody.Builder()
                .add("id", parameter)
                .add("InputNickname", parameter2)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestChanePassword(String id,String existingPassword, String newPassword,String newPasswordReconfirm, Callback callback,String url) {
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("existingPassword", existingPassword)
                .add("newPassword",newPassword)
                .add("newPasswordReconfirm",newPasswordReconfirm)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestChangeProfile(String parameter,Callback callback,String url){
        RequestBody body = new FormBody.Builder()
                .add("InputId", parameter)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public void requestChangeAlbumProfile(String id,String profileRoute,Callback callback,String url){
        RequestBody body = new FormBody.Builder()
                .add("id", id)
                .add("profileRoute", profileRoute)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
