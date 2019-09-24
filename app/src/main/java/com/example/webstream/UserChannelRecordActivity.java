package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.Adapter_recordList;
import dataList.DataList_liveList;
import dataList.DataList_recordedList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserChannelRecordActivity extends AppCompatActivity {

    private static final String TAG = "UserChannelRecordActivity";
    public void setLog(String content){
        Log.e(TAG,content);
    }

    public static final int CHANGE_PROFILE = 100;
    public static final int REFRESH = 200;
    public static final int CHANGE_SUBSCRIBER = 300;
    public static final int NON_STREAM = 400;
    public static final int STREAM = 500;
    public static final int STREAM_SETTING = 600;
    public static final int CHECK_SUBSCRIBE = 700;
    public static final String ON_SUBSCRIBE = "ON_SUBSCRIBE";
    public static final String CANCEL_SUBSCRIBE = "NON_SUBSCRIBE";
    public static final String GET_SUBSCRIBELIST = "GET_SUBSCRIBELIST";








    BottomNavigationView bottomNavigationView;
    private HttpConnection httpConn = HttpConnection.getInstance();

    String strWritter;

    ImageView imgProfile;
    TextView txtId;
    ImageButton btnSubscribe;
    TextView txtSubscribing;
    TextView txtViewerCount;
    TextView loading;


    Handler handler;

    ProgressDialog progressDialog;

    TextView txtTitle;
    TextView txtHost;
    ImageView imgThumbnail;
    TextView txtViewer;
    String strTitle;
    String strHost;
    String strViewer;
    String strThumbnail;
    String strRouteStream;
    int intSubscriber;
    Boolean blnNowSubscribe = null;

    ConstraintLayout nonConstraint;
    ConstraintLayout defaultConstraint;

    DataList_liveList dataList_liveList;


    RecyclerView recyRecordList;
    private ArrayList<DataList_recordedList> dataRecordList;
    private Adapter_recordList adapter_recordList;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.userchannel_bottom_navigation_projectlist:
                    setLog("projectList클릭");
                    Intent intent = new Intent(UserChannelRecordActivity.this,UserChannelProjectActivity.class);
                    intent.putExtra("writter",strWritter);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.userchannel_bottom_navigation_recordlist:
                    setLog("recordlist클릭");
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_channel_record);

        progressDialog = new ProgressDialog(UserChannelRecordActivity.this);
        progressDialog.setMessage("불러오는 중...");
        progressDialog.show();

        Intent intent = getIntent();
        strWritter = intent.getStringExtra("writter");


        bottomNavigationView = findViewById(R.id.userChannelRecord_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.userchannel_bottom_navigation_recordlist);

        imgProfile = findViewById(R.id.userChannelRecord_image_profile);
        txtId = findViewById(R.id.userChannelRecord_text_id);
        btnSubscribe = findViewById(R.id.userChannelRecord_button_subscribe);
        txtSubscribing = findViewById(R.id.userChannelRecord_text_subscribing);
        txtViewerCount = findViewById(R.id.userChannelRecord_text_subsercount);
        loading = findViewById(R.id.userChannelRecord_loading);
        nonConstraint = findViewById(R.id.userChannelRecord_nonconstraint);
        defaultConstraint = findViewById(R.id.userChannelRecord_constraint);
        recyRecordList = findViewById(R.id.userChannelRecord_recycler_recordlist);
        recyRecordList.setLayoutManager(new LinearLayoutManager(UserChannelRecordActivity.this));
        dataRecordList = new ArrayList<>();
        adapter_recordList = new Adapter_recordList(UserChannelRecordActivity.this,dataRecordList);
        recyRecordList.setAdapter(adapter_recordList);

        //구독중
        if(HomeActivity.loginedUser.equals(strWritter)){
            btnSubscribe.setVisibility(View.GONE);
            txtSubscribing.setVisibility(View.GONE);
        }else {

        }


        txtTitle = findViewById(R.id.userChannelRecord_text_title);
        txtHost = findViewById(R.id.userChannelRecord_text_host);
        imgThumbnail = findViewById(R.id.userChannelRecord_img_thumbnail);
        txtViewer = findViewById(R.id.userChannelRecord_text_viewer);



        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //구독 하고 있을 때 클릭
                if(blnNowSubscribe){
                    AlertDialog.Builder alert = new AlertDialog.Builder(UserChannelRecordActivity.this);
                    alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SendSubscribeData(CANCEL_SUBSCRIBE);
                            dialog.dismiss();
                            blnNowSubscribe = false;
                            btnSubscribe.setImageResource(R.drawable.ic_star_blue_24dp);
                            txtSubscribing.setText("구독");
                            txtSubscribing.setTextColor(Color.parseColor("#257EF3"));
                            intSubscriber = intSubscriber -1;
                            txtViewerCount.setText("구독자 "+String.valueOf(intSubscriber)+" 명");

                        }
                    });
                    alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.setMessage(strWritter+"님 구독을 취소하시겠습니까?");
                    alert.show();

                }
                //구독 안하고 있을 때 클릭
                else {
                    SendSubscribeData(ON_SUBSCRIBE);
                    blnNowSubscribe = true;
                    btnSubscribe.setImageResource(R.drawable.ic_star_gray_24dp);
                    txtSubscribing.setText("구독중");
                    txtSubscribing.setTextColor(Color.parseColor("#B7B7B7"));
                    intSubscriber = intSubscriber +1;
                    txtViewerCount.setText("구독자 "+String.valueOf(intSubscriber)+" 명");
                }




            }
        });

        //defaultConstraint.setVisibility(View.GONE);
        defaultConstraint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewStreamActivity.class);
                intent.putExtra("title",dataList_liveList.getTitle());
                intent.putExtra("number",dataList_liveList.getNumber());
                intent.putExtra("host",dataList_liveList.getHost());
                intent.putExtra("routeStream",dataList_liveList.getRouteStream());
                intent.putExtra("viewer",dataList_liveList.getViewer());
                intent.putExtra("loginedUser",HomeActivity.loginedUser);
                intent.putExtra("hostNickname",dataList_liveList.getHostNickname());
                startActivity(intent);
            }
        });

        recyRecordList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),recyRecordList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                setLog("클릭함");
                DataList_recordedList dataList_recordedList = dataRecordList.get(position);

                Intent intent = new Intent(getApplicationContext(), ViewRecordedActivity.class);
                intent.putExtra("host",dataList_recordedList.getHost());
                intent.putExtra("hostNickname",dataList_recordedList.getHostNickname());
                intent.putExtra("title",dataList_recordedList.getTitle());
                intent.putExtra("RecordNumber",dataList_recordedList.getRecordNumber());
                intent.putExtra("routeVideo",dataList_recordedList.getRouteVideo());
                intent.putExtra("routeThumbnail",dataList_recordedList.getRouteThumbnail());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        txtId.setText(strWritter);
        GetUserData(strWritter);
        GetRecordList(strWritter);
        GetStreamList(strWritter);
        SendSubscribeData(GET_SUBSCRIBELIST);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CHANGE_PROFILE:
                        setLog("핸들러 메세지 확인 (프로필) : "+msg.obj);
                        if(msg.obj.equals("null")){
                            Glide.with(UserChannelRecordActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(UserChannelRecordActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }
                        loading.setVisibility(View.GONE);
                        progressDialog.dismiss();
                        break;
                    case REFRESH:
                        adapter_recordList.notifyItemChanged(dataRecordList.size());
                        //progressDialog.dismiss();
                        break;
                    case CHANGE_SUBSCRIBER:
                        txtViewerCount.setText("구독자 "+String.valueOf(msg.obj)+" 명");
                        break;
                    case NON_STREAM:
                        defaultConstraint.setVisibility(View.GONE);
                        nonConstraint.setVisibility(View.VISIBLE);
                        break;
                    case STREAM:
                        nonConstraint.setVisibility(View.GONE);
                        defaultConstraint.setVisibility(View.VISIBLE);
                        break;
                    case STREAM_SETTING:
                        txtTitle.setText(strTitle);
                        txtHost.setText(strHost);
                        txtViewer.setText(strViewer);
                        Glide.with(UserChannelRecordActivity.this)
                                .load(strThumbnail)
                                .apply(new RequestOptions().circleCrop())
                                .into(imgThumbnail);
                        break;
                    case CHECK_SUBSCRIBE:
                        //구독중
                        if(String.valueOf(msg.obj).equals("1")){
                            btnSubscribe.setImageResource(R.drawable.ic_star_gray_24dp);
                            txtSubscribing.setText("구독중");
                            blnNowSubscribe = true;
                        }
                        //구독 안함
                        else if(String.valueOf(msg.obj).equals("0")){
                            btnSubscribe.setImageResource(R.drawable.ic_star_blue_24dp);
                            txtSubscribing.setText("구독");
                            txtSubscribing.setTextColor(Color.parseColor("#257EF3"));
                            blnNowSubscribe = false;
                        }
                        break;



                }
            }
        };

    }

    private void GetUserData(final String param) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/getUserData/getUserData.php";
        new Thread() {
            public void run() {
                httpConn.requestGetUserData(param, callbackGetUserData, url);
            }
        }.start();

    }

    private final Callback callbackGetUserData = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body callbackGetUserData:"+body);

            try {
                setLog("제이슨 시작");
                JSONObject getUserData = new JSONObject(body);
                String dummyData = getUserData.getString("userInfo");
                setLog("111 : "+dummyData);
                JSONArray getUserDataArray = new JSONArray(dummyData);
                JSONObject getUserData2 = getUserDataArray.getJSONObject(0);
                setLog("222 : "+String.valueOf(getUserData2));
                String getProfile = getUserData2.getString("profileRoute");
                String getSubscriber = getUserData2.getString("subscriber");
                intSubscriber = Integer.parseInt(getSubscriber);
                if(Integer.parseInt(getSubscriber)<0){
                    getSubscriber = "0";
                }

                Message messageProfile = handler.obtainMessage();
                messageProfile.what = CHANGE_PROFILE;
                messageProfile.obj = getProfile;
                handler.sendMessage(messageProfile);

                Message messageSubscriber = handler.obtainMessage();
                messageSubscriber.what = CHANGE_SUBSCRIBER;
                messageSubscriber.obj = getSubscriber;
                handler.sendMessage(messageSubscriber);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    private void GetRecordList(final String param) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/recording/getRecordListChannel.php";
        new Thread() {
            public void run() {
                httpConn.requestRecordListChannel(param, callbackGetRecordList, url);
            }
        }.start();

    }

    private final Callback callbackGetRecordList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog("콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body callbackGetRecordList:" + body);

            try {
                JSONObject jsonObject = new JSONObject(body);
                String data1 = jsonObject.getString("recordedList");
                setLog("data1 : " + data1);
                JSONArray jaBroadcastList = new JSONArray(data1);
                    /*Logging.e(TAG,"제이슨 길이 : "+jaBroadcastList.length());
                    Logging.e(TAG,"data1 : "+jaBroadcastList.getJSONObject(0));*/
                for (int i = 0; i < jaBroadcastList.length(); i++) {
                    setLog("어디보자 : " + jaBroadcastList.getJSONObject(i));
                    JSONObject joBroadList = jaBroadcastList.getJSONObject(i);
                    String host = joBroadList.getString("host");
                    String title = joBroadList.getString("title");
                    String hostNickname = joBroadList.getString("hostNickname");
                    int recordNumber = Integer.parseInt(joBroadList.getString("RecordNumber"));
                    String routeVideo = joBroadList.getString("routeVideo");
                    String routeThumbnail = joBroadList.getString("routeThumbnail");

                    DataList_recordedList dataList = new DataList_recordedList();
                    dataList.setHost(host);
                    dataList.setHostNickname(hostNickname);
                    dataList.setTitle(title);
                    dataList.setRecordNumber(recordNumber);
                    dataList.setRouteVideo(routeVideo);
                    dataList.setRouteThumbnail(routeThumbnail);
                    setLog("데이터 접속중 6 ");
                    dataRecordList.add(dataList);


                    Message messageRefresh = handler.obtainMessage();
                    messageRefresh.what = REFRESH;
                    handler.sendMessage(messageRefresh);

                    //adapter_recordList.notifyDataSetChanged();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void GetStreamList(final String param) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/broadcast/getBroadListChannel.php";
        new Thread() {
            public void run() {
                httpConn.requestBroadListChannel(param, callbackGetStreamList, url);
            }
        }.start();

    }

    private final Callback callbackGetStreamList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog("콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body callbackGetStreamList:" + body);
            if(body.equals("{\"broadcastList\": []}")){
                setLog("비엇다.");
            }
            try {
                JSONObject jsonObject = new JSONObject(body);
                String data1 = jsonObject.getString("broadcastList");
                setLog("비었나 : "+data1);
                if(data1.equals("[]")){
                    setLog("비었다 드뎌2");

                    Message messageNonStream = handler.obtainMessage();
                    messageNonStream.what = NON_STREAM;
                    handler.sendMessage(messageNonStream);
                }else {
                    Message messageStream = handler.obtainMessage();
                    messageStream.what = STREAM;
                    handler.sendMessage(messageStream);

                    /*Logging.e(TAG,"data1 : "+data1);*/
                    JSONArray jaBroadcastList = new JSONArray(data1);
                    /*Logging.e(TAG,"제이슨 길이 : "+jaBroadcastList.length());
                    Logging.e(TAG,"data1 : "+jaBroadcastList.getJSONObject(0));*/
                    for(int i=0;i<jaBroadcastList.length();i++){
                        Log.e(TAG,"어디보자 : "+jaBroadcastList.getJSONObject(i));
                        JSONObject joBroadList = jaBroadcastList.getJSONObject(i);
                        strHost = joBroadList.getString("host");
                        strTitle = joBroadList.getString("title");
                        int number = Integer.parseInt(joBroadList.getString("number"));
                        strViewer = joBroadList.getString("viewer");
                        strThumbnail = joBroadList.getString("routeThumbnail");
                        strRouteStream = joBroadList.getString("routeStream");
                        String password = joBroadList.getString("password");
                        String hostNickname = joBroadList.getString("hostNickname");
                        setLog("AAA가져온 시청자수 : "+strTitle);

                        Message messageStreamSet = handler.obtainMessage();
                        messageStreamSet.what = STREAM_SETTING;
                        handler.sendMessage(messageStreamSet);

                        dataList_liveList = new DataList_liveList();
                        dataList_liveList.setHost(strHost);
                        dataList_liveList.setTitle(strTitle);
                        dataList_liveList.setNumber(number);
                        dataList_liveList.setViewer(Integer.parseInt(strViewer));
                        dataList_liveList.setRouteThumbnail(strThumbnail);
                        dataList_liveList.setRouteStream(strRouteStream);
                        dataList_liveList.setPassword(password);
                        dataList_liveList.setHostNickname(hostNickname);

//                    setLog("AAA넣기전 시청자수 : "+viewer);
//                    DataList_liveList dataList = new DataList_liveList();
//                    dataList.setHost(host);
//                    dataList.setTitle(title);
//                    dataList.setNumber(number);
//                    dataList.setRouteThumbnail(routeThumbnail);
//                    dataList.setRouteStream(routeStream);
//                    dataList.setViewer(viewer);
//                    dataList.setPassword(password);
//                    dataList.setHostNickname(hostNickname);
//                    setLog("오디1 : ");
//                    dataList_liveLists.add(dataList);
//                    adapter_liveList.notifyDataSetChanged();
//                    setLog("오디2 : ");
                    }
                }



            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private void SendSubscribeData(final String flag) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/subscribe/sendSubscribeData.php";
        new Thread() {
            public void run() {
                setLog("플래그 : "+flag);
                httpConn.requestSendSubscribe(strWritter,HomeActivity.loginedUser, flag,callbackSendSubscribeData, url);
            }
        }.start();

    }
    private final Callback callbackSendSubscribeData = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog("콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body callbackSendSubscribeData:" + body);

            Message messageTest = handler.obtainMessage();
            messageTest.what = CHECK_SUBSCRIBE;
            messageTest.obj = body;
            handler.sendMessage(messageTest);
        }
    };

    //recyclerview 터치시 이벤트 발생하는 기능 사용하려면 이 class가 있어야됨, 정확한 이유는 모름, 나중에
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    //recyclerview 터치시 이벤트 발생하는 기능 사용하려면 이 class가 있어야됨, 정확한 이유는 모름, 나중에
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private UserChannelRecordActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final UserChannelRecordActivity
                .ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }
}
