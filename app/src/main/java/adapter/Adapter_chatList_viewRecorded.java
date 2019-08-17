package adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webstream.R;

import java.util.ArrayList;

import dataList.DataList_chatList_recorded;
import dataList.DataList_chatList_viewStream;
/*
 * 녹화 방송 시청 중 채팅 내용을 출력해주는 어댑터
 * 방송자의 닉네임을 어댑터 생성자에서 받아 채팅 보낸사람이 방송자인지 구분해준다
 * 구분 기준은 방송작의 닉네임과 동일한지.
 * 방송자라면 채팅내용과 닉네임이 초록색으로 출력되게 한다.
 * */
public class Adapter_chatList_viewRecorded extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "Adapter_viewRecorded";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_chatList_recorded> dataList;
    private Context mContext;
    static String hostNick;                                            //방송자의 채팅 내용을 초록색으로 출력시키기 위해 방송자의 닉네임을 받아 놓는다.

    //생성자
    public Adapter_chatList_viewRecorded(Context context, ArrayList<DataList_chatList_recorded> list, String hostNickname){
        hostNick = hostNickname;
        setLog("어댑터에서 받은 호스트 닉 : "+hostNick);
        dataList = list;
        mContext = context;
    }

    //방송자용 뷰홀더
    public class ViewHolderHost extends RecyclerView.ViewHolder{
        protected TextView txtId;
        protected TextView txtContent;
        protected TextView txtSplit;

        public ViewHolderHost(@NonNull View itemView) {
            super(itemView);
            this.txtId = itemView.findViewById(R.id.item_list_chat_host_id);
            this.txtContent = itemView.findViewById(R.id.item_list_chat_host_content);
            this.txtSplit = itemView.findViewById(R.id.item_list_chat_host_split);
        }
    }
    //시청자용 뷰홀더
    public class ViewHolderViewer extends RecyclerView.ViewHolder{
        protected TextView txtId;
        protected TextView txtContent;
        protected TextView txtSplit;

        public ViewHolderViewer(@NonNull View itemView) {
            super(itemView);
            this.txtId = itemView.findViewById(R.id.item_list_chat_id);
            this.txtContent = itemView.findViewById(R.id.item_list_chat_content);
            this.txtSplit = itemView.findViewById(R.id.item_list_chat_split);
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view;
        switch (viewType){
            //채팅 보낸사람이 방송자일 때
            case DataList_chatList_recorded.HOST:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_chat_host,viewGroup,false);
                ViewHolderHost viewHolderHost = new ViewHolderHost(view);
                return viewHolderHost;
            //채팅 보낸사람이 시청자일 때
            case DataList_chatList_recorded.VIEWER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_chat,viewGroup,false);
                ViewHolderViewer viewHolderViewer = new ViewHolderViewer(view);
                return viewHolderViewer;

        }
        return null;
    }
    @Override
    public int getItemViewType(int position) {
        if(dataList.get(position).getNickname().equals(this.hostNick)){
            return DataList_chatList_recorded.HOST;
        }else {
            return DataList_chatList_recorded.VIEWER;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(dataList.get(position).getNickname().equals(hostNick)){
            ((ViewHolderHost)holder).txtId.setText(dataList.get(position).getNickname());
            ((ViewHolderHost)holder).txtContent.setText(dataList.get(position).getContent());
        }else {
            ((ViewHolderViewer)holder).txtId.setText(dataList.get(position).getNickname());
            ((ViewHolderViewer)holder).txtContent.setText(dataList.get(position).getContent());
        }

    }
    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }
}
