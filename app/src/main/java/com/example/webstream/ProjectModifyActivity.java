package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import adapter.Adapter_projectWriteList;
import dataList.DataList_project_view;
import dataList.DataList_project_write;
import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProjectModifyActivity extends AppCompatActivity
        implements  Adapter_projectModifyList.OnStartDragListener {

    private static final String TAG = "ProjectModifyActivity";
    public void setLog(String content){
        Log.e(TAG,content);}

    Button btnCancel;                   //취소 버튼. 글쓰기 액티비티 종료됨
    Button btnAddImage;                 //이미지 추가버튼.
    Button btnComplete;                 //작성 완료버튼. 작성한 게시글 내용 서버에 업로드. 작성해야될 내용 1개라도 null이면 버튼 클릭 시 토스트 메세지 출력
    TextInputEditText etxtTitle;        //프로젝트명 입력받는 edittext
    TextInputEditText etxtOwner;        //건축주나 소유자 입력받는 edittext
    TextInputEditText etxtLocation;     //프로젝트의 위치 입력받는 edittext
    TextView txtContent;                //내용 텍스트뷰. 클릭 시 글쓰기 리사이클러뷰에 텍스트 아이템 추가

    public RecyclerView recyProjectModifyList;
    public static ArrayList<DataList_project_view> modifyDataListBefore;
    public static ArrayList<DataList_project_write> modifyDataListAfter;
    public static Adapter_projectModifyList adapter_projectModify;

    ItemTouchHelper itemTouchHelper;

    public static boolean masterIs = false;           //대표이미지

    String strTitle;
    String strOwner;
    String strLocation;
    List<Uri> selectedUriList = null;
    String postNumber;

    JSONObject jsonPost;
    JSONArray contentArray;
    JSONObject jsonContent;

    int isLastUpload;

    String imagePath ;

    private HttpConnection httpConn = HttpConnection.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_modify);

        masterIs = true;

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.ProjectModifyActivity_button_cancel:
                        setLog("취소버튼");
                        finish();
                        break;
                    case R.id.ProjectModifyActivity_button_addimage:
                        setLog("이미지추가버튼");
                        TedBottomPicker.with(ProjectModifyActivity.this)
                                .setPeekHeight(1600)
                                .showTitle(false)
                                .setCompleteButtonText("완료")
                                .setEmptySelectionText("선택된 사진 없음")
                                .setSelectedUriList(selectedUriList)
                                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                                    @Override
                                    public void onImagesSelected(List<Uri> uriList) {
                                        Log.e("ddd",uriList.toString());
                                        // here is selected image uri list
                                        for(int i = 0; i<uriList.size(); i++){
                                            DataList_project_write ImageDataList = new DataList_project_write();
                                            //String path = "/storage/emulated/0/Pictures/JPEG_20190917103957_8806806323473041896.jpg";
                                            ImageDataList.setImgUri(uriList.get(i));
                                            setLog("현재 대표사진 여부 : "+masterIs);
                                            if(i == 0 && masterIs == false){
                                                ImageDataList.setMaster(true);
                                                masterIs = true;
                                            }
                                            modifyDataListAfter.add(ImageDataList);
                                            setLog("대표 이미지 점검 "+i+"번째 : "+ImageDataList.isMaster());
                                            adapter_projectModify.notifyItemChanged(modifyDataListAfter.size());
                                        }

                                    }
                                });
                        break;
                    case R.id.ProjectModifyActivity_button_complete:
                        if(etxtTitle.getText().toString().equals("")) {
                            etxtTitle.requestFocus();
                            Toast.makeText(ProjectModifyActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                            setLog("내용 : " + etxtTitle.getText());
                            setLog("타이틀 빈칸");
                        }else if(etxtOwner.getText().toString().equals("")){
                            etxtOwner.requestFocus();
                            Toast.makeText(ProjectModifyActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }else if(etxtLocation.getText().toString().equals("")){
                            etxtLocation.requestFocus();
                            Toast.makeText(ProjectModifyActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }else if(modifyDataListAfter.size()==0){
                            Toast.makeText(ProjectModifyActivity.this, "게시글 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            jsonPost = new JSONObject();
                            try {
                                jsonPost.put("title",etxtTitle.getText().toString());
                                jsonPost.put("owner",etxtOwner.getText().toString());
                                jsonPost.put("location",etxtLocation.getText().toString());
                                jsonPost.put("writter",HomeActivity.loginedUser);
                                setLog("확인 필요2 : "+postNumber);
                                jsonPost.put("postNumber",postNumber);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Boolean imgOn = false;
                            for(int i=0;i<modifyDataListAfter.size();i++){
                                if(modifyDataListAfter.get(i).getImgUri()==null){

                                }else {
                                    imgOn = true;
                                }
                            }

                            if(imgOn==false){
                                Toast.makeText(ProjectModifyActivity.this, "한 장 이상의 이미지를 추가해야 됩니다.", Toast.LENGTH_SHORT).show();
                            }else {
                                contentArray = new JSONArray();
                                for(int i=0;i<modifyDataListAfter.size();i++){
                                    setLog(i+"번째 리스트 네임 : "+modifyDataListAfter.get(i).getName());
                                    setLog(i+"번째 리스트 이미지 : "+modifyDataListAfter.get(i).getImgUri());
                                    setLog(i+"번째 리스트 대표 : "+String.valueOf(modifyDataListAfter.get(i).isMaster()));
                                    setLog(i+"번째 리스트 수정 : "+String.valueOf(modifyDataListAfter.get(i).isAlready()));
                                    setLog("   ");
                                    setLog("   ");
                                    String lastItem = "false";
                                    jsonContent= new JSONObject();
                                    if(modifyDataListAfter.size()-1 == i){
                                        lastItem = "true";
                                    }
                                    try {
                                        if (modifyDataListAfter.get(i).getImgUri() != null && modifyDataListAfter.get(i).isAlready()==false){
                                            setLog(i+"번째 리스트 이미지 업로드 체크: "+modifyDataListAfter.get(i).getImgUri().getPath());
                                            String[] filePathColumn = {MediaStore.Images.Media.DATA};

                                            ExifInterface exif = null;
                                            try{
                                                exif = new ExifInterface(modifyDataListAfter.get(i).getImgUri().getPath());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
                                            setLog("ddddddd : "+ orientation);

                                            String ImageUploadURL = "http://"+HomeActivity.singletonData.ipAppData+"/uploadImg/boardImg/uploadBoardImage.php";
                                            new ImageUploadTask().execute(
                                                    ImageUploadURL,
                                                    modifyDataListAfter.get(i).getImgUri().getPath(),
                                                    String.valueOf(i),
                                                    String.valueOf(modifyDataListAfter.get(i).isMaster()),
                                                    lastItem,
                                                    String.valueOf(orientation));
                                            Cursor cursor = getContentResolver().query(modifyDataListAfter.get(i).getImgUri(), filePathColumn, null, null, null);
                                            if(cursor != null){
                                                cursor.moveToFirst();
                                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                                imagePath = cursor.getString(columnIndex);
                                                setLog("이미지경로 : "+imagePath);
                                            }else {

                                            }
                                        }else if(modifyDataListAfter.get(i).getImgUri() != null && modifyDataListAfter.get(i).isAlready()==true){
                                            JSONObject jsonObject = new JSONObject();
                                            jsonObject.put("position",String.valueOf(i));
                                            jsonObject.put("imagePath",modifyDataListAfter.get(i).getImgUri());
                                            jsonObject.put("isMaster",String.valueOf(modifyDataListAfter.get(i).isMaster()));
                                            jsonObject.put("orientation",modifyDataListAfter.get(i).getOrientation());
                                            contentArray.put(jsonObject);
                                            isLastUpload++;
                                        }
                                        else {
                                            jsonContent.put("position",i);
                                            jsonContent.put("text",modifyDataListAfter.get(i).getName());
                                            isLastUpload++;
                                            contentArray.put(jsonContent);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        break;
                    case R.id.ProjectModifyActivity_textview_content:
                        setLog("내용텍스트");
                        DataList_project_write TextDataList = new DataList_project_write();
                        TextDataList.setName("");
                        TextDataList.setPosition(modifyDataListAfter.size());
                        modifyDataListAfter.add(TextDataList);
                        adapter_projectModify.notifyItemChanged(modifyDataListAfter.size());
                        //adapter_projectWrite.notifyDataSetChanged();
                        break;
                }
            }
        };


        modifyDataListBefore = new ArrayList<>();
        modifyDataListAfter = new ArrayList<>();

        btnCancel = findViewById(R.id.ProjectModifyActivity_button_cancel);
        btnAddImage = findViewById(R.id.ProjectModifyActivity_button_addimage);
        btnComplete = findViewById(R.id.ProjectModifyActivity_button_complete);
        etxtTitle = findViewById(R.id.ProjectModifyActivity_edittext_title);
        etxtOwner = findViewById(R.id.ProjectModifyActivity_edittext_owner);
        etxtLocation = findViewById(R.id.ProjectModifyActivity_edittext_location);
        txtContent = findViewById(R.id.ProjectModifyActivity_textview_content);
        int isLastUpload = 0;

        btnCancel.setOnClickListener(onClickListener);
        btnAddImage.setOnClickListener(onClickListener);
        btnComplete.setOnClickListener(onClickListener);
        txtContent.setOnClickListener(onClickListener);

        Intent intent = getIntent();
        strTitle = intent.getStringExtra("title");
        strOwner = intent.getStringExtra("owner");
        postNumber = intent.getStringExtra("postNumber");
        setLog("확인 필요1 : "+postNumber);
        strLocation = intent.getStringExtra("location");
        modifyDataListBefore = (ArrayList<DataList_project_view>) intent.getSerializableExtra("dataSet");

        etxtTitle.setText(strTitle);
        etxtOwner.setText(strOwner);
        etxtLocation.setText(strLocation);


        for(int i =0;i<modifyDataListBefore.size();i++){
            DataList_project_write dataSet = new DataList_project_write();
            dataSet.setAlready(true);
            try {
                dataSet.setName(modifyDataListBefore.get(i).getName());
            }catch (Exception e){}
            try {
                dataSet.setPosition(modifyDataListBefore.get(i).getPosition());
            }catch (Exception e){}
            try {
                dataSet.setImgUri(Uri.parse(modifyDataListBefore.get(i).getImgUrl()));
            }catch (Exception e){}
            try {
                dataSet.setMaster(modifyDataListBefore.get(i).isMaster());
            }catch (Exception e){}
            try {
                dataSet.setOrientation(modifyDataListBefore.get(i).getOrientation());
            }catch (Exception e){}

            modifyDataListAfter.add(dataSet);
        }

        setLog("잘 받아왔나"+modifyDataListBefore.get(0).getName());




        adapter_projectModify = new Adapter_projectModifyList(ProjectModifyActivity.this,modifyDataListAfter,this);
        recyProjectModifyList = findViewById(R.id.ProjectModifyActivity_recyclerview);
        recyProjectModifyList.setLayoutManager(new LinearLayoutManager(ProjectModifyActivity.this));

        writeItemTouchHelperCallback writeItemTouchHelperCallback = new writeItemTouchHelperCallback(adapter_projectModify);
        itemTouchHelper = new ItemTouchHelper(writeItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyProjectModifyList);

        recyProjectModifyList.setAdapter(adapter_projectModify);

        adapter_projectModify.notifyDataSetChanged();
    }
    private class ImageUploadTask extends AsyncTask<String,Integer,Boolean> {
        ProgressDialog progressDialog;
        boolean isLast;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ProjectModifyActivity.this);
            progressDialog.setMessage("업로드 중...");
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                setLog("마지막 값 : "+strings[4]);
                isLast = true;
//                if(strings[4].equals("true")){
//                    isLast = true;
//                }else {
//                    isLast = false;
//                }
//                setLog("마지막 결과 : "+isLast);

                JSONObject jsonObject = JSONParser.uploadImage(strings[0],strings[1]);
                if (jsonObject != null){
                    String imgPath = "http://"+HomeActivity.singletonData.ipAppData+"/uploadImg/boardImg/"+jsonObject.getString("result");
                    JSONObject jsonImage = new JSONObject();
                    jsonImage.put("position",strings[2]);
                    jsonImage.put("imagePath",imgPath);
                    jsonImage.put("isMaster",strings[3]);
                    jsonImage.put("orientation",strings[5]);
                    contentArray.put(jsonImage);




                    //return jsonObject.getString("result").equals("success");
                }


            } catch (JSONException e) {
                Log.i("TAG", "Error : " + e.getLocalizedMessage());
            }
            return false;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            isLastUpload++;
            setLog("마지막이다 확인 : "+isLastUpload);
            setLog("마지막이다 위해 : "+modifyDataListAfter.size());
            if(progressDialog != null){
                progressDialog.dismiss();
            }
            if(isLastUpload == modifyDataListAfter.size()){
                setLog("마지막이다");

                setLog("결과물" + jsonPost);
                setLog("결과물2" + contentArray);
                sendData(jsonPost,contentArray);



            }else {
                //Toast.makeText(ProjectWriteActivity.this, "한 장 이상의 이미지를 추가해주세요.", Toast.LENGTH_SHORT).show();
            }
            if(isLast){



//                try {
//                    jsonPost.put("postContent",contentArray);
//                    setLog("결과물" + jsonPost);
//                    sendData(jsonPost);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    setLog(e.toString());
//                }
            }else {
                //setLog("마지막인줄 ");
            }
            //setLog("결과물" + contentArray);

        }
    }

    //게시글 정보 저장 하는 메서드 인자값[0]은 게시글 기본 정보(제목, 작성자, 위치, 소유자등) 인자값[1]은 게시글 내용.
    private void sendData(final JSONObject jsonContent, final JSONArray jsonArrayContent) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://"+HomeActivity.singletonData.ipAppData+"/board/modifyBoardContent.php";
        new Thread() {
            public void run() {
                httpConn.requestBoardWrite(jsonContent.toString(), jsonArrayContent.toString(),callback, url);
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

            Intent intent = new Intent(ProjectModifyActivity.this,ViewProjectActivity.class);
            intent.putExtra("postNumber",body);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            setLog("보내는 포스트 넘버 : "+body);
            startActivity(intent);
            finish();


        }
    };

    //이미지 Okhttp 로 업로드
    public static class JSONParser {

        public static JSONObject uploadImage(String imageUploadUrl, String sourceImageFile) {
            try {
                File sourceFile = new File(sourceImageFile);
                Log.e(TAG, "File...::::" + sourceFile + " : " + sourceFile.exists());
                final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
                String filename = sourceImageFile.substring(sourceImageFile.lastIndexOf("/") + 1);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("uploaded_file", filename, RequestBody.create(MEDIA_TYPE_PNG, sourceFile))
                        .addFormDataPart("result", "photo_image")
                        .build();

                //Log.e(TAG, "추정 1");

                Request request = new Request.Builder()
                        .url(imageUploadUrl)
                        .post(requestBody)
                        .build();
                //Log.e(TAG, "추정 2");
                OkHttpClient client = new OkHttpClient();
                //Log.e(TAG, "추정 3");
                Response response = client.newCall(request).execute();
                //Log.e(TAG, "추정 4");
                String res = response.body().string();
                //Log.e(TAG, "추정 5");
                Log.e(TAG, "Error: " + res);
                return new JSONObject(res);

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                Log.e(TAG, "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e(TAG, "Other Error: " + e.getLocalizedMessage());
            }
            return null;
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    }
}

