package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import adapter.Adapter_recordList;
import dataList.DataList_recordedList;
import okhttp3.Call;
import okhttp3.Callback;

public class RecordedListActivity extends AppCompatActivity {
    private static final String TAG = "RecordedListActivity";
    RecyclerView mRecyclerView;
    private ArrayList<DataList_recordedList> mArrayList;
    private Adapter_recordList mAdapter;

    private RequestQueue queue;

    SwipeRefreshLayout swipeRefreshLayout;
    private HttpConnection httpConn = HttpConnection.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorded_list);

        swipeRefreshLayout = findViewById(R.id.refresh);

        setLog("데이터 접속중 1 ");
        mRecyclerView = findViewById(R.id.recyclerview_recordedList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(RecordedListActivity.this));
        mArrayList = new ArrayList<>();
        setLog("데이터 접속중 2 ");
        mAdapter = new Adapter_recordList(RecordedListActivity.this,mArrayList);
        mRecyclerView.setAdapter(mAdapter);

        //------------------------------리사이클러뷰 새로고침 이벤트----------------------------

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setLog("데이터 접속중refresh 1 ");
                /*MainActivity.getBroadcastTask getBroadcastTask = new MainActivity.getBroadcastTask();
                getBroadcastTask.execute();*/

                getBroadcastList();

                swipeRefreshLayout.setRefreshing(false);
            }
        });
        setLog("데이터 접속중 1 ");
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new recyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                DataList_recordedList dataList_recordedList = mArrayList.get(position);
                setLog("인텐트로 레코드뷰에 넘기는 데이터"+dataList_recordedList.getHostNickname());

                Intent intent = new Intent(getApplicationContext(), ViewRecordedActivity.class);
                intent.putExtra("host",dataList_recordedList.getHost());
                intent.putExtra("hostNickname",dataList_recordedList.getHostNickname());
                intent.putExtra("title",dataList_recordedList.getTitle());
                intent.putExtra("RecordNumber",dataList_recordedList.getRecordNumber());
                intent.putExtra("routeVideo",dataList_recordedList.getRouteVideo());
                intent.putExtra("routeThumbnail",dataList_recordedList.getRouteThumbnail());
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
        getBroadcastList();
    }

    public void getBroadcastList(){
        setLog("데이터 접속중 3 ");
        mArrayList.clear();
        mAdapter.notifyDataSetChanged();

        setLog("onResume");

        //------------------------------서버에서 방송 목록을 가져옴----------------------------

        queue = Volley.newRequestQueue(this);
        String url = "http://13.124.223.128/recording/getRecordedList.php";
        setLog("데이터 접속중 4 ");

        sendData("테스트",url);

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                setLog("데이터 접속중 5 ");
                setLog(response);
                /*Logging.e("TAG","json2 : "+data);*/
                try {
                    JSONObject jsonObject = new JSONObject(response.replaceAll("\\P{Print}",""));
                    String data1 = jsonObject.getString("recordedList");
                    setLog("data1 : "+data1);
                    JSONArray jaBroadcastList = new JSONArray(data1);
                    /*Logging.e(TAG,"제이슨 길이 : "+jaBroadcastList.length());
                    Logging.e(TAG,"data1 : "+jaBroadcastList.getJSONObject(0));*/
                    for(int i=0;i<jaBroadcastList.length();i++){
                        setLog("어디보자 : "+jaBroadcastList.getJSONObject(i));
                        JSONObject joBroadList = jaBroadcastList.getJSONObject(i);
                        String host = joBroadList.getString("host");
                        String title = joBroadList.getString("title");
                        String hostNickname = joBroadList.getString("hostNickname");
                        int recordNumber = Integer.parseInt(joBroadList.getString("RecordNumber"));
                        String routeVideo = joBroadList.getString("routeVideo");
                        String routeThumbnail = joBroadList.getString("routeThumbnail");

                        DataList_recordedList dataList = new DataList_recordedList();
                        dataList.setHost(host);
                        dataList.setHostNickname(hostNickname);
                        dataList.setTitle(title);
                        dataList.setRecordNumber(recordNumber);
                        dataList.setRouteVideo(routeVideo);
                        dataList.setRouteThumbnail(routeThumbnail);
                        setLog("데이터 접속중 6 ");
                        mArrayList.add(dataList);
                        mAdapter.notifyDataSetChanged();
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

    @Override
    protected void onStop() {
        super.onStop();
        setLog("onStop");
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    public void onMoveStreamListActivity(View view) {
        Intent intent = new Intent(RecordedListActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

    }

    public class getBroadcastTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            getBroadcastList();
            return null;
        }
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
    private void sendData(final String param, final String url) {
// 네트워크 통신하는 작업은 무조건 작업스레드를 생성해서 호출 해줄 것!!
        new Thread() {
            public void run() {
                httpConn.requestWebServer(param, callback, url);
            }
        }.start();

    }

    public void setLog(String content){
        android.util.Log.e(TAG,content);
    }


}
