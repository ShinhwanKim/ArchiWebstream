package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.Adapter_liveList;
import adapter.Adapter_recordList;
import dataList.DataList_recordedList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class VodListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    //답사 게시판 레이아웃

    private static final String TAG = "VodListActivity";
    public void setLog(String content){
        Log.e(TAG,content);}

    public static final int CHANGE_USERID = 100;
    public static final int CHANGE_USERNICKNAME = 200;
    public static final int CHANGE_PROFILE = 300;

    NavigationView navigationView;
    View header;
    MenuItem menuItemMyprofile;
    Handler handler;

    TextView txtHeaderId;
    TextView txtHeaderNickname;
    ImageView imgProfile;

    String loginedUser;
    private HttpConnection httpConn = HttpConnection.getInstance();

    boolean autoLogin;
    public static Activity activity = null;
    BottomNavigationView bottomNavigationView;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_navigation_live:
                    setLog("바텀 네비게이션 : 라이브 방송");
                    Intent intent = new Intent(VodListActivity.this,LiveListActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
                    return true;
                case R.id.bottom_navigation_vod:
                    setLog("바텀 네비게이션 : 녹화 방송");
                    return true;
                case R.id.bottom_navigation_subscribe:
                    setLog("바텀 네비게이션 : 구독");
                    return true;
                case R.id.bottom_navigation_broadcast:
                    setLog("바텀 네비게이션 : 방송하기");
                    onBroadcast();
                    return true;
            }
            return false;
        }
    };


    private ArrayList<DataList_recordedList> dataList_vodList;
    private Adapter_recordList adapter_vodList;

    private RequestQueue queue;

    SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLog("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod_list);
        Toolbar toolbar = findViewById(R.id.toolbar_vodlist);
        //setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.vodlist_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        DrawerLayout drawer = findViewById(R.id.drawer_layout_vodlist);
        navigationView = findViewById(R.id.nav_view_vodlist);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        Menu menu = navigationView.getMenu();
        menuItemMyprofile = menu.findItem(R.id.nav_myprofile);

        activity = this;

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
                            Glide.with(VodListActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(VodListActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }

                        break;



                }
            }
        };

        RecyclerView recyVodList = findViewById(R.id.activity_vodlist_recyclerview_broadcastList);
        recyVodList.setLayoutManager(new LinearLayoutManager(VodListActivity.this));
        dataList_vodList = new ArrayList<>();

        adapter_vodList = new Adapter_recordList(VodListActivity.this,dataList_vodList);
        recyVodList.setAdapter(adapter_vodList);

        swipeRefreshLayout = findViewById(R.id.activity_vodlist_refresh);
        //------------------------------리사이클러뷰 새로고침 이벤트----------------------------

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {


                getBroadcastTask getBroadcastTask = new getBroadcastTask();
                getBroadcastTask.execute();
                /*getBroadcastList();*/


                swipeRefreshLayout.setRefreshing(false);
            }
        });

        recyVodList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyVodList, new recyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                DataList_recordedList dataList_recordedList = dataList_vodList.get(position);

                Intent intent = new Intent(getApplicationContext(), ViewRecordedActivity.class);
                intent.putExtra("host",dataList_recordedList.getHost());
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        setLog("onResume");

        getBroadcastList();

        //바텀 네비게이션 뷰 선택
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_vod);

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
    }
    @Override
    protected void onStop() {
        super.onStop();
        setLog("onStop");
        if(!autoLogin){
            SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
            SharedPreferences.Editor editor = spCurrentUser.edit();
            editor.remove("currentUser");
            editor.commit();
        }
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


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    public class getBroadcastTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getBroadcastList();
            return null;
        }
    }

    public void OnMoveLoginActivity(View view) {
        Intent intent = new Intent(VodListActivity.this,LoginActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","VodListActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void OnMoveSignupActivity(View view) {
        Intent intent = new Intent(VodListActivity.this,SignupActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","VodListActivity");
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

        Intent intent = new Intent(VodListActivity.this,VodListActivity.class);
        startActivity(intent);
        finish();


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_vodlist);
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
        //*getMenuInflater().inflate(R.menu.main, menu);*//*
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
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            Intent intentHome = new Intent(VodListActivity.this,HomeActivity.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intentHome);
        } else if (id == R.id.nav_project) {

        } /*else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_interview) {

        }*/

        else if (id == R.id.nav_exploration) {

        } else if (id == R.id.nav_myprofile) {
            Intent intent = new Intent(VodListActivity.this,MyProfileActivity.class);
            intent.putExtra("loginedUser",loginedUser);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_vodlist);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void onBroadcast(){
        if(loginedUser.equals("")){
            Toast.makeText(activity, "로그인이 필요한 기능입니다. 로그인을 해주세요.", Toast.LENGTH_SHORT).show();
        }else {
            final AlertDialog.Builder inputBroadcastInfo = new AlertDialog.Builder(VodListActivity.this);
            View editboxview = LayoutInflater.from(VodListActivity.this).inflate(R.layout.editbox_broadcast,
                    null,false);
            inputBroadcastInfo.setView(editboxview);


            final EditText etextTitle = editboxview.findViewById(R.id.editbox_edittext_title);
            final EditText etextPassword = editboxview.findViewById(R.id.editbox_edittext_password);
            final CheckBox checkBoxPassword = editboxview.findViewById(R.id.editbox_checkbox_password);
            final TextInputLayout textInputLayoutPassword = editboxview.findViewById(R.id.editbox_textinputlayout_password);

            checkBoxPassword.setOnClickListener(new CheckBox.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(checkBoxPassword.isChecked()){
                        textInputLayoutPassword.setVisibility(View.VISIBLE);
                    }else {
                        textInputLayoutPassword.setVisibility(View.GONE);
                    }
                }
            });


            //------------------------------다이얼로그 완료 버튼 이벤트----------------------------
            inputBroadcastInfo.setPositiveButton("완료", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String dataTitle = etextTitle.getText().toString();
                    String dataPassword = etextPassword.getText().toString();

                    setLog("타이틀 : "+dataTitle);
                    setLog("비번 : "+dataPassword);


                    dialog.dismiss();

                    Intent broadcastIntent = new Intent(getApplicationContext(),BroadcastActivity.class);
                    broadcastIntent.putExtra("title",dataTitle);
                    broadcastIntent.putExtra("password",dataPassword);
                    broadcastIntent.putExtra("loginedUser",loginedUser);
                    startActivity(broadcastIntent);
                }
            });

            //------------------------------다이얼로그 취소 버튼 이벤트----------------------------
            inputBroadcastInfo.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                }
            });



            inputBroadcastInfo.show();
        }

    }
    public void getBroadcastList(){
        setLog("데이터 접속중 3 ");
        dataList_vodList.clear();
        adapter_vodList.notifyDataSetChanged();



        //------------------------------서버에서 방송 목록을 가져옴----------------------------

        queue = Volley.newRequestQueue(this);
        String url = "http://13.124.223.128/recording/getRecordedList.php";
        setLog("데이터 접속중 4 ");

        sendData("테스트",url);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setLog("데이터 접속중 5 ");
                setLog(response);
                /*Logging.e("TAG","json2 : "+data);*/
                try {
                    JSONObject jsonObject = new JSONObject(response.replaceAll("\\P{Print}",""));
                    String data1 = jsonObject.getString("recordedList");
                    setLog("data1 : "+data1);
                    JSONArray jaBroadcastList = new JSONArray(data1);
                    /*Logging.e(TAG,"제이슨 길이 : "+jaBroadcastList.length());
                    Logging.e(TAG,"data1 : "+jaBroadcastList.getJSONObject(0));*/
                    for(int i=0;i<jaBroadcastList.length();i++){
                        setLog("어디보자 : "+jaBroadcastList.getJSONObject(i));
                        JSONObject joBroadList = jaBroadcastList.getJSONObject(i);
                        String host = joBroadList.getString("host");
                        String title = joBroadList.getString("title");
                        int recordNumber = Integer.parseInt(joBroadList.getString("RecordNumber"));
                        String routeVideo = joBroadList.getString("routeVideo");
                        String routeThumbnail = joBroadList.getString("routeThumbnail");

                        DataList_recordedList dataList = new DataList_recordedList();
                        dataList.setHost(host);
                        dataList.setTitle(title);
                        dataList.setRecordNumber(recordNumber);
                        dataList.setRouteVideo(routeVideo);
                        dataList.setRouteThumbnail(routeThumbnail);
                        setLog("데이터 접속중 6 ");
                        dataList_vodList.add(dataList);
                        adapter_vodList.notifyDataSetChanged();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }



    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }
}
