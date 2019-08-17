package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.Adapter_chatList_viewRecorded;
import dataList.DataList_chatList_recorded;
import okhttp3.Call;
import okhttp3.Callback;

public class ViewRecordedActivity extends AppCompatActivity
implements View.OnClickListener {

    String TAG = "ViewRecordedActivity";
    public void setLog(String content){
        android.util.Log.v(TAG,content);
    }

    //Flag모음
    public static final int GET_CHATLIST = 100;     //채팅 내용 가져올 때
    public static final int GET_CHAT_CONTENT = 101;


    String host ;
    String hostNickname ;
    String title ;
    int recordNumber ;
    String routeVideo ;
    String routeVideoName;
    //String routeThumbnail ;               //해당 녹화방송 썸네일 경로

    ProgressDialog progressDialog;          //녹화 방송 로딩 중  출력되는 progressDialog


    VideoView videoView ;                   //녹화방송이 재생되는 VideoView

    ImageView imageChatState;               //채팅창 Gone/Visible 해주는 이미지버튼
    boolean booleanChatState = true;        //채팅창이 Gone/Visible 인지 true/false로 구분해주는 boolean값. 최초 실행 시 채팅창은 Visible 상태이기때문에 true.
    RecyclerView recyclerViewChatList;      //채팅 내용들이 보여질 recyclerView
    ArrayList<DataList_chatList_recorded> dataList_chatList_recordeds;              //채팅 정보들을 recyclerview에 출력시킬 데이터리스트
    Adapter_chatList_viewRecorded adapterChatListViewRecorded;                      //채팅 리스트를 출력 시켜줄 adapter
    ArrayList<DataList_chatList_recorded> dataList_chatList_recordeds_getter;       //채팅 정보들을 담을 데이터리스트

    HttpConnection httpConn = HttpConnection.getInstance();                         //okhttp쓰기위한 객체
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recorded);

        //인테트로 선택한 녹화방송 정보 받아오기
        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        hostNickname = intent.getStringExtra("hostNickname");
        title = intent.getStringExtra("title");
        recordNumber = intent.getIntExtra("RecordNumber",0);
        routeVideo = intent.getStringExtra("routeVideo");

        //routeStreamName 알아내기 위한 split 작업
        //setLog("루트 비디오 : "+routeVideo);
        String[] split ;
        split = routeVideo.split("http://15.164.121.33/");
        String[] split2;
        split2 = split[1].split(".mp4");
        routeVideoName = split2[0];
        //setLog("루트 비디오 네임 : "+routeVideoName);

        //routeVideo = "http://15.164.215.245/content/admin.mp4";
        //routeThumbnail = intent.getStringExtra("routeThumbnail");

        //프로그레스 다이얼로그 출력
        progressDialog = new ProgressDialog(ViewRecordedActivity.this);
        progressDialog.setMessage("동영상 로딩 중...");
        progressDialog.show();

        //비디오 셋팅.
        videoView = findViewById(R.id.videoView);
        final MediaController controller = new MediaController(this);
        controller.setVisibility(View.VISIBLE);
        //controller.findViewById(R.id.activity_view_recorded_controller);
        videoView.setMediaController(controller);
        videoView.setVideoPath(routeVideo);
        controller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLog("영상 포지션 : "+videoView.getCurrentPosition());
            }
        });



        //int thth = getResources().getIdentifier("mediacontroller_progress", "id", "android");
        //SeekBar seekBar = controller.findViewById(thth);
//        SeekBar seekBar = null;
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                setLog("addad");
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                setLog("bbb");
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                setLog("cccc");
//            }
//        });

        //동영상 재생 준비 완료 감지.
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                setLog("준비완료");
                videoView.start();
                setLog("영상 길이 : "+videoView.getDuration());


                //영상 준비가 완료되고 재생까지 시간이 조금 걸려 sleep으로 채팅 출력 쓰레드 지연
                Thread playThread = new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            Thread.sleep(3200);
                            progressDialog.dismiss();
                            chattingThread.start();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                playThread.start();

            }
        });


        setLog("비디오 시작");

        //채팅창 Gone/Visible 이미지 버튼
        imageChatState = findViewById(R.id.activity_view_recorded_imageview_chatstate);
        imageChatState.setOnClickListener(this);

        //채팅창 리사이클러뷰 정의
        recyclerViewChatList = findViewById(R.id.activity_view_recorded_recyclerview_chatlist);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(ViewRecordedActivity.this));
        dataList_chatList_recordeds = new ArrayList<>();

        setLog("어댑터에게 주는 호스트 닉 : "+hostNickname);
        adapterChatListViewRecorded = new Adapter_chatList_viewRecorded(ViewRecordedActivity.this,dataList_chatList_recordeds,hostNickname);
        recyclerViewChatList.setAdapter(adapterChatListViewRecorded);

        dataList_chatList_recordeds_getter = new ArrayList<>();

        //선택한 영상의 모든 채팅 정보 가져오기
        sendData(GET_CHATLIST);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    //채팅 내용 출력.
                    case GET_CHAT_CONTENT:
                        adapterChatListViewRecorded.notifyItemChanged(dataList_chatList_recordeds.size());
                        recyclerViewChatList.scrollToPosition(dataList_chatList_recordeds.size()-1);
                        break;
                }
            }
        };


        /*setLog("host : "+host);
        setLog("title : "+title);
        setLog("recordNumber : "+recordNumber);
        setLog("routeVideo : "+routeVideo);
        setLog("routeThumbnail : "+routeThumbnail);*/


    }

    private void sendData(int flag) {
        switch (flag){
            //채팅 데이터 가져오기
            case GET_CHATLIST:
                new Thread() {
                    public void run() {
                        String url = "http://13.124.223.128/recording/getRecordedChatList.php";
                        httpConn.requestGetChatRecorded(routeVideoName, callback, url);
                    }
                }.start();
                break;
        }


    }




    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //ImageChatState 클릭
            case R.id.activity_view_recorded_imageview_chatstate:
                setLog("채팅창 온/오프 버튼 클릭함.");
                //채팅창이 Visible 상태일 때 채팅창을 Gone(안보이게)함
                if (booleanChatState == true) {
                    recyclerViewChatList.setVisibility(View.GONE);
                    booleanChatState = false;
                    setLog("채팅창 오프");
                }
                //채팅창이 Gone 상태일 때 채팅창을 Visible(보이게)함
                else if(booleanChatState == false){
                    recyclerViewChatList.setVisibility(View.VISIBLE);
                    booleanChatState = true;
                    setLog("채팅창 온");
                }
                break;
        }
    }

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body:"+body);
            try {
                //채팅 내용 파싱
                setLog("제이슨 시작");
                JSONObject getUserData = new JSONObject(body);
                String dummyData = getUserData.getString("recordedChatList");
                setLog("111 : "+dummyData);
                JSONArray getUserDataArray = new JSONArray(dummyData);

                for(int i =0; i<getUserDataArray.length(); i++){
                    JSONObject getUserData2 = getUserDataArray.getJSONObject(i);
                    setLog("222 : "+String.valueOf(getUserData2));
                    String getNickname = getUserData2.getString("nickname");
                    String getContent = getUserData2.getString("content");
                    String getChatTime = getUserData2.getString("chatTime");
                    DataList_chatList_recorded dataSetter = new DataList_chatList_recorded();

                    dataSetter.setChatTime(Integer.parseInt(getChatTime));
                    dataSetter.setNickname(getNickname);
                    dataSetter.setContent(getContent);
                    //1차적으로 전체 채팅 내용을 ArrayList에 담아 놓는다.
                    dataList_chatList_recordeds_getter.add(dataSetter);

                    setLog("유저아이디 "+i+"번쨰 : "+getNickname);
                    setLog("유저닉네임 "+i+"번째 : "+getContent);
                    setLog("유저닉네임 "+i+"번째 : "+getChatTime);
                }

                //setLog("어레이 리스트 : "+dataList_chatList_recordeds_getter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    //채팅 출력 쓰레드
    Thread chattingThread = new Thread(){
        @Override
        public void run() {
            for(int i =1;i<dataList_chatList_recordeds_getter.size();i++){

                DataList_chatList_recorded dataGetter = dataList_chatList_recordeds_getter.get(i);
                DataList_chatList_recorded dataGetterPre = dataList_chatList_recordeds_getter.get(i-1);
                try {
                    //이전 채팅내용 출력된 시간과의 차를 Sleep인자값으로 주어 실시간 방송과 동일하게 출력.
                    Thread.sleep(dataGetter.getChatTime()-dataGetterPre.getChatTime());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                setLog("닉네임 "+i+"번째 : "+dataGetter.getNickname());
                setLog("내용 "+i+"번째 : "+dataGetter.getContent());
                //전체 채팅내용을 담아 놓았던 ArrayList에서 i번째 채팅을 꺼내어 recyclerview에 출력한다.
                dataList_chatList_recordeds.add(dataList_chatList_recordeds_getter.get(i));
                handler.sendEmptyMessage(GET_CHAT_CONTENT);

            }
            super.run();
        }
    };
}
