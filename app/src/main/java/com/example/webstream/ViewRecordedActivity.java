package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class ViewRecordedActivity extends AppCompatActivity {

    String TAG = "ViewRecordedActivity";

    String host ;
    String title ;
    int recordNumber ;
    String routeVideo ;
    String routeThumbnail ;

    VideoView videoView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recorded);

        Intent intent = getIntent();
        host = intent.getStringExtra("host");
        title = intent.getStringExtra("title");
        recordNumber = intent.getIntExtra("RecordNumber",0);
        routeVideo = intent.getStringExtra("routeVideo");
        routeThumbnail = intent.getStringExtra("routeThumbnail");

        videoView = findViewById(R.id.videoView);
        MediaController controller = new MediaController(this);
        controller.setVisibility(View.VISIBLE);
        videoView.setMediaController(controller);
        videoView.setVideoPath(routeVideo);
        videoView.start();

        /*setLog("host : "+host);
        setLog("title : "+title);
        setLog("recordNumber : "+recordNumber);
        setLog("routeVideo : "+routeVideo);
        setLog("routeThumbnail : "+routeThumbnail);*/

    }

    public void setLog(String content){
        android.util.Log.e(TAG,content);
    }
}
