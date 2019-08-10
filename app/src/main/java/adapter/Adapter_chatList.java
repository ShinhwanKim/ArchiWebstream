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

import dataList.DataList_chatList_broadcast;

public class Adapter_chatList extends RecyclerView.Adapter<Adapter_chatList.CustomViewHolder>{
    private static final String TAG = "Adapter_chatList";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_chatList_broadcast> dataList;
    private Context mContext;
    private String loginedUser;
    private String hostUser;
    private String hostNick;


    public class CustomViewHolder extends RecyclerView.ViewHolder{
        protected TextView txtId;
        protected TextView txtContent;
        protected TextView txtSplit;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtId = itemView.findViewById(R.id.item_list_chat_id);
            this.txtContent = itemView.findViewById(R.id.item_list_chat_content);
            this.txtSplit = itemView.findViewById(R.id.item_list_chat_split);
        }
    }

    public Adapter_chatList(Context context, ArrayList<DataList_chatList_broadcast> list, String hostNickname){
        hostNick = hostNickname;
        dataList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_chat,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.txtId.setText(dataList.get(position).getNickname());
        holder.txtContent.setText(dataList.get(position).getContent());

        setLog("챗 어댑터 적용 / 호스트 닉네임 : "+hostNick);
        setLog("챗 어댑터 적용 / 호스트 아이디 : "+(dataList.get(position).getNickname()));
        setLog("챗 어댑터 적용 / 호스트 내용 : "+(dataList.get(position).getContent()));

        //채팅 메시지가 나일 때
        if(hostNick.equals(dataList.get(position).getNickname())){
            setLog("초록색");
            holder.txtId.setTextColor(Color.parseColor("#3ED641"));
            holder.txtContent.setTextColor(Color.parseColor("#3ED641"));
            holder.txtSplit.setTextColor(Color.parseColor("#3ED641"));
        }
    }
    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }
}
