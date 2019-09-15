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

import dataList.DataList_liveList;
import dataList.DataList_project_write;

public class Adapter_projectWriteList extends RecyclerView.Adapter<Adapter_projectWriteList.CustomViewHolder> {
    private static final String TAG = "Adapter_projectWrite";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_project_write> datalist;
    private Context mContext;

    public class CustomViewHolder extends RecyclerView.ViewHolder{
        protected TextView txtTitle;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtTitle = itemView.findViewById(R.id.projectwrite_list_title);
        }
    }
    public Adapter_projectWriteList(Context context, ArrayList<DataList_project_write> list){
        datalist = list;
        mContext = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_list_project_write,parent,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        holder.txtTitle.setText(datalist.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return (null != datalist ? datalist.size() : 0);
    }
}
