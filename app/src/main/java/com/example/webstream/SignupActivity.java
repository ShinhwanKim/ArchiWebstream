package com.example.webstream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.Random;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignupActivity extends Activity
        implements EditText.OnFocusChangeListener , View.OnClickListener {

    private static final String TAG = "SignupActivity";

    public void setLog(String content){
        android.util.Log.e(TAG,content);
        //private static final String TAG = "MainActivity";
    }
    public static final int CHECKID = 100;
    public static final int CHECKID_IMPOSSIBLE = 101;
    public static final int CHECKID_POSSIBLE = 102;
    public static final int CHECKID_INVALID = 103;
    public static final int CHECKNICKNAME = 200;
    public static final int CHECKNICKNAME_IMPOSSIBLE = 201;
    public static final int CHECKNICKNAME_POSSIBLE = 202;
    public static final int CHECKNICKNAME_INVALID = 203;
    public static final int CHECKPASSWORD = 300;
    public static final int CHECKPASSWORD_VALID = 301;
    public static final int CHECKPASSWORD_INVALID = 302;
    public static final int SIGNUP = 400;




    TextInputEditText etxtInputId;                          //아이디 입력 EditText
    TextInputEditText etxtInputPassword;                    //비밀번호 입력 EditText
    TextInputEditText etxtInputPasswordReconfirm;           //비밀번호 확인 입력 EditText
    TextInputEditText etxtInputNickname;                    //닉네임 입력 EditText
    TextInputEditText etxtInputEmail;                       //이메일 입력 EditText
    TextView txtAlertId;                                    //이미 사용중인 아이디 있을 때 경고해주는 textview
    TextView txtAlertPassword;                              //비밀번호와 비밀번호 확인이 일치하지 않을 떄 경고해주는 textview
    TextView txtAlertPasswordReconfirm;
    TextView txtAlertNickname;                              //이미 사용중인 닉네임이 있을 때 경고해주는 textview
    TextView txtAlertEmail;                                 //유효하지 않은 이메일을 입력했을 때 경고해주는 textview
    Button btnSigupComplete;                                //회원가입 완료 버튼
    Button btnEmailAuth;

    boolean idState = false;
    boolean passwordState = false;
    boolean passwordReconfirmState = false;
    boolean nicknameState = false;
    boolean emailState = false;

    String strInputId;
    String strInputPassword;
    String strInputPasswordReconfirm;
    String strInputNickname;
    String strInputEmail;


    private HttpConnection httpConn = HttpConnection.getInstance();

    Handler handler;

    TextView timeCount;
    String strEmailAuth;

    CountDownTimer countDownTimer;

    AlertDialog.Builder inputEmailAuth;
    AlertDialog dialogEmailAuth;

    int certificationNumber;
    boolean autoLogin;

    String authedEmail = "NONEAUTH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);


        etxtInputId = findViewById(R.id.SignupActivity_edittext_id);
        etxtInputPassword = findViewById(R.id.SignupActivity_edittext_password);
        etxtInputPasswordReconfirm = findViewById(R.id.SignupActivity_edittext_password_reconfirm);
        etxtInputNickname = findViewById(R.id.SignupActivity_edittext_nickname);
        etxtInputEmail = findViewById(R.id.SignupActivity_edittext_email);

        txtAlertId = findViewById(R.id.SignupActivity_alert_id);
        txtAlertPassword = findViewById(R.id.SignupActivity_alert_password);
        txtAlertPasswordReconfirm = findViewById(R.id.SignupActivity_alert_password_reconfirm);
        txtAlertNickname = findViewById(R.id.SignupActivity_alert_nickname);
        txtAlertEmail = findViewById(R.id.SignupActivity_alert_email);

        btnSigupComplete = findViewById(R.id.SignupActivity_button_signup);
        btnEmailAuth = findViewById(R.id.SignupActivity_button_email_auth);

        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case CHECKID_POSSIBLE:
                        txtAlertId.setVisibility(View.GONE);
                        break;
                    case CHECKID_IMPOSSIBLE:
                        txtAlertId.setText("이미 사용중인 아이디 입니다.");
                        txtAlertId.setVisibility(View.VISIBLE);
                        break;
                    case CHECKID_INVALID:
                        txtAlertId.setText("유효하지 않는 형식입니다.");
                        txtAlertId.setVisibility(View.VISIBLE);
                    case CHECKNICKNAME_POSSIBLE:
                        txtAlertNickname.setVisibility(View.GONE);
                        break;
                    case CHECKNICKNAME_IMPOSSIBLE:
                        txtAlertNickname.setVisibility(View.VISIBLE);
                        break;
                    case CHECKNICKNAME_INVALID:
                        txtAlertNickname.setText("유효하지 않는 형식입니다.");
                        txtAlertNickname.setVisibility(View.VISIBLE);
                        break;
                    case CHECKPASSWORD_VALID:
                        txtAlertPassword.setVisibility(View.GONE);
                        break;
                    case CHECKPASSWORD_INVALID:
                        txtAlertPassword.setVisibility(View.VISIBLE);
                        break;

                }
            }
        };

        //--------------------------------회원가입 시 입력해야되는 EditText들 모두 Focus이벤트 리스너 적용-----------------------------------------
        etxtInputId.setOnFocusChangeListener(this);
        etxtInputPassword.setOnFocusChangeListener(this);
        etxtInputPasswordReconfirm.setOnFocusChangeListener(this);
        etxtInputNickname.setOnFocusChangeListener(this);
        etxtInputEmail.setOnFocusChangeListener(this);

        btnSigupComplete.setOnClickListener(this);
        btnEmailAuth.setOnClickListener(this);


    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        //포커스 잃었을 때
        if(!hasFocus){
            switch (view.getId()){

                case R.id.SignupActivity_edittext_id:                      //아이디 EditText Focus
                    setLog("아이디");
                    strInputId = etxtInputId.getText().toString();
                    if(strInputId.equals("")){
                        idState = false;
                    }else {
                        sendData(strInputId,"http://13.124.223.128/signup/checkId.php",CHECKID);
                    }
                    break;

                case R.id.SignupActivity_edittext_password:                //비밀번호 EditText Focus
                    setLog("비밀번호");
                    strInputPassword = etxtInputPassword.getText().toString();
                    if(strInputPassword.equals("")){
                        passwordState = false;
                    }else {
                        sendData(strInputPassword,"http://13.124.223.128/signup/checkPassword.php",CHECKPASSWORD);
                    }
                    break;

                case R.id.SignupActivity_edittext_password_reconfirm:      //비밀번호 확인 EditText Focus
                    setLog("비밀번호 확인");
                    strInputPasswordReconfirm = etxtInputPasswordReconfirm.getText().toString();
                    boolean compareResult = ComparePassword();
                    if(compareResult){
                        setLog("같다");
                        txtAlertPasswordReconfirm.setVisibility(View.GONE);
                        passwordReconfirmState = true;
                    }else {
                        //비밀번호, 비밀번호 확인이 일치하지 않아 경고문 출력
                        txtAlertPasswordReconfirm.setVisibility(View.VISIBLE);
                        passwordReconfirmState = false;
                    }

                    break;

                case R.id.SignupActivity_edittext_nickname:                 //닉네임 EditText Focus
                    setLog("닉네임");
                    strInputNickname = etxtInputNickname.getText().toString();
                    if(strInputNickname.equals("")){
                        nicknameState = false;
                    }else {
                        sendData(strInputNickname,"http://13.124.223.128/signup/checkNick.php",CHECKNICKNAME);
                        nicknameState = true;
                    }
                    break;

                /*case R.id.SignupActivity_edittext_email:                    //이메일 EditText Focus
                    setLog("이메일");
                    strInputEmail = etxtInputEmail.getText().toString();
                    if(strInputEmail.equals("")){
                        emailState = false;
                    }else{
                        emailState = true;
                    }
                    break;*/
            }

        }
    }

    /*비밀번호 EditText와 비밀번호 확인 EditText에 입력한 값이 서로 같은지 비교해주는 메서드
      같으면 true 그렇지 않으면 false return
     */
    public boolean ComparePassword(){
        boolean compareResult = false;
        strInputPassword = etxtInputPassword.getText().toString();
        strInputPasswordReconfirm = etxtInputPasswordReconfirm.getText().toString();

        //비밀번호 EditText에 아무것도 입력하지 않음.
        if(strInputPassword.equals("")){
            compareResult = false;
        }else {
            //비밀번호, 비밀번호 확인 값이 서로 같다
            if(strInputPassword.equals(strInputPasswordReconfirm)){
                compareResult = true;
            }
            //비밀번호, 비밀번호 확인 값이 서로 같지 않다.
            else{
                compareResult = false;
            }
        }

        return compareResult;
    }

    private void sendData(final String param, final String url, final int flag) {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                if (flag == CHECKID) {
                    httpConn.requestCheckId(param, callback, url);
                }else if(flag == CHECKNICKNAME){
                    httpConn.requestCheckNickname(param,callback,url);
                    setLog("보낸 별명 : "+param);
                }else if(flag == CHECKPASSWORD){
                    httpConn.requestCheckPassword(param,callback,url);
                    setLog("보낸 비번 : "+param);
                }

            }
        }.start();

    }
    private void sendDataSignup(final String id, final String password, final String nickname, final String email, final String url, final int flag){
        new Thread(){
            @Override
            public void run() {
                if(flag == SIGNUP){
                    httpConn.requestSignup(id,password,nickname,email,callback,url);
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
            if(body.equals("CHECKID_NONOVERLAP")){
                handler.sendEmptyMessage(CHECKID_POSSIBLE);
                idState = true;
                setLog("아이디 가능");
            }else if(body.equals("CHECKID_OVERLAP")){
                handler.sendEmptyMessage(CHECKID_IMPOSSIBLE);
                setLog("아이디 불가능 : 중복");
                idState = false;
            }else if(body.equals("CHECKID_INVALID")){
                handler.sendEmptyMessage(CHECKID_INVALID);
                setLog("아이디 불가능 : 유효하지 않는 형식");
                idState = false;
            }else if(body.equals("CHECKNICK_NONOVERLAP")){
                handler.sendEmptyMessage(CHECKNICKNAME_POSSIBLE);
                setLog("닉네임 가능");
                nicknameState = true;
            }else if(body.equals("CHECKNICK_OVERLAP")){
                handler.sendEmptyMessage(CHECKNICKNAME_IMPOSSIBLE);
                setLog("닉네임 불가능 : 중복");
                nicknameState = false;
            }else if(body.equals("CHECKNICK_INVALID")) {
                handler.sendEmptyMessage(CHECKNICKNAME_INVALID);
                setLog("닉네임 불가능 : 유효하지 않는 형식");
                nicknameState = false;
            }else if(body.equals("CHECKPASSWORD_VALID")){
                handler.sendEmptyMessage(CHECKPASSWORD_VALID);
                passwordState = true;
            }else if(body.equals("CHECKPASSWORD_INVALID")){
                handler.sendEmptyMessage(CHECKPASSWORD_INVALID);
                passwordState = false;
            }else if(body.equals("SIGNUP_INSERT_SUCCESS")){



                SharedPreferences currentUser = getSharedPreferences("currentUser",MODE_PRIVATE);
                SharedPreferences.Editor editor = currentUser.edit();
                editor.putBoolean("autoLogin",autoLogin);
                editor.putString("currentUser",strInputId);

                editor.commit();

                SharedPreferences spLatelyLoginId = getSharedPreferences("latelyLoginId",MODE_PRIVATE);
                SharedPreferences.Editor editorLatelyLoginId = spLatelyLoginId.edit();

                editorLatelyLoginId.putString("latelyLoginId",strInputId);
                editorLatelyLoginId.commit();

                if(HomeActivity.activity!=null){
                    HomeActivity activity = (HomeActivity) HomeActivity.activity;
                    activity.finish();
                }

                Intent intent = new Intent(SignupActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
            }else if(body.equals("SIGNUP_INSERT_FAIL")){
                Toast.makeText(SignupActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.SignupActivity_button_signup:
                etxtInputId.clearFocus();
                etxtInputPassword.clearFocus();
                etxtInputPasswordReconfirm.clearFocus();
                etxtInputNickname.clearFocus();
                etxtInputEmail.clearFocus();



                TaskSignupComplete taskSignupComplete = new TaskSignupComplete();
                taskSignupComplete.execute();
                break;
            case R.id.SignupActivity_button_email_auth:
                final String strInputEmail = etxtInputEmail.getText().toString();
                if(!android.util.Patterns.EMAIL_ADDRESS.matcher(strInputEmail).matches()){
                    txtAlertEmail.setVisibility(View.VISIBLE);
                    setLog("안맞음");

                }else {
                    setLog("맞음");
                    txtAlertEmail.setVisibility(View.GONE);
                    TaskSendMail taskSendMail = new TaskSendMail();
                    taskSendMail.execute(strInputEmail);

                    inputEmailAuth = new AlertDialog.Builder(SignupActivity.this);

                    View editboxview = LayoutInflater.from(SignupActivity.this).inflate(R.layout.dialog_email_auth,
                            null,false);
                    inputEmailAuth.setView(editboxview)
                            .setPositiveButton("완료",null)
                            .setNegativeButton("취소", null);


                    final EditText etxtInputAuth = editboxview.findViewById(R.id.emailAuth_number);
                    timeCount = editboxview.findViewById(R.id.emailAuth_time_counter);



                    /*inputEmailAuth.setPositiveButton("완료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            strEmailAuth = etxtInputAuth.getText().toString();

                            if(certificationNumber == Integer.parseInt(strEmailAuth)){
                                Toast.makeText(SignupActivity.this, "이메일 인증이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                countDownTimer.cancel();
                                dialogInterface.dismiss();
                                emailState = true;
                            }else {
                                Toast.makeText(SignupActivity.this, "유효한 인증 번호가 아닙니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                                emailState = false;
                            }


                        }
                    });
                    inputEmailAuth.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            countDownTimer.cancel();
                            dialogInterface.dismiss();
                            emailState = false;
                        }
                    });*/


                    dialogEmailAuth = inputEmailAuth.create();
                    dialogEmailAuth.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(final DialogInterface dialogInterface) {
                            Button btnComplete = dialogEmailAuth.getButton(AlertDialog.BUTTON_POSITIVE);
                            Button btnCancel = dialogEmailAuth.getButton(AlertDialog.BUTTON_NEGATIVE);
                            btnComplete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    strEmailAuth = etxtInputAuth.getText().toString();

                                    if(certificationNumber == Integer.parseInt(strEmailAuth)){
                                        Toast.makeText(SignupActivity.this, "이메일 인증이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        countDownTimer.cancel();
                                        dialogInterface.dismiss();
                                        authedEmail = strInputEmail;
                                        emailState = true;
                                    }else {
                                        Toast.makeText(SignupActivity.this, "유효한 인증 번호가 아닙니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                                        emailState = false;
                                    }
                                }
                            });
                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    countDownTimer.cancel();
                                    dialogInterface.dismiss();
                                    emailState = false;
                                }
                            });
                        }
                    });
                    dialogEmailAuth.show();
                    CountDownTimer();
                }

                break;


        }
    }
    public class TaskSignupComplete extends AsyncTask<Void,Void,Void>{
        ProgressDialog asyncDialog = new ProgressDialog(SignupActivity.this);


        @Override
        protected void onPreExecute() {
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setMessage("진행 중입니다..");
            asyncDialog.show();
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            asyncDialog.dismiss();
            ResultSignUp();
            super.onPostExecute(aVoid);
        }
    }
    public void ResultSignUp(){
        boolean authEmailChange;
        setLog("아이디 : "+idState);
        setLog("비밀번호 : "+passwordState);
        setLog("비번확인 : "+passwordReconfirmState);
        setLog("닉 : "+nicknameState);
        setLog("이메일 : "+emailState);
        setLog("이증전 메일 : "+authedEmail);
        if(authedEmail.equals("NONEAUTH")){
            Toast.makeText(this, "이메일 인증을 해주세요.", Toast.LENGTH_SHORT).show();
            emailState = false;
        }else{
            String lastCheckEmail = etxtInputEmail.getText().toString();
            if(!authedEmail.equals(lastCheckEmail)){
                Toast.makeText(this, "이메일 인증을 다시 해주세요.", Toast.LENGTH_SHORT).show();
                emailState = false;
            }
        }


        if(idState == true && passwordState == true && passwordReconfirmState == true &&
                nicknameState == true && emailState == true){
            strInputId = etxtInputId.getText().toString();
            strInputPassword = etxtInputPassword.getText().toString();
            strInputNickname = etxtInputNickname.getText().toString();
            strInputEmail = etxtInputEmail.getText().toString();

            AlertDialog.Builder autoLoginConfirm = new AlertDialog.Builder(SignupActivity.this);
            autoLoginConfirm.setTitle("자동로그인")
                    .setMessage("로그인 상태를 유지하시겠습니까?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            autoLogin = true;
                            sendDataSignup(strInputId,strInputPassword,strInputNickname,strInputEmail,"http://13.124.223.128/signup/signup.php",SIGNUP);
                        }
                    })
                    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            autoLogin = false;
                            sendDataSignup(strInputId,strInputPassword,strInputNickname,strInputEmail,"http://13.124.223.128/signup/signup.php",SIGNUP);
                        }
                    }).show();

        }else {

            Toast.makeText(this, "입력정보를 다시 확인해주세요.", Toast.LENGTH_SHORT).show();
            if(!idState){
                etxtInputId.requestFocus();
            }else if(!passwordState){
                etxtInputPassword.requestFocus();
            }else if(!passwordReconfirmState){
                etxtInputPasswordReconfirm.requestFocus();
            }else if(!nicknameState){
                etxtInputNickname.requestFocus();
            }else if(!emailState){
                etxtInputEmail.requestFocus();
                if(authedEmail.equals("NONEAUTH")){
                    Toast.makeText(this, "이메일 인증을 해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

        }
    }
    public void CountDownTimer(){
        /*countDownTimer = new CountDownTimer(300000,1000)*/
        countDownTimer = new CountDownTimer(45000,1000){
            @Override
            public void onTick(long l) {
                long emailAuthCount = l / 1000;
                setLog(emailAuthCount + "");

                if ((emailAuthCount - ((emailAuthCount / 60) * 60)) >= 10) { //초가 10보다 크면 그냥 출력
                    timeCount.setText((emailAuthCount / 60) + " : " + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                } else { //초가 10보다 작으면 앞에 '0' 붙여서 같이 출력. ex) 02,03,04...
                    timeCount.setText((emailAuthCount / 60) + " : 0" + (emailAuthCount - ((emailAuthCount / 60) * 60)));
                }
            }

            @Override
            public void onFinish() {
                dialogEmailAuth.dismiss();
                certificationNumber = 0;
            }
        }.start();
    }

    public class TaskSendMail extends AsyncTask<String,Void,Void> {



        @Override
        protected Void doInBackground(String... strings) {
            certificationNumber = MakeRandomAuth();
            GmailSender sender = new GmailSender("sinhwan0211@gmail.com","Apple00545");
            try {
                sender.sendMail(
                        "ArchiMagazine 이메일을 인증해주세요.",   //subject.getText().toString(),
                        "안녕하세요. ArchiMagazine 입니다.\n" +
                                "ArchiMagazine 회원가입 중에 등록한 이메일 주소가 올바른지 확인하기 위한 인증번호입니다. \n" +
                                "아래의 인증번호를 ArchiMagazine에 입력하여 이메일 인증을 완료해 주세요.\n\n" +
                                "인증번호 : "+certificationNumber +"\n\n" +
                                "위 인증번호는 5분 동안만 유효합니다.\n",           //body.getText().toString(),
                        "sinhwan0211@gmail.com",          //from.getText().toString(),
                        strings[0]            //to.getText().toString()
                );
            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
            }
            return null;
        }
    }
    public int MakeRandomAuth(){
        int result;
        Random rnd = new Random();
        int first = rnd.nextInt(9)+1;
        int second = rnd.nextInt(10);
        int third = rnd.nextInt(10);
        int forth = rnd.nextInt(10);
        int fifth = rnd.nextInt(10);
        int sixth = rnd.nextInt(10);
        result = first*100000 + second*10000 + third*1000 + forth*100 + fifth*10 +sixth;
        return result;
    }


}

