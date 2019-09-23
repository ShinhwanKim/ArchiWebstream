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

import dataList.DataList_liveList;
import dataList.DataList_project_list;

public class Adapter_projectList extends RecyclerView.Adapter<Adapter_projectList.CustomViewHolder>{
    private static final String TAG = "Adapter_projectList";
    public void setLog(String content){android.util.Log.e(TAG,content);}

    private ArrayList<DataList_project_list> mList;
    private Context mContext;

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected LinearLayout linearContent;
        protected TextView txtTitle;
        protected TextView txtOwner;
        protected TextView txtLocation;
        protected TextView txtWrittenDate;
        protected TextView txtView;
        protected TextView txtLike;
        protected ImageView imgMaster;

        public CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            linearContent = itemView.findViewById(R.id.item_list_project_list_linear);
            this.txtTitle = itemView.findViewById(R.id.item_list_project_text_title);
            this.txtOwner = itemView.findViewById(R.id.item_list_project_text_owner);
            this.txtLocation = itemView.findViewById(R.id.item_list_project_text_location);
            this.txtWrittenDate = itemView.findViewById(R.id.item_list_project_text_writtendate);
            this.imgMaster = itemView.findViewById(R.id.item_list_project_image);
            this.txtView = itemView.findViewById(R.id.item_list_project_text_view);
            this.txtLike = itemView.findViewById(R.id.item_list_project_text_like);

        }
    }
    public Adapter_projectList(Context context, ArrayList<DataList_project_list> list){
        mList = list;
        mContext = context;
    }
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_project_list,viewGroup,false);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
        String[] result = mList.get(position).getWrittenDate().split(" ");
        String writtenDate =result[0];
        setLog("데이터 검사 getTitle: "+mList.get(position).getTitle()+"  /  ");
        holder.txtTitle.setText(mList.get(position).getTitle()+"  /  ");
        setLog("데이터 검사 getOwner: "+mList.get(position).getOwner());
        holder.txtOwner.setText(mList.get(position).getOwner());
        setLog("데이터 검사 getLocation: "+mList.get(position).getLocation());
        holder.txtLocation.setText("in  "+mList.get(position).getLocation());
        setLog("데이터 검사 getWrittenDate: "+writtenDate);
        holder.txtWrittenDate.setText(writtenDate);

        setLog("데이터 검사 getView: "+mList.get(position).getView());
        holder.txtView.setText(String.valueOf(mList.get(position).getView()));
        setLog("데이터 검사 getWrittenDate: "+mList.get(position).getLike());
        holder.txtLike.setText(String.valueOf(mList.get(position).getLike()));
        final Bitmap[] bitmap = new Bitmap[1];
        Thread mThread = new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    setLog("확인중1");
                    URL url = new URL(mList.get(position).getImage());
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
            if(mList.get(position).getImageOrientation().equals("6")){
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
        return (null != mList ? mList.size() : 0);
    }
}
