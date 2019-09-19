package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import adapter.Adapter_liveList;
import adapter.Adapter_projectView;
import dataList.DataList_liveList;
import dataList.DataList_project_view;
import dataList.DataList_project_write;
import okhttp3.Call;
import okhttp3.Callback;

public class ViewProjectActivity extends AppCompatActivity
    implements View.OnClickListener {

    private static final String TAG = "ViewProjectActivity";
    public void setLog(String content){
        Log.e(TAG,content);}

    public static final int CHANGE_TITLE = 0;
    public static final int CHANGE_OWNER = 1;
    public static final int CHANGE_OWNER_SECOND = 2;
    public static final int CHANGE_LOCATION = 3;
    public static final int CHANGE_WRITTER = 4;
    public static final int CHANGE_WRITTENDATE = 5;
    public static final int CHANGE_VIEW = 6;
    public static final int CHANGE_LIKE = 7;
    public static final int ADD_TEXTVIEW = 8;


    TextView txtWritter;            //게시글 작성자. 클릭 시 작성자의 활동 목록을 보여준다.
    ImageView imgLikeIcon;          //좋아요 이미지 아이콘. 클릭 시 이 게시글을 좋아요 클릭한 적이 없다면 아이콘의 색이 파란색으로, 좋아요 클린한 적이 있다면 원래색 회색으로 바뀐다.
    TextView txtLike;               //좋아요 텍스트. 클릭 시 이 게시글을 좋아요 클릭한 적이 없다면 텍스트의 색이 파란색으로, 좋아요 클린한 적이 있다면 원래색 회색으로 바뀐다.
    TextView txtLikeCount;          //좋아요 카운트 수 텍스트. 클릭 시 이 게시글을 좋아요 클릭한 적이 없다면 텍스트의 색이 파란색으로, 좋아요 클린한 적이 있다면 원래색 회색으로 바뀐다.
    TextView txtTitle;
    TextView txtOwner;
    TextView txtWrittenDate;
    TextView txtViewCount;
    TextView txtOwnerSecond;
    TextView txtLocation;

    ImageView imgMasterImg;         //게시글의 대표 이미지. 클릭시 전체보기
    ImageView imgUp;

    LinearLayout linearContent;
    TextView addText;



//    RecyclerView recyContent;       //게시글 내용이 들어가는 리사이클러뷰. 2가지 뷰타입이 있다. 이미지, 텍스트 본문
    private ArrayList<DataList_project_view> dataList_project_content;
//    private Adapter_projectView adapter_projectView;

    Boolean isLike;                 //게시글 좋아요 여부 확인하는 불린.

    private HttpConnection httpConn = HttpConnection.getInstance();
    Handler handler;
    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        txtTitle = findViewById(R.id.viewproject_text_title);
        txtOwner = findViewById(R.id.viewproject_text_owner);
        txtWrittenDate = findViewById(R.id.viewproject_text_datetime);
        txtViewCount = findViewById(R.id.viewproject_text_viewcount);
        txtOwnerSecond = findViewById(R.id.viewproject_text_owner_second);
        txtLocation = findViewById(R.id.viewproject_text_location);
        txtWritter = findViewById(R.id.viewproject_text_writter);
        imgLikeIcon = findViewById(R.id.viewproject_image_likeicon);
        txtLike = findViewById(R.id.viewproject_text_like);
        txtLikeCount = findViewById(R.id.viewproject_text_likecount);
        imgMasterImg = findViewById(R.id.viewproject_image_masterimage);
        //recyContent = findViewById(R.id.viewproject_recycler_content);
        imgUp = findViewById(R.id.viewproject_image_up);
        linearContent = findViewById(R.id.viewproject_linear_content);

        txtWritter.setOnClickListener(this);
        imgLikeIcon.setOnClickListener(this);
        txtLike.setOnClickListener(this);
        imgMasterImg.setOnClickListener(this);

        isLike = false;

        getBoardContent("27");

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CHANGE_TITLE:
                        txtTitle.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_OWNER:
                        txtOwner.setText("  /  "+String.valueOf(msg.obj));
                        txtOwnerSecond.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_LOCATION:
                        txtLocation.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_WRITTER:
                        txtWritter.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_WRITTENDATE:
                        txtWrittenDate.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_VIEW:
                        txtViewCount.setText(String.valueOf(msg.obj));
                        break;

                    case CHANGE_LIKE:
                        txtLikeCount.setText(String.valueOf(msg.obj));
                        break;
                    case ADD_TEXTVIEW:
                        linearContent.addView(addText);
                        break;
                }
            }
        };

//        recyContent.setLayoutManager(new LinearLayoutManager(ViewProjectActivity.this));
        dataList_project_content = new ArrayList<>();
//
//        adapter_projectView = new Adapter_projectView(ViewProjectActivity.this,dataList_project_content);
//        recyContent.setAdapter(adapter_projectView);


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.viewproject_text_writter:
                setLog("작성자 클릭");
                break;
            case R.id.viewproject_image_likeicon:
            case R.id.viewproject_text_like:
                setLog("좋아요 클릭");
                //사용자가 이 게시글을 좋아요 했었다면.
                if(isLike){
                    txtLike.setTextColor(Color.parseColor("#727272"));
                    txtLikeCount.setTextColor(Color.parseColor("#000000"));
                    imgLikeIcon.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                    isLike = false;
                }else {
                    txtLike.setTextColor(Color.parseColor("#257EF3"));
                    txtLikeCount.setTextColor(Color.parseColor("#257EF3"));
                    imgLikeIcon.setImageResource(R.drawable.ic_thumb_up_blue_24dp);
                    isLike = true;
                }

                break;
            case R.id.viewproject_image_masterimage:
                setLog("대표 이미지 클릭");
                break;
            case R.id.viewproject_image_up:
                setLog("가장 위로 클릭");
                break;

        }
    }
    private void getBoardContent(final String postNumber) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/board/getBoardContent.php";
        new Thread() {
            public void run() {
                httpConn.requestGetBoard(postNumber,callback, url);
            }
        }.start();
    }
    private final Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            String body = response.body().string();
            setLog("서버에서 응답한 Body:"+body);

            try {
                JSONObject jsonContentDummy = new JSONObject(body);
                String dummyData = jsonContentDummy.getString("BoardContent");
                JSONArray jsonArrayContentDumm = new JSONArray(dummyData);
                JSONObject dummyDataYet = jsonArrayContentDumm.getJSONObject(0);
                String strPostNumber = dummyDataYet.getString("number");
                String strPostTitle = dummyDataYet.getString("title");
                String strPostOwner = dummyDataYet.getString("owner");
                String strPostLocation = dummyDataYet.getString("location");
                String strPostWritter = dummyDataYet.getString("writter");
                String strPostWrittenDate = dummyDataYet.getString("writtenDate");
                String strPostView = dummyDataYet.getString("view");
                String strPostLike = dummyDataYet.getString("like");

                setLog("게시글 정보 strPostNumber : "+strPostNumber);
                setLog("게시글 정보 strPostTitle : "+strPostTitle);
                setLog("게시글 정보 strPostOwner : "+strPostOwner);
                setLog("게시글 정보 strPostLocation : "+strPostLocation);
                setLog("게시글 정보 strPostWritter: "+strPostWritter);
                setLog("게시글 정보 strPostWrittenDate: "+strPostWrittenDate);
                setLog("게시글 정보 strPostView: "+strPostView);
                setLog("게시글 정보 strPostLike: "+strPostLike);
                String[] splitWrittenDate = strPostWrittenDate.split(" ");

                Message messageTitle = handler.obtainMessage();
                messageTitle.what = CHANGE_TITLE;
                messageTitle.obj = strPostTitle;
                handler.sendMessage(messageTitle);

                Message messageOwner = handler.obtainMessage();
                messageOwner.what = CHANGE_OWNER;
                messageOwner.obj = strPostOwner;
                handler.sendMessage(messageOwner);

                Message messageLocation = handler.obtainMessage();
                messageLocation.what = CHANGE_LOCATION;
                messageLocation.obj = strPostLocation;
                handler.sendMessage(messageLocation);

                Message messageWritter = handler.obtainMessage();
                messageWritter.what = CHANGE_WRITTER;
                messageWritter.obj = strPostWritter;
                handler.sendMessage(messageWritter);

                Message messageWrittenDate = handler.obtainMessage();
                messageWrittenDate.what = CHANGE_WRITTENDATE;
                messageWrittenDate.obj = splitWrittenDate[0];
                handler.sendMessage(messageWrittenDate);

                Message messageView = handler.obtainMessage();
                messageView.what = CHANGE_VIEW;
                messageView.obj = strPostView;
                handler.sendMessage(messageView);

                Message messageLike = handler.obtainMessage();
                messageLike.what = CHANGE_LIKE;
                messageLike.obj = strPostLike;
                handler.sendMessage(messageLike);

                setLog("추적1");
                JSONArray jsonArrayContentDummyYet = new JSONArray(dummyDataYet.getString("content"));
                for(int i=0;i<jsonArrayContentDummyYet.length();i++){
                    setLog("추적2");
                    JSONObject contentData = jsonArrayContentDummyYet.getJSONObject(i);
                    int contentPosition = 0;
                    String contentText= null;
                    String contentImageUrl = null;
                    Boolean contentMaster = null;
                    setLog("추적3");
                    try{
                        contentPosition = Integer.parseInt(contentData.getString("position"));
                        setLog("게시글 위치 contentPosition : "+contentPosition);
                        setLog("추적4");
                    }catch (Exception e){

                    }
                    try{
                        contentText = contentData.getString("text");
                        setLog("게시글 내용 contentText : "+contentText);
                        setLog("추적5");
                    }catch (Exception e){

                    }try{
                        contentImageUrl = contentData.getString("imagePath");
                        setLog("게시글 이미지 contentImageUrl : "+contentImageUrl);
                        setLog("추적6");
                    }catch (Exception e){

                    }try{
                        contentMaster = Boolean.valueOf(contentData.getString("isMaster"));
                        setLog("게시글 대표 contentMaster : "+contentMaster);
                        setLog("추적7");
                        if(contentMaster){
                            final String finalContentImageUrl = contentImageUrl;
                            Thread mThread = new Thread(){
                                @Override
                                public void run() {
                                    super.run();
                                    try {
                                        URL url = new URL(finalContentImageUrl);
                                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                        conn.setDoInput(true);
                                        conn.connect();
                                        InputStream is = conn.getInputStream();
                                        bitmap = BitmapFactory.decodeStream(is);
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            mThread.start();

                            try {
                                mThread.join();
                                imgMasterImg.setImageBitmap(bitmap);
                                /*Glide.with(ViewProjectActivity.this)
                                        .load(bitmap)
                                        .apply(new RequestOptions().centerCrop())
                                        .into(imgMasterImg);*/
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mThread.isInterrupted();
                        }
                    }catch (Exception e){

                    }
                    DataList_project_view contentDataList = new DataList_project_view();
                    try{
                        contentDataList.setPosition(contentPosition);
                    }catch (Exception e){

                    }
                    try{
                        contentDataList.setName(contentText);
                    }catch (Exception e){

                    }
                    try{
                        contentDataList.setImgUrl(contentImageUrl);
                    }catch (Exception e){

                    }
                    try{
                        contentDataList.setMaster(contentMaster);
                    }catch (Exception e){

                    }

                    dataList_project_content.add(contentDataList);
//                    dataList_project_content.add(contentDataList);
//                    adapter_projectView.notifyItemChanged(dataList_project_content.size());



                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //
            ArrayList<DataList_project_view> lastDataSet = new ArrayList<DataList_project_view>();
            for(int i=0;i<dataList_project_content.size();i++){
                for(int k=0;k<dataList_project_content.size();k++){
                    if(dataList_project_content.get(k).getPosition()==i){
                        setLog("비교 : "+i);
                        setLog("비교 : "+dataList_project_content.get(k).getPosition());

                        lastDataSet.add(dataList_project_content.get(k));
                    }
                }

            }
            for(int i=0;i<lastDataSet.size();i++){
                setLog("제발 : "+i+"   "+lastDataSet.get(i).getPosition());
                setLog("제발 : "+i+"   "+lastDataSet.get(i).getName());
                setLog("제발 : "+i+"   "+lastDataSet.get(i).getImgUrl());
                setLog("제발 : "+i+"   "+lastDataSet.get(i).isMaster());

                if(lastDataSet.get(i).getName()!=null){
                    setLog("텍스트 생겨라");
                    TextView addText = new TextView(ViewProjectActivity.this);
                    setLog("텍스트 생겨라2");
                    addText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    setLog("텍스트 생겨라3");
                    addText.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                    setLog("텍스트 생겨라4");
                    addText.setPadding(20, 10, 10, 10);
                    addText.setTextColor(Color.parseColor("#000000"));
                    addText.setTextSize(30);
                    addText.setText(lastDataSet.get(i).getName());
                    setLog("텍스트 생겨라5");
//                    Message messageAddText = handler.obtainMessage();
//                    messageAddText.what = ADD_TEXTVIEW;
//                    handler.sendMessage(messageAddText);
                    linearContent.addView(addText);
                    setLog("텍스트 생겨라6");
                }else {
                    setLog("이미지 생겨라");
                    TextView addText = new TextView(ViewProjectActivity.this);
                    setLog("이미지 생겨라2");
                    addText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    setLog("이미지 생겨라3");
                    addText.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                    addText.setPadding(20, 10, 10, 10);
                    addText.setTextColor(Color.parseColor("#000000"));
                    addText.setTextSize(30);
                    addText.setText("뛰뛰뛰뛰뛰뛰");
                    setLog("이미지 생겨라4");
//                    Message messageAddText = handler.obtainMessage();
//                    messageAddText.what = ADD_TEXTVIEW;
//                    handler.sendMessage(messageAddText);
                    linearContent.addView(addText);
                    setLog("이미지 생겨라5");
                }


//                LinearLayout linearContent = findViewById(R.id.viewproject_linear_content);
//                TextView topTV1 = new TextView(ViewProjectActivity.this);
//                topTV1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                topTV1.setBackgroundColor(Color.parseColor("#00FFFFFF"));
//                topTV1.setPadding(20, 10, 10, 10);
//                topTV1.setTextColor(Color.parseColor("#FF7200"));
//                topTV1.setTextSize(13);
//                topTV1.setText("텍스트");
//                linearContent.addView(topTV1);


            }



        }
    };
}
