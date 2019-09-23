package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.webstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import dataList.DataList_project_view;
import dataList.DataList_project_write;

public class Adapter_projectView extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = "Adapter_projectView";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_project_view> datalist;
    private Context mContext;
    Bitmap bitmap;

    public class TextViewHolder extends RecyclerView.ViewHolder{
        protected TextView txtContent;

        public TextViewHolder(@NonNull View itemView){
            super(itemView);
            this.txtContent = itemView.findViewById(R.id.item_list_viewboard_text);

        }
    }
    public class ImageViewHolder extends RecyclerView.ViewHolder{
        protected ImageView imgContent;

        public ImageViewHolder(@NonNull View itemView){
            super(itemView);
            this.imgContent = itemView.findViewById(R.id.item_list_viewboard_image);

        }
    }
    public Adapter_projectView(Context context, ArrayList<DataList_project_view> list){
        datalist = list;
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case DataList_project_view.TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_viewboard_text,parent,false);
                TextViewHolder textViewHolder = new TextViewHolder(view);
                return textViewHolder;
            case DataList_project_view.IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_viewboard_image,parent,false);
                ImageViewHolder imageViewHolder = new ImageViewHolder(view);
                return imageViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        setLog("추적 1");
        if(datalist.get(position).getName()==null){
            setLog("추적 2 : "+datalist.get(position).getPosition());
            Thread mThread = new Thread(){
                @Override
                public void run() {
                    super.run();
                    try {
                        setLog("추적 4");
                        URL url = new URL(datalist.get(position).getImgUrl());
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setDoInput(true);
                        conn.connect();
                        InputStream is = conn.getInputStream();
                        bitmap = BitmapFactory.decodeStream(is);
                        if(datalist.get(position).getPosition()==6){
                            Matrix rotateMatrix = new Matrix();
                            setLog("으디보자4");
                            rotateMatrix.postRotate(90);
                            setLog("으디보자5");
                            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),rotateMatrix,false);
                            setLog("으디보자6");
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            mThread.start();

            try {
                setLog("추적 5");
                mThread.join();
                Glide.with(mContext)
                        .load(bitmap)
                        .apply(new RequestOptions().centerCrop())
                        .into(((ImageViewHolder)holder).imgContent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mThread.isInterrupted();




        }else {
            setLog("추적 6");
            ((TextViewHolder)holder).txtContent.setText(datalist.get(position).getName());
        }
    }
    @Override
    public int getItemViewType(int position) {
        //이미지 아이템. 데이터리스트에 name (=텍스트내용)이 없을 때.
        if(datalist.get(position).getName()==null){
            return DataList_project_view.IMAGE;
        }
        else {
            return DataList_project_view.TEXT;
        }
    }

    @Override
    public int getItemCount() {
        return (null != datalist ? datalist.size() : 0);
    }
}
