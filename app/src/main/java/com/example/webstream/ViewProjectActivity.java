package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
    implements View.OnClickListener , Serializable {

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
    public static final int TTT = 9;
    public static final int ADD_IMAGE = 10;
    public static final int LIKE_UP = 11;
    public static final int LIKE_DOWN = 12;
    public static final int GET_LIKE_LIST = 13;




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
    TextView txtLoading;

    ImageView imgMasterImg;         //게시글의 대표 이미지. 클릭시 전체보기
    ImageView imgUp;
    ImageView imgOption;

    LinearLayout linearContent;
    TextView addText;
    //ImageView addImage;
    String requestBody;
    int postNumber;
    String strOwner;

    ScrollView scrollView;
    String strPostNumber;

//    RecyclerView recyContent;       //게시글 내용이 들어가는 리사이클러뷰. 2가지 뷰타입이 있다. 이미지, 텍스트 본문
    private ArrayList<DataList_project_view> dataList_project_content;
//    private Adapter_projectView adapter_projectView;

    Boolean isLike=null;                 //게시글 좋아요 여부 확인하는 불린.

    private HttpConnection httpConn = HttpConnection.getInstance();
    Handler handler;
    Bitmap bitmap;
    Bitmap bitmapContent;

    int likeCount;
    LoadingTask loadingTask;
    String startPostNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_project);

        Intent intent = getIntent();
        startPostNumber= intent.getStringExtra("postNumber");
        setLog("받아온 포스트 넘버 : "+startPostNumber);


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
        txtLoading = findViewById(R.id.viewproject_loading);
        imgOption = findViewById(R.id.viewproject_image_option);
        imgMasterImg = findViewById(R.id.viewproject_image_masterimage);
//        recyContent = findViewById(R.id.viewproject_recycler_content);
        imgUp = findViewById(R.id.viewproject_image_up);
        linearContent = findViewById(R.id.viewproject_linear_content);
        //addImage = new ImageView(ViewProjectActivity.this);
        scrollView = findViewById(R.id.viewproject_scrollview);

        imgOption.setOnClickListener(this);
        txtWritter.setOnClickListener(this);
        imgLikeIcon.setOnClickListener(this);
        txtLike.setOnClickListener(this);
        imgMasterImg.setOnClickListener(this);
        imgUp.setOnClickListener(this);



        loadingTask = new LoadingTask();
        loadingTask.execute();
//        Thread getThread = new Thread(){
//            @Override
//            public void run() {
//                super.run();
//
//            }
//        };
//        getThread.start();
        getMyLikeList(startPostNumber);
        getBoardContent(startPostNumber);






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
                        strOwner = String.valueOf(msg.obj);
                        txtOwnerSecond.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_LOCATION:
                        txtLocation.setText(String.valueOf(msg.obj));
                        break;
                    case CHANGE_WRITTER:
                        txtWritter.setText(String.valueOf(msg.obj));
                        if(String.valueOf(msg.obj).equals(HomeActivity.loginedUser)){
                            imgOption.setVisibility(View.VISIBLE);
                        }
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
                        //linearContent.addView(addText);
//                        adapter_projectView.notifyItemChanged(dataList_project_content.size());
                        break;
                    case ADD_IMAGE:
                        //addImage.setImageBitmap(bitmapContent);
                        break;
                    case LIKE_UP:
                        likeCount++;
                        txtLikeCount.setText(String.valueOf(likeCount));
                        break;
                    case LIKE_DOWN:
                        likeCount--;
                        txtLikeCount.setText(String.valueOf(likeCount));
                        break;
                    case GET_LIKE_LIST:
                        setLog("어니냐1");
                        if(msg.obj.equals("1")){
                            setLog("어니냐2");
                            IsLike();
                        }else if(msg.obj.equals("0")){
                            setLog("어니냐3");
                            IsDisLike();
                        }
                        break;

                    case TTT:
                        try {
                            setLog("어니냐4");
                            JSONObject jsonContentDummy = new JSONObject(requestBody);
                            String dummyData = jsonContentDummy.getString("BoardContent");
                            JSONArray jsonArrayContentDumm = new JSONArray(dummyData);
                            JSONObject dummyDataYet = jsonArrayContentDumm.getJSONObject(0);
                            strPostNumber = dummyDataYet.getString("number");
                            String strPostTitle = dummyDataYet.getString("title");
                            String strPostOwner = dummyDataYet.getString("owner");
                            String strPostLocation = dummyDataYet.getString("location");
                            String strPostWritter = dummyDataYet.getString("writter");
                            String strPostWrittenDate = dummyDataYet.getString("writtenDate");
                            String strPostView = dummyDataYet.getString("view");
                            String strPostLike = dummyDataYet.getString("like");
                            likeCount = Integer.parseInt(strPostLike);

                            setLog("게시글 정보 strPostNumber : "+strPostNumber);
                            postNumber = Integer.parseInt(strPostNumber);
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
                                int contentOrientation = 0;
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

                                }
                                try{
                                    contentOrientation = Integer.parseInt(contentData.getString("orientation"));
                                    setLog("게시글 내용 contentOrientation : "+contentOrientation);
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
                                        setLog("으디보자1");
                                        try {
                                            mThread.join();
                                            setLog("으디보자2");
//                                            ExifInterface exif = new ExifInterface(String.valueOf(bitmap));
//                                            setLog("으디보자3");
//                                            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//                                            setLog("으디보자" + exifOrientation);
                                            if(contentOrientation==6){
                                                Matrix rotateMatrix = new Matrix();
                                                setLog("으디보자4");
                                                rotateMatrix.postRotate(90);
                                                setLog("으디보자5");
                                                bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),rotateMatrix,false);
                                                setLog("으디보자6");
                                            }


                                            imgMasterImg.setImageBitmap(bitmap);
                                            setLog("으디보자7");
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
                                try{
                                    contentDataList.setOrientation(contentOrientation);
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
                        final ArrayList<DataList_project_view> lastDataSet = new ArrayList<DataList_project_view>();
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
                            //dataList_project_content.add(lastDataSet.get(i));
                            //adapter_projectView.notifyItemChanged(dataList_project_content.size());
                            setLog("돌아가나 : "+i);

//                setLog("제발 : "+i+"   "+lastDataSet.get(i).getPosition());
//                setLog("제발 : "+i+"   "+lastDataSet.get(i).getName());
//                setLog("제발 : "+i+"   "+lastDataSet.get(i).getImgUrl());
//                setLog("제발 : "+i+"   "+lastDataSet.get(i).isMaster());

                            if(lastDataSet.get(i).getName()!=null){
                                setLog("텍스트 생겨라");
                                TextView addText = new TextView(ViewProjectActivity.this);
                                setLog("텍스트 생겨라2");
                                addText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//                                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) addText.getLayoutParams();
//                                layoutParams.leftMargin = 30;
//                                addText.setLayoutParams(layoutParams);
                                setLog("텍스트 생겨라3");
                                addText.setBackgroundColor(Color.parseColor("#00FFFFFF"));
                                setLog("텍스트 생겨라4");
                                addText.setPadding(50, 10, 50, 100);
                                addText.setTextColor(Color.parseColor("#000000"));
                                addText.setTextSize(18);
                                addText.setText(lastDataSet.get(i).getName());
                                setLog("텍스트 생겨라5");
                                linearContent.addView(addText);
                                setLog("텍스트 생겨라6");
                            }else {

                                final String imgUrl = lastDataSet.get(i).getImgUrl();
                                final int imgOrientation = lastDataSet.get(i).getOrientation();
                                setLog("이미지 생겨라 : "+imgUrl);
                                final ImageView addImage = new ImageView(ViewProjectActivity.this);
                                addImage.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                addImage.setBackgroundResource(R.drawable.loadingimag);
                                addImage.setAdjustViewBounds(true);
                                addImage.setPadding(0, 0, 0, 100);
                                final Bitmap[] contentBitmap = new Bitmap[1];
                                setLog("이미지 생겨라 1");
                                Thread mThread = new Thread(){
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            setLog("이미지 생겨라 2");
                                            URL url = new URL(imgUrl);
                                            setLog("이미지 생겨라 3");
                                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                            setLog("이미지 생겨라 4");
                                            conn.setDoInput(true);
                                            conn.connect();
                                            InputStream is = conn.getInputStream();
                                            setLog("이미지 생겨라 5");
                                            contentBitmap[0] = BitmapFactory.decodeStream(is);

                                            if(imgOrientation==6){
                                                Matrix rotateMatrix = new Matrix();
                                                setLog("으디보자4");
                                                rotateMatrix.postRotate(90);
                                                setLog("으디보자5");
                                                contentBitmap[0] = Bitmap.createBitmap(contentBitmap[0],0,0,contentBitmap[0].getWidth(),contentBitmap[0].getHeight(),rotateMatrix,false);
                                                setLog("으디보자6");
                                            }


                                            setLog("이미지 생겨라 6");

//                                            Message messageAddImg = handler.obtainMessage();
//                                            messageAddImg.what = ADD_IMAGE;
//                                            handler.sendMessage(messageAddImg);


                                            //addImage.setImageBitmap(contentBitmap[0]);
                                            setLog("이미지 생겨라 7");

                                            setLog("이미지 생겨라 8");
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
                                    addImage.setImageBitmap(contentBitmap[0]);
                                    setLog("으디보자7");
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                mThread.isInterrupted();
                                linearContent.addView(addImage);

                                setLog("이미지 생겨라5");
                            }




                        }

                        txtLoading.setVisibility(View.GONE);
                        loadingTask.progressDialog.dismiss();
                        loadingTask.cancel(true);
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
                if(HomeActivity.loginedUser.equals(txtWritter.getText().toString())){
                    //Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(ViewProjectActivity.this, UserChannelProjectActivity.class);
                    intent.putExtra("writter",txtWritter.getText().toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                }

                break;
            case R.id.viewproject_image_likeicon:
            case R.id.viewproject_text_like:
                setLog("좋아요 클릭");
                //사용자가 이 게시글을 좋아요 했었다면.
                if(isLike){
                    IsDisLike();


                    Message likeDown = handler.obtainMessage();
                    likeDown.what = LIKE_DOWN;
                    handler.sendMessage(likeDown);

                    touchLike(String.valueOf(postNumber),"DOWN");

                }else {
                    IsLike();

                    Message likeUp = handler.obtainMessage();
                    likeUp.what = LIKE_UP;
                    handler.sendMessage(likeUp);

                    touchLike(String.valueOf(postNumber),"UP");
                }

                break;
            case R.id.viewproject_image_masterimage:
                setLog("대표 이미지 클릭");
                //imgMasterImg.setImageBitmap(bitmap);
                break;
            case R.id.viewproject_image_up:
                setLog("가장 위로 클릭");
                //scrollView.scrollTo(0,0);
                scrollView.smoothScrollTo(0,0);
                break;
            case R.id.viewproject_image_option:
                setLog("옵션 클릭");
                final CharSequence[] items = {"수정하기","삭제하기"};
                AlertDialog.Builder selectDialog = new AlertDialog.Builder(ViewProjectActivity.this);
                selectDialog.setTitle("게시글 옵션");
                selectDialog.setItems(items,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Toast.makeText(ViewProjectActivity.this, which+"선택", Toast.LENGTH_SHORT).show();
                                switch (which){
                                    case 0:
                                        Intent intent = new Intent(ViewProjectActivity.this,ProjectModifyActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        //intent.putParcelableArrayListExtra("dataSet",dataList_project_content);
                                        intent.putExtra("postNumber",startPostNumber);
                                        setLog("확인 필요3 : "+startPostNumber);
                                        intent.putExtra("title",txtTitle.getText().toString());
                                        intent.putExtra("owner",strOwner);
                                        intent.putExtra("location",txtLocation.getText().toString());
                                        intent.putExtra("dataSet",dataList_project_content);
                                        startActivity(intent);
                                        finish();
                                        break;
                                    case 1:
                                        AlertDialog.Builder removeDialog = new AlertDialog.Builder(ViewProjectActivity.this);
                                        removeDialog.setMessage("이 글을 삭제 하시겠습니까?");
                                        removeDialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog2, int which) {
                                                RemovePost(strPostNumber);
                                                dialog2.dismiss();
                                            }
                                        });
                                        removeDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                dialog.dismiss();
                                            }
                                        });
                                        removeDialog.show();
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                selectDialog.show();
                break;

        }
    }
    private void IsDisLike(){
        txtLike.setTextColor(Color.parseColor("#727272"));
        txtLikeCount.setTextColor(Color.parseColor("#000000"));
        imgLikeIcon.setImageResource(R.drawable.ic_thumb_up_black_24dp);
        isLike = false;
    }
    private void IsLike(){
        txtLike.setTextColor(Color.parseColor("#257EF3"));
        txtLikeCount.setTextColor(Color.parseColor("#257EF3"));
        imgLikeIcon.setImageResource(R.drawable.ic_thumb_up_blue_24dp);
        isLike = true;
    }
    private class LoadingTask extends AsyncTask<Void,Void,Void>{
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setLog("onPreExecute");
            progressDialog = new ProgressDialog(ViewProjectActivity.this);
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
    protected void onPause() {
        super.onPause();
//        Intent intent = new Intent(ViewProjectActivity.this,ProjectListActivity.class);
//        startActivity(intent);
//        finish();
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
    private void getMyLikeList(final String postNumber) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/board/getMyLikeList.php";
        new Thread() {
            public void run() {
                httpConn.requestLikeList(postNumber,likeListCallback, url);
            }
        }.start();
    }

    private void touchLike(final String postNumber, final String flag) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/board/boardLike.php";
        new Thread() {
            public void run() {
                httpConn.requestLike(postNumber,flag,likeCallback, url);
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
            requestBody = response.body().string();
            setLog("서버에서 응답한 Bodycallback:"+requestBody);

            Message messageTest = handler.obtainMessage();
            messageTest.what = TTT;
            handler.sendMessage(messageTest);
            setLog("서버에서 메세지보냄callback");

        }
    };

    private final Callback likeCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            String requestlike = response.body().string();
            setLog("서버에서 응답한 BodylikeCallback:"+requestlike);
        }
    };
    private final Callback likeListCallback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            String requestLike = response.body().string();
            setLog("서버에서 응답한 BodylikeListCallback:"+requestBody);

            Message messageLikeList = handler.obtainMessage();
            messageLikeList.what = GET_LIKE_LIST;
            messageLikeList.obj = requestLike;
            handler.sendMessage(messageLikeList);
            setLog("서버에서 메세지보냄likeListCallback");

        }
    };
    private void RemovePost(final String postNumber) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/board/removeBoard.php";
        new Thread() {
            public void run() {
                httpConn.requestRemovePost(postNumber,callbackRemovePost, url);
            }
        }.start();
    }
    private final Callback callbackRemovePost = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            setLog( "콜백오류:"+e.getMessage());
        }
        @Override
        public void onResponse(Call call, okhttp3.Response response) throws IOException {
            requestBody = response.body().string();
            setLog("서버에서 응답한 Bodycallback:"+requestBody);

            finish();
        }
    };

}
