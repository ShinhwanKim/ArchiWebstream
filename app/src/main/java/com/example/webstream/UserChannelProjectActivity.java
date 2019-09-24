package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import adapter.Adapter_projectList;
import dataList.DataList_project_list;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserChannelProjectActivity extends AppCompatActivity {

    private static final String TAG = "UserChannelProjectActivity";
    public void setLog(String content){
        Log.e(TAG,content);
    }

    public static final int CHANGE_PROFILE = 100;
    public static final int REFRESH = 200;
    public static final int CHANGE_SUBSCRIBER = 300;

    public static final int CHECK_SUBSCRIBE = 700;

    public static final String ON_SUBSCRIBE = "ON_SUBSCRIBE";
    public static final String CANCEL_SUBSCRIBE = "NON_SUBSCRIBE";
    public static final String GET_SUBSCRIBELIST = "GET_SUBSCRIBELIST";

    ImageView imgProfile;
    TextView txtId;
    ImageButton btnSubscribe;
    TextView txtViewerCount;
    TextView loading;
    TextView txtSubscribing;

    String strWritter;
    String strSubscriber;
    String strUserProfile;

    int intSubscriber;

    Boolean blnNowSubscribe = null;
    Handler handler;


    BottomNavigationView bottomNavigationView;
    private HttpConnection httpConn = HttpConnection.getInstance();

    ProgressDialog progressDialog;

    private ArrayList<DataList_project_list> dataListProject;
    private Adapter_projectList adapter_projectList;
    private RecyclerView recyProjectList;

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()){
                case R.id.userchannel_bottom_navigation_projectlist:
                    setLog("projectList클릭");
                    return true;
                case R.id.userchannel_bottom_navigation_recordlist:
                    setLog("recordlist클릭");
                    Intent intent = new Intent(UserChannelProjectActivity.this,UserChannelRecordActivity.class);
                    intent.putExtra("writter",strWritter);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_channel_project);

        progressDialog = new ProgressDialog(UserChannelProjectActivity.this);
        progressDialog.setMessage("불러오는 중...");
        progressDialog.show();

        Intent intent = getIntent();
        strWritter = intent.getStringExtra("writter");

        bottomNavigationView = findViewById(R.id.userChannelProject_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.userchannel_bottom_navigation_projectlist);

        imgProfile = findViewById(R.id.userChannelProject_image_profile);
        txtId = findViewById(R.id.userChannelProject_text_id);
        btnSubscribe = findViewById(R.id.userChannelProject_button_subscribe);
        txtSubscribing = findViewById(R.id.userChannelProject_text_subscribing);
        txtViewerCount = findViewById(R.id.userChannelProject_text_subsercount);
        recyProjectList = findViewById(R.id.userChannelProject_recycler_projectlist);
        loading = findViewById(R.id.userChannelProject_loading);

        recyProjectList.setLayoutManager(new LinearLayoutManager(UserChannelProjectActivity.this));
        dataListProject = new ArrayList<>();

        adapter_projectList = new Adapter_projectList(UserChannelProjectActivity.this,dataListProject);
        recyProjectList.setAdapter(adapter_projectList);

        //구독중
        if(HomeActivity.loginedUser.equals(strWritter)){
            btnSubscribe.setVisibility(View.GONE);
            txtSubscribing.setVisibility(View.GONE);
        }else {

        }

        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blnNowSubscribe){
                    AlertDialog.Builder alert = new AlertDialog.Builder(UserChannelProjectActivity.this);
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

        recyProjectList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),recyProjectList,new UserChannelProjectActivity.ClickListener(){
            @Override
            public void onClick(View view, int position) {
                DataList_project_list dataListProjectList = dataListProject.get(position);

                setLog("넘기는 넘버 : "+dataListProjectList.getNumber());
                Intent intent = new Intent(UserChannelProjectActivity.this, ViewProjectActivity.class);
                intent.putExtra("postNumber",String.valueOf(dataListProjectList.getNumber()));
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));
        txtId.setText(strWritter);

        GetUserData(strWritter);
        GetProjectList(strWritter);
        SendSubscribeData(GET_SUBSCRIBELIST);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CHANGE_PROFILE:
                        setLog("핸들러 메세지 확인 (프로필) : "+msg.obj);
                        if(msg.obj.equals("null")){
                            Glide.with(UserChannelProjectActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(UserChannelProjectActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }
                        loading.setVisibility(View.GONE);
                        progressDialog.dismiss();
                        break;
                    case REFRESH:
                        adapter_projectList.notifyItemChanged(dataListProject.size());
                        //progressDialog.dismiss();
                        break;
                    case CHANGE_SUBSCRIBER:
                        txtViewerCount.setText("구독자 "+String.valueOf(msg.obj)+" 명");
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

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    private void GetProjectList(final String targetId){
        final String url ="http://13.124.223.128/board/getProjectListChannel.php";
        new Thread(){
            @Override
            public void run() {
                super.run();
                httpConn.requestProjectListChannel(targetId,callbackProjectList,url);
            }
        }.start();
    }

    private final Callback callbackProjectList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body callbackProjectList:"+body);
            try{
                JSONObject getUserData = new JSONObject(body);

                setLog("파싱 추적1");
                JSONArray totalData = new JSONArray(getUserData.getString("projectList"));
                setLog("파싱 추적2");
                for(int i=0;i<totalData.length();i++){
                    setLog("파싱 추적3");
                    JSONObject projectDataSet = totalData.getJSONObject(i);
                    setLog("파싱 추적4");
                    String postNumber = projectDataSet.getString("number");
                    setLog("파싱 추적5");
                    String postTitle = projectDataSet.getString("title");
                    setLog("파싱 추적6");
                    String postOwner = projectDataSet.getString("owner");
                    setLog("파싱 추적7");
                    String postLocation = projectDataSet.getString("location");
                    setLog("파싱 추적8");
                    String postWrittenDate = projectDataSet.getString("writtenDate");
                    String postView = projectDataSet.getString("view");
                    String postLike = projectDataSet.getString("like");
                    setLog("파싱 추적9");
                    setLog("파싱 데이터 postNumber : "+postNumber);
                    setLog("파싱 데이터 postTitle : "+postTitle);
                    setLog("파싱 데이터 postOwner : "+postOwner);
                    setLog("파싱 데이터 postLocation : "+postLocation);
                    setLog("파싱 데이터 postWrittenDate : "+postWrittenDate);
                    JSONArray dummyContent = new JSONArray(projectDataSet.getString("content"));
                    DataList_project_list dataSet = new DataList_project_list();
                    dataSet.setNumber(Integer.parseInt(postNumber));
                    setLog("파싱 결과물 : "+dataSet.getNumber());
                    dataSet.setTitle(postTitle);
                    setLog("파싱 결과물 : "+dataSet.getTitle());
                    dataSet.setOwner(postOwner);
                    setLog("파싱 결과물 : "+dataSet.getOwner());
                    dataSet.setLocation(postLocation);
                    setLog("파싱 결과물 : "+dataSet.getLocation());
                    dataSet.setWrittenDate(postWrittenDate);
                    setLog("파싱 결과물 : "+dataSet.getWrittenDate());
                    dataSet.setView(Integer.parseInt(postView));
                    setLog("파싱 결과물 : "+dataSet.getView());
                    dataSet.setLike(Integer.parseInt(postLike));
                    setLog("파싱 결과물 : "+dataSet.getLike());
                    for(int k=0;k<dummyContent.length();k++){
                        String isMaster = null;
                        JSONObject jsonContent = dummyContent.getJSONObject(k);
                        try{
                            isMaster = jsonContent.getString("isMaster");
                        }catch (Exception e){

                        }
                        if(Boolean.valueOf(isMaster)){
                            String masterImgPath = jsonContent.getString("imagePath");
                            String masterImgOrientation = jsonContent.getString("orientation");
                            setLog("대표이미지 : "+masterImgPath);

                            dataSet.setImage(masterImgPath);
                            dataSet.setImageOrientation(masterImgOrientation);
                            setLog("파싱 결과물 : "+dataSet.getImage());

                        }
                    }

                    setLog("파싱 결과물 : "+dataSet.getTitle());
                    setLog("파싱 결과물 : "+dataSet.getOwner());
                    setLog("파싱 결과물 : "+dataSet.getLocation());
                    setLog("파싱 결과물 : "+dataSet.getWrittenDate());
                    setLog("파싱 결과물 : "+dataSet.getImage());
                    dataListProject.add(dataSet);



                    setLog("파싱 데이터 추가 결과물 : "+dataListProject);
                    setLog("파싱 데이터 추가 결과물 갯수 : "+dataListProject.size());

                    Message messageRefresh = handler.obtainMessage();
                    messageRefresh.what = REFRESH;
                    handler.sendMessage(messageRefresh);

                }
            }catch (Exception e){

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
        private UserChannelProjectActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final UserChannelProjectActivity
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
}
