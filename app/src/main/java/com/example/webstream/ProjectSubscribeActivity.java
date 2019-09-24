package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import dataList.DataList_project_list;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProjectSubscribeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "ProjectSubscribeActivity";
    public void setLog(String content){
        Log.e(TAG,content);
    }

    public static final int CHANGE_USERID = 100;
    public static final int CHANGE_USERNICKNAME = 200;
    public static final int CHANGE_PROFILE = 300;

    BottomNavigationView bottomNavigationView;

    NavigationView navigationView;
    View header;
    MenuItem menuItemMyprofile;
    Handler handler;
    TextView txtHeaderId;
    TextView txtHeaderNickname;
    ImageView imgProfile;

    String getNickname;

    public static String loginedUser;
    boolean autoLogin;

    private HttpConnection httpConn = HttpConnection.getInstance();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_navigation_projectlist:
                    setLog("바텀 네비게이션 : 글목록");
                    Intent intentProjectlist = new Intent(ProjectSubscribeActivity.this,ProjectListActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentProjectlist.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intentProjectlist);
                    finish();
                    return true;
                case R.id.bottom_navigation_project_subscribe:
                    setLog("바텀 네비게이션 : 구독");

                    return true;
                case R.id.bottom_navigation_write:
                    setLog("바텀 네비게이션 : 글쓰기");
                    Intent intentWrite = new Intent(ProjectSubscribeActivity.this,ProjectWriteActivity.class);
                    intentWrite.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intentWrite);
                    //finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_subscribe);

        bottomNavigationView = findViewById(R.id.project_subscribe_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Toolbar toolbar = findViewById(R.id.toolbar_project_subscribe);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_project_subscribe);
        navigationView = findViewById(R.id.nav_view_project_subscribe);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        menuItemMyprofile = menu.findItem(R.id.nav_myprofile);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CHANGE_USERID:
                        txtHeaderId.setText(String.valueOf(msg.obj));
                        setLog("핸들러 메세지 확인 (아이디) : "+msg.obj);
                        break;
                    case CHANGE_USERNICKNAME:
                        txtHeaderNickname.setText(String.valueOf(msg.obj));
                        setLog("핸들러 메세지 확인 (닉) : "+msg.obj);
                        break;
                    case CHANGE_PROFILE:
                        setLog("핸들러 메세지 확인 (프로필) : "+msg.obj);
                        if(msg.obj.equals("null")){
                            Glide.with(ProjectSubscribeActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(ProjectSubscribeActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }

                        break;



                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLog("onResume");

        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_project_subscribe);

        SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
        loginedUser = spCurrentUser.getString("currentUser","");
        autoLogin = spCurrentUser.getBoolean("autoLogin",false);
        setLog("로그인 된 사람 : "+loginedUser);
        setLog("자동 로그인 여부 : "+autoLogin);

        if(loginedUser.equals("")){

        }else {

            navigationView.inflateHeaderView(R.layout.nav_header_logined);
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            header=navigationView.getHeaderView(0);
            menuItemMyprofile.setVisible(true);


            sendData(loginedUser,"http://13.124.223.128/getUserData/getUserData.php");

        }
        GetSubscriberList();
        //dataListProject.clear();
//        GetProjectList(currentFilter, getStartIndex,HomeActivity.loginedUser,"http://13.124.223.128/board/getProjectList.php");
//        setLog("데이터 불러오기 최초 : "+getStartIndex);

    }

    private void sendData(final String param, final String url) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                httpConn.requestGetUserData(param, callback, url);
            }
        }.start();

    }

    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body:"+body);

            try {
                setLog("제이슨 시작");
                JSONObject getUserData = new JSONObject(body);
                String dummyData = getUserData.getString("userInfo");
                setLog("111 : "+dummyData);
                JSONArray getUserDataArray = new JSONArray(dummyData);
                JSONObject getUserData2 = getUserDataArray.getJSONObject(0);
                setLog("222 : "+String.valueOf(getUserData2));
                String getId = getUserData2.getString("id");
                getNickname = getUserData2.getString("nickname");
                String getProfile = getUserData2.getString("profileRoute");

                setLog("유저아이디 : "+getId);
                setLog("유저닉네임 : "+getNickname);

                txtHeaderId = header.findViewById(R.id.nav_header_id);
                txtHeaderNickname = header.findViewById(R.id.nav_header_nickname);
                imgProfile = header.findViewById(R.id.nav_header_profile);



                Message messageId = handler.obtainMessage();
                messageId.what = CHANGE_USERID;
                messageId.obj = getId;
                handler.sendMessage(messageId);

                Message messageNickname = handler.obtainMessage();
                messageNickname.what = CHANGE_USERNICKNAME;
                messageNickname.obj = getNickname;
                handler.sendMessage(messageNickname);

                Message messageProfile = handler.obtainMessage();
                messageProfile.what = CHANGE_PROFILE;
                messageProfile.obj = getProfile;
                handler.sendMessage(messageProfile);


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    private void GetSubscriberList() {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/subscribe/getSubscriberList.php";
        new Thread() {
            public void run() {
                httpConn.requestGetSubscriberList(loginedUser, callbackGetSubscriberList, url);
            }
        }.start();

    }
    private final Callback callbackGetSubscriberList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 GetSubscriberList:"+body);

        }
    };

//    private void GetProjectList(final String param, final int param2, final String currentUser,final String url) {
//        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
//        new Thread() {
//            public void run() {
//                httpConn.requestProjectList(param, param2,currentUser,callbackForProjectList, url);
//            }
//        }.start();
//
//    }
//    private final Callback callbackForProjectList = new Callback() {
//        @Override
//        public void onFailure(Call call, IOException e) {
//            setLog( "콜백오류:"+e.getMessage());
//        }
//        @Override
//        public void onResponse(Call call, Response response) throws IOException {
//            String body = response.body().string();
//            getStartIndex = getStartIndex + 10;
//            filterChangeIs = false;
//            setLog("서버에서 응답한 callbackForProjectList:"+body);
//            setLog("파싱 추적");
//            ArrayList<DataList_project_list> dataListSet = new ArrayList<>();
//            try {
//                JSONObject getUserData = new JSONObject(body);
//
//                setLog("파싱 추적1");
//                JSONArray totalData = new JSONArray(getUserData.getString("projectList"));
//                setLog("파싱 추적2");
//                for(int i=0;i<totalData.length();i++){
//                    setLog("파싱 추적3");
//                    JSONObject projectDataSet = totalData.getJSONObject(i);
//                    setLog("파싱 추적4");
//                    String postNumber = projectDataSet.getString("number");
//                    setLog("파싱 추적5");
//                    String postTitle = projectDataSet.getString("title");
//                    setLog("파싱 추적6");
//                    String postOwner = projectDataSet.getString("owner");
//                    setLog("파싱 추적7");
//                    String postLocation = projectDataSet.getString("location");
//                    setLog("파싱 추적8");
//                    String postWrittenDate = projectDataSet.getString("writtenDate");
//                    String postView = projectDataSet.getString("view");
//                    String postLike = projectDataSet.getString("like");
//                    setLog("파싱 추적9");
//                    setLog("파싱 데이터 postNumber : "+postNumber);
//                    setLog("파싱 데이터 postTitle : "+postTitle);
//                    setLog("파싱 데이터 postOwner : "+postOwner);
//                    setLog("파싱 데이터 postLocation : "+postLocation);
//                    setLog("파싱 데이터 postWrittenDate : "+postWrittenDate);
//                    JSONArray dummyContent = new JSONArray(projectDataSet.getString("content"));
//                    DataList_project_list dataSet = new DataList_project_list();
//                    dataSet.setNumber(Integer.parseInt(postNumber));
//                    setLog("파싱 결과물 : "+dataSet.getNumber());
//                    dataSet.setTitle(postTitle);
//                    setLog("파싱 결과물 : "+dataSet.getTitle());
//                    dataSet.setOwner(postOwner);
//                    setLog("파싱 결과물 : "+dataSet.getOwner());
//                    dataSet.setLocation(postLocation);
//                    setLog("파싱 결과물 : "+dataSet.getLocation());
//                    dataSet.setWrittenDate(postWrittenDate);
//                    setLog("파싱 결과물 : "+dataSet.getWrittenDate());
//                    dataSet.setView(Integer.parseInt(postView));
//                    setLog("파싱 결과물 : "+dataSet.getView());
//                    dataSet.setLike(Integer.parseInt(postLike));
//                    setLog("파싱 결과물 : "+dataSet.getLike());
//                    for(int k=0;k<dummyContent.length();k++){
//                        String isMaster = null;
//                        JSONObject jsonContent = dummyContent.getJSONObject(k);
//                        try{
//                            isMaster = jsonContent.getString("isMaster");
//                        }catch (Exception e){
//
//                        }
//                        if(Boolean.valueOf(isMaster)){
//                            String masterImgPath = jsonContent.getString("imagePath");
//                            String masterImgOrientation = jsonContent.getString("orientation");
//                            setLog("대표이미지 : "+masterImgPath);
//
//                            dataSet.setImage(masterImgPath);
//                            dataSet.setImageOrientation(masterImgOrientation);
//                            setLog("파싱 결과물 : "+dataSet.getImage());
////                            Message messageMasterImg = handler.obtainMessage();
////                            messageMasterImg.what = SET_MATERIMAGE;
////                            messageMasterImg.obj = masterImgPath;
////                            handler.sendMessage(messageMasterImg);
//
//                        }
//                    }
//
//                    setLog("파싱 결과물 : "+dataSet.getTitle());
//                    setLog("파싱 결과물 : "+dataSet.getOwner());
//                    setLog("파싱 결과물 : "+dataSet.getLocation());
//                    setLog("파싱 결과물 : "+dataSet.getWrittenDate());
//                    setLog("파싱 결과물 : "+dataSet.getImage());
//                    //dataListProject.add(dataSet);
//
//                    dataListSet.add(dataSet);
//
//
//                    setLog("파싱 데이터 추가 결과물 : "+dataListProject);
//
//
//
//                    //adapter_projectList.notifyDataSetChanged();
//                }
//                //dataListProject.clear();
//                dataListProject.addAll(dataListSet);
//                Message messageRefresh = handler.obtainMessage();
//                messageRefresh.what = REFRESH;
//                handler.sendMessage(messageRefresh);
//            } catch (JSONException e) {
//                e.printStackTrace();
//                setLog("파싱 추적10");
//            }
//
//
//        }
//    };

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            Intent intentHome = new Intent(ProjectSubscribeActivity.this,HomeActivity.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intentHome);
            finish();
        } else if (id == R.id.nav_project) {
        } /*else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_interview) {

        }*/

        else if (id == R.id.nav_exploration) {
            Intent intent = new Intent(ProjectSubscribeActivity.this,LiveListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_myprofile) {
            Intent intent = new Intent(ProjectSubscribeActivity.this,MyProfileActivity.class);
            intent.putExtra("loginedUser",loginedUser);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_projectlist);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void OnMoveLoginActivity(View view) {
        Intent intent = new Intent(ProjectSubscribeActivity.this,LoginActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","ProjectSubscribeActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void OnMoveSignupActivity(View view) {
        Intent intent = new Intent(ProjectSubscribeActivity.this,SignupActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","ProjectSubscribeActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void OnLogout(View view) {
        setLog("로그아웃 버튼 클릭");
        SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
        SharedPreferences.Editor editor = spCurrentUser.edit();
        editor.remove("currentUser");
        editor.commit();

        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(ProjectSubscribeActivity.this,ProjectSubscribeActivity.class);
        startActivity(intent);
        finish();


    }
}
