package com.onzhou.module.liveroom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * @author: andy
 * @date: 2018-06-19
 **/
public class LiveRoomActivity extends AppCompatActivity {

    public static void intentStart(Context ctx) {
        Intent intent = new Intent(ctx, LiveRoomActivity.class);
        ctx.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ImageView imageView = (ImageView) findViewById(R.id.iv_main);
        Glide.with(this).load("http://cdn.byhook.cn/0fc87e6251577216b34ec31f1f76ffdd-320_320.jpg").into(imageView);
    }
}
