package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.webstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import dataList.DataList_liveList;

public class Adapter_liveList extends RecyclerView.Adapter<Adapter_liveList.CustomViewHolder> {
    private static final String TAG = "Adapter_liveList";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_liveList> mList;
    private Context mContext;
    Bitmap bitmap;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtTitle;
        protected TextView txtHost;
        protected TextView txtViewer;
        protected ImageView imgThumbnail;
        protected ImageView imgLock;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtTitle = itemView.findViewById(R.id.livelist_title);
            this.txtHost = itemView.findViewById(R.id.livelist_host);
            this.imgThumbnail = itemView.findViewById(R.id.livelist_img_thumbnail);
            this.imgLock = itemView.findViewById(R.id.livelist_img_lock);
            this.txtViewer = itemView.findViewById(R.id.livelist_viewer);

        }
    }
    public Adapter_liveList(Context context, ArrayList<DataList_liveList> list){
        mList = list;
        mContext = context;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_livelist,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder,final int position) {
        holder.txtTitle.setText("[생방송]"+mList.get(position).getTitle());
        holder.txtHost.setText(mList.get(position).getHostNickname());
        holder.txtViewer.setText(String.valueOf(mList.get(position).getViewer()));

        if(!mList.get(position).getPassword().equals("")){
            holder.imgLock.setVisibility(View.VISIBLE);
        }
        Thread mThread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(mList.get(position).getRouteThumbnail());
                    setLog("error 1");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    setLog("error 2");
                    conn.setDoInput(true);
                    setLog("error 3");
                    conn.connect();
                    setLog("error 4");

                    InputStream is = conn.getInputStream();
                    setLog("error 5");
                    bitmap = BitmapFactory.decodeStream(is);
                    setLog("error 6");

                    /*Thread.sleep(1000);*/
                    /*setLog("얼씨구씨구 돌아간다." + mList.get(position).getTitle());*/
                } catch (MalformedURLException e) {
                    setLog("error 7");
                    e.printStackTrace();
                } catch (IOException e) {
                    setLog("error 8");
                    e.printStackTrace();
                }
            }
        };
        setLog("error 9");
        mThread.start();

        try {
            mThread.join();
            holder.imgThumbnail.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mThread.isInterrupted();
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }


}
