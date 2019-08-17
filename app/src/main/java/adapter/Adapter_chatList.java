package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webstream.R;

import java.util.ArrayList;

import dataList.DataList_chatList_broadcast;

/*
* 실시간 스트리밍 방송 중 채팅 내용을 출력해주는 어댑터
* 방송자의 닉네임을 어댑터 생성자에서 받아 채팅 보낸사람이 방송자인지 구분해준다
* 구분 기준은 방송작의 닉네임과 동일한지.
* 방송자라면 채팅내용과 닉네임이 초록색으로 출력되게 한다.
* */

public class Adapter_chatList extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "Adapter_chatList";
    public static void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_chatList_broadcast> dataList;
    private Context mContext;
    private String loginedUser;
    private String hostUser;
    private String hostNick;

    public Adapter_chatList(Context context, ArrayList<DataList_chatList_broadcast> list, String hostNickname){
        hostNick = hostNickname;
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
    //시청자용 뷰홀더더
    public class ViewHolderViewer extends RecyclerView.ViewHolder{
        protected TextView txtId;
        protected TextView txtContent;
        protected TextView txtSplit;
        private DataList_chatList_broadcast dataSet=null;

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
            //방송자용 뷰홀더
            case DataList_chatList_broadcast.HOST:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_chat_host,viewGroup,false);
                ViewHolderHost viewHolderHost = new ViewHolderHost(view);
                return viewHolderHost;
            //시청자용 뷰홀더
            case DataList_chatList_broadcast.VIEWER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_chat,viewGroup,false);
                ViewHolderViewer viewHolderViewer = new ViewHolderViewer(view);
                return viewHolderViewer;
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //채팅 보낸 사람이 방송자일 때
        if(dataList.get(position).getNickname().equals(hostNick)){
            ((ViewHolderHost)holder).txtId.setText(dataList.get(position).getNickname());
            ((ViewHolderHost)holder).txtContent.setText(dataList.get(position).getContent());
        }
        //
        else {
            ((ViewHolderViewer)holder).txtId.setText(dataList.get(position).getNickname());
            ((ViewHolderViewer)holder).txtContent.setText(dataList.get(position).getContent());
        }


        //holder.setColor(dataList.get(position));

        setLog("챗 어댑터 적용 / 호스트 닉네임 : "+hostNick);
        setLog("챗 어댑터 적용 / 호스트 아이디 : "+(dataList.get(position).getNickname()));
        setLog("챗 어댑터 적용 / 호스트 내용 : "+(dataList.get(position).getContent()));

        /*//채팅 메시지가 나일 때
        if(hostNick.equals(dataList.get(position).getNickname())){
            setLog("초록색");
            holder.txtId.setTextColor(Color.parseColor("#3ED641"));
            holder.txtContent.setTextColor(Color.parseColor("#3ED641"));
            holder.txtSplit.setTextColor(Color.parseColor("#3ED641"));
        }*/

    }

    @Override
    public int getItemViewType(int position) {
        if(dataList.get(position).getNickname().equals(this.hostNick)){
            return DataList_chatList_broadcast.HOST;
        }else {
            return DataList_chatList_broadcast.VIEWER;
        }
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }


}
