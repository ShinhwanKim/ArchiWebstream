package com.example.webstream;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class Main2Activity extends AppCompatActivity {
VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        videoView = findViewById(R.id.videoView);
        MediaController controller = new MediaController(this);
        controller.setVisibility(View.VISIBLE);
        videoView.setMediaController(controller);
        videoView.setVideoPath("http://15.164.121.33/good.mp4");
        videoView.start();

    }
}
