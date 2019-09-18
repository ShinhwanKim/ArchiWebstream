package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.TextUtils;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import adapter.Adapter_liveList;
import adapter.Adapter_projectWriteList;
import dataList.DataList_liveList;
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

public class ProjectWriteActivity extends AppCompatActivity
    implements Adapter_projectWriteList.OnStartDragListener{

    private static final String TAG = "ProjectWriteActivity";
    public void setLog(String content){
        Log.e(TAG,content);}

    Button btnCancel;                   //취소 버튼. 글쓰기 액티비티 종료됨
    Button btnAddImage;                 //이미지 추가버튼.
    Button btnComplete;                 //작성 완료버튼. 작성한 게시글 내용 서버에 업로드. 작성해야될 내용 1개라도 null이면 버튼 클릭 시 토스트 메세지 출력
    TextInputEditText etxtTitle;        //프로젝트명 입력받는 edittext
    TextInputEditText etxtOwner;        //건축주나 소유자 입력받는 edittext
    TextInputEditText etxtLocation;     //프로젝트의 위치 입력받는 edittext
    TextView txtContent;                //내용 텍스트뷰. 클릭 시 글쓰기 리사이클러뷰에 텍스트 아이템 추가

    public RecyclerView recyProjectWriteList;
    public static ArrayList<DataList_project_write> writeDataList;
    public static Adapter_projectWriteList adapter_projectWrite;

    ItemTouchHelper itemTouchHelper;

    List<Uri> selectedUriList = null;


    public static boolean masterIs = false;           //대표이미지

    private HttpConnection httpConn = HttpConnection.getInstance();

    String imagePath ;

    JSONObject jsonPost;
    JSONArray contentArray;
    JSONObject jsonContent;

    Bitmap rotatedImg;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_write);


        btnCancel = findViewById(R.id.ProjectWirteActivity_button_cancel);
        btnAddImage = findViewById(R.id.ProjectWirteActivity_button_addimage);
        btnComplete = findViewById(R.id.ProjectWirteActivity_button_complete);
        etxtTitle = findViewById(R.id.ProjectWirteActivity_edittext_title);
        etxtOwner = findViewById(R.id.ProjectWirteActivity_edittext_owner);
        etxtLocation = findViewById(R.id.ProjectWirteActivity_edittext_location);
        txtContent = findViewById(R.id.ProjectWirteActivity_textview_content);

        //------------------------------뷰 클릭 리스너 정의----------------------------
        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (view.getId()){

//취소 버튼. 클릭 시 현재 액티비티 종료
                    case R.id.ProjectWirteActivity_button_cancel:
                        setLog("취소버튼");
                        break;

//이미지 추가버튼. 클릭 시 TedBottomPicker실행하여 선택한 이미지 url 가져온다.
                    case R.id.ProjectWirteActivity_button_addimage:
                        setLog("이미지추가버튼");

                        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                        //사진을 여러개 선택할수 있도록 한다
                        //intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        //intent.setType("image/*");
                        //startActivityForResult(Intent.createChooser(intent, "Select Picture"),  1);




                        TedBottomPicker.with(ProjectWriteActivity.this)
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
                                            if(i == 0 && masterIs == false){
                                                ImageDataList.setMaster(true);
                                                masterIs = true;
                                            }
                                            writeDataList.add(ImageDataList);
                                            setLog("대표 이미지 점검 "+i+"번째 : "+ImageDataList.isMaster());
                                            adapter_projectWrite.notifyItemChanged(writeDataList.size());
                                        }

                                    }
                                });




//                        DataList_project_write ImageDataList = new DataList_project_write();
//                        writeDataList.add(ImageDataList);
//                        //adapter_projectWrite.notifyDataSetChanged();
//                        adapter_projectWrite.notifyItemChanged(writeDataList.size());
                        break;

//작성 완료 버튼. 클릭 시 입력해야되는 edittext중 빈 값이 없는지, 글쓰기 recyclerview에 내용이 1개도 없는지 체크 후 서버에 업로드
                    case R.id.ProjectWirteActivity_button_complete:

                        if(etxtTitle.getText().toString().equals("")) {
                            etxtTitle.requestFocus();
                            Toast.makeText(ProjectWriteActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                            setLog("내용 : " + etxtTitle.getText());
                            setLog("타이틀 빈칸");
                        }else if(etxtOwner.getText().toString().equals("")){
                            etxtOwner.requestFocus();
                            Toast.makeText(ProjectWriteActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }else if(etxtLocation.getText().toString().equals("")){
                            etxtLocation.requestFocus();
                            Toast.makeText(ProjectWriteActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }else if(writeDataList.size()==0){
                            Toast.makeText(ProjectWriteActivity.this, "게시글 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                        }
                        //서버에 업로드
                        else {
                            setLog("내용 : "+etxtTitle.getText());
                            setLog("작성완료버튼");

                            //JSON에 게시글 정보 넣기. 글제목, 건축주, 위치, 내용(JSONarray) 순으로.
                            jsonPost = new JSONObject();
                            try {
                                jsonPost.put("title",etxtTitle.getText().toString());
                                jsonPost.put("owner",etxtOwner.getText().toString());
                                jsonPost.put("location",etxtLocation.getText().toString());
                                jsonPost.put("writter",ProjectListActivity.loginedUser);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //게시글 내용이 될 JSONArray
                            contentArray = new JSONArray();

                            for(int i=0;i<writeDataList.size();i++){
                                setLog(i+"번째 리스트 네임 : "+writeDataList.get(i).getName());
                                setLog(i+"번째 리스트 이미지 : "+writeDataList.get(i).getImgUri());
                                setLog(i+"번째 리스트 대표 : "+String.valueOf(writeDataList.get(i).isMaster()));
                                setLog("   ");
                                setLog("   ");
                                String lastItem = "false";

                                //내용. 리사이클러뷰 아이템 하나의 정보. position, 글내용, 이미지 경로, 대표 이미지 유무
                               jsonContent= new JSONObject();
                                if(writeDataList.size()-1 == i){
                                    lastItem = "true";
                                }
                                try {


                                    if (writeDataList.get(i).getImgUri() != null) {

                                        setLog(i+"번째 리스트 이미지 업로드 체크: "+writeDataList.get(i).getImgUri().getPath());
                                        String[] filePathColumn = {MediaStore.Images.Media.DATA};

                                        ExifInterface exif = null;
                                        try{
                                            exif = new ExifInterface(writeDataList.get(i).getImgUri().getPath());
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
                                        setLog("ddddddd : "+ orientation);

                                        if(orientation == 130){

                                            Bitmap rotateImg;
                                            Matrix rotateMatrix = new Matrix();
                                            rotateMatrix.postRotate(270);
                                            rotateImg = BitmapFactory.decodeFile(writeDataList.get(i).getImgUri().getPath());

                                            rotateImg = Bitmap.createBitmap(rotateImg,0,0,rotateImg.getWidth(),rotateImg.getHeight(),rotateMatrix,false);
                                            SaveBitmapToFileCache(rotateImg,writeDataList.get(i).getImgUri().getPath(),"ttt");

                                            String ImageUploadURL = "http://13.124.223.128/uploadImg/boardImg/uploadBoardImage.php";
                                            new ImageUploadTask().execute(
                                                    ImageUploadURL,
                                                    writeDataList.get(i).getImgUri().getPath(),
                                                    String.valueOf(i),
                                                    String.valueOf(writeDataList.get(i).isMaster()),
                                                    lastItem);


                                        }
                                        else {
                                            setLog("결과물 업로드 전 이미지 : "+writeDataList.get(i).getImgUri().getPath());
                                            setLog("결과물 업로드 전 순서 : "+i);
                                            setLog("결과물 업로드 전 대표 : "+String.valueOf(writeDataList.get(i).isMaster()));
                                            setLog("결과물 업로드 전 순서 : "+i);
                                            setLog("결과물 업로드 전 순서 : "+i);
                                            setLog("결과물 업로드 전 순서 : "+i);
                                            String ImageUploadURL = "http://13.124.223.128/uploadImg/boardImg/uploadBoardImage.php";
                                            new ImageUploadTask().execute(
                                                    ImageUploadURL,
                                                    writeDataList.get(i).getImgUri().getPath(),
                                                    String.valueOf(i),
                                                    String.valueOf(writeDataList.get(i).isMaster()),
                                                    lastItem);
                                        }

                                        Cursor cursor = getContentResolver().query(writeDataList.get(i).getImgUri(), filePathColumn, null, null, null);
                                        if(cursor != null){
                                            cursor.moveToFirst();
                                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                            imagePath = cursor.getString(columnIndex);
                                            setLog("이미지경로 : "+imagePath);
                                        }else {

                                        }

//                                        if (!TextUtils.isEmpty(imagePath)) {
//
//
//                                        } else {
//                                            //Toast.makeText(ProjectWriteActivity.this, "먼저 업로드할 파일을 선택하세요", Toast.LENGTH_SHORT).show();
//                                        }
                                        
//                                        String ImageUploadURL = "http://13.124.223.128/uploadImg/boardImg/uploadBoardImage.php";
//                                        new ImageUploadTask().execute(ImageUploadURL, imagePath);


                                    }
                                    else {
                                        jsonContent.put("position",i);
                                        jsonContent.put("text",writeDataList.get(i).getName());
                                        contentArray.put(jsonContent);
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
//                            try {
//                                jsonPost.put("content",contentArray);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }

                            setLog("확인 : "+jsonPost.toString());

                        }
                        break;

//내용 텍스트뷰. 선택시 글쓰기 리사이클러뷰에 텍스트를 적을 수 있는 아이템 추가.
                    case R.id.ProjectWirteActivity_textview_content:
                        setLog("내용텍스트");

                        //내용 클릭 시 리사이클러뷰 가장 최근 아이템이 텍스트 아이템이라면 해당 텍스트 아이템에 포커스를 주기위한 코드
//                        if(writeDataList.size()!=0){
//                            if(writeDataList.get(writeDataList.size()-1).getName()!=null){
//                                setLog("찾았다.");
//
//                            }
//                        }

                        DataList_project_write TextDataList = new DataList_project_write();
                        TextDataList.setName("");
                        TextDataList.setPosition(writeDataList.size());
                        writeDataList.add(TextDataList);
                        adapter_projectWrite.notifyItemChanged(writeDataList.size());
                        //adapter_projectWrite.notifyDataSetChanged();
                        break;
                }
            }
        };

        btnCancel.setOnClickListener(onClickListener);
        btnAddImage.setOnClickListener(onClickListener);
        btnComplete.setOnClickListener(onClickListener);
        txtContent.setOnClickListener(onClickListener);

        //------------------------------리사이클러뷰 정의----------------------------
        writeDataList = new ArrayList<>();

        adapter_projectWrite = new Adapter_projectWriteList(ProjectWriteActivity.this,writeDataList,this);
        recyProjectWriteList = findViewById(R.id.ProjectWirteActivity_recyclerview);
        recyProjectWriteList.setLayoutManager(new LinearLayoutManager(ProjectWriteActivity.this));

        //리사이클러뷰 아이템을 드래그 앤 드랍으로 위치 이동 시키려면 아래의 callback이 필요.
        writeItemTouchHelperCallback writeItemTouchHelperCallback = new writeItemTouchHelperCallback(adapter_projectWrite);
        itemTouchHelper = new ItemTouchHelper(writeItemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyProjectWriteList);

        recyProjectWriteList.setAdapter(adapter_projectWrite);
        //리사이클러뷰 안에 있는 에딧텍스트의 값이 ""일때 아이템이 삭제 되게하려고 하다가 에러가 발생하여 해결하려했던 방법중 하나
//        recyProjectWriteList.post(new Runnable() {
//            @Override
//            public void run() {
//                adapter_projectWrite.notifyDataSetChanged();
//            }
//        });





        //------------------------------드래그앤 드랍테스트----------------------------
//        for(int i = 0; i<50; i++){
//            DataList_project_write dataList = new DataList_project_write();
//            dataList.setName(String.valueOf(i));
//            writeDataList.add(dataList);
//
//        }
//        adapter_projectWrite.notifyDataSetChanged();

    }

    private class ImageUploadTask extends AsyncTask<String,Integer,Boolean>{
        ProgressDialog progressDialog;
        boolean isLast;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(ProjectWriteActivity.this);
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
                    String imgPath = "http://13.124.223.128/uploadImg/boardImg/"+jsonObject.getString("result");
                    JSONObject jsonImage = new JSONObject();
                    jsonImage.put("position",strings[2]);
                    jsonImage.put("imagePath",imgPath);
                    jsonImage.put("isMaster",strings[3]);
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
            if(progressDialog != null){
                progressDialog.dismiss();
            }
            if(isLast){
                setLog("마지막이다");

                setLog("결과물" + jsonPost);
                sendData(jsonPost,contentArray);

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

//    private class BitmapUploadTask extends AsyncTask<String,Integer,Boolean>{
//        ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            progressDialog = new ProgressDialog(ProjectWriteActivity.this);
//            progressDialog.setMessage("업로드 중...");
//            progressDialog.show();
//        }
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            try {
//                //String imageUploadUrl, Bitmap imageFile, String sourceImageFile)
//                String uploadResult = JSONParser.uploadImageBitmap(strings[0],rotatedImg,strings[1]);
//                if (uploadResult != null){
//                    String imgPath = "http://13.124.223.128/uploadImg/boardImg/"+uploadResult;
//                    JSONObject jsonImage = new JSONObject();
//                    jsonImage.put("position",strings[2]);
//                    jsonImage.put("imagePath",imgPath);
//                    jsonImage.put("isMaster",strings[3]);
//                    contentArray.put(jsonImage);
//
//
//
//                    //return jsonObject.getString("result").equals("success");
//                }
//
//
//            } catch (JSONException e) {
//                Log.i("TAG", "Error : " + e.getLocalizedMessage());
//            }
//            return false;
//
//        }
//
//        @Override
//        protected void onPostExecute(Boolean aBoolean) {
//            super.onPostExecute(aBoolean);
//            if(progressDialog != null){
//                progressDialog.dismiss();
//            }
//            setLog("결과물" + contentArray);
//
//        }
//    }
    private void sendData(final JSONObject jsonContent, final JSONArray jsonArrayContent) {
        // 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        final String url = "http://13.124.223.128/board/saveBoardContent.php";
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


        }
    };

    public static class JSONParser{

        public static JSONObject uploadImage(String imageUploadUrl, String sourceImageFile){
            try{
                File sourceFile = new File(sourceImageFile);
                Log.e(TAG, "File...::::" + sourceFile + " : " + sourceFile.exists());
                final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
                String filename = sourceImageFile.substring(sourceImageFile.lastIndexOf("/")+1);

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("uploaded_file",filename,RequestBody.create(MEDIA_TYPE_PNG,sourceFile))
                        .addFormDataPart("result","photo_image")
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

//        public static String uploadImageBitmap(String imageUploadUrl, Bitmap imageFile, String sourceImageFile){
//            try {
//                String filename = sourceImageFile.substring(sourceImageFile.lastIndexOf("/")+1);
//                //
//                SaveBitmapToFileCache(imageFile,sourceImageFile,filename);
//                RequestBody requestBody = new MultipartBody.Builder()
//                        .setType(MultipartBody.FORM)
//                        .addFormDataPart("uploaded_file",filename,RequestBody.create(MediaType.parse("image/png"), fileCacheItem))
//                        .build();
//
//                Request request = new Request.Builder()
//                        .url(imageUploadUrl)
//                        .post(requestBody)
//                        .build();
//
//                OkHttpClient client = new OkHttpClient();
//                okhttp3.Response response = client.newCall(request).execute();
//                return response.body().string();
//            }catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
    }

    // Bitmap to File
    public static void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath,
                                             String filename) {

        File file = new File(strFilePath);

        // If no folders
        if (!file.exists()) {
            file.mkdirs();


            // Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
        }

        File fileCacheItem = new File(strFilePath + filename);
        OutputStream out = null;

        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    }
}
