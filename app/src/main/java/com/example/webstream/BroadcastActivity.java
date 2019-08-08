package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    String streamRoute;
    String thumbnailRoute;
    int userRecordedCount;

    ImageButton broadcastButton;

    Button btnSendChat;
    TextView txtContent;
    EditText inputContent;

    String userId;
    String chatContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast);


        //------------------------------MainActivity 방송 정보 입력 다이얼로그에 입력했던 방송정보들 변수에 입력----------------------------
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        host = intent.getStringExtra("loginedUser");
        password = intent.getStringExtra("password");

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

        /*Button broadcastButton = (Button) findViewById(R.id.broadcast_button);*/
        broadcastButton = findViewById(R.id.broadcast_button);
        broadcastButton.setOnClickListener(this);

        mWZCameraView = findViewById(R.id.camera_preview);

        CommunicateServer("http://13.124.223.128/recording/createUserRecordList.php",FLAG_CREATE_USER);

        btnSendChat = findViewById(R.id.buttonChat);
        txtContent = findViewById(R.id.textView4Chata);
        inputContent = findViewById(R.id.editTextChat);

        SocketConnect();
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

                    broadcastButton.setImageResource(R.drawable.ic_start);
                    finish();
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

            broadcastButton.setImageResource(R.drawable.ic_stop);

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
                if(flag == FLAG_STOP_RECORD || flag == FLAG_START_RECORD || flag == FLAG_INSERT_BROADCAST){
                    params.put("streamRoute", streamRoute);
                    params.put("password", password);
                    setLog("비밀번호 : "+password);
                }else if(flag == FLAG_INSERT_RECORD){
                    params.put("streamRoute", streamRoute);
                    params.put("userRecordedCount", String.valueOf(userRecordedCount));
                    params.put("routeThumbnail", thumbnailRoute);
                    setLog("썸네일 경로1 : "+thumbnailRoute);
                }else if(flag == FLAG_SAVE_THUMBNAIL){
                    params.put("streamRoute", streamRoute);
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
        WOWZCamera newCamera = mWZCameraView.switchCamera();
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
                String[] split;

                while(true) {
                    receiveString = tmpbuf.readLine();

                    split = receiveString.split(">");
                    if(split.length >=2 && split[0].equals(userId)) {
                        continue;
                    }
                    setLog(receiveString);
                    //txtContent.setText(receiveString);
                    System.out.println(receiveString);
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

                final PrintWriter sendWriter = new PrintWriter(new OutputStreamWriter(m_Socket.getOutputStream(), StandardCharsets.UTF_8), true);

                final String sendString;


                setLog("사용할 ID를 입력해주세요 : ");
                System.out.println("사용할 ID를 입력해주세요 : ");
                userId = "sinhwan02112멀";

                sendWriter.println("IDhighkrs12345" + host);
                sendWriter.flush();

                btnSendChat.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLog("보내기");
                        chatContent = inputContent.getText().toString();
                        Thread sendThread = new Thread() {
                            @Override
                            public void run() {
                                super.run();
                                sendWriter.println(chatContent);
//                                try {
//                                    //sendWriter.println(contentChat.getBytes("UTF-8"));
//
//                                } catch (UnsupportedEncodingException e) {
//                                    e.printStackTrace();
//                                }
                                sendWriter.flush();
                            }
                        };
                        sendThread.start();


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
}
