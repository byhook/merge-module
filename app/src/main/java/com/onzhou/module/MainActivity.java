package com.onzhou.module;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.onzhou.module.download.DownloadActivity;
import com.onzhou.module.liveroom.LiveRoomActivity;
import com.onzhou.module.upload.UploadActivity;
import com.onzhou.module.video.VideoPlayActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onDownloadClick(View view) {
        DownloadActivity.intentStart(this);
    }

    public void onUploadClick(View view) {
        UploadActivity.intentStart(this);
    }


    public void onLiveClick(View view) {

        LiveRoomActivity.intentStart(this);
    }

    public void onVideoClick(View view) {
        VideoPlayActivity.intentStart(this);
    }
}
