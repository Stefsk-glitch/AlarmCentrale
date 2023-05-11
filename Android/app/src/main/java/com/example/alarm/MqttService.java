package com.example.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLSocketFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public class MqttService extends Service {
    private static final String TAG = MqttService.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "MqttChannel";
    private static MqttClient client;
    private final String JSON_FILE_NAME = "data.json";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        // Create initial JSON test data
//        JSONObject jsonData = new JSONObject();
//        try {
//            jsonData.put("1", "test");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        saveJSONToFile(jsonData);

          // To clear the list
//        JSONObject jsonData = new JSONObject();
//        saveJSONToFile(jsonData);

        try {
            initMqttClient();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initMqttClient() throws MqttException {
        client = new MqttClient(
                "ssl://8a33a3616838419591e76673e87f395d.s1.eu.hivemq.cloud:8883",
                MqttClient.generateClientId(),
                new MemoryPersistence());

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName("phone");
        mqttConnectOptions.setPassword("password123@L".toCharArray());
        mqttConnectOptions.setSocketFactory(SSLSocketFactory.getDefault());
        client.connect(mqttConnectOptions);

        Log.d(TAG, "Connected: " + client.isConnected());

        client.setCallback(new MqttCallback() {

            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("client lost connection " + cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                Log.d(TAG, "message Arrived");
                String payload = new String(message.getPayload());
                Log.d(TAG, payload);

                Date date = new Date();
                Timestamp time = new Timestamp(date.getTime());

                if (payload.equals("alarm1") || payload.equals("alarm2") || payload.equals("alarm12")){
                    createJSONObjectAndSaveToJSONFile(getString(R.string.alarm) + " " + time);
                    showNotification(getString(R.string.alarm) + " " + time);
                }

                if (payload.equals("alarm1")){
                    alarm1();
                }

                if (payload.equals("alarm2")){
                    alarm2();
                }

                if (payload.equals("alarm12")){
                    alarm12();
                }

                if (payload.equals("door1door2")){
                    createJSONObjectAndSaveToJSONFile(getString(R.string.door1Door2) + " " + time);
                    showNotification(getString(R.string.door1Door2) + " " + time);

                    showNotification(payload);
                    door1door2Warning();
                }

                if (payload.equals("door1")){
                    createJSONObjectAndSaveToJSONFile(getString(R.string.door1) + " " + time);
                    showNotification(getString(R.string.door1) + " " + time);

                    showNotification(payload);
                    door1Warning();
                }

                if (payload.equals("door2")){
                    createJSONObjectAndSaveToJSONFile(getString(R.string.door2) + " " + time);
                    showNotification(getString(R.string.door2) + " " + time);

                    showNotification(payload);
                    door2Warning();
                }

                if (payload.equals("door1closed")){
                    createJSONObjectAndSaveToJSONFile(getString(R.string.door1Closed) + " " + time);
                    showNotification(getString(R.string.door1Closed) + " " + time);

                    showNotification(payload);
                    door1Closed();
                }

                if (payload.equals("door2closed")){
                    createJSONObjectAndSaveToJSONFile(getString(R.string.door2Closed) + " " + time);
                    showNotification(getString(R.string.door2Closed) + " " + time);

                    showNotification(payload);
                    door2Closed();
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("delivery complete " + token);
            }
        });

        client.subscribe("topic/alarm", 0);
    }

    private void showNotification(String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "MQTT Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("MQTT Notification")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        Notification notification = notificationBuilder.build();
        notificationManager.notify(0, notification);
    }

    @Override
    public void onDestroy() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (Exception e) {
                Log.d(TAG, "Failed to disconnect MQTT client");
            }
        }
        super.onDestroy();
    }

    private int getYellowColor() {
        return Color.rgb(255, 255, 0);
    }

    private int getWhiteColor() {
        return Color.rgb(255, 255, 255);
    }

    private int getBlackColor() {
        return Color.rgb(0, 0, 0);
    }

    private void alarm1(){
        int color = getBlackColor();
        int color1 = getYellowColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color3", color);
        broadcastIntent.putExtra("color1", color1);
        sendBroadcast(broadcastIntent);
    }

    private void alarm2(){
        int color = getBlackColor();
        int color1 = getYellowColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color3", color);
        broadcastIntent.putExtra("color2", color1);
        sendBroadcast(broadcastIntent);
    }

    private void alarm12(){
        int color = getBlackColor();
        int color1 = getYellowColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color3", color);
        broadcastIntent.putExtra("color2", color1);
        broadcastIntent.putExtra("color1", color1);
        sendBroadcast(broadcastIntent);
    }

    private void door1Warning(){
        int color = getYellowColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color1", color);
        sendBroadcast(broadcastIntent);
    }

    private void door2Warning(){
        int color = getYellowColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color2", color);
        sendBroadcast(broadcastIntent);
    }

    private void door1door2Warning(){
        int color = getYellowColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color1", color);
        broadcastIntent.putExtra("color2", color);
        sendBroadcast(broadcastIntent);
    }

    public static void fix() throws MqttException {
        client.publish("topic/androidPhone", "fixed".getBytes(UTF_8), 2,
                false);
    }

    private void door1Closed(){
        int color = getWhiteColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color2", color);
        sendBroadcast(broadcastIntent);
    }

    private void door2Closed(){
        int color = getWhiteColor();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MyReceiver.ACTION_UPDATE_BACKGROUND);
        broadcastIntent.putExtra("color2", color);
        sendBroadcast(broadcastIntent);
    }

    private void saveJSONToFile(JSONObject jsonData) {
        try {
            File file = new File("/storage/emulated/0/Documents/data", JSON_FILE_NAME);

            Log.d("stored", "/storage/emulated/0/Documents/data");

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonData.toString().getBytes());
            fos.close();
            Log.d("JSON", "JSON data saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createJSONObjectAndSaveToJSONFile(String text) {
        try {
            // Read the existing JSON data from the file
            String jsonString = loadJSONDataFromFile();
            JSONObject jsonData;

            if (jsonString.isEmpty()) {
                // If the file is empty, create a new JSON object
                jsonData = new JSONObject();
            } else {
                // If the file contains data, parse the JSON string
                jsonData = new JSONObject(jsonString);
            }

            // Check if the text already exists in the JSON object
            boolean textExists = false;
            Iterator<String> keysIterator = jsonData.keys();
            while (keysIterator.hasNext()) {
                String key = keysIterator.next();
                String value = jsonData.getString(key);
                if (value.equals(text)) {
                    textExists = true;
                    break;
                }
            }

            if (!textExists) {
                // Generate a random UUID
                UUID uuid = UUID.randomUUID();

                // Add the new data to the JSON object
                jsonData.put(uuid.toString(), text);

                // Save the updated JSON data to the file
                saveJSONToFile(jsonData);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONDataFromFile() {
        String loadedData = "";

        try {
            // Open the file using the appropriate methods
            File file = new File("/storage/emulated/0/Documents/data", JSON_FILE_NAME);
            InputStream inputStream = new FileInputStream(file);

            // Rest of your code remains the same
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String receiveString;

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                loadedData = stringBuilder.toString();
                Log.d("JSON", "JSON data loaded to List.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return loadedData;
    }
}

