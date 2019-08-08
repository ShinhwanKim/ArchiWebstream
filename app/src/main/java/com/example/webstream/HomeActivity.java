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
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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


    String loginedUser;
    NavigationView navigationView;
    private HttpConnection httpConn = HttpConnection.getInstance();

    TextView txtHeaderId;
    TextView txtHeaderNickname;
    TextView txtHeaderLogout;
    View header;
    MenuItem menuItem;
    Handler handler;
    ImageView imgProfile;


    public static Activity activity = null;
    boolean autoLogin;

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



                }
            }
        };

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

        } /*else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_interview) {

        }*/

        else if (id == R.id.nav_exploration) {
            Intent intent = new Intent(HomeActivity.this,LiveListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
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
