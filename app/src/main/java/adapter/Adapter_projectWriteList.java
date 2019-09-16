package adapter;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webstream.ProjectWriteActivity;
import com.example.webstream.R;
import com.example.webstream.writeItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.Collections;

import dataList.DataList_chatList_broadcast;
import dataList.DataList_project_write;

public class Adapter_projectWriteList extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements writeItemTouchHelperCallback.OnItemMoveListener{

    private static final String TAG = "Adapter_projectWrite";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_project_write> datalist;
    private Context mContext;
    private OnStartDragListener mStartDragListener;

    //------------------------------아이템 옮기는 메서드----------------------------
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(datalist, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    //------------------------------아이템 드래그 앤 드랍 가능하게 하는 리스너----------------------------
    public interface OnStartDragListener{
        void onStartDrag(RecyclerView.ViewHolder holder);
    }

    //------------------------------텍스트 아이템을 위한 뷰홀더----------------------------
    public class TextViewHolder extends RecyclerView.ViewHolder{
        protected EditText etxtTitle;
        protected ImageView imgDrag;
        protected LinearLayout linearLayout;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            this.etxtTitle = itemView.findViewById(R.id.projectwrite_list_title);
            this.imgDrag = itemView.findViewById(R.id.projectwrite_list_move);
            this.linearLayout = itemView.findViewById(R.id.projectwrite_linear);

            //포커스 잃었을 때, edittext 내용이 비어 있다면 해당 텍스트 아이템을 없애버린다.
            etxtTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if(!hasFocus){
                        switch (view.getId()){
                            case R.id.projectwrite_list_title:
                                setLog("포커스잃음 어디 : "+getAdapterPosition());
                                if(etxtTitle.getText().toString().equals("")){
                                    //텍스트 아이템을 추가를 누르면서 이미 있던 텍스트 아이템이 없어질 때
                                    //java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling
                                    //에러 발생 정확한 원인 추적 했으나 언급되던 모든 해결방법이 안먹힘. 아이템 삭제와 추가가 동시에 이뤄지다 보니 그런듯.
                                    try{
                                        ProjectWriteActivity.writeDataList.remove(getAdapterPosition());
                                        notifyDataSetChanged();
                                    }catch (Exception e){
                                        setLog(e.toString());
                                    }


                                    //ProjectWriteActivity.adapter_projectWrite.notifyDataSetChanged();
                                }


                                break;
                        }
                    }

                }
            });
        }
    }
    //------------------------------이미지 아이템을 위한 뷰홀더----------------------------
    public class ImageViewHolder extends RecyclerView.ViewHolder{
        protected ImageView imgContent;
        protected ImageView imgDrag;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.imgContent = itemView.findViewById(R.id.projectwrite_imgage_list_content);
            this.imgDrag = itemView.findViewById(R.id.projectwrite_imgage_list_move);
        }
    }

    //------------------------------어댑터 생성자----------------------------
    public Adapter_projectWriteList(Context context, ArrayList<DataList_project_write> list, OnStartDragListener startDragListener){
        mStartDragListener = startDragListener;
        datalist = list;
        mContext = context;
    }

    //------------------------------뷰홀더 생성----------------------------
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case DataList_project_write.TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_project_write_text,parent,false);
                TextViewHolder textViewHolder = new TextViewHolder(view);
                return textViewHolder;
            case DataList_project_write.IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_project_write_image,parent,false);
                ImageViewHolder imageViewHolder = new ImageViewHolder(view);
                return imageViewHolder;

        }
        /*View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_project_write_text,parent,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);*/
        return null;
    }

    //------------------------------바인드----------------------------
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        //holder.etxtTitle.setText(datalist.get(position).getName());

//이미지 아이템일 때
        if(datalist.get(position).getName()==null){
            //아이템 우측 끝 move이미지를 터치지 드래그 앤 드랍 가능
            ((ImageViewHolder)holder).imgDrag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                        //setLog("터치");
                        mStartDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }
//텍스트 아이템일때
        else {
            //((TextViewHolder)holder).etxtTitle.setOnFocusChangeListener(this);


            ((TextViewHolder)holder).etxtTitle.setText(datalist.get(position).getName());

            //처음 아이템이 생성됐을 때 포커스를 주기 위함. 포커스 맨뒤로
           ((TextViewHolder)holder).etxtTitle.requestFocus();
            ((TextViewHolder)holder).etxtTitle.setSelection(((TextViewHolder)holder).etxtTitle.length());
            //아이템 우측 끝 move이미지를 터치지 드래그 앤 드랍 가능
            ((TextViewHolder)holder).imgDrag.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
                        //setLog("터치");
                        mStartDragListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
        }

//        holder.imgDrag.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
//                    setLog("터치");
//                    mStartDragListener.onStartDrag(holder);
//                }
//                return false;
//
//            }
//        });

    }

//    @Override
//    public void onFocusChange(View view, boolean hasFocus) {
//        if(!hasFocus){
//            switch (view.getId()){
//                case R.id.projectwrite_list_title:
//                    //setLog("포커스잃음");
//                    //if(datalist.get())
//                    //ProjectWriteActivity.writeDataList.remove(0);
//                    //ProjectWriteActivity.adapter_projectWrite.notifyItemChanged(ProjectWriteActivity.writeDataList.size());
//
//                    break;
//            }
//        }
//    }

    //------------------------------뷰타입 구분----------------------------
    @Override
    public int getItemViewType(int position) {
        //이미지 아이템. 데이터리스트에 name (=텍스트내용)이 없을 때.
        if(datalist.get(position).getName()==null){
            return DataList_project_write.IMAGE;
        }
        //텍스트 아이템
        else {
            return DataList_project_write.TEXT;
        }
    }
    @Override
    public int getItemCount() {
        return (null != datalist ? datalist.size() : 0);
    }
}
