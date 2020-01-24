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
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

import adapter.Adapter_projectList;
import dataList.DataList_liveList;
import dataList.DataList_project_list;
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
    public static final int SET_MATERIMAGE = 400;
    public static final int REFRESH = 500;
    public static final String SORT_VIEW = "SORT_VIEW";
    public static final String SORT_LIKE = "SORT_LIKE";
    public static final String SORT_DATE_UP = "SORT_DATE_UP";
    public static final String SORT_DATE_DOWN = "SORT_DATE_DOWN";
    public static final String SORT_MYLIKE = "SORT_MYLIKE";





    NavigationView navigationView;
    View header;
    MenuItem menuItemMyprofile;
    Handler handler;

    TextView txtHeaderId;
    TextView txtHeaderNickname;
    ImageView imgProfile;
    Button btnFilter;
    int filterResult;
    int getStartIndex;
    String currentFilter;
    boolean filterChangeIs;

    String getNickname;
    public static String loginedUser;
    boolean autoLogin;

    BottomNavigationView bottomNavigationView;

    public static Activity activity = null;

    LoadingTask loadingTask;


    private HttpConnection httpConn = HttpConnection.getInstance();

    public static ArrayList<DataList_project_list> dataListProject;
    public static Adapter_projectList adapter_projectList;
    private RecyclerView recyProjectList;

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
                    Intent intentSubscribe = new Intent(ProjectListActivity.this,ProjectSubscribeActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    intentSubscribe.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intentSubscribe);
                    finish();
                    return true;
                case R.id.bottom_navigation_write:
                    setLog("바텀 네비게이션 : 글쓰기");
                    Intent intentWrite = new Intent(ProjectListActivity.this,ProjectWriteActivity.class);
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
        setLog("onCreate");
        setContentView(R.layout.activity_project_list);



        bottomNavigationView = findViewById(R.id.projectlist_bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        filterResult = 0;

        Toolbar toolbar = findViewById(R.id.toolbar_projectlist);

        btnFilter = findViewById(R.id.projectlist_button_filter);

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

        currentFilter = SORT_DATE_DOWN;



        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String [] items = {"최신 순","오래된 순","조회수 높은 순","좋아요 높은 순","내가 좋아요 한 게시물만"};
                AlertDialog.Builder builder = new AlertDialog.Builder(ProjectListActivity.this);
                builder.setTitle("필터 및 정렬");
                builder.setSingleChoiceItems(items, filterResult, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filterResult = which;
                        switch (which){
                            case 0:
                                currentFilter = SORT_DATE_DOWN;
                                filterResult = 0;
                                break;
                            case 1:
                                currentFilter = SORT_DATE_UP;
                                filterResult = 1;
                                break;
                            case 2:
                                currentFilter = SORT_VIEW;
                                filterResult = 2;
                                break;
                            case 3:
                                currentFilter = SORT_LIKE;
                                filterResult = 3;
                                break;
                            case 4:
                                currentFilter = SORT_MYLIKE;
                                filterResult = 4;
                                break;
                        }

                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setLog("보쟈 : "+filterResult);
                        getStartIndex = 0;
                        dataListProject.clear();
                        adapter_projectList.notifyDataSetChanged();
                        filterChangeIs = true;
                        GetProjectList(currentFilter,getStartIndex,HomeActivity.loginedUser,"http://13.124.223.128/board/getProjectList.php");
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        filterResult = 0;
                        setLog("보쟈2 : "+filterResult);
                    }
                });
                builder.create();
                builder.show();
            }
        });



        recyProjectList = findViewById(R.id.projectlist_recycler);
        recyProjectList.setLayoutManager(new LinearLayoutManager(ProjectListActivity.this));
        dataListProject = new ArrayList<>();

        adapter_projectList = new Adapter_projectList(ProjectListActivity.this,dataListProject);
        recyProjectList.setAdapter(adapter_projectList);
        //recyProjectList.getRecycledViewPool().setMaxRecycledViews();

        recyProjectList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),recyProjectList, new ProjectListActivity.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //선택한 아이템의 stream 데이터를 ViewStreamActivity로 넘겨준다.

                DataList_project_list dataListProjectList = dataListProject.get(position);

                setLog("넘기는 넘버 : "+dataListProjectList.getNumber());
                Intent intent = new Intent(ProjectListActivity.this, ViewProjectActivity.class);
                intent.putExtra("postNumber",String.valueOf(dataListProjectList.getNumber()));
                startActivity(intent);
                //finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        recyProjectList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(currentFilter == SORT_DATE_DOWN){
                    setLog("스크롤 중");
                    super.onScrolled(recyclerView, dx, dy);

                    int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    int itemTotalCount = recyclerView.getAdapter().getItemCount();
                    setLog("스크롤 : "+lastVisibleItemPosition);
                    setLog("스크롤2 : "+itemTotalCount);
                    if(lastVisibleItemPosition == itemTotalCount-1){
                        setLog("마지막 도착");
                        setLog("데이터 불러오기스크롤 : "+getStartIndex);
                        if(!filterChangeIs){
                            GetProjectList(currentFilter, getStartIndex,HomeActivity.loginedUser,"http://13.124.223.128/board/getProjectList.php");
                        }

                    }
                }

            }
        });

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
                    case REFRESH:
                        adapter_projectList.notifyItemChanged(dataListProject.size());
                        loadingTask.progressDialog.dismiss();
                        loadingTask.cancel(true);
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

        getStartIndex = 0;

        filterChangeIs = false;

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
        dataListProject.clear();
        adapter_projectList.notifyDataSetChanged();
        GetProjectList(currentFilter, getStartIndex,HomeActivity.loginedUser,"http://13.124.223.128/board/getProjectList.php");
        setLog("데이터 불러오기 최초 : "+getStartIndex);

    }

    private void sendData(final String param, final String url) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                httpConn.requestGetUserData(param, callback, url);
            }
        }.start();

    }
    private void GetProjectList(final String param, final int param2, final String currentUser,final String url) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
//        dataListProject.clear();
//        adapter_projectList.notifyDataSetChanged();
        loadingTask = new LoadingTask();
        loadingTask.execute();
        new Thread() {
            public void run() {
                httpConn.requestProjectList(param, param2,currentUser,callbackForProjectList, url);
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

    private final Callback callbackForProjectList = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            getStartIndex = getStartIndex + 10;
            filterChangeIs = false;
            setLog("서버에서 응답한 callbackForProjectList:"+body);
            setLog("파싱 추적");
            ArrayList<DataList_project_list> dataListSet = new ArrayList<>();
            try {
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
                    //dataListProject.add(dataSet);

                    dataListSet.add(dataSet);


                    setLog("파싱 데이터 추가 결과물 : "+dataListProject);



                    //adapter_projectList.notifyDataSetChanged();
                }
                //dataListProject.clear();
                dataListProject.addAll(dataListSet);
                Message messageRefresh = handler.obtainMessage();
                messageRefresh.what = REFRESH;
                handler.sendMessage(messageRefresh);
            } catch (JSONException e) {
                e.printStackTrace();
                setLog("파싱 추적10");
            }


        }
    };

    private class LoadingTask extends AsyncTask<Void,Void,Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setLog("onPreExecute");
            progressDialog = new ProgressDialog(ProjectListActivity.this);
            progressDialog.setMessage("데이터 불러오는 중...");
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            setLog("doInBackground");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            setLog("onPostExecute");
            if(progressDialog != null){
                progressDialog.dismiss();
            }

        }
    }

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
        }else if (id == R.id.nav_ar){
            Intent intent = new Intent(ProjectListActivity.this,ArActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_myactivity){
            Intent intent = new Intent(ProjectListActivity.this,UserChannelProjectActivity.class);
            intent.putExtra("writter",loginedUser);
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

    //recyclerview 터치시 이벤트 발생하는 기능 사용하려면 이 class가 있어야됨, 정확한 이유는 모름, 나중에
    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    //recyclerview 터치시 이벤트 발생하는 기능 사용하려면 이 class가 있어야됨, 정확한 이유는 모름, 나중에
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ProjectListActivity.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ProjectListActivity
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



//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = findViewById(R.id.drawer_layout_livelist);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }
}
