package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MyProfileActivity extends Activity
        implements View.OnTouchListener {
    private static final String TAG = "MyProfileActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 1000;
    private Object NetworkHelper;


    public void setLog(String content){
        android.util.Log.e(TAG,content);

    }

    public static final int SET_ID = 100;
    public static final int SET_NICKNAME = 200;
    public static final int SET_EMAIL = 300;
    public static final int SET_PROFILE = 400;

    private static final int GET_USER_DATA = 501;

    public static final int CHECKNICKNAME = 600;
    public static final int CHECKNICKNAME_IMPOSSIBLE = 601;
    public static final int CHECKNICKNAME_POSSIBLE = 602;
    public static final int CHECKNICKNAME_INVALID = 603;

    public static final int CHECKPASSWORD = 700;
    public static final int CHECKPASSWORD_EXISTINGPW_INCORRECT = 701;
    public static final int CHECKPASSWORD_NEWPW_INVALID = 702;
    public static final int CHECKPASSWORD_NEWPW_RECONFIRM_INCORRECT = 703;
    public static final int CHECKPASSWORD_CHANGE_PASSWORD = 704;

    private static final int CHANGE_PROFILE = 800;
    private static final int CHANGE_ORIGINAL_PROFILE_SUCCESS = 801;
    private static final int CHANGE_ORIGINAL_PROFILE_FAILED = 802;
    private static final int CHANGE_ALBUM_PROFILE = 803;

    private static final int PICK_FROM_ALBUM = 900;
    private static final int CAMERA_CODE = 901;


    String loginedUser;
    String strChangeInputNickname;

    LinearLayout linearLayoutNickname ;
    LinearLayout linearLayoutEmail ;
    LinearLayout linearLayoutPassword ;
    LinearLayout linearLayoutCancel ;

    Handler handler;

    TextView txtId;
    TextView txtNickname;
    TextView txtEmail;
    ImageView imgProfile;

    String imagePath;

    private HttpConnection httpConn = HttpConnection.getInstance();

    DialogInterface dialogInterfaceChangeNickname;
    DialogInterface dialogInterfaceChangePassword;

    TextView txtAlertNickname;
    TextView txtAlertExistingPassword;
    TextView txtAlertNewPassword;
    TextView txtAlertNewPasswordReconfirm;

    private Uri photoUri;
    private String currentPhotoPath;//실제 사진 파일 경로
    String mImageCaptureName;//이미지 이름


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기 (이 액이티비에서만)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_my_profile);

        Intent intent = getIntent();
        loginedUser = intent.getStringExtra("loginedUser");
        setLog("현재 로그인 한 아이디 : "+loginedUser);

        linearLayoutNickname = findViewById(R.id.activity_myprofile_linear_nickname);
        linearLayoutEmail = findViewById(R.id.activity_myprofile_linear_email);
        linearLayoutPassword = findViewById(R.id.activity_myprofile_linear_change_password);
        linearLayoutCancel = findViewById(R.id.activity_myprofile_linear_cancel);

        linearLayoutNickname.setOnTouchListener(this);
        linearLayoutEmail.setOnTouchListener(this);
        linearLayoutPassword.setOnTouchListener(this);
        linearLayoutCancel.setOnTouchListener(this);

        txtId = findViewById(R.id.activity_myprofile_textview_id);
        txtNickname = findViewById(R.id.activity_myprofile_textview_nickname);
        txtEmail = findViewById(R.id.activity_myprofile_textview_email);
        imgProfile = findViewById(R.id.activity_myprofile_imageview);

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeProfile();
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case SET_ID:
                        txtId.setText(String.valueOf(msg.obj));
                        setLog("핸들러 메세지 확인 (아이디) : "+msg.obj);
                        break;
                    case SET_NICKNAME:
                        txtNickname.setText(String.valueOf(msg.obj));
                        setLog("핸들러 메세지 확인 (닉) : "+msg.obj);
                        break;
                    case SET_EMAIL:
                        txtEmail.setText(String.valueOf(msg.obj));
                        setLog("핸들러 메세지 확인 (이메일) : "+msg.obj);
                        break;
                    case SET_PROFILE:
                        setLog("핸들러 메세지 확인 (프로필) : "+msg.obj);

                        if(String.valueOf(msg.obj).equals("null")){
                            Glide.with(MyProfileActivity.this)
                                    .load(R.drawable.user_profile_default)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }else {
                            Glide.with(MyProfileActivity.this)
                                    .load(msg.obj)
                                    .apply(new RequestOptions().circleCrop())
                                    .into(imgProfile);
                        }
                        break;
                    case CHECKNICKNAME_POSSIBLE:
                        txtAlertNickname.setVisibility(View.GONE);
                        txtNickname.setText(strChangeInputNickname);
                        Toast.makeText(MyProfileActivity.this, "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    case CHECKNICKNAME_IMPOSSIBLE:
                        setLog("");
                        txtAlertNickname.setText("이미 사용중인 닉네임입니다.");
                        txtAlertNickname.setVisibility(View.VISIBLE);
                        break;
                    case CHECKNICKNAME_INVALID:
                        txtAlertNickname.setText("유효하지 않는 형식입니다.");
                        txtAlertNickname.setVisibility(View.VISIBLE);
                        break;
                    //비밀번호 변경에대한 핸들러
                    case CHECKPASSWORD_CHANGE_PASSWORD:
                        txtAlertExistingPassword.setVisibility(View.GONE);
                        txtAlertNewPassword.setVisibility(View.GONE);
                        txtAlertNewPasswordReconfirm.setVisibility(View.GONE);
                        Toast.makeText(MyProfileActivity.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        break;
                    case CHECKPASSWORD_EXISTINGPW_INCORRECT:
                        txtAlertExistingPassword.setVisibility(View.VISIBLE);
                        txtAlertNewPassword.setVisibility(View.GONE);
                        txtAlertNewPasswordReconfirm.setVisibility(View.GONE);
                        break;
                    case CHECKPASSWORD_NEWPW_INVALID:
                        txtAlertExistingPassword.setVisibility(View.GONE);
                        txtAlertNewPassword.setVisibility(View.VISIBLE);
                        txtAlertNewPasswordReconfirm.setVisibility(View.GONE);
                        break;
                    case CHECKPASSWORD_NEWPW_RECONFIRM_INCORRECT:
                        txtAlertExistingPassword.setVisibility(View.GONE);
                        txtAlertNewPassword.setVisibility(View.GONE);
                        txtAlertNewPasswordReconfirm.setVisibility(View.VISIBLE);
                        break;
                    //프로필 변경(기본 이미지로 변경) 에 대한 핸들러
                    case CHANGE_ORIGINAL_PROFILE_SUCCESS:
                        Glide.with(MyProfileActivity.this)
                                .load(R.drawable.user_profile_default)
                                .apply(new RequestOptions().circleCrop())
                                .into(imgProfile);
                        break;
                    case CHANGE_ORIGINAL_PROFILE_FAILED:
                        break;

                }
            }
        };
        sendData(loginedUser,"http://13.124.223.128/getUserData/getUserData.php", GET_USER_DATA);

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()){
            //닉네임
            case R.id.activity_myprofile_linear_nickname:
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    linearLayoutNickname.setBackgroundColor(Color.parseColor("#B3B3B3"));
                    setLog("터치중");
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    linearLayoutNickname.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    setLog("터치끝");
                    ChangeNickname();
                }
                break;
            //이메일
            case R.id.activity_myprofile_linear_email:
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    linearLayoutEmail.setBackgroundColor(Color.parseColor("#B3B3B3"));
                    setLog("터치중");
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    linearLayoutEmail.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    setLog("터치끝");
                }
                break;
            //비밀번호 변경
            case R.id.activity_myprofile_linear_change_password:
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    linearLayoutPassword.setBackgroundColor(Color.parseColor("#B3B3B3"));
                    setLog("터치중");
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    linearLayoutPassword.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    setLog("터치끝");
                    ChangePassword();

                }
                break;
            //회원 탈퇴
            case R.id.activity_myprofile_linear_cancel:
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    linearLayoutCancel.setBackgroundColor(Color.parseColor("#B3B3B3"));
                    setLog("터치중");
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    linearLayoutCancel.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    setLog("터치끝");
                }
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    private void sendData(final String param, final String url, final int flag) {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                switch (flag){
                    case GET_USER_DATA:
                        httpConn.requestGetUserData(param, callback, url);
                        break;
                    case CHANGE_PROFILE:
                        httpConn.requestChangeProfile(param, callback, url);
                        break;
                }

            }
        }.start();

    }
    private void sendDataDouble(final String id, final String param, final String url, final int flag) {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                switch (flag){
                    case CHECKNICKNAME:
                        httpConn.requestChaneNickname(id, param, callback, url);
                        break;
                    case CHANGE_ALBUM_PROFILE:
                        httpConn.requestChangeAlbumProfile(id, param, callback, url);
                        break;
                }

            }
        }.start();

    }
    private void sendDataChangePassword(final String id, final String existingPassword,final String newPassword,final String newPasswordReconfirm, final String url, final int flag) {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                switch (flag){
                    case CHECKPASSWORD:
                        httpConn.requestChanePassword(id, existingPassword, newPassword, newPasswordReconfirm, callback, url);
                        break;
                }

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
            //닉네임 변경에 대한 반환값
            if(body.equals("CHECKNICK_NONOVERLAP")){
                handler.sendEmptyMessage(CHECKNICKNAME_POSSIBLE);
                dialogInterfaceChangeNickname.dismiss();
                setLog("닉네임 가능");
            }else if(body.equals("CHECKNICK_OVERLAP")){
                handler.sendEmptyMessage(CHECKNICKNAME_IMPOSSIBLE);
                setLog("닉네임 불가능 : 중복");
            }else if(body.equals("CHECKNICK_INVALID")) {
                handler.sendEmptyMessage(CHECKNICKNAME_INVALID);
                setLog("닉네임 불가능 : 유효하지 않는 형식");
            }
            //패스워드 변경에 대한 반환값
            else if(body.equals("CHECKPASSWORD_SUCCESS")){
                handler.sendEmptyMessage(CHECKPASSWORD_CHANGE_PASSWORD);
                dialogInterfaceChangePassword.dismiss();
                setLog("비밀 번호 변경 성공");
            }else if(body.equals("CHECKPASSWORD_NEWPASSWORD_RECONFIRM_INCORRECT")){
                handler.sendEmptyMessage(CHECKPASSWORD_NEWPW_RECONFIRM_INCORRECT);
                setLog("비밀 번호 변경 불가능 : 새로운 비밀번호 확인 다름");
            }else if(body.equals("CHECKPASSWORD_NEWPASSWORD_INVALID")){
                handler.sendEmptyMessage(CHECKPASSWORD_NEWPW_INVALID);
                setLog("비밀 번호 변경 불가능 : 새로운 비밀번호 정규식 표현 틀림");
            }else if(body.equals("CHECKPASSWORD_EXISTING_PASSWORD_INCORRECT")){
                handler.sendEmptyMessage(CHECKPASSWORD_EXISTINGPW_INCORRECT);
                setLog("비밀 번호 변경 불가능 : 기존 비밀번호 틀림");
            }
            //프로필 변경(기존 이미지로)에 대한 반환값
            else if(body.equals("CHANGE_ORIGINAL_PROFILE_SUCCESS")){
                handler.sendEmptyMessage(CHANGE_ORIGINAL_PROFILE_SUCCESS);
                setLog("기존 프로필 변경 가능");
            }else if(body.equals("CHANGE_ORIGINAL_PROFILE_FAILED")){
                handler.sendEmptyMessage(CHANGE_ORIGINAL_PROFILE_FAILED);
                setLog("기존 프로필 변경 불가능");
            }
            //프로필 변경(앨범에서 선택)에 대한 반환값
            else if(body.equals("CHANGE_ALBUM_PROFILE_SUCCESS")){
                setLog("앨범 프로필 변경 가능");
            }else if(body.equals("CHANGE_ALBUM_PROFILE_FAILED")){
                setLog("앨범 프로필 변경 불가능");
            }

            else {
                try {
                    setLog("제이슨 시작");
                    JSONObject getUserData = new JSONObject(body);
                    String dummyData = getUserData.getString("userInfo");
                    JSONArray getUserDataArray = new JSONArray(dummyData);
                    JSONObject getUserData2 = getUserDataArray.getJSONObject(0);
                    String getId = getUserData2.getString("id");
                    String getNickname = getUserData2.getString("nickname");
                    String getEmail = getUserData2.getString("email");
                    String getProfile = getUserData2.getString("profileRoute");

                    setLog("유저아이디 : "+getId);
                    setLog("유저닉네임 : "+getNickname);

                    Message messageId = handler.obtainMessage();
                    messageId.what = SET_ID;
                    messageId.obj = getId;
                    handler.sendMessage(messageId);

                    Message messageNickname = handler.obtainMessage();
                    messageNickname.what = SET_NICKNAME;
                    messageNickname.obj = getNickname;
                    handler.sendMessage(messageNickname);

                    Message messageEmail = handler.obtainMessage();
                    messageEmail.what = SET_EMAIL;
                    messageEmail.obj = getEmail;
                    handler.sendMessage(messageEmail);

                    Message messageProfile = handler.obtainMessage();
                    messageProfile.what = SET_PROFILE;
                    messageProfile.obj = getProfile;
                    handler.sendMessage(messageProfile);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }



        }
    };
    private void ChangeNickname(){
        AlertDialog.Builder changeNickDialog = new AlertDialog.Builder(MyProfileActivity.this);
        View editboxview = LayoutInflater.from(MyProfileActivity.this).inflate(R.layout.dialog_change_nickname,
                null,false);
        changeNickDialog.setView(editboxview);
        final Button changeComplete = editboxview.findViewById(R.id.dialog_change_nickname_button_chagecomplete);
        final EditText etxtInputNickname = editboxview.findViewById(R.id.dialog_change_nickname_editText_input_nickname);
        txtAlertNickname = editboxview.findViewById(R.id.dialog_change_nickname_alert_nickname);

        AlertDialog dialogChangeNicakname = changeNickDialog.create();
        dialogChangeNicakname.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialogInterfaceChangeNickname = dialogInterface;
                changeComplete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        strChangeInputNickname = etxtInputNickname.getText().toString();
                        String chaneNicknameUrl = "http://13.124.223.128/change/changeNickname.php";

                        if(strChangeInputNickname.equals("")){
                            Toast.makeText(MyProfileActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                            etxtInputNickname.requestFocus();
                        }else {
                            sendDataDouble(loginedUser,strChangeInputNickname,chaneNicknameUrl,CHECKNICKNAME);
                        }

                    }
                });
            }
        });
        dialogChangeNicakname.show();
    }
    private void ChangePassword(){
        AlertDialog.Builder changePasswordDialog = new AlertDialog.Builder(MyProfileActivity.this);
        View editboxview = LayoutInflater.from(MyProfileActivity.this).inflate(R.layout.dialog_change_password,
                null,false);
        changePasswordDialog.setView(editboxview);
        final Button changeComplete = editboxview.findViewById(R.id.dialog_change_password_button_changecomplete);
        final EditText etxtInputExistingPassowrd = editboxview.findViewById(R.id.dialog_change_password_edittext_existing_password);
        final EditText etxtInputNewPassword = editboxview.findViewById(R.id.dialog_change_password_edittext_newpassword);
        final EditText etxtInputNewPasswordReconfirm = editboxview.findViewById(R.id.dialog_change_password_edittext_newpassword_reconfirm);
        txtAlertExistingPassword = editboxview.findViewById(R.id.dialog_change_password_alert_existing_password);
        txtAlertNewPassword = editboxview.findViewById(R.id.dialog_change_password_alert_newpassword);
        txtAlertNewPasswordReconfirm = editboxview.findViewById(R.id.dialog_change_password_alert_newpassword_reconfirm);

        AlertDialog dialogChangePassword = changePasswordDialog.create();
        dialogChangePassword.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialogInterfaceChangePassword = dialogInterface;
                changeComplete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String strChangeInputExistingPassword = etxtInputExistingPassowrd.getText().toString();
                        String strChangeInputNewPassword = etxtInputNewPassword.getText().toString();
                        String stChangeInputrNewPasswordReconfirm = etxtInputNewPasswordReconfirm.getText().toString();
                        String changePasswordUrl = "http://13.124.223.128/change/changePassword.php";

                        if(strChangeInputExistingPassword.equals("")){
                            Toast.makeText(MyProfileActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                            etxtInputExistingPassowrd.requestFocus();
                        }else if(strChangeInputNewPassword.equals("")){
                            Toast.makeText(MyProfileActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                            etxtInputNewPassword.requestFocus();
                        }else if(stChangeInputrNewPasswordReconfirm.equals("")){
                            Toast.makeText(MyProfileActivity.this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
                            etxtInputNewPasswordReconfirm.requestFocus();
                        }else {
                            sendDataChangePassword(loginedUser, strChangeInputExistingPassword, strChangeInputNewPassword, stChangeInputrNewPasswordReconfirm, changePasswordUrl, CHECKPASSWORD);
                        }


                    }
                });
            }
        });
        dialogChangePassword.show();

    }
    private void ChangeProfile(){
        String camera = "카메라로 찍기";
        String album = "앨범에서 선택";
        String original = "기본 이미지로 변경";
        final int CAMERA = 0;
        final int ALBUM = 1;
        final int ORIGINAL = 2;
        final CharSequence[] items = { camera, album, original};

        AlertDialog.Builder alertDialogProfile = new AlertDialog.Builder(MyProfileActivity.this);

        alertDialogProfile.setTitle("프로필 변경");
        alertDialogProfile.setItems(items,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case CAMERA:
                                String state = Environment.getExternalStorageState();
                                setLog("용량 : "+state);
                                if(Environment.MEDIA_MOUNTED.equals(state)){
                                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    if(intentCamera.resolveActivity(getPackageManager())!=null){
                                        File photoFile =null;
                                        try{
                                            photoFile = createImageFile();
                                        } catch (IOException ex) {

                                        }
                                        if(photoFile != null){
                                            photoUri = FileProvider.getUriForFile(MyProfileActivity.this,getPackageName(),photoFile);
                                            setLog("photoFile : "+photoFile.toString());
                                            setLog("photoUri : "+photoUri.toString());
                                            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                                            startActivityForResult(intentCamera,CAMERA_CODE);
                                        }
                                    }
                                }

                                //startActivityForResult(intentCamera, CAMERA_CODE);

                                break;
                            case ALBUM:
                                Intent intent = new Intent(Intent.ACTION_PICK);
                                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                                startActivityForResult(intent,PICK_FROM_ALBUM);
                                break;
                            case ORIGINAL:
                                ChangeProfileOriginal();
                                break;
                        }
                    }
                });

        AlertDialog ProfileDialog = alertDialogProfile.create();

        ProfileDialog.show();

    }

    private void ChangeProfileOriginal(){
        AlertDialog.Builder alertDialogProfile = new AlertDialog.Builder(MyProfileActivity.this);
        alertDialogProfile.setTitle("기본 이미지로 변경")
                .setMessage("기본 이미지로 변경 시 원래의 프로필을 복구할 수 없습니다. 진행하시겠습니까?");

        alertDialogProfile.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setLog("프로필 변경 확인버튼");
                String chaneProfileUrl = "http://13.124.223.128/change/changeProfile.php";
                sendData(loginedUser,chaneProfileUrl,CHANGE_PROFILE);
            }
        });
        alertDialogProfile.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setLog("프로필 변경 취소버튼");
            }
        });
        alertDialogProfile.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_ALBUM && resultCode == RESULT_OK) {

            /*imgProfile.setImageURI(data.getData());*/
            setLog(String.valueOf("가져온 사진 : "+data.getData()));
            Uri selectedImageUri  = data.getData();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
            if(cursor != null){
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imagePath = cursor.getString(columnIndex);
            }else {

            }
            try {
                Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImageUri);
                SaveBitmapToFileCache(bm,imagePath,"tt");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!TextUtils.isEmpty(imagePath)) {

                String ImageUploadURL = "http://13.124.223.128/uploadImg/userProfileImg/uploadUserProfile.php";
                new ImageUploadTask().execute(ImageUploadURL, imagePath);
            } else {
                Toast.makeText(MyProfileActivity.this, "먼저 업로드할 파일을 선택하세요", Toast.LENGTH_SHORT).show();
            }



            Glide.with(MyProfileActivity.this)
                    .load(selectedImageUri )
                    .apply(new RequestOptions().circleCrop())
                    .into(imgProfile);

            /*imgProfile.setImageURI(data.getData()); // 가운데 원본 뷰를 바꿈
            imageUri = data.getData(); //이미지 경로 원본
            profileWarinig.setVisibility(View.INVISIBLE);*/
        }else if (requestCode == CAMERA_CODE && resultCode == RESULT_OK){
            /*Bundle extras = data.getExtras();

            Bitmap imageBitmap = (Bitmap) extras.get("data");*/

            getPictureForPhoto();


            /*Glide.with(MyProfileActivity.this)
                    .load(mImageCaptureUri )
                    .apply(new RequestOptions().circleCrop())
                    .into(imgProfile);*/
        }
    }




    private  class ImageUploadTask extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*progressDialog = new ProgressDialog(MyProfileActivity.this);
            progressDialog.setMessage("이미지 업로드중....");
            progressDialog.show();*/
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                JSONObject jsonObject = JSONParser.uploadImage(params[0],params[1]);
                if (jsonObject != null){
                    String profileRoute = "http://13.124.223.128/uploadImg/userProfileImg/"+jsonObject.getString("result");
                    String url = "http://13.124.223.128/change/changeAlbumProfile.php";
                    sendDataDouble(loginedUser, profileRoute, url, CHANGE_ALBUM_PROFILE );
                }
            } catch (JSONException e) {
                Log.i("TAG", "Error : " + e.getLocalizedMessage());
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (progressDialog != null)
                progressDialog.dismiss();

        }
    }
    public static class JSONParser {
        public static JSONObject uploadImage(String imageUploadUrl, String sourceImageFile) {

            try {
                File sourceFile = new File(sourceImageFile);
                Log.d("TAG", "File...::::" + sourceFile + " : " + sourceFile.exists());
                final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/*");
                String filename = sourceImageFile.substring(sourceImageFile.lastIndexOf("/")+1);

                // OKHTTP3
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("uploaded_file", filename, RequestBody.create(MEDIA_TYPE_PNG, sourceFile))
                        .addFormDataPart("result", "photo_image")
                        .build();

                Request request = new Request.Builder()
                        .url(imageUploadUrl)
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                Response response = client.newCall(request).execute();
                String res = response.body().string();
                Log.e("TAG", "Error: " + res);
                return new JSONObject(res);

            } catch (UnknownHostException | UnsupportedEncodingException e) {
                Log.e("TAG", "Error: " + e.getLocalizedMessage());
            } catch (Exception e) {
                Log.e("TAG", "Other Error: " + e.getLocalizedMessage());
            }
            return null;
        }

    }
    private File createImageFile() throws IOException{
        File dir = new File(Environment.getExternalStorageDirectory()+"/path/");
        if(!dir.exists()){
            dir.mkdir();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); mImageCaptureName = timeStamp + ".png";
        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/path/"+mImageCaptureName);
        currentPhotoPath = storageDir.getAbsolutePath();
        return storageDir;

    }

    private void getPictureForPhoto(){
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);

        SaveBitmapToFileCache(bitmap, currentPhotoPath, "ttt");

        ExifInterface exif = null;
        try{
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if(exif != null){
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        }else {
            exifDegree = 0;
        }
        Glide.with(MyProfileActivity.this)
                .load(bitmap)
                .apply(new RequestOptions().circleCrop())
                .into(imgProfile);

        String ImageUploadURL = "http://13.124.223.128/uploadImg/userProfileImg/uploadUserProfile.php";
        new ImageUploadTask().execute(ImageUploadURL, currentPhotoPath);

    }
    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        } return 0;
    }

    public static void SaveBitmapToFileCache(Bitmap bitmap, String strFilePath, String filename) {

        File file = new File(strFilePath);

        if (!file.exists())

            file.mkdirs();

        File fileCacheItem = new File(strFilePath);

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






}
