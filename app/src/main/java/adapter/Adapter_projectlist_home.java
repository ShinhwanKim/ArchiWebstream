package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.webstream.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import dataList.DataList_project_list;

public class Adapter_projectlist_home extends RecyclerView.Adapter<Adapter_projectlist_home.CustomViewHolder> {
    private static final String TAG = "Adapter_projectlist_home";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_project_list> dataList;
    private Context mContext;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView txtTitle;
        protected TextView txtOwner;
        protected ImageView imgMaster;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            this.txtTitle = itemView.findViewById(R.id.item_list_home_project_title);
            this.txtOwner = itemView.findViewById(R.id.item_list_home_project_owner);
            this.imgMaster = itemView.findViewById(R.id.item_list_home_project_image);

        }
    }
    public Adapter_projectlist_home(Context context, ArrayList<DataList_project_list> list){
        dataList = list;
        mContext = context;
    }
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_project_list_home,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        holder.txtTitle.setText(dataList.get(position).getTitle());
        setLog("데이터 검사 getOwner: "+dataList.get(position).getOwner());
        holder.txtOwner.setText("/ "+dataList.get(position).getOwner());
        final Bitmap[] bitmap = new Bitmap[1];
        Thread mThread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    setLog("확인중1");
                    URL url = new URL(dataList.get(position).getImage());
                    setLog("확인중2");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    setLog("확인중3");
                    conn.setDoInput(true);
                    setLog("확인중4");
                    conn.connect();

                    setLog("확인중5");
                    InputStream is = conn.getInputStream();
                    setLog("확인중6");
                    //BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 4;
                    //bitmap[0] = BitmapFactory.decodeStream(is,null,options);
                    bitmap[0] = BitmapFactory.decodeStream(is);
                    //Bitmap.createScaledBitmap()
                    setLog("확인중7");

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
        mThread.start();

        try {
            mThread.join();
            if(dataList.get(position).getImageOrientation().equals("6")){
                Matrix rotateMatrix = new Matrix();
                setLog("으디보자4");
                rotateMatrix.postRotate(90);
                setLog("으디보자5");
                bitmap[0] = Bitmap.createBitmap(bitmap[0],0,0,bitmap[0].getWidth(),bitmap[0].getHeight(),rotateMatrix,false);
                setLog("으디보자6");

            }
            setLog("확인중8");
            Glide.with(mContext)
                    .load(bitmap[0])
                    .into(holder.imgMaster);
            holder.imgMaster.setImageBitmap(bitmap[0]);
            setLog("확인중9");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mThread.isInterrupted();
    }
    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }
}
