package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import adapter.Adapter_liveList;
import adapter.Adapter_projectWriteList;
import dataList.DataList_liveList;
import dataList.DataList_project_write;

public class ProjectWriteActivity extends AppCompatActivity
    implements Adapter_projectWriteList.OnStartDragListener{

    private static final String TAG = "ProjectWriteActivity";
    public void setLog(String content){
        Log.d(TAG,content);}

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
                        DataList_project_write ImageDataList = new DataList_project_write();
                        writeDataList.add(ImageDataList);
                        //adapter_projectWrite.notifyDataSetChanged();
                        adapter_projectWrite.notifyItemChanged(writeDataList.size());
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
                        else {
                            setLog("내용 : "+etxtTitle.getText());
                            setLog("작성완료버튼");
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


    @Override
    public void onStartDrag(RecyclerView.ViewHolder holder) {
        itemTouchHelper.startDrag(holder);
    }
}
