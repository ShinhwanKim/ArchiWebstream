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

import dataList.DataList_recordedList;
import com.example.webstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Adapter_recordList extends RecyclerView.Adapter<Adapter_recordList.CustomViewHolder>{
    private static final String TAG = "RecordedListActivity";
    private ArrayList<DataList_recordedList> mList;
    private Context mContext;

    Bitmap bitmap;

    public class  CustomViewHolder extends RecyclerView.ViewHolder{
        protected TextView txtTitle;
        protected TextView txtHost;
        protected ImageView imgThumbnail;

        public CustomViewHolder(View view) {
            super(view);
            setLog("데이터 접속중 7 ");
            this.txtTitle = (TextView) view.findViewById(R.id.txt_recordedList_title);
            this.txtHost = (TextView) view.findViewById(R.id.txt_recordedList_host);
            this.imgThumbnail = view.findViewById(R.id.img_recordedList_imageView);

        }

    }
    public Adapter_recordList(Context context, ArrayList<DataList_recordedList> list){
        setLog("데이터 접속중 8 ");
        mList = list;
        mContext = context;
    }
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        setLog("데이터 접속중 9 ");
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_recordedlist,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        setLog("데이터 접속중 10 ");
        holder.txtTitle.setText("[녹화방송]"+mList.get(position).getTitle());
        setLog("데이터 접속중 11 ");
        holder.txtHost.setText(mList.get(position).getHost());
        setLog("데이터 접속중 12 ");
        Thread mThread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    URL url = new URL(mList.get(position).getRouteThumbnail());
                    setLog("데이터 접속중 13 ");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    setLog("데이터 접속중 14 ");
                    conn.setDoInput(true);
                    setLog("데이터 접속중 15 ");
                    conn.connect();
                    setLog("데이터 접속중 16 ");
                    InputStream is = conn.getInputStream();
                    setLog("데이터 접속중 17 ");
                    bitmap = BitmapFactory.decodeStream(is);
                    setLog("데이터 접속중 18 ");
                    /*Thread.sleep(1000);*/
                    /*setLog("얼씨구씨구 돌아간다." + mList.get(position).getTitle());*/
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        setLog("데이터 접속중 19 ");
        mThread.start();
        setLog("데이터 접속중 20 ");
        try {
            mThread.join();
            holder.imgThumbnail.setImageBitmap(bitmap);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setLog("데이터 접속중 21 ");
        mThread.isInterrupted();
        setLog("데이터 접속중 22 ");
    }
    @Override
    public int getItemCount() {

        return (null != mList ? mList.size() : 0);
    }

    public void setLog(String content){
        android.util.Log.e(TAG,content);
    }
}
