package com.example.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.ImageView;

public class MyReceiver extends BroadcastReceiver {
    public static final String ACTION_UPDATE_BACKGROUND = "UPDATE_BACKGROUND";

    private ImageView door1;
    private ImageView door2;
    private ImageView alarm;

    public MyReceiver(ImageView door1, ImageView door2, ImageView alarm) {
        this.door1 = door1;
        this.door2 = door2;
        this.alarm = alarm;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(ACTION_UPDATE_BACKGROUND)) {
            int color1 = intent.getIntExtra("color1", Color.WHITE);
            int color2 = intent.getIntExtra("color2", Color.WHITE);
            int color3 = intent.getIntExtra("color3", Color.WHITE);
            door1.setBackgroundColor(color1);
            door2.setBackgroundColor(color2);
            alarm.setBackgroundColor(color3);
        }
    }
}
