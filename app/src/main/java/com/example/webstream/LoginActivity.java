package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private HttpConnection httpConn = HttpConnection.getInstance();

    public void setLog(String content){
        android.util.Log.e(TAG,content);
        //private static final String TAG = "MainActivity";
    }

    TextInputEditText etxtInputId;              //아이디 입력 EditText
    TextInputEditText etxtInputPassword;        //비밀번호 입력 EditText
    Button btnLogin;                            //로그인 버튼. 로그인 버튼 클릭했을 때 서버에, 입력한 아이디와 비밀번호 POST로 보낸다
    Button btnLoginGoogle;                      //구글 로그인 버튼
    Button btnLoginKakao;                       //카카오 로그인 버튼
    TextView txtSignup;                         //회원가입 텍스트. 클릭 시 회원가입 액티비티로 이동
    TextView txtFindPassword;                   //비밀번호 찾기 텍스트. 클릭 시 비밀번호 찾기 액티비티로 이동
    String strId;
    String strPassword;
    CheckBox checkBoxLoginState;
    boolean autoLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기 (이 액이티비에서만)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        etxtInputId = findViewById(R.id.loginactivity_edittext_id);
        etxtInputPassword = findViewById(R.id.loginactivity_edittext_password);
        btnLogin = findViewById(R.id.loginactivity_button_login);
        btnLoginGoogle = findViewById(R.id.loginactivity_button_login_google);
        btnLoginKakao = findViewById(R.id.loginactivity_button_login_kakao);
        txtSignup = findViewById(R.id.loginactivity_textview_signup);
        txtFindPassword = findViewById(R.id.loginactivity_textview_findpassword);
        checkBoxLoginState = findViewById(R.id.loginactivity_checkBox_loginstate);

        SharedPreferences spLatelyLoginId = getSharedPreferences("latelyLoginId",MODE_PRIVATE);
        String strLatelyLoginId = spLatelyLoginId.getString("latelyLoginId","");
        if(!strLatelyLoginId.equals("")){
            etxtInputId.setText(strLatelyLoginId);
        }


        //--------------------------------회원가입 텍스트 클릭 시 SigupActivity (회원가입) 으로 이동.-----------------------------------------
        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                strId = etxtInputId.getText().toString();
                strPassword = etxtInputPassword.getText().toString();
                String loginUrl = "http://13.124.223.128/login/login.php";
                sendDataLogin(strId, strPassword,loginUrl);


            }
        });
        checkBoxLoginState.setOnClickListener(new CheckBox.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBoxLoginState.isChecked()){
                    setLog("로그인 상태 체크박스 : 체크됨");
                    autoLogin = true;
                }else {
                    setLog("로그인 상태 체크박스 : 체크안됨");
                    autoLogin = false;
                }
            }
        });



    }
    private void sendDataLogin(final String id,final String password, final String url) {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                httpConn.requestLogin(id, password, callback, url);
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
            if(body.equals("LOGIN_SUCCESS")){
                setLog("로그인 성공");


                SharedPreferences spLastActivity = getSharedPreferences("lastActivity",MODE_PRIVATE);
                String lastActivity = spLastActivity.getString("lastActivity","");
                setLog("로그인 전 액티비티 : "+lastActivity);

                Intent intent = null;

                if(lastActivity.equals("HomeActivity")){
                    intent = new Intent(LoginActivity.this,HomeActivity.class);
                    if(HomeActivity.activity!=null){
                        HomeActivity activity = (HomeActivity) HomeActivity.activity;
                        activity.finish();
                    }
                }
                else if(lastActivity.equals("LiveListActivity")){
                    intent = new Intent(LoginActivity.this,LiveListActivity.class);
                    if(LiveListActivity.activity!=null){
                        LiveListActivity activity = (LiveListActivity) LiveListActivity.activity;
                        activity.finish();
                    }
                }

                SharedPreferences spCurrentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
                SharedPreferences.Editor editor = spCurrentUser.edit();

                editor.putString("currentUser",strId);
                editor.putBoolean("autoLogin",autoLogin);
                editor.commit();

                SharedPreferences spLatelyLoginId = getSharedPreferences("latelyLoginId",MODE_PRIVATE);
                SharedPreferences.Editor editorLatelyLoginId = spLatelyLoginId.edit();

                editorLatelyLoginId.putString("latelyLoginId",strId);
                editorLatelyLoginId.commit();

                //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("id",strId);
                intent.putExtra("password",strPassword);
                startActivity(intent);
                finish();
            }else if(body.equals("LOGIN_FAIL_NONID") || body.equals("LOGIN_FAIL_NONPASSWORD")){
                Looper.prepare();
                Toast.makeText(LoginActivity.this, "아이디 또는 비밀번호를 다시 확인하세요.\n" +
                        "ArchiMagazine에 등록되지 않은 아이디이거나, 비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }

        }
    };
}
