package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.status.WOWZState;
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
import java.util.HashMap;
import java.util.Map;

import adapter.Adapter_chatList;
import dataList.DataList_chatList_broadcast;

/*
이 액티비티는 방송을 와우자 스트리밍 엔진에 송출하는 액티비티이다.
최초 액티비티가 실행되더라도 방송하기 버튼을 누르기 전까지는 방송을 송출하지 않는다
방송하기 버튼을 누르면 와우자 스트리밍 엔진에 방송을 송출하고,
ArchiApp 서버에 방송관 관련된 데이터를 insert한다.
*/
public class BroadcastActivity extends AppCompatActivity
        implements WOWZStatusCallback, View.OnClickListener{

    private int FLAG_NONE = 0;
    private int FLAG_INQUIRE = 1;
    private int FLAG_START_RECORD = 3;
    private int FLAG_STOP_RECORD = 4;
    private int FLAG_INSERT_RECORD = 5;
    private int FLAG_CREATE_USER = 6;
    private int FLAG_SAVE_THUMBNAIL = 7;
    private int FLAG_INSERT_BROADCAST = 8;

    public static final int GET_CHAT_CONTENT = 100;
    public static final int SEND_CHAT_CONTENT = 200;



    private String ORIENTATION_LANDSCAPE = "가로";
    private String ORIENTATION_PORTRAIT = "세로";

    private String ORIENTATION_STATE = ORIENTATION_PORTRAIT;



    private static final String TAG = "BroadcastActivity";

    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView;

    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;

    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;

    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;

    // Properties needed for Android 6+ permissions handling
    private static final int PERMISSIONS_REQUEST_CODE = 0x1;
    private boolean mPermissionsGranted = true;
    private String[] mRequiredPermissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private RequestQueue queue;

    WOWZCameraView mWZCameraView = null;

    //------------------------------MainActivity 방송 정보 입력 다이얼로그에 입력했던 방송정보들----------------------------
    String title;
    String host;
    String password;
    String hostNickname;
    String streamRoute;
    String thumbnailRoute;
    int userRecordedCount;

    ImageButton btnBroadcast;
    ImageView imgCameraReversal;

    private RecyclerView recyChatList;
    private ImageView imgChatState;


    EditText inputContent;

    String userId;
    String chatContent;

    TextView txtTitle ;

    ImageView imgViewer ;
    TextView txtViewer ;

    private boolean blnOptionState;
    private boolean blnChatState;

    LinearLayout chatSet ;
    TextView txtSendContent ;
    EditText etxtChatContent;

    PrintWriter sendWriter = null;

    private Handler handler;

    private ArrayList<DataList_chatList_broadcast> dataList_chatListBroadcasts;    //채팅 내용을 보여주는 리사이클러뷰에 들어갈 데이터 리스트
    private Adapter_chatList adapter_chatList;                  //채팅 내용을 보여주는 리사이클러뷰에 적용될 어댑터


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_broadcast);

        InputMethodManager immhide = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        immhide.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);



        //------------------------------MainActivity 방송 정보 입력 다이얼로그에 입력했던 방송정보들 변수에 입력----------------------------
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        host = intent.getStringExtra("loginedUser");
        password = intent.getStringExtra("password");
        hostNickname = intent.getStringExtra("nickname");


        //------------------------------MainActivity 방송 정보 입력 다이얼로그에 입력했던 방송정보들 변수에 입력----------------------------

        txtTitle = findViewById(R.id.activity_broadcast_textview_title);
        txtSendContent = findViewById(R.id.activity_broadcast_text_sendchat);
        txtViewer = findViewById(R.id.activity_broadcast_text_viewer);
        imgViewer = findViewById(R.id.activity_broadcast_imageview_viewer);
        imgCameraReversal = findViewById(R.id.activity_broadcast_camera_reversal);
        imgChatState = findViewById(R.id.activity_broadcast_chatstate);
        recyChatList = findViewById(R.id.activity_broadcast_recyclerview_chatlist);

        imgChatState.setOnClickListener(this);


        chatSet = findViewById(R.id.activity_broadcast_chatset);
        etxtChatContent = findViewById(R.id.activity_broadcast_edittext_chatcontent);

        txtTitle.setText(title);
        txtSendContent.setOnClickListener(this);

        //------------------------------Wowza SDK이용을 위한 라이센스키 입력----------------------------

        goCoder = WowzaGoCoder.init(getApplicationContext(), "GOSK-AF46-010C-C23E-E352-51DD");

        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Toast.makeText(this,
                    "GoCoder SDK error: " + goCoderInitError.getErrorDescription(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        goCoderCameraView = (WOWZCameraView) findViewById(R.id.camera_preview);
        goCoderCameraView.setOnClickListener(this);

        // Create an audio device instance for capturing and broadcasting audio
        goCoderAudioDevice = new WOWZAudioDevice();

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();

// Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_1920x1080);

// Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream

        //-----------------------------와우자 스트리밍 엔진에 송출하기 위한 연결 루트??(수정필요)----------------------------
        goCoderBroadcastConfig.setHostAddress("15.164.121.33");
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setApplicationName("live");
        goCoderBroadcastConfig.setStreamName(streamRoute);
        goCoderBroadcastConfig.setUsername("kkimshin");
        goCoderBroadcastConfig.setPassword("dhrfks02126263");

        /*goCoderBroadcastConfig.set(WOWZMediaConfig.FRAME_SIZE_1280x720);*/
        goCoderBroadcastConfig.set(WOWZMediaConfig.FRAME_SIZE_1920x1080);


// Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

// Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

        /*Button btnBroadcast = (Button) findViewById(R.id.broadcast_button);*/
        btnBroadcast = findViewById(R.id.broadcast_button);
        btnBroadcast.setOnClickListener(this);

        CommunicateServer("http://13.124.223.128/recording/createUserRecordList.php",FLAG_CREATE_USER);

        final RecyclerView recyChatList = findViewById(R.id.activity_broadcast_recyclerview_chatlist);
        recyChatList.setLayoutManager(new LinearLayoutManager(BroadcastActivity.this));
        dataList_chatListBroadcasts = new ArrayList<>();

        adapter_chatList = new Adapter_chatList(BroadcastActivity.this, dataList_chatListBroadcasts, hostNickname);
        recyChatList.setAdapter(adapter_chatList);

//        //하드코딩
//        DataList_chatList_broadcast dataChat = new DataList_chatList_broadcast();
//        dataChat.setNickname(hostNickname);
//        dataChat.setContent("안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd안녕하세요1123aaasd");
//        dataList_chatListBroadcasts.add(dataChat);
//        adapter_chatList.notifyDataSetChanged();
//
//        for(int i=0;i<10;i++){
//            DataList_chatList_broadcast dataChat2 = new DataList_chatList_broadcast();
//            dataChat2.setNickname("안녕하세요안녕하세요안녕하세요안녕하세요안녕하세요");
//            dataChat2.setContent("요요료료11212avee안녕하세요안녕하세요");
//            dataList_chatListBroadcasts.add(dataChat2);
//            adapter_chatList.notifyDataSetChanged();
//        }



        blnOptionState = true;
        blnChatState = false;

        //채틸 소켓 연결??
        SocketConnect();

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case GET_CHAT_CONTENT:
                        //adapter_chatList.notifyDataSetChanged();
                        //리사이클러뷰를 통째로 재 배열 하니 글자색이 뒤죽박죽되는 에러 발생
                        adapter_chatList.notifyItemChanged(dataList_chatListBroadcasts.size());
                        recyChatList.scrollToPosition(dataList_chatListBroadcasts.size()-1);
                        break;
                    case SEND_CHAT_CONTENT:
                        //adapter_chatList.notifyDataSetChanged();
                        //리사이클러뷰를 통째로 재 배열 하니 글자색이 뒤죽박죽되는 에러 발생
                        adapter_chatList.notifyItemChanged(dataList_chatListBroadcasts.size());
                        recyChatList.scrollToPosition(dataList_chatListBroadcasts.size()-1);
                        break;
                }
            }
        };

    }



    @Override
    protected void onResume() {
        super.onResume();


        // If running on Android 6 (Marshmallow) and later, check to see if the necessary permissions
        // have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mPermissionsGranted = hasPermissions(this, mRequiredPermissions);
            if (!mPermissionsGranted)
                ActivityCompat.requestPermissions(this, mRequiredPermissions, PERMISSIONS_REQUEST_CODE);
        } else
            mPermissionsGranted = true;

        // Start the camera preview display
        if (mPermissionsGranted && goCoderCameraView != null) {
            if (goCoderCameraView.isPreviewPaused())
                goCoderCameraView.onResume();
            else
                goCoderCameraView.startPreview();
        }

    }

    //
// Callback invoked in response to a call to ActivityCompat.requestPermissions() to interpret
// the results of the permissions request
//
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        mPermissionsGranted = true;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                // Check the result of each permission granted
                for(int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mPermissionsGranted = false;
                    }
                }
            }
        }
    }

    //
// Utility method to check the status of a permissions request for an array of permission identifiers
//
    private static boolean hasPermissions(Context context, String[] permissions) {
        for(String permission : permissions)
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;

        return true;
    }

    //----------------------------방송하기 버튼 클릭시 이벤트 발생--------------------------------------
    @Override
    public void onClick(View view) {
        switch (view.getId()){

            //방송하기 버튼
            case R.id.broadcast_button:
                /*setLog("방송중");*/
                // return if the user hasn't granted the app the necessary permissions

                goCoderBroadcastConfig.setStreamName(streamRoute);
                if (!mPermissionsGranted) return;

                // Ensure the minimum set of configuration settings have been specified necessary to
                // initiate a broadcast streaming session
                WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();

                if (configValidationError != null) {
                    Toast.makeText(this, configValidationError.getErrorDescription(), Toast.LENGTH_LONG).show();
                } else if (goCoderBroadcaster.getStatus().isRunning()) {
                    //----------------------------현재 방송중인 상태라면, 방송 종료.--------------------------------------
                    // Stop the broadcast that is currently running

                    AlertDialog.Builder alertDialogProfile = new AlertDialog.Builder(BroadcastActivity.this);
                    alertDialogProfile.setTitle("방송 종료")
                            .setMessage("진행중인 방송을 종료하시겠습니까?");

                    alertDialogProfile.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setLog("방송종료 확인버튼");
                            goCoderBroadcaster.endBroadcast(BroadcastActivity.this);
                            setLog("방송종료");

                            CommunicateServer("http://13.124.223.128/broadcast/deleteBroadcastList.php",FLAG_NONE);
                            CommunicateServer("http://13.124.223.128/broadcast/insertRecordList.php",FLAG_INSERT_RECORD);
                            setLog("썸네일 경로2 : "+thumbnailRoute);
                            CommunicateServer("http://15.164.121.33/php/recording/stopRecord.php",FLAG_STOP_RECORD);

                            btnBroadcast.setImageResource(R.drawable.ic_start);

                        }
                    });
                    alertDialogProfile.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setLog("방송종료 취소버튼");
                        }
                    });
                    alertDialogProfile.show();



                } else {
                    if(ORIENTATION_STATE == ORIENTATION_PORTRAIT){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }else if(ORIENTATION_STATE == ORIENTATION_LANDSCAPE){
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    }
                    //----------------------------현재 방송중이 아니라면, 방송 시작.--------------------------------------
                    // Start streaming
                    goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, this);
                    setLog("방송시작");


                    //------------------------------ArchiApp 서버에 입력한 정보들 저장장---------------------------
                    //post로 서버에 방송 정보 전달 후 DB에 저장
                    CommunicateServer("http://13.124.223.128/broadcast/insertBroadcast.php",FLAG_INSERT_BROADCAST);

                    CommunicateServer("http://15.164.121.33/php/recording/startRecord.php",FLAG_START_RECORD);

                    btnBroadcast.setImageResource(R.drawable.ic_stop);

                }
                break;
                //채팅 메세지 보내기 버튼
            case R.id.activity_broadcast_text_sendchat:

                break;
            case R.id.camera_preview :
                //옵션 뷰 온/오픈 버튼 ,Visible 상태 감지
                if (blnOptionState == true) {
                    setLog("옵션뷰 오프 ");
                    txtTitle.setVisibility(View.GONE);
                    chatSet.setVisibility(View.GONE);
                    txtViewer.setVisibility(View.GONE);
                    imgViewer.setVisibility(View.GONE);
                    btnBroadcast.setVisibility(View.GONE);
                    imgChatState.setVisibility(View.GONE);
                    imgCameraReversal.setVisibility(View.GONE);

                    blnOptionState = false;

                }else if(blnOptionState == false){
                    setLog("옵션뷰 온 ");
                    txtTitle.setVisibility(View.VISIBLE);
                    chatSet.setVisibility(View.VISIBLE);
                    txtViewer.setVisibility(View.VISIBLE);
                    imgViewer.setVisibility(View.VISIBLE);
                    btnBroadcast.setVisibility(View.VISIBLE);
                    imgChatState.setVisibility(View.VISIBLE);
                    imgCameraReversal.setVisibility(View.VISIBLE);

                    blnOptionState = true;
                }else {
                    setLog("옵션뷰 온/오프 에러??");
                }
                break;
                //채팅 온/오프 버튼
            case R.id.activity_broadcast_chatstate:
                // 채팅창 visible상태 -> Gone
                if(blnChatState == true){
                    setLog("채팅 리사이클러뷰 오프");
                    recyChatList.setVisibility(View.GONE);
                    imgChatState.setImageResource(R.drawable.ic_speaker_notes_black_24dp);
                    blnChatState = false;
                }
                // 채팅창 Gone상태 -> visible
                else if(blnChatState == false){
                    setLog("채팅 리사이클러뷰 온");
                    recyChatList.setVisibility(View.VISIBLE);
                    imgChatState.setImageResource(R.drawable.ic_speaker_notes_off_black_24dp);
                    blnChatState = true;
                }else {
                    setLog("채팅 리사이클러뷰 온/오프 에러");
                }

                break;

        }




    }

    @Override
    public void onWZStatus(final WOWZStatus goCoderStatus) {
        // A successful status transition has been reported by the GoCoder SDK
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (goCoderStatus.getState()) {
            case WOWZState.STARTING:
                //statusMessage.append("Broadcast initialization");
                statusMessage.append("방송 준비 중 입니다.");
                break;

            case WOWZState.READY:
                //statusMessage.append("Ready to begin streaming");
                statusMessage.append("방송 준비 중 입니다.");
                break;

            case WOWZState.RUNNING:
                //statusMessage.append("Streaming is active");
                statusMessage.append("방송이 시작 되었습니다.");
                break;

            case WOWZState.STOPPING:
                statusMessage.append("Broadcast shutting down");
                break;

            case WOWZState.IDLE:
                //statusMessage.append("The broadcast is stopped");
                statusMessage.append("방송이 종료 되었습니다.");
                break;

            default:
                return;
        }

        // Display the status message using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadcastActivity.this, statusMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    //
// The callback invoked when an error occurs during a broadcast
//
    @Override
    public void onWZError(final WOWZStatus goCoderStatus) {
        // If an error is reported by the GoCoder SDK, display a message
        // containing the error details using the U/I thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BroadcastActivity.this,
                        "Streaming error: " + goCoderStatus.getLastError().getErrorDescription(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootView != null)
            rootView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void setLog(String content){
        Log.e(TAG,content);
    }

    protected void CommunicateServer(final String url, final int flag){
        queue = Volley.newRequestQueue(this);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                /*setLog(url+" : "+response);*/
                if(flag == FLAG_INQUIRE){
                    String dataResponse = response.replaceAll("\\P{Print}","");
                    userRecordedCount = Integer.parseInt(dataResponse)+1;
                    setLog(String.valueOf(userRecordedCount));
                    streamRoute = host+"_"+userRecordedCount;
                    setLog(streamRoute);
                }else if(flag == FLAG_CREATE_USER){
                    setLog("유저생성완료 : "+response);
                    CommunicateServer("http://13.124.223.128/broadcast/inquireBroadcastList.php",FLAG_INQUIRE);
                }else if(flag == FLAG_SAVE_THUMBNAIL){
                    /*String dataResponse = response.replaceAll("\\P{Print}","");*/
                    thumbnailRoute = response.replaceAll("\\P{Print}","");
                    setLog("썸네일 저장 루트 : "+thumbnailRoute);
                    /*setLog("썸네일 저장 루트 가공: "+dataResponse);*/
                }else if(flag == FLAG_START_RECORD){
                    setLog("녹환끝 녹화 시작 : ");
                    Thread mThread = new Thread(){
                        @Override
                        public void run() {
                            setLog("썸네일 쓰레드 시작 : ");
                            super.run();
                            try {
                                Thread.sleep(5000);
                                setLog("녹환끝 썸네일 시작 : ");
                                CommunicateServer("http://13.124.223.128/uploadImg/downloadThumbnailImg.php",FLAG_SAVE_THUMBNAIL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mThread.start();

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
                params.put("title", title);
                if(flag == FLAG_STOP_RECORD || flag == FLAG_START_RECORD ){
                    params.put("streamRoute", streamRoute);
                    params.put("password", password);
                    setLog("비밀번호 : "+password);
                }else if(flag == FLAG_INSERT_BROADCAST){
                    params.put("streamRoute", streamRoute);
                    params.put("password", password);
                    params.put("hostNickname", hostNickname);
                    setLog("비밀번호 : "+password);
                }else if(flag == FLAG_INSERT_RECORD){
                    params.put("title", streamRoute);
                    params.put("userRecordedCount", String.valueOf(userRecordedCount));
                    params.put("routeThumbnail", thumbnailRoute);
                    setLog("썸네일 경로1 : "+thumbnailRoute);
                }else if(flag == FLAG_SAVE_THUMBNAIL){
                    params.put("title", streamRoute);
                    params.put("orientation", ORIENTATION_STATE);
                }
                return params;
            }

        };

        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    public void onSwitchCamera(View view) {
        WOWZCamera newCamera = goCoderCameraView.switchCamera();
        /*CommunicateServer("http://13.124.223.128/uploadImg/downloadThumbnailImg.php",FLAG_SAVE_THUMBNAIL);*/
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("onConfigurationChanged" , "onConfigurationChanged");
        if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){//세로 전환시
            Log.d("onConfigurationChanged" , "Configuration.ORIENTATION_PORTRAIT");
            ORIENTATION_STATE = ORIENTATION_PORTRAIT;
            setLog("가로 세로 : "+ORIENTATION_STATE);
        }else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){ //가로전환시
            Log.d("onConfigurationChanged", "Configuration.ORIENTATION_LANDSCAPE");
            ORIENTATION_STATE = ORIENTATION_LANDSCAPE;
            setLog("가로 세로 : "+ORIENTATION_STATE);
        }else{

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        goCoderCameraView.clearView();
        goCoderCameraView.stopPreview();
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

                while(true) {
                    receiveString = tmpbuf.readLine();

                    spliter = receiveString.split(">");
                    //if(spliter.length >=2 && spliter[0].equals(userId))
                    setLog("현재 내 닉네임 : "+hostNickname);
                    if(spliter.length >=2 && !spliter[0].equals(hostNickname)) {


                        String nick = spliter[0];
                        String content = spliter[1];
                        setLog("채팅 받아옴 닉 : "+nick);
                        setLog("채팅 받아옴 내용 : "+content);
                        setLog("현재 내 닉네임 : "+hostNickname);
                        setLog(receiveString);

                        DataList_chatList_broadcast dataChat = new DataList_chatList_broadcast();
                        dataChat.setNickname(nick);
                        dataChat.setContent(content);
                        dataList_chatListBroadcasts.add(dataChat);
                        handler.sendEmptyMessage(GET_CHAT_CONTENT);





                        continue;
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

        private Socket m_Socket;

        public SendThread(Socket socket) {
            this.m_Socket = socket;
        }

        @Override
        public void run() {
            super.run();

            try {
                final BufferedReader tmpbuf = new BufferedReader(new InputStreamReader(System.in));

                sendWriter = new PrintWriter(new OutputStreamWriter(m_Socket.getOutputStream(), StandardCharsets.UTF_8), true);

                final String sendString;


                setLog("사용할 ID를 입력해주세요 : ");
                System.out.println("사용할 ID를 입력해주세요 : ");
                userId = "sinhwan02112멀";

                sendWriter.println("IDhighkrs12345" + hostNickname);
                sendWriter.flush();



                txtSendContent.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        setLog("전송 버튼 클릭");
                        if(!goCoderBroadcaster.getStatus().isRunning()){
                            Toast.makeText(BroadcastActivity.this, "채팅 기능은 방송 중에만 이용할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        }else {
                            chatContent = etxtChatContent.getText().toString();
                            if(chatContent.equals("")){

                            }else {
                                DataList_chatList_broadcast dataChat = new DataList_chatList_broadcast();
                                dataChat.setNickname(hostNickname);
                                dataChat.setContent(chatContent);
                                dataList_chatListBroadcasts.add(dataChat);
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

                SocketAddress addr = new InetSocketAddress("192.168.0.24",8888);
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
    public void onBackPressed() {

        if(goCoderBroadcaster.getStatus().isRunning()){
            Toast.makeText(this, "지금은 방송 중입니다. 방송을 먼저 종료해주세요. ", Toast.LENGTH_SHORT).show();
        }else{
            super.onBackPressed();
        }

    }

    class TaskSendChat extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... Voids) {
            setLog("doInBackground : 메세지 보내는중 ");
            sendWriter.println(chatContent);
            sendWriter.flush();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setLog("onPostExecute : 메세지 다보냄 ");
        }
    }
}
