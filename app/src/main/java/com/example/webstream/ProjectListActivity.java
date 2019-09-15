package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProjectListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ProjectListActivity";
    public void setLog(String content){
        Log.e(TAG,content);
    }

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

    String getNickname;
    String loginedUser;

    BottomNavigationView bottomNavigationView;

    public static Activity activity = null;
    boolean autoLogin;

    private HttpConnection httpConn = HttpConnection.getInstance();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_navigation_projectlist:
                    setLog("바텀 네비게이션 : 글목록");
                    return true;
                case R.id.bottom_navigation_project_subscribe:
                    setLog("바텀 네비게이션 : 구독");
//                    Intent intent = new Intent(ProjectListActivity.this,VodListActivity.class);
//                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                    startActivity(intent);
//                    finish();
                    return true;
                case R.id.bottom_navigation_write:
                    setLog("바텀 네비게이션 : 글쓰기");
                    Intent intent = new Intent(ProjectListActivity.this,ProjectWriteActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLog("onCreate");
        setContentView(R.layout.activity_project_list);

        bottomNavigationView = findViewById(R.id.projectlist_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Toolbar toolbar = findViewById(R.id.toolbar_projectlist);

        DrawerLayout drawer = findViewById(R.id.drawer_layout_projectlist);
        navigationView = findViewById(R.id.nav_view_projectlist);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

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
                            Glide.with(ProjectListActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(ProjectListActivity.this)
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
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setLog("onResume");
        //바텀 네비게이션 뷰 선택
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_projectlist);

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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            Intent intentHome = new Intent(ProjectListActivity.this,HomeActivity.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intentHome);
            finish();
        } else if (id == R.id.nav_project) {
        } /*else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_interview) {

        }*/

        else if (id == R.id.nav_exploration) {
            Intent intent = new Intent(ProjectListActivity.this,LiveListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_myprofile) {
            Intent intent = new Intent(ProjectListActivity.this,MyProfileActivity.class);
            intent.putExtra("loginedUser",loginedUser);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_projectlist);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void OnMoveLoginActivity(View view) {
        Intent intent = new Intent(ProjectListActivity.this,LoginActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","ProjectListActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void OnMoveSignupActivity(View view) {
        Intent intent = new Intent(ProjectListActivity.this,SignupActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","ProjectListActivity");
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

        Intent intent = new Intent(ProjectListActivity.this,ProjectListActivity.class);
        startActivity(intent);
        finish();


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_livelist);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
