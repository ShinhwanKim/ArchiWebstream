package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import adapter.Adapter_chatList;
import adapter.Adapter_chatList_viewStream;
import dataList.DataList_chatList_broadcast;
import dataList.DataList_chatList_viewStream;
import okhttp3.Call;
import okhttp3.Callback;

public class ViewStreamActivity extends AppCompatActivity
implements View.OnClickListener {

    public static final int GET_CHAT_CONTENT = 100;
    public static final int SEND_CHAT_CONTENT = 200;

    public static final int PARTICIPATE_VIEWER = 300;
    public static final int EXIT_VIEWER = 301;

    String TAG = "ViewStreamActivity";
    String title;
    String host;
    String routeStream;
    String loginedUser;
    int viewer;
    int number;
    private RequestQueue queue;
    WOWZPlayerView mStreamPlayerView, mStreamPlayerView2;
    //ImageButton btnPlayStop;
    Boolean playerState = true;
    WOWZPlayerConfig mStreamPlayerConfig;
    StatusCallback statusCallback;

    //------------------------------chat set변수들 ----------------------------
    private LinearLayout chatSet ;
    private EditText etxtChatContent;
    private TextView txtSendContent;

    //------------------------------title set변수들 ----------------------------
    private LinearLayout titleSet ;
    private TextView txtTitle;
    private TextView txtHost;
    private TextView txtViewer;
    private ImageView imgChatState;

    //------------------------------title set변수들 ----------------------------
    private boolean blnOptionState;
    private boolean blnChatState;

    private RecyclerView recyChatList;
    Guideline guideline;
    private Socket m_Socket;
    PrintWriter sendWriter = null;
    BufferedReader tmpbuf;
    String chatContent;
    String hostNickname;

    private HttpConnection httpConn = HttpConnection.getInstance();

    private Handler handler;

    private ArrayList<DataList_chatList_viewStream> dataList_chatListViewStream;    //채팅 내용을 보여주는 리사이클러뷰에 들어갈 데이터 리스트
    private Adapter_chatList_viewStream adapter_chatListViewStream;                  //채팅 내용을 보여주는 리사이클러뷰에 적용될 어댑터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream);

        //------------------------------변수들 정의----------------------------

        //방송 목록 리싸이클러뷰 선택시 해당 아이템의 title 전달 받음.
        Intent intent = getIntent();
        //와우자 스트림 서버에 접속을 위한 변수
        title = intent.getStringExtra("title");
        host = intent.getStringExtra("host");
        number = intent.getIntExtra("number",0);
        viewer = intent.getIntExtra("viewer",0);
        routeStream = intent.getStringExtra("routeStream");
        loginedUser = intent.getStringExtra("loginedUser");
        hostNickname = intent.getStringExtra("hostNickname");

        /*setLog(title);
        setLog(host);
        setLog(String.valueOf(number));*/
        setLog("보려고 하는 방송 스트림명 : "+ title);
        guideline = findViewById(R.id.guideline6);


        mStreamPlayerView = (WOWZPlayerView) findViewById(R.id.vwStreamPlayer);
        //btnPlayStop = findViewById(R.id.imgBtn_playStop);

        chatSet = findViewById(R.id.activity_viewstrean_chatset);
        etxtChatContent = findViewById(R.id.activity_viewstrean__edittext_chatcontent);
        txtSendContent = findViewById(R.id.activity_viewstrean_text_sendchat);

        titleSet = findViewById(R.id.activity_viewstrean_titleset);
        txtTitle = findViewById(R.id.activity_viewstrean_text_title);
        txtHost = findViewById(R.id.activity_viewstrean_text_host);
        txtViewer = findViewById(R.id.activity_viewstrean_text_viewer);
        imgChatState = findViewById(R.id.activity_viewstrean__chatstate);

        recyChatList = findViewById(R.id.activity_viewstrean_recyclerview_chatlist);

        mStreamPlayerView.setOnClickListener(this);

        imgChatState.setOnClickListener(this);

        //------------------------------방송 정보 titleSet에 출력----------------------------

        txtTitle.setText(title);
        txtHost.setText(hostNickname);
        txtViewer.setText(String.valueOf(viewer));



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

        blnOptionState = true;
        blnChatState = true;

        recyChatList = findViewById(R.id.activity_viewstrean_recyclerview_chatlist);
        recyChatList.setLayoutManager(new LinearLayoutManager(ViewStreamActivity.this));
        dataList_chatListViewStream = new ArrayList<>();

        adapter_chatListViewStream = new Adapter_chatList_viewStream(ViewStreamActivity.this, dataList_chatListViewStream, hostNickname, loginedUser);
        recyChatList.setAdapter(adapter_chatListViewStream);

        SocketConnect();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case GET_CHAT_CONTENT:
                        //adapter_chatList.notifyDataSetChanged();
                        //리사이클러뷰를 통째로 재 배열 하니 글자색이 뒤죽박죽되는 에러 발생
                        adapter_chatListViewStream.notifyItemChanged(dataList_chatListViewStream.size());
                        recyChatList.scrollToPosition(dataList_chatListViewStream.size()-1);
                        break;
                    case SEND_CHAT_CONTENT:
                        //adapter_chatList.notifyDataSetChanged();
                        //리사이클러뷰를 통째로 재 배열 하니 글자색이 뒤죽박죽되는 에러 발생
                        adapter_chatListViewStream.notifyItemChanged(dataList_chatListViewStream.size());
                        recyChatList.scrollToPosition(dataList_chatListViewStream.size()-1);
                        break;
                    case PARTICIPATE_VIEWER:
                        txtViewer.setText(String.valueOf(viewer));
                        //sendData("http://13.124.223.128/broadcast/updateViewer.php");
                        break;
                    case EXIT_VIEWER:
                        txtViewer.setText(String.valueOf(viewer));
                        //sendData("http://13.124.223.128/broadcast/updateViewer.php");
                        break;
                }
            }
        };



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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //플레이어뷰 클릭
            case R.id.vwStreamPlayer :
                if(blnOptionState == true){
                    setLog("옵션뷰 오프 ");
                    titleSet.setVisibility(View.GONE);
                    chatSet.setVisibility(View.GONE);
                    blnOptionState = false;
                }else if(blnOptionState == false){
                    setLog("옵션뷰 온 ");
                    titleSet.setVisibility(View.VISIBLE);
                    chatSet.setVisibility(View.VISIBLE);
                    blnOptionState = true;
                }else {
                    setLog("옵션뷰 온/오프 에러??");
                }
                break;
                //채팅 온/오프 버튼
            case R.id.activity_viewstrean__chatstate :
                // 채팅창 visible상태 -> Gone
                if(blnChatState == true){
                    setLog("채팅 리사이클러뷰 오프");
                    recyChatList.setVisibility(View.GONE);
                    imgChatState.setImageResource(R.drawable.ic_speaker_notes_black_24dp);
                    blnChatState = false;
                    guideline.setGuidelinePercent((float) 0.94);

                }
                // 채팅창 Gone상태 -> visible
                else if(blnChatState == false){
                    setLog("채팅 리사이클러뷰 온");
                    recyChatList.setVisibility(View.VISIBLE);
                    imgChatState.setImageResource(R.drawable.ic_speaker_notes_off_black_24dp);
                    blnChatState = true;
                    guideline.setGuidelinePercent((float) 0.5);
                }else {
                    setLog("채팅 리사이클러뷰 온/오프 에러");
                }
                break;
                //채팅 메세지 전송 버튼
            case R.id.activity_viewstrean_text_sendchat :
                break;
        }
    }
    private void sendData(final String url) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                httpConn.requestUpdateViewer(routeStream, viewer, callback, url);
            }
        }.start();

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


        }
    };

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
    public class ReceiveThread extends Thread{
        private Socket m_socket;

        public ReceiveThread(Socket socket){
            this.m_socket = socket;
        }

        @Override
        public void run() {
            super.run();

            try {
                BufferedReader tmpbuf= new BufferedReader(new InputStreamReader(m_socket.getInputStream(),"EUC-KR"));

                String receiveString;
                String[] spliter;
                String[] spliterParticipate;
                String[] spliterExit;

                while(true) {
                    receiveString = tmpbuf.readLine();

                    if(receiveString == null){
                        break;
                    }

                    spliter = receiveString.split(">");
                    //if(spliter.length >=2 && spliter[0].equals(userId))
                    setLog("현재 내 닉네임 : "+loginedUser);
                    if(spliter.length >=2 && !spliter[0].equals(loginedUser)) {


                        String nick = spliter[0];
                        String content = spliter[1];
                        setLog("채팅 받아옴 닉 : "+nick);
                        setLog("채팅 받아옴 내용 : "+content);
                        setLog("현재 내 닉네임 : "+loginedUser);
                        setLog(receiveString);

                        DataList_chatList_viewStream dataChat = new DataList_chatList_viewStream();
                        dataChat.setNickname(nick);
                        dataChat.setContent(content);
                        dataList_chatListViewStream.add(dataChat);
                        handler.sendEmptyMessage(GET_CHAT_CONTENT);





                        continue;
                    }
                    spliterParticipate = receiveString.split("<<<");
                    if(spliterParticipate.length == 2 && spliterParticipate[1].equals("참가")){
                        viewer++;
                        handler.sendEmptyMessage(PARTICIPATE_VIEWER);
                    }
                    spliterExit = receiveString.split("//////");
                    if(spliterExit.length == 2 && spliterExit[1].equals("나감")){
                        viewer--;
                        handler.sendEmptyMessage(EXIT_VIEWER);
                    }
                    setLog(receiveString);
                    //txtContent.setText(receiveString);
                    //System.out.println(receiveString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void setSocket(Socket _socket) {
            m_socket = _socket;
        }
    }

    public class SendThread extends Thread {

        //private Socket m_Socket;

        public SendThread(Socket socket) {
            m_Socket = socket;
        }

        @Override
        public void run() {
            super.run();

            try {
                tmpbuf = new BufferedReader(new InputStreamReader(System.in));

                sendWriter = new PrintWriter(new OutputStreamWriter(m_Socket.getOutputStream(), StandardCharsets.UTF_8), true);

                final String sendString;


                setLog("사용할 ID를 입력해주세요 : ");
                System.out.println("사용할 ID를 입력해주세요 : ");

                sendWriter.println("IDhighkrs12345" + loginedUser +"highkrs12345" +routeStream);
                sendWriter.flush();



                txtSendContent.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        setLog("전송 버튼 클릭");
                        chatContent = etxtChatContent.getText().toString();
                        if(chatContent.equals("")){

                        }else {
                            DataList_chatList_viewStream dataChat = new DataList_chatList_viewStream();
                            dataChat.setNickname(loginedUser);
                            dataChat.setContent(chatContent);
                            dataList_chatListViewStream.add(dataChat);
                            handler.sendEmptyMessage(SEND_CHAT_CONTENT);

                            TaskSendChat taskSendChat = new TaskSendChat();
                            taskSendChat.execute();
//                                sendWriter.println(chatContent);
//                                sendWriter.flush();

//                                Thread sendThread = new Thread() {
//                                    @Override
//                                    public void run() {
//                                        super.run();
//                                        sendWriter.println(chatContent);
////                                try {
////                                    //sendWriter.println(contentChat.getBytes("UTF-8"));
////
////                                } catch (UnsupportedEncodingException e) {
////                                    e.printStackTrace();
////                                }
//                                        sendWriter.flush();
//                                    }
//                                };
//                                sendThread.start();
                            etxtChatContent.setText("");
                        }
                    }
                });
//                btnExit.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        setLog("나가기");
//                        sendWriter.close();
//                        try {
//                            tmpbuf.close();
//                            m_Socket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                });

//                while(true) {
//                    sendString = tmpbuf.readLine();
//                    btnSend.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            sendString = content.getText().toString();
//                        }
//                    });
//                    if(sendString.equals("exit")) {
//                        break;
//                    }
//
//                    sendWriter.println(sendString);
//                    sendWriter.flush();
//                }

//                sendWriter.close();
//                tmpbuf.close();
//                m_Socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setSocket(Socket _socket) {
            m_Socket = _socket;
        }
    }
    public void SocketConnect(){
        final Socket c_socket = new Socket();
        Thread connectSocket = new Thread(){
            @Override
            public void run() {
                super.run();
                setLog("2");
                //Socket c_socket = new Socket("192.168.0.1",8888);

                SocketAddress addr = new InetSocketAddress("192.168.0.221",8888);
                try {
                    c_socket.connect(addr);

                    setLog("3");
                    ReceiveThread rec_thread = new ReceiveThread(c_socket);
                    setLog("4");
                    //rec_thread.setSocket(c_socket);
                    setLog("5");

                    SendThread send_thread = new SendThread(c_socket);
                    setLog("6");
                    //send_thread.setSocket(c_socket);
                    setLog("7");

                    send_thread.start();
                    setLog("8");
                    rec_thread.start();
                    setLog("9");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        connectSocket.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TaskExitChat taskExitChat = new TaskExitChat();
        taskExitChat.execute();

    }

    class TaskSendChat extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... Voids) {
            setLog("doInBackground : 메세지 보내는중 ");
            sendWriter.println(routeStream+"kkkkk"+chatContent);
            sendWriter.flush();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setLog("onPostExecute : 메세지 다보냄 ");
        }
    }
    class TaskExitChat extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            viewer--;
//            sendData("http://13.124.223.128/broadcast/updateViewer.php");
        }

        @Override
        protected Void doInBackground(Void... Voids) {
            setLog("doInBackground : 채팅방 나가는 중 ");
            sendWriter.println("ID"+"//////"+routeStream);
            sendWriter.flush();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setLog("onPostExecute : 채팅방 나감 ");
            try {
                sendWriter.close();
                tmpbuf.close();
                m_Socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void setLog(String content){
        android.util.Log.e(TAG,content);
    }


}
