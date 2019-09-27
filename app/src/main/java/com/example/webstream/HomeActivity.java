package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
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
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.Adapter_liveList;
import adapter.Adapter_projectlist_home;
import adapter.Adapter_recordList;
import dataList.DataList_liveList;
import dataList.DataList_project_list;
import dataList.DataList_recordedList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = "HomeActivity";
    public void setLog(String content){
        Log.e(TAG,content);}

    public static final int CHANGE_USERID = 100;
    public static final int CHANGE_USERNICKNAME = 200;
    public static final int CHANGE_PROFILE = 300;
    public static final int REFRESH_PROJECT = 400;
    public static final int REFRESH_LIVE = 500;
    public static final int REFRESH_RECORD = 600;
    public static final int NON_LIVE_LIST = 700;





    public static String loginedUser;
    NavigationView navigationView;
    private HttpConnection httpConn = HttpConnection.getInstance();

    TextView txtHeaderId;
    TextView txtHeaderNickname;
    TextView txtHeaderLogout;
    TextView txtNonLivelist;
    View header;
    MenuItem menuItem;
    Handler handler;
    ImageView imgProfile;


    public static Activity activity = null;
    boolean autoLogin;

    RecyclerView recyHomeProject;
    ArrayList<DataList_project_list> dataProjectList;
    Adapter_projectlist_home adapterProjectlistHome;

    RecyclerView recyHomeLive;
    ArrayList<DataList_liveList> dataLiveList;
    Adapter_liveList adapterLiveList;

    RecyclerView recyHomeRecord;
    ArrayList<DataList_recordedList> dataRecordList;
    Adapter_recordList adapterRecordList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLog("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_home);
        navigationView = findViewById(R.id.nav_view_main);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        header=navigationView.getHeaderView(0);

        Menu menu = navigationView.getMenu();
        menuItem = menu.findItem(R.id.nav_myprofile);

        activity = this;

        txtNonLivelist = findViewById(R.id.home_non_live);

        recyHomeProject = findViewById(R.id.home_recycler_project);
        recyHomeProject.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        dataProjectList = new ArrayList<>();
        adapterProjectlistHome = new Adapter_projectlist_home(HomeActivity.this,dataProjectList);
        recyHomeProject.setAdapter(adapterProjectlistHome);

        recyHomeProject.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyHomeProject, new recyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                DataList_project_list dataListProjectList = dataProjectList.get(position);

                setLog("넘기는 넘버 : "+dataListProjectList.getNumber());
                Intent intent = new Intent(HomeActivity.this, ViewProjectActivity.class);
                intent.putExtra("postNumber",String.valueOf(dataListProjectList.getNumber()));
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        recyHomeLive = findViewById(R.id.home_recycler_live);
        recyHomeLive.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        dataLiveList = new ArrayList<>();
        adapterLiveList = new Adapter_liveList(HomeActivity.this,dataLiveList);
        recyHomeLive.setAdapter(adapterLiveList);

        recyHomeLive.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyHomeLive, new recyclerClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        recyHomeRecord = findViewById(R.id.home_recycler_record);
        recyHomeRecord.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        dataRecordList = new ArrayList<>();
        adapterRecordList = new Adapter_recordList(HomeActivity.this,dataRecordList);
        recyHomeRecord.setAdapter(adapterRecordList);

        recyHomeRecord.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyHomeProject, new recyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                DataList_recordedList dataList_recordedList = dataRecordList.get(position);
                setLog("인텐트로 레코드뷰에 넘기는 데이터"+dataList_recordedList.getHostNickname());

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

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
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
                            Glide.with(HomeActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(HomeActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }

                        break;
                    case REFRESH_PROJECT:
                        adapterProjectlistHome.notifyDataSetChanged();
                        break;
                    case REFRESH_RECORD:
                        adapterRecordList.notifyDataSetChanged();
                        break;
                    case REFRESH_LIVE:
                        adapterLiveList.notifyDataSetChanged();
                        setLog("제발 보자");
                        if(dataLiveList.get(0).getTitle()==null){
                            txtNonLivelist.setVisibility(View.VISIBLE);
                        }else {
                            txtNonLivelist.setVisibility(View.GONE);
                        }
                        break;
                    case NON_LIVE_LIST:

                        break;





                }
            }
        };
        GetLiveList();
        GetProjectList();
        GetRecordList();

    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onResume() {

        super.onResume();
        setLog("onResume");
        Intent intent = getIntent();
        setLog("아이디 : "+intent.getStringExtra("id"));
        setLog("아이디 : "+intent.getStringExtra("password"));

        SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
        loginedUser = spCurrentUser.getString("currentUser","");
        autoLogin = spCurrentUser.getBoolean("autoLogin",false);


        setLog("로그인 된 사람 : "+loginedUser);


        if(loginedUser.equals("")){

        }else {

            navigationView.inflateHeaderView(R.layout.nav_header_logined);
            navigationView.removeHeaderView(navigationView.getHeaderView(0));
            header=navigationView.getHeaderView(0);
            menuItem.setVisible(true);


            sendData(loginedUser,"http://13.124.223.128/getUserData/getUserData.php");

        }
        //헤더 추가 가능
        //navigationView.inflateHeaderView(R.layout.nav_header_logined);

        //헤더 빼기 가능
        //navigationView.removeHeaderView(navigationView.getHeaderView(0));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_home);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //앱바 메뉴 들
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.main, menu);*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_project) {
            Intent intent = new Intent(HomeActivity.this,ProjectListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } /*else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_interview) {

        }*/

        else if (id == R.id.nav_exploration) {
            Intent intent = new Intent(HomeActivity.this,LiveListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_myprofile) {
            Intent intent = new Intent(HomeActivity.this,MyProfileActivity.class);
            intent.putExtra("loginedUser",loginedUser);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_home);
        drawer.closeDrawer(GravityCompat.START);
        return true;


    }
    public void OnMoveLoginActivity(View view) {
        Intent intent = new Intent(HomeActivity.this,LoginActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","HomeActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void OnMoveSignupActivity(View view) {
        Intent intent = new Intent(HomeActivity.this,SignupActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","HomeActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
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
                String getNickname = getUserData2.getString("nickname");
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


                /*Looper.prepare();
                Toast.makeText(MainActivity.this, "반갑습니다. "+getNickname+"님", Toast.LENGTH_SHORT).show();
                Looper.loop();*/
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    private void GetProjectList() {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url ="http://13.124.223.128/home/getProjectList.php";
        new Thread() {
            public void run() {
                httpConn.requestHomeProjectList(callbackHomeProjectList, url);
            }
        }.start();

    }
    private final Callback callbackHomeProjectList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 callbackHomeProjectList:"+body);

            try {
                JSONObject getUserData = new JSONObject(body);
                JSONArray totalData = new JSONArray(getUserData.getString("projectList"));
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
                    //dataListProject.add(dataSet);

                    dataProjectList.add(dataSet);

                    Message messageRefresh = handler.obtainMessage();
                    messageRefresh.what = REFRESH_PROJECT;
                    handler.sendMessage(messageRefresh);




                    //adapter_projectList.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    private void GetLiveList() {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url ="http://13.124.223.128/home/getLiveList.php";
        new Thread() {
            public void run() {
                httpConn.requestHomeLiveList(callbackHomeLiveList, url);
            }
        }.start();

    }
    private final Callback callbackHomeLiveList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 callbackHomeLiveList:"+body);

            try {
                JSONObject jsonObject = new JSONObject(body);
                String data1 = jsonObject.getString("liveList");
                JSONArray jaBroadcastList = new JSONArray(data1);
                if(jaBroadcastList.getJSONObject(0).getString("host")==null){
                    setLog("비었다요");
                    Message messageRefresh = handler.obtainMessage();
                    messageRefresh.what = NON_LIVE_LIST;
                    handler.sendMessage(messageRefresh);
                }
                for(int i=0;i<jaBroadcastList.length();i++){
                    Log.e(TAG,"어디보자 : "+jaBroadcastList.getJSONObject(i));
                    JSONObject joBroadList = jaBroadcastList.getJSONObject(i);
                    String host = joBroadList.getString("host");
                    String title = joBroadList.getString("title");
                    int number = Integer.parseInt(joBroadList.getString("number"));
                    int viewer = Integer.parseInt(joBroadList.getString("viewer"));
                    String routeThumbnail = joBroadList.getString("routeThumbnail");
                    String routeStream = joBroadList.getString("routeStream");
                    String password = joBroadList.getString("password");
                    String hostNickname = joBroadList.getString("hostNickname");
                    setLog("AAA가져온 시청자수 : "+viewer);
                    setLog("AAA넣기전 시청자수 : "+viewer);

                    DataList_liveList dataList = new DataList_liveList();
                    dataList.setHost(host);
                    dataList.setTitle(title);
                    dataList.setNumber(number);
                    dataList.setRouteThumbnail(routeThumbnail);
                    dataList.setRouteStream(routeStream);
                    dataList.setViewer(viewer);
                    dataList.setPassword(password);
                    dataList.setHostNickname(hostNickname);
                    setLog("오디1 : ");
                    dataLiveList.add(dataList);

                    setLog("오디2 : ");
                }
                Message messageRefresh = handler.obtainMessage();
                messageRefresh.what = REFRESH_LIVE;
                handler.sendMessage(messageRefresh);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    private void GetRecordList() {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url ="http://13.124.223.128/home/getRecordList.php";
        new Thread() {
            public void run() {
                httpConn.requestHomeRecordList(callbackHomeRecordList, url);
            }
        }.start();

    }
    private final Callback callbackHomeRecordList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 callbackHomeRecordList:"+body);

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
                    messageRefresh.what = REFRESH_RECORD;
                    handler.sendMessage(messageRefresh);

                    //adapter_recordList.notifyDataSetChanged();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    public void OnLogout(View view) {
        setLog("로그아웃 버튼 클릭");
        SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
        SharedPreferences.Editor editor = spCurrentUser.edit();
        editor.remove("currentUser");
        editor.commit();

        Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(HomeActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();


    }
    @Override
    protected void onStop() {
        setLog("onStop");
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setLog("onDestroy");
        if(!autoLogin){
            SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
            SharedPreferences.Editor editor = spCurrentUser.edit();
            editor.remove("currentUser");
            editor.commit();
        }
    }
}
