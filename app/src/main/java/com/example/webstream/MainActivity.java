package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
이 액티비티는 현재 방송중인 Stream 목록을 출력하고, 방송하기 버튼을 눌러 BroadcastActivity로 이동하는 액티비티이다.
Stream목록을 가져올 때는 Volley 라이브러리를 이용하여 ArchiApp 서버와 통신한다.
*/
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /*Button btnViewStream;
    Button btnViewStream2;*/
    Button btnBroadStream;                                   //방송하기 버튼. 클릭 시 BroadcastActivity로 이동
    Button btnRecordedList;                                  //녹화 방송 목록애티비티로 이동 버튼

    RecyclerView recyclerview_broadcastList1;                              //방송 목록을 보여주는 리사이클러뷰
    private ArrayList<DataList_broadcastList> mArrayList;    //방송 목록을 보여주는 리사이클러뷰에 들어갈 데이터 리스트
    private CustomAdapter adapter_broadcastList;                          //방송 목록을 보여주는 리사이클러뷰에 적용될 어댑터

    private RequestQueue queue;                              //volley 쓸 때 사용

    SwipeRefreshLayout swipeRefreshLayout;                    //당겨서 새로고침 할 때 쓰는 스와이프 레이아웃



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLog("onCreate");

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //------------------------------뷰들 정의----------------------------
        /*btnViewStream = findViewById(R.id.btn_viewStream);*/
        /*btnViewStream2 = findViewById(R.id.btn_viewStream2);*/
        btnBroadStream = findViewById(R.id.btn_broadStream);
        btnRecordedList = findViewById(R.id.btn_RecordedList);
        swipeRefreshLayout = findViewById(R.id.refresh);

        //------------------------------리사이클러뷰 정의----------------------------
        RecyclerView recyclerview_broadcastList = findViewById(R.id.recyclerview_broadcastList);
        recyclerview_broadcastList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mArrayList = new ArrayList<>();

        adapter_broadcastList = new CustomAdapter(MainActivity.this,mArrayList);
        recyclerview_broadcastList.setAdapter(adapter_broadcastList);



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


        //------------------------------방송하기 버튼 이벤트----------------------------
        //방송하기 버튼을 누르면 방송 데이터를 입력할 수 있는 다이얼로그가 뜬다.

        btnBroadStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Intent broadcastIntent = new Intent(getApplicationContext(),SetBroadcastActivity.class);
                startActivity(broadcastIntent);*/

                final AlertDialog.Builder inputBroadcastInfo = new AlertDialog.Builder(MainActivity.this);
                View editboxview = LayoutInflater.from(MainActivity.this).inflate(R.layout.editbox_broadcast2,
                        null,false);
                inputBroadcastInfo.setView(editboxview);


                final EditText etextTitle = editboxview.findViewById(R.id.editboxBroadcast_editText_title);
                final EditText etextPassword = editboxview.findViewById(R.id.editboxBroadcast_editText_password);


                //------------------------------다이얼로그 완료 버튼 이벤트----------------------------
                inputBroadcastInfo.setPositiveButton("완료", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String dataTitle = etextTitle.getText().toString();
                        String dataPassword = etextPassword.getText().toString();


                        dialog.dismiss();

                        Intent broadcastIntent = new Intent(getApplicationContext(),BroadcastActivity.class);
                        broadcastIntent.putExtra("title",dataTitle);
                        broadcastIntent.putExtra("password",dataPassword);
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
        });

        btnRecordedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordedListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });

        /*btnViewStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ViewStreamActivity.class);
                startActivity(intent);
            }
        });

        btnViewStream2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewStreamActivity2.class);
                startActivity(intent);
            }
        });*/


        //------------------------------리사이클러뷰 아이템 터치 이벤트----------------------------

        recyclerview_broadcastList.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerview_broadcastList, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //선택한 아이템의 stream 데이터를 ViewStreamActivity로 넘겨준다.
                DataList_broadcastList dataList_broadcastList = mArrayList.get(position);

                Intent intent = new Intent(getApplicationContext(), ViewStreamActivity.class);
                intent.putExtra("title",dataList_broadcastList.getTitle());
                intent.putExtra("number",dataList_broadcastList.getNumber());
                intent.putExtra("host",dataList_broadcastList.getHost());
                intent.putExtra("routeStream",dataList_broadcastList.getRouteStream());
                startActivity(intent);

                /*Toast.makeText(MainActivity.this, dataList_broadcastList.getTitle(), Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


    }

    @Override
    protected void onResume() {
        super.onResume();

        getBroadcastList();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setLog("onStop");
        if (queue != null) {
            queue.cancelAll(TAG);
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

    public void getBroadcastList(){
        mArrayList.clear();
        adapter_broadcastList.notifyDataSetChanged();

        setLog("onResume");


        //------------------------------서버에서 방송 목록을 가져옴----------------------------

        queue = Volley.newRequestQueue(this);
        String url = "http://13.124.223.128/broadcast/getBroadcastList.php";

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
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
                        String routeThumbnail = joBroadList.getString("routeThumbnail");
                        String routeStream = joBroadList.getString("routeStream");

                        DataList_broadcastList dataList = new DataList_broadcastList();
                        dataList.setHost(host);
                        dataList.setTitle(title);
                        dataList.setNumber(number);
                        dataList.setRouteThumbnail(routeThumbnail);
                        dataList.setRouteStream(routeStream);

                        mArrayList.add(dataList);
                        adapter_broadcastList.notifyDataSetChanged();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        stringRequest.setTag(TAG);
        queue.add(stringRequest);
    }
    public void setLog(String content){
        android.util.Log.e(TAG,content);
        //private static final String TAG = "MainActivity";
    }

    public class getBroadcastTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            getBroadcastList();
            return null;
        }
    }


}
