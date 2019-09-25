package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.webstream.ProjectSubscribeActivity;
import com.example.webstream.R;
import com.example.webstream.UserChannelProjectActivity;

import java.util.ArrayList;

import dataList.DataList_liveList;
import dataList.DataList_subscriber_list;

public class Adapter_subscribelist extends RecyclerView.Adapter<Adapter_subscribelist.CustomViewHolder> {
    private static final String TAG = "Adapter_subscribelist";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_subscriber_list> dataList;
    private Context mContext;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtID;
        protected ImageView imgProfile;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            setLog("작업중 1");
            this.txtID = itemView.findViewById(R.id.item_list_subscribelist_text_id);
            this.imgProfile = itemView.findViewById(R.id.item_list_subscribelist_image_profile);

        }
    }
    public Adapter_subscribelist(Context context, ArrayList<DataList_subscriber_list> list){
        setLog("작업중 2");
        dataList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        setLog("작업중 3");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_subscriberlist,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        setLog("작업중 4");
        holder.txtID.setText(dataList.get(position).getId());
//        Glide.with(mContext)
//                .load(R.drawable.user_profile_default_white)
//                .apply(new RequestOptions().circleCrop())
//                .into(holder.imgProfile);
        if(dataList.get(position).getProfile().equals("null")){
            Glide.with(mContext)
                    .load(R.drawable.user_profile_default_white)
                    .apply(new RequestOptions().circleCrop())
                    .into(holder.imgProfile);
        }else {
            Glide.with(mContext)
                    .load(dataList.get(position).getProfile())
                    .apply(new RequestOptions().circleCrop())
                    .into(holder.imgProfile);
        }
    }
    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }
}
