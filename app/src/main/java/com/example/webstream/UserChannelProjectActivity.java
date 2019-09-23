package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
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
    public static final int CHANGE_SUBSCRIBER = 200;

    ImageView imgProfile;
    TextView txtId;
    Button btnSubscribe;
    TextView txtViewerCount;

    String strWritter;
    String strSubscriber;
    String strUserProfile;

    Handler handler;

    BottomNavigationView bottomNavigationView;
    private HttpConnection httpConn = HttpConnection.getInstance();

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
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_channel_project);

        Intent intent = getIntent();
        strWritter = intent.getStringExtra("writter");

        bottomNavigationView = findViewById(R.id.userChannelProject_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        bottomNavigationView.setSelectedItemId(R.id.userchannel_bottom_navigation_projectlist);

        imgProfile = findViewById(R.id.userChannelProject_image_profile);
        txtId = findViewById(R.id.userChannelProject_text_id);
        btnSubscribe = findViewById(R.id.userChannelProject_button_subscribe);
        txtViewerCount = findViewById(R.id.userChannelProject_text_subsercount);
        recyProjectList = findViewById(R.id.userChannelProject_recycler_projectlist);

        recyProjectList.setLayoutManager(new LinearLayoutManager(UserChannelProjectActivity.this));
        dataListProject = new ArrayList<>();

        adapter_projectList = new Adapter_projectList(UserChannelProjectActivity.this,dataListProject);
        recyProjectList.setAdapter(adapter_projectList);

        txtId.setText(strWritter);

        GetUserData(strWritter);
        GetProjectList(strWritter);

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

                Message messageProfile = handler.obtainMessage();
                messageProfile.what = CHANGE_PROFILE;
                messageProfile.obj = getProfile;
                handler.sendMessage(messageProfile);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

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
//                            Message messageMasterImg = handler.obtainMessage();
//                            messageMasterImg.what = SET_MATERIMAGE;
//                            messageMasterImg.obj = masterImgPath;
//                            handler.sendMessage(messageMasterImg);

                        }
                    }

                    setLog("파싱 결과물 : "+dataSet.getTitle());
                    setLog("파싱 결과물 : "+dataSet.getOwner());
                    setLog("파싱 결과물 : "+dataSet.getLocation());
                    setLog("파싱 결과물 : "+dataSet.getWrittenDate());
                    setLog("파싱 결과물 : "+dataSet.getImage());
                    dataListProject.add(dataSet);



                    setLog("파싱 데이터 추가 결과물 : "+dataListProject);



                    adapter_projectList.notifyDataSetChanged();
                }
            }catch (Exception e){

            }



        }
    };
}
