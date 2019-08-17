package com.example.webstream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import java.util.regex.Pattern;

import adapter.Adapter_liveList;
import dataList.DataList_liveList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LiveListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "LiveListActivity";
    public static boolean finishView;
    public static int finishPosition;

    public void setLog(String content){Log.e(TAG,content);}

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
    private HttpConnection httpConn = HttpConnection.getInstance();

    boolean autoLogin;
    public static Activity activity = null;
    BottomNavigationView bottomNavigationView;

    //---------------------------------------------------------

    private ArrayList<DataList_liveList> dataList_liveLists;    //방송 목록을 보여주는 리사이클러뷰에 들어갈 데이터 리스트
    private Adapter_liveList adapter_liveList;                          //방송 목록을 보여주는 리사이클러뷰에 적용될 어댑터
    private RequestQueue queue;                              //volley 쓸 때 사용
    SwipeRefreshLayout swipeRefreshLayout;                    //당겨서 새로고침 할 때 쓰는 스와이프 레이아웃

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.bottom_navigation_live:
                    setLog("바텀 네비게이션 : 라이브 방송");
                    return true;
                case R.id.bottom_navigation_vod:
                    setLog("바텀 네비게이션 : 녹화 방송");
                    Intent intent = new Intent(LiveListActivity.this,VodListActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLog("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_livelist);
        Toolbar toolbar = findViewById(R.id.toolbar_livelist);
        /*setSupportActionBar(toolbar);*/

        bottomNavigationView = findViewById(R.id.livelist_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //bottomNavigationView.setOnNavigationItemSelectedListener(new ExplorationBottomNavListener().mOnNavigationItemSelectedListener);


        DrawerLayout drawer = findViewById(R.id.drawer_layout_livelist);
        navigationView = findViewById(R.id.nav_view_livelist);
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
                            Glide.with(LiveListActivity.this)
                                    .load(R.drawable.user_profile_default_white)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(LiveListActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }

                        break;



                }
            }
        };
        RecyclerView recyLiveList = findViewById(R.id.activity_livelist_recyclerview_broadcastList);
        recyLiveList.setLayoutManager(new LinearLayoutManager(LiveListActivity.this));
        dataList_liveLists = new ArrayList<>();

        adapter_liveList = new Adapter_liveList(LiveListActivity.this,dataList_liveLists);
        recyLiveList.setAdapter(adapter_liveList);

        finishView = false;

        swipeRefreshLayout = findViewById(R.id.activity_livelist_refresh);
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
        recyLiveList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),recyLiveList, new MainActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //선택한 아이템의 stream 데이터를 ViewStreamActivity로 넘겨준다.

                DataList_liveList dataBroadcastList = dataList_liveLists.get(position);

                Intent intent = new Intent(getApplicationContext(), ViewStreamActivity.class);
                intent.putExtra("title",dataBroadcastList.getTitle());
                intent.putExtra("number",dataBroadcastList.getNumber());
                intent.putExtra("host",dataBroadcastList.getHost());
                intent.putExtra("routeStream",dataBroadcastList.getRouteStream());
                intent.putExtra("viewer",dataBroadcastList.getViewer());
                intent.putExtra("loginedUser",loginedUser);
                intent.putExtra("hostNickname",dataBroadcastList.getHostNickname());
                intent.putExtra("position",position);
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
        //바텀 네비게이션 뷰 선택
        bottomNavigationView.setSelectedItemId(R.id.bottom_navigation_live);

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
        getBroadcastList();
    }
    @Override
    protected void onStop() {
        super.onStop();
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
    public void getBroadcastList(){
        dataList_liveLists.clear();
        adapter_liveList.notifyDataSetChanged();

        setLog("onResume");


        //------------------------------서버에서 방송 목록을 가져옴----------------------------

        queue = Volley.newRequestQueue(this);
        String url = "http://13.124.223.128/broadcast/getBroadcastList.php";

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                /*Logging.e("TAG","json2 : "+data);*/
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String data1 = jsonObject.getString("broadcastList");
                    /*Logging.e(TAG,"data1 : "+data1);*/
                    JSONArray jaBroadcastList = new JSONArray(data1);
                    /*Logging.e(TAG,"제이슨 길이 : "+jaBroadcastList.length());
                    Logging.e(TAG,"data1 : "+jaBroadcastList.getJSONObject(0));*/
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
                        setLog("AAAfinishView : "+finishView);
                        if(finishPosition  == i){
                            if(finishView == true){
                                viewer -- ;
                                finishView = false;
                            }
                        }

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

                        dataList_liveLists.add(dataList);
                        adapter_liveList.notifyDataSetChanged();
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

    public void OnMoveLoginActivity(View view) {
        Intent intent = new Intent(LiveListActivity.this,LoginActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","LiveListActivity");
        editor.commit();
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    public void OnMoveSignupActivity(View view) {
        Intent intent = new Intent(LiveListActivity.this,SignupActivity.class);
        SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
        SharedPreferences.Editor editor = spLastActivity.edit();
        editor.putString("lastActivity","LiveListActivity");
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

        Intent intent = new Intent(LiveListActivity.this,LiveListActivity.class);
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
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {
            Intent intentHome = new Intent(LiveListActivity.this,HomeActivity.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intentHome);
        } else if (id == R.id.nav_project) {

        } /*else if (id == R.id.nav_news) {

        } else if (id == R.id.nav_interview) {

        }*/

        else if (id == R.id.nav_exploration) {

        } else if (id == R.id.nav_myprofile) {
            Intent intent = new Intent(LiveListActivity.this,MyProfileActivity.class);
            intent.putExtra("loginedUser",loginedUser);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout_livelist);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public class getBroadcastTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getBroadcastList();
            return null;
        }
    }

    public void onBroadcast(){
        if(loginedUser.equals("")){
            Toast.makeText(activity, "로그인이 필요한 기능입니다. 로그인을 해주세요.", Toast.LENGTH_SHORT).show();
        }else {
            final AlertDialog.Builder inputBroadcastInfo = new AlertDialog.Builder(LiveListActivity.this);
            View editboxview = LayoutInflater.from(LiveListActivity.this).inflate(R.layout.editbox_broadcast,
                    null,false);
            inputBroadcastInfo.setView(editboxview);


            final EditText etextTitle = editboxview.findViewById(R.id.editbox_edittext_title);
            final EditText etextPassword = editboxview.findViewById(R.id.editbox_edittext_password);
            final CheckBox checkBoxPassword = editboxview.findViewById(R.id.editbox_checkbox_password);
            final TextInputLayout textInputLayoutPassword = editboxview.findViewById(R.id.editbox_textinputlayout_password);

            etextTitle.setText( loginedUser+"님의 방송입니다.");
            etextTitle.selectAll();

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);



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
                    if(dataTitle.equals("")){
                        dataTitle = loginedUser+"님의 방송입니다.";
                    }

                    setLog("타이틀 : "+dataTitle);
                    setLog("비번 : "+dataPassword);


                    dialog.dismiss();

                    Intent broadcastIntent = new Intent(getApplicationContext(),BroadcastActivity.class);
                    broadcastIntent.putExtra("title",dataTitle);
                    broadcastIntent.putExtra("password",dataPassword);
                    broadcastIntent.putExtra("loginedUser",loginedUser);
                    broadcastIntent.putExtra("nickname",getNickname);
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
    //recyclerview 터치시 이벤트 발생하는 기능 사용하려면 이 class가 있어야됨, 정확한 이유는 모름, 나중에
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    //recyclerview 터치시 이벤트 발생하는 기능 사용하려면 이 class가 있어야됨, 정확한 이유는 모름, 나중에
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private MainActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final MainActivity
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
