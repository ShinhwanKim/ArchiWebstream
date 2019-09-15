package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import adapter.Adapter_liveList;
import adapter.Adapter_projectWriteList;
import dataList.DataList_liveList;
import dataList.DataList_project_write;

public class ProjectWriteActivity extends AppCompatActivity{

    private static final String TAG = "ProjectWriteActivity";
    public void setLog(String content){
        Log.d(TAG,content);}

    Button btnCancel;
    Button btnAddImage;
    Button btnComplete;
    TextInputEditText etxtTitle;
    TextInputEditText etxtOwner;
    TextInputEditText etxtLocation;
    TextView txtContent;

    private RecyclerView recyProjectWriteList;
    private ArrayList<DataList_project_write> writeDataList;
    private Adapter_projectWriteList adapter_projectWrite;


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


        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.ProjectWirteActivity_button_cancel:
                        setLog("취소버튼");
                        break;
                    case R.id.ProjectWirteActivity_button_addimage:
                        setLog("이미지추가버튼");
                        break;
                    case R.id.ProjectWirteActivity_button_complete:
                        setLog("작성완료버튼");
                        break;
                    case R.id.ProjectWirteActivity_textview_content:
                        setLog("내용텍스트");
                        break;
                }
            }
        };

        btnCancel.setOnClickListener(onClickListener);
        btnAddImage.setOnClickListener(onClickListener);
        btnComplete.setOnClickListener(onClickListener);
        txtContent.setOnClickListener(onClickListener);

        recyProjectWriteList = findViewById(R.id.ProjectWirteActivity_recyclerview);
        recyProjectWriteList.setLayoutManager(new LinearLayoutManager(ProjectWriteActivity.this));
        writeDataList = new ArrayList<>();

        adapter_projectWrite = new Adapter_projectWriteList(ProjectWriteActivity.this,writeDataList);
        recyProjectWriteList.setAdapter(adapter_projectWrite);



    }



}
