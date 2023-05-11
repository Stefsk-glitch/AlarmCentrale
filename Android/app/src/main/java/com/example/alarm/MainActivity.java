package com.example.alarm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {

    private ImageView door1;
    private ImageView door2;
    private ImageView alarm;
    private static TextView textView1;
    private static TextView textView2;
    private MyReceiver receiver;
    private boolean mqttServiceStarted = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        door1 = (ImageView) findViewById(R.id.imgDoor1);
        door2 = (ImageView) findViewById(R.id.imgDoor2);
        alarm = (ImageView) findViewById(R.id.imageViewAlarm);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);

        // Register BroadcastReceiver
        receiver = new MyReceiver(door1, door2, alarm);
        IntentFilter filter = new IntentFilter(MyReceiver.ACTION_UPDATE_BACKGROUND);
        registerReceiver(receiver, filter);

        // Start the MqttService only if it hasn't been started before
        if (!mqttServiceStarted) {
            // Create an intent to start the service
            Intent serviceIntent = new Intent(this, MqttService.class);

            // Start the service with the intent
            startService(serviceIntent);

            mqttServiceStarted = true; // Update the flag to indicate that the service has been started
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister BroadcastReceiver
        unregisterReceiver(receiver);
    }

    public void btnOnInfo(View view){
        openActivity2();
    }

    public void btnOnAlarm(View view) throws MqttException {
        MqttService.fix();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void openActivity2(){
        Intent intent = new Intent(this, MainActivity2.class);
        startActivity(intent);
    }
}