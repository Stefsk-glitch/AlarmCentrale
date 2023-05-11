package com.example.alarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.*;

public class MainActivity2 extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private static List<String> dataList;
    private final String JSON_FILE_NAME = "data.json";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        recyclerView = findViewById(R.id.recyclerView);

        List<String> loadedData = loadJSONDataToList();
        for (String value : loadedData) {
            Log.d("Value", value);
        }
    }

    public void btnBackOnClick(View view){
        openActivity1();
    }

    private void openActivity1(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        for (String value : dataList) {
            Log.d("PAUSE", value);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update dataList when the activity resumes
        List<String> dataList = new ArrayList<>();
        List<String> loadedData = loadJSONDataToList();

        if (loadedData != null) {
            Collections.reverse(loadedData);
            dataList.addAll(loadedData);
        }

        adapter = new MyAdapter(dataList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private List<String> loadJSONDataToList() {
        List<String> loadedData = new ArrayList<>();

        try {
            File file = new File("/storage/emulated/0/Documents/data", JSON_FILE_NAME);
            InputStream inputStream = new FileInputStream(file);
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String receiveString;

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                String jsonString = stringBuilder.toString();
                JSONObject jsonObject = new JSONObject(jsonString);

                // Extract values from the JSON object and add them to the loadedData list
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = jsonObject.getString(key);
                    loadedData.add(value);
                }

                Log.d("JSON", "JSON data loaded to List.");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return loadedData;
    }
}
