package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

public class ViewRecordedActivity extends AppCompatActivity
implements View.OnClickListener {

    String TAG = "ViewRecordedActivity";
    public void setLog(String content){
        android.util.Log.e(TAG,content);
    }

    //Flag모음
    public static final int GET_CHATLIST = 100;     //채팅 내용 가져올 때

    String host ;
    String title ;
    int recordNumber ;
    String routeVideo ;
    String routeVideoName;
    String routeThumbnail ;


    VideoView videoView ;

    ImageView imageChatState;               //채팅창 Gone/Visible 해주는 이미지버튼
    boolean booleanChatState = true;       //채팅창이 Gone/Visible 인지 true/false로 구분해주는 boolean값. 최초 실행 시 채팅창은 Visible 상태이기때문에 true.
    RecyclerView recyclerViewChatList;      //채팅 내용들이 보여질 recyclerView

    HttpConnection httpConn = HttpConnection.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recorded);

        Intent intent = getIntent();
        host = intent.getStringExtra("host");
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

        //http://15.164.121.33/sinhwan0211_15.mp4
        //routeVideo = "http://15.164.215.245/content/admin.mp4";
        routeThumbnail = intent.getStringExtra("routeThumbnail");

        videoView = findViewById(R.id.videoView);
        MediaController controller = new MediaController(this);
        controller.setVisibility(View.VISIBLE);
        videoView.setMediaController(controller);
        videoView.setVideoPath(routeVideo);
        videoView.start();

        //채팅창 Gone/Visible 이미지 버튼
        imageChatState = findViewById(R.id.activity_view_recorded_imageview_chatstate);
        imageChatState.setOnClickListener(this);

        //채팅창 리사이클러뷰 정의
        recyclerViewChatList = findViewById(R.id.activity_view_recorded_recyclerview_chatlist);

        sendData(GET_CHATLIST);


        /*setLog("host : "+host);
        setLog("title : "+title);
        setLog("recordNumber : "+recordNumber);
        setLog("routeVideo : "+routeVideo);
        setLog("routeThumbnail : "+routeThumbnail);*/

    }

    private void sendData(int flag) {
        switch (flag){
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
                setLog("제이슨 시작");

                JSONObject getUserData = new JSONObject(body);
                String dummyData = getUserData.getString("recordedChatList");
                setLog("111 : "+dummyData);
                JSONArray getUserDataArray = new JSONArray(dummyData);
                JSONObject getUserData2 = getUserDataArray.getJSONObject(0);
                setLog("222 : "+String.valueOf(getUserData2));
                String getNickname = getUserData2.getString("nickname");
                String content = getUserData2.getString("content");
                String chatTime = getUserData2.getString("chatTime");



                setLog("유저아이디 : "+getNickname);
                setLog("유저닉네임 : "+content);
                setLog("유저닉네임 : "+chatTime);

//                Message messageId = handler.obtainMessage();
//                messageId.what = CHANGE_USERID;
//                messageId.obj = getId;
//                handler.sendMessage(messageId);
//
//                Message messageNickname = handler.obtainMessage();
//                messageNickname.what = CHANGE_USERNICKNAME;
//                messageNickname.obj = getNickname;
//                handler.sendMessage(messageNickname);
//
//                Message messageProfile = handler.obtainMessage();
//                messageProfile.what = CHANGE_PROFILE;
//                messageProfile.obj = getProfile;
//                handler.sendMessage(messageProfile);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    };
}
