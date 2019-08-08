package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import java.util.HashMap;
import java.util.Map;

public class ViewStreamActivity extends AppCompatActivity {
    String TAG = "ViewStreamActivity";
    String streamRoute;
    String host;
    String routeStream;
    int number;
    private RequestQueue queue;
    WOWZPlayerView mStreamPlayerView, mStreamPlayerView2;
    //ImageButton btnPlayStop;
    Boolean playerState = true;
    WOWZPlayerConfig mStreamPlayerConfig;
    StatusCallback statusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream);

        //------------------------------변수들 정의----------------------------

        //방송 목록 리싸이클러뷰 선택시 해당 아이템의 title 전달 받음.
        Intent intent = getIntent();
        //와우자 스트림 서버에 접속을 위한 변수
        streamRoute = intent.getStringExtra("title");
        host = intent.getStringExtra("host");
        number = intent.getIntExtra("number",0);
        routeStream = intent.getStringExtra("routeStream");
        /*setLog(streamRoute);
        setLog(host);
        setLog(String.valueOf(number));*/
        setLog("보려고 하는 방송 스트림명 : "+streamRoute);


        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);
        //btnPlayStop = findViewById(R.id.imgBtn_playStop);

        //------------------------------와우자 플레이어 재생을 위한 라이센스 입력----------------------------

        WowzaGoCoder goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-AF46-010C-C23E-E352-51DD");

        //------------------------------와우자 플레이어 재생을 위한 정보 입력----------------------------


        mStreamPlayerConfig = new WOWZPlayerConfig();
        mStreamPlayerConfig.setIsPlayback(true);
        mStreamPlayerConfig.setHostAddress("15.164.121.33");   //와우자 스트리밍 서버 IP
        mStreamPlayerConfig.setApplicationName("live");        //와우자 서버 중 실시간 스트림 폴더선택. VOD와 LIVE 둘 중 선택 가능.
        mStreamPlayerConfig.setStreamName(routeStream);        //방송 제목. 제목으로 방송 방을 구분한다.
        mStreamPlayerConfig.setPortNumber(1935);               //와우자 스트림을 받아오는 포트

        //아이디와 비번을 따로 저장해야 할 듯 한데...... 질문 필요
        mStreamPlayerConfig.setUsername("kkimshin");   //와우자 관리자 아이디디
        mStreamPlayerConfig.setPassword("dhrfks02126263");
        mStreamPlayerConfig.setAudioEnabled(true);
        mStreamPlayerConfig.setVideoEnabled(true);
        mStreamPlayerConfig.setVideoFramerate(10);
        /*mStreamPlayerConfig.set(.FRAME_SIZE_3840x2160);*/


        /*queue = Volley.newRequestQueue(this);
        String url = "http://13.124.223.128/broadcast/searchBroadList.php";

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                String dataResponse = response.replaceAll("\\P{Print}","");
                if(dataResponse.equals("없음")){
                    setLog("비었다.");
                    Toast.makeText(ViewStreamActivity.this, "이미 종료된 방송입니다.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ViewStreamActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                                    }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("host", host);
                return params;
            }
        };

        stringRequest.setTag(TAG);
        queue.add(stringRequest);*/

        statusCallback = new StatusCallback();
        mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);





        /*btnPlayStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playerState == true){
                    btnPlayStop.setImageResource(R.drawable.ic_stop);
                    playerState = false;
                    setLog("정지 : "+playerState);
                    mStreamPlayerView.stop();
                }else {
                    btnPlayStop.setImageResource(R.drawable.ic_play_stream);
                    playerState = true;
                    setLog("재생 : "+playerState);
                    mStreamPlayerView.play(mStreamPlayerConfig, statusCallback);
                }


            }
        });*/

        // WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        /*mStreamPlayerView.setScaleMode(mStreamPlayerConfig.FILL_VIEW);*/

    }

    @Override
    protected void onStop() {
        super.onStop();
        mStreamPlayerView.clear();
    }

    class StatusCallback implements WOWZStatusCallback {
        @Override
        public void onWZStatus(WOWZStatus wzStatus) {
            setLog("허허허 : "+wzStatus);
            setLog("프레임 레이트:"+ mStreamPlayerView.getStreamConfig().getVideoFramerate());
            setLog("프레임 사이즈:"+mStreamPlayerView.getStreamConfig().getVideoFrameSize());
            if(wzStatus.isIdle()){
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ViewStreamActivity.this, "방송이 종료되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                },0);
                finish();
            }

        }
        @Override
        public void onWZError(WOWZStatus wzStatus) {
            setLog("에러에러");
        }
        public void setLog(String content){
            Log.e(TAG,content);
        }
    }


    public void setLog(String content){
        android.util.Log.e(TAG,content);
    }


}
